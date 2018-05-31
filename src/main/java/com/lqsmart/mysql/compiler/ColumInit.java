package com.lqsmart.mysql.compiler;

import com.lqsmart.mysql.entity.SqlTypeToJava;
import com.lqsmart.util.LqLogUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/2.
 */
public class ColumInit{
    private SqlTypeToJava sqlTypeToJava;
    private String fieldName;
    public void setSqlTypeToJava(SqlTypeToJava sqlTypeToJava,String fieldName) {
        this.sqlTypeToJava = sqlTypeToJava;
        this.fieldName = fieldName;
    }

    public final void set(Object obj, ResultSet rs, String colum) throws SQLException {
        this.set(obj,sqlTypeToJava.get(rs,colum));
    }

    public final void set(Object obj, ResultSet rs, int index) throws SQLException {
        this.set(obj,sqlTypeToJava.get(rs,index));

    }

    public final void setFromRedis(Object obj,Object fieldValue) throws SQLException {
        this.set(obj,sqlTypeToJava.formtDataFromDb(fieldValue));
    }

    public final void set(Object obj,Object fieldValue){
        try {
            this.doSet(obj,fieldValue);
        }catch (Exception ex){
            LqLogUtil.error("class:"+obj.getClass().getSimpleName()+"  fieldName:"+fieldName+"  fieldValue:"+fieldValue,ex);
        }
    }

    protected void doSet(Object obj,Object fieldValue){}
}
