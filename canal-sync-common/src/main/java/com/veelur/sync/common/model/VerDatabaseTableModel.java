package com.veelur.sync.common.model;


import com.veelur.sync.common.model.base.DatabaseTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class VerDatabaseTableModel extends DatabaseTableModel {

    private String listname;

    private String mainKey;

    private Integer main;

    private String pkStr;

    private List<String> years;

    private List<String> includeField;

    private List<String> excludeField;

    private AttchNode attchs;

    private List<String> attchKeys;

    private Map<String, String> convert;

    private Boolean addition = false;

    private String additionField;

    private String datasource;

    public VerDatabaseTableModel(String database, String table) {
        super(database, table);
    }

    public VerDatabaseTableModel() {
    }

    public String getListname() {
        return listname;
    }

    public void setListname(String listname) {
        this.listname = listname;
    }

    public String getMainKey() {
        return mainKey;
    }

    public void setMainKey(String mainKey) {
        this.mainKey = mainKey;
    }

    public Integer getMain() {
        return main;
    }

    public void setMain(Integer main) {
        this.main = main;
    }

    public String getPkStr() {
        return pkStr;
    }

    public void setPkStr(String pkStr) {
        this.pkStr = pkStr;
    }

    public List<String> getYears() {
        if (null == years) {
            years = new ArrayList<>();
        }
        return years;
    }

    public void setYears(List<String> years) {
        this.years = years;
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

    public AttchNode getAttchs() {
        return attchs;
    }

    public void setAttchs(AttchNode attchs) {
        this.attchs = attchs;
    }

    public List<String> getAttchKeys() {
        return attchKeys;
    }

    public void setAttchKeys(List<String> attchKeys) {
        this.attchKeys = attchKeys;
    }

    public Map<String, String> getConvert() {
        return convert;
    }

    public void setConvert(Map<String, String> convert) {
        this.convert = convert;
    }

    public Boolean getAddition() {
        return addition;
    }

    public void setAddition(Boolean addition) {
        this.addition = addition;
    }

    public String getAdditionField() {
        return additionField;
    }

    public void setAdditionField(String additionField) {
        this.additionField = additionField;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
