package com.lqsmart.mysql.db;

import com.lqsmart.mysql.entity.DBTable;

import java.util.List;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/1.
 */
public interface DbExecutor {
    /** 批量插入sql分割界限默认值 **/
    int defaultCommitLimitCount = 500;
    String updateSql(Object instance, DBTable table);

    String insertSql(Object instance, DBTable table);

    /**
     *
     * @param tableName
     * @param datas
     * @param columNames 要插入数据的列名集合(与columValues 顺序对应)
     * @param columValues 要插入数据的列名对应值（或者函数或者固定值）集合(与columValues 顺序对应)
     * @param commitLimitCount 最大提交数量（根据mysql.cnf中 max_allowed_packet调整）如果小于等于0则为默认5000
     * @param
     * @return
     */
    List<String> insertBatchSql(String tableName, List<Map<String, String>> datas, String[] columNames, String[] columValues, int commitLimitCount);
}
