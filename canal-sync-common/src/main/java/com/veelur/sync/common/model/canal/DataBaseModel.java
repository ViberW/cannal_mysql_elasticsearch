package com.veelur.sync.common.model.canal;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: Admin
 * @date: 2019/5/13
 * @Description: {相关描述}
 */
public class DataBaseModel {
    private String datasource;
    private String jdbcUrl;
    private String username;
    private String password;

    @Override
    public int hashCode() {
        return System.identityHashCode(datasource);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataBaseModel) {
            DataBaseModel model = (DataBaseModel) obj;
            return StringUtils.isNotBlank(model.getDatasource())
                    && model.getDatasource().equals(datasource);
        }
        return false;
    }

    public DataBaseModel() {
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
