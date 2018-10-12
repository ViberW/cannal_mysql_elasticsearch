package com.veelur.sync.elasticsearch.model;

import java.util.List;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class DadaDatabaseModel {

    List<DataDatabaseTableModel> models;

    public List<DataDatabaseTableModel> getModels() {
        return models;
    }

    public void setModels(List<DataDatabaseTableModel> models) {
        this.models = models;
    }
}
