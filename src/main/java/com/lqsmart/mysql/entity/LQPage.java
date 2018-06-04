package com.lqsmart.mysql.entity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public class LQPage<T> {
    private int start;
    private int end;
    private int pageCurrent = 1;
    private int pageSize;
    private int count;//总数量
    private String sql;
    /** select 语句*/
    private String selectSql;
    /**  from语句（除select 语句外的部分） */
    private String fromSql;
    private List<T> results = new LinkedList<>();

    public LQPage() {
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

    /**
     *
     * @param sql
     * @param uniqueKeyColum 唯一索引 的columName（最好使用id） 如果不设置则不做优化
     */
    public void setSql(String sql, String uniqueKeyColum){
        this.parsetSql(sql,uniqueKeyColum);
    }

    private void parsetSql(String sql,String uniqueKeyColum){
        if(this.selectSql != null){
            return;
        }

        int idex = sql.indexOf("from");
        if(idex < 0){
            idex = sql.toLowerCase().indexOf("from");
        }

        if(idex < 0){
            throw new RuntimeException(" cant find from from sql:"+sql);
        }
        this.selectSql = sql.substring(0,idex);
        this.fromSql = sql.substring(idex,sql.length());
        this.sql = sql;
    }


    public int getPageSize() {
        return pageSize;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public String getSql() {
        return sql;
    }

    public String getFromSql() {

        return fromSql;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;

        this.start = (pageCurrent-1)*pageSize;
        this.end = pageCurrent*pageSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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
