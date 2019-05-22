package com.veelur.sync.common.model;

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
    //错误信息
    private String message;
    //执行id列表
    private List<Long> executionIds = new ArrayList<>();

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


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Long> getExecutionIds() {
        return executionIds;
    }

    public void setExecutionIds(List<Long> executionIds) {
        this.executionIds = executionIds;
    }

    public void setExecutionId(Long executionId) {
        if (null == this.executionIds) {
            this.executionIds = new ArrayList<>();
        }
        //executionIds仅仅保留最小值和最大值
        if (this.executionIds.size() < 2) {
            this.executionIds.add(executionId);
        } else {
            this.executionIds.add(1, executionId);
        }
    }

    public ElasticResultEntity restart() {
        this.success = true;
        this.error = false;
        this.message = "OK";
        this.executionIds.clear();
        return this;
    }
}
