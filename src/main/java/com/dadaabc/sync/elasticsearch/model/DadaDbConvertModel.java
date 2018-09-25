package com.dadaabc.sync.elasticsearch.model;

import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class DadaDbConvertModel {
    private String database;
    private String table;
    private String include;
    private String exclude;
    private String pkstr;
    private Boolean main;
    private Map<String, String> convert;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getPkstr() {
        return pkstr;
    }

    public void setPkstr(String pkstr) {
        this.pkstr = pkstr;
    }

    public Boolean getMain() {
        return main;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }

    public Map<String, String> getConvert() {
        return convert;
    }

    public void setConvert(Map<String, String> convert) {
        this.convert = convert;
    }
}
