package com.lqsmart.mysql.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/4.
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBRelation {
    String colum();
    String targetColum() default "";
}
