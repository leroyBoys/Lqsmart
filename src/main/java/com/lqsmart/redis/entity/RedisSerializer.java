package com.lqsmart.redis.entity;

import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.redis.impl.LQRedisConnection;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/17.
 */
public abstract class RedisSerializer {
    public abstract void serializer(LQRedisConnection redisConnection, DBTable table, Object entity);
    public abstract <T> T mergeFrom(LQRedisConnection redisConnection, DBTable table, Class<T> instance, Object uniqueId);
}
