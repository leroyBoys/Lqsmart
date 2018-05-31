package com.lqsmart.mysql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by leroy:656515489@qq.com
 * 2017/4/13.
 */
public interface SqlDataSource {
    public Connection getConnection() throws SQLException;
}
