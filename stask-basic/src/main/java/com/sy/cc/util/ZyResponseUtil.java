package com.sy.cc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZyResponseUtil {
    private static final Logger log = LoggerFactory.getLogger(ZyResponseUtil.class);

    public ZyResponseUtil() {
    }

    public static <T> ZyResponse<T> success(T data) {
        ZyResponse<T> mtResponse = new ZyResponse();
        mtResponse.setData(data);
        mtResponse.setCode(0);
        return mtResponse;
    }

    public static <T> ZyResponse<T> success() {
        ZyResponse<T> mtResponse = new ZyResponse();
        mtResponse.setCode(0);
        return mtResponse;
    }

    public static <T> ZyResponse<T> fail(String msg) {
        log.error(msg);
        ZyResponse<T> mtResponse = new ZyResponse();
        mtResponse.setMsg(msg);
        mtResponse.setCode(1);
        return mtResponse;
    }

    public static <T> ZyResponse<T> fail(String msg, T data) {
        log.error(msg);
        ZyResponse<T> mtResponse = new ZyResponse();
        mtResponse.setMsg(msg);
        mtResponse.setCode(1);
        mtResponse.setData(data);
        return mtResponse;
    }

    public static <T> ZyResponse<T> fail(int code, String msg, T data) {
        log.error(msg);
        ZyResponse<T> mtResponse = new ZyResponse();
        mtResponse.setMsg(msg);
        mtResponse.setCode(code);
        mtResponse.setData(data);
        return mtResponse;
    }


}
