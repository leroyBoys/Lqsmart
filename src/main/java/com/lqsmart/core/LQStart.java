package com.lqsmart.core;

import com.lqsmart.entity.*;
import com.lqsmart.mysql.compiler.ScanEntitysTool;
import com.lqsmart.mysql.impl.JDBCManager;
import com.lqsmart.redis.impl.RedisConnectionManager;
import com.lqsmart.util.LqLogUtil;
import com.lqsmart.util.LqUtil;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/2.
 */
public class LQStart extends Thread{
    private static WeakReference<StartInitCache> tmpCache = new WeakReference<>(new StartInitCache());
    public static ScanEntitysTool instance;
    public final static Set<Node> needCheckNodes = new HashSet<>();
    public final static LQStart loopCheck = new LQStart();
    static {
        loopCheck.start();
    }

    public static StartInitCache getMethodCache(){
        StartInitCache startInitCache = tmpCache.get();
        if(startInitCache != null){
            return startInitCache;
        }
        startInitCache = new StartInitCache();
        tmpCache = new WeakReference<>(startInitCache);
        return startInitCache;
    }

    public static void scan(String... packs) throws Exception {
        if(instance != null){
            return;
        }
        synchronized (ScanEntitysTool.class){
            if(instance != null){
                return;
            }
            instance = new ScanEntitysTool(packs);
        }
    }

    public static void init(Properties properties) throws Exception {
        Map<DBType,MasterSlaveGlobalConfig> globalConfigMap = new HashMap<>();
        for(DBType dbType: DBType.values()){
            globalConfigMap.put(dbType,new MasterSlaveGlobalConfig(dbType));
        }
        StartInitCache startInitCache = getMethodCache();
        boolean configIsRight = false;

        Map<String,MasterSlaveConfig> node_configMap = new HashMap<>();
        for (Enumeration<?> e = properties.keys(); e.hasMoreElements() ;) {
            Object ko = e.nextElement();
            if (!(ko instanceof String)) {
                continue;
            }

            String k = (String) ko;
            String v = properties.get(k).toString();

            if(k.indexOf("datasource")<0){
               continue;
            }

            String[] array = k.split("\\.");
            if(array.length < 3){
                System.err.println("数据格式配置错误:"+k);
                continue;
            }

            DBType dbType = DBType.valueOf(array[1]);
            if(dbType == null){
                LqLogUtil.warn("warn: not find match dbType:"+array[1]);
                continue;
            }

            if(!configIsRight) configIsRight = true;
            if(array.length == 3){
                globalConfigMap.get(dbType).addMaster(propertiesKey(k),v);
                globalConfigMap.get(dbType).addSlave(propertiesKey(k),v);
                continue;
            }

            if(array.length == 4){
                if(array[2].equals("master")){
                    globalConfigMap.get(dbType).addMaster(propertiesKey(k),v);
                }else {
                    globalConfigMap.get(dbType).addSlave(propertiesKey(k),v);
                }
                continue;
            }

            String node = array[2];
            MasterSlaveConfig masterSlaveConfig= node_configMap.get(node);
            if(masterSlaveConfig == null){
                masterSlaveConfig = new MasterSlaveConfig(dbType);
                node_configMap.put(node,masterSlaveConfig);
            }

            if(array[3].equals("master")){
                masterSlaveConfig.addMaster(propertiesKey(k),v);
            }else if(array[3].startsWith("slave")){
                masterSlaveConfig.addSlave(array[3],propertiesKey(k),v);
            }else if(array[4].startsWith("sentinel")){
                masterSlaveConfig.setListeners(v);
            }else if(array[4].startsWith("slowopen")){
                masterSlaveConfig.setSlowOpen(array[4].toLowerCase().equals("true")?true:false);
            }
        }

        if(!configIsRight){
            System.err.println("error:配置数据源格式错误，为初始化数据源");
            return;
        }

        startInitCache.getGlobalConfigMap().putAll(globalConfigMap);
        startInitCache.getNode_configMap().putAll(node_configMap);

        Set<DBType> dbTypes = new HashSet<>();

        if(!node_configMap.isEmpty()){

            for(Map.Entry<String,MasterSlaveConfig> entry:node_configMap.entrySet()){
                MasterSlaveConfig masterSlaveConfig = entry.getValue();
                MasterSlaveGlobalConfig config = globalConfigMap.get(masterSlaveConfig.getDbType());
                inintDataSourceManger(masterSlaveConfig.getDbType(),entry.getKey(),masterSlaveConfig.isSlowOpen(),masterSlaveConfig.getListeners(),config.getMaster(masterSlaveConfig.getMaster()),config.getSlave(masterSlaveConfig.getSlaves()));
                dbTypes.add(masterSlaveConfig.getDbType());
            }
        }

        for(DBType dbType: DBType.values()){
           if(dbTypes.contains(dbType)){
               continue;
           }
            inintDataSourceManger(dbType,null,false,null,globalConfigMap.get(dbType).getMaster(null));
        }
    }

    private static Properties getNewProperties(DBType dbType, LQNewNode newNode, LQConnConfig connConfig, boolean isMaster){
        StartInitCache startInitCache = getMethodCache();
        MasterSlaveGlobalConfig masterSlaveGlobalConfig = startInitCache.getGlobalConfigMap().get(dbType);

        Map<String,String> globlmap = new HashMap<>(50);
        if(masterSlaveGlobalConfig != null){
            Map<String,String> map = null;
            if(isMaster){
                globlmap = masterSlaveGlobalConfig.getMaster();
            }else {
                globlmap = masterSlaveGlobalConfig.getSlave();
            }
            globlmap.putAll(map);
        }

        if(newNode != null && newNode.getReadNodeName() != null){
            MasterSlaveConfig masterSlaveConfig = startInitCache.getNode_configMap().get(newNode.getReadNodeName());
            if(masterSlaveConfig != null){
                Map<String,String> map = null;
                if(isMaster){
                    map = masterSlaveConfig.getMaster();
                }else {
                    map = masterSlaveConfig.getSlaves().isEmpty()?masterSlaveConfig.getMaster():masterSlaveConfig.getSlaves().values().iterator().next();
                }
                globlmap.putAll(map);
            }
        }
        if(dbType == DBType.db){
            LQConnConfig.LQDBConnConfig lqRedisConnConfig = (LQConnConfig.LQDBConnConfig) connConfig;
            globlmap.put("url",lqRedisConnConfig.getUrl());
            globlmap.put("jdbcUrl",lqRedisConnConfig.getUrl());
            globlmap.put("username",lqRedisConnConfig.getUserName());
            globlmap.put("user",lqRedisConnConfig.getUserName());
            globlmap.put("password",lqRedisConnConfig.getPassword());
        }else if(dbType == DBType.redis){
            LQConnConfig.LQRedisConnConfig lqRedisConnConfig = (LQConnConfig.LQRedisConnConfig) connConfig;
            globlmap.put("url",lqRedisConnConfig.getUrl());
        }
        return LqUtil.createProperties(globlmap);
    }

    public static void addNewDataSource(DBType dbType, LQConnConfig master, LQConnConfig... slaves) throws Exception {
        addNewDataSource(dbType,null,master,slaves);
    }

    public static void addNewDataSource(DBType dbType, LQNewNode newNode, LQConnConfig master, LQConnConfig... slaves) throws Exception {
        Properties masterProperties = getNewProperties(dbType,newNode,master,true);
        Properties[] properties = null;
        if(slaves != null && slaves.length>0){
            properties = new Properties[slaves.length];
            for(int i = 0;i<slaves.length;i++){
                properties[i] = getNewProperties(dbType,newNode,slaves[i],false);
            }
        }

        if(newNode == null){
            inintDataSourceManger(dbType,null,false,null,masterProperties,properties);
        }else {
            inintDataSourceManger(dbType,newNode.getNewNodeName(),newNode.isSlowOpen(),newNode.getSentinels(),masterProperties);
        }
    }

    /**
     *
     * @param dbType
     * @param nodeName
     * @param slowSlaveOn 慢查询是否开启
     * @param listeners
     * @param master
     * @param slaves
     * @throws Exception
     */
    private static void inintDataSourceManger(DBType dbType, String nodeName, boolean slowSlaveOn, String listeners, Properties master, Properties... slaves) throws Exception {
        dbType.getNodeManger().initProperties(nodeName,slowSlaveOn,listeners,master,slaves);
    }

    private static String propertiesKey(String configKey){
        return configKey.substring(configKey.lastIndexOf(".")+1);
    }

    public static RedisConnectionManager getRedisConnectionManager(){
        return (RedisConnectionManager) DBType.redis.getNodeManger();
    }

    public static JDBCManager getJdbcManager() {
        return (JDBCManager) DBType.db.getNodeManger();
    }

    @Override
    public void run() {
        while (true){
            synchronized (needCheckNodes){
                if(!needCheckNodes.isEmpty()){
                    Iterator<Node> iterator = needCheckNodes.iterator();
                    while (iterator.hasNext()){
                        Node node = iterator.next();
                        if(!node.loopCheck()){
                            iterator.remove();
                        }
                    }
                }
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addNeedCheckNode(Node node){
        synchronized (needCheckNodes){
            needCheckNodes.add(node);
        }
    }
}
