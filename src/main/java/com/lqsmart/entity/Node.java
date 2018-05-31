package com.lqsmart.entity;

import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/24.
 */
public abstract class Node<T extends LQConntion>{
    private T master;
    private T[] slaves;
    private final boolean slowSlaveOn;//是否支持慢查询
    private Map<String,T> allConnectionMap = new HashMap<>();
    private short max;
    private short cur = 0;
    private final Object refreshLock = new Object();
    public Node(boolean slowSlaveOn){
        this.slowSlaveOn = slowSlaveOn;
    }

    public boolean initProperties(String listeners,Properties masterConfig, Properties... slavesConfig){

        boolean initSuc = false;
        if(masterConfig != null){
           T t = this.initRedisConnection(masterConfig);
           if(t.connctioned()){
               initSuc = true;
              if(!allConnectionMap.containsKey(t.getKey())){
                  allConnectionMap.put(t.getKey(),t);
              }
               master = t;
           }
        }

        if (slavesConfig != null && slavesConfig.length > 0) {
            for (int i = 0; i < slavesConfig.length; i++) {
                try {
                    T t = initRedisConnection(slavesConfig[i]);
                    if(!t.connctioned()){
                        continue;
                    }

                    if(!allConnectionMap.containsKey(t.getKey())){
                        allConnectionMap.put(t.getKey(),t);
                    }

                    if(!initSuc) initSuc = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        if(initSuc){
            init(listeners);
        }
        return initSuc;
    }

    protected void init(String listeners){

        short length = (short) allConnectionMap.size();
        String configMasterKey = null;
        if(master != null){//无master配置，自动选择一个slave为master即可
           String key =  checkMaster(master);
           if(!allConnectionMap.containsKey(key)){
               throw new RuntimeException(master.getKey()+"not master becuause master is:"+key);
           }
           master = allConnectionMap.get(key);
           length--;
           configMasterKey = master.getKey();
        }

        slaves = createArray(length);
        int i = 0;
        for(T t:allConnectionMap.values()){
            if(t.getKey().equals(configMasterKey)){
                continue;
            }

            slaves[i++] = t;
        }
        max = (short) (length - 1);
    }

    protected abstract T initRedisConnection(Properties properties);
    public abstract T[] createArray(int size);

    /**
     * 定时检测并返回是否需要监控改node
     * @return
     */
    public boolean loopCheck(){
        return false;
    }

    public synchronized void down(String hostPort){
        T connection = allConnectionMap.get(hostPort);
        if(connection == null || !connection.connctioned()){
            return;
        }
        down(connection);
    }

    /**
     * 下线并移除当前状态及所在列表
     * @param down
     */
    public synchronized void down(T down){
        if(down == null || !down.connctioned()){
            return;
        }
        down.setConnetioned(false);

        LinkedList<T> list = new LinkedList<>();
        if(master != null && down.getKey().equals(master.getKey())){//如果是master,则选一个slave为master即可
            Collections.addAll(list,slaves);
            master = list.removeFirst();
        }else {
            for(T t:slaves){
                if(!t.getKey().equals(down.getKey())){
                    list.add(t);
                }
            }
        }

        T[] array = createArray(list.size());
        list.toArray(array);

        synchronized (refreshLock){
            max = (short) (array.length-1);
            if(cur>max) cur = 0;
            slaves = array;
        }

    }

    /**
     * 上线恢复其当前角色职能
     * @param hostPort
     */
    public synchronized void reboot(String hostPort){
        T connection = allConnectionMap.get(hostPort);
        if(connection == null || connection.connctioned()){
            return;
        }

        connection.setConnetioned(true);
        connection.reLoad();
        if(master != null && hostPort.equals(master.getKey())){
            return;
        }

        boolean isSlave = false;
        for(T t:slaves){
            if(t.getKey().equals(master.getKey())){
                isSlave = true;
                break;
            }
        }

        if(isSlave){
            return;
        }

        T[] array = createArray(slaves.length+1);
        System.arraycopy(slaves,0,array,1,slaves.length);
        array[0] = connection;
        synchronized (refreshLock){
            max = (short) (array.length-1);
            slaves = array;
        }
    }

    /**
     * 重新赋予master权限
     * @param hostPort
     */
    public synchronized void chownMaster(String hostPort){
        if(master != null && hostPort.equals(master.getKey())){
            return;
        }

        T connection = allConnectionMap.get(hostPort);
        if(connection == null){
            return;
        }

        T  oldMaster = master;
        master = connection;
        if(oldMaster != null && oldMaster.connctioned()){//原master加入slaves
            boolean isSlave = false;
            for(T t:slaves){
                if(t.getKey().equals(oldMaster.getKey())){
                    isSlave = true;
                    break;
                }
            }

            if(isSlave){
                return;
            }
            T[] array = createArray(slaves.length+1);
            System.arraycopy(slaves,0,array,1,slaves.length);
            array[0] = oldMaster;

            synchronized (refreshLock){
                max = (short) (array.length-1);
                slaves = array;
            }
        }

    }

    public String checkMaster(T master){
        return master.getKey();
    }

    public T getMaster() {
        return master;
    }

    public T getRandomSlave() {
        if(slaves.length == 1){
            return slaves[0];
        }

        synchronized (refreshLock){
            if(cur++==max){
                cur = (short) (slowSlaveOn && max>0?1:0);
            }
        }

        return slaves[cur];
    }

    public T getSlowSlave(){
        if(!slowSlaveOn){
            return getRandomSlave();
        }
        return slaves[0];
    }

    public T[] getSlaves() {
        return slaves;
    }

}
