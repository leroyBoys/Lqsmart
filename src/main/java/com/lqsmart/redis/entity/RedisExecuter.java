package com.lqsmart.redis.entity;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.lqsmart.util.LQSerializerTool;

import java.nio.charset.Charset;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/14.
 */
public interface RedisExecuter<T> {
    public byte[] serializer(T entity);
    public <T> T mergeFrom(byte[] bytes);

    /**
     * 返回对象解析器
     * @param cls
     * @return
     */
    static SchemaSerialer bytes(Class cls){
        return new SchemaSerialer(cls);
    }

    /**
     * 返回字符串解析器
     * @return
     */
    static StringSerialer strings(){
        return stringSerialer;
    }

    static byte[] serializerForString(String entity){
        return entity.getBytes(StringSerialer.charset);
    }

    static final StringSerialer stringSerialer = new StringSerialer();
    static String mergeFromString(byte[] bytes) {
        return new String(bytes,StringSerialer.charset);
    }
    final class SchemaSerialer implements RedisExecuter {
        private Schema schema;
        private Class cls;
        private SchemaSerialer(Class cls){
            this.schema = RuntimeSchema.getSchema(cls);
            this.cls = cls;
        }
        public byte[] serializer(Object entity){
            return LQSerializerTool.serializer(entity,schema);
        }

        @Override
        public Object mergeFrom(byte[] bytes) {
            return LQSerializerTool.mergeFrom(cls,schema,bytes);
        }

    }

    class StringSerialer implements RedisExecuter<String> {
        private static Charset charset = Charset.forName("UTF-8");
        private StringSerialer(){
        }
        public byte[] serializer(String entity){
            return entity.getBytes(charset);
        }

        @Override
        public String mergeFrom(byte[] bytes) {
            return new String(bytes,charset);
        }
    }
}
