package com.sy.cc.comm.util;

public class StringUtil {

    public static boolean isNullOrEmpty(String s){
        return s == null || s.isEmpty();
    }

    public static boolean nonNull(String s){
        return !(s == null || s.isEmpty());
    }
}
