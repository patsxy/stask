package com.sy.cc.uitl;

import com.sy.cc.comm.entity.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class MessageDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
       // System.out.println("MessageDecoder decode 被调用");
        //需要将得到二进制字节码-> MessageProtocol 数据包(对象)

        // 通过长度，获取我们的content
        int length = in.readInt();

        byte[] content = new byte[length];
        in.readBytes(content);

        //封装成 MessageProtocol 对象，放入 out， 传递下一个handler业务处理
        MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setLen(length);
        messageProtocol.setContent(content);

        // 消息传递给下一个handler
        //out.add(messageProtocol);
        // 这样也行
        ctx.fireChannelRead(messageProtocol);

    }
}

