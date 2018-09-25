package com.dadaabc.sync.elasticsearch.model;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class DadaConnectModel {

    private DataDatabaseTableModel dbModel;

    private DadaIndexTypeModel esModel;

    public DataDatabaseTableModel getDbModel() {
        return dbModel;
    }

    public void setDbModel(DataDatabaseTableModel dbModel) {
        this.dbModel = dbModel;
    }

    public DadaIndexTypeModel getEsModel() {
        return esModel;
    }

    public void setEsModel(DadaIndexTypeModel esModel) {
        this.esModel = esModel;
    }
}
