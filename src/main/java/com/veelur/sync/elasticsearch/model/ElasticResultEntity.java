package com.veelur.sync.elasticsearch.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author: veelur
 * @date: 18-11-13
 * @Description: {相关描述}
 */
public class ElasticResultEntity {
    //有某些请求异常
    private Boolean success;
    //es本身问题异常
    private Boolean error;
    //es本身问题异常
    private Throwable throwable;
    //日志id
    private String id;
    //错误信息
    private String message;
    //执行id列表
    private List<Long> executionIds;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
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

    public List<Long> getExecutionIds() {
        if (null == this.executionIds) {
            executionIds = new ArrayList<>();
        }
        return executionIds;
    }

    public void setExecutionIds(List<Long> executionIds) {
        this.executionIds = executionIds;
    }

    public void restart() {
        this.success = true;
        this.error = false;
        this.id = UUID.randomUUID().toString();
        this.message = "OK";
    }
}
