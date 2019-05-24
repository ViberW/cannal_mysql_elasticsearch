package com.veelur.sync.component.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author: veelur
 * @date: 18-11-1
 * @Description: {相关描述}
 */
@Component
public class ParamsConfig implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @Value(value = "classpath:mybatis/common/*.xml")
    private Resource[] mapperLocations;

    @Value(value = "classpath:mybatis/mybatis-config.xml")
    private Resource configLocation;

    public Resource[] getMapperLocations() {
        return mapperLocations;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    @Value("${thread.size.pool:10}")
    private Integer threadPoolSize;
    @Value("${thread.size.down-latch:10}")
    private Integer threadDownLatchSize;
    @Value("${elasticsearch.index.retry-conflit:3}")
    private Integer elasticRetryConflit;
    @Value("${elasticsearch.index.number-of-shards:3}")
    private Integer elasticIndexNumOfShards;
    @Value("${elasticsearch.index.number-of-replicas:2}")
    private Integer elasticIndexNumOfReplicas;
    @Value("${elasticsearch.index.bulk-action-size:2000}")
    private Integer bulkActionSize;
    @Value("${elasticsearch.index.convert-nested:true}")
    private Boolean convertNested;

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public Integer getThreadDownLatchSize() {
        return threadDownLatchSize;
    }

    public Integer getElasticRetryConflit() {
        return elasticRetryConflit;
    }

    public Integer getElasticIndexNumOfShards() {
        return elasticIndexNumOfShards;
    }

    public Integer getElasticIndexNumOfReplicas() {
        return elasticIndexNumOfReplicas;
    }

    public Integer getBulkActionSize() {
        return bulkActionSize;
    }

    public Boolean getConvertNested() {
        return convertNested;
    }
}
