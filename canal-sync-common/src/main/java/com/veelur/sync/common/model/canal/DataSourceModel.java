package com.veelur.sync.common.model.canal;

/**
 * @author: veelur
 * @date: 18-11-1
 * @Description: {相关描述}
 */
public class DataSourceModel {

    private String host;
    private String port;
    private String destination;
    private String username;
    private String password;
    private Integer batchSize;
    private Boolean onOff;
    private String model;
    private DataBaseModel database;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Boolean getOnOff() {
        return onOff;
    }

    public void setOnOff(Boolean onOff) {
        this.onOff = onOff;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public DataBaseModel getDatabase() {
        return database;
    }

    public void setDatabase(DataBaseModel database) {
        this.database = database;
    }
}
