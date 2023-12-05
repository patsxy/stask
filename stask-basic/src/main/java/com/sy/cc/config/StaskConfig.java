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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class StaskConfig {

    private static final Logger logger = LoggerFactory.getLogger(StaskConfig.class);


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
                    }


                }
            }




        } catch (Exception e) {
            logger.error("start stask  fail!", e);
        }
    }

}
