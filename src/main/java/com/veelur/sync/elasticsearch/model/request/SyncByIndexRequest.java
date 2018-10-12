package com.veelur.sync.elasticsearch.model.request;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class SyncByIndexRequest {

    @NotBlank(message = "index不能为空")
    private String index;
    @NotBlank(message = "type不能为空")
    private String type;
    @NotBlank(message = "orderSign不能为空")
    private String orderSign;
    @NotBlank(message = "start不能为空")
    private String start;
    @NotBlank(message = "end不能为空")
    private String end;

    private Integer limit = 200;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrderSign() {
        return orderSign;
    }

    public void setOrderSign(String orderSign) {
        this.orderSign = orderSign;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "SyncByIndexRequest{" +
                "index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", orderSign='" + orderSign + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", limit=" + limit +
                '}';
    }
}
