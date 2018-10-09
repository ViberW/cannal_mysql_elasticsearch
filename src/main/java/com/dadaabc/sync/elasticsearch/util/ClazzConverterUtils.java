package com.dadaabc.sync.elasticsearch.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.Collection;
import java.util.List;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class ClazzConverterUtils {

    public static <T1, T2> T1 converterClass(T2 srcClazz, Class<T1> dstClazz) {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(srcClazz);
        return jsonObject == null ? null : JSONObject.toJavaObject(jsonObject, dstClazz);
    }

    public static <T1, T2> Collection<T1> converterClassArray(T2 srcClazz, Class<T1> dstClazz) {
        JSONArray jsonArray = (JSONArray) JSONArray.toJSON(srcClazz);
        return jsonArray == null ? null : JSONArray.parseArray(jsonArray.toJSONString(), dstClazz);
    }


    public static <T1, T2> T1 converterClass(T2 srcClazz, TypeReference<T1> type) {
        String json = JsonUtils.toJson(srcClazz);
        return json == null ? null : JsonUtils.fromJson(json, type);
    }

    public static <T1, T2> Collection<T1> converterClass(Collection<T2> srcClazzCollection, Class<T1> dstClazz) {
        JSONArray jsonArray = (JSONArray) JSONObject.toJSON(srcClazzCollection);
        return jsonArray == null ? null : JSONArray.parseArray(jsonArray.toJSONString(), dstClazz);
    }

    public static <T1, T2> T1[] converterClass(T2[] srcClazzArray, Class<T1> dstClazz) {
        JSONArray jsonArray = (JSONArray) JSONObject.toJSON(srcClazzArray);
        if (jsonArray == null) {
            return null;
        } else {
            List<T1> result = JSONArray.parseArray(jsonArray.toJSONString(), dstClazz);
            return result == null ? null : (T1[]) result.toArray();
        }
    }
}

