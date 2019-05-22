package com.veelur.sync.common.model;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class ConnectModel {

    private VerDatabaseTableModel dbModel;

    private VerIndexTypeModel esModel;

    public VerDatabaseTableModel getDbModel() {
        return dbModel;
    }

    public void setDbModel(VerDatabaseTableModel dbModel) {
        this.dbModel = dbModel;
    }

    public VerIndexTypeModel getEsModel() {
        return esModel;
    }

    public void setEsModel(VerIndexTypeModel esModel) {
        this.esModel = esModel;
    }
}
