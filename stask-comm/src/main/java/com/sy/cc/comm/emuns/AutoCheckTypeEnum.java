package com.sy.cc.comm.emuns;

import lombok.Getter;

@Getter
public enum AutoCheckTypeEnum {

    HTTP(0,"http"),
    UDP(1,"udp"),
    ;


    private int code;
    private String message;


    AutoCheckTypeEnum(int code, String message){

        this.code=code;
        this.message=message;
    }

    /**
     * 获得枚举中文
     * @param code
     * @return
     */
    public static String  getName(int code){
        for(AutoCheckTypeEnum enumname: AutoCheckTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname.getMessage();
            }
        }

        return "";
    }



    public static AutoCheckTypeEnum get(int code){
        for(AutoCheckTypeEnum enumname: AutoCheckTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname;
            }
        }

        return null;
    }
}
