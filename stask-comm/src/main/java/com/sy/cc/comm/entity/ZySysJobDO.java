package com.sy.cc.comm.entity;

import com.baomidou.mybatisplus.annotation.*;

import com.sy.cc.comm.util.CronUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("zy_sys_job")
public class ZySysJobDO  implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.Long id;

    @TableField("name")
    private String name;
    @TableField("cron")
    private String cron;
    @TableField("clazz_name")
    private String clazzName;
    @TableField("clazz_method")
    private String clazzMethod;
    @TableField("instance_name")
    private String instanceName;
    @TableField("params")
    private String params;

    private String remark;

    private Boolean hasEnable;

    private Long tenantId;
    private Long taskId;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Integer type;

    public String getCronDesc(){
        return this.getCron()!=null? CronUtil.descCorn(this.getCron()):null;
    }

}