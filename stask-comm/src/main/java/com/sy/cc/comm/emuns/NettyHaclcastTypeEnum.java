package com.sy.cc.comm.emuns;

import lombok.Getter;

@Getter
public enum NettyHaclcastTypeEnum {

    APPLY(1,"申请"),
    USE(2,"使用"),
    ;


    private int code;
    private String message;


    NettyHaclcastTypeEnum(int code, String message){

        this.code=code;
        this.message=message;
    }

    /**
     * 获得枚举中文
     * @param code
     * @return
     */
    public static String  getName(int code){
        for(NettyHaclcastTypeEnum enumname: NettyHaclcastTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname.getMessage();
            }
        }

        return "";
    }



    public static NettyHaclcastTypeEnum get(int code){
        for(NettyHaclcastTypeEnum enumname: NettyHaclcastTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname;
            }
        }

        return null;
    }
}
