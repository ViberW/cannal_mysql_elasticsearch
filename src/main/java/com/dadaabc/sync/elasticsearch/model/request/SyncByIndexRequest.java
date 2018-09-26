package com.dadaabc.sync.elasticsearch.model.request;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class SyncByIndexRequest {

    private String index;

    private String type;

    private String orderSign;

    private String start;

    private String end;

    private Integer limit;

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
}
