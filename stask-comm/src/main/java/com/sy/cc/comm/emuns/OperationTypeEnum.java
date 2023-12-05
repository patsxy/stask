package com.sy.cc.comm.emuns;

import lombok.Getter;

@Getter
public enum OperationTypeEnum {

    ADD(1,"addTask"),
    UPDATE(2,"updateTask"),



    SUBMITLISTENABLE(3,"submitListenable"),

    REMOVE(4,"removeTask"),
    ;


    private int code;
    private String message;


    OperationTypeEnum(int code, String message){

        this.code=code;
        this.message=message;
    }

    /**
     * 获得枚举中文
     * @param code
     * @return
     */
    public static String  getName(int code){
        for(OperationTypeEnum enumname: OperationTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname.getMessage();
            }
        }

        return "";
    }



    public static OperationTypeEnum get(int code){
        for(OperationTypeEnum enumname: OperationTypeEnum.values()){
            if(code==enumname.getCode()){
                return enumname;
            }
        }

        return null;
    }
}
