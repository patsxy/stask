package com.sy.cc.comm.emuns;

import lombok.Getter;

@Getter
public enum CacheTypeEnum {
    HAZELCAST(0, "hazelcast"),
    REDIS(1, "redis"),


    ;


    private int code;
    private String message;


    CacheTypeEnum(int code, String message) {

        this.code = code;
        this.message = message;
    }

    /**
     * 获得枚举中文
     *
     * @param code
     * @return
     */
    public static String getName(int code) {
        for (CacheTypeEnum enumname : CacheTypeEnum.values()) {
            if (code == enumname.getCode()) {
                return enumname.getMessage();
            }
        }

        return "";
    }


    public static CacheTypeEnum get(int code) {
        for (CacheTypeEnum enumname : CacheTypeEnum.values()) {
            if (code == enumname.getCode()) {
                return enumname;
            }
        }

        return null;
    }
}
