package com.dadaabc.sync.parse.service.impl;

import com.dadaabc.sync.parse.service.VerElasticsearchService;
import com.veelur.sync.common.constant.BaseConstants;
import com.veelur.sync.common.exception.ElasticErrorException;
import com.veelur.sync.common.util.JsonUtils;
import com.veelur.sync.component.config.ParamsConfig;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.cluster.storedscripts.GetStoredScriptResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.bytes.BytesArray;
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
    @Autowired
    protected ParamsConfig paramsConfig;

    /****************************************dada自定义方法****************************************/
    @Override
    public void checkAndSetIndex(String index, String type) {
        if (!paramsConfig.getConvertNested()) {
            return;
        }
        IndicesAdminClient adminClient = transportClient.admin().indices();
        IndicesExistsRequest request = new IndicesExistsRequest(index);
        IndicesExistsResponse response = adminClient.exists(request).actionGet();
        if (response.isExists()) {
            return;
        }
        adminClient.prepareCreate(index).setSettings(Settings.builder().put("index.number_of_shards",
                paramsConfig.getElasticIndexNumOfShards())
                .put("index.number_of_replicas", paramsConfig.getElasticIndexNumOfReplicas()))
                .addMapping(type,
                        "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\": \"object\"," +
                                "\"mapping\":{\"type\":\"nested\"}}}]}", XContentType.JSON).get();
    }

    @Override
    public void checkAndSetStoredScript() {
        ClusterAdminClient cluster = transportClient.admin().cluster();
        checkStoredScript(cluster, BaseConstants.SCRIPT_INSET_LIST, "{\"script\":{\"lang\":\"painless\"," +
                "\"source\":\"if(ctx._source.containsKey(params.field))" +
                "{Map it= ctx._source.get(params.field).find(item -> item.get(params.key) == params.value);"
                + "if(it == null || it.isEmpty()){ctx._source.get(params.field).add(params.message)}}" +
                "else{ctx._source.put(params.field,[params.message])}\"}}");
        checkStoredScript(cluster, BaseConstants.SCRIPT_UPDATE_LIST, "{\"script\":{\"lang\":\"painless\"," +
                "\"source\":\"if(ctx._source.containsKey(params.field))" +
                "{Map it= ctx._source.get(params.field).find(item -> item.get(params.key) == params.value);"
                + "if(it != null && !it.isEmpty()){it.putAll(params.updates)}" +
                "else{ctx._source.get(params.field).add(params.message)}}" +
                "else{ctx._source.put(params.field,[params.message])}\"}}");
        checkStoredScript(cluster, BaseConstants.SCRIPT_DELETE_LIST, "{\"script\":{\"lang\":\"painless\"," +
                "\"source\":\"if(ctx._source.containsKey(params.field))" +
                "{ctx._source.get(params.field).removeIf(item -> item.get(params.key) == params.value)}\"}}");
    }

    private void checkStoredScript(ClusterAdminClient cluster, String name, String bytes) {
        GetStoredScriptResponse responseStore = cluster.prepareGetStoredScript(name).get();
        if (null == responseStore || null == responseStore.getSource() || StringUtils.isBlank(responseStore.getSource().getSource())) {
            cluster.preparePutStoredScript().setId(name)
                    .setContent(new BytesArray(bytes), XContentType.JSON).get();
        } else {
            String source = responseStore.getSource().getSource();
            if (!bytes.trim().equals(source.trim())) {
                logger.warn(name + "==============>当前保存的stored_script与当前不一致");
                //throw new ElasticErrorException("stored—script信息不匹配,请核实校验：" + name);
                cluster.prepareDeleteStoredScript(name).get();
                cluster.preparePutStoredScript().setId(name)
                        .setContent(new BytesArray(bytes), XContentType.JSON).get();
            }
        }
    }

    @Override
    public void updateSet(String index, String type, String id, Map<String, Object> dataMap) {
        UpdateRequest updateRequest = new UpdateRequest(index, type, id)
                .retryOnConflict(paramsConfig.getElasticRetryConflit()).doc(dataMap).upsert(dataMap);
        ElasticProcessor.PROCESSOR_THREAD_LOCAL.get().add(updateRequest);
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
            UpdateRequest updateRequest = new UpdateRequest(index, type, id)
                    .retryOnConflict(paramsConfig.getElasticRetryConflit())
                    .upsert(XContentFactory.jsonBuilder()
                            .startObject().array(listName, dataMap).endObject());
            updateRequest.script(new Script(ScriptType.STORED,
                    null,
                    BaseConstants.SCRIPT_UPDATE_LIST,
                    params));
            ElasticProcessor.PROCESSOR_THREAD_LOCAL.get().add(updateRequest);
        } catch (IOException e) {
            logger.error("更新数据异常", e);
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
            UpdateRequest updateRequest = new UpdateRequest(index, type, id)
                    .retryOnConflict(paramsConfig.getElasticRetryConflit())
                    .upsert(XContentFactory.jsonBuilder()
                            .startObject().array(listName, dataMap).endObject());
            updateRequest.script(new Script(ScriptType.STORED,
                    null,
                    BaseConstants.SCRIPT_INSET_LIST,
                    params));
            ElasticProcessor.PROCESSOR_THREAD_LOCAL.get().add(updateRequest);
        } catch (IOException e) {
            logger.error("更新数据异常", e);
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
        params.put("field", listName);
        params.put("value", mainValue);
        params.put("key", mainKey);
        UpdateRequest updateRequest = new UpdateRequest(index, type, id)
                .retryOnConflict(paramsConfig.getElasticRetryConflit())
                .script(new Script(
                        ScriptType.STORED,
                        null,
                        BaseConstants.SCRIPT_DELETE_LIST,
                        params));
        ElasticProcessor.PROCESSOR_THREAD_LOCAL.get().add(updateRequest);
    }

    /****************************************简单的根据id进行操作****************************************/
    @Override
    public void batchInsertById(String index, String type, Map<String, Map<String, Object>> idDataMap) {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        idDataMap.forEach((id, dataMap) -> bulkRequestBuilder.add(new UpdateRequest(index, type, id)
                .retryOnConflict(paramsConfig.getElasticRetryConflit()).doc(dataMap).upsert(dataMap)));
        try {
            BulkResponse bulkResponse = bulkRequestBuilder.execute().get();
            if (bulkResponse.hasFailures()) {
                logger.error("elasticsearch批量插入错误, index=" + index + ", type=" + type + ", data=" + JsonUtils.toJson(idDataMap) + ", cause:" + bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            logger.error("elasticsearch批量插入错误, index=" + index + ", type=" + type + ", data=" + JsonUtils.toJson(idDataMap), e);
        }
    }

    @Override
    public void deleteById(String index, String type, String id) {
        ElasticProcessor.PROCESSOR_THREAD_LOCAL.get().add(new DeleteRequest(index, type, id));
    }

    @Override
    public void deleteByQuerySet(String index, String type, String id, Map<String, Object> dataMap) {
        ElasticProcessor.PROCESSOR_THREAD_LOCAL.get().add(new UpdateRequest(index, type, id).doc(dataMap));
    }
}
