package com.lqsmart.core;

import com.lqsmart.entity.DBType;
import com.lqsmart.util.LqUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/23.
 */
public class MasterSlaveGlobalConfig {
    private DBType dbType;
    private Map<String,String> master = new HashMap<>();
    private Map<String,String> slave = new HashMap<>();

    public Map<String, String> getMaster() {
        return master;
    }

    public Map<String, String> getSlave() {
        return slave;
    }

    public MasterSlaveGlobalConfig(DBType dbType) {
        this.dbType =dbType;
    }

    public void addMaster(String key, String value) {
        master.put(key,value);
    }

    public void addSlave(String key, String value) {
        slave.put(key,value);
    }

    public Properties getMaster(Map<String,String> masterMap) {
        return LqUtil.createProperties(masterMap,master);
    }

    public Properties[] getSlave(Map<String,Map<String,String>> slaves) {
        if(slaves == null || slaves.isEmpty()){
            if(master.isEmpty()){
                return null;
            }
            return new Properties[]{LqUtil.createProperties(null,slave)};
        }

        Properties[] arary = new Properties[slaves.size()];
        int i = 0;
        for(Map<String,String> map:slaves.values()){
            arary[i++] = LqUtil.createProperties(map,slave);
        }
        return arary;
    }

}
