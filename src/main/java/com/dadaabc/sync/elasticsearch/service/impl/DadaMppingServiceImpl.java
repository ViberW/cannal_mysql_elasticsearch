package com.dadaabc.sync.elasticsearch.service.impl;

import com.dadaabc.sync.elasticsearch.common.BaseConstants;
import com.dadaabc.sync.elasticsearch.model.DadaDatabaseModel;
import com.dadaabc.sync.elasticsearch.model.DadaEsModel;
import com.dadaabc.sync.elasticsearch.model.DadaIndexTypeModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.service.DadaMppingService;
import com.dadaabc.sync.elasticsearch.util.ClazzConverterUtils;
import com.dadaabc.sync.elasticsearch.util.DateUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
@Service
@PropertySource("classpath:mapping-dada.yml")
@ConfigurationProperties("dada.db-es")
public class DadaMppingServiceImpl implements DadaMppingService, InitializingBean {

    @Value("${mappings}")
    private List<Map<String, String>> dbEsMapping;
    private BiMap<DadaDatabaseModel, DadaEsModel> dbEsBiMapping;
    private Map<String, DadaMppingServiceImpl.Converter> mysqlTypeElasticsearchTypeMapping;

    @Override
    public void afterPropertiesSet() throws Exception {
        dbEsBiMapping = HashBiMap.create();
        if (CollectionUtils.isEmpty(dbEsMapping)) {
            // TODO: 18-9-21 停止
        }
        DadaDatabaseModel dadaDatabaseModel;
        DadaEsModel dadaEsModel;
        DadaIndexTypeModel dadaIndexTypeModel;
        List<DadaIndexTypeModel> dadaIndexTypeModels;
        for (Map<String, String> map : dbEsMapping) {
            dadaDatabaseModel = new DadaDatabaseModel();
            dadaEsModel = new DadaEsModel();
            dadaIndexTypeModel = new DadaIndexTypeModel();
            dadaIndexTypeModels = new ArrayList<>();
            //获取数据库
            String database = map.getOrDefault(BaseConstants.SCHEMA, "");
            dadaDatabaseModel.setDatabase(database);
            String tableStr = map.getOrDefault(BaseConstants.TABLE, "");
            List<DataDatabaseTableModel> models = (List<DataDatabaseTableModel>) ClazzConverterUtils.converterClassArray(tableStr, DataDatabaseTableModel.class);
            dadaDatabaseModel.setModels(models);

            String index = map.getOrDefault(BaseConstants.INDEX, "");
            dadaIndexTypeModel.setIndex(index);
            String type = map.getOrDefault(BaseConstants.TYPE, "");
            dadaIndexTypeModel.setType(type);
            dadaIndexTypeModels.add(dadaIndexTypeModel);
            dadaEsModel.setModels(dadaIndexTypeModels);
            dbEsBiMapping.put(dadaDatabaseModel, dadaEsModel);
        }

        mysqlTypeElasticsearchTypeMapping = Maps.newHashMap();
        mysqlTypeElasticsearchTypeMapping.put("char", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("text", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("blob", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("int", Long::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("date", DateUtils::strToDate4GMT);
        mysqlTypeElasticsearchTypeMapping.put("time", DateUtils::strToDate4GMT);
        mysqlTypeElasticsearchTypeMapping.put("float", Double::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("double", Double::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("decimal", Double::valueOf);
    }

    @FunctionalInterface
    private interface Converter {
        Object convert(String data);
    }
}
