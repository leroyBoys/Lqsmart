package com.lqsmart.redis.entity;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.lqsmart.core.LqTimeCacheManager;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.redis.impl.LQRedisConnection;
import com.lqsmart.util.LqUtil;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/17.
 */
public class ByteRedisSerializer extends RedisSerializer {
    private Schema schema;
    public ByteRedisSerializer(Class cls){
        this.schema = RuntimeSchema.getSchema(cls);
    }

    @Override
    public void serializer(LQRedisConnection redisConnection, DBTable table, Object entity) {
         byte[] bytes = ProtostuffIOUtil.toByteArray(entity, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

        String key = table.redisKey(table.getRedisKeyGetInace().formatToDbData(entity));
        try {
            byte[] keys = LqUtil.hex2byte(key);
            redisConnection.set(keys,bytes);

            if(table.getRedisCache().expire() > 0){
                if(table.getRedisCache().expireAt() > 0){
                    long endTime = LqTimeCacheManager.getInstance().getCurTime()+table.getRedisCache().expire()*1000;
                    endTime = Math.min(endTime,table.getRedisCache().expireAt());
                    redisConnection.expireAt(keys,endTime);
                    return;
                }
                redisConnection.expire(keys,table.getRedisCache().expire());
            }else  if(table.getRedisCache().expireAt() > 0){
                redisConnection.expireAt(keys,table.getRedisCache().expireAt());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object mergeFrom(LQRedisConnection redisConnection, DBTable table, Class instance, Object uniqueId) {
        String key = table.redisKey(uniqueId);
        try {
            byte[] keys = LqUtil.hex2byte(key);
            byte[] bytes = redisConnection.get(keys);
            if(bytes == null){
                return null;
            }
            Object t = instance.newInstance();
            ProtostuffIOUtil.mergeFrom(bytes, t,schema);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
