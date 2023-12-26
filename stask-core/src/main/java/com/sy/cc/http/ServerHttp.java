package com.sy.cc.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;

public class ServerHttp {

    private static  Integer localPort=null;


    public static void  setLocalPort(Integer port){
        localPort=port;
    }

    public static  Integer getLocalPort(){
        return localPort;
    }

    public static void buildServer() {

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {

            ServerBootstrap sb = new ServerBootstrap();

            sb.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new HttpServerCodec());

                            pipeline.addLast(new HttpObjectAggregator(61024));

                            pipeline.addLast(new ServerHttpHandler());


                        }
                    });

            if(localPort==null){
                localPort=0;
            }

            ChannelFuture future = sb.bind(localPort).sync();
            InetSocketAddress inetSocketAddress = (InetSocketAddress) future.channel().localAddress();
            localPort = inetSocketAddress.getPort();
            System.out.println("Server...............................");
            System.out.println("port:"+localPort);
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            boss.shutdownGracefully();
            worker.shutdownGracefully();

        }


    }


    public static void main(String[] args) {
        buildServer();
    }
}
