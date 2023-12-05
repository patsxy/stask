package com.sy.cc.hazelcast;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;


import java.util.UUID;

public class HazelcastClient {



    public static class Holder {
        private static final HazelcastInstance INSTANCE =
                getHazelcastInstance();
    }

    public static HazelcastInstance getInstance() {
        return Holder.INSTANCE;
    }


    public static HazelcastInstance getHazelcastInstance() {

        ClientConfig clientConfig = ConfigProviderImpl.locateAndGetClientConfig();

        clientConfig.setInstanceName("Schedled" + UUID.randomUUID());
        HazelcastInstance instance = com.hazelcast.client.HazelcastClient.newHazelcastClient(clientConfig);
        //  Map<Integer, String> clusterMap = instance.getMap("map");
        return instance;
    }

//    public static void main(String[] args) {
//        getHazelcastInstance();
//    }
}
