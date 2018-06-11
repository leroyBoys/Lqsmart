package com.lqsmart.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import java.util.WeakHashMap;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/11.
 */
public class LQSerializerTool {
    final static WeakHashMap<Class,Schema> schemaWeakHashMap = new WeakHashMap<>();


    private static Schema getSchema(Class cls){
        Schema schema = schemaWeakHashMap.get(cls);
        if(schema == null){
            synchronized (cls){
                schema = schemaWeakHashMap.get(cls);
                if(schema != null){
                    return schema;
                }
                schema = RuntimeSchema.getSchema(cls);
                schemaWeakHashMap.put(cls, schema);
            }

        }
        return schema;
    }

    public static byte[] serializer(Object entity){
        byte[] bytes = ProtostuffIOUtil.toByteArray(entity, getSchema(entity.getClass()), LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        return bytes;
    }

    public static <T> T mergeFrom(Class<T> tClass,byte[] bytes){
        T t = null;
        try {
            t = tClass.newInstance();
            ProtostuffIOUtil.mergeFrom(bytes, t,getSchema(tClass));
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

}
