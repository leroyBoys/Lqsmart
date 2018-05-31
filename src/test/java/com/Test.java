package com;
import com.lqsmart.core.LQStart;
import com.lqsmart.mysql.impl.LQDataSource;
import com.lqsmart.redis.impl.RedisConnectionManager;
import com.lqsmart.util.LqUtil;
import com.test.TestData;

import java.util.List;
import java.util.Random;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/16.
 */
public class Test {

    public void test() throws Exception {
        LQStart.scan("com");

        LQStart.init(LqUtil.loadProperty("db.properties"));

    //    LqJdbcPool jdbcPool = new LqJdbcPool(LqJdbcPool.DataSourceType.Hikari, LqUtil.loadProperty("hikari_db.properties"));
        LQDataSource jdbcPool = LQStart.getJdbcManager().getMaster();
        String sql = "SELECT test_data.* ,test1.`id` AS tid,test1.`name` AS tname FROM `test_data` RIGHT JOIN test1 ON test_data.`id` = test1.`id`";

        List<TestData> testDataList = jdbcPool.ExecuteQueryList(TestData.class,sql);

        System.out.println(testDataList.size());

        TestData testData = testDataList.get(1);
        testData.setName("tomess");
        jdbcPool.ExecuteEntity(testData);

        System.out.println(testData.getName());


        testData.setId(0);
        testData.setName("新增");
        jdbcPool.ExecuteEntity(testData);

        System.out.println(testData.getName()+"   "+testData.getId());
    }

    @org.junit.Test
    public void test2() throws Exception {
        LQStart.scan("com");
        String sql = "SELECT test_data.* ,test1.`id` AS tid,test1.`name` AS tname FROM `test_data` RIGHT JOIN test1 ON test_data.`id` = test1.`id`\n";
        LQStart.init(LqUtil.loadProperty("db.properties"));
        LQDataSource db = LQStart.getJdbcManager().getMaster();

        RedisConnectionManager redisConn = LQStart.getRedisConnectionManager();


        String key ="abd";
    /*    redisConn.getMaster("6378").set(key,"哈哈哈222");
        System.out.println("master:"+redisConn.getRandomSlave("6378").get(key));
        Thread.sleep(100);
        System.out.println("sleav1:"+redisConn.getRandomSlave("6378").get(key));
        Thread.sleep(100);
        System.out.println("sleav1:"+redisConn.getRandomSlave("6378").get(key));*/


        String keyss = "keysskeyss";
        redisConn.getMaster("6378").set(keyss,keyss+new Random().nextInt(222));
        redisConn.getMaster("6378").get(keyss);
        redisConn.getRandomSlave("6378").get(keyss);

        while (true){
            try {
                redisConn.getMaster("6378").set(keyss,keyss+new Random().nextInt(222));
                redisConn.getMaster("6378").get(keyss);
                System.out.println(redisConn.getRandomSlave("6378").get(keyss));
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("===>error:"+e.getMessage());
            }

            Thread.sleep(200);
        }
    }
}
