package com.sy.cc.entity.dto;



import com.alibaba.fastjson.serializer.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 任务对象 zy_sys_job
 * 
 * @author sy
 * @date 2023-11-03
 */
@Data
@Schema(description = "任务DTO", name = "任务")
public class ZySysJobDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
//    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "${comment}", name = "id")
    private Long id;

    /** 任务名称 */
    @Schema(description = "任务名称", name = "name")
    private String name;

    /** 任务cron */
    @Schema(description = "任务cron", name = "cron")
    private String cron;

    /** 任务类名称 */
    @Schema(description = "任务类名称", name = "clazzName")
    private String clazzName;

    /** 任务类方法名称 */
    @Schema(description = "任务类方法名称", name = "clazzMethod")
    private String clazzMethod;

    /** 实例名称 */
    @Schema(description = "实例名称", name = "instanceName")
    private String instanceName;

    /** 任务方法参数 */
    @Schema(description = "任务方法参数", name = "params")
    private String params;

    /** 是否允许 */
    @Schema(description = "是否允许", name = "hasEnable")
    private Boolean hasEnable;

    /** 租户id */
    @Schema(description = "租户id", name = "tenantId")
    private Long tenantId;

    /** task_id */
    @Schema(description = "task_id", name = "taskId")
    private Long taskId;

    /** type */
    @Schema(description = "type", name = "type")
    private Integer type;

    /** 备注 */
    @Schema(description = "备注", name = "remark")
    private String remark;

}
