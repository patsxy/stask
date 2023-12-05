package com.sy.cc.comm.emuns;

public enum ZyHttpStatus {
    UNAUTHORIZED(401, "未授权"),
    COUPONCANNOTUSETOGETHER(601, "优惠券不能共用"),
    SOCIAL_ACCOUNT_NOT_BIND(475, "social account not bind"),
    ACCOUNT_NOT_REGISTER(476, "account not register");

    private final int value;
    private final String msg;

    private ZyHttpStatus(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public int value() {
        return this.value;
    }

    public String getMsg() {
        return this.msg;
    }

    public String toString() {
        return this.value + " " + this.name();
    }

    public static ZyHttpStatus valueOf(int statusCode) {
        ZyHttpStatus status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("没有找到该Http状态码包含状态 [" + statusCode + "]");
        } else {
            return status;
        }
    }

    public static ZyHttpStatus resolve(int statusCode) {
        ZyHttpStatus[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ZyHttpStatus status = var1[var3];
            if (status.value == statusCode) {
                return status;
            }
        }

        return null;
    }
}
