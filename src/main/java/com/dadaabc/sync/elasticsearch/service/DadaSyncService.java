package com.dadaabc.sync.elasticsearch.service;

import com.dadaabc.sync.elasticsearch.model.request.SyncByIndexRequest;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public interface DadaSyncService {

    boolean syncByIndex(SyncByIndexRequest request);
}
