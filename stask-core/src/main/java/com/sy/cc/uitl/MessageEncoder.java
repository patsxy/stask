package com.sy.cc.uitl;

import com.sy.cc.comm.entity.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<MessageProtocol>  {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) throws Exception {
       // System.out.println("MessageEncoder encode 方法被调用");
        // 编码
        out.writeInt(msg.getLen());
        out.writeBytes(msg.getContent());
    }

}
