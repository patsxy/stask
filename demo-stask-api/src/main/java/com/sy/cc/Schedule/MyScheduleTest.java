package com.sy.cc.Schedule;

import com.sy.cc.comm.entity.Identity;
import org.springframework.stereotype.Component;

@Component(value = "MyScheduleTest")
public class MyScheduleTest {

    public void getTest(String arg) {

        System.out.println(String.format("我%s是第二个MyScheduleTest模块，在处理,id:%s", Identity.getUUID(),arg));
    }
}
