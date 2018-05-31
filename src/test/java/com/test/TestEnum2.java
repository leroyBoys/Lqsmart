package com.test;

import com.lqsmart.mysql.entity.LQDBEnum;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/14.
 */
public enum TestEnum2 implements LQDBEnum<Integer> {
    Hell(1),More(2);
    private int i;
    TestEnum2(int i ){
        this.i = i;
    }

    public int getI() {
        return i;
    }

    @Override
    public Integer getDBValue() {
        return i;
    }
}
