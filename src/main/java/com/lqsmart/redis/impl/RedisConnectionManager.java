package com.lqsmart.redis.impl;

import com.lqsmart.entity.Node;
import com.lqsmart.entity.NodeManger;

/**
 * Created by Administrator on 2017/4/15.
 */
public class RedisConnectionManager extends NodeManger<LQRedisConnection>{
    @Override
    protected Node<LQRedisConnection> intanceNode(boolean slowSlaveOn) {
        return new RedisNode(slowSlaveOn);
    }

}
