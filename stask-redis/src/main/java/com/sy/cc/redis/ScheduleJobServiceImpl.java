package com.sy.cc.redis;

import cn.hutool.core.collection.CollectionUtil;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import com.sy.cc.comm.emuns.OperationTypeEnum;
import com.sy.cc.comm.entity.*;
import com.sy.cc.comm.exception.ZyException;
import com.sy.cc.comm.service.IScheduleJobService;


import org.quartz.CronExpression;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScheduleJobServiceImpl implements IScheduleJobService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleJobServiceImpl.class);

    @Override
    public void execDistrib(List<ZySysJobDO> listAll, Map<String, ScheduledExecDTO> FUTURESJOBMAP) {
        //执行定时 更新本地job
        execScheduleExec(listAll, FUTURESJOBMAP);
    }

    @Override
    public void delUseList(List<String> zySysJobDOS) {
        if (CollectionUtil.isNotEmpty(zySysJobDOS)) {
            RedissonClient redisson = RedisClinet.getRedisson();
            Set<DistribExec> useMap = redisson.getSet(RedisAutoCompute.getSCHEDULEDUSE());
            if (CollectionUtils.isNotEmpty(useMap)) {
                Set<DistribExec> currCollect = useMap.stream().filter(f -> !zySysJobDOS.contains(f.getTaskId())).collect(Collectors.toSet());
                RLock lock = redisson.getLock(RedisAutoCompute.getEXECERSTRLOCK());
                //加锁
                try {
                    if (lock.tryLock(RedisAutoCompute.getTHREE(), RedisAutoCompute.getTHREE(), TimeUnit.SECONDS)) {// 阻塞式等待
                        useMap.clear();
                        useMap.addAll(currCollect);
                        lock.unlock();
                    }
                } catch (Exception e) {
                    logger.error("获取redis锁失败!", e);
                }
            }
        }
    }

    @Override
    public void sendListAppl(List<ZySysJobDO> zySysJobDOS) {
        if (CollectionUtil.isNotEmpty(zySysJobDOS)) {
            RedissonClient redisson = RedisClinet.getRedisson();
            RLock lock = redisson.getLock(RedisAutoCompute.getEXECERSTRLOCK());
            //加锁
            try {
                if (lock.tryLock(RedisAutoCompute.getTHREE(), RedisAutoCompute.getTHREE(), TimeUnit.SECONDS)) {// 阻塞式等待
                    Set<DistribExec> applMap = redisson.getSet(RedisAutoCompute.getSCHEDULEDAPPLY());
                    Set<DistribExec> useMap = redisson.getSet(RedisAutoCompute.getSCHEDULEDUSE());

                    String uuid = Identity.getUUID();
                    if (CollectionUtils.isNotEmpty(useMap)) {
                        List<String> collect = zySysJobDOS.stream().map(m -> m.getId() + "").collect(Collectors.toList());
                        List<String> allUser = new ArrayList<>();


                        List<String> oldTaskId = useMap.stream().map(DistribExec::getTaskId).collect(Collectors.toList());
                        Set<DistribExec> newDistribExecs = zySysJobDOS.stream().filter(f -> !oldTaskId.contains(f.getId() + ""))
                                .map(m -> DistribExec.builder().uuid(uuid).taskId(m.getId() + "").build())
                                .collect(Collectors.toSet());
                        Set<DistribExec> surplus = applMap.stream().filter(f -> !collect.contains(f.getTaskId())).collect(Collectors.toSet());

                        //加入历史
                        if (CollectionUtil.isNotEmpty(surplus)) {
                            newDistribExecs.addAll(surplus);
                        }

                        //清除历史
                        applMap.clear();
                        //加入 自己
                        applMap.addAll(newDistribExecs);


                    } else {
                        applMap.addAll(zySysJobDOS.stream()
                                .map(m -> DistribExec.builder().uuid(uuid).taskId(m.getId() + "").build())
                                .collect(Collectors.toList()));
                    }


                    lock.unlock();
                }

            } catch (Exception e) {
                logger.error("获取redis锁失败!", e);
            }

        }


    }

    @Override
    public List<Task> execScheduleExec(List<ZySysJobDO> listAll, Map<String, ScheduledExecDTO> FUTURESJOBMAP) {
        RedissonClient redisson = RedisClinet.getRedisson();

        Set<DistribExec> useMap = redisson.getSet(RedisAutoCompute.getSCHEDULEDUSE());
        //  List<DistribExec> applMap = RedisUtil.get(RedisAutoCompute.getSCHEDULEDAPPLY(), List.class);
        Set<DistribExec> applMap = redisson.getSet(RedisAutoCompute.getSCHEDULEDAPPLY());

        if (useMap != null) {

            List<String> allUser = new ArrayList<>();
            List<DistribExec> distribExecList = new ArrayList<>();
            for (DistribExec distribExec : useMap) {
//                DistribExec distribExec = JSONObject.parseObject(useMap.get(i) + "", DistribExec.class);
                allUser.add(distribExec.getTaskId());
                distribExecList.add(distribExec);
            }

            allUser.addAll(applMap.stream().map(DistribExec::getTaskId).collect(Collectors.toSet()));

            List<ZySysJobDO> newappls = listAll.stream().filter(f -> !allUser.contains(f.getId() + "")).collect(Collectors.toList());

            if (CollectionUtil.isNotEmpty(newappls)) {
                sendListAppl(newappls);
            }
            //现在库中的id
            List<String> currDbId = listAll.stream().map(m -> m.getId() + "").collect(Collectors.toList());
            //需要移除的id
            List<DistribExec> currcollect = distribExecList.stream().filter(f -> currDbId.contains(f.getTaskId())).collect(Collectors.toList());


            //处理自己的数据
            List<DistribExec> distribExecs = currcollect.stream().filter(f -> f.getUuid().equals(Identity.getUUID())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(distribExecs)) {
                List<String> ids = distribExecs.stream().map(DistribExec::getTaskId).collect(Collectors.toList());


                //只要自己的
                List<ZySysJobDO> list = listAll.stream().filter(f -> ids.contains(f.getId() + "")).collect(Collectors.toList());
                return execComparison(list, FUTURESJOBMAP);


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
        RedissonClient redisson = RedisClinet.getRedisson();
        //处理自己的数据
        Set<DistribExec> distribExecs = redisson.getSet(RedisAutoCompute.getSCHEDULEDUSE());
        return distribExecs.stream().collect(Collectors.toList());

    }

    @Override
    public Execer getExecer() {
        RedissonClient redisson = RedisClinet.getRedisson();
        RBucket<Execer> execAll = redisson.getBucket(RedisAutoCompute.getEXECERSTR());
        if (execAll == null) {
            return null;
        }
        return execAll.get();
    }


}
