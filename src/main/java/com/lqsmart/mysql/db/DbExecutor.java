package com.lqsmart.mysql.db;

import com.lqsmart.mysql.entity.DBTable;
import com.lqsmart.mysql.entity.LQPage;

import java.util.List;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public interface DbExecutor {
    class SqlData{
        private String sql;
        private Object[] paratmers;

        public SqlData(String sql, Object[] paratmers) {
            this.sql = sql;
            this.paratmers = paratmers;
        }

        public String getSql() {
            return sql;
        }

        public Object[] getParatmers() {
            return paratmers;
        }
    }

    /** 批量插入sql分割界限默认值 **/
    int defaultCommitLimitCount = 5000;
    SqlData updateSql(Object instance, DBTable table);

    SqlData insertSql(Object instance, DBTable table);

    /**
     *
     * @param tableName
     * @param datas
     * @param columNames 要插入数据的列名集合(与columValues 顺序对应)
     * @param commitLimitCount 最大提交数量（根据mysql.cnf中 max_allowed_packet调整）如果小于等于0则为默认5000
     * @param
     * @return
     */
    List<String> insertBatchSql(String tableName, List<Map<String, String>> datas, String[] columNames, int commitLimitCount);

    /**
     * 将普通的查询sql转换page有关的sql
     * @param lqPage
     * @return
     */
    String getQuerySqlForPage(LQPage lqPage);

    /**
     * 将普通的查询sql转换查询总数量的sql
     * @param lqPage
     * @return
     */
    String getResultCountForQuerySql(LQPage lqPage);
}
