package com.sy.cc.client;

import com.sy.cc.comm.entity.MessageProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;

public class ClientNetty {


    private static ChannelFuture channelFuture;

    public static void buildClient() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();


        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                //2.1设置链接超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                //2.2设置配置项接收与发送缓存区大小
                .option(ChannelOption.SO_RCVBUF, 1024 * 32)
                .option(ChannelOption.SO_SNDBUF, 1024 * 32)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .handler(new ClientInitializer()); //自定义一个初始化类

        channelFuture = bootstrap.connect("localhost", 7000).sync();

        //异步阻塞，后续才能传输数据
        channelFuture.sync();


    }

    public static ChannelFuture getChannelFuture() {
        return channelFuture;
    }


    public static void write(MessageProtocol messageProtocol) {
        channelFuture.channel().writeAndFlush(messageProtocol);
    }

    public static void close() throws Exception {
        channelFuture.channel().closeFuture().sync();
    }

}
