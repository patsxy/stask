package com.sy.cc.multicast;

import com.alibaba.fastjson2.JSONObject;
import com.sy.cc.comm.entity.UdpProtocol;
import com.sy.cc.comm.service.IUdpMulticastService;

import java.util.concurrent.ConcurrentHashMap;

public class IUdpMulticastServiceImpl implements IUdpMulticastService {
    private static volatile ConcurrentHashMap<String, UdpProtocol> sendRMap = new ConcurrentHashMap();

    public ConcurrentHashMap<String, UdpProtocol> getSendRMap() {
        return sendRMap;
    }

    @Override
    public void sendReceiveMap(UdpProtocol udpProtocol) {
        String sendStr = JSONObject.toJSONString(udpProtocol);
        UdpMulticast.send(sendStr);
    }
}
