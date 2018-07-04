package com.lqsmart.mysql.db.impl;

import com.lqsmart.mysql.compiler.FieldGetProxy;
import com.lqsmart.mysql.db.DbExecutor;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.mysql.entity.LQPage;

import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public class MysqlExecutor implements DbExecutor{
    @Override
    public String getQuerySqlForAll(DBTable dbTable) {
        return "select * from "+dbTable.getName();
    }

    @Override
    public String getQuerySqlForId(DBTable dbTable,Object id) {
        return "select * from "+dbTable.getName()+" where "+dbTable.getIdColumName()+" = "+id;
    }

    @Override
    public String getDelSqlForId(DBTable dbTable, Object id) {
        return "delete from "+dbTable.getName()+" where "+dbTable.getIdColumName()+" = "+id;
    }

    @Override
    public SqlData updateSql(Object instance, DBTable table) {
        StringBuilder sql = new StringBuilder(50);
        sql.append("update").append(' ').append(table.getName()).append(' ');
        sql.append("set").append(' ');

        Object[] p = new Object[table.getColumGetMap().entrySet().size()-1];
        int i = 0;
        Object object;
        for(Map.Entry<String,FieldGetProxy.FieldGet> entry:table.getColumGetMap().entrySet()){
            if(entry.getKey() == table.getIdColumName()){
                continue;
            }
            if(i > 0){
                sql.append(' ').append(',').append(' ');
            }
            object =  entry.getValue().formatToDbData(instance);
            sql.append(entry.getKey()).append('=').append('?');
            p[i] = object;
            i++;
        }
        sql.append(' ').append("where id = ").append(table.getColumGetMap().get(table.getIdColumName()).formatToDbData(instance));
        return new SqlData(sql.toString(),p);
    }

    @Override
    public SqlData insertSql(Object instance, DBTable table) {
        StringBuilder sql = new StringBuilder(50);
        StringBuilder names = new StringBuilder(20);
        StringBuilder values = new StringBuilder(20);
        sql.append("insert into").append(' ').append(table.getName());
        names.append(' ').append('(').append(' ');
        values.append(' ').append('(').append(' ');
        int i = 0;
        Object object;

        Object[] p = new Object[table.getColumGetMap().entrySet().size()-1];
        for(Map.Entry<String,FieldGetProxy.FieldGet> entry:table.getColumGetMap().entrySet()){
            if(entry.getKey() == table.getIdColumName()){
                continue;
            }

            if(i > 0){
                names.append(' ').append(',').append(' ');
                values.append(' ').append(',').append(' ');
            }
            names.append('`').append(entry.getKey()).append('`');
            object =  entry.getValue().formatToDbData(instance);
            values.append('?');
            p[i] = object;
            i++;
        }
        names.append(' ').append(')').append(' ');
        values.append(' ').append(')').append(' ');
        sql.append(names).append(' ').append("values").append(' ').append(values);
        return new SqlData(sql.toString(),p);
    }

    @Override
    public List<String> insertBatchSql(String tableName, List<Map<String, String>> datas, String[] columNames, int commitLimitCount) {
        if(commitLimitCount <= 0){
            commitLimitCount = defaultCommitLimitCount;
        }

        StringBuilder sb = new StringBuilder("INSERT INTO").append(' ');
        sb.append(tableName).append(' ').append('(');
        for(int i = 0;i<columNames.length;i++){
            if(i != 0){
                sb.append(',');
            }
            sb.append('`').append(columNames[i]).append('`');
        }
        sb.append(") values").append(' ');
        String sql = sb.toString();

        String str;
        List<String> sqls = new LinkedList<>();
        int i = 0;
        for(Map<String,String> map:datas){
            if(i++ != 0){
                sb.append(',');
            }

            sb.append("(");
            for(int j = 0;j<columNames.length;j++){
                if(j != 0){
                    sb.append(',');
                }

                str = map.get(columNames[j]);
                if(str == null){
                    sb.append("null");
                    continue;
                }

                sb.append('\'');
                sb.append(str);
                sb.append('\'');
            }
            sb.append(")");

            if(i > commitLimitCount){
                sqls.add(sb.toString());
                sb=new StringBuilder(sql);
                i=0;
            }
        }

        if(i > 0){
            sqls.add(sb.toString());
        }
        return sqls;
    }

    @Override
    public String getQuerySqlForPage(DBTable dbTable,LQPage lqPage) {
        String sql = "select * ";
        sql+=getFromSql(dbTable,lqPage);
        LinkedHashMap<String,String> sorts = lqPage.getSortColums();
        if(!sorts.isEmpty()){
            int i = 0;
            for(Map.Entry<String,String> columEntry:sorts.entrySet()){
                if(i++ == 0){
                    sql+=" order by "+columEntry.getKey()+"  "+columEntry.getValue();
                }else {
                    sql+=" , "+columEntry.getKey()+"  "+columEntry.getValue();
                }
            }
        }
        sql+=" limit "+lqPage.getStart()+","+lqPage.getEnd();
        return sql;
    }

    protected String getFromSql(DBTable dbTable,LQPage lqPage){
        String fromSql = " from "+dbTable.getName();
        Map<String, String> conditions = lqPage.getConditions();
        int i = 0;
        if(conditions != null && !conditions.isEmpty()){
            for(Map.Entry<String,String> entry:conditions.entrySet()){
                if(dbTable.getColumInit(entry.getKey()) == null){
                    continue;
                }
                if(i++ == 0){
                    fromSql+=" where ";
                }else {
                    fromSql+=" and ";
                }
                fromSql += "  "+entry.getKey()+" = '"+entry.getValue()+"'";
            }
        }

        conditions = lqPage.getLikeConditions();
        if(conditions != null && !conditions.isEmpty()){
            for(Map.Entry<String,String> entry:conditions.entrySet()){
                if(dbTable.getColumInit(entry.getKey()) == null){
                    continue;
                }
                if(i++ == 0){
                    fromSql+=" where ";
                }else {
                    fromSql+=" and ";
                }
                fromSql += "  "+entry.getKey()+" like '%"+entry.getValue()+"%'";
            }
        }

        return fromSql;
    }

    @Override
    public String getResultCountForQuerySql(DBTable dbTable,LQPage lqPage) {
        return  "SELECT COUNT(1) " +getFromSql(dbTable,lqPage);
    }
}
