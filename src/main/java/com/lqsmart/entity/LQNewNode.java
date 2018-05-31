package com.lqsmart.entity;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/25.
 */
public class LQNewNode {
    private String readNodeName;
    private String newNodeName;
    private String sentinels;
    private boolean slowOpen;

    public LQNewNode(String readNodeName, String newNodeName) {
        this.readNodeName = readNodeName;
        this.newNodeName = newNodeName;
    }

    public LQNewNode(String readNodeName, String newNodeName, String sentinels) {
        this.readNodeName = readNodeName;
        this.newNodeName = newNodeName;
        this.sentinels = sentinels;
    }

    public LQNewNode(String readNodeName, String newNodeName, String sentinels, boolean slowOpen) {
        this.readNodeName = readNodeName;
        this.newNodeName = newNodeName;
        this.sentinels = sentinels;
        this.slowOpen = slowOpen;
    }

    public String getSentinels() {
        return sentinels;
    }

    public boolean isSlowOpen() {
        return slowOpen;
    }

    public String getReadNodeName() {
        return readNodeName;
    }

    public String getNewNodeName() {
        return newNodeName;
    }
}
