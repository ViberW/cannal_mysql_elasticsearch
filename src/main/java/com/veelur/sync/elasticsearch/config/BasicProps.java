package com.veelur.sync.elasticsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: veelur
 * @date: 18-10-12
 * @Description: {相关描述}
 */
@Component
public class BasicProps {

    @Value("${zookeeper.server}")
    private String zookeeperServer;

    @Value("${zookeeper.sessionTime}")
    private Integer zookeeperSessionTime;

    @Value("${zookeeper.path}")
    private String zookeeperPath;

    public String getZookeeperServer() {
        return zookeeperServer;
    }

    public Integer getZookeeperSessionTime() {
        return zookeeperSessionTime;
    }

    public String getZookeeperPath() {
        return zookeeperPath;
    }
}
