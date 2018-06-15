package com.lqsmart.redis.entity;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.redis.impl.LQRedisConnection;
import com.lqsmart.util.LQSerializerTool;
import com.lqsmart.util.LqUtil;
import com.lqsmart.util.RandomUtil;

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
         byte[] bytes = LQSerializerTool.serializer(entity,schema);

        String key = table.redisKey(table.getRedisKeyGetInace().formatToDbData(entity));
        try {
            byte[] keys = LqUtil.hex2byte(key);
            redisConnection.set(keys,bytes);

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
        String key = table.redisKey(uniqueId);
        try {
            byte[] keys = LqUtil.hex2byte(key);
            byte[] bytes = redisConnection.get(keys);
            if(bytes == null){
                return null;
            }
            return LQSerializerTool.mergeFrom(instance,schema,bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
