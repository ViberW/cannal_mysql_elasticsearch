package com.veelur.sync.canal.extend;

import org.apache.commons.lang.StringUtils;

/**
 * @author: Admin
 * @date: 2019/5/13
 * @Description: {相关描述}
 */
public class InstanceMonitorModel {
    private String address;
    private String dbUsername;
    private String dbPassword;
    private String defaultDatabaseName;
    private String whiteRegex;

    public InstanceMonitorModel() {
    }

    public InstanceMonitorModel(String address, String dbUsername, String dbPassword, String defaultDatabaseName, String whiteRegex) {
        this.address = address;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.defaultDatabaseName = defaultDatabaseName;
        this.whiteRegex = whiteRegex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDefaultDatabaseName() {
        return defaultDatabaseName;
    }

    public void setDefaultDatabaseName(String defaultDatabaseName) {
        this.defaultDatabaseName = defaultDatabaseName;
    }

    public String getWhiteRegex() {
        return whiteRegex;
    }

    public void setWhiteRegex(String whiteRegex) {
        this.whiteRegex = whiteRegex;
    }

    public boolean check() {
        return StringUtils.isNotEmpty(this.address)
                && StringUtils.isNotEmpty(this.dbUsername)
                && StringUtils.isNotEmpty(this.dbPassword);
    }
}
