package com.lqsmart.mysql.impl;

import com.lqsmart.entity.Node;

import java.util.Properties;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/30.
 */
class JdbcNode extends Node<LQDataSource> {
    JdbcNode(boolean slowSlaveOn){
        super(slowSlaveOn);
    }

    @Override
    protected LQDataSource initRedisConnection(Properties properties) {
        return new LQDataSource(properties);
    }

    @Override
    public LQDataSource[] createArray(int size) {
        return new LQDataSource[size];
    }

}
