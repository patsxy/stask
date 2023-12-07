package com.sy.cc.redis;

import cn.hutool.core.collection.CollectionUtil;

import com.alibaba.fastjson2.JSONObject;
import com.sy.cc.comm.config.AutoCheckConfig;
import com.sy.cc.comm.emuns.AutoCheckTypeEnum;
import com.sy.cc.comm.emuns.ExecerStatusEnum;
import com.sy.cc.comm.emuns.MessageTypeEnum;
import com.sy.cc.comm.entity.*;
import com.sy.cc.comm.service.AutoCompute;
import com.sy.cc.comm.service.UdpMulticastService;
import com.sy.cc.comm.util.HttpUrlConnectionClientUtil;
import com.sy.cc.comm.util.MapUtils;

import com.sy.cc.comm.util.StringUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RedisAutoCompute implements AutoCompute {
    private static final Logger logger = LoggerFactory.getLogger(RedisAutoCompute.class);

    private static final String EXECERSTR = "STASK:EXECER:V1";
    private static final String EXECERSTRLOCK = "STASK:EXECER:LOCK_V1";
    private static final String MASTER = "STASK:MASTER:V1";
    private static final String MASTERLOCK = "STASK:MASTER:LOCK_V1";

    private static final String SCHEDULEDAPPLY = "STASK:SCHEDULED:APPLY_V1";
    private static final String SCHEDULEDLOCKAPPLY = "STASK:SCHEDULED:LOCK_APPLY_V1";

    private static final String SCHEDULEDUSE = "STASK:SCHEDULED:USE_V1";
    private static final String SCHEDULEDLOCKUSE = "STASK:SCHEDULED:LOCK_USE_V1";
    private static final long EXECERSTR_TIME_OUT = 60000000L;

    protected static Set<String> distribUse = new HashSet<>();

    protected static Set<String> distribAppl = new HashSet<>();
    protected static Set<String> distribAllStr = new HashSet<>();
    protected static List<DistribExec> distribAll = new ArrayList<>();


    // protected static ConcurrentHashMap<String, UserInfo> execAllLoacl = new ConcurrentHashMap<>();
    private static final long LOCK_TIME_OUT = 3000L;
    private static long timeOut = 4;
    private static final int ONE = 1;

    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int ZERO = 0;
    private static final long ONESECOND = 1000L;
    //  protected static AtomicBoolean autoStatus = new AtomicBoolean(false);

    protected static AtomicInteger rotation = new AtomicInteger(0);

    public static int getTHREE() {
        return THREE;
    }

    public static String getEXECERSTR() {
        return EXECERSTR;
    }

    public static String getMaster() {
        return MASTER;
    }

    public static String getSCHEDULEDAPPLY() {
        return SCHEDULEDAPPLY;
    }


    public static Long getEXECERSTRTIMOUT() {
        return EXECERSTR_TIME_OUT;
    }

    public static String getSCHEDULEDUSE() {
        return SCHEDULEDUSE;
    }

    public static String getEXECERSTRLOCK() {
        return EXECERSTRLOCK;
    }

    public static Long getLOCKTIMEOUT() {
        return LOCK_TIME_OUT;
    }

    public static class Hold {
        private static UdpMulticastService udpMulticastService;
    }


    public static UdpMulticastService getUdpMulticastService() {
        if (Hold.udpMulticastService != null) {
            return Hold.udpMulticastService;
        }
        Hold.udpMulticastService = null;
        ServiceLoader<UdpMulticastService> autoCompute = ServiceLoader.load(UdpMulticastService.class);
        for (UdpMulticastService dao : autoCompute) {
            Hold.udpMulticastService = dao;
        }

        return Hold.udpMulticastService;
    }


    @Override
    public void hostUpdate(UserInfo userInfo) {
        String uuid = userInfo.getUuid();
        logger.info("开始更新host:" + uuid);
        RedissonClient redisson = RedisClinet.getRedisson();
        RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);
        Execer execAll = execerRBucket.get();
        if (execAll != null) {
            Map<String, UserInfo> execers = execAll.getExecers();
            if (MapUtils.nonNull(execers)) {
                UserInfo userInfo1 = execers.get(uuid);
                if (userInfo1 == null) {
                    //没找到加入新的 并将网络host状态变动
                    addHost(userInfo, execers);
                    //网络变动
                    //autoStatus.set(true);
                    execAll.setExecers(execers);
                    execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
                } else {
                    userInfo1.setTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
                    execers.put(uuid, userInfo1);
                    execAll.setExecers(execers);
                }
            } else {
                //没找到加入新的 并将网络host状态变动
                addHost(userInfo, execers);
                //网络变动
                // autoStatus.set(true);
                execAll.setExecers(execers);
                execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
            }


        } else {
            execAll = new Execer();
            Map<String, UserInfo> execers = new HashMap<>();
            //没找到加入新的 并将网络host状态变动
            addHost(userInfo, execers);
            execAll.setExecers(execers);
            execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());

        }
        RLock lock = redisson.getLock(EXECERSTRLOCK);
        try {
            if (lock.tryLock(TWO, TWO, TimeUnit.SECONDS)) {
                execerRBucket.set(execAll);

                lock.unlock();
            }
        } catch (Exception e) {
            logger.error("获取redis锁失败!", e);
        }
    }

    private static void addHost(UserInfo userInfo, Map<String, UserInfo> execers) {
        String uuid = userInfo.getUuid();
        userInfo.setTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        //没找到加入新的 并将网络host状态变动
        execers.put(uuid, userInfo);

        // execAllLoacl.put(uuid, userInfo);
    }

    @Override
    public void checkHost(UserInfo userInfo) {
        logger.info("开始检查是否超时！");
        RedissonClient redisson = RedisClinet.getRedisson();

        RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);
        Execer execAll = execerRBucket.get();
        RBucket<String> masterRBucket = redisson.getBucket(MASTER);
        String mapMaster = masterRBucket.get();

        if (execAll != null) {
            //  execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
        } else {
            //Hazelcast 传播 造成数据丢失
            execAll = new Execer();
            execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
        }

        Map<String, UserInfo> execAllLoacl = execAll.getExecers();
//        if (MapUtils.isNull(execAllLoacl)) {
//         //   autoStatus.set(false);
//            // execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
//        }

        // Map<String, Long> execers = execAll.getExecers();
        //  Map<String, Long> newExecers = new HashMap<>();
        if (CollectionUtil.isNotEmpty(execAllLoacl)) {
            logger.info("当前master:" + mapMaster);
            List<String> collect = execAllLoacl.entrySet().stream().map(m -> m.getKey()).collect(Collectors.toList());
            boolean present = false;
            if (!StringUtil.isNullOrEmpty(mapMaster) && !collect.contains(mapMaster)) {
                logger.info(present + "删除master:" + mapMaster);

                RLock lock = redisson.getLock(MASTERLOCK);
                try {

                    if (lock.tryLock(THREE, THREE, TimeUnit.SECONDS)) {
                        masterRBucket.delete();

                        lock.unlock();
                    }
                    //  autoStatus.set(true);
                } catch (Exception e) {
                    logger.error("获取redis锁失败!", e);
                }


            }
            RLock lock = redisson.getLock(EXECERSTRLOCK);
            try {

                if (lock.tryLock(TWO, TWO, TimeUnit.SECONDS)) {
                    if (MapUtils.nonNull(execAllLoacl)) {
                        for (Map.Entry<String, UserInfo> host : execAllLoacl.entrySet()) {
//                    if(!userInfo.getUuid().equals(host.getValue().getUuid())){
//                        //不是自己不检查
//                        continue;
//                    }
                            long epochSecond = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
                            UserInfo value = host.getValue();
                            long l = epochSecond - value.getTime();
                            if (timeOut * THREE < l) {
                                logger.info(host.getKey() + "主机超时！");
                                AutoCheckTypeEnum checkType = AutoCheckConfig.getCheckType();
                                if (checkType == null) {
                                    checkType = AutoCheckTypeEnum.HTTP;
                                }
                                switch (checkType) {
                                    case UDP:
                                        CompletableFuture<UdpProtocol> future = CompletableFuture.supplyAsync(() -> {

                                            UdpProtocol udpProtocol = new UdpProtocol<>();
                                            udpProtocol.setUuid(host.getKey());
                                            udpProtocol.setType(MessageTypeEnum.APPLRECEIVE);
                                            udpProtocol.setData(epochSecond);
                                            String string = JSONObject.toJSONString(udpProtocol);
                                            getUdpMulticastService().getSendRMap().put(host.getKey(), udpProtocol);
                                            getUdpMulticastService().sendReceiveMap(udpProtocol);
                                            while (true) {
                                                long epochSecond1 = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
                                                UdpProtocol udpProtocol1 = getUdpMulticastService().getSendRMap().get(host.getKey());
                                                if (null == udpProtocol1) {
                                                    return null;
                                                } else if (udpProtocol1.getType() == MessageTypeEnum.RECEIVE) {
                                                    //  remoteHost(mapMaster, execAll, host);
                                                    return udpProtocol1;
                                                } else if (epochSecond1 - epochSecond > 2) {
                                                    return null;
                                                }
                                            }


                                        });
                                        try {
                                            UdpProtocol udpProtocol1 = future.get(1, TimeUnit.SECONDS);
                                            if (udpProtocol1 == null || udpProtocol1.getType() != MessageTypeEnum.RECEIVE) {
                                                remoteHost(mapMaster, execAll, host);
                                            } else {
                                                value.setTime(epochSecond);
                                                //还没超时的，放入新的list中。
                                                execAllLoacl.put(host.getKey(), value);
                                            }
                                        } catch (Exception e) {
                                            logger.info("再次检查请求超时！");
                                            remoteHost(mapMaster, execAll, host);
                                        }


                                        break;
                                    case HTTP:
                                        try {
                                            String url = "http://" + value.getAddress() + ":" + value.getPort();
                                            String s;
                                            try {
                                                s = HttpUrlConnectionClientUtil.doGet(url);
                                            } catch (Exception e) {
                                                s = "";
                                            }
                                            if (!StringUtil.isNullOrEmpty(s)) {
                                                UserInfo userInfo1 = JSONObject.parseObject(s, UserInfo.class);
                                                if (userInfo1.getUuid().equals(value.getUuid())) {
                                                    value.setTime(epochSecond);
                                                    //还没超时的，放入新的list中。
                                                    execAllLoacl.put(host.getKey(), value);
                                                }
                                            } else {
                                                remoteHost(mapMaster, execAll, host);
                                            }
                                        } catch (Exception e) {
                                            logger.info("端口不存在！", e);
                                            remoteHost(mapMaster, execAll, host);
                                        }

                                        break;
                                }
                            } else {
                                value.setTime(epochSecond);
                                //还没超时的，放入新的list中。
                                execAllLoacl.put(host.getKey(), value);


                            }
                        }
                    }


                    //修改成新的没超时的
                    execAll.setExecers(execAllLoacl);


                    execerRBucket.set(execAll);
                    lock.unlock();
                }

            } catch (Exception e) {
                logger.error("获取redis锁失败!", e);
            }
            //  INSTANCE.getCPSubsystem().getLock(HazelcastAutoCompute.getMASTERSTR()).unlock();
        }
        //  }


    }

    @Override
    public void sycCompute(UserInfo userInfo) {
        RedissonClient redisson = RedisClinet.getRedisson();
        RBucket<String> bucket = redisson.getBucket(MASTER);

        if (bucket != null) {
            String master = bucket.get();
            String myuuid = Identity.getUUID();
            //  String myuuid = userInfo.getUuid();
            if (!StringUtil.isNullOrEmpty(master)) {

                if (master.equals(myuuid)) {

                    RLock lock = redisson.getLock(MASTERLOCK);
                    try {

                        if (lock.tryLock(THREE, THREE, TimeUnit.SECONDS)) {
                            bucket.set(master);
                            logger.info("申请成功：" + myuuid);
                            lock.unlock();
                        }

                    } catch (Exception e) {
                        logger.error("获取redis锁失败!", e);
                    }
                    //取到就是自己是 master  ,自己就要看执行有没变得
                    //添加执行者 ，执行分发
                    logger.info("我是master,需要执行分发");
                    logger.info("我收到的master：" + myuuid);
                    execDistrib(userInfo);

                } else {
                    logger.info("我不是master");
                    logger.info("master是：" + master);
                    //添加执行者
                    addextracted(userInfo);
                }

            } else {
                logger.info("没有master,我申请成master");
                try {

                    RLock lock = redisson.getLock(MASTERLOCK);

                    if (lock.tryLock(TWO, TWO, TimeUnit.SECONDS)) {
                        bucket.set(myuuid);
                        logger.info("申请成功：" + myuuid);
                        lock.unlock();
                    } else {
                        logger.error("申请master锁，失败！");
                    }

                    //添加执行者 ，执行分发
                    execDistrib(userInfo);
                } catch (Exception e) {
                    logger.error("获取redis锁失败!,申请成master失败！", e);
                }

            }
        }


    }


    private static void remoteHost(String master, Execer execAll, Map.Entry<String, UserInfo> host) {
        //有超时的 变动
        RedissonClient redisson = RedisClinet.getRedisson();
        // autoStatus.set(true);
        execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
        if (!StringUtil.isNullOrEmpty(master) && master.equals(host.getKey())) {
            logger.info("master:" + host.getKey() + "超时！清除！" + host.getValue());

            RBucket<String> bucket = redisson.getBucket(MASTER);
            RLock lock = redisson.getLock(MASTERLOCK);
            try {

                if (lock.tryLock(THREE, THREE, TimeUnit.SECONDS)) {
                    bucket.delete();
                    lock.unlock();
                }

            } catch (Exception e) {
                logger.error("获取redis锁失败!", e);
            }
        }
        //有变得
        execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
        // Map<String, UserInfo> newMap = new HashMap<>();
        //    newMap.putAll(execAll.getExecers());
        for (Map.Entry<String, UserInfo> userInfo : execAll.getExecers().entrySet()) {
            if (userInfo.getValue().getUuid().equals(host.getValue().getUuid())) {
                //newMap.remove(userInfo.getKey(), userInfo.getValue());
                execAll.getExecers().remove(userInfo.getKey());
                execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
            }
        }

//        RLock lock = redisson.getLock(MASTERLOCK);
        RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);
        //   execAll.setExecers(newMap);
        RLock lock = redisson.getLock(EXECERSTRLOCK);
        try {
            if (lock.tryLock(TWO, TWO, TimeUnit.SECONDS)) {
                execerRBucket.set(execAll);
                lock.unlock();
            }

        } catch (Exception e) {
            logger.error("获取redis锁失败!", e);
        }

    }


    private static void execDistrib(UserInfo userInfo) {

        Execer execAll = addextracted(userInfo);
        try {
            Thread.sleep(ONESECOND);
        } catch (Exception e) {
            logger.error("线程休息1秒问题！");
        }
        //休息1秒后，自己master 开始调度分发
        //添加了执行者 ，一定义有的
        if (execAll != null) {
            RedissonClient redisson = RedisClinet.getRedisson();
            RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);

//            Map<String, UserInfo> execAllLoacl = execerRBucket.get().getExecers();

//            Map<String, UserInfo> execers = execAll.getExecers();
            Map<String, UserInfo> execers = execAll.getExecers();
            if (MapUtils.nonNull(execers)) {
                logger.info("执行者名单(" + execers.size() + "):" + execers.toString());
                Set<DistribExec> applMap = redisson.getSet(SCHEDULEDAPPLY);
                int status = execAll.getStatus();
                //有组变动 或者 applMap 申请组不为空 需要重新分配
                if (MapUtils.isNull(execers)) {
                    logger.info(Identity.getUUID() + "目前，没有执行组！");
                    Set<DistribExec> useMap = redisson.getSet(SCHEDULEDUSE);
                    logger.info("申请组：-》" + JSONObject.toJSONString(applMap));
                    logger.info("使用组：-》" + JSONObject.toJSONString(useMap));

                } else if (status == ExecerStatusEnum.CHANGE.getCode() || CollectionUtil.isNotEmpty(applMap)) {
                    logger.info("定时任务，开始重新分配........");
                    adjustDistrib(applMap);

                    //修改执行组状态 重新分配完成
                    //  autoStatus.set(false);
                    execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
                    RLock lock = redisson.getLock(EXECERSTRLOCK);
                    try {
                        if (lock.tryLock(THREE, THREE, TimeUnit.SECONDS)) {
                            // RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);
                            execerRBucket.set(execAll);
                            lock.unlock();
                        }
                        //RedisUtil.releaseLock(EXECERSTRLOCK, Identity.getUUID());

                    } catch (Exception e) {
                        logger.error("获取redis锁失败!", e);
                    }

                } else {
                    logger.info(Identity.getUUID() + "定时任务，没变动无需要重新分配！按照userMap执行！");
                    Set<DistribExec> useMap = redisson.getSet(SCHEDULEDUSE);
                    logger.info("申请组：-》" + JSONObject.toJSONString(applMap));
                    logger.info("使用组：-》" + JSONObject.toJSONString(useMap));

                }

            } else {
                logger.error("执行者为空！");
            }


        } else {
            //这行 应该不会执行。 执行 代码前面有错！
            logger.error("执行者不存在");
        }
    }


    private static Execer addExecer(UserInfo userInfo, Execer execAll) {
        RedissonClient redisson = RedisClinet.getRedisson();
        RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);

        Map<String, UserInfo> uuidList = execerRBucket.get().getExecers();
        if (MapUtils.isNull(uuidList)) {
            uuidList = new HashMap<>();
        }
        addHost(userInfo, uuidList);

        execAll.getExecers().putAll(uuidList);
        //  autoStatus.set(true);
        execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());


        return execAll;
    }

    private static void adjustDistrib(Set<DistribExec> applMap) {
        RedissonClient redisson = RedisClinet.getRedisson();
        RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);
        Execer execer = execerRBucket.get();
        if (execer != null) {
            Map<String, UserInfo> execers = execer.getExecers();

            List<String> execerList = execers.entrySet().stream().map(m -> m.getKey()).collect(Collectors.toList());

            //  boolean status = autoStatus.get();


            if (CollectionUtil.isNotEmpty(execerList)) {
                int size = execerList.size();

                Set<DistribExec> userMap = redisson.getSet(SCHEDULEDUSE);

                List<DistribExec> distribExecListnew = new ArrayList<>();
                if (userMap != null) {
                    for (DistribExec distribExec : userMap) {
                        //  DistribExec distribExec = JSONObject.parseObject(userMap.get(i) + "", DistribExec.class);
                        distribExecListnew.add(distribExec);
                    }

                }
                //清楚以前记录
                distribAllStr.clear();
                distribAll.clear();
                distribUse.clear();

                if (execer.getStatus() == ExecerStatusEnum.CHANGE.getCode()) {
                    logger.info("网络执行主机变动需要重新分配所有任务");
                    //不管是否切换 都需要执行 申请组的分配
                    Integer cn = applGroupDistrib(applMap);


                    //集群组 状态切换 需要执行使用组的变得
                    useGroupDistrib(cn, distribExecListnew);
                    //加入申请
                    distribAllStr.addAll(distribAppl);
                    //加入使用 这个可能为空
                    distribAllStr.addAll(distribUse);


                } else {
                    logger.info("申请组变动需要分配申请组的任务");
                    //不管是否切换 都需要执行 申请组的分配
                    applGroupDistrib(applMap);

                    if (CollectionUtil.isNotEmpty(distribExecListnew)) {
                        // 处理保留现有的userMap
                        for (DistribExec value : distribExecListnew) {

                            distribUse.add(value.getTaskId());
                            distribAll.add(value);
                        }
                    }
                    //加入申请 不在使用中的
                    distribAllStr.addAll(distribAppl.stream().filter(f -> !distribUse.contains(f)).collect(Collectors.toSet()));


                }

                List<String> distribExecList = distribAllStr.stream().collect(Collectors.toSet()).stream().collect(Collectors.toList());
                int zcn = execerList.size();
                logger.info("可分配主机数量：" + zcn);
                if (rotation.get() >= zcn) {
                    rotation.set(0);
                }

                int cnt = rotation.getAndAdd(1);
                //重新分配下
                for (int i = 0; i < distribExecList.size(); i++) {
                    cnt = (i + cnt) % size;
                    String taskId = distribExecList.get(i);
                    String hoststr = execerList.get(cnt);
                    DistribExec distribExec = DistribExec.builder().taskId(taskId).uuid(hoststr).build();
                    distribAll.add(distribExec);

                }

                Map<String, List<DistribExec>> collect = distribAll.stream().collect(Collectors.groupingBy(DistribExec::getUuid));

                logger.info("分配列表--》", collect.toString());
                RLock lock = redisson.getLock(SCHEDULEDLOCKUSE);
                try {
                    if (lock.tryLock(THREE, THREE, TimeUnit.SECONDS)) {
                        //清空使用组
                        userMap.clear();
                        //添加使用组
                        List<DistribExec> collect1 = distribAll.stream().collect(Collectors.toSet()).stream().collect(Collectors.toList());
                        userMap.addAll(collect1);


                        // RedisUtil.set(SCHEDULEDUSE, userMap, EXECERSTR_TIME_OUT);
                        lock.unlock();
                    }
                } catch (Exception e) {
                    logger.error("获取redis锁失败!", e);
                }
            }
        }
    }


    private static Integer useGroupDistrib(int userCn, List<DistribExec> userMap) {
        if (CollectionUtil.isNotEmpty(userMap)) {
            //清空本地所有 重新分配
            distribUse.clear();
            int cn = userCn;


            for (int i = 0; i < userMap.size(); i++) {


                cn = cn + ONE;


                DistribExec distribExec = userMap.get(i);

                //加入分配组
                distribUse.add(distribExec.getTaskId());

                //移除当前
                userMap.remove(distribExec);

            }


            return cn;

        }
        return 0;
    }

    private static Integer applGroupDistrib(Set<DistribExec> applMap) {


        int cn = 0;


        if (applMap != null) {
            distribAppl.clear();

            for (DistribExec distribExec : applMap) {
                cn++;

                //添加到申请组list
                distribAppl.add(distribExec.getTaskId());

                //移除原有的申请组
                // applMap.remove(distribExec);
            }
            //移除原有的申请组
            applMap.clear();
        }


        return cn;

    }

    /**
     * 加入执行组
     *
     * @param
     * @return
     */
    private static Execer addextracted(UserInfo userInfo) {
        String uuid = userInfo.getUuid();
        RedissonClient redisson = RedisClinet.getRedisson();
        RBucket<Execer> execerRBucket = redisson.getBucket(EXECERSTR);
        Execer execAll = null;

        if (execerRBucket != null) {
            execAll = execerRBucket.get();
            // logger.info("网络组！判断是否存在！");
            Map<String, UserInfo> execers = execAll.getExecers();
            if (null == execers.get(uuid)) {


                //加入这台
                addHost(userInfo, execers);

                //变动
                // autoStatus.set(true);
                execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());

                logger.info("网络组变动！");
                RLock lock = redisson.getLock(EXECERSTRLOCK);
                try {
                    if (lock.tryLock(TWO, TWO, TimeUnit.SECONDS)) {

                        execerRBucket.set(execAll);
                        lock.unlock();
                    }
                } catch (Exception e) {
                    logger.error("获取redis锁失败!", e);
                }
            }
            //有了就不做任何操作！
        } else {
            execAll = new Execer();
            //  autoStatus.set(true);
            execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
            logger.info("网络组变动！");
            Map<String, UserInfo> execers = new HashMap<>();
            execers.put(Identity.getUUID(), userInfo);
            execAll.setExecers(execers);

            RLock lock = redisson.getLock(EXECERSTRLOCK);
            try {
                if (lock.tryLock(TWO, TWO, TimeUnit.SECONDS)) {

                    execerRBucket.set(execAll);
                    lock.unlock();
                }
            } catch (Exception e) {
                logger.error("获取redis锁失败!", e);
            }
        }


        return execAll;
    }

}
