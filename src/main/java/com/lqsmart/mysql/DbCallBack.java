package com.lqsmart.mysql;

import java.sql.ResultSet;

/**
 * Created by Administrator on 2017/5/7.
 */
public interface DbCallBack<T> {
    public T doInPreparedStatement(ResultSet rs);
}
