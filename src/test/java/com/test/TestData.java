package com.test;

import com.lqsmart.mysql.entity.DBRelation;
import com.lqsmart.mysql.entity.DBRelations;
import com.lqsmart.mysql.entity.LQDBTable;
import com.lqsmart.mysql.entity.LQField;
import com.lqsmart.redis.entity.RedisCache;

import java.util.Date;
import java.util.List;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/2.
 */
@RedisCache
@LQDBTable(name="test_data")
public class TestData {
    @LQField(name = "id",isPrimaryKey = true)
    private int id;
    @LQField()
    private String num;
    @LQField()
    private Date date;
    @LQField()
    private String name;
    @LQField(convertDBType= LQField.ConvertDBType.BoolNumber)
    private boolean isGood;

    @LQField(name ="test_enum")
    private TestEnum testEnum;

    @LQField(name ="test_enum2",convertDBTypeClass = "com.test.MyConvertDefaultDBType")
    private TestEnum2 testEnum2;

    @DBRelations(relation = DBRelations.Reltaion.OneToMany,
            map={
                @DBRelation(colum = "id",targetColum = "id"),
                @DBRelation(colum = "tname",targetColum = "name")
            })
    private List<Test1> test1;

    @DBRelations(relation = DBRelations.Reltaion.OneToOne,
            map={
                    @DBRelation(colum = "id",targetColum = "id"),
                    @DBRelation(colum = "toname",targetColum = "name")
            })
    private TestOne testOne;

    @LQField(convertDBType= LQField.ConvertDBType.BoolNumber)
    private Boolean visid;
    @LQField()
    private Double mydouble;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TestEnum2 getTestEnum2() {
        return testEnum2;
    }

    public int db_getTestEnum2() {
        return testEnum2 == null?0:testEnum2.getDBValue();
    }

    public void setTestEnum2(TestEnum2 testEnum2) {
        this.testEnum2 = testEnum2;
    }

    public TestEnum getTestEnum() {
        return testEnum;
    }

    public void setTestEnum(TestEnum testEnum) {
        this.testEnum = testEnum;
    }

    public boolean isGood() {
        return isGood;
    }

    public int db_isGood() {
        return isGood?1:0;
    }

    public void setGood(boolean good) {
        isGood = good;
    }

    public Boolean isVisid() {
        return visid;
    }

    public int db_isVisid() {
        return visid != null && visid?1:0;
    }

    public List<Test1> getTest1() {
        return test1;
    }

    public void setTest1(List<Test1> test1) {
        this.test1 = test1;
    }

    public TestOne getTestOne() {
        return testOne;
    }

    public void setTestOne(TestOne testOne) {
        this.testOne = testOne;
    }

    public void setVisid(Boolean visid) {
        this.visid = visid;
    }

    public Double getMydouble() {
        return mydouble;
    }

    public void setMydouble(Double mydouble) {
        this.mydouble = mydouble;
    }
}
