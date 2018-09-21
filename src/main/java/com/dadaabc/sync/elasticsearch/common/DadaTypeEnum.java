package com.dadaabc.sync.elasticsearch.common;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public enum DadaTypeEnum {

    DATABASE(1, "数据库"), ELASTIC(2, "elasticsearch");

    private Integer code;

    private String desc;

    DadaTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
