package com.lqsmart.redis.entity;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/15.
 */
public interface RedisKey {
    boolean isSynFromDb();
    Object queryFromDb(Object... paramters);
    String getKey(Object... paramters);
    interface RedisExpiresKey extends RedisKey {
        long getExpireAt();
        int getExpire();
    }

    interface RedisExpireKey extends RedisKey {
        int getExpire();
    }

    interface RedisExpireAtKey extends RedisKey {
        long getExpireAt();
    }
}
