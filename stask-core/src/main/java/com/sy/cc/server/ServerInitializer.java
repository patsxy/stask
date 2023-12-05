package com.sy.cc.server;

import com.sy.cc.uitl.MessageDecoder;
import com.sy.cc.uitl.MessageEncoder;
import com.sy.cc.uitl.TcpIdleStateHandler;
import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.entity.StaskClient;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        StaskClient client= ConfigBase.getConfigClient();
        Integer idleTime = client.getStaskClient().getIdleTime();
        if(idleTime==null){
            idleTime=4;
        }
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MessageDecoder());//解码器
        pipeline.addLast(new MessageEncoder());//编码器
        pipeline.addLast(new TcpIdleStateHandler(idleTime,0,0));
        pipeline.addLast(new ServerHandler());

    }
}
