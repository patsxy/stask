package com.sy.cc.comm.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ScheduledFuture;

@Data
@NoArgsConstructor
public class ScheduledExecDTO {
    private static final long serialVersionUID = 1L;
    //执行器
    private ScheduledFuture<?> future;
    //corn 表达式
    private String corn;

    public ScheduledExecDTO(ScheduledFuture<?> future, String corn){
        this.future=future;
        this.corn=corn;
    }
}