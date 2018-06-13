package com.lqsmart.entity;

import com.lqsmart.util.LqLogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/24.
 */
public abstract class NodeManger<T extends LQConntion> extends Thread{
    private Node<T> node;
    private String tmpNodeName;
    private Map<String,Node<T>> nodeMap = new HashMap<>();

    public void initProperties(String nodeName,boolean slowSlaveOn,String listeners,Properties masterConfig, Properties... slavesConfig) throws Exception {

        if(nodeName == null){
            if(node != null && this.tmpNodeName == null){
                return;
            }

            Node<T> _node = intanceNode(slowSlaveOn);
            if(!_node.initProperties(listeners,masterConfig,slavesConfig)){
                return;
            }
            node = _node;
            this.tmpNodeName = null;
            return;
        }

        Node<T> nodeData = nodeMap.get(nodeName);
        boolean notHave = nodeData == null;
        if(notHave){
            nodeData = intanceNode(slowSlaveOn);
        }

        if(!nodeData.initProperties(listeners,masterConfig,slavesConfig)){
            return;
        }

        if(nodeMap.isEmpty()){
            node = nodeData;
            this.tmpNodeName = nodeName;
        }

        if(notHave){
            nodeMap.put(nodeName,nodeData);
        }
    }

    protected abstract Node<T> intanceNode(boolean slowSlaveOn);

    public T getMaster() {
        return node.getMaster();
    }

    public T getMaster(String nodeName) {
        Node<T> nodeData = nodeMap.get(nodeName);
        if(nodeData == null){
            return null;
        }
        return nodeData.getMaster();
    }

    public T getRandomSlave() {
        return node.getRandomSlave();
    }

    public T getSlowSlave() {
        return node.getSlowSlave();
    }

    public Node<T> getNode(){
        return node;
    }

    public T getRandomSlave(String nodeName) {
        Node<T> nodeData = nodeMap.get(nodeName);
        if(nodeData == null){
            return null;
        }
        return nodeData.getRandomSlave();
    }

    public T[] getSlaves() {
        return node.getSlaves();
    }

    public T[] getSlaves(String nodeName) {
        Node<T> nodeData = nodeMap.get(nodeName);
        if(nodeData == null){
            return null;
        }
        return nodeData == null?null: nodeData.getSlaves();
    }

    public synchronized void loseConnection(String nodeName, String key){
        Node<T> loseNode  = node;
        if(nodeName != null && !nodeName.trim().isEmpty()){
            loseNode = nodeMap.get(nodeName);
        }else {
            nodeName = "_";
        }

        if(loseNode == null){
            LqLogUtil.error("connection lose for:nodeName:"+nodeName+" url:"+key+" not find");
            return;
        }

      //  loseNode.addLoseKey(key);

        //loseNode.loseConnection(key);
        this.start();
    }

    private volatile boolean isRun;
    @Override
    public void run() {
        if(isRun){
            return;
        }

        isRun = true;
      //  check();
    }

 /*   private synchronized void check(){
        if (!loseNodeKeys.isEmpty()){
            for(Map.Entry<String,Node<T>> entry:loseNodeKeys.entrySet()){
                if(entry.getValue().isNotLoseKey()){
                    loseNodeKeys.remove(entry.getKey());
                    return;
                }
            }
        }

        if (loseNodeKeys.isEmpty()){
            isRun = false;
            return;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        check();
    }*/
}
