package com.sy.cc.config;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.emuns.AutoCheckTypeEnum;
import com.sy.cc.comm.emuns.CacheTypeEnum;
import com.sy.cc.comm.entity.Cache;
import com.sy.cc.comm.entity.Check;
import com.sy.cc.comm.entity.StaskInfo;
import com.sy.cc.comm.entity.StaskServer;

import com.sy.cc.comm.config.AutoCheckConfig;
import com.sy.cc.http.ServerHttp;
import com.sy.cc.multicast.UdpMulticast;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class StaskConfig {

    private static final Logger logger = LoggerFactory.getLogger(StaskConfig.class);


    public static final String  STASKINFO_ADDRESS= "staskInfo_address";
    public static final String  STASKINFO_PORT= "staskInfo_port";

    public static final String STASKINFO_TYPE="staskInfo_type";

    public static final String STASKINFO_ENABLE="staskInfo_enable";
    public static final String STASKINFO_IDLETIME="staskInfo_idleTime";
    public static final String STASKINFO_GROUPS="staskInfo_groups";
    public static final String STASKINFO_CHECK_TYPE="staskInfo_check_type";

    public static final String STASKINFO_CHECK_DOCKERADDRESS="staskInfo_check_dockerAddress";
    public static final String STASKINFO_CHECK_DOCKERPORT="staskInfo_check_dockerPort";
    public static final String STASKINFO_HASEPOLL="staskInfo_hasEpoll";

    public static final String STASKINFO_CACHE_TYPE="staskInfo_cache_type";

    public static final String STASKINFO_CACHE_SERVER="staskInfo_cache_server";

    public static final String STASKINFO_CACHE_REDIS="staskInfo_cache_redis";
    private static Map<String, Object> staskMap = new HashMap<>();

    public static Map<String, Object> getStaskMap() {
        return staskMap;
    }


    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        //最多10000
        threadPoolTaskScheduler.setPoolSize(10000);
        //允许等待关闭
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        //等待关闭的时间 60秒
        threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
        //守护进程线程
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }


    public StaskConfig() {
        try {

            StaskServer configServer = ConfigBase.getStaskServer();
            AutoCheckConfig.setCheckType(AutoCheckTypeEnum.HTTP);
            if (configServer != null) {
                StaskInfo staskServer = configServer.getStaskServer();
                if (staskServer != null) {
                    Check check = staskServer.getCheck();
                    if (check != null) {
                        if (check.getType().toLowerCase().equals(AutoCheckTypeEnum.UDP.getMessage())) {
                            AutoCheckConfig.setCheckType(AutoCheckTypeEnum.UDP);
                        } else {
                            AutoCheckConfig.setCheckType(AutoCheckTypeEnum.HTTP);
                        }

                        if (!StringUtil.isNullOrEmpty(check.getDockerAddress())) {
                            ServerConfig.setAddress(check.getDockerAddress());
                            UdpMulticast.setAddress(check.getDockerAddress());
                        }
                        if (check.getDockerPort() != null) {
                            ServerHttp.setLocalPort(check.getDockerPort());
                        }
                    }


                }

                setStaskMap(configServer);
            }


        } catch (Exception e) {
            logger.error("start stask  fail!", e);
        }
    }

    public void setStaskMap(StaskServer configServer) {
        if (configServer != null) {
            StaskInfo staskServer = configServer.getStaskServer();
            if (staskServer != null) {
                staskMap.put(STASKINFO_ADDRESS,staskServer.getAddress());
                staskMap.put(STASKINFO_PORT,staskServer.getPort());
                staskMap.put(STASKINFO_TYPE,staskServer.getType());
                staskMap.put(STASKINFO_ENABLE,staskServer.getEnable());
                staskMap.put(STASKINFO_IDLETIME,staskServer.getIdleTime());
                staskMap.put(STASKINFO_GROUPS,staskServer.getGroups());
                Check check = staskServer.getCheck();
                if(check!=null) {
                    staskMap.put(STASKINFO_CHECK_TYPE,check.getType());
                    staskMap.put(STASKINFO_CHECK_DOCKERADDRESS,check.getDockerAddress());
                    staskMap.put(STASKINFO_CHECK_DOCKERPORT,check.getDockerPort());
                }

                Cache cache = staskServer.getCache();
                if(cache!=null){
                    staskMap.put(STASKINFO_CACHE_SERVER,cache.getServer());
                    staskMap.put(STASKINFO_CACHE_TYPE,cache.getType());
                    staskMap.put(STASKINFO_CACHE_REDIS,cache.getRedis());

                }

                staskMap.put(STASKINFO_HASEPOLL,staskServer.getHasEpoll());
            }
        }
    }


}
