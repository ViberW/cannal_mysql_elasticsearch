package com.dadaabc.sync.elasticsearch.model;

import java.util.List;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class DadaDatabaseModel {

    private String database;

    List<DataDatabaseTableModel> models;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public List<DataDatabaseTableModel> getModels() {
        return models;
    }

    public void setModels(List<DataDatabaseTableModel> models) {
        this.models = models;
    }
}
