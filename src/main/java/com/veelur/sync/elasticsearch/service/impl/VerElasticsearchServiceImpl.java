package com.veelur.sync.elasticsearch.service.impl;

import com.star.sync.elasticsearch.util.JsonUtil;
import com.veelur.sync.elasticsearch.exception.ElasticErrorException;
import com.veelur.sync.elasticsearch.service.VerElasticsearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
@Service
public class VerElasticsearchServiceImpl implements VerElasticsearchService {

    private static final Logger logger = LoggerFactory.getLogger(VerElasticsearchServiceImpl.class);

    @Autowired
    private TransportClient transportClient;

    /****************************************dada自定义方法****************************************/
    @Override
    public void checkAndSetIndex(String index, String type, int numShards, int numReplicas, boolean convertNested) {
        IndicesAdminClient adminClient = transportClient.admin().indices();
        IndicesExistsRequest request = new IndicesExistsRequest(index);
        IndicesExistsResponse response = adminClient.exists(request).actionGet();
        if (response.isExists()) {
            return;
        }
        CreateIndexRequestBuilder builder = adminClient.prepareCreate(index)
                .setSettings(Settings.builder().put("index.number_of_shards", numShards)
                        .put("index.number_of_replicas", numReplicas));
        if (convertNested) {
            builder.addMapping(type,
                    "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\": \"object\"," +
                            "\"mapping\":{\"type\":\"nested\"}}}]}", XContentType.JSON);
        }
        builder.get();
    }

    @Override
    public void updateSet(String index, String type, String id, Map<String, Object> dataMap) {
        transportClient.prepareUpdate(index, type, id).setDoc(dataMap).setUpsert(dataMap);
    }

    @Override
    public void updateList(String index, String type, String id,
                           Map<String, Object> dataMap, Map<String, Object> updateMap, String listName, String mainKey) {

        Object mainValue = dataMap.get(mainKey);
        if (null == mainValue) {
            logger.error("mainkey错误");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("message", dataMap);
        params.put("field", listName);
        params.put("updates", updateMap);
        params.put("value", mainValue);
        params.put("key", mainKey);
        try {
            transportClient.prepareUpdate(index, type, id).setScript(new Script(ScriptType.INLINE,
                    Script.DEFAULT_SCRIPT_LANG,
                    "if(ctx._source.containsKey(params.field))" +
                            "{Map it= ctx._source.get(params.field).find(item -> item.get(params.key) == params.value);"
                            + "if(it != null && !it.isEmpty()){it.putAll(params.updates)}" +
                            "else{ctx._source.get(params.field).add(params.message)}}" +
                            "else{ctx._source.put(params.field,[params.message])}",
                    params)).setUpsert(XContentFactory.jsonBuilder()
                    .startObject().array(listName, dataMap).endObject()).get();
        } catch (IOException e) {
            logger.error("组建数据异常", e);
            throw new ElasticErrorException(e.getMessage());
        }
    }

    @Override
    public void insertList(String index, String type, String id,
                           Map<String, Object> dataMap, String listName, String mainKey) {
        Object mainValue = dataMap.get(mainKey);
        if (null == mainValue) {
            logger.error("mainkey错误");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("message", dataMap);
        params.put("field", listName);
        params.put("value", mainValue);
        params.put("key", mainKey);
        try {
            transportClient.prepareUpdate(index, type, id).setScript(new Script(ScriptType.INLINE,
                    Script.DEFAULT_SCRIPT_LANG,
                    "if(ctx._source.containsKey(params.field))" +
                            "{Map it= ctx._source.get(params.field).find(item -> item.get(params.key) == params.value);"
                            + "if(it == null || it.isEmpty()){ctx._source.get(params.field).add(params.message)}}" +
                            "else{ctx._source.put(params.field,[params.message])}",
                    params)).setUpsert(XContentFactory.jsonBuilder()
                    .startObject().array(listName, dataMap).endObject()).get();
        } catch (IOException e) {
            logger.error("组建数据异常", e);
            throw new ElasticErrorException(e.getMessage());
        }
    }

    @Override
    public void deleteList(String index, String type, String id,
                           Map<String, Object> dataMap, String listName, String mainKey) {
        Object mainObj = dataMap.get(mainKey);
        String mainValue;
        if (null == mainObj || StringUtils.isEmpty(mainValue = mainObj.toString())) {
            logger.error("mainkey错误");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("message", dataMap);
        params.put("value", mainValue);
        params.put("key", mainKey);
        params.put("field", listName);
        transportClient.prepareUpdate(index, type, id)
                .setScript(new Script(
                        ScriptType.INLINE,
                        Script.DEFAULT_SCRIPT_LANG,
                        "if(ctx._source !=null && !ctx._source.isEmpty() && ctx._source.containsKey(params.field))" +
                                "{ctx._source.get(params.field).removeIf(item -> item.get(params.key) == params.value)}",
                        params))
                .get();
    }

    @Override
    public void deleteByQuerySet(String index, String type, String id, Map<String, Object> dataMap) {
        Map<String, Object> params = new HashMap<>();
        params.put("message", dataMap);
        transportClient.prepareUpdate(index, type, id).setScript(new Script(
                ScriptType.INLINE,
                Script.DEFAULT_SCRIPT_LANG,
                "if(ctx._source !=null && !ctx._source.isEmpty())ctx._source.putAll(params.message)",
                params)).get();
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
