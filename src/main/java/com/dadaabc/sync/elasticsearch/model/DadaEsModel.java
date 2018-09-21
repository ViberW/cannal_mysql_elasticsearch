package com.dadaabc.sync.elasticsearch.model;

import java.util.List;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class DadaEsModel {

    private List<DadaIndexTypeModel> models;

    public List<DadaIndexTypeModel> getModels() {
        return models;
    }

    public void setModels(List<DadaIndexTypeModel> models) {
        this.models = models;
    }
}
