package com.lqsmart.redis.impl;

import com.lqsmart.core.LQStart;
import com.lqsmart.core.LqTimeCacheManager;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.redis.RedisConnection;
import com.lqsmart.redis.entity.RedisKey;
import com.lqsmart.util.LqLogUtil;
import com.lqsmart.util.LqUtil;

import java.util.HashMap;
import java.util.Map;

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
                this.expire(key,instanceKey.getExpire());
            }

        }else if(redisKey instanceof RedisKey.RedisExpireAtKey){
            RedisKey.RedisExpireAtKey instanceKey = (RedisKey.RedisExpireAtKey) redisKey;
            if(instanceKey.getExpireAt() > 0){
                this.expireAt(key,instanceKey.getExpireAt());
            }
        }else if(redisKey instanceof RedisKey.RedisExpiresKey){
            RedisKey.RedisExpiresKey redisExpiresKey = (RedisKey.RedisExpiresKey) redisKey;
            if(redisExpiresKey.getExpire() > 0){
                if(redisExpiresKey.getExpireAt() > 0){
                    long endTime = LqTimeCacheManager.getInstance().getCurTime()+redisExpiresKey.getExpire()*1000;
                    endTime = Math.min(endTime,redisExpiresKey.getExpireAt());
                    this.expireAt(key,endTime);
                }else {
                    this.expire(key,redisExpiresKey.getExpire());
                }
            }else if(redisExpiresKey.getExpireAt() > 0){
                this.expireAt(key,redisExpiresKey.getExpireAt());
            }
        }
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
        if(map == null){
            if(key.isSynFromDb()){
                map = (Map<String, String>) key.queryFromDb(keyParamters);
                if(map == null ||  map.isEmpty()){
                    return null;
                }
                super.hmset(redisKey,map);
                return map;
            }
        }else {
            return map;
        }
        return null;
    }

    public Map<String, String> hgetAll2(RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));

        Map<byte[], byte[]> map = super.hgetAll(redisKey);
        Map<String, String> retMap;
        if(map == null){
            if(key.isSynFromDb()){
                retMap = (Map<String, String>) key.queryFromDb(keyParamters);
                if(retMap == null ||  retMap.isEmpty()){
                    return null;
                }
                map = new HashMap<>(retMap.size());
                for(Map.Entry<String,String> entry:retMap.entrySet()){
                    map.put(LqUtil.hex2byte(entry.getKey()),LqUtil.hex2byte(entry.getValue()));
                }
                super.hmset(redisKey,map);
                return retMap;
            }
        }else {
            retMap = new HashMap<>(map.size());

            for(Map.Entry<byte[],byte[]> entry:map.entrySet()){
                retMap.put(LqUtil.byte2hex(entry.getKey()),LqUtil.byte2hex(entry.getValue()));
            }
            return retMap;
        }
        return null;
    }

    public String hget(String field,RedisKey key,Object... keyParamters) {
        byte[] redisKey = LqUtil.hex2byte(key.getKey(keyParamters));
        byte[] fieldByte = LqUtil.hex2byte(field);
        byte[] bytes = super.hget(redisKey,fieldByte );
        if(bytes == null){
            if(key.isSynFromDb()){
                String value = (String) key.queryFromDb(keyParamters);
                if(value == null){
                    return null;
                }
                hset(redisKey,fieldByte,LqUtil.hex2byte(value));
                return value;
            }
        }else {
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

   /* public Long zrem(String member,RedisKey key,Object... keyParamters) {
        return super.zrem(key.getKey(), member);
    }

    public Set<String> zrevrange(int start, int end,RedisKey key,Object... keyParamters) {
        return super.zrevrange(key.getKey(), start, end);
    }

    public Set<String> zrange(int start, int end,RedisKey key,Object... keyParamters) {
        return super.zrange(key.getKey(), start, end);
    }

    public Long zrevrank(String member,RedisKey key,Object... keyParamters) {
        return super.zrevrank(key.getKey(), member);
    }

    public Long zrank(String member,RedisKey key,Object... keyParamters) {
        return super.zrank(key.getKey(), member);
    }

    public Double zscore(String member,RedisKey key,Object... keyParamters) {
        return super.zscore(key.getKey(), member);
    }

    public long lpush(String member,RedisKey key,Object... keyParamters) {
        return super.lpush(key.getKey(), member);
    }

    public long llen(RedisKey key,Object... keyParamters) {
        return super.llen(key.getKey());
    }

    public List<String> lrange(long start, long end,RedisKey key,Object... keyParamters) {
        return super.lrange(key.getKey(), start, end);
    }

    public Long sadd(String[] member,RedisKey key,Object... keyParamters) {
        return super.sadd(key.getKey(), member);
    }

    public Long sadd(String member,RedisKey key,Object... keyParamters) {
        return super.sadd(key.getKey(), member);
    }

    public Set<String> smembers(RedisKey key,Object... keyParamters) {
        return super.smembers(key.getKey());
    }

    public List<String> srandmember(int count,RedisKey key,Object... keyParamters) {
        return super.srandmember(key.getKey(), count);
    }*/
}
