package com.lqsmart.mysql.entity;

import com.lqsmart.core.LQStart;
import com.lqsmart.util.LqUtil;

import java.util.Date;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/17.
 */
public interface ConvertDefaultDBType<FROM> {
    public Object formatToDbData(FROM o);
    public Object formatFromDb(Class cls, String value);

    public static class ConvertDefault<FROM> implements ConvertDefaultDBType<FROM> {
        @Override
        public Object formatToDbData(FROM o) {
            return o;
        }

        @Override
        public Object formatFromDb(Class cls,String value) {
            return value;
        }
    }

    public static class EnumNumberConvertDBType extends ConvertDefault<LQDBEnum> {
        @Override
        public Object formatToDbData(LQDBEnum o) {
            return o == null?null:o.getDBValue();
        }

        @Override
        public Object formatFromDb(Class cls,String value) {
            return value==null?null: LQStart.instance.getEnum(cls,Integer.valueOf(value));
        }
    }

    public static class BooleanNumberConvertDBType extends ConvertDefault<Boolean> {
        @Override
        public Object formatToDbData(Boolean o) {
            return o == null || !o?0:1;
        }

        @Override
        public Object formatFromDb(Class cls,String value) {
            return value==null || !"1".equals(value)?false:true;
        }
    }

    public static class DateNumberConvertDBType extends ConvertDefault<Date> {
        @Override
        public Object formatToDbData(Date o) {
            return o==null?0:o.getTime();
        }

        @Override
        public Object formatFromDb(Class cls,String value) {
            return value==null?null:new Date(Long.valueOf(value));
        }
    }


    public static class DateDefaultConvertDBType extends ConvertDefault<Date> {
        @Override
        public Object formatToDbData(Date o) {
            return o==null?null: LqUtil.getDateTime(o);
        }

        @Override
        public Object formatFromDb(Class cls,String value) {
            return value==null?null: LqUtil.getDateTime(value, LqUtil.C_TIME_PATTON_DEFAULT);
        }
    }

    public static class BytesConvertDBType extends ConvertDefault<byte[]> {
        @Override
        public Object formatToDbData(byte[] o) {
            return o==null?null: LqUtil.byte2hex(o);
        }

        @Override
        public Object formatFromDb(Class cls,String value) {
            return LqUtil.hex2byte(value);
        }
    }
}
