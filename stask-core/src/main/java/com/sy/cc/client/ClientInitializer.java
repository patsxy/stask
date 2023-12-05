package com.sy.cc.client;

import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.uitl.MessageDecoder;
import com.sy.cc.uitl.MessageEncoder;
import com.sy.cc.uitl.TcpIdleStateHandler;
import com.sy.cc.comm.entity.StaskClient;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        StaskClient client= ConfigBase.getConfigClient();
        Integer idleTime = client.getStaskClient().getIdleTime();
        if(idleTime==null){
            idleTime=4;
        }

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MessageEncoder()); //加入编码器
        pipeline.addLast(new MessageDecoder()); //加入解码器
        pipeline.addLast(new TcpIdleStateHandler(idleTime,0,0));
        pipeline.addLast(new ClientHandler());

    }

}
