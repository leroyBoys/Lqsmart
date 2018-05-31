package com.lqsmart.mysql.entity;

import com.lqsmart.mysql.compiler.ColumInit;
import com.lqsmart.util.LqLogUtil;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/7.
 */
public class MoreJdbcColumsArray extends JdbcColumsArray {

    public MoreJdbcColumsArray(String[] array, Map<String,Map<String,ColumInit>> relationFieldNameMap) {
        super(array);
        this.relationFieldNameMap = relationFieldNameMap;
    }

    public <T> T doExute(DBTable dbTable, ResultSet rs,Class<T> tClass,QueryResultData<T> resultData) throws Exception {
        int id = rs.getInt(dbTable.getIdColumName());
        if(id == 0){
            LqLogUtil.error(dbTable.getName()+":idKey is 0 maybe is error!!!please not use idKey = 0");
        }

        T t = resultData.getReultById(id);
        final boolean isNew = t == null;
        if(isNew){
            t = tClass.newInstance();
            resultData.put(id,t);
            resultData.add(t);
        }

        Object obj;
        String columName;
        RelationData relationData;
        Object reationObj;

        Map<String,ColumInit> tmpMap;
        Set<String> objMap = new HashSet<>(columsArray.length);
        for (int i = 0, size = columsArray.length; i < size; ++i) {
            columName = get(i);
            if(columName == null){
                continue;
            }

            ColumInit columInit = dbTable.getColumInit(columName);
            if(columInit!= null && isNew){
                try {
                    columInit.set(t,rs,i+1);
                }catch (Exception ex){
                    LqLogUtil.error(dbTable.getName()+" columName:"+columsArray[i]+" "+ex.getMessage(),ex);
                }
                continue;
            }

            relationData = dbTable.getRelationMap(columName);
            if(relationData == null){
                if(isNew){
                    LqLogUtil.error(dbTable.getName()+":columName:"+columName+" not find from config relationData");
                }
                continue;
            }else if(objMap.contains(relationData.getFieldName())){
                continue;
            }

            objMap.add(relationData.getFieldName());
            //可以对一对多的对象也做缓存，这里暂时不做了，以后再扩展
            reationObj = relationData.getFieldClass().newInstance();

            tmpMap = relationFieldNameMap.get(relationData.getFieldName());
            if(tmpMap == null){
                continue;
            }

            for(Map.Entry<String,ColumInit> entry:tmpMap.entrySet()){
                entry.getValue().set(reationObj,rs,entry.getKey());
            }

            if(relationData.isOneToMany()){
                obj = relationData.getFieldGetProxy().get(t);
                if(obj == null){
                    obj = relationData.getNewInstance().create();
                    relationData.getColumInit().set(t,obj);
                }

                relationData.getNewInstance().add(obj,reationObj);
            }else {
                relationData.getColumInit().set(t,reationObj);
            }

        }

        return t;
    }

}
