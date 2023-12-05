package com.sy.cc;

import com.sy.cc.annotaion.EnableStask;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableStask
public class StaskStart {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(StaskStart.class, args);
        StaskStart bean =run.getBean(StaskStart.class, args);


    }
}