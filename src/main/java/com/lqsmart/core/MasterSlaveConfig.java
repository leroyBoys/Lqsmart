package com.lqsmart.core;

import com.lqsmart.entity.DBType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/23.
 */
public class MasterSlaveConfig {
    private DBType dbType;
    private Map<String,String> master = new HashMap<>();

    private Map<String,Map<String,String>> slaves = new HashMap<>();
    private String listeners;
    private boolean slowOpen;

    public MasterSlaveConfig(DBType dbType) {
        this.dbType = dbType;
    }

    public void addMaster(String key, String value) {
        master.put(key,value);
    }

    public void addSlave(String uniqueKey, String key, String value) {
        Map<String,String> map = slaves.get(uniqueKey);
        if(map == null){
            map = new HashMap<>();
            slaves.put(uniqueKey,map);
        }
        map.put(key,value);
    }

    public DBType getDbType() {
        return dbType;
    }

    public boolean isSlowOpen() {
        return slowOpen;
    }

    public void setSlowOpen(boolean slowOpen) {
        this.slowOpen = slowOpen;
    }

    public Map<String, String> getMaster() {
        return master;
    }

    public Map<String, Map<String, String>> getSlaves() {
        return slaves;
    }

    public String getListeners() {
        return listeners;
    }

    public void setListeners(String listeners) {
        this.listeners = listeners;
    }
}
