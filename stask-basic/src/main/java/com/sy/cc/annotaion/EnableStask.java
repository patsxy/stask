package com.sy.cc.annotaion;

import com.sy.cc.config.StaskConfig;
import com.sy.cc.config.ServerConfig;
import com.sy.cc.service.ApplConfigService;
import com.sy.cc.service.impl.ScheduleServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({StaskConfig.class, ServerConfig.class, ScheduleServiceImpl.class, ApplConfigService.class})
@Documented
@EnableScheduling
@MapperScan(
        basePackages = {"com.sy.cc.mapper"})
public @interface EnableStask {
}
