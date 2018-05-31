package com.lqsmart.entity;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/25.
 */
public interface LQConntion {
    String getKey();
    boolean connctioned();
    void setConnetioned(boolean isConnectioned);
    void reLoad();
}
