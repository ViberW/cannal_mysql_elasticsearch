package com.veelur.sync.common.model;

import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class DbConvertModel {
    private String database;
    private String table;
    private String include;
    private String exclude;
    private String pkstr;
    private Integer main;
    private String listname;
    private String listkey;
    private String attchstr;
    private Map<String, String> convert;
    private String datasource;

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

    public Integer getMain() {
        return main;
    }

    public void setMain(Integer main) {
        this.main = main;
    }

    public Map<String, String> getConvert() {
        return convert;
    }

    public void setConvert(Map<String, String> convert) {
        this.convert = convert;
    }

    public String getListname() {
        return listname;
    }

    public void setListname(String listname) {
        this.listname = listname;
    }

    public String getListkey() {
        return listkey;
    }

    public void setListkey(String listkey) {
        this.listkey = listkey;
    }

    public String getAttchstr() {
        return attchstr;
    }

    public void setAttchstr(String attchstr) {
        this.attchstr = attchstr;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
