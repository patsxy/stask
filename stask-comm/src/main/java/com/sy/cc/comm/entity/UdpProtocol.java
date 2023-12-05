package com.sy.cc.comm.entity;


import com.sy.cc.comm.emuns.MessageTypeEnum;
import lombok.Data;

@Data
public class UdpProtocol<T> {

    private String uuid;

    private MessageTypeEnum type;
    private T data;

    private Long time;

}
