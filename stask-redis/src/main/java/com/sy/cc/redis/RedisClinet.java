package com.sy.cc.redis;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sy.cc.comm.config.ConfigBase;
import com.sy.cc.comm.emuns.CacheTypeEnum;
import com.sy.cc.comm.entity.Cache;
import com.sy.cc.comm.entity.Redis;
import com.sy.cc.comm.entity.StaskInfo;
import com.sy.cc.comm.entity.StaskServer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;


public class RedisClinet {

    public static class Hold {
        private static RedissonClient REDISSONCLIENT;
    }

    public static RedissonClient getRedisson() {
        if (Hold.REDISSONCLIENT == null) {
            StaskServer configServer = ConfigBase.getStaskServer();


            Config config = new Config();
            if (configServer != null) {
                StaskInfo staskServer = configServer.getStaskServer();
                Cache cache = staskServer.getCache();
                if (cache != null) {
                    if (cache.getType().toLowerCase().equals(CacheTypeEnum.REDIS.getMessage())) {
                        Redis redis = cache.getRedis();
                        String type = redis.getType();
                        if (StringUtils.isBlank(type)) {
                            type = "redis";
                        }
                        String passwd = redis.getPassword();
                        String user = redis.getUser();

                        if (redis != null) {
                            String url = redis.getUrl();
                            if (StringUtils.isNotBlank(url)) {
                                String[] split = url.split(",");
                                if (split.length > 1) {
                                    ClusterServersConfig clusterServersConfig = config.useClusterServers();
                                    for (String s : split) {
                                        clusterServersConfig.addNodeAddress(type + "://" + s);
                                    }

                                    if(StringUtils.isNotBlank(user)){
                                        clusterServersConfig.setUsername(user);
                                    }
                                    if (StringUtils.isNotBlank(passwd)) {
                                        clusterServersConfig.setPassword(passwd);
                                    }

                                } else {
                                    SingleServerConfig singleServerConfig = config.useSingleServer().setAddress(type +"://"+ split[0]);
                                    if(StringUtils.isNotBlank(user)){
                                        singleServerConfig.setUsername(user);
                                    }
                                    if (StringUtils.isNotBlank(passwd)) {
                                        singleServerConfig.setPassword(passwd);
                                    }

                                }

                            } else {
                                Integer port=redis.getPort();
                                if(port==null){
                                    port=6379;
                                }

                                SingleServerConfig singleServerConfig = config.useSingleServer().setAddress(type +"://"+redis.getHost()+":"+port);
                                if(StringUtils.isNotBlank(user)){
                                    singleServerConfig.setUsername(user);
                                }
                                if (StringUtils.isNotBlank(passwd)) {
                                    singleServerConfig.setPassword(passwd);
                                }
                            }

                        }
                    }
                }

            }
            // Config config=Config.fromYAML()
         //   config.useSingleServer().setAddress("redis://192.168.1.179:51369").setPassword("zysoft");
//            config.useClusterServers().addNodeAddress("redis://192.168.1.179:51369")
//                    .setPassword("zysoft");
            Hold.REDISSONCLIENT = Redisson.create(config);
        } else {
            Hold.REDISSONCLIENT.reactive();
        }
        return Hold.REDISSONCLIENT;
    }


}
