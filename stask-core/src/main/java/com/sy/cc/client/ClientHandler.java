package com.sy.cc.client;


import com.sy.cc.comm.entity.MessageProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;


public class ClientHandler extends SimpleChannelInboundHandler<MessageProtocol> {
    private int count;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //使用客户端发送10条数据 "你好啊，我的baby" 编号

        String mes = "已经链接！";
        System.out.println(mes);
        MessageProtocol messageProtocol = MessageProtocol.getMessageProtocol(mes);
        ctx.writeAndFlush(messageProtocol);

    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {

        int len = msg.getLen();
        byte[] content = msg.getContent();

        System.out.println("客户端接收到消息如下");
        System.out.println("长度=" + len);
        System.out.println("内容=" + new String(content, Charset.forName("utf-8")));

        System.out.println("客户端接收消息数量=" + (++this.count));


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常消息=" + cause.getMessage());
        ctx.close();
    }



    //    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
//
//
//        System.out.println("客户端接收到消息如下");
//
//        System.out.println("内容=" + new String(byteBuf.array(), Charset.forName("utf-8")));
//
//        System.out.println("客户端接收消息数量=" + (++this.count));
//    }
}
