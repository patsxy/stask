package com.sy.cc.comm.entity;

import lombok.Data;

@Data
public class Redis {
    private String host;

    private Integer port;

    private String  user;

    private String  url;
    private String password;

    private Integer expire;

    private String type;
}
