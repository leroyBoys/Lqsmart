package com.lqsmart.mysql.compiler;

import com.lqsmart.mysql.entity.ConvertDefaultDBType;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/2.
 */
public class FieldGetProxy {
    public Object get(Object obj){return null;}

    public static class FieldGet{
        private FieldGetProxy fieldGetProxy;
        private ConvertDefaultDBType defaultDBType;
        private Class fieldClss;
        public FieldGet(FieldGetProxy proxy, ConvertDefaultDBType defaultDBType, Class fieldClss){
            this.fieldGetProxy = proxy;
            this.defaultDBType = defaultDBType;
            this.fieldClss = fieldClss;
        }

        public Object formatToDbData(Object obj){
            Object o = fieldGetProxy.get(obj);
            if(defaultDBType == null){
                return o;
            }

            return defaultDBType.formatToDbData(o);
        }

        public Object formatFromDb(String value){
            return defaultDBType.formatFromDb(fieldClss,value);
        }
    }
}
