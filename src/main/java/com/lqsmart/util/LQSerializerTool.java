package com.lqsmart.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/11.
 */
public class LQSerializerTool {

    public static byte[] serializer(Object entity,Schema schema){
        byte[] bytes = ProtostuffIOUtil.toByteArray(entity, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        return bytes;
    }

    public static <T> T mergeFrom(Class<T> tClass,Schema schema,byte[] bytes){
        T t = null;
        try {
            t = tClass.newInstance();
            ProtostuffIOUtil.mergeFrom(bytes, t,schema);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

}
