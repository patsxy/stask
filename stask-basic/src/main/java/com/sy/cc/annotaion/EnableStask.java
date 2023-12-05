package com.sy.cc.annotaion;

import com.sy.cc.config.StaskConfig;
import com.sy.cc.config.ServerConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ServerConfig.class, StaskConfig.class})
@Documented
@EnableScheduling
@MapperScan(
        basePackages = {"com.sy.cc.mapper"})
public @interface EnableStask {
}
