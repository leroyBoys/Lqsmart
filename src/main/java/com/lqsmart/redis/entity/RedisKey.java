package com.lqsmart.redis.entity;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/15.
 */
public interface RedisKey {
    String getPrexKey();
    RedisExecuter getSerialzer();
    int getExpire();
}
