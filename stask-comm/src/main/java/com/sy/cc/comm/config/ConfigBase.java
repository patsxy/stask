package com.sy.cc.comm.config;


import cn.hutool.core.io.resource.ClassPathResource;

import com.sy.cc.comm.emuns.AutoCheckTypeEnum;
import com.sy.cc.comm.emuns.CacheTypeEnum;
import com.sy.cc.comm.entity.Cache;
import com.sy.cc.comm.entity.StaskClient;
import com.sy.cc.comm.entity.StaskServer;
import com.sy.cc.comm.service.IConfigProvider;
import com.sy.cc.comm.util.StringUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


import java.io.InputStream;
import java.util.*;

public class ConfigBase {


    public static class Configer {
        private static StaskServer STASK_SERVER;
        private static StaskClient STASK_CLIENT = getStaskClient();
    }

//    public static StaskServer getConfigServer() {
//        return Configer.STASK_SERVER;
//    }

    public static StaskClient getConfigClient() {
        return Configer.STASK_CLIENT;
    }

    public static StaskServer getStaskServer() {
        if (Configer.STASK_SERVER != null) {
            return Configer.STASK_SERVER;
        }


        try {
            Yaml yaml = new Yaml(new Constructor(StaskServer.class));
            ClassPathResource classPathResource = new ClassPathResource("stask.yaml");


            InputStream inputStream = classPathResource.getStream();
            Configer.STASK_SERVER = yaml.load(inputStream);
//            staskServer = objectMapper.readValue(new File(classPathResource.getUrl().getFile()), StaskServer.class);
            if (Configer.STASK_SERVER != null) {
                String type = Configer.STASK_SERVER.getStaskServer().getCheck().getType();
                if (!StringUtil.isNullOrEmpty(type) && type.toLowerCase().equals(AutoCheckTypeEnum.UDP.getMessage())) {
                    AutoCheckConfig.setCheckType(AutoCheckTypeEnum.UDP);
                } else if (!StringUtil.isNullOrEmpty(type) && type.toLowerCase().equals(AutoCheckTypeEnum.HTTP.getMessage())) {
                    AutoCheckConfig.setCheckType(AutoCheckTypeEnum.HTTP);
                }

                Cache cache = Configer.STASK_SERVER.getStaskServer().getCache();
                if (cache != null) {
                    if (cache.getType().toLowerCase().equals(CacheTypeEnum.HAZELCAST.getMessage())) {
                        ServiceLoader<IConfigProvider> configProviders = ServiceLoader.load(IConfigProvider.class);
                        for (IConfigProvider dao : configProviders) {
                            String name = dao.getClass().getName();
                            if (StringUtil.nonNull(name) && name.equals("com.sy.cc.hazelcast.ConfigProviderImpl")) {
                                dao.runHazelcastServer();
                                break;
                            }
                        }
                    }
                }

            }


        } catch (Exception e) {
            System.out.println("读取StaskServer yaml失败！");

        }


        return Configer.STASK_SERVER;
    }


    private static StaskClient getStaskClient() {
        // ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        StaskClient staskClient = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("stask-client.yaml");
            //     staskClient = objectMapper.readValue(new File(classPathResource.getUrl().getFile()), StaskClient.class);
        } catch (Exception e) {
            System.out.println("stask yaml失败！");

        }
        return staskClient;
    }


    public static void main(String[] args) {
        getStaskServer();
        getStaskClient();
    }
}
