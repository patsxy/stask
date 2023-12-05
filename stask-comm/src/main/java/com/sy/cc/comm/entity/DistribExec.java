package com.sy.cc.comm.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class DistribExec implements Serializable {

    private static final long serialVersionUID = 1L;

    private String  uuid;

    private String taskId;
}
