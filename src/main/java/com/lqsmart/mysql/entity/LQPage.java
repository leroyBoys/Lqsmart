package com.lqsmart.mysql.entity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public class LQPage {
    private int start;
    private int end;
    private int pageCurrent = 1;
    private int pageSize;
    private int pageCount;
    private String[] orderByNames;

    public String[] getOrderByNames() {
        return orderByNames;
    }

    public void setOrderByNames(String... orderByNames) {
        this.orderByNames = orderByNames;
    }

    /**
     * 多个字段用,分割
     */
    public void setOrderBy(String orderBy) {
        String[] arry = orderBy.split(",");
        List<String> list = new LinkedList<>();
        for(String str:arry){
            if(!str.trim().isEmpty()){
                list.add(str);
            }
        }

        this.orderByNames = new String[list.size()];
        list.toArray(this.orderByNames);
    }

    public int getPageCurrent() {
        return pageCurrent;
    }

    public void setPageCurrent(int current) {
        if(current > 0){
            this.pageCurrent = current;
        }else {
            this.pageCurrent = 1;
        }

        if(pageSize>0){
            this.start = (pageCurrent-1)*pageSize;
            this.end = pageCurrent*pageSize;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;

        this.start = (pageCurrent-1)*pageSize;
        this.end = pageCurrent*pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
