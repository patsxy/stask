package com.sy.cc.multicast;

import com.alibaba.fastjson2.JSONObject;
import com.sy.cc.comm.emuns.MessageTypeEnum;
import com.sy.cc.comm.entity.Identity;
import com.sy.cc.comm.entity.UdpProtocol;
import com.sy.cc.comm.entity.UserInfo;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class UdpIdleStateHandler extends IdleStateHandler {
    public UdpIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    public UdpIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    public UdpIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit);
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
        UserInfo  userInfo= UdpMulticast.getUserInfo();


        UdpProtocol udpProtocol=new UdpProtocol();
        udpProtocol.setType(MessageTypeEnum.HEARTBEAT);
        udpProtocol.setUuid(Identity.getUUID());
        udpProtocol.setData(userInfo);
        String sendStr = JSONObject.toJSONString(udpProtocol);
        UdpMulticast.send(sendStr);
    }


}
