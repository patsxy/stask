package com.sy.cc.config;



import com.sy.cc.multicast.UdpMulticast;
import io.netty.util.internal.StringUtil;
import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.context.WebServerInitializedEvent;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


import java.net.Inet4Address;
import java.net.UnknownHostException;

@Getter
@Component
public class ServerConfig implements ApplicationListener<WebServerInitializedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

    private Integer port;

    private String address;

    public Integer getPort() {
        return port;
    }


    @Override
    public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
        this.port = webServerInitializedEvent.getWebServer().getPort();
        try {
            address = Inet4Address.getLocalHost().getHostAddress();
            if (StringUtil.isNullOrEmpty(UdpMulticast.getAddress()) || UdpMulticast.getAddress().equals("0.0.0.0")  ||
                    !UdpMulticast.getAddress().equals(address)
            ) {
                UdpMulticast.setAddress(address);
            }
        } catch (UnknownHostException e) {
            logger.error("本地ip获取错误", e);
        }
        logger.info("spring boot server:{},port:{}",address,port);
    }


}
