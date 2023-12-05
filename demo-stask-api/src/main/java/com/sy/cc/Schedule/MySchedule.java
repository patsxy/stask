package com.sy.cc.Schedule;

import com.sy.cc.comm.entity.Identity;
import org.springframework.stereotype.Component;

@Component(value = "MySchedule")
public class MySchedule {


    public void getHandleTest(String arg) {

        System.out.println(String.format("我%s是第一个MySchedule模块，在处理,id:%s", Identity.getUUID(),arg));
    }

}
