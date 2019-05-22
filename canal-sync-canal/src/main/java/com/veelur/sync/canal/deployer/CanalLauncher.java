package com.veelur.sync.canal.deployer;

import com.veelur.sync.canal.extend.InstanceMonitorModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * canal独立版本启动的入口类
 *
 * @author jianghang 2012-11-6 下午05:20:49
 * @version 1.0.0
 */
@Component
@Order(0)
public class CanalLauncher implements EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(CanalLauncher.class);
    public static volatile boolean running = false;

    public static Map<String, InstanceMonitorModel> CLIENTS = new HashMap<>();

    @Override
    public void setEnvironment(Environment env) {
        try {
            String s = parseEnvironment(env);
            System.setProperty(CanalConstants.CANAL_DESTINATIONS, s);
            String zk = env.getProperty("canal.zookeeper.server");
            String port = env.getProperty("server.port");
            running = true;
            logger.info("## set default uncaught exception handler");
            setGlobalUncaughtExceptionHandler();
            logger.info("## load canal configurations");
            Properties properties = new Properties();
            properties.load(CanalLauncher.class.getClassLoader().getResourceAsStream("canal.properties"));
            System.setProperty(CanalConstants.CANAL_ZKSERVERS, zk);
            System.setProperty(CanalConstants.CANAL_METRICS_PULL_PORT, port);
            //加载子文件
            final CanalStater canalStater = new CanalStater();
            canalStater.start(properties);
        } catch (Throwable e) {
            logger.error("## Something goes wrong when starting up the canal Server:", e);
        }
    }

    private static void setGlobalUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error("UnCaughtException", e);
            }
        });
    }

    private String parseEnvironment(Environment env) {
        Map<String, InstanceMonitorModel> clients = new HashMap<>();
        Set<String> destinations = new HashSet<>();
        for (int i = 0; ; i++) {
            String destination = env.getProperty("canal.client[" + i + "].destination");
            String onOff = env.getProperty("canal.client[" + i + "].onOff");
            if (StringUtils.isBlank(destination)) {
                logger.info("## parse yml to i[{}]", i);
                break;
            }
            if (!Boolean.TRUE.toString().equals(onOff)) {
                logger.info("## {}'s onOff is not true", destination);
                continue;
            }
            destinations.add(destination);
            List<String> strings = getSplitUrl(env, i);
            if (null == strings) {
                strings = Collections.emptyList();
            }
            InstanceMonitorModel model = new InstanceMonitorModel(
                    strings.get(0),
                    env.getProperty("canal.client[" + i + "].database.username"),
                    env.getProperty("canal.client[" + i + "].database.password"),
                    strings.get(1),
                    env.getProperty("canal.client[" + i + "].white-regex"));
            if (!model.check()) {
                logger.warn("client[{}] info not match", i);
                break;
            }
            clients.put(destination, model);
        }

        CLIENTS = clients;
        return destinations.isEmpty() ? StringUtils.EMPTY : StringUtils.join(destinations, ",");
    }

    private List<String> getSplitUrl(Environment env, int i) {
        String property = env.getProperty("canal.client[" + i + "].database.jdbc-url");
        if (StringUtils.isEmpty(property)) {
            return null;
        }
        String[] split = property.split("/");
        return Arrays.asList(split);
    }
}
