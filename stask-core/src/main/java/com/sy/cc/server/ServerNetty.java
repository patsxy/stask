package com.sy.cc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class ServerNetty {
    private static ChannelFuture channelFuture;

    private static Integer localPort = 0;

    private static String  address;

    public static Integer getLocalPort() {
        return localPort;
    }


    public static void buildServer() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    //3.2因为是server端，所以需要配置NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    //3.3设置链接超时时间
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    //3.4设置TCPbacklog参数=sync队列+accept队列
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //3.5设置配置项通信不延迟
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //3.6设置配置项接收与发送缓存区大小
                    .childOption(ChannelOption.SO_RCVBUF, 1024 * 32)
                    .childOption(ChannelOption.SO_SNDBUF, 1024 * 32)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ServerInitializer()); //自定义一个初始化类


            channelFuture = serverBootstrap.bind(0).sync();
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channelFuture.channel().localAddress();
            localPort = inetSocketAddress.getPort();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            channelFuture.channel().close();
        }


    }


    public static ChannelFuture getChannelFuture() {
        return channelFuture;
    }


}
