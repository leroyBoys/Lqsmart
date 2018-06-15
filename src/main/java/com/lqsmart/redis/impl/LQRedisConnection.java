package com.lqsmart.redis.impl;

import com.lqsmart.core.LQStart;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.redis.RedisConnection;
import com.lqsmart.redis.entity.RedisExecuter;
import com.lqsmart.redis.entity.RedisKey;
import com.lqsmart.util.LqLogUtil;
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

    private void resetExpire(byte[] key,RedisKey redisKey,long expireAtTime){
        if(expireAtTime > 0){
            this.expireAt(key,expireAtTime+ RandomUtil.random(1800));
            return;
        }

        this.expire(key,redisKey.getExpire()+ RandomUtil.random(600));
    }

    protected String getRedisKey(RedisKey key,Object... keyParamters){
        if(keyParamters == null || keyParamters.length == 0){
            return key.getPrexKey();
        }

        String redisKey = key.getPrexKey();
        for(int i=0,size=keyParamters.length;i<size;i++){
            redisKey+="."+keyParamters[i];
        }
        return redisKey;
    }

    public <T> T getObject(RedisKey key,Object... keyParamters) {
        byte[] redisKey = RedisExecuter.serializerForString(getRedisKey(key,keyParamters));
        byte[] data = super.get(redisKey);
        if(data == null){
            return null;
        }
        return (T) key.getSerialzer().mergeFrom(data);
    }

    public String setObject(RedisKey key,Object value,Object... keyParamters) {
        return setObject(key,value,0,keyParamters);
    }

    public String setObject(RedisKey key,Object value,long expireAtTime,Object... keyParamters) {
        byte[] redisKey = RedisExecuter.serializerForString(getRedisKey(key,keyParamters));
        super.set(redisKey,key.getSerialzer().serializer(value));
        resetExpire(redisKey,key,expireAtTime);
        return null;
    }

    /**
     *
     * @param key
     * @param keyParamters
     * @return
     */
    public Map<String, String> hgetAll(RedisKey key,Object... keyParamters) {
        String redisKey = getRedisKey(key,keyParamters);
        Map<String, String> map = super.hgetAll(redisKey);
        if(map != null){
            return map;
        }
        return null;
    }

    public String hget(RedisKey key,String field,Object... keyParamters) {
        byte[] redisKey = RedisExecuter.serializerForString(getRedisKey(key,keyParamters));
        byte[] fieldByte = RedisExecuter.serializerForString(field);
        byte[] bytes = super.hget(redisKey,fieldByte);
        if(bytes != null){
            return RedisExecuter.mergeFromString(bytes);
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
    public Long hset(RedisKey key,String field, String value,Object... keyParamters) {
        return hset(key,field,value,0,keyParamters);
    }

    public Long hset(RedisKey key,String field, String value,long expireAtTime,Object... keyParamters) {
        byte[] redisKey = RedisExecuter.serializerForString(getRedisKey(key,keyParamters));
        Long flag = super.hset(redisKey, RedisExecuter.serializerForString(field), RedisExecuter.serializerForString(value));
        resetExpire(redisKey,key,expireAtTime);
        return flag;
    }

    /**
     *
     * @param key
     * @param hash
     * @return
     */
    public String hmset(RedisKey key,Map<String, String> hash,Object... keyParamters) {
        return hmset(key,hash,0,keyParamters);
    }

    public String hmset(RedisKey key,Map<String, String> hash,long expireAtTime,Object... keyParamters) {
        byte[] redisKey = RedisExecuter.serializerForString(getRedisKey(key,keyParamters));

        Map<byte[], byte[]> hashByts = new HashMap<>(hash.size());
        for(Map.Entry<String,String> entry:hash.entrySet()){
            hashByts.put(RedisExecuter.serializerForString(entry.getKey()), RedisExecuter.serializerForString(entry.getValue()));
        }
        String ret = super.hmset(redisKey, hashByts);
        resetExpire(redisKey,key,expireAtTime);
        return ret;
    }

    public long del(RedisKey key,Object... keyParamters) {
        return super.del(getRedisKey(key,keyParamters));
    }

    public long hdel(RedisKey key,String field,Object... keyParamters) {
        return super.hdel(RedisExecuter.serializerForString(getRedisKey(key,keyParamters)), RedisExecuter.serializerForString(field));
    }

    public long hlen(RedisKey key,Object... keyParamters) {
        return super.hlen(RedisExecuter.serializerForString(getRedisKey(key,keyParamters)));
    }

    public Long zadd(RedisKey key,double score, String member,Object... keyParamters) {
        return super.zadd(getRedisKey(key,keyParamters), score, member);
    }

    public Double zincrby(RedisKey key,double score, String member,Object... keyParamters) {
        return super.zincrby(getRedisKey(key,keyParamters), score, member);
    }

    public Long zrem(String member,RedisKey key,Object... keyParamters) {
        return super.zrem(getRedisKey(key,keyParamters), member);
    }

    public Set<String> zrevrange(int start, int end, RedisKey key, Object... keyParamters) {
        return super.zrevrange(getRedisKey(key,keyParamters), start, end);
    }

    public Set<String> zrange(int start, int end,RedisKey key,Object... keyParamters) {
        return super.zrange(getRedisKey(key,keyParamters), start, end);
    }

    public Long zrevrank(String member,RedisKey key,Object... keyParamters) {
        return super.zrevrank(getRedisKey(key,keyParamters), member);
    }

    public Long zrank(String member,RedisKey key,Object... keyParamters) {
        return super.zrank(getRedisKey(key,keyParamters), member);
    }

    public Double zscore(String member,RedisKey key,Object... keyParamters) {
        return super.zscore(getRedisKey(key,keyParamters), member);
    }

    public long lpush(String member,RedisKey key,Object... keyParamters) {
        return super.lpush(getRedisKey(key,keyParamters), member);
    }

    public long llen(RedisKey key,Object... keyParamters) {
        return super.llen(getRedisKey(key,keyParamters));
    }

    public List<String> lrange(long start, long end, RedisKey key, Object... keyParamters) {
        return super.lrange(getRedisKey(key,keyParamters), start, end);
    }

    public Long sadd(String[] member,RedisKey key,Object... keyParamters) {
        return super.sadd(getRedisKey(key,keyParamters), member);
    }

    public Long sadd(String member,RedisKey key,Object... keyParamters) {
        return super.sadd(getRedisKey(key,keyParamters), member);
    }

    /**
     * 通过key获取set中所有的value
     * @param key
     * @param keyParamters
     * @return
     */
    public Set<String> smembers(RedisKey key,Object... keyParamters) {
        return super.smembers(getRedisKey(key,keyParamters));
    }

    /**
     * 随机返回Set中 count个成员
     * @param count
     * @param key
     * @param keyParamters
     * @return
     */
    public List<String> srandmember(int count,RedisKey key,Object... keyParamters) {
        return super.srandmember(getRedisKey(key,keyParamters), count);
    }
}
