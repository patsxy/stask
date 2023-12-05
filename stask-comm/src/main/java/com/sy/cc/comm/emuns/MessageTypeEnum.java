package com.sy.cc.comm.emuns;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {

    HEARTBEAT(1,"心跳"),


    DATA(2,"数据"),
    APPLRECEIVE(3,"申请回复"),

    RECEIVE(4,"回复"),
    ;


    private int code;
    private String message;


    MessageTypeEnum(int code, String message){

        this.code=code;
        this.message=message;
    }

    /**
     * 获得枚举中文
     * @param code
     * @return
     */
    public static String  getName(int code){
        for(MessageTypeEnum enumname: MessageTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname.getMessage();
            }
        }

        return "";
    }



    public static MessageTypeEnum get(int code){
        for(MessageTypeEnum enumname: MessageTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname;
            }
        }

        return null;
    }
}
