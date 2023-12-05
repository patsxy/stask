package com.sy.cc.comm.exception;


import cn.hutool.http.HttpStatus;
import com.sy.cc.comm.emuns.ZyHttpStatus;


public class ZyException extends RuntimeException {
    private static final long serialVersionUID = -4137688758944857209L;
    private Integer httpStatusCode;
    private Object object;

    public ZyException(ZyHttpStatus httpStatus) {
        super(httpStatus.getMsg());
        this.httpStatusCode = httpStatus.value();
    }

    public ZyException(ZyHttpStatus httpStatus, String msg) {
        super(msg);
        this.httpStatusCode = httpStatus.value();
    }

    public ZyException(String msg) {
        super(msg);
        this.httpStatusCode = HttpStatus.HTTP_INTERNAL_ERROR;
    }

    public ZyException(String msg, Object object) {
        super(msg);
        this.httpStatusCode = HttpStatus.HTTP_INTERNAL_ERROR;
        this.object = object;
    }

    public Integer getHttpStatusCode() {
        return this.httpStatusCode;
    }
}
