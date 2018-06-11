package com.lqsmart.mysql.entity;

import com.lqsmart.mysql.db.DbExecutor;
import com.lqsmart.mysql.db.impl.MysqlExecutor;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public enum LQDbType {
    MySql("mysql",new MysqlExecutor()),;
    private String autoMark;//自动识别匹配标志（主要通过driverclass与url）
    private DbExecutor dbExecutor;
    LQDbType(String autoMark,DbExecutor dbExecutor){
        this.autoMark = autoMark;
        this.dbExecutor = dbExecutor;
    }

    public String getAutoMark() {
        return autoMark;
    }

    public DbExecutor getDbExecutor() {
        return dbExecutor;
    }
}
