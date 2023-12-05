package com.sy.cc.multicast;

import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.entity.StaskServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UpdInitializer extends ChannelInitializer<NioDatagramChannel> {
    @Override
    protected void initChannel(NioDatagramChannel ch) throws Exception {
        StaskServer staskServer = ConfigBase.getStaskServer();
        Integer idleTime=6;
        if(staskServer !=null) {
            idleTime = staskServer.getStaskServer().getIdleTime();

        }
        System.out.println(String.format("心跳：%s秒",idleTime));
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new NioEventLoopGroup(),new UdpIdleStateHandler(idleTime, 0, 0));
        pipeline.addLast(new NioEventLoopGroup(), new UdpHandler());

    }

}
