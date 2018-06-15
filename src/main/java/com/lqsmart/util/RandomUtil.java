package com.lqsmart.util;

import java.util.Random;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/13.
 */
public class RandomUtil {
    private static Random random = new Random();
    public static int random(int max){
        return random.nextInt(max);
    }
}
