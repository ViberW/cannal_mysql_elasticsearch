package com.dadaabc.sync.elasticsearch.model;

import com.star.sync.elasticsearch.model.DatabaseTableModel;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class DataDatabaseTableModel extends DatabaseTableModel {

    private String field = "*";

    private String includeField;

    private String excludeField;

    private Data2EsFieldModel fields;

    public Data2EsFieldModel getFields() {
        return fields;
    }

    public void setFields(Data2EsFieldModel fields) {
        this.fields = fields;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getIncludeField() {
        return includeField;
    }

    public void setIncludeField(String includeField) {
        this.includeField = includeField;
    }

    public String getExcludeField() {
        return excludeField;
    }

    public void setExcludeField(String excludeField) {
        this.excludeField = excludeField;
    }
}
