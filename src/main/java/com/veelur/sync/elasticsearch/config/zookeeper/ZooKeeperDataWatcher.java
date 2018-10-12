package com.veelur.sync.elasticsearch.config.zookeeper;

import com.veelur.sync.elasticsearch.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author: veelur
 * @date: 18-10-12
 * @Description: {相关描述}
 */
public class ZooKeeperDataWatcher implements Watcher {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperDataWatcher.class);

    protected CountDownLatch countDownLatch = new CountDownLatch(1);
    /**
     * zookeeper订阅状态
     * <p>
     * 注册节点
     * <p>
     * 本机ip
     * <p>
     * 服务端口
     * <p>
     * 是否当前应用执行订阅
     * <p>
     * 连接zookeeper
     *
     * @param hostPort
     * @param sessionTime
     * @throws IOException
     * @throws InterruptedException
     * <p>
     * 抢占本机执行
     * @return 获取是否本机运行数据订阅任务
     * @return
     */

    protected volatile boolean syncStatus = false;

    /**
     * 注册节点
     */

    protected String path;

    /**
     * 本机ip
     */

    protected String serverIp;

    /**
     * 服务端口
     */

    protected Integer serverPort;

    protected ZooKeeper zooKeeper;

    /**
     * 是否当前应用执行订阅
     */

    protected volatile Boolean isThisRun;


    public ZooKeeperDataWatcher(String hostPort, int sessionTime, String path, String serverIp, Integer serverPort)
            throws IOException, InterruptedException {
        this.path = path;
        this.isThisRun = false;
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        this.connect(hostPort, sessionTime);
    }

    /**
     * 连接zookeeper
     *
     * @param hostPort
     * @param sessionTime
     * @throws IOException
     * @throws InterruptedException
     */

    protected void connect(String hostPort, int sessionTime) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(hostPort, sessionTime, this);
        countDownLatch.await();
    }


    @Override
    public void process(WatchedEvent event) {
        // 判断是否连接成功
        if (!this.syncStatus && event.getState() == Event.KeeperState.SyncConnected) {
            // 连接初始化
            logger.info("zookeeper connect ok.");

            //初始化节点
            this.createNode();

            try {
                // 判断节点是否存在
                Stat stat = this.zooKeeper.exists(this.path, this);
                if (null == stat) {
                    this.registerThisRun();
                }
            } catch (KeeperException e) {
                logger.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

            countDownLatch.countDown();
            syncStatus = true;
        }

        if (event.getType() == Event.EventType.NodeDeleted) {
            if (this.path.equals(event.getPath())) {
                // 如果监听节点消失，则抢占执行
                this.registerThisRun();
            }
        }
    }


    protected Boolean createNode() {

        String[] paths = this.path.split("/");

        String pathNode = "";

        int index = -1;
        for (String p : paths) {
            index++;
            if (StringUtils.isEmpty(p)) {
                continue;
            }
            if (index >= (paths.length - 1)) {
                break;
            }
            pathNode += ("/" + p);

            try {
                // 判断节点是否存在
                Stat stat = this.zooKeeper.exists(pathNode, null);
                if (null == stat) {
                    this.zooKeeper.create(pathNode, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                logger.error(e.getMessage(), e);
                return false;
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }


    /**
     * 抢占本机执行
     *
     * @return
     */
    protected Boolean registerThisRun() {
        // 不存在的话，本机注册节点，并抢占本机运行数据订阅
        try {
            String value = DateUtils.getCurrentTime() + "_" + serverIp + "_" + serverPort;

            String str = this.zooKeeper.create(
                    this.path, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("register this run. value:" + value);
            this.isThisRun = true;
        } catch (KeeperException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return this.isThisRun;
    }

    /**
     * 获取是否本机运行数据订阅任务
     *
     * @return
     */

    public Boolean getIsThisRun() {
        return this.isThisRun;
    }


    public void setData(String path, String data) {
        try {
            this.zooKeeper.setData(path, data.getBytes(), this.zooKeeper.exists(path, this).getVersion());
        } catch (KeeperException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public String getData(String path) {
        try {
            return new String(this.zooKeeper.getData(path, this, null));
        } catch (KeeperException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
