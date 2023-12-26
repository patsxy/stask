package com.sy.cc.multicast;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;

import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.emuns.MessageTypeEnum;
import com.sy.cc.comm.emuns.NettyHaclcastTypeEnum;
import com.sy.cc.comm.entity.*;

import com.sy.cc.comm.service.IAutoCompute;
import com.sy.cc.comm.service.IUdpMulticastService;

import com.sy.cc.comm.config.AutoCheckConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger logger = LoggerFactory.getLogger(UdpHandler.class);

    public static class Hold {
        private static IAutoCompute IAutoCompute;
        private static IUdpMulticastService IUdpMulticastService;
    }

    public static IAutoCompute getAutoCompute() {
        if (Hold.IAutoCompute != null) {
            return Hold.IAutoCompute;
        }
        Hold.IAutoCompute =null;
        ServiceLoader<IAutoCompute> autoCompute = ServiceLoader.load(IAutoCompute.class);
        for (IAutoCompute dao : autoCompute) {
            Hold.IAutoCompute =dao;
        }

        return Hold.IAutoCompute;

    }

    public static IUdpMulticastService getUdpMulticastService(){
        if(Hold.IUdpMulticastService !=null){
            return  Hold.IUdpMulticastService;
        }

        Hold.IUdpMulticastService =new IUdpMulticastServiceImpl();

        return  Hold.IUdpMulticastService;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        final ByteBuf buf = msg.content();
        int readableBytes = buf.readableBytes();
        byte[] msgData = new byte[readableBytes];
        buf.readBytes(msgData);

        //String s = ByteUtil.bytesToHexString(msgData);
        String s = new String(msgData);
        UdpProtocol udpProtocol = JSONObject.parseObject(s, UdpProtocol.class);
        StaskServer configServer = ConfigBase.getStaskServer();
        switch (udpProtocol.getType()) {
            case HEARTBEAT:
                try {
                    UserInfo userInfo = JSON.parseObject(udpProtocol.getData().toString(), UserInfo.class);

                    getAutoCompute().hostUpdate(userInfo);
                    getAutoCompute().checkHost(userInfo);
                    if (Identity.getUUID().equals(udpProtocol.getUuid())) {
                        getAutoCompute().sycCompute(userInfo);
                    }
                } catch (Exception e) {
                    logger.error("接收的心跳类------》，转换数据有错！", e);
                }
                break;
            case DATA:
                try {
                    Object data = udpProtocol.getData();
                    JSONObject jsonObject = JSONObject.parseObject(data.toString());
                    String type = jsonObject.getString("type");
                    List<DistribExec> dislist = jsonObject.getList(AutoCheckConfig.getSCHEDULEDAPPLYMAP(), DistribExec.class);
                    if (!StringUtil.isNullOrEmpty(type) && type.equals(NettyHaclcastTypeEnum.APPLY.getCode())) {
//                        HazelcastInstance instance = HazelcastClient.getInstance();
//                        Map<String, List<DistribExec>> map = instance.getMap(AutoCheckConfig.getSCHEDULEDAPPLYMAP());
//                        List<DistribExec> distribExecs = map.get(udpProtocol.getUuid());
//                        if (CollectionUtil.isNotEmpty(distribExecs)) {
//                            distribExecs.addAll(dislist);
//                        } else {
//                            distribExecs = new ArrayList<>();
//                            distribExecs.addAll(dislist);
//                        }
//                        map.put(AutoCheckConfig.getSCHEDULEDAPPLYMAP(), distribExecs);
                    }
                } catch (Exception e) {
                    logger.error("接收的数据类，转换数据有错！", e);
                }
                break;

            case APPLRECEIVE:
                try {
                    if (udpProtocol.getUuid().equals(Identity.getUUID())) {
                        logger.info("是我{}的需要返回的请求包！", Identity.getUUID());
                        UdpProtocol reudpProtocol = new UdpProtocol();
                        reudpProtocol.setUuid(Identity.getUUID());
                        reudpProtocol.setType(MessageTypeEnum.RECEIVE);
                        reudpProtocol.setData(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
                        // String string = JSON.toJSONString(reudpProtocol);
                        getUdpMulticastService().sendReceiveMap(reudpProtocol);
                    } else {

                        logger.info("{}不是我的需要返回包！", udpProtocol.getUuid());
                    }

                } catch (Exception e) {
                    logger.error("回复数据失败！", e);
                }

                break;
            case RECEIVE:
                ConcurrentHashMap<String, UdpProtocol> sendRMap =  getUdpMulticastService().getSendRMap();
                UdpProtocol udpPro = sendRMap.get(udpProtocol.getUuid());
                if (null != udpPro) {
                    //改成接收到回复
                    udpPro.setType(MessageTypeEnum.RECEIVE);
                }
                break;
            default:
                break;
        }
        System.out.println("接收到的数据 " + s);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("活动:" + Identity.getUUID());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //组播 用不了
        System.out.println("不活动:" + Identity.getUUID());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("注册:" + Identity.getUUID());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        //组播 用不了
        System.out.println("取消注册:" + Identity.getUUID());
        super.channelUnregistered(ctx);
    }
}

