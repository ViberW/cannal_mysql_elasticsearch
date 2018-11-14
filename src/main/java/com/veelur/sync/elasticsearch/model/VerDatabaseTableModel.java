package com.veelur.sync.elasticsearch.model;


import com.veelur.sync.elasticsearch.model.base.DatabaseTableModel;

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

    private List<String> includeField;

    private List<String> excludeField;

    private Map<String, String> attchs;

    private Map<String, String> convert;

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

    public Map<String, String> getAttchs() {
        return attchs;
    }

    public void setAttchs(Map<String, String> attchs) {
        this.attchs = attchs;
    }

    public Map<String, String> getConvert() {
        return convert;
    }

    public void setConvert(Map<String, String> convert) {
        this.convert = convert;
    }
}
