package com.veelur.sync.elasticsearch.service;

import com.veelur.sync.elasticsearch.model.DataDatabaseTableModel;
import com.veelur.sync.elasticsearch.model.request.SyncByIndexRequest;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public interface DadaSyncService {

    boolean syncByIndex(SyncByIndexRequest request);

    String convertColumnAndEsName(String columnName, DataDatabaseTableModel dbModel);
}
