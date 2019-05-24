package com.veelur.sync.worker;

import com.veelur.sync.common.util.IpUtils;
import com.veelur.sync.component.zookeeper.ZooKeeperDataWatcher;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: veelur
 * @date: 18-10-12
 * @Description: {相关描述}
 */
@Component
public class BasicWorker {
    private static final Logger logger = LoggerFactory.getLogger(BasicWorker.class);

    private static ZooKeeperDataWatcher zooKeeperDataWatcher;

    @Value("${canal.zookeeper.server:localhost:2181}")
    private String zookeeperServer;

    @Value("${canal.zookeeper.sessionTime:3000}")
    private Integer zookeeperSessionTime;

    @Value("${server.port:80}")
    private int serverPort;
    // 获取对应的zookeeper的路径: /com/veelur/sync/elasticsearch/worker/running
    @Value("${canal.zookeeper.path}")
    private String zookeeperPath;

    public boolean checkZookeeper() {
        if (StringUtils.isBlank(zookeeperPath)) {
            zookeeperPath = "/" + getClass().getPackage().getName().replace(".", "/") + "/running";
        }
        if (null == zooKeeperDataWatcher) {
            synchronized (ZooKeeperDataWatcher.class) {
                if (null == zooKeeperDataWatcher) {
                    try {
                        zooKeeperDataWatcher = new ZooKeeperDataWatcher(zookeeperServer,
                                zookeeperSessionTime, zookeeperPath,
                                IpUtils.getServerIpAddress(), this.serverPort);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                        return false;
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                        return false;
                    }
                }
            }
        }
        // 判断是否需要本机运行
        if (!zooKeeperDataWatcher.getIsThisRun()) {
            String info = zooKeeperDataWatcher.getData(zookeeperPath);
            logger.info("其他服务器正在运行: " + info);
            return false;
        }
        return true;
    }
}
