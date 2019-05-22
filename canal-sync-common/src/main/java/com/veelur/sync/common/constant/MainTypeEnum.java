package com.veelur.sync.common.constant;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public enum MainTypeEnum {

    MAIN(1, "自身"), ONE_TO_ONE(2, "1对1"), ONE_TO_MORE(3, "1对n");

    private Integer code;

    private String desc;

    MainTypeEnum(Integer code, String desc) {
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
