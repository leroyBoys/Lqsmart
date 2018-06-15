package com.lqsmart.redis.entity;

import com.lqsmart.mysql.compiler.FieldGetProxy;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.redis.impl.LQRedisConnection;
import com.lqsmart.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/17.
 */
public class MapRedisSerializer extends RedisSerializer{
    /**  colum--getMethod */
    private Map.Entry<String,FieldGetProxy.FieldGet>[] aliasArray;
    public MapRedisSerializer(Map<String,FieldGetProxy.FieldGet> alias){
        aliasArray = new Map.Entry[alias.size()];
        alias.entrySet().toArray(aliasArray);
    }

    @Override
    public void serializer(LQRedisConnection redisConnection, DBTable table, Object entity) {

        String keys = table.redisKey(table.getRedisKeyGetInace().formatToDbData(entity));
        try {
            Map<String, String> map = new HashMap<>(aliasArray.length);
            Object object;
            for(Map.Entry<String,FieldGetProxy.FieldGet> entry:aliasArray){
                object = entry.getValue().formatToDbData(entity);
                if(object == null){
                    continue;
                }
                map.put(entry.getKey(),object.toString());
            }
            redisConnection.hmset(keys,map);

            if(table.getRedisCache().expire() > 0){
                redisConnection.expire(keys,table.getRedisCache().expire()+ RandomUtil.random(600));
            }else  if(table.getRedisCache().expireAt() > 0){
                redisConnection.expireAt(keys,table.getRedisCache().expireAt()+ RandomUtil.random(1800));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object mergeFrom(LQRedisConnection redisConnection, DBTable table, Class instance, Object uniqueId) {
        try {
            String keys = table.redisKey(uniqueId);
            Map<String, String> map = redisConnection.hgetAll(keys);
            if(map == null || map.isEmpty()){
                return null;
            }

            Object entity = instance.newInstance();
            for(Map.Entry<String,FieldGetProxy.FieldGet> entry:aliasArray){
                if(entry.getValue() == null){
                    continue;
                }
                table.getColumInit(entry.getKey()).setFromRedis(entity, entry.getValue().formatFromDb(map.get(entry.getKey())));
            }

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
