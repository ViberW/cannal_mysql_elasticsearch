package com.veelur.sync.elasticsearch.service;

import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public interface VerElasticsearchService {

    void checkAndSetIndex(String index, String type, int numShards, int numReplicas, boolean convertNested);

    void insertById(String index, String type, String id, Map<String, Object> dataMap);

    void batchInsertById(String index, String type, Map<String, Map<String, Object>> idDataMap);

    void deleteById(String index, String type, String id);

    void updateSet(String index, String type, String id, Map<String, Object> dataMap);

    void updateList(String index, String type, String id,
                    Map<String, Object> dataMap, Map<String, Object> updateMap, String listName, String mainKey);

    void deleteByQuerySet(String index, String type, String id, Map<String, Object> dataMap);

    void deleteList(String index, String type, String id,
                    Map<String, Object> dataMap, String listName, String mainKey);

    void insertList(String index, String type, String id,
                    Map<String, Object> dataMap, String listName, String mainKey);
}
