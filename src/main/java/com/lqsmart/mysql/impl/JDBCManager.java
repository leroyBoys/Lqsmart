package com.lqsmart.mysql.impl;

import com.lqsmart.entity.Node;
import com.lqsmart.entity.NodeManger;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/24.
 */
public class JDBCManager extends NodeManger<LQDataSource> {

    @Override
    protected Node<LQDataSource> intanceNode(boolean slowSlaveOn) {
        return new JdbcNode(slowSlaveOn);
    }

}
