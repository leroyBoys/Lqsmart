package com.lqsmart.redis.impl;

import com.lqsmart.core.LQStart;
import com.lqsmart.core.LqTimeCacheManager;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.redis.RedisConnection;
import com.lqsmart.redis.entity.RedisKey;
import com.lqsmart.util.LQSerializerTool;
import com.lqsmart.util.LqLogUtil;
import com.lqsmart.util.LqUtil;
import com.lqsmart.util.RandomUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by leroy:656515489@qq.com
 * 2017/4/11.
 */
public class LQRedisConnection extends RedisConnection {
    /**
     *
     * @param url:redis://db@119.254.166.136:6379/pwd
     * @param timeout
     * @param maxTotal
     * @param maxIdel
     */
    public LQRedisConnection(String url, int timeout, int maxTotal, int maxIdel, long maxWaitMillis){
        super(url,timeout,maxTotal,maxIdel,maxWaitMillis);
    }

    /**
     * @param url:redis://db@119.254.166.136:6379/pwd
     */
    public LQRedisConnection(String url){
       super(url);
    }

    public <T> T query(Class<T> cls,Object uniqueId){
        DBTable table = LQStart.instance.getDBTable(cls);
        if(table == null){
            LqLogUtil.error(cls.getName()+" not config redis ");
            return null;
        }

        return table.getRedisSerializer().mergeFrom(this,table,cls,uniqueId);
    }

    public void save(Object obj){
        DBTable table = LQStart.instance.getDBTable(obj.getClass());
        if(table == null){
            LqLogUtil.error(obj.getClass().getName()+" not config redis ");
            return;
        }

        table.getRedisSerializer().serializer(this,table,obj);
    }

    private void resetExpire(byte[] key,RedisKey redisKey){
        if(redisKey instanceof RedisKey.RedisExpireKey){
            RedisKey.RedisExpireKey instanceKey = (RedisKey.RedisExpireKey) redisKey;
            if(instanceKey.getExpire() > 0){
                this.expire(key,instanceKey.getExpire()+ RandomUtil.random(600));
            }

        }else{
            RedisKey.RedisExpireAtKey instanceKey = (RedisKey.RedisExpireAtKey) redisKey;
            if(instanceKey.getExpireAt() > 0){
                this.expireAt(key,instanceKey.getExpireAt()+ RandomUtil.random(1800));
            }
        }
    }

    public String get(RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));
        byte[] data = super.get(redisKey);
        if(data == null){
            return null;
        }
        return LqUtil.byte2hex(data);
    }

    public String set(String value,RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));
        super.set(redisKey,LqUtil.hex2byte(value));
        resetExpire(redisKey,key);
        return value;
    }

    public <T> T getObject(Class<T> cls,RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));
        byte[] data = super.get(redisKey);
        if(data == null){
            return null;
        }
        return LQSerializerTool.mergeFrom(cls,data);
    }

    public String setObject(Object value,RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));
        super.set(redisKey,LQSerializerTool.serializer(value));
        resetExpire(redisKey,key);
        return null;
    }

    /**
     *
     * @param key
     * @param keyParamters
     * @return
     */
    public Map<String, String> hgetAll(RedisKey key,Object... keyParamters) {
        String redisKey = key.getKey(keyParamters);
        Map<String, String> map = super.hgetAll(redisKey);
        if(map != null){
            return map;
        }
        return null;
    }

    public String hget(String field,RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));
        byte[] fieldByte = LqUtil.hex2byte(field);
        byte[] bytes = super.hget(redisKey,fieldByte );
        if(bytes != null){
            return LqUtil.byte2hex(bytes);
        }
        return null;
    }

    /**
     * 将哈希表key中的域field的值设为value Hash操作
     *
     * @param key
     * @param field
     * @param value
     * @return 状态码 1成功，0失败，fieid已存在将更新，也返回0
     */
    public Long hset(String field, String value,RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));
        Long flag = super.hset(redisKey, LqUtil.hex2byte(field), LqUtil.hex2byte(value));
        resetExpire(redisKey,key);
        return flag;
    }

    /**
     *
     * @param key
     * @param hash
     * @return
     */
    public String hmset(Map<String, String> hash,RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));

        Map<byte[], byte[]> hashByts = new HashMap<>(hash.size());
        for(Map.Entry<String,String> entry:hash.entrySet()){
            hashByts.put(LqUtil.hex2byte(entry.getKey()),LqUtil.hex2byte(entry.getValue()));
        }
        String ret = super.hmset(redisKey, hashByts);
        resetExpire(redisKey,key);
        return ret;
    }

    public long del(RedisKey key,Object... keyParamters) {
        return super.del(key.getKey(keyParamters));
    }

    public long hdel(String field,RedisKey key,Object... keyParamters) {
        return super.hdel(LqUtil.hex2byte(key.getKey(keyParamters)), LqUtil.hex2byte(field));
    }

    public long hlen(RedisKey key,Object... keyParamters) {
        return super.hlen(LqUtil.hex2byte(key.getKey(keyParamters)));
    }

    public Long zadd(double score, String member,RedisKey key,Object... keyParamters) {
        return super.zadd(key.getKey(keyParamters), score, member);
    }

    public Double zincrby(double score, String member,RedisKey key,Object... keyParamters) {
        return super.zincrby(key.getKey(keyParamters), score, member);
    }

    public Long zrem(String member,RedisKey key,Object... keyParamters) {
        return super.zrem(key.getKey(keyParamters), member);
    }

    public Set<String> zrevrange(int start, int end, RedisKey key, Object... keyParamters) {
        return super.zrevrange(key.getKey(keyParamters), start, end);
    }

    public Set<String> zrange(int start, int end,RedisKey key,Object... keyParamters) {
        return super.zrange(key.getKey(keyParamters), start, end);
    }

    public Long zrevrank(String member,RedisKey key,Object... keyParamters) {
        return super.zrevrank(key.getKey(keyParamters), member);
    }

    public Long zrank(String member,RedisKey key,Object... keyParamters) {
        return super.zrank(key.getKey(keyParamters), member);
    }

    public Double zscore(String member,RedisKey key,Object... keyParamters) {
        return super.zscore(key.getKey(keyParamters), member);
    }

    public long lpush(String member,RedisKey key,Object... keyParamters) {
        return super.lpush(key.getKey(keyParamters), member);
    }

    public long llen(RedisKey key,Object... keyParamters) {
        return super.llen(key.getKey(keyParamters));
    }

    public List<String> lrange(long start, long end, RedisKey key, Object... keyParamters) {
        return super.lrange(key.getKey(keyParamters), start, end);
    }

    public Long sadd(String[] member,RedisKey key,Object... keyParamters) {
        return super.sadd(key.getKey(keyParamters), member);
    }

    public Long sadd(String member,RedisKey key,Object... keyParamters) {
        return super.sadd(key.getKey(keyParamters), member);
    }

    /**
     * 通过key获取set中所有的value
     * @param key
     * @param keyParamters
     * @return
     */
    public Set<String> smembers(RedisKey key,Object... keyParamters) {
        return super.smembers(key.getKey(keyParamters));
    }

    /**
     * 随机返回Set中 count个成员
     * @param count
     * @param key
     * @param keyParamters
     * @return
     */
    public List<String> srandmember(int count,RedisKey key,Object... keyParamters) {
        return super.srandmember(key.getKey(keyParamters), count);
    }
}
