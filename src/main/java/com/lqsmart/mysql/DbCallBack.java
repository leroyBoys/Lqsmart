package com.lqsmart.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Administrator on 2017/5/7.
 */
public interface DbCallBack<T> {
    public T doInPreparedStatement(ResultSet rs) throws SQLException;
}
