package com.star.sync.elasticsearch.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.google.common.collect.Lists;
import com.veelur.sync.elasticsearch.exception.InfoNotRightException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:wangchao.star@gmail.com">wangchao</a>
 * @version 1.0
 * @since 2017-08-25 17:26:00
 */
@Component
public class CanalClient implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CanalClient.class);
    private CanalConnector canalConnector;

    @Value("${canal.host:}")
    private String canalHost;
    @Value("${canal.port:}")
    private String canalPort;
    @Value("${canal.destination}")
    private String canalDestination;
    @Value("${canal.username}")
    private String canalUsername;
    @Value("${canal.password}")
    private String canalPassword;
    @Value("${canal.zookeeper.server}")
    private String canalZkServers;

    @Bean
    public CanalConnector getCanalConnector() throws InfoNotRightException {
        if (StringUtils.isNotBlank(canalHost) && StringUtils.isNotBlank(canalPort)) {
            logger.info("canal客户端准备连接canal...");
            canalConnector = CanalConnectors.newClusterConnector(
                    Lists.newArrayList(new InetSocketAddress(canalHost, Integer.valueOf(canalPort))),
                    canalDestination, canalUsername, canalPassword);
        } else if (StringUtils.isNotBlank(canalZkServers)) {
            logger.info("canal客户端准备连接zookeeper...");
            canalConnector = CanalConnectors.newClusterConnector(canalZkServers, canalDestination, canalUsername, canalPassword);
        } else {
            throw new InfoNotRightException("canal连接信息不符合");
        }
        canalConnector.connect();
        // 指定filter，格式 {database}.{table}，这里不做过滤，过滤操作留给用户
        canalConnector.subscribe();
        // 回滚寻找上次中断的位置
        canalConnector.rollback();
        logger.info("canal客户端启动成功");
        return canalConnector;
    }

    @Override
    public void destroy() throws Exception {
        if (canalConnector != null) {
            canalConnector.disconnect();
        }
    }
}
