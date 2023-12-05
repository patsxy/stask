CREATE TABLE `zy_sys_job` (
                              `id` bigint NOT NULL,
                              `name` varchar(255) DEFAULT NULL COMMENT '任务名称',
                              `cron` varchar(255) DEFAULT NULL COMMENT '任务cron',
                              `clazz_name` varchar(255) DEFAULT NULL COMMENT '执行任务类名称',
                              `clazz_method` varchar(255) DEFAULT NULL COMMENT '执行任务类方法名称',
                              `instance_name` varchar(255) DEFAULT NULL COMMENT '实例名称',
                              `params` varchar(255) DEFAULT NULL COMMENT '任务方法参数',
                              `remark` varchar(255) DEFAULT NULL COMMENT '任务备注',
                              `has_enable` bit(1) DEFAULT NULL COMMENT '是否允许',
                              `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
                              `create_time` datetime DEFAULT NULL,
                              `update_time` datetime DEFAULT NULL,
                              `task_id` bigint DEFAULT NULL COMMENT 'task_id',
                              `type` int DEFAULT NULL COMMENT 'type',
                              PRIMARY KEY (`id`),
                              KEY `zy_idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务表';