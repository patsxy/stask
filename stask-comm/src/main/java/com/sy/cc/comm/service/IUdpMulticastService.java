package com.sy.cc.comm.service;

import com.sy.cc.comm.entity.UdpProtocol;

import java.util.concurrent.ConcurrentHashMap;

public interface IUdpMulticastService {
    ConcurrentHashMap<String, UdpProtocol> getSendRMap();
    void sendReceiveMap(UdpProtocol udpProtocol);
}
