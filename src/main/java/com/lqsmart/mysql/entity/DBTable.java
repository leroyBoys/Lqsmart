package com.lqsmart.mysql.entity;

import com.lqsmart.mysql.compiler.ColumInit;
import com.lqsmart.mysql.compiler.FieldGetProxy;
import com.lqsmart.redis.entity.RedisCache;
import com.lqsmart.redis.entity.RedisSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/4/25.
 */
public class DBTable {
    private String name;
    private String idColumName;
    private RedisSerializer redisSerializer;
    private RedisCache redisCache;
    private FieldGetProxy.FieldGet redisKeyGetInace;

    private Map<String,FieldGetProxy.FieldGet> columGetMap = new HashMap<>();
    private Map<String,ColumInit> columInitMap = new HashMap<>(5);
    private Map<String,RelationData> columRelationMap = new HashMap<>();
    private Map<String,RelationData> fieldRelationMap = new HashMap<>();

    public String getIdColumName() {
        return idColumName;
    }

    public void setIdColumName(String idColumName) {
        this.idColumName = idColumName;
    }

    public DBTable(String name) {
        this.name = name;
    }

    public RedisSerializer getRedisSerializer() {
        return redisSerializer;
    }

    public void setRedisSerializer(RedisSerializer redisSerializer) {
        this.redisSerializer = redisSerializer;
    }

    public RelationData getRelationMap(String columName){
        return columRelationMap.get(columName);
    }

    public RelationData getRelationByFieldName(String fieldName){
        return fieldRelationMap.get(fieldName);
    }

    public ColumInit getColumInit(String columName){
        return columInitMap.get(columName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RedisCache getRedisCache() {
        return redisCache;
    }

    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    public void addColumInit(String columName, ColumInit columInit, FieldGetProxy.FieldGet fieldGetProxy) {
        columInitMap.put(columName,columInit);
        columGetMap.put(columName, fieldGetProxy);
    }

    public FieldGetProxy.FieldGet getRedisKeyGetInace() {
        return redisKeyGetInace;
    }

    public void setRedisKeyGetInace(FieldGetProxy.FieldGet redisKeyGetInace) {
        this.redisKeyGetInace = redisKeyGetInace;
    }

    public Map<String, ColumInit> getColumInitMap() {
        return columInitMap;
    }

    public void putColumRelationMap(String columName, RelationData relationData) {
        if(!columRelationMap.containsKey(columName)){
            columRelationMap.put(columName,relationData);
        }
    }

    public void addRelationData(RelationData relationData) {
        fieldRelationMap.put(relationData.getFieldName(),relationData);
    }

    public Map<String, FieldGetProxy.FieldGet> getColumGetMap() {
        return columGetMap;
    }

    public String redisKey(Object uniqueid){
        return getName()+"."+uniqueid;
    }
}
