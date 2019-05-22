package com.veelur.sync.dao;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.SqlDateTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author: Admin
 * @date: 2019/1/29
 * @Description: {相关描述}
 */
public class MySqlDateTypeHandler extends SqlDateTypeHandler {

    private static final Logger logger = LoggerFactory.getLogger(MySqlDateTypeHandler.class);

    public MySqlDateTypeHandler() {
        super();
    }

    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
        try {
            super.setNonNullParameter(ps, i, parameter, jdbcType);
        } catch (Exception e) {
        }
    }

    public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return super.getNullableResult(rs, columnName);
        } catch (Exception e) {
            return null;
        }
    }

    public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return super.getNullableResult(rs, columnIndex);
        } catch (Exception e) {
            return null;
        }
    }

    public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return super.getNullableResult(cs, columnIndex);
        } catch (Exception e) {
            return null;
        }
    }
}
