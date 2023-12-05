package com.sy.cc.comm.emuns;

import lombok.Getter;

@Getter
public enum ExecerStatusEnum {
    NO_CHANGE(0,"没有变动"),
    CHANGE(1,"有变动"),
    ;


    private int code;
    private String message;


    ExecerStatusEnum(int code, String message){

        this.code=code;
        this.message=message;
    }

    /**
     * 获得枚举中文
     * @param code
     * @return
     */
    public static String  getName(int code){
        for(ExecerStatusEnum enumname: ExecerStatusEnum.values()){
            if(code==enumname.getCode()){
                return enumname.getMessage();
            }
        }

        return "";
    }



    public static ExecerStatusEnum get(int code){
        for(ExecerStatusEnum enumname: ExecerStatusEnum.values()){
            if(code==enumname.getCode()){
                return enumname;
            }
        }

        return null;
    }
}
