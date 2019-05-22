package com.veelur.sync.common.model;

import java.util.List;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class DatabaseModel {

    List<VerDatabaseTableModel> models;

    public List<VerDatabaseTableModel> getModels() {
        return models;
    }

    public void setModels(List<VerDatabaseTableModel> models) {
        this.models = models;
    }
}
