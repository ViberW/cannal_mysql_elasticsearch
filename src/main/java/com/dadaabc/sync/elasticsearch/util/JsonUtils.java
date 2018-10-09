package com.dadaabc.sync.elasticsearch.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class JsonUtils {
    static{
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    }

    /**
     * 对象转换为json
     * @param obj
     * @return
     */
    public static String toJson(Object obj){
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat);
    }

    /**
     * json转换为对象
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz){
        return JSON.parseObject(json, clazz);
    }

    /**
     * json转换为泛型
     * @param json
     * @param type
     * @return
     */
    public static <T> T fromJson(String json, TypeReference<T> type){
        return JSON.parseObject(json, type);
    }



}
