package com.sy.cc.server;

import com.alibaba.fastjson2.JSONObject;

import com.sy.cc.comm.emuns.NettyHaclcastTypeEnum;
import com.sy.cc.comm.entity.*;

import com.sy.cc.comm.service.AutoCompute;

import com.sy.cc.comm.config.AutoCheckConfig;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;

public class ServerHandler extends SimpleChannelInboundHandler<MessageProtocol>{
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private int count;

    private static final UUID uuid=UUID.randomUUID();

    public  static  class  Hold{
        private static AutoCompute autoCompute;
    }

    public static AutoCompute getAutoCompute(){
        if(Hold.autoCompute!=null){
            return  Hold.autoCompute;
        }
       Hold.autoCompute=null;
        ServiceLoader<AutoCompute> autoCompute = ServiceLoader.load(AutoCompute.class);
        for (AutoCompute dao : autoCompute) {
           Hold.autoCompute=dao;
        }

        return Hold.autoCompute;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        cause.printStackTrace();
        logger.error("错误关闭！");
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {

        //接收到数据，并处理
        int len = msg.getLen();
        byte[] content = msg.getContent();


        TcpProtocol tcpProtocol = JSONObject.parseObject(new String(content), TcpProtocol.class);

      //  StaskServer configServer = ConfigBase.getStaskServer();
        switch (tcpProtocol.getType()) {
            case HEARTBEAT:
                try {
                    UserInfo userInfo = (UserInfo) tcpProtocol.getData();
                    getAutoCompute().hostUpdate(userInfo);
                    getAutoCompute().checkHost(userInfo);
                    if (Identity.getUUID().equals(tcpProtocol.getUuid())) {
                        getAutoCompute().sycCompute(userInfo);
                    }
                } catch (Exception e){
                    logger.error("接收的心跳类------》，转换数据有错！",e);
                }
                break;
            case DATA:
                try {
                    Object data = tcpProtocol.getData();
                    JSONObject jsonObject = JSONObject.parseObject(data.toString());
                    String type = jsonObject.getString("type");
                    List<DistribExec> dislist = jsonObject.getList(AutoCheckConfig.getSCHEDULEDAPPLYMAP(), DistribExec.class);
                    if (!StringUtil.isNullOrEmpty(type) && type.equals(NettyHaclcastTypeEnum.APPLY.getCode())) {
//                        HazelcastInstance instance = HazelcastClient.getInstance();
//                        Map<String, List<DistribExec>> map = instance.getMap(AutoCheckConfig.getSCHEDULEDAPPLYMAP());
//                        List<DistribExec> distribExecs = map.get(tcpProtocol.getUuid());
//                        if(CollectionUtil.isNotEmpty(distribExecs)) {
//                            distribExecs.addAll(dislist);
//                        } else {
//                            distribExecs=new ArrayList<>();
//                            distribExecs.addAll(dislist);
//                        }
//                        map.put(AutoCheckConfig.getSCHEDULEDAPPLYMAP(),distribExecs);
                    }
                }  catch (Exception e){
                    logger.error("接收的数据类，转换数据有错！",e);
                }
                break;
        }



        //回复消息

        String responseContent = UUID.randomUUID().toString();




        Map map=new HashMap();
        map.put("status",0);
        map.put("count",count);
        map.put("msg","ok");
        map.put("uuid",responseContent);
        String string = JSONObject.toJSONString(map);
        ctx.writeAndFlush(MessageProtocol.getMessageProtocol(string));

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(MessageProtocol.getMessageProtocol(uuid.toString()));
        super.channelActive(ctx);
    }
}
