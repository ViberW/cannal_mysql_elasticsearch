package com.veelur.sync.common.constant;

/**
 * @author: Admin
 * @date: 2019/1/24
 * @Description: {相关描述}
 */
public enum ModelEnum {

    ELASTIC("elastic", "es输出"), KAFKA("kafka", "kafka输出");

    private String code;

    private String desc;

    ModelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static ModelEnum convert(String code) {
        if (code == null)
            return null;
        for (ModelEnum modelEnum : values()) {
            if (modelEnum.getCode().equals(code)) {
                return modelEnum;
            }
        }
        return null;

    }
}
