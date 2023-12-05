package com.sy.cc.comm.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Execer implements Serializable {

    private static final long serialVersionUID = 1L;
    //执行者集合
    private Map<String,UserInfo> execers=new HashMap<>();
    //执行者群状态 0 没变动  1 变动
    private int status=0;

}
