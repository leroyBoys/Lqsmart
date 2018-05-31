package com.lqsmart.entity;

import com.lqsmart.core.MasterSlaveConfig;
import com.lqsmart.core.MasterSlaveGlobalConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/24.
 */
public class StartInitCache {
    private Map<String,MethodCache> methodCacheMap = new HashMap<>();
    private Map<DBType,MasterSlaveGlobalConfig> globalConfigMap = new HashMap<>();
    private  Map<String,MasterSlaveConfig> node_configMap = new HashMap<>();

    public MethodCache getMethodCache(String classSourceName) throws ClassNotFoundException {
        MethodCache methodCache = methodCacheMap.get(classSourceName);
        if(methodCache != null){
            return methodCache;
        }

        methodCache = new MethodCache();

        Class cls = Class.forName(classSourceName);
        Method[] methods = cls.getMethods();
        Map<String,Method> fieldMap = new HashMap<>(methods.length);
        for(Method method:methods){
            if(!method.getName().startsWith("set")){
                continue;
            }
            String str = method.getName().substring(3);
            char[] cs=str.toCharArray();
            cs[0]+=32;
            str = new String(cs);
            fieldMap.put(str,method);
        }

        methodCache.cls = cls;
        methodCache.methodMap = fieldMap;
        methodCacheMap.put(classSourceName,methodCache);
        return methodCache;
    }

    public Map<DBType, MasterSlaveGlobalConfig> getGlobalConfigMap() {
        return globalConfigMap;
    }

    public void setGlobalConfigMap(Map<DBType, MasterSlaveGlobalConfig> globalConfigMap) {
        this.globalConfigMap = globalConfigMap;
    }

    public Map<String, MasterSlaveConfig> getNode_configMap() {
        return node_configMap;
    }

    public void setNode_configMap(Map<String, MasterSlaveConfig> node_configMap) {
        this.node_configMap = node_configMap;
    }

    public class MethodCache{
        public Class cls;
        public Map<String,Method> methodMap = new HashMap<>();
    }
}
