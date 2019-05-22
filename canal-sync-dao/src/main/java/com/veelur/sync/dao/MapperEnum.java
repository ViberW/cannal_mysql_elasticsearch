package com.veelur.sync.dao;

/**
 * @author: Admin
 * @date: 2019/1/25
 * @Description: {相关描述}
 */
public enum MapperEnum {

    ABC("dadaabc"), DWS("dws");

    private String code;

    MapperEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static MapperEnum convert(String code) {
        if (code == null)
            return null;
        for (MapperEnum mapperEnum : values()) {
            if (mapperEnum.getCode().equals(code)) {
                return mapperEnum;
            }
        }
        return null;
    }

    public static boolean contain(String code) {
        return null != convert(code);
    }

}
