package com.sy.cc.comm.entity;

import lombok.Data;

import java.util.List;

@Data
public class StaskInfo {
    private String address;

    private Integer port;

    private String type;

    private Integer idleTime = 4;
    private List<String> groups;

    private Check check;

    private Cache cache;

    private Boolean  hasEpoll;
}
