package com.veelur.sync.elasticsearch.model;

/**
 * @author: veelur
 * @date: 18-10-17
 * @Description: {相关描述}
 */
public class ThreadExecModel {

    private Object pkValue;

    private String index;
    private String type;
    private String orderSign;
    private Object start;
    private Object end;
    private Integer limit = 200;

    public ThreadExecModel() {
    }

    public ThreadExecModel(Object pkValue, String index, String type, String orderSign, Object start, Object end, Integer limit) {
        this.pkValue = pkValue;
        this.index = index;
        this.type = type;
        this.orderSign = orderSign;
        this.start = start;
        this.end = end;
        this.limit = limit;
    }

    public Object getPkValue() {
        return pkValue;
    }

    public void setPkValue(Object pkValue) {
        this.pkValue = pkValue;
    }

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

    public Object getStart() {
        return start;
    }

    public void setStart(Object start) {
        this.start = start;
    }

    public Object getEnd() {
        return end;
    }

    public void setEnd(Object end) {
        this.end = end;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
