package com.lqsmart.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/4.
 */
public class SqlLock {
    private static Map<String,String> datas = new HashMap<>();

    public static String lock(String key){

        String value = datas.get(key);
        if(value != null){
            return value;
        }
        key = key.intern();
        value = key;
        synchronized (key){
            if(datas.containsKey(key)){
                return value;
            }

            datas.put(key,value);
        }
        return value;
    }
}
