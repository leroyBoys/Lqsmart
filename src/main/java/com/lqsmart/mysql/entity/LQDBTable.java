package com.lqsmart.mysql.entity;

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
public @interface LQDBTable {
    String name() default "";
}
