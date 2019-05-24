package com.veelur.sync.parse.service;

import com.veelur.sync.common.model.ConnectModel;
import com.veelur.sync.common.model.DatabaseModel;
import com.veelur.sync.common.model.VerIndexTypeModel;

import java.util.List;
import java.util.Set;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public interface VerMappingService {

    /**
     * 获取Elasticsearch的数据转换后类型
     *
     * @param mysqlType mysql数据类型
     * @param data      具体数据
     * @return Elasticsearch对应的数据类型
     */
    Object getElasticsearchTypeObject(String mysqlType, String data);

    List<ConnectModel> getColumnWithData(String database, String table);

    DatabaseModel getDatabaseWithIndexType(String index, String type);

    Set<VerIndexTypeModel> getIndexTypeModels();

}
