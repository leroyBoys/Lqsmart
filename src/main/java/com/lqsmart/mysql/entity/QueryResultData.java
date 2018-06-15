package com.lqsmart.mysql.entity;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/8.
 */
public class QueryResultData<T> {
    private LinkedList<T> dataList = new LinkedList<>();
    private final Map<Integer,T> result_now = new LinkedHashMap<>();
    //private final Map<String,Map<Integer,Object>> fieldName_ObjMap = new LinkedHashMap<>();

    public T getReultById(int id){
        return result_now.get(id);
    }

    public void put(int id,T t){
        result_now.put(id,t);
    }

    public void add(T t){
        dataList.add(t);
    }

  /*  public Map<Integer,Object> getFieldReultByFile(String field){
        Map<Integer,Object> map = fieldName_ObjMap.get(field);
        if(map == null){
            map = new LinkedHashMap<>();
            fieldName_ObjMap.put(field,map);
        }
        return map;
    }
*/
    public LinkedList<T> getResult(){
        return dataList;
    }

  /*  public T getOnlyOneResult(){
        return dataList.getFirst();
    }*/
}
