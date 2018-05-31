package com.lqsmart.module;

/**
 * Created by Administrator on 2017/4/26.
 */
public class ServerGroup {
    public final static ServerGroup instance = new ServerGroup();
    private int group;
    private String sqlUrl;
    private String redisUrl;
    private String sqlUserName;
    private String sqlPwd;
    private String redisUserName;
    private String redisPwd;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getSqlUrl() {
        return sqlUrl;
    }

    public void setSqlUrl(String sqlUrl) {
        this.sqlUrl = sqlUrl;
    }

    public String getRedisUrl() {
        return redisUrl;
    }

    public void setRedisUrl(String redisUrl) {
        this.redisUrl = redisUrl;
    }

    public String getSqlUserName() {
        return sqlUserName;
    }

    public void setSqlUserName(String sqlUserName) {
        this.sqlUserName = sqlUserName;
    }

    public String getSqlPwd() {
        return sqlPwd;
    }

    public void setSqlPwd(String sqlPwd) {
        this.sqlPwd = sqlPwd;
    }

    public String getRedisUserName() {
        return redisUserName;
    }

    public void setRedisUserName(String redisUserName) {
        this.redisUserName = redisUserName;
    }

    public String getRedisPwd() {
        return redisPwd;
    }

    public void setRedisPwd(String redisPwd) {
        this.redisPwd = redisPwd;
    }

}
