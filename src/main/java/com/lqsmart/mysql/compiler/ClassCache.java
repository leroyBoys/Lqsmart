package com.lqsmart.mysql.compiler;

import com.lqsmart.mysql.entity.DBRelations;
import com.lqsmart.mysql.entity.LQDBTable;
import com.lqsmart.mysql.entity.LQField;
import com.lqsmart.redis.entity.RedisCache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/21.
 */
public class ClassCache {
    private LQDBTable lqdbTable;
    private RedisCache redisCache;
    private String redisKeyClassName;
    private Map<String,FieldCache> fieldCacheMap;//fieldName-data
    private Map<String,FieldCache> methodCacheMap;//method_get-data
    ClassCache(int size){
        fieldCacheMap = new HashMap<>(size);
        methodCacheMap = new HashMap<>(size);
    }

    public void addFieldCache(String fieldName,FieldCache fieldCache){
        fieldCacheMap.put(fieldName,fieldCache);
        if(fieldCache.getMethodGetFileName() != null && !fieldCache.getMethodGetFileName().isEmpty()){
            methodCacheMap.put(fieldCache.getMethodGetFileName(),fieldCache);
        }
    }

    public String getRedisKeyClassName() {
        return redisKeyClassName;
    }

    public void setRedisKeyClassName(String redisKeyClassName) {
        this.redisKeyClassName = redisKeyClassName;
    }

    public boolean isContainMethodClass(String methodGetName){
        return methodCacheMap.containsKey(methodGetName);
    }

    public LQDBTable getLqdbTable() {
        return lqdbTable;
    }

    public void setLqdbTable(LQDBTable lqdbTable) {
        this.lqdbTable = lqdbTable;
    }

    public RedisCache getRedisCache() {
        return redisCache;
    }

    public Map<String, FieldCache> getFieldCacheMap() {
        return fieldCacheMap;
    }

    public Map<String, FieldCache> getMethodCacheMap() {
        return methodCacheMap;
    }

    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    public   static class FieldCache{
        private Field field;
        private LQField lqField;
        private DBRelations dbRelations;
        private String columName;
        private String methodGetFileName;
        private String methodSetFileName;

        public FieldCache(LQField lqField, String columName, String methodGetFileName, String methodSetFileName,Field field) {
            this.lqField = lqField;
            this.columName = columName;
            this.methodGetFileName = methodGetFileName;
            this.methodSetFileName = methodSetFileName;
            this.field = field;
        }

        public FieldCache(DBRelations dbRelations, String methodGetFileName, String methodSetFileName,Field field) {
            this.dbRelations = dbRelations;
            this.methodGetFileName = methodGetFileName;
            this.methodSetFileName = methodSetFileName;
            this.field = field;
        }

        public Field getField() {
            return field;
        }

        public LQField getLqField() {
            return lqField;
        }

        public String getColumName() {
            return columName;
        }

        public DBRelations getDbRelations() {
            return dbRelations;
        }

        public String getMethodGetFileName() {
            return methodGetFileName;
        }

        public String getMethodSetFileName() {
            return methodSetFileName;
        }
    }
}
