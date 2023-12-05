package com.sy.cc.service.impl;

import cn.hutool.core.collection.CollectionUtil;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sy.cc.comm.entity.*;
import com.sy.cc.comm.exception.ZyException;
import com.sy.cc.comm.service.IScheduleJobService;
import com.sy.cc.comm.util.MapUtils;

import com.sy.cc.mapper.ZySysJobsMapper;
import com.sy.cc.multicast.UdpMulticast;

import com.sy.cc.service.IScheduleService;
import com.sy.cc.util.SpringUtil;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements IScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);


    // 1、创建ScheduledExecutorService线程池，做定时器
    ScheduledExecutorService pool = Executors.newScheduledThreadPool(2000);

    public static Map<String, ScheduledExecDTO> FUTURESJOBMAP = new ConcurrentHashMap<>();

    @Autowired
    private ZySysJobsMapper jobMng;


    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Value("${isOpenJob:false}")
    private boolean isOpenJob;


    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private static final int MAXTIME = 4;

    public static class Hold {
        private static IScheduleJobService scheduleJobService;
    }


    public static IScheduleJobService getScheduleJobService() {
        if (Hold.scheduleJobService != null) {
            return Hold.scheduleJobService;
        }
        Hold.scheduleJobService = null;
        ServiceLoader<IScheduleJobService> autoCompute = ServiceLoader.load(IScheduleJobService.class);
        for (IScheduleJobService dao : autoCompute) {
            Hold.scheduleJobService = dao;
        }

        return Hold.scheduleJobService;
    }

    @Override
    public void execDistrib() {
        //执行定时 更新本地job
        execSchedule();
    }


    /**
     * 每15秒钟 更新查询 有没变动
     **/
    @Scheduled(fixedRateString = "${cron.task:15000}")
    private void execSchedule() {
        try {
            List<DistribExec> distribExecs = getScheduleJobService().getDistribExecs();
            List<String> ids = new ArrayList<>();
            List<ZySysJobDO> listAll = jobMng.selectList(null);
            if (CollectionUtil.isNotEmpty(distribExecs)) {

                for (int i = 0; i < distribExecs.size(); i++) {
                    Object distribExec1 = distribExecs.get(i);
                    if (distribExec1 instanceof DistribExec) {
                        ids.add(((DistribExec) distribExec1).getTaskId());
                    } else if (distribExec1 instanceof JSONObject) {
                        DistribExec distribExec = JSONObject.parseObject(distribExec1 + "", DistribExec.class);
                        ids.add(distribExec.getTaskId());
                    }
                }


                if (MapUtils.nonNull(FUTURESJOBMAP)) {
                    //找不到的 已经不是自己的 需要移除
                    FUTURESJOBMAP.entrySet().stream().filter(f -> !ids.contains(f.getKey())).forEach(m -> {
                        removeTask(m.getKey());
                    });
                }

                List<String> currCollect = listAll.stream().map(m -> m.getId() + "").collect(Collectors.toList());
                List<String> delcollect = ids.stream().filter(f -> !currCollect.contains(f)).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(delcollect)) {
                    getScheduleJobService().delUseList(delcollect);
                }

            }


            List<Task> task = getScheduleJobService().execScheduleExec(listAll, FUTURESJOBMAP);
            if (CollectionUtil.isNotEmpty(task)) {
                List<ZySysJobDO> collect = task.stream().map(Task::getJob).collect(Collectors.toList());
                if (MapUtils.nonNull(FUTURESJOBMAP)) {
                    //找不到的 已经不是自己的 需要移除
                    FUTURESJOBMAP.entrySet().stream().filter(f -> !collect.contains(f.getKey())).forEach(m -> {
                        removeTask(m.getKey());
                    });
                }
            } else {
                FUTURESJOBMAP.entrySet().stream().forEach(m -> {
                    removeTask(m.getKey());
                });
            }


            if (CollectionUtil.isNotEmpty(task)) {
                task.forEach(t -> {
                    if (t != null) {
                        switch (t.getOperationType()) {
                            case ADD:
                                ScheduledExecDTO scheduledExecDTONew = addTask(getTask(t.getJob()), new CronTrigger(t.getCron()), t.getId(), t.getCron());
                                if (Objects.nonNull(scheduledExecDTONew)) {
                                    FUTURESJOBMAP.put(t.getId(), scheduledExecDTONew);
                                }
                                break;
                            case UPDATE:
                                ScheduledExecDTO scheduledExecDTOUpdate = updateTask(getTask(t.getJob()), new CronTrigger(t.getCron()), t.getId(), t.getCron());
                                if (Objects.nonNull(scheduledExecDTOUpdate)) {
                                    FUTURESJOBMAP.put(t.getId(), scheduledExecDTOUpdate);
                                }
                                break;
                            case REMOVE:


                                break;
                            case SUBMITLISTENABLE:
                                ListenableFuture<?> listenableFuture = threadPoolTaskScheduler.submitListenable(getTask(t.getJob()));
                                break;

                        }

                    }
                });
            }
            int cn = atomicInteger.getAndAdd(1);
            Execer execAll = getScheduleJobService().getExecer();
            if (cn > MAXTIME && execAll == null) {
                restartMulicast();
                atomicInteger.set(0);
            }
        } catch (Exception e) {
            logger.error("执行任务计划错误！", e);
        }
    }


    private void restartMulicast() {
        //大于
        try {
            //关闭主播
            NioDatagramChannel channel = UdpMulticast.getCHANNEL();
            channel.close();
            //重新链接
            UdpMulticast.buildMulticast();
        } catch (Exception e) {
            logger.error("start stask  fail!", e);
        }

        atomicInteger.set(0);
    }


    /**
     * @param key
     * @return
     */
    public boolean hasTask(String key) {
        return FUTURESJOBMAP.get(key) != null;
    }


    /**
     * 添加定时任务，如果任务名重复则抛出异常
     *
     * @param task    任务
     * @param trigger 定时器
     * @param key     任务名
     * @return
     */
    public ScheduledExecDTO addTask(Runnable task, Trigger trigger, String key, String corn) {
        ScheduledExecDTO scheduledExecDTOOld = FUTURESJOBMAP.get(key);
        if (Objects.nonNull(scheduledExecDTOOld)) {
            removeTask(key);

            logger.info("添加任务key名： " + key + "重复");
            return null;
        }


        ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(task, trigger);

        ScheduledExecDTO scheduledExecDTO = new ScheduledExecDTO(future, corn);
        FUTURESJOBMAP.put(key, scheduledExecDTO);
        return scheduledExecDTO;

    }

    /**
     * 移除定时任务
     *
     * @param key 任务名
     * @return
     */
    public boolean removeTask(String key) {
        ScheduledExecDTO toBeRemovedFuture = FUTURESJOBMAP.get(key);
        if (!Objects.isNull(toBeRemovedFuture)) {
            toBeRemovedFuture.getFuture().cancel(true);
            FUTURESJOBMAP.remove(key);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 更新定时任务
     * 有可能会出现：1、旧的任务不存在，此时直接添加新任务；
     * 2、旧的任务存在，先删除旧的任务，再添加新的任务
     *
     * @param task    任务
     * @param trigger 定时器
     * @param key     任务名称
     * @return
     */
    public ScheduledExecDTO updateTask(Runnable task, Trigger trigger, String key, String cron) {
        ScheduledExecDTO toBeRemovedFuture = FUTURESJOBMAP.get(key);
        // 存在则删除旧的任务
        if (!Objects.isNull(toBeRemovedFuture)) {
            toBeRemovedFuture.getFuture().cancel(true);
            FUTURESJOBMAP.remove(key);
        }
        return addTask(task, trigger, key, cron);

    }


    /**
     * 封装任务
     *
     * @param job
     * @return
     */
    public Runnable getTask(ZySysJobDO job) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String clzzNamestr = job.getClazzName();
                if (StringUtils.isBlank(clzzNamestr)) {
                    throw new ZyException("任务执行类空");
                }

                if (clzzNamestr.indexOf(".") > -1) {
                    String[] s = clzzNamestr.split(".");
                    if (s.length > 0) {
                        clzzNamestr = s[s.length - 1];
                    }
                }

                Object clazz = SpringUtil.getObject(clzzNamestr);
                try {
                    if (Objects.nonNull(clazz)) {
                        String params = job.getParams();
                        if (StringUtils.isNotBlank(params)) {
                            JSONObject from = JSONObject.from(job);
                            String[] para = params.split(",");
                            //先让 newPara==para,后面更改
                            String[] newPara = para;
                            if (ArrayUtil.isNotEmpty(para)) {
                                //判断参数是否为空
                                for (int i = 0; i < para.length; i++) {
                                    String newKey = from.getString(para[i]);
                                    if (StringUtils.isNotBlank(newKey)) {
                                        //当不等空找到内容，更改
                                        newPara[i] = newKey;
                                    }

                                }

                            }

                            clazz.getClass().getMethod(job.getClazzMethod(), String.class).invoke(clazz, newPara);
//
                        } else {
                            clazz.getClass().getMethod(job.getClazzMethod()).invoke(clazz);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

        };
        return runnable;
    }


}
