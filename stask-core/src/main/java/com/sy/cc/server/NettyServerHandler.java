package com.sy.cc.server;

import com.alibaba.fastjson2.JSONObject;



import com.sy.cc.comm.emuns.NettyHaclcastTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
@Deprecated
public class NettyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private int count;




//    public NettyChannelHandler() {
//        INSTANCE = MyHazelcastClient.getHazelcastInstance();
//    }




    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
        //把msg转成byte数组
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        //将buffer转成字符串
        String message = new String(buffer, CharsetUtil.UTF_8);
        System.out.println("服务器接收到" + ip + ":" + port + "的数据：" + message);
        System.out.println("服务器接收到" + ip + ":" + port + "消息条数：" + (++this.count));
        try {
            JSONObject jsonObject = JSONObject.parseObject(message);
            UUID uuid = UUID.randomUUID();

            String type = jsonObject.getString("type");
            NettyHaclcastTypeEnum nettyHaclcastTypeEnum = NettyHaclcastTypeEnum.valueOf(type);


            switch (nettyHaclcastTypeEnum) {
                case APPLY:
                   
                    break;
                case USE:

                    break;
            }

        } catch (Exception e) {
            logger.error("接受类型不是JSON", e);
        }


        //服务器回送数据给客户端，回送一个随机id值
        ByteBuf response = Unpooled.copiedBuffer("ok-s", CharsetUtil.UTF_8);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        cause.printStackTrace();
        System.out.println("错误！");
        ctx.channel().close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
        System.out.println("注销链接:" + ip + ":" + port);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
        System.out.println("断开链接:" + ip + ":" + port);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
        System.out.println("注册链接:" + ip + ":" + port);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
        System.out.println("激活链接:" + ip + ":" + port);
        super.channelActive(ctx);
    }
}
