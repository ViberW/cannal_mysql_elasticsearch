package com.veelur.sync.elasticsearch.service;

import com.veelur.sync.elasticsearch.model.DadaConnectModel;
import com.veelur.sync.elasticsearch.model.DadaDatabaseModel;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public interface DadaMappingService {

    /**
     * 获取Elasticsearch的数据转换后类型
     *
     * @param mysqlType mysql数据类型
     * @param data      具体数据
     * @return Elasticsearch对应的数据类型
     */
    Object getElasticsearchTypeObject(String mysqlType, String data);

    DadaConnectModel getColumnWithData(String database, String table);

    DadaDatabaseModel getDatabaseWithIndexType(String index, String type);

}
