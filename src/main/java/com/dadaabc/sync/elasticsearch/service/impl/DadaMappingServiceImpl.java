package com.dadaabc.sync.elasticsearch.service.impl;

import com.dadaabc.sync.elasticsearch.common.BaseConstants;
import com.dadaabc.sync.elasticsearch.model.DadaConnectModel;
import com.dadaabc.sync.elasticsearch.model.DadaDatabaseModel;
import com.dadaabc.sync.elasticsearch.model.DadaIndexTypeModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
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

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
@Service
@PropertySource("classpath:mapping-dada.yml")
@ConfigurationProperties("dada.db-es")
public class DadaMappingServiceImpl implements DadaMappingService, InitializingBean {

    @Value("${mappings}")
    private List<Map<String, String>> dbEsMapping;
    private BiMap<DadaDatabaseModel, DadaIndexTypeModel> dbEsBiMapping;
    private Map<String, DadaMappingServiceImpl.Converter> mysqlTypeElasticsearchTypeMapping;
    private BiMap<DataDatabaseTableModel, DadaConnectModel> dbSingleMapping;

    public DadaConnectModel getColumnWithData(String database, String table) {
        return dbSingleMapping.get(new DataDatabaseTableModel(database, table));
    }

    @Override
    public Object getElasticsearchTypeObject(String mysqlType, String data) {
        Optional<Map.Entry<String, DadaMappingServiceImpl.Converter>> result = mysqlTypeElasticsearchTypeMapping.entrySet()
                .parallelStream().filter(entry -> mysqlType.toLowerCase().contains(entry.getKey())).findFirst();
        return (result.isPresent() ? result.get().getValue() : (DadaMappingServiceImpl.Converter) data1 -> data1).convert(data);
    }

    @PostConstruct
    public void afterPropertiesSet() {
        dbEsBiMapping = HashBiMap.create();
        if (CollectionUtils.isEmpty(dbEsMapping)) {
            // TODO: 18-9-21 停止
        }
        DadaDatabaseModel dadaDatabaseModel;
        DadaIndexTypeModel dadaIndexTypeModel;
        for (Map<String, String> map : dbEsMapping) {
            dadaDatabaseModel = new DadaDatabaseModel();
            dadaIndexTypeModel = new DadaIndexTypeModel();
            //获取数据库
            String database = map.getOrDefault(BaseConstants.SCHEMA, "");
            dadaDatabaseModel.setDatabase(database);
            String tableStr = map.getOrDefault(BaseConstants.TABLE, "");
            List<DataDatabaseTableModel> models = (List<DataDatabaseTableModel>) ClazzConverterUtils
                    .converterClassArray(tableStr, DataDatabaseTableModel.class);
            dadaDatabaseModel.setModels(models);

            String index = map.getOrDefault(BaseConstants.INDEX, "");
            dadaIndexTypeModel.setIndex(index);
            String type = map.getOrDefault(BaseConstants.TYPE, "");
            dadaIndexTypeModel.setType(type);
            dbEsBiMapping.put(dadaDatabaseModel, dadaIndexTypeModel);
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
