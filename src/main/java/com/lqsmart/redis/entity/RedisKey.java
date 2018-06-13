package com.lqsmart.redis.entity;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/15.
 */
public interface RedisKey {
    String getKey(Object... paramters);

    interface RedisExpireKey extends RedisKey {
        int getExpire();
    }

    interface RedisExpireAtKey extends RedisKey {
        long getExpireAt();
    }
}
