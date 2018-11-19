package com.veelur.sync.elasticsearch.scheduling;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.Message;
import com.veelur.sync.elasticsearch.config.ParamsConfig;
import com.veelur.sync.elasticsearch.event.VerDeleteCanalEvent;
import com.veelur.sync.elasticsearch.event.VerInsertCanalEvent;
import com.veelur.sync.elasticsearch.event.VerUpdateCanalEvent;
import com.veelur.sync.elasticsearch.exception.ElasticErrorException;
import com.veelur.sync.elasticsearch.model.ElasticResultEntity;
import com.veelur.sync.elasticsearch.service.VerElasticsearchService;
import com.veelur.sync.elasticsearch.util.ThreadUtil;
import com.veelur.sync.elasticsearch.worker.BasicWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author <a href="mailto:wangchao.star@gmail.com">wangchao</a>
 * @version 1.0
 * @since 2017-08-26 22:44:00
 */
@Component
@ConditionalOnExpression("${thread.schedul.active:true}")
public class CanalScheduling extends BasicWorker implements Runnable, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CanalScheduling.class);
    private ApplicationContext applicationContext;

    @Resource
    private CanalConnector canalConnector;

    @Autowired
    private ParamsConfig paramsConfig;

    @Autowired
    private VerElasticsearchService verElasticsearchService;

    private boolean zkPathNode = false;

    @Scheduled(fixedDelay = 100)
    @Override
    public void run() {
        if (!checkNeedExec()) return;
        try {
            Message message = canalConnector.getWithoutAck(paramsConfig.getElasticBatchSize());
            long batchId = message.getId();
            try {
                List<Entry> entries = message.getEntries();
                if (batchId != -1 && entries.size() > 0) {
                    verElasticsearchService.restart();
                    entries.forEach(entry -> {
                        if (entry.getEntryType() == EntryType.ROWDATA) {
                            publishCanalEvent(entry);
                        }
                    });
                    //执行处理bulk中的请求
                    verElasticsearchService.flush();
                    if (verElasticsearchService.getResultEntity().getError()) {
                        throw new ElasticErrorException(verElasticsearchService.getResultEntity().getThrowable());
                    }
                    if (!verElasticsearchService.getResultEntity().getSuccess()) {
                        ElasticResultEntity resultEntity = verElasticsearchService.getResultEntity();
                        throw new ElasticErrorException("_traceId:" + resultEntity.getId() + ",resultEntity:" + JSON.toJSONString(resultEntity));
                    }
                    logger.info("当前获取binlog信息条数,size:" + entries.size());
                }
                canalConnector.ack(batchId);
            } catch (Exception e) {
                logger.error("发送监听事件失败！batchId回滚,batchId=" + batchId, e);
                canalConnector.rollback(batchId);
            }
        } catch (Exception e) {
            logger.error("canal_scheduled异常！", e);
        }
    }

    private boolean checkNeedExec() {
        if (!zkPathNode) {
            if (!checkZookeeper()) {
                ThreadUtil.sleep(10000);//休眠10秒钟
                return false;
            }
            zkPathNode = true;
            // 指定filter，格式 {database}.{table}，这里不做过滤，过滤操作留给用户
            canalConnector.subscribe();
            // 回滚寻找上次中断的位置
            canalConnector.rollback();
        }
        return true;
    }

    private void publishCanalEvent(Entry entry) {
        EventType eventType = entry.getHeader().getEventType();
        switch (eventType) {
            case INSERT:
                applicationContext.publishEvent(new VerInsertCanalEvent(entry));
                break;
            case UPDATE:
                applicationContext.publishEvent(new VerUpdateCanalEvent(entry));
                break;
            case DELETE:
                applicationContext.publishEvent(new VerDeleteCanalEvent(entry));
                break;
            default:
                break;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
