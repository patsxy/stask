package com.sy.cc.hazelcast;

import cn.hutool.core.collection.CollectionUtil;
import org.quartz.CronExpression;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import com.sy.cc.comm.config.AutoCheckConfig;
import com.sy.cc.comm.emuns.OperationTypeEnum;
import com.sy.cc.comm.entity.*;
import com.sy.cc.comm.exception.ZyException;
import com.sy.cc.comm.service.IScheduleJobService;
import com.sy.cc.comm.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ScheduleJobServiceImpl implements IScheduleJobService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleJobServiceImpl.class);

    // 1、创建ScheduledExecutorService线程池，做定时器
    ScheduledExecutorService pool = Executors.newScheduledThreadPool(2000);


    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void execDistrib(List<ZySysJobDO> listAll, Map<String, ScheduledExecDTO> FUTURESJOBMAP) {
        //执行定时 更新本地job
        execScheduleExec(listAll, FUTURESJOBMAP);
    }


    @Override
    public void delUseList(List<String> zySysJobDOS) {
        if (CollectionUtil.isNotEmpty(zySysJobDOS)) {
            Map<String, List<DistribExec>> useMap = HazelcastClient.getInstance().getMap(AutoCheckConfig.getSCHEDULEDUSEMAP());



            //加锁
            if (MapUtils.nonNull(useMap) && HazelcastClient.getInstance().getCPSubsystem().getLock(AutoCheckConfig.getSCHEDULEDUSEMAP()).tryLock(10000, TimeUnit.MILLISECONDS)) {
                for (Map.Entry<String, List<DistribExec>> user : useMap.entrySet()) {
                    List<DistribExec> collect1 = user.getValue().stream().filter(f -> zySysJobDOS.contains(f.getTaskId())).collect(Collectors.toList());
                    useMap.put(user.getKey(),collect1);
                }
                HazelcastClient.getInstance().getCPSubsystem().getLock(AutoCheckConfig.getSCHEDULEDUSEMAP()).unlock();

            }

        }
    }

    @Override
    public void sendListAppl(List<ZySysJobDO> zySysJobDOS) {
        if (CollectionUtil.isNotEmpty(zySysJobDOS)) {
            //加锁
            if (HazelcastClient.getInstance().getCPSubsystem().getLock(AutoCheckConfig.getSCHEDULEDAPPLYMAP()).tryLock(10000, TimeUnit.MILLISECONDS)) {
                Map<String, List<DistribExec>> applMap = HazelcastClient.getInstance().getMap(AutoCheckConfig.getSCHEDULEDAPPLYMAP());
                Map<String, List<DistribExec>> useMap = HazelcastClient.getInstance().getMap(AutoCheckConfig.getSCHEDULEDUSEMAP());
                String uuid = Identity.getUUID();

                List<String> oldTaskId = useMap.entrySet().stream().flatMap(entry -> entry.getValue().stream()).map(DistribExec::getTaskId).collect(Collectors.toList());
                List<DistribExec> newDistribExecs = zySysJobDOS.stream().filter(f -> !oldTaskId.contains(f.getId() + ""))
                        .map(m -> DistribExec.builder().uuid(uuid).taskId(m.getId() + "").build())
                        .collect(Collectors.toList());


                if (MapUtils.nonNull(applMap)) {
                    List<DistribExec> distribExecs = applMap.get(uuid);
                    if (CollectionUtil.isNotEmpty(distribExecs)) {
                        newDistribExecs.addAll(distribExecs);
                    }
                }
                //加入 自己
                applMap.put(uuid, newDistribExecs);

                //删除使用组多余的
                List<Long> collect = zySysJobDOS.stream().map(ZySysJobDO::getTaskId).collect(Collectors.toList());
                for (Map.Entry<String, List<DistribExec>> user : useMap.entrySet()) {
                    List<DistribExec> collect1 = user.getValue().stream().filter(f -> collect.contains(f.getTaskId())).collect(Collectors.toList());
                    useMap.put(user.getKey(), collect1);
                }


                HazelcastClient.getInstance().getCPSubsystem().getLock(AutoCheckConfig.getSCHEDULEDAPPLYMAP()).unlock();
            }

        }
    }

    @Override
    public List<Task> execScheduleExec(List<ZySysJobDO> listAll, Map<String, ScheduledExecDTO> FUTURESJOBMAP) {
        Map<String, List<DistribExec>> useMap = HazelcastClient.getInstance().getMap(AutoCheckConfig.getSCHEDULEDUSEMAP());
        if (MapUtils.nonNull(useMap)) {
            List<String> allUser = useMap.entrySet().stream().flatMap(entry -> entry.getValue().stream()).map(DistribExec::getTaskId).collect(Collectors.toList());

            List<ZySysJobDO> newappls = listAll.stream().filter(f -> !allUser.contains(f.getId() + "")).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(newappls)) {
                sendListAppl(newappls);
            }
            //现在库中的id
            List<String> currDbId = listAll.stream().map(m -> m.getId() + "").collect(Collectors.toList());
            //需要移除的id
            List<String> remotes = allUser.stream().filter(f -> !currDbId.contains(f)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(remotes)) {
                for (Map.Entry<String, List<DistribExec>> entry : useMap.entrySet()) {
                    List<DistribExec> newDistrList = new ArrayList<>();
                    for (DistribExec dis : entry.getValue()) {
                        if (!remotes.contains(dis.getTaskId())) {
                            newDistrList.add(dis);
                        }
                    }
                    useMap.put(entry.getKey(), newDistrList);
                }


            }


            //处理自己的数据
            List<DistribExec> distribExecs = useMap.get(Identity.getUUID());
            if (CollectionUtil.isNotEmpty(distribExecs)) {
                List<String> ids = distribExecs.stream().map(DistribExec::getTaskId).collect(Collectors.toList());


                //只要自己的
                List<ZySysJobDO> list = listAll.stream().filter(f -> ids.contains(f.getId() + "")).collect(Collectors.toList());


                atomicInteger.set(0);
                return execComparison(list, FUTURESJOBMAP);
            } else {
                logger.info("本机" + Identity.getUUID() + "没有任务需要处理！");
//                if (MapUtils.nonNull(FUTURESJOBMAP)) {
//                    //找不到的 已经不是自己的 需要移除
//                    FUTURESJOBMAP.entrySet().stream().forEach(m -> {
//                        removeTask(m.getKey());
//                    });
//                }
//
//
//                Map<String, Execer> mapScheduled =HazelcastClient.getInstance().getMap(AutoCheckConfig.getEXECMAPSTR());
//                if (MapUtils.isNull(mapScheduled)) {
//                    restartMulicast();
//                    return;
//                }
//                int cn = atomicInteger.getAndAdd(1);
//
//                Execer execAll = mapScheduled.get(AutoCheckConfig.getEXECALL());
//                if (cn > MAXTIME && execAll == null) {
//                    restartMulicast();
//                }
            }
        } else {

            logger.warn("没有定时任务！");

        }
        return null;
    }


    private List<Task> execComparison(List<ZySysJobDO> list, Map<String, ScheduledExecDTO> FUTURESJOBMAP) {
        CompletableFuture<List<Task>> completableFuture = CompletableFuture.supplyAsync(() -> {
            List<Task> taskList = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(list)) {


                //开始更新
                for (ZySysJobDO job : list) {
                    Task task = new Task();
                    //封装时间
                    String corn = job.getCron();
                    boolean validExpression = CronExpression.isValidExpression(corn);
                    if (!validExpression) {
                        logger.error("无效的cron格式，请重新填写");
                        continue;
                    }

                    String id = job.getId().toString();
                    ScheduledExecDTO scheduledExecDTO = FUTURESJOBMAP.get(id);
                    if (Objects.isNull(scheduledExecDTO)) {

                        //验证是否存在执行类和执行方法
                        task.setJob(job);
                        task.setId(id);
                        task.setCron(corn);
                        task.setOperationType(OperationTypeEnum.ADD);
                        taskList.add(task);


                    } else {
                        String oldCorn = scheduledExecDTO.getCorn();
                        if (!corn.equals(oldCorn) || !scheduledExecDTO.getFuture().isDone()) {
                            //移除更新
                            //    futuresMapNew.remove(id);
                            //验证是否存在执行类和执行方法

                            task.setJob(job);
                            task.setId(id);
                            task.setCron(corn);
                            task.setOperationType(OperationTypeEnum.UPDATE);
                            taskList.add(task);

                        } else if (scheduledExecDTO.getFuture().isDone()) {

                            task.setJob(job);
                            task.setId(id);
                            task.setCron(corn);
                            task.setOperationType(OperationTypeEnum.SUBMITLISTENABLE);
                            taskList.add(task);

                            //   ListenableFuture<?> listenableFuture = threadPoolTaskScheduler.submitListenable(getTask(job));


                        }
                    }

                }
            }

            return taskList;
        });
        try {
            List<Task> tasks = completableFuture.get();
            if (tasks != null) {
                logger.info("执行计划任务完成！");
                return tasks;
            }

        } catch (Exception e) {
            logger.error("执行计划任务错误！", e);
            throw new ZyException("执行计划任务错误！", e);
        }
        return null;
    }


    @Override
    public List<DistribExec> getDistribExecs() {
        Map<String, List<DistribExec>> useMap = HazelcastClient.getInstance().getMap(AutoCheckConfig.getSCHEDULEDUSEMAP());
        //处理自己的数据
        List<DistribExec> distribExecs = useMap.get(Identity.getUUID());
        return distribExecs;
    }

    @Override
    public Execer getExecer() {
        Map<String, Execer> mapScheduled = HazelcastClient.getInstance().getMap(AutoCheckConfig.getEXECMAPSTR());
        if (MapUtils.isNull(mapScheduled)) {
            return null;
        }

        Execer execAll = mapScheduled.get(AutoCheckConfig.getEXECALL());
        return execAll;
    }
}
