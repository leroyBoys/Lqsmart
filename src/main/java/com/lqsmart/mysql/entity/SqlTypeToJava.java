package com.lqsmart.mysql.entity;

import com.lqsmart.core.LQStart;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/8.
 */
public class SqlTypeToJava {
    public static final Map<Class,SqlTypeToJava> sqlTypsMap = new HashMap<>();
    static {
        sqlTypsMap.put(String.class,new SqlTypeToJava());
        sqlTypsMap.put(int.class,new IntToJava());
        sqlTypsMap.put(Integer.class,new IntToJava());
        sqlTypsMap.put(short.class,new ShortToJava());
        sqlTypsMap.put(Short.class,new ShortToJava());
        sqlTypsMap.put(float.class,new FloatToJava());
        sqlTypsMap.put(Float.class,new FloatToJava());
        sqlTypsMap.put(double.class,new DoubleToJava());
        sqlTypsMap.put(Double.class,new DoubleToJava());
        sqlTypsMap.put(BigDecimal.class,new BigDecimalToJava());
        sqlTypsMap.put(byte.class,new ByteStreamToJava());
        sqlTypsMap.put(Byte.class,new ByteStreamToJava());
        sqlTypsMap.put(byte[].class,new BytesStreamToJava());
        sqlTypsMap.put(Blob.class,new BlobToJava());
        sqlTypsMap.put(Boolean.class,new BooleanToJava());
        sqlTypsMap.put(boolean.class,new BooleanToJava());
        sqlTypsMap.put(java.io.Reader.class,new CharacterStreamToJava());
        sqlTypsMap.put(Clob.class,new ClobStreamToJava());
        sqlTypsMap.put(Long.class,new LongToJava());
        sqlTypsMap.put(long.class,new LongToJava());
        sqlTypsMap.put(Date.class,new DateToJava());
        sqlTypsMap.put(java.util.Date.class,new UDateToJava());
        sqlTypsMap.put(Timestamp.class,new TimestampToJava());
        sqlTypsMap.put(java.net.URL.class,new URLToJava());
    }


    public Object get(ResultSet rs,String colum) throws SQLException {
        return rs.getString(colum);
    }

    public Object get(ResultSet rs,int index) throws SQLException {
        return rs.getString(index);
    }

    public Object formtDataFromDb(Object value) throws SQLException {
        if(value == null || !(value instanceof String)){
            return value;
        }
        return formtDataTypeFromDb(value.toString());
    }

    protected Object formtDataTypeFromDb(String value) throws SQLException {
        return value;
    }

    public static SqlTypeToJava get(Class<?> type) {
        return sqlTypsMap.get(type);
    }


    public static class EnumIntJava extends SqlTypeToJava {
        protected Class enumClass;
        public EnumIntJava(Class enumClss){
            this.enumClass = enumClss;
        }

        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return  LQStart.instance.getEnum(enumClass,rs.getInt(colum));
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return  LQStart.instance.getEnum(enumClass,rs.getInt(index));
        }

    }

    public static class EnumStringJava extends SqlTypeToJava {
        private Class enumClass;
        public EnumStringJava(Class enumClss){
            this.enumClass = enumClss;
        }

        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return  LQStart.instance.getEnum(enumClass,rs.getString(colum));
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return  LQStart.instance.getEnum(enumClass,rs.getString(index));
        }
    }

    public static class EnumDefaultJava extends SqlTypeToJava {
        protected Class enumClass;
        public EnumDefaultJava(Class enumClss){
            this.enumClass = enumClss;
        }

        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            String str = rs.getString(colum);
            if(str == null){
                return null;
            }
            return Enum.valueOf(enumClass,str);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            String str = rs.getString(index);
            if(str == null){
                return null;
            }
            return Enum.valueOf(enumClass,str);
        }

        @Override
        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Enum.valueOf(enumClass,value);
        }
    }

    public static class ShortToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getShort(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getShort(index);
        }

        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Short.valueOf(value);
        }
    }

    public static class IntToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getInt(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getInt(index);
        }

        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Integer.valueOf(value);
        }
    }

    public static class FloatToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getFloat(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getFloat(index);
        }
        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Float.valueOf(value);
        }
    }

    public static class DoubleToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getDouble(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getDouble(index);
        }

        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Double.valueOf(value);
        }
    }

    public static class BigDecimalToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getBigDecimal(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getBigDecimal(index);
        }

        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return new BigDecimal(value);
        }
    }

    public static class ByteStreamToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getByte(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getByte(index);
        }

        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Byte.valueOf(value);
        }
    }

    public static class BytesStreamToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getBytes(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getBytes(index);
        }

    }

    public static class UDateToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            Timestamp timestamp = rs.getTimestamp(colum);
            if(timestamp == null){
                return null;
            }
            return new java.util.Date(timestamp.getTime());
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            Timestamp timestamp = rs.getTimestamp(index);
            if(timestamp == null){
                return null;
            }
            return new java.util.Date(timestamp.getTime());
        }

    }

    public static class DateToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getDate(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getDate(index);
        }
    }

    public static class TimestampToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getTimestamp(colum);
        }
        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getTimestamp(index);
        }
    }

    public static class LongToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getLong(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getLong(index);
        }

        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Long.valueOf(value);
        }
    }

    public static class URLToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getURL(colum);
        }
        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getURL(index);
        }
    }

    public static class ClobStreamToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getClob(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getClob(index);
        }
    }

    public static class CharacterStreamToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getCharacterStream(colum);
        }
        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getCharacterStream(index);
        }
    }

    public static class  BooleanToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getBoolean(colum);
        }
        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getBoolean(index);
        }

        protected Object formtDataTypeFromDb(String value) throws SQLException {
            return Boolean.valueOf(value);
        }
    }

    public static class BlobToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getBlob(colum);
        }
        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getBlob(index);
        }
    }

    public static class BinaryStreamToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getBinaryStream(colum);
        }
        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getBinaryStream(index);
        }
    }

    public static class AsciiStreamToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getAsciiStream(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getAsciiStream(index);
        }
    }

    public static class ArrayToJava extends SqlTypeToJava {
        @Override
        public Object get(ResultSet rs, String colum) throws SQLException {
            return rs.getArray(colum);
        }

        public Object get(ResultSet rs,int index) throws SQLException {
            return rs.getArray(index);
        }
    }
}
