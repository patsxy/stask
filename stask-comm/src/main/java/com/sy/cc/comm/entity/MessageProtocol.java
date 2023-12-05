package com.sy.cc.comm.entity;

import java.nio.charset.Charset;

//协议包
public class MessageProtocol {
    private int len; //关键，content的长度
    private byte[] content;

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public static MessageProtocol getMessageProtocol(String mes) {
        byte[] content = mes.getBytes(Charset.forName("utf-8"));
        int length = mes.getBytes(Charset.forName("utf-8")).length;

        //创建协议包对象
        MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setLen(length);
        messageProtocol.setContent(content);
        return messageProtocol;
    }
}

