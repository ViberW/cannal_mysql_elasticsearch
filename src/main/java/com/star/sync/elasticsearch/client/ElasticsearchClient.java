package com.star.sync.elasticsearch.client;

import com.veelur.sync.elasticsearch.common.BaseConstants;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * @author <a href="mailto:wangchao.star@gmail.com">wangchao</a>
 * @version 1.0
 * @since 2017-08-25 17:32:00
 */
@Component
public class ElasticsearchClient implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchClient.class);
    private TransportClient transportClient;

    @Value("${elasticsearch.cluster.name}")
    private String clusterName;
    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private Integer port;

    @Bean
    public TransportClient getTransportClient() throws Exception {
        Settings settings = Settings.builder().put("client.transport.ignore_cluster_name", true)
                /*.put("client.transport.sniff", true)*/
                .build();
        String[] hosts = host.split(BaseConstants.DEFAULT_SPLIT);
        transportClient = new PreBuiltTransportClient(settings);
        for (String ho : hosts) {
            transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName(ho), port));
        }
        logger.info("elasticsearch transportClient 连接成功");
        return transportClient;
    }

    @Override
    public void destroy() throws Exception {
        if (transportClient != null) {
            transportClient.close();
        }
    }
}
