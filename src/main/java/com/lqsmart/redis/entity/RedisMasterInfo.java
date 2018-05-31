package com.lqsmart.redis.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/29.
 */
public class RedisMasterInfo {
    private boolean isMaster;
    private Map<String,String> details = new HashMap<>();
    private Map<String,String> slaves = new HashMap<>();
    private String masterHost;

    public RedisMasterInfo(String info){
     //   System.out.println(info);
        String[] array = info.split("(\n|\r)");
        for(String str:array){
            if(str.isEmpty()){
                continue;
            }
            String[] arr = str.split("\\:");
            if(arr.length < 2){
                continue;
            }
            details.put(arr[0],arr[1]);

            if(arr[0].startsWith("slave")){
                slaves.put(arr[0],arr[1]);
             //   System.out.println("================="+arr[1]);
            }
        }

        isMaster = "master".equals(details.get("role"));
        if(!isMaster){
            slaves.clear();

            masterHost = details.get("master_host")+":"+details.get("master_port");
        }
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public String getMasterHost() {
        return masterHost;
    }

    public Map<String, String> getSlaves() {
        return slaves;
    }

    public void setSlaves(Map<String, String> slaves) {
        this.slaves = slaves;
    }
}
