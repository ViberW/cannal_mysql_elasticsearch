package com.veelur.sync.elasticsearch.service;

import com.veelur.sync.elasticsearch.exception.InfoNotRightException;
import com.veelur.sync.elasticsearch.model.VerDatabaseTableModel;
import com.veelur.sync.elasticsearch.model.request.SyncByIndexRequest;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public interface VerSyncService {

    boolean syncByIndex(SyncByIndexRequest request) throws InfoNotRightException;

    String convertColumnAndEsName(String columnName, VerDatabaseTableModel dbModel);
}