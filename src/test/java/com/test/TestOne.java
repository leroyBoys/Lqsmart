package com.test;

import com.lqsmart.mysql.entity.LQDBTable;
import com.lqsmart.mysql.entity.LQField;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/4.
 */
@LQDBTable(name = "testone")
public class TestOne {
    @LQField
    private int id;
    @LQField
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
