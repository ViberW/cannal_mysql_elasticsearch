package com.veelur.sync.elasticsearch.worker;

import com.veelur.sync.elasticsearch.config.BasicProps;
import com.veelur.sync.elasticsearch.config.zookeeper.ZooKeeperDataWatcher;
import com.veelur.sync.elasticsearch.util.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private BasicProps basicProps;

    @Value("${server.port:80}")
    private int serverPort;

    public boolean checkZookeeper() {

        if (null == zooKeeperDataWatcher) {
            synchronized (ZooKeeperDataWatcher.class) {
                if (null == zooKeeperDataWatcher) {
                    try {
                        zooKeeperDataWatcher = new ZooKeeperDataWatcher(basicProps.getZookeeperServer(),
                                basicProps.getZookeeperSessionTime(), basicProps.getZookeeperPath(),
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
            String info = zooKeeperDataWatcher.getData(basicProps.getZookeeperPath());
            logger.info("其他服务器正在运行: " + info);
            return false;
        }
        return true;
    }
}
