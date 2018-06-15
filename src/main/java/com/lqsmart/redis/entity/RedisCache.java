package com.lqsmart.redis.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Created by leroy:656515489@qq.com
 * 2018/4/25.
 */
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCache{
    /** 作为唯一值的对应值（默认id） */
    String keyMethodName() default "getId";
    Type type() default Type.Map;
    /** 有效期时间（秒）,0：无限期(默认) */
    int expire() default 0;
    /** 到期时间（时间戳）,0：无限期(默认) */
    long expireAt() default 0l;
    enum Type{
        Serialize,Map
    }
}
