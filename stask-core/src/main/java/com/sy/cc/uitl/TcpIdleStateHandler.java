package com.sy.cc.uitl;

import com.alibaba.fastjson2.JSONObject;
import com.sy.cc.client.ClientNetty;
import com.sy.cc.comm.emuns.MessageTypeEnum;
import com.sy.cc.comm.entity.Identity;
import com.sy.cc.comm.entity.MessageProtocol;
import com.sy.cc.comm.entity.TcpProtocol;
import com.sy.cc.comm.entity.UserInfo;

import com.sy.cc.http.ServerHttp;
import com.sy.cc.multicast.UdpMulticast;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class TcpIdleStateHandler extends IdleStateHandler {
    public TcpIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    public TcpIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    public TcpIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    public static UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUuid(Identity.getUUID());
        userInfo.setPort(ServerHttp.getLocalPort());
        if(com.sy.cc.comm.util.StringUtil.isNullOrEmpty(UdpMulticast.getAddress()) || UdpMulticast.getAddress().equals("0.0.0.0")) {
            userInfo.setAddress(UdpMulticast.getLocalAddress().getHostAddress());
        } else {
            userInfo.setAddress(UdpMulticast.getAddress());
        }
        userInfo.setTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        return userInfo;
    }
    @Override
    public IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
        sendUserInfo();
        switch (state) {
            case ALL_IDLE:
                return first ? IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT : IdleStateEvent.ALL_IDLE_STATE_EVENT;
            case READER_IDLE:
                return first ? IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT : IdleStateEvent.READER_IDLE_STATE_EVENT;
            case WRITER_IDLE:
                return first ? IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT : IdleStateEvent.WRITER_IDLE_STATE_EVENT;
            default:
                throw new IllegalArgumentException("Unhandled: state=" + state + ", first=" + first);
        }

    }

    private static void sendUserInfo() {
        UserInfo userInfo=getUserInfo();

        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setType(MessageTypeEnum.HEARTBEAT);
        tcpProtocol.setUuid(Identity.getUUID());
        String sendStr = JSONObject.toJSONString(tcpProtocol);
        MessageProtocol messageProtocol = MessageProtocol.getMessageProtocol(sendStr);
        ClientNetty.write(messageProtocol);
    }


}
