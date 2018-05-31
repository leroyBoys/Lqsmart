package com.lqsmart.redis.impl;

import com.lqsmart.core.LQStart;
import com.lqsmart.entity.Node;
import com.lqsmart.redis.entity.RedisMasterInfo;
import com.lqsmart.util.LqLogUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/30.
 */
class RedisNode extends Node<LQRedisConnection> {
    private MasterListener[] masterListeners;

    RedisNode(boolean slowSlaveOn){
        super(slowSlaveOn);
    }

    @Override
    protected LQRedisConnection initRedisConnection(Properties config) {
        int timeOut = 5000;
        int maxTotal = 3000;
        int maxIdel = 1500;
        long maxWaitMillis = -1l;
        if(config.getProperty("timeOut") != null){
            timeOut = Integer.valueOf(config.getProperty("timeOut"));
        }

        if(config.getProperty("maxTotal") != null){
            maxTotal = Integer.valueOf(config.getProperty("maxTotal"));
        }

        if(config.getProperty("maxIdel") != null){
            maxIdel = Integer.valueOf(config.getProperty("maxIdel"));
        }

        if(config.getProperty("maxWaitMillis") != null){
            maxWaitMillis = Long.valueOf(config.getProperty("maxWaitMillis"));
        }

        LQRedisConnection lqRedisConnection = new LQRedisConnection(config.getProperty("url"),timeOut,maxTotal,maxIdel,maxWaitMillis);
        new Thread(() -> {
            lqRedisConnection.subscribe(new JedisPubSub() {
            },"lister");
            this.down(lqRedisConnection);
        }).start();
        return lqRedisConnection;
    }

    @Override
    public LQRedisConnection[] createArray(int size) {
        return new LQRedisConnection[size];
    }

    @Override
    public String checkMaster(LQRedisConnection master) {
        RedisMasterInfo masterInfo = master.masterSlaveInfo();
        if(masterInfo.isMaster()){
            return master.getKey();
        }
        return masterInfo.getMasterHost();
    }

    @Override
    public boolean loopCheck() {
        boolean isNeedCheck = false;
        for(int i=0;i<masterListeners.length;i++){
            if(!masterListeners[i].checkRun()){
                isNeedCheck = true;
            }
        }
        return isNeedCheck;
    }

    @Override
    protected void init(String listeners) {
        super.init(listeners);

        if(listeners == null){
            return;
        }
        String[] array = listeners.split(",");

        masterListeners = new MasterListener[array.length];
        for(int i=0;i<array.length;i++){
            masterListeners[i]=new MasterListener(array[i],this);
            masterListeners[i].start();
        }
    }

    private class MasterListener extends Thread {
        private String host;
        private int port;
        private Jedis jedis;
        private RedisNode node;
        private AtomicBoolean running = new AtomicBoolean(false);

        public MasterListener(String hostStr,RedisNode node) {
            super("MasterListener-on-" + hostStr);
            this.node = node;
            String[] array = hostStr.split("\\:");
            host = array[0];
            this.port = Integer.valueOf(array[1]);
        }

        public void run() {
            running.set(true);
            jedis = new Jedis(host, port);
            try {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        String[] array = message.split(" ");
                        List<String> list = new LinkedList<>();
                        String str;
                        for(int i = 0;i<array.length;i++){
                            str = array[i].trim();
                            if(str.isEmpty()){
                                continue;
                            }

                            if(!str.matches("(([0-9]*+\\.*[0-9]*)*)")){
                                continue;
                            }

                        /*    if(str.endsWith(".")){
                                str = str.substring(0,str.length()-1);
                            }*/
                            list.add(str);
                        }

                        array = new String[list.size()];
                        list.toArray(array);

                        LqLogUtil.info("Sentinel channel:" + channel + " " + host + ":" + port + " published: " + Arrays.toString(array) + ".");
                        if(channel.equals("+switch-master")){
                            node.down(array[0]+":"+array[1]);
                            node.chownMaster(array[2]+":"+array[3]);
                        }else if(channel.equals("+sdown")){
                            node.down(array[0]+":"+array[1]);
                        }else if(channel.equals("-sdown")){
                            node.reboot(array[0]+":"+array[1]);
                        }

                    }
                }, "+switch-master","+sdown","-sdown");
            } catch (JedisConnectionException e) {
                e.printStackTrace();
                running.set(false);
                LQStart.addNeedCheckNode(node);
            }
        }

        public boolean checkRun() {
            if(!running.get()){
                run();
            }
            return running.get();
        }
    }
}
