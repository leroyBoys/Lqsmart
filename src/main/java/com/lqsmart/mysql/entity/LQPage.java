package com.lqsmart.mysql.entity;

import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public class LQPage<T> {
    private int pageCurrent = 1;
    private int pageSize=5;
    private Map<String,String> conditions = new HashMap<>();//等价匹配
    private Map<String,String> likeConditions = new HashMap<>();//模糊匹配
    private LinkedHashMap sortColums = new LinkedHashMap<>();

    private int start;
    private int end;
    private int allCount;//总数量
    private List<T> results = new LinkedList<>();

    public LQPage() {
        this.setPageCurrent(pageCurrent);
        this.setPageSize(pageSize);
    }

    public LQPage(int pageCurrent, int pageSize) {
        this.setPageCurrent(pageCurrent);
        this.setPageSize(pageSize);
    }

    public List<T> getResults() {
        return results;
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

    public Map<String, String> getConditions() {
        return conditions;
    }

    public LinkedHashMap getSortColums() {
        return sortColums;
    }

    public void setSortColums(LinkedHashMap sortColums) {
        this.sortColums = sortColums;
    }

    public void setConditions(Map<String, String> conditions) {
        this.conditions = conditions;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.min(100,pageSize);

        this.start = (pageCurrent-1)*pageSize;
        this.end = pageCurrent*pageSize;
    }

    public Map<String, String> getLikeConditions() {
        return likeConditions;
    }

    public void setLikeConditions(Map<String, String> likeConditions) {
        this.likeConditions = likeConditions;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public int getStart() {
        return start;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public int getEnd() {
        return end;
    }

}
