package com.dadaabc.sync.elasticsearch.model;

import com.star.sync.elasticsearch.model.DatabaseTableModel;

import java.util.List;
import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class DataDatabaseTableModel extends DatabaseTableModel {

    private Boolean main;

    private String pkStr;

    private List<String> includeField;

    private List<String> excludeField;

    private Map<String, Data2EsFieldModel> fields;

    public DataDatabaseTableModel(String database, String table) {
        super(database, table);
    }

    public DataDatabaseTableModel() {
    }

    public Boolean getMain() {
        return main;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }

    public String getPkStr() {
        return pkStr;
    }

    public void setPkStr(String pkStr) {
        this.pkStr = pkStr;
    }

    public List<String> getIncludeField() {
        return includeField;
    }

    public void setIncludeField(List<String> includeField) {
        this.includeField = includeField;
    }

    public List<String> getExcludeField() {
        return excludeField;
    }

    public void setExcludeField(List<String> excludeField) {
        this.excludeField = excludeField;
    }

    public Map<String, Data2EsFieldModel> getFields() {
        return fields;
    }

    public void setFields(Map<String, Data2EsFieldModel> fields) {
        this.fields = fields;
    }
}
