package com.lqsmart.mysql.db.impl;

import com.lqsmart.mysql.compiler.FieldGetProxy;
import com.lqsmart.mysql.db.DbExecutor;
import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.mysql.entity.LQPage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public class MysqlExecutor implements DbExecutor{
    @Override
    public String updateSql(Object instance, DBTable table) {
        StringBuilder sql = new StringBuilder(50);
        sql.append("update").append(' ').append(table.getName()).append(' ');
        sql.append("set").append(' ');
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
            sql.append(entry.getKey()).append('=');
            if(object == null){
                sql.append("null");
            }else{
                sql.append('\'').append(object).append('\'');
            }
            i++;
        }
        sql.append(' ').append("where id = ").append(table.getColumGetMap().get(table.getIdColumName()).formatToDbData(instance));
        return sql.toString();
    }

    @Override
    public String insertSql(Object instance, DBTable table) {
        StringBuilder sql = new StringBuilder(50);
        StringBuilder names = new StringBuilder(20);
        StringBuilder values = new StringBuilder(20);
        sql.append("insert into").append(' ').append(table.getName());
        names.append(' ').append('(').append(' ');
        values.append(' ').append('(').append(' ');
        int i = 0;
        Object object;
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
            if(object == null){
                values.append("null");
            }else{
                values.append('\'').append(object).append('\'');
            }
            i++;
        }
        names.append(' ').append(')').append(' ');
        values.append(' ').append(')').append(' ');
        sql.append(names).append(' ').append("values").append(' ').append(values);
        return sql.toString();
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
    public String getQuerySqlForPage(String sourceSql, LQPage lqPage) {
        StringBuilder sb = new StringBuilder(sourceSql);
        sb.append(" limit ").append(lqPage.getStart()).append(',').append(lqPage.getEnd());

        return null;
    }

    @Override
    public String getResultCountForQuerySql(String sourceSql, LQPage lqPage) {
        return null;
    }
}
