package com.veelur.sync.common.model.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: veelur
 * @date: 18-11-21
 * @Description: {相关描述}
 */
@Component
public class BaseCache {

    private volatile Boolean elasticRun = true;

    private volatile Integer count = 0;

    private List<String> allIndexs = new ArrayList<>();

    public Boolean getElasticRun() {
        return elasticRun;
    }

    public Integer getCount() {
        return count;
    }

    public synchronized void tryElasticRun(Boolean elasticRun) {
        elasticRun = null == elasticRun ? false : elasticRun;
        this.elasticRun = elasticRun && count == 0;
    }

    public synchronized boolean startAllSync(String index) {
        if (StringUtils.isEmpty(index)) {
            return false;
        }
        index = index.trim();
        if (!allIndexs.contains(index)) {
            return false;
        }
        allIndexs.add(index);
        return true;
    }

    public synchronized void beginAllSync() {
        count += 1;
        elasticRun = false;
    }

    public synchronized void releaseAllSync(String index) {
        if (!elasticRun) {
            count -= 1;
            if (count <= 0) {
                elasticRun = true;
                count = 0;
                allIndexs.clear();
            }
        }
        if (StringUtils.isNotEmpty(index)) {
            index = index.trim();
            allIndexs.remove(index);
        }
    }
}
