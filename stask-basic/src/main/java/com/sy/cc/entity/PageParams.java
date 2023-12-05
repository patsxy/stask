package com.sy.cc.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class PageParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private  Integer  pageSize;

    private  Integer  pageNum;

}
