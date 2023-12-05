package com.sy.cc.comm.entity;

import com.sy.cc.comm.emuns.OperationTypeEnum;
import lombok.Data;


@Data
public class Task {

    private ZySysJobDO job;



    private String  id;
    private String  cron;

    private OperationTypeEnum operationType;

}
