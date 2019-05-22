package com.dadaabc.sync.parse.service;

import com.veelur.sync.common.exception.InfoNotRightException;
import com.veelur.sync.common.model.AttchNode;
import com.veelur.sync.common.model.VerDatabaseTableModel;
import com.veelur.sync.common.model.request.SyncByIndexRequest;

import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public interface VerSyncService {

    boolean syncByIndex(SyncByIndexRequest request) throws InfoNotRightException;

    String convertColumnAndEsName(String columnName, VerDatabaseTableModel dbModel, String index);

    boolean checkAttch(AttchNode node, Map<String, Object> map);
}
