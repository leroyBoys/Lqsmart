package com.lqsmart.module;

import com.lqsmart.mysql.entity.LQDBTable;
import com.lqsmart.mysql.entity.LQField;

/**
 * Created by Administrator on 2017/4/26.
 */
@LQDBTable
public class GameServer {
    @LQField(isPrimaryKey = true)
    private int id;
    private int g_v_id;
    private String zoneName;
    private String zoneDes;
    private String zoneIcon;
    private ServerType serverType;
    private int groupNum;
    private String ip;
    private int udpPort;
    private int port;
    private int maxCount;
    private int severStatus;
    private String key;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getG_v_id() {
        return g_v_id;
    }

    public void setG_v_id(int g_v_id) {
        this.g_v_id = g_v_id;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getZoneDes() {
        return zoneDes;
    }

    public String getZoneIcon() {
        return zoneIcon;
    }

    public void setZoneIcon(String zoneIcon) {
        this.zoneIcon = zoneIcon;
    }

    public void setZoneDes(String zoneDes) {
        this.zoneDes = zoneDes;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public int getGroupNum() {
        return groupNum;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setGroupNum(int groupNum) {
        this.groupNum = groupNum;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getSeverStatus() {
        return severStatus;
    }

    public void setSeverStatus(int severStatus) {
        this.severStatus = severStatus;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public enum ServerType{
        server,gate
    }
}
