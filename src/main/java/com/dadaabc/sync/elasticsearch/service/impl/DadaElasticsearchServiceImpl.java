package com.dadaabc.sync.elasticsearch.service.impl;

import com.dadaabc.sync.elasticsearch.service.DadaElasticsearchService;
import com.star.sync.elasticsearch.util.JsonUtil;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
@Service
public class DadaElasticsearchServiceImpl implements DadaElasticsearchService {

    private static final Logger logger = LoggerFactory.getLogger(DadaElasticsearchServiceImpl.class);

    //@Autowired
    private TransportClient transportClient;

    /****************************************dada自定义方法****************************************/
    @Override
    public void updateById(String index, String type, String id, Map<String, Object> dataMap) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(index, type, id);
            updateRequest.doc(dataMap);
            UpdateResponse updateResponse = transportClient.update(updateRequest).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    @Override
    public void updateSet(String index, String type, String id, Map<String, Object> dataMap) {
        try {
            IndexRequest indexRequest = new IndexRequest(index, type, id);
            indexRequest.source(dataMap);
            UpdateRequest updateRequest = new UpdateRequest(index, type, id).upsert(indexRequest);
            updateRequest.doc(dataMap);
            UpdateResponse updateResponse = transportClient.update(updateRequest).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    /****************************************简单的根据id进行操作****************************************/
    @Override
    public void insertById(String index, String type, String id, Map<String, Object> dataMap) {
        transportClient.prepareIndex(index, type, id).setSource(dataMap).get();
    }

    @Override
    public void batchInsertById(String index, String type, Map<String, Map<String, Object>> idDataMap) {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        idDataMap.forEach((id, dataMap) -> bulkRequestBuilder.add(transportClient.prepareIndex(index, type, id).setSource(dataMap)));
        try {
            BulkResponse bulkResponse = bulkRequestBuilder.execute().get();
            if (bulkResponse.hasFailures()) {
                logger.error("elasticsearch批量插入错误, index=" + index + ", type=" + type + ", data=" + JsonUtil.toJson(idDataMap) + ", cause:" + bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            logger.error("elasticsearch批量插入错误, index=" + index + ", type=" + type + ", data=" + JsonUtil.toJson(idDataMap), e);
        }
    }

    @Override
    public void deleteById(String index, String type, String id) {
        transportClient.prepareDelete(index, type, id).get();
    }
}
