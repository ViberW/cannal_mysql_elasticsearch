package com.veelur.sync.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * @author: veelur
 * @date: 18-10-22
 * @Description: {相关描述}
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> collection) {
        return !isEmpty(collection);
    }
}
