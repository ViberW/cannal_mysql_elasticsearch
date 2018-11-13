package com.veelur.sync.elasticsearch.model;

import java.util.UUID;

/**
 * @author: veelur
 * @date: 18-11-13
 * @Description: {相关描述}
 */
public class ElasticResultEntity {
    private Boolean flag;

    private String id;

    private String message;

    private Long executionId;

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public void restart() {
        this.flag = true;
        this.id = UUID.randomUUID().toString();
        this.message = "OK";
    }
}
