package com.sy.cc.service;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.emuns.AutoCheckTypeEnum;
import com.sy.cc.comm.entity.*;


import com.sy.cc.comm.entity.ZySysJobDO;

import com.sy.cc.http.ServerHttp;
import com.sy.cc.mapper.ZySysJobsMapper;

import com.sy.cc.multicast.UdpMulticast;

import com.sy.cc.service.impl.ScheduleServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


import java.util.List;


@Slf4j
@Component
public class ApplConfigService implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(ApplConfigService.class);




    @Autowired
    private ZySysJobsMapper jobMng;
    public void sendAllAppl() {
        List<ZySysJobDO> zySysJobDOS = jobMng.selectList(null);
        ScheduleServiceImpl.getScheduleJobService().sendListAppl(zySysJobDOS);
    }



    @Override
    public void run(ApplicationArguments args) throws Exception {
        sendAllAppl();
        UdpMulticast.buildMulticast();

        String type="http";
        StaskServer configServer = ConfigBase.getStaskServer();
        if(configServer!=null){
            StaskInfo staskServer = configServer.getStaskServer();
            if(staskServer!=null){
                Check check = staskServer.getCheck();
                if(check!=null){
                    String checkType = check.getType();
                    if(StringUtils.isNotBlank(checkType)){
                        type=checkType;
                    }
                }
            }
        }

        try {
            if (type.toLowerCase().equals(AutoCheckTypeEnum.HTTP.getMessage())) {
                //http 方式 需要开启
                ServerHttp.buildServer();
            }
        } catch (Exception e) {
            logger.error("start stask  fail!", e);
        }
        logger.info("启动时申请所有！");


    }

}
