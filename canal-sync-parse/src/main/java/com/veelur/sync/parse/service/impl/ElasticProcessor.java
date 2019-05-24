package com.veelur.sync.parse.service.impl;

import com.alibaba.fastjson.JSON;
import com.veelur.sync.common.model.ElasticResultEntity;
import com.veelur.sync.common.util.JsonUtils;
import com.veelur.sync.component.config.ParamsConfig;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: Admin
 * @date: 2019/1/25
 * @Description: {相关描述}
 */
public class ElasticProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ElasticProcessor.class);

    private static final Logger esSyncFailLogger = LoggerFactory.getLogger("ES-SYNC-FAIL");

    public static InheritableThreadLocal<ElasticResultEntity> ENTITY_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static InheritableThreadLocal<BulkProcessor> PROCESSOR_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void build() {
        ParamsConfig paramsConfig = ParamsConfig.getBean(ParamsConfig.class);
        ENTITY_THREAD_LOCAL.set(new ElasticResultEntity().restart());
        BulkProcessor bulkProcessor = BulkProcessor.builder(ParamsConfig.getBean(TransportClient.class), new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId,
                                   BulkRequest request) {
                ENTITY_THREAD_LOCAL.get().setExecutionId(executionId);
            }

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  BulkResponse response) {
                int errorCount = 0;
                if (response.hasFailures()) {
                    BulkItemResponse[] items = response.getItems();
                    List<DocWriteRequest> requests = request.requests();
                    BulkItemResponse bulkItemResponse;
                    for (int i = 0; i < items.length; i++) {
                        bulkItemResponse = items[i];
                        if (bulkItemResponse.isFailed()) {
                            BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                            if (failure.getCause() instanceof DocumentMissingException) {
                                continue;
                            }
                            if (failure.getCause() instanceof IndexNotFoundException) {
                                continue;
                            }
                            errorCount++;
                            logger.error("执行操作失败", failure.getCause());
                            //添加到日志记录
                            esSyncFailLogger.error("操作失败," +
                                    ",executionId：" + executionId + ",msg:" + JSON.toJSONString(requests.get(i)));
                        }
                    }
                    ElasticResultEntity resultEntity = ENTITY_THREAD_LOCAL.get();
                    resultEntity.setSuccess(resultEntity.getSuccess() && errorCount == 0);
                    resultEntity.setMessage(null != resultEntity.getMessage() ?
                            resultEntity.getMessage() + ":" + errorCount : "" + errorCount);
                    logger.info("批量操作结果:" + JsonUtils.toJson(resultEntity));
                }

            }

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  Throwable failure) {
                ElasticResultEntity resultEntity = ENTITY_THREAD_LOCAL.get();
                resultEntity.setSuccess(false);
                resultEntity.setError(true);
                logger.error("执行处理批量异常,executionId:" + executionId, failure);
                //添加日志记录
                for (DocWriteRequest req : request.requests()) {
                    esSyncFailLogger.error("elastic错误," +
                            ",executionId：" + executionId + ",msg:" + JSON.toJSONString(req));
                }
            }
        }).setBulkActions(paramsConfig.getBulkActionSize())
                .setConcurrentRequests(0)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(50), 3))
                .build();
        PROCESSOR_THREAD_LOCAL.set(bulkProcessor);
    }

    public static void destroy() throws Exception {
        PROCESSOR_THREAD_LOCAL.get().awaitClose(4000, TimeUnit.MILLISECONDS);
    }

    public static void restart() {
        ENTITY_THREAD_LOCAL.get().restart();
    }

    public static void flush() {
        PROCESSOR_THREAD_LOCAL.get().flush();
    }

    public static boolean getError() {
        return ENTITY_THREAD_LOCAL.get().getError();
    }

    public static boolean getSuccess() {
        return ENTITY_THREAD_LOCAL.get().getSuccess();
    }
}
