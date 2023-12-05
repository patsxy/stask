package com.sy.cc.comm.entity;

import com.sy.cc.comm.util.StringUtil;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class UserInfo  implements Serializable {
    private static final long serialVersionUID = 1L;
    private String uuid;

    private Integer port;

    private String address;
    private long time;



}
