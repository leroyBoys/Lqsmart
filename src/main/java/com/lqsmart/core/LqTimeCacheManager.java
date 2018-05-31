package com.lqsmart.core;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/22.
 */
public class LqTimeCacheManager {
    private final static LqTimeCacheManager timerManager = new LqTimeCacheManager();
    private long curTime;

    private LqTimeCacheManager(){}
    public static LqTimeCacheManager getInstance(){
        return timerManager;
    }

    public void setCurTime(long time){
        this.curTime = time;
    }

    public long getCurTime(){
        return getCurTimeNoCache();
    }
    /*public long getCurTime(){
        return curTime;
    }*/

    public long getCurTimeNoCache(){
        return System.currentTimeMillis();
    }
}
