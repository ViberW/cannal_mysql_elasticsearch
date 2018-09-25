package com.dadaabc.sync.elasticsearch.model;

import java.util.List;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class DadaConvertModel {
    private String index;
    private String type;
    private List<DadaDbConvertModel> dbs;

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

    public List<DadaDbConvertModel> getDbs() {
        return dbs;
    }

    public void setDbs(List<DadaDbConvertModel> dbs) {
        this.dbs = dbs;
    }
}
