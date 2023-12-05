package com.sy.cc.multicast;


import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.entity.StaskServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;

public class UpdInitializerEpoll extends ChannelInitializer<EpollDatagramChannel> {
    @Override
    protected void initChannel(EpollDatagramChannel ch) throws Exception {
        StaskServer staskServer = ConfigBase.getStaskServer();
        Integer idleTime = 6;
        if (staskServer != null) {
            idleTime = staskServer.getStaskServer().getIdleTime();

        }
        System.out.println(String.format("心跳：%s秒", idleTime));
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new EpollEventLoopGroup(), new UdpIdleStateHandler(idleTime, 0, 0));
        pipeline.addLast(new EpollEventLoopGroup(), new UdpHandler());

    }

}
