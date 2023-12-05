package com.sy.cc.comm.entity;

import java.util.UUID;

public class Identity {
    private static String UUID_STR=UUID.randomUUID().toString();

    public  static String getUUID(){
        return  UUID_STR;
    }
}
