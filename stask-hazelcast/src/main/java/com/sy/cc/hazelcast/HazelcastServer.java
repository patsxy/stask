package com.sy.cc.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastServer {

    public static class Holder{
        private static final HazelcastInstance instance = Hazelcast.newHazelcastInstance();
    }

    public static   HazelcastInstance getHazelcastServer(){
        return Holder.instance;
    }
}
