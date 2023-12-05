package com.sy.cc.comm.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Host implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uuid;

    private Long time;
}
