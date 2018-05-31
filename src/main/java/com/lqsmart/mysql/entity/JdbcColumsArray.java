package com.lqsmart.mysql.entity;

import com.lqsmart.mysql.compiler.ColumInit;
import com.lqsmart.util.LqLogUtil;

import java.sql.ResultSet;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/4.
 */
public class JdbcColumsArray {
    protected String[] columsArray = null;
    protected Map<String,Map<String,ColumInit>> relationFieldNameMap;


    public JdbcColumsArray(String[] array) {
        columsArray = array;
    }

    public String get(int idex){
        return columsArray[idex];
    }

    public int size() {
        return columsArray.length;
    }

    public <T> T doExute(DBTable dbTable, ResultSet rs, Class<T> tClass,QueryResultData<T> resultData) throws Exception {
        T t = doExuteOnlyOne(dbTable,rs,tClass);
        resultData.add(t);
        return t;
    }

    public <T> T doExuteOnlyOne(DBTable dbTable, ResultSet rs, Class<T> tClass) throws Exception {
        T t = tClass.newInstance();
        for (int i = 0, size = columsArray.length; i < size; ++i) {
            if(columsArray[i] == null){
                continue;
            }
            try {
                dbTable.getColumInit(columsArray[i]).set(t,rs,i + 1);
            }catch (Exception ex){
                LqLogUtil.error(dbTable.getName()+"  doExuteOnlyOne:columName:"+columsArray[i]+" not find from config",ex);
            }
        }
        return t;
    }
}
