package com.sy.cc.comm.util;

import java.util.Map;

public class MapUtils {
    public  static  boolean isNull(Map map){
        if(!map.isEmpty() && map.size()>0){
            return  false;
        }

        return  true;
    }


    public  static  boolean nonNull(Map map){
        if(!map.isEmpty() && map.size()>0){
            return  true;
        }

        return  false;
    }
}
