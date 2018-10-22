package com.veelur.sync.elasticsearch.service.impl;

import com.veelur.sync.elasticsearch.common.BaseConstants;
import com.veelur.sync.elasticsearch.common.MainTypeEnum;
import com.veelur.sync.elasticsearch.exception.InfoNotRightException;
import com.veelur.sync.elasticsearch.service.VerMappingService;
import com.veelur.sync.elasticsearch.util.CollectionUtils;
import com.veelur.sync.elasticsearch.util.DateUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.veelur.sync.elasticsearch.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
@Service
@ConfigurationProperties(prefix = "ver.db-es")
public class VerMappingServiceImpl implements VerMappingService, InitializingBean {

    private List<ConvertModel> mappings = new ArrayList<>();


    public List<ConvertModel> getMappings() {
        return mappings;
    }

    public void setMappings(List<ConvertModel> mappings) {
        this.mappings = mappings;
    }

    private BiMap<DatabaseModel, VerIndexTypeModel> dbEsBiMapping;
    private Map<String, VerMappingServiceImpl.Converter> mysqlTypeElasticsearchTypeMapping;
    private BiMap<VerDatabaseTableModel, ConnectModel> dbSingleMapping;

    @Override
    public DatabaseModel getDatabaseWithIndexType(String index, String type) {
        return dbEsBiMapping.inverse().get(new VerIndexTypeModel(index, type));
    }

    @Override
    public ConnectModel getColumnWithData(String database, String table) {
        return dbSingleMapping.get(new VerDatabaseTableModel(database, table));
    }

    @Override
    public Object getElasticsearchTypeObject(String mysqlType, String data) {
        Optional<Map.Entry<String, VerMappingServiceImpl.Converter>> result = mysqlTypeElasticsearchTypeMapping.entrySet()
                .parallelStream().filter(entry -> mysqlType.toLowerCase().contains(entry.getKey())).findFirst();
        return (result.isPresent() ? result.get().getValue() : (VerMappingServiceImpl.Converter) data1 -> data1).convert(data);
    }

    @Override
    public Set<VerIndexTypeModel> getIndexTypeModels() {
        if (CollectionUtils.isNotEmpty(dbEsBiMapping)) {
            return dbEsBiMapping.values();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws InfoNotRightException {
        dbEsBiMapping = HashBiMap.create();
        if (CollectionUtils.isEmpty(mappings)) {
            throw new InfoNotRightException("mapping映射为空");
        }
        DatabaseModel databaseModel;
        VerIndexTypeModel verIndexTypeModel;
        List<VerDatabaseTableModel> models;
        VerDatabaseTableModel tableModel;
        ConnectModel connectModel;
        String include;
        String exclude;
        String[] split;
        dbSingleMapping = HashBiMap.create();

        for (ConvertModel model : mappings) {
            boolean havaMain = false;
            verIndexTypeModel = new VerIndexTypeModel();
            verIndexTypeModel.setIndex(model.getIndex());
            verIndexTypeModel.setType(model.getType());
            //获取数据库
            databaseModel = new DatabaseModel();
            List<DbConvertModel> dbs = model.getDbs();
            models = new ArrayList<>();
            for (DbConvertModel dbConvertModel : dbs) {
                tableModel = new VerDatabaseTableModel();
                tableModel.setDatabase(dbConvertModel.getDatabase());
                tableModel.setTable(dbConvertModel.getTable());
                tableModel.setMain(null != dbConvertModel.getMain() ? dbConvertModel.getMain() : MainTypeEnum.MAIN.getCode());
                if (havaMain && MainTypeEnum.MAIN.getCode().equals(tableModel.getMain())) {
                    throw new InfoNotRightException("包含重复的mapping-main信息");
                }
                String listkv = dbConvertModel.getListkv();
                if (MainTypeEnum.ONE_TO_MORE.getCode().equals(tableModel.getMain())) {
                    if (StringUtils.isNotEmpty(listkv)) {
                        String[] split1 = listkv.split(BaseConstants.DEFAULT_SPLIT_2);
                        tableModel.setListname(split1[0]);
                        tableModel.setMainKey(split1[1]);
                    } else {
                        tableModel.setListname(tableModel.getTable());
                        tableModel.setMainKey(BaseConstants.DEFAULT_ID);
                    }
                }
                tableModel.setPkStr(StringUtils.isEmpty(dbConvertModel.getPkstr()) ? BaseConstants.DEFAULT_ID : dbConvertModel.getPkstr());
                include = dbConvertModel.getInclude();
                if (StringUtils.isNotEmpty(include)) {
                    split = include.split(BaseConstants.DEFAULT_SPLIT);
                    tableModel.setIncludeField(Arrays.asList(split));
                }
                exclude = dbConvertModel.getExclude();
                if (StringUtils.isNotEmpty(exclude)) {
                    split = exclude.split(BaseConstants.DEFAULT_SPLIT);
                    tableModel.setExcludeField(Arrays.asList(split));
                }
                tableModel.setConvert(dbConvertModel.getConvert());
                models.add(tableModel);
                if (MainTypeEnum.MAIN.getCode().equals(tableModel.getMain())) {
                    havaMain = true;
                    if (StringUtils.isEmpty(verIndexTypeModel.getIndex())) {
                        verIndexTypeModel.setIndex(tableModel.getDatabase());
                    }
                    if (StringUtils.isEmpty(verIndexTypeModel.getType())) {
                        verIndexTypeModel.setIndex(tableModel.getTable());
                    }
                }
                connectModel = new ConnectModel();
                connectModel.setDbModel(tableModel);
                connectModel.setEsModel(verIndexTypeModel);
                dbSingleMapping.put(tableModel, connectModel);
            }
            databaseModel.setModels(models);
            dbEsBiMapping.put(databaseModel, verIndexTypeModel);
        }
        mysqlTypeElasticsearchTypeMapping = Maps.newHashMap();
        mysqlTypeElasticsearchTypeMapping.put("char", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("text", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("blob", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("int", Long::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("date", DateUtils::convertDate);
        mysqlTypeElasticsearchTypeMapping.put("time", DateUtils::convertDate);
        mysqlTypeElasticsearchTypeMapping.put("float", Double::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("double", Double::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("decimal", Double::valueOf);
    }


    @FunctionalInterface
    private interface Converter {
        Object convert(String data);
    }
}
