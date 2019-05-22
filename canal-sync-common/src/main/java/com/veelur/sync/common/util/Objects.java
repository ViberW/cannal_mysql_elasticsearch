package com.veelur.sync.common.util;

import java.util.Arrays;

/**
 * @author: Admin
 * @date: 2019/5/10
 * @Description: {相关描述}
 */
public class Objects {

    public static boolean equal(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    public static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }
}
