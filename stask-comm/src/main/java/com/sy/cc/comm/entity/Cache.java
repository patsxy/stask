package com.sy.cc.comm.entity;

import lombok.Data;

@Data
public class Cache {
    private Boolean server;
    private String type;

    private Redis redis;
}
