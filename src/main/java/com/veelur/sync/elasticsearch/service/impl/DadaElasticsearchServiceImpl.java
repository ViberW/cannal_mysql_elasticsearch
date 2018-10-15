package com.veelur.sync.elasticsearch.service.impl;

import com.veelur.sync.elasticsearch.service.DadaElasticsearchService;
import com.star.sync.elasticsearch.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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

    @Autowired
    private TransportClient transportClient;

    /****************************************dada自定义方法****************************************/
    @Override
    public void updateById(String index, String type, String id, Map<String, Object> dataMap) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(index, type, id);
            updateRequest.doc(dataMap);
            transportClient.update(updateRequest).get();
        } catch (InterruptedException e) {
            logger.error("更新数据异常", e);
        } catch (ExecutionException e) {
            logger.error("更新数据异常", e);
        }
    }

    @Override
    public void updateSet(String index, String type, String id, Map<String, Object> dataMap) {
        try {
            IndexRequest indexRequest = new IndexRequest(index, type, id);
            indexRequest.source(dataMap);
            UpdateRequest updateRequest = new UpdateRequest(index, type, id).upsert(indexRequest);
            updateRequest.doc(dataMap);
            transportClient.update(updateRequest).get();
        } catch (InterruptedException e) {
            logger.error("更新数据异常", e);
        } catch (ExecutionException e) {
            logger.error("更新数据异常", e);
        }
    }

    @Override
    public void updateList(String index, String type, String id,
                           Map<String, Object> dataMap, String listName, String mainKey) {
        try {
            Object mainValue = dataMap.get(mainKey);
            if (null == mainValue) {
                logger.error("mainkey错误");
                return;
            }
            Map<String, Object> params = new HashMap<>();
            params.put("message", dataMap);
            params.put("field", listName);
            IndexRequest indexRequest = new IndexRequest(index, type, id).source(XContentFactory.jsonBuilder()
                    .startObject().array(listName, dataMap).endObject());
            UpdateRequest updateRequest = new UpdateRequest(index, type, id).upsert(indexRequest);
            updateRequest.script(new Script(ScriptType.INLINE,
                    Script.DEFAULT_SCRIPT_LANG,
                    "if(ctx._source.containsKey(params.field))" +
                            "{ctx._source." + listName + ".removeIf(item -> item." + mainKey + " == '" + mainValue + "');"
                            + "ctx._source." + listName + ".add(params.message)}" +
                            "else{ctx._source." + listName + "=[params.message]}",
                    params));
            transportClient.update(updateRequest).get();
        } catch (InterruptedException e) {
            logger.error("更新数据异常", e);
        } catch (ExecutionException e) {
            logger.error("更新数据异常", e);
        } catch (IOException e) {
            logger.error("更新数据异常", e);
        }
    }

    @Override
    public void insertList(String index, String type, String id,
                           Map<String, Object> dataMap, String listName) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("message", dataMap);
            params.put("field", listName);
            IndexRequest indexRequest = new IndexRequest(index, type, id).source(XContentFactory.jsonBuilder()
                    .startObject().array(listName, dataMap).endObject());
            UpdateRequest updateRequest = new UpdateRequest(index, type, id).upsert(indexRequest);
            updateRequest.script(new Script(ScriptType.INLINE,
                    Script.DEFAULT_SCRIPT_LANG,
                    "if(ctx._source.containsKey(params.field))" +
                            "{ctx._source." + listName + ".add(params.message)}" +
                            "else{ctx._source." + listName + "=[params.message]}",
                    params));
            transportClient.update(updateRequest).get();
        } catch (InterruptedException e) {
            logger.error("更新数据异常", e);
        } catch (ExecutionException e) {
            logger.error("更新数据异常", e);
        } catch (IOException e) {
            logger.error("更新数据异常", e);
        }
    }

    @Override
    public void deleteList(String index, String type, String id,
                           Map<String, Object> dataMap, String listName, String mainKey) {
        try {
            Object mainObj = dataMap.get(mainKey);
            String mainValue;
            if (null == mainObj || StringUtils.isEmpty(mainValue = mainObj.toString())) {
                logger.error("mainkey错误");
                return;
            }
            UpdateRequest updateRequest = new UpdateRequest(index, type, id);
            updateRequest.script(new Script(ScriptType.INLINE,
                    Script.DEFAULT_SCRIPT_LANG,
                    "ctx._source." + listName + ".removeIf(item -> item." + mainKey + " == " + mainValue + ");",
                    Collections.emptyMap()));
            transportClient.update(updateRequest).get();
        } catch (InterruptedException e) {
            logger.error("更新数据异常", e);
        } catch (ExecutionException e) {
            logger.error("更新数据异常", e);
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
