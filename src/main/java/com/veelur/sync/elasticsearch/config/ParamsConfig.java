package com.veelur.sync.elasticsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: veelur
 * @date: 18-11-1
 * @Description: {相关描述}
 */
@Component
public class ParamsConfig {

    @Value("${canal.batch-size:1000}")
    private Integer elasticBatchSize;
    @Value("${thread.size.pool:10}")
    private Integer threadPoolSize;
    @Value("${thread.size.down-latch:10}")
    private Integer threadDownLatchSize;
    @Value("${elasticsearch.retry.conflit:3}")
    private Integer elasticRetryConflit;
    @Value("${elasticsearch.index.number_of_shards:3}")
    private Integer elasticIndexNumOfShards;
    @Value("${elasticsearch.index.number_of_replicas:2}")
    private Integer elasticIndexNumOfReplicas;

    public Integer getElasticBatchSize() {
        return elasticBatchSize;
    }

    public void setElasticBatchSize(Integer elasticBatchSize) {
        this.elasticBatchSize = elasticBatchSize;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(Integer threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public Integer getThreadDownLatchSize() {
        return threadDownLatchSize;
    }

    public void setThreadDownLatchSize(Integer threadDownLatchSize) {
        this.threadDownLatchSize = threadDownLatchSize;
    }

    public Integer getElasticRetryConflit() {
        return elasticRetryConflit;
    }

    public void setElasticRetryConflit(Integer elasticRetryConflit) {
        this.elasticRetryConflit = elasticRetryConflit;
    }

    public Integer getElasticIndexNumOfShards() {
        return elasticIndexNumOfShards;
    }

    public void setElasticIndexNumOfShards(Integer elasticIndexNumOfShards) {
        this.elasticIndexNumOfShards = elasticIndexNumOfShards;
    }

    public Integer getElasticIndexNumOfReplicas() {
        return elasticIndexNumOfReplicas;
    }

    public void setElasticIndexNumOfReplicas(Integer elasticIndexNumOfReplicas) {
        this.elasticIndexNumOfReplicas = elasticIndexNumOfReplicas;
    }
}
