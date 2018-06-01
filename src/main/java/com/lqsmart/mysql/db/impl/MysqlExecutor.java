package com.lqsmart.mysql.db.impl;

import com.lqsmart.mysql.compiler.FieldGetProxy;
import com.lqsmart.mysql.db.DbExecutor;
import com.lqsmart.mysql.entity.DBTable;

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
        sql.append("update ").append(table.getName());
        sql.append("  set ");
        int i = 0;
        Object object;
        for(Map.Entry<String,FieldGetProxy.FieldGet> entry:table.getColumGetMap().entrySet()){
            if(entry.getKey() == table.getIdColumName()){
                continue;
            }
            if(i > 0){
                sql.append(" , ");
            }
            object =  entry.getValue().formatToDbData(instance);
            sql.append(entry.getKey()).append("=");
            if(object == null){
                sql.append("null");
            }else{
                sql.append("'").append(object).append("'");
            }
            i++;
        }
        sql.append("  where id = ").append(table.getColumGetMap().get(table.getIdColumName()).formatToDbData(instance));
        return sql.toString();
    }

    @Override
    public String insertSql(Object instance, DBTable table) {
        StringBuilder sql = new StringBuilder(50);
        StringBuilder names = new StringBuilder(20);
        StringBuilder values = new StringBuilder(20);
        sql.append("insert into ").append(table.getName());
        names.append("  ( ");
        values.append("  ( ");
        int i = 0;
        Object object;
        for(Map.Entry<String,FieldGetProxy.FieldGet> entry:table.getColumGetMap().entrySet()){
            if(entry.getKey() == table.getIdColumName()){
                continue;
            }

            if(i > 0){
                names.append(" , ");
                values.append(" , ");
            }
            names.append("`").append(entry.getKey()).append("`");
            object =  entry.getValue().formatToDbData(instance);
            if(object == null){
                values.append("null");
            }else{
                values.append("'").append(object).append("'");
            }
            i++;
        }
        names.append("  ) ");
        values.append("  ) ");
        sql.append(names).append("  values ").append(values);
        return sql.toString();
    }

    @Override
    public List<String> insertBatchSql(String tableName, List<Map<String, String>> datas, String[] columNames, String[] columValues, int commitLimitCount) {
        if(commitLimitCount <= 0){
            commitLimitCount = defaultCommitLimitCount;
        }

        StringBuilder sb = new StringBuilder("INSERT INTO  ");
        sb.append(tableName).append(" (");
        for(int i = 0;i<columNames.length;i++){
            if(i != 0){
                sb.append(",");
            }
            sb.append("`").append(columNames[i]).append("`");
        }
        sb.append(") values ");
        String sql = sb.toString();

        String str;
        List<String> sqls = new LinkedList<>();
        int i = 0;
        for(Map<String,String> map:datas){
            if(i++ != 0){
                sb.append(",");
            }

            sb.append("(");
            for(int j = 0;j<columNames.length;j++){
                if(j != 0){
                    sb.append(",");
                }

                str = columValues[j];
                if(str.endsWith("()")){
                    sb.append(str);
                    continue;
                }

                sb.append("'");
                str = map.get(str);
                if(str == null){
                    str = columValues[j];
                }
                sb.append(str);
                sb.append("'");
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
}
