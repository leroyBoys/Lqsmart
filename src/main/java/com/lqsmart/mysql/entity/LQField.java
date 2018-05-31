package com.lqsmart.mysql.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Created by leroy:656515489@qq.com
 * 2018/4/25.
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LQField {
    String name() default "";
    ConvertDBType convertDBType() default ConvertDBType.Default;
    /** 自定义格式化类路径（需要实现接口ConvertDefaultDBType） */
    String convertDBTypeClass() default "";
    boolean isPrimaryKey() default false;
    boolean redisSave() default true;
    enum ConvertDBType{
        Default(new ConvertDefaultDBType.ConvertDefault()),
        EnumNumber(new ConvertDefaultDBType.EnumNumberConvertDBType()),
        BoolNumber(new ConvertDefaultDBType.BooleanNumberConvertDBType()),
        DateNumber(new ConvertDefaultDBType.DateNumberConvertDBType()),
        DateDefault(new ConvertDefaultDBType.DateDefaultConvertDBType()),
        ByteArray(new ConvertDefaultDBType.BytesConvertDBType()),
        ;

        ConvertDefaultDBType convertDBTypeInter;
        ConvertDBType(ConvertDefaultDBType convertDBTypeInter){
            this.convertDBTypeInter =convertDBTypeInter;
        }

        public ConvertDefaultDBType getConvertDBTypeInter() {
            return convertDBTypeInter;
        }
    }
}
