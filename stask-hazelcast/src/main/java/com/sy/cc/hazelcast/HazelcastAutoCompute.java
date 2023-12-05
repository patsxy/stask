package com.sy.cc.hazelcast;


import com.alibaba.fastjson2.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.util.CollectionUtil;
import com.hazelcast.internal.util.StringUtil;

import com.sy.cc.comm.config.AutoCheckConfig;
import com.sy.cc.comm.emuns.AutoCheckTypeEnum;
import com.sy.cc.comm.emuns.ExecerStatusEnum;
import com.sy.cc.comm.emuns.MessageTypeEnum;
import com.sy.cc.comm.entity.*;
import com.sy.cc.comm.service.AutoCompute;
import com.sy.cc.comm.service.UdpMulticastService;
import com.sy.cc.comm.util.HttpUrlConnectionClientUtil;
import com.sy.cc.comm.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HazelcastAutoCompute implements AutoCompute {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastAutoCompute.class);

    public static  ConcurrentHashMap<String, UserInfo>  getExecAllLoacl(){
        return    execAllLoacl;
    }

    protected static List<String> distribUse = new ArrayList<>();

    protected static List<String> distribAppl = new ArrayList<>();
    protected static List<String> distribAllStr = new ArrayList<>();
    protected static List<DistribExec> distribAll = new ArrayList<>();


    protected static ConcurrentHashMap<String, UserInfo> execAllLoacl = new ConcurrentHashMap<>();

    private static int timeOut = 4;
    private static final int ONE = 1;

    private static final int THREE = 3;
    private static final int ZERO = 0;
    private static final long ONESECOND = 1000L;


    protected static AtomicBoolean autoStatus =new AtomicBoolean(false);

    protected static AtomicInteger rotation = new AtomicInteger(0);




    public  static long  getONESECOND(){
        return  ONESECOND;
    }

    public static int getZERO(){
        return ZERO;
    }

    public static  int getTHREE(){
        return  THREE;
    }

    public static int getONE(){
        return  ONE;
    }


    public static int getTIMEOUT(){
        return timeOut;
    }


    private static HazelcastInstance INSTANCE = HazelcastClient.getInstance();

    public static HazelcastInstance getINSTANCE() {
        return INSTANCE;
    }

    public static Map<String, Execer> getEXECMAP() {

        return INSTANCE.getMap(AutoCheckConfig.getEXECMAPSTR());
    }

    public static Map<String, String> getMASTER() {

        return INSTANCE.getMap(AutoCheckConfig.getMASTERSTR());
    }


    public static class Hold {
        private static UdpMulticastService udpMulticastService;
    }


    public static UdpMulticastService getUdpMulticastService(){
        if(Hold.udpMulticastService!=null){
            return  Hold.udpMulticastService;
        }
        Hold.udpMulticastService=null;
        ServiceLoader<UdpMulticastService> autoCompute = ServiceLoader.load(UdpMulticastService.class);
        for (UdpMulticastService dao : autoCompute) {
            Hold.udpMulticastService=dao;
        }

        return  Hold.udpMulticastService;
    }
    public static void clear() {

        Map<String, Integer> mapMaster = INSTANCE.getMap(AutoCheckConfig.getMASTERSTR());
        Map<String, Execer> mapScheduled = INSTANCE.getMap(AutoCheckConfig.getEXECMAPSTR());
        mapMaster.clear();
        mapScheduled.clear();
    }

    @Override
    public  void sycCompute(UserInfo userInfo) {


        Map<String, String> mapMaster = INSTANCE.getMap(AutoCheckConfig.getMASTERSTR());
        String myuuid = Identity.getUUID();
        //  String myuuid = userInfo.getUuid();
        if (MapUtils.nonNull(mapMaster)) {
            String master = mapMaster.get(AutoCheckConfig.getMASTER());
            if (!StringUtil.isNullOrEmpty(master) && master.equals(myuuid)) {
                //取到就是自己是 master  ,自己就要看执行有没变得
                //添加执行者 ，执行分发
                logger.info("我是master,需要执行分发");
                logger.info("我收到的master：" + myuuid);
                execDistrib(userInfo);

            } else {
                logger.info("我不是master");
                logger.info("master是：" + JSONObject.toJSONString(mapMaster));
                //添加执行者
                addextracted(userInfo);
            }

        } else {
            logger.info("没有master,我申请成masert");
            if (INSTANCE.getCPSubsystem().getLock(AutoCheckConfig.getMASTERSTR()).tryLock(2000, TimeUnit.MILLISECONDS)) {
                addMaster(mapMaster, myuuid);
                //添加执行者 ，执行分发
                execDistrib(userInfo);
                logger.info("申请成功：" + JSONObject.toJSONString(mapMaster));
                INSTANCE.getCPSubsystem().getLock(AutoCheckConfig.getMASTERSTR()).unlock();

            }
        }


    }

    private static void execDistrib(UserInfo userInfo) {
        Map<String, Execer> addextractedMap = addextracted(userInfo);
        try {
            Thread.sleep(getONESECOND());
        } catch (Exception e) {
            logger.error("线程休息1秒问题！");
        }
        //休息1秒后，自己master 开始调度分发
        //添加了执行者 ，一定义有的
        if (MapUtils.nonNull(addextractedMap)) {
            Execer execAll = addextractedMap.get(AutoCheckConfig.getEXECALL());
//            Map<String, UserInfo> execers = execAll.getExecers();
            Map<String, UserInfo> execers =  execAllLoacl;
            if (MapUtils.nonNull(execers)) {
                logger.info("执行者名单("+execers.size()+"):" + execers.toString());
                Map<String, List<DistribExec>> applMap = INSTANCE.getMap(AutoCheckConfig.getSCHEDULEDAPPLYMAP());
                //有组变动 或者 applMap 申请组不为空 需要重新分配
                if(execAllLoacl==null){
                    logger.info(Identity.getUUID()+ "目前，没有执行组！");
                    Map<String, List<DistribExec>> useMap = INSTANCE.getMap(AutoCheckConfig.getSCHEDULEDUSEMAP());
                    logger.info("申请组：-》"+JSONObject.toJSONString(applMap));
                    logger.info("使用组：-》" + JSONObject.toJSONString(useMap));

                } else if (autoStatus.get() || MapUtils.nonNull(applMap)) {
                    logger.info("定时任务，开始重新分配........");
                    adjustDistrib(applMap);

                    //修改执行组状态 重新分配完成
                    autoStatus.set(false);
                    execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
                    addextractedMap.clear();
                    addextractedMap.put(AutoCheckConfig.getEXECALL(), execAll);

                } else {
                    logger.info(Identity.getUUID()+ "定时任务，没变动无需要重新分配！按照userMap执行！");
                    Map<String, List<DistribExec>> useMap = INSTANCE.getMap(AutoCheckConfig.getSCHEDULEDUSEMAP());
                    logger.info("申请组：-》"+JSONObject.toJSONString(applMap));
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

    /**
     * 加入执行组
     *
     * @param
     * @return
     */
    private static Map<String, Execer> addextracted(UserInfo userInfo) {
        String uuid = userInfo.getUuid();
        Map<String, Execer> mapScheduled = INSTANCE.getMap(AutoCheckConfig.getEXECMAPSTR());
        if (MapUtils.nonNull(mapScheduled)) {
            Execer execAll = mapScheduled.get(AutoCheckConfig.getEXECALL());
            if (execAll != null) {
                // logger.info("网络组！判断是否存在！");
                Map<String, UserInfo> execers = execAll.getExecers();
                if (null == execers.get(uuid)) {


                    //加入这台
                    addHost(userInfo, execers);

                    //变动
                    autoStatus.set(true);
                    execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
                    mapScheduled.put(AutoCheckConfig.getEXECALL(), execAll);
                    logger.info("网络组变动！");
                }
            } else {
                addExecer(userInfo, mapScheduled);
            }

        } else {
            addExecer(userInfo, mapScheduled);
        }
        return mapScheduled;
    }

    private static void addMaster(Map<String, String> mapMaster, String uuid) {
        Map<String, String> newMapMaster = new HashMap<>();
        newMapMaster.put(AutoCheckConfig.getMASTER(), uuid);
        mapMaster.putAll(newMapMaster);
    }

    private static Map<String, Execer> addExecer(UserInfo userInfo, Map<String, Execer> mapScheduled) {
        Map<String, Execer> newMapScheduled = new HashMap<>();
        Execer execer = new Execer();

        Map<String, UserInfo> uuidList = execAllLoacl;
        if (MapUtils.isNull(uuidList)) {
            uuidList = new HashMap<>();
        }
        addHost(userInfo, uuidList);

        execer.getExecers().putAll(uuidList);
        autoStatus.set(true);
        execer.setStatus(ExecerStatusEnum.CHANGE.getCode());
        newMapScheduled.put(AutoCheckConfig.getEXECALL(), execer);

        mapScheduled.putAll(newMapScheduled);

        return mapScheduled;
    }

    private static void adjustDistrib(Map<String, List<DistribExec>> applMap) {

            Map<String, UserInfo> execers = execAllLoacl;
            List<String> execerList = execers.entrySet().stream().map(m -> m.getKey()).collect(Collectors.toList());

            boolean status = autoStatus.get();

            if (CollectionUtil.isNotEmpty(execerList)) {
                int size = execerList.size();
                Map<String, List<DistribExec>> userMap = INSTANCE.getMap(AutoCheckConfig.getSCHEDULEDUSEMAP());
                //清楚以前记录
                distribAllStr.clear();
                distribAll.clear();
                distribUse.clear();

                if (status) {
                    logger.info("网络执行主机变动需要重新分配所有任务");
                    //不管是否切换 都需要执行 申请组的分配
                    Integer cn = applGroupDistrib(applMap);


                    //集群组 状态切换 需要执行使用组的变得
                    useGroupDistrib(cn, userMap);
                    //加入申请
                    distribAllStr.addAll(distribAppl);
                    //加入使用 这个可能为空
                    distribAllStr.addAll(distribUse);


                } else {
                    logger.info("申请组变动需要分配申请组的任务");
                    //不管是否切换 都需要执行 申请组的分配
                    applGroupDistrib(applMap);


                    // 处理保留现有的userMap
                    for (Map.Entry<String, List<DistribExec>> map : userMap.entrySet()) {
                        List<DistribExec> value = map.getValue();
                        distribUse.addAll(value.stream().map(DistribExec::getTaskId).collect(Collectors.toSet()));
                        distribAll.addAll(value);
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
                //清空使用组
                userMap.clear();
                //添加使用组
                userMap.putAll(collect);


            }

    }

    private static Integer useGroupDistrib(int userCn, Map<String, List<DistribExec>> userMap) {
        if (MapUtils.nonNull(userMap)) {
            //清空本地所有 重新分配
            distribUse.clear();
            int cn = userCn;
            for (Map.Entry<String, List<DistribExec>> map : userMap.entrySet()) {

                List<DistribExec> useGroup = map.getValue();
                if (CollectionUtil.isNotEmpty(useGroup)) {

                    for (int i = 0; i < useGroup.size(); i++) {
                        cn = cn + ONE;

                        DistribExec distribExec = useGroup.get(i);

                        //加入分配组
                        distribUse.add(distribExec.getTaskId());

                    }
                    //移除使用组
                    useGroup.clear();
                }
                //移除当前
                userMap.remove(map.getKey());

            }
            return cn;

        }
        return 0;
    }

    private static Integer applGroupDistrib(Map<String, List<DistribExec>> applMap) {
        if (MapUtils.nonNull(applMap)) {
            distribAppl.clear();
            int cn = 0;
            for (Map.Entry<String, List<DistribExec>> map : applMap.entrySet()) {
                List<DistribExec> applGroup = map.getValue();

                if (CollectionUtil.isNotEmpty(applGroup)) {

                    for (int i = 0; i < applGroup.size(); i++) {
                        cn = i;
                        DistribExec distribExec = applGroup.get(i);
                        //添加到申请组list
                        distribAppl.add(distribExec.getTaskId());


                    }
                    //移除原有的申请组
                    applGroup.clear();
                }
                //移除申请组
//                applMap.remove(map.getKey());
            }
            //移除申请组
            applMap.clear();

            return cn;
        }
        return 0;
    }

    @Override
    public  void hostUpdate(UserInfo userInfo) {
        String uuid = userInfo.getUuid();
        logger.info("开始更新host:" + userInfo.getUuid());
        Map<String, Execer> mapScheduled = INSTANCE.getMap(AutoCheckConfig.getEXECMAPSTR());
        if (MapUtils.nonNull(mapScheduled)) {
            Execer execAll = mapScheduled.get(AutoCheckConfig.getEXECALL());
            if (execAll != null) {
                Map<String, UserInfo> execers = execAll.getExecers();
                if (MapUtils.nonNull(execAllLoacl)) {
                    UserInfo userInfolocal = execAllLoacl.get(uuid);
                    if (userInfolocal != null) {
                        userInfolocal.setTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
                        //找到更新 时间
                        execers.put(uuid, userInfolocal);
                        execAllLoacl.put(uuid, userInfolocal);
                    } else {
                        //没找到加入新的 并将网络host状态变动
                        addHost(userInfo, execers);
                        //网络变动
                        autoStatus.set(true);
                        execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
                    }
                    execAll.setExecers(execAllLoacl);
                    mapScheduled.put(AutoCheckConfig.getEXECALL(), execAll);
                } else {
                    //没找到加入新的 并将网络host状态变动
                    addHost(userInfo, execers);
                    //网络变动
                    autoStatus.set(true);
                    execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
                    mapScheduled.put(AutoCheckConfig.getEXECALL(), execAll);
                }

            }
        }

    }


    private static void addHost(UserInfo userInfo, Map<String, UserInfo> execers) {
        String uuid = userInfo.getUuid();
        userInfo.setTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        //没找到加入新的 并将网络host状态变动
        execers.put(uuid, userInfo);
        execAllLoacl.put(uuid, userInfo);
    }

    public static void removeHost(String uuid) {
        Map<String, Execer> mapScheduled = INSTANCE.getMap(AutoCheckConfig.getEXECMAPSTR());
        if (MapUtils.nonNull(mapScheduled)) {
            Execer execAll = mapScheduled.get(AutoCheckConfig.getEXECALL());
            if (execAll != null) {
                Map<String, UserInfo> execers = execAll.getExecers();
                Map<String, UserInfo> newExecers = new HashMap<>();
                for (Map.Entry<String, UserInfo> map : execers.entrySet()) {
                    if (!map.getKey().equals(uuid)) {
                        newExecers.put(map.getKey(), map.getValue());
                    }
                }
                //网络变动
                execAll.setExecers(newExecers);
                autoStatus.set(true);
                execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
                mapScheduled.put(AutoCheckConfig.getEXECALL(), execAll);
            }
        }
    }


    /**
     * 检查host
     */
    @Override
    public  void checkHost(UserInfo userInfo) {
        logger.info("开始检查是否超时！");

      //  HazelcastInstance INSTANCE = HazelcastAutoCompute.getINSTANCE();

        Map<String, Execer> mapScheduled = HazelcastAutoCompute.getEXECMAP();
        Map<String, String> mapMaster = HazelcastAutoCompute.getMASTER();

        if (MapUtils.nonNull(mapScheduled)) {
            Execer execAll = mapScheduled.get(AutoCheckConfig.getEXECALL());

            if (execAll != null) {
                execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
            } else {
                //Hazelcast 传播 造成数据丢失
                execAll = new Execer();
                execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
            }


            if (MapUtils.isNull(execAllLoacl)) {
                autoStatus.set(false);
               // execAll.setStatus(ExecerStatusEnum.NO_CHANGE.getCode());
            }

            // Map<String, Long> execers = execAll.getExecers();
            //  Map<String, Long> newExecers = new HashMap<>();
            ConcurrentHashMap<String, UserInfo> execAllLoacl = HazelcastAutoCompute.getExecAllLoacl();
            if (MapUtils.nonNull(execAllLoacl)) {

                List<String> collect = execAllLoacl.entrySet().stream().map(m -> m.getKey()).collect(Collectors.toList());
                boolean present = false;
                if (MapUtils.nonNull(mapMaster)) {
                    present = mapMaster.entrySet().stream().filter(m -> !collect.contains(m.getValue())).findFirst().isPresent();
                }
                logger.info(present + "当前master:" + JSONObject.toJSONString(mapMaster));
                // if (INSTANCE.getCPSubsystem().getLock(HazelcastAutoCompute.getMASTERSTR()).tryLock(7000, TimeUnit.MILLISECONDS)) {
                if (present) {
                    logger.info("master:" + JSONObject.toJSONString(mapMaster) + "超时！清除！");
                    //master  已经不在执行组内需要清除
                    mapMaster.clear();
                    autoStatus.set(true);
                    execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
                }


                for (Map.Entry<String, UserInfo> host : execAllLoacl.entrySet()) {
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
                                    String s = HttpUrlConnectionClientUtil.doGet(url);
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
                                    logger.info("端口不存在！");
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


                mapScheduled.clear();
                //修改成新的没超时的
                execAll.setExecers(execAllLoacl);
                mapScheduled.put(AutoCheckConfig.getEXECALL(), execAll);

                //  INSTANCE.getCPSubsystem().getLock(HazelcastAutoCompute.getMASTERSTR()).unlock();
            }
            //  }


        }
    }

    private static void remoteHost(Map<String, String> mapMaster, Execer execAll, Map.Entry<String, UserInfo> host) {
        //有超时的 变动
        HazelcastAutoCompute.getExecAllLoacl().remove(host.getKey());
        execAll.setStatus(ExecerStatusEnum.CHANGE.getCode());
        if (MapUtils.nonNull(mapMaster)) {
            String master = mapMaster.get(getMASTER());
            if (!StringUtil.isNullOrEmpty(master) && master.equals(host.getKey())) {
                logger.info("master:" + host.getKey() + "超时！清除！" + host.getValue());
                mapMaster.clear();
            }
        }
    }


    public static void main(String[] args) throws Exception {

//        Map<Object, Object> execmapMap = INSTANCE.getMap("execmapMap");
//        execmapMap.clear();

//        Map<String, Integer> mapMaster = INSTANCE.getMap(MASTERSTR);
//

//        Map<String, Execer> mapScheduled = INSTANCE.getMap(EXECMAPSTR);
//        mapScheduled.clear();
//        String uuid = "4";
//
//        hostUpdate("5");
//        hostUpdate("2");
//        checkHost(timeOut * 15);
//        Map<String, List<DistribExec>> applMap = instance.getMap(SCHEDULEDAPPLYMAP);
//
//        List<DistribExec> distribExecs = new ArrayList<>();
//        DistribExec distribExec1 =   DistribExec.builder().uuid(uuid).taskId("555232832").build();
//        DistribExec distribExec2 =  DistribExec.builder().uuid(uuid).taskId("666283244").build();
//        DistribExec distribExec3 = DistribExec.builder().uuid(uuid).taskId("888283255").build();
//
//
//
//        distribExecs.add(distribExec1);
//        distribExecs.add(distribExec2);
//        distribExecs.add(distribExec3);
//
//        applMap.put(uuid, distribExecs);

//        sycCompute(uuid);
//        HazelcastServer.getHazelcastServer();
//        UdpMulticast.buildMulticast();
//
//        UdpProtocol udpProtocol = new UdpProtocol<>();
//        udpProtocol.setUuid(Identity.getUUID());
//        udpProtocol.setType(MessageTypeEnum.APPLRECEIVE);
//        udpProtocol.setData(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
//        String string = JSONObject.toJSONString(udpProtocol);
//
//        UdpProtocol udpProtocol1 = UdpMulticast.sendReceive(string);
//
//        logger.info(JSONObject.toJSONString( udpProtocol1));


    }
}
