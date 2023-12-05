package com.sy.cc.comm.service;


import com.sy.cc.comm.entity.*;

import java.util.List;
import java.util.Map;

public interface IScheduleJobService {
    //获取 HazelcastInstance


    //执行分发
    void execDistrib(List<ZySysJobDO> listAll, Map<String, ScheduledExecDTO> FUTURESJOBMAP);

    void sendListAppl(List<ZySysJobDO> zySysJobDOS);


    void delUseList(List<String> zySysJobDOS);

    List<Task> execScheduleExec(List<ZySysJobDO> listAll, Map<String, ScheduledExecDTO> FUTURESJOBMAP);



    List<DistribExec> getDistribExecs();

    Execer getExecer();
}
