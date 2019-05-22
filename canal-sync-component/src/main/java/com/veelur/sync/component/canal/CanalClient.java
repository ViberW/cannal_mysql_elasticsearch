package com.veelur.sync.component.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.veelur.sync.common.constant.ModelEnum;
import com.veelur.sync.common.exception.InfoNotRightException;
import com.veelur.sync.common.model.canal.DataBaseModel;
import com.veelur.sync.common.model.canal.DataSourceModel;
import com.veelur.sync.common.util.CollectionUtils;
import com.veelur.sync.component.config.ParamsConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author <a href="mailto:wangchao.star@gmail.com">wangchao</a>
 * @version 1.0
 * @since 2017-08-25 17:26:00
 */
@Component
@ConfigurationProperties(prefix = "canal")
public class CanalClient implements InitializingBean,DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CanalClient.class);

    protected List<DataSourceModel> client;

    @Value("${canal.zookeeper.server}")
    private String canalZkServers;

    @Autowired
    private MapperCreateHandler mapperCreateHandler;
    @Autowired
    private ParamsConfig paramsConfig;

    private Map<String, CanalConnector> connectors;
    private Map<String, DataSourceModel> destinationMap;
    private List<String> destinations;
    //destination和datasource的关系映射
    private BiMap<String, String> destinationSources;

    public BiMap<String, String> getDestinationSources() {
        return destinationSources;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public List<DataSourceModel> getClient() {
        return client;
    }

    public void setClient(List<DataSourceModel> client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(client)) {
            destinationMap = new HashMap<>(client.size());
            destinations = new ArrayList<>(client.size());
            for (DataSourceModel model : client) {
                destinationMap.put(model.getDestination(), model);
                if (null != model.getOnOff() && model.getOnOff()) {
                    destinations.add(model.getDestination());
                }
            }
        }
        connectors = new HashMap<>(client.size() * 2);
        destinationSources = HashBiMap.create();
        initDaos();
    }

    private void initDaos() {
        if (CollectionUtils.isEmpty(destinationMap)) {
            return;
        }
        Set<DataBaseModel> dataBaseModels = new HashSet<>();
        for (String key : destinationMap.keySet()) {
            DataSourceModel model = destinationMap.get(key);
            if (null == model ||
                    !ModelEnum.ELASTIC.getCode().equals(model.getModel())) {
                continue;
            }
            DataBaseModel database = model.getDatabase();
            if (null == database || StringUtils.isBlank(database.getDatasource())) {
                continue;
            }
            dataBaseModels.add(database);
            destinationSources.put(key, database.getDatasource());
        }
        mapperCreateHandler.register(dataBaseModels, paramsConfig);
    }

    public DataSourceModel getModelByDestination(String destination) {
        if (StringUtils.isEmpty(destination)) {
            return null;
        }
        return destinationMap.get(destination);
    }

    public CanalConnector getCanalElasticConnector(String destination) throws InfoNotRightException {
        CanalConnector connector = connectors.get(destination);
        if (null == connector) {
            synchronized (CanalClient.class) {
                if (null == connectors.get(destination)) {
                    if (!destinationMap.containsKey(destination)) {
                        throw new InfoNotRightException("error destination:" + destination);
                    }
                    connector = getCanalConnector(destinationMap.get(destination));
                    connectors.put(destination, connector);
                }
            }
        }
        return connector;
    }

    private CanalConnector getCanalConnector(DataSourceModel model) throws InfoNotRightException {
        CanalConnector canalConnector;
        String destination = model.getDestination();
        if (StringUtils.isNotBlank(model.getHost()) && StringUtils.isNotBlank(model.getPort())) {
            logger.info(destination + "_canal客户端准备连接canal...");
            canalConnector = CanalConnectors.newClusterConnector(
                    Lists.newArrayList(new InetSocketAddress(model.getHost(), Integer.valueOf(model.getPort()))),
                    destination, model.getUsername(), model.getPassword());
        } else if (StringUtils.isNotBlank(canalZkServers)) {
            logger.info(destination + "_canal客户端准备连接zookeeper...");
            canalConnector = CanalConnectors.newClusterConnector(canalZkServers, destination, model.getUsername(), model.getPassword());
            ((ClusterCanalConnector) canalConnector).setRetryTimes(60);//60次乘以5秒 5分钟
        } else {
            throw new InfoNotRightException(destination + "_canal连接信息不符合");
        }
        canalConnector.connect();
        logger.info(destination + "_canal客户端启动成功");
        return canalConnector;
    }

    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String, CanalConnector> entry : connectors.entrySet()) {
            logger.info(entry.getKey() + "_canal客户端断开连接...");
            if (null != entry.getValue()) {
                entry.getValue().disconnect();
            }
        }
    }

}
