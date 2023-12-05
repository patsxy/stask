package com.sy.cc.comm.config;



import com.sy.cc.comm.emuns.AutoCheckTypeEnum;
import com.sy.cc.comm.emuns.CacheTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class AutoCheckConfig {
    private static final Logger logger = LoggerFactory.getLogger(AutoCheckConfig.class);

    private static int timeOut = 4;
    private static final int THREE = 3;


    private static AutoCheckTypeEnum checkType=null;


    private static CacheTypeEnum cacheType=null;


    public static void setCacheType(CacheTypeEnum cache){
        cacheType=cache;
    }

    public static CacheTypeEnum getCacheType(){
        return cacheType;
    }
    public static void setCheckType(AutoCheckTypeEnum autoCheckTypeEnum) {
        checkType = autoCheckTypeEnum;
    }

    public static AutoCheckTypeEnum getCheckType() {
        return checkType;
    }


    private static final String MASTERSTR = "masterMap";

    private static final String EXECMAPSTR = "execMap";

    private static final String SCHEDULEDUSEMAP = "scheduledUseMap";

    private static final String SCHEDULEDAPPLYMAP = "scheduledApplyMap";

    private static final String EXECALL = "execAll";

    private static final String MASTER = "master";


    public static String getMASTER(){
        return  MASTER;
    }

    public static String getSCHEDULEDAPPLYMAP() {
        return SCHEDULEDAPPLYMAP;
    }

    public static String getSCHEDULEDUSEMAP() {
        return SCHEDULEDUSEMAP;
    }


    public static String getEXECALL() {
        return EXECALL;
    }

    public static String getEXECMAPSTR() {
        return EXECMAPSTR;
    }




    public static String getMASTERSTR(){
        return MASTERSTR;
    }




}
