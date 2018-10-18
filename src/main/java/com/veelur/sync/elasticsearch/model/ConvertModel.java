package com.veelur.sync.elasticsearch.model;

import java.util.List;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class ConvertModel {
    private String index;
    private String type;
    private List<DbConvertModel> dbs;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DbConvertModel> getDbs() {
        return dbs;
    }

    public void setDbs(List<DbConvertModel> dbs) {
        this.dbs = dbs;
    }
}
