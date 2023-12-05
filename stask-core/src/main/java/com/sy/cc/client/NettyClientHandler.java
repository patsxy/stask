package com.sy.cc.client;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Deprecated
public class NettyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private int count;



//    public NettyClientHandler() {
//        INSTANCE= MyHazelcastClient.getHazelcastInstance();
//    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        //把msg转成byte数组
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);
        String message = new String(buffer, CharsetUtil.UTF_8);

        System.out.println("客户端接收到的数据：" + message);
        System.out.println("客户端接收到消息条数：" + (++this.count));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //使用客户端发送10条数据，hello server
//        for (int i=0; i<10; i++) {
//            ByteBuf buffer = Unpooled.copiedBuffer("hello,server", CharsetUtil.UTF_8);
//            ctx.writeAndFlush(buffer);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("注销链接");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("断开链接");
        super.channelInactive(ctx);
    }

}
