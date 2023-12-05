package com.sy.cc.multicast;

import com.alibaba.fastjson2.JSONObject;
import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.emuns.MessageTypeEnum;
import com.sy.cc.comm.entity.*;

import com.sy.cc.http.ServerHttp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class UdpMulticast {

    private static final Logger logger = LoggerFactory.getLogger(UdpMulticast.class);
    private static String GROUPIP = "225.1.2.2";
    // 组播端口号
    private static int GROUPPORT = 5678;
//    // 本机地址
//    private static final String localIp = "172.32.1.113";

    private static int TIMEOUT = 4000;
    private static DatagramChannel CHANNEL;

    private static InetSocketAddress GROUPADDRESS;

    private static InetAddress localAddress = null;

    private static String address;

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String add) {
        address = add;
    }

    public static InetAddress getLocalAddress() {
        return localAddress;
    }

    public static DatagramChannel getCHANNEL() {
        return CHANNEL;
    }

    public static InetSocketAddress getGROUPADDRESS() {
        return GROUPADDRESS;
    }






    public static void buildMulticast() throws Exception {
        // 组播地址
        // 组播地址
        boolean hasEpoll = false;

        StaskServer configServer = ConfigBase.getStaskServer();
        if (configServer != null) {
            StaskInfo staskServer = configServer.getStaskServer();
            if (staskServer != null) {
                String address = staskServer.getAddress();
                if (!StringUtil.isNullOrEmpty(address)) {
                    GROUPIP = address;
                }

                Integer port = staskServer.getPort();
                if (port != null) {
                    GROUPPORT = port;
                }

                if (staskServer.getHasEpoll() != null) {
                    hasEpoll = staskServer.getHasEpoll();
                }
            }
        }

        GROUPADDRESS = new InetSocketAddress(GROUPIP, GROUPPORT);
        logger.info("组播地址：" + GROUPIP + ",组播端口：" + GROUPPORT + "---->" + GROUPADDRESS);
        //EventLoopGroup group = new NioEventLoopGroup();


        try {
//            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(localIp));

            Enumeration<NetworkInterface> niall = NetworkInterface.getNetworkInterfaces();
            // Enumeration<InetAddress> addresses = ni.getInetAddresses();
            NetworkInterface networkInterface = null;

            while (niall.hasMoreElements()) {
                NetworkInterface network = niall.nextElement();
                Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (address instanceof Inet4Address) {
                        if (!address.getHostAddress().equals("127.0.0.1")) {
                            localAddress = address;
                            networkInterface = network;
                            logger.info("网络接口名称为：" + networkInterface.getName());
                            logger.info("网卡接口地址：" + address.getHostAddress());
                        }

                    }
                }
            }
//
            logger.info("当前地址：{}", localAddress.getHostAddress());
            logger.info("Epoll.isAvailable():{}", Epoll.isAvailable());
            logger.info("hasEpoll:{}",hasEpoll);
            address = localAddress.getHostAddress();
            if (Epoll.isAvailable() && hasEpoll) {

                //表示服务器连接监听线程组，专门接受 accept 新的客户端client 连接
                EventLoopGroup bossLoopGroup = new EpollEventLoopGroup();

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(bossLoopGroup)
                        //设置NIO UDP连接通道
                        .channel(EpollDatagramChannel.class)
//                    .channelFactory(new ChannelFactory<NioDatagramChannel>() {
//                        @Override
//                        public NioDatagramChannel newChannel() {
//                            return new NioDatagramChannel(InternetProtocolFamily.IPv4);
//                        }
//                    })

                        .localAddress(new InetSocketAddress(localAddress, 0))
                        .option(ChannelOption.IP_MULTICAST_IF, networkInterface)
                        .option(ChannelOption.IP_MULTICAST_ADDR, InetAddress.getByName(localAddress.getHostAddress()))
                        .option(ChannelOption.SO_BROADCAST, true)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_RCVBUF, 2048 * 1024)
                        .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .handler(new UpdInitializerEpoll());

                // linux平台下支持SO_REUSEPORT特性以提高性能

                logger.info("SO_REUSEPORT");
                bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
                // linux系统下使用SO_REUSEPORT特性，使得多个线程绑定同一个端口
                int cpuNum = Runtime.getRuntime().availableProcessors();
                logger.info("using epoll reuseport and cpu:" + cpuNum);

                for (int i = 0; i < cpuNum; i++) {
                    logger.info("worker-{} bind", i);
                    //6、绑定server，通过调用sync（）方法异步阻塞，直到绑定成功
                    CHANNEL = (EpollDatagramChannel) bootstrap.bind(GROUPADDRESS.getPort()).sync().channel();

                }
                CHANNEL.joinGroup(GROUPADDRESS, networkInterface).sync();

            } else {

                //表示服务器连接监听线程组，专门接受 accept 新的客户端client 连接
                EventLoopGroup bossLoopGroup = new NioEventLoopGroup();

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(bossLoopGroup)
                        //设置NIO UDP连接通道
                        .channel(NioDatagramChannel.class)
//                    .channelFactory(new ChannelFactory<NioDatagramChannel>() {
//                        @Override
//                        public NioDatagramChannel newChannel() {
//                            return new NioDatagramChannel(InternetProtocolFamily.IPv4);
//                        }
//                    })
                        .localAddress(new InetSocketAddress(localAddress, 0))
                        .option(ChannelOption.IP_MULTICAST_IF, networkInterface)
                        .option(ChannelOption.IP_MULTICAST_ADDR, InetAddress.getByName(localAddress.getHostAddress()))
                        .option(ChannelOption.SO_BROADCAST, true)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_RCVBUF, 2048 * 1024)
                        .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .handler(new UpdInitializer());


                CHANNEL = (NioDatagramChannel) bootstrap.bind(GROUPADDRESS.getPort()).sync().channel();
                CHANNEL.joinGroup(GROUPADDRESS, networkInterface).sync();
            }


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //   group.shutdownGracefully();
        }



    }

    public static void send(Object ob) {
        UdpProtocol udpProtocol = new UdpProtocol();
        udpProtocol.setUuid(Identity.getUUID());
        udpProtocol.setType(MessageTypeEnum.DATA);
        udpProtocol.setData(ob);

        String sendStr = JSONObject.toJSONString(udpProtocol);
        send(sendStr);
    }

    public static void send(String sendStr) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(sendStr.getBytes(StandardCharsets.UTF_8));
        DatagramPacket datagramPacket = new DatagramPacket(byteBuf, UdpMulticast.getGROUPADDRESS());
        CHANNEL.writeAndFlush(datagramPacket);
    }





    public static UdpProtocol sendReceive(String sendStr) {
        try {

            InetAddress group = InetAddress.getByName(GROUPIP);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(sendStr.getBytes(StandardCharsets.UTF_8));
            MulticastSocket socket = new MulticastSocket(GROUPPORT);
            ; // 创建UDP套接字
//            byte[] buffer = new byte[1024];
            //    byte[]  bytes=  sendStr.getBytes(StandardCharsets.UTF_8);
            java.net.DatagramPacket datagramPacket = new
                    java.net.DatagramPacket(byteBuf.array(), byteBuf.capacity(), group, GROUPPORT);

            socket.joinGroup(group);
            socket.receive(datagramPacket);

            // 设置超时时间
            socket.setSoTimeout(TIMEOUT);
            String reply = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            UdpProtocol udpProtocol = JSONObject.parseObject(reply, UdpProtocol.class);

            socket.close();
            return udpProtocol;

        } catch (Exception e) {

            logger.error("发送接收数据失败！");

        }

        return null;
    }

    public static UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUuid(Identity.getUUID());
        userInfo.setPort(ServerHttp.getLocalPort());
        if(com.sy.cc.comm.util.StringUtil.isNullOrEmpty(UdpMulticast.getAddress()) || UdpMulticast.getAddress().equals("0.0.0.0")) {
            userInfo.setAddress(UdpMulticast.getLocalAddress().getHostAddress());
        } else {
            userInfo.setAddress(UdpMulticast.getAddress());
        }
        userInfo.setTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        return userInfo;
    }
}
