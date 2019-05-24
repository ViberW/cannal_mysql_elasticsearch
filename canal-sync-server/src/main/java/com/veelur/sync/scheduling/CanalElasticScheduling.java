package com.veelur.sync.scheduling;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.veelur.sync.parse.service.impl.ElasticProcessor;
import com.veelur.sync.canal.event.VerDeleteCanalEvent;
import com.veelur.sync.canal.event.VerInsertCanalEvent;
import com.veelur.sync.canal.event.VerUpdateCanalEvent;
import com.veelur.sync.common.exception.ElasticErrorException;
import com.veelur.sync.component.canal.ScheduleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class CanalElasticScheduling extends BaseCanalScheduling implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CanalElasticScheduling.class);
    private ApplicationContext applicationContext;

    @Override
    protected boolean isRun() {
        return false;
    }

    @Override
    public void dealCanalLogs(Message message, ScheduleModel model) {
        ElasticProcessor.restart();
        message.getEntries().forEach(entry -> {
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                CanalEntry.EventType eventType = entry.getHeader().getEventType();
                //1-insert 2-update 3 -delete 更多参考CanalEntry.EventType的枚举定义
                logger.info("操作方式:" + eventType.getNumber());
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
        });
        //执行处理bulk中的请求
        ElasticProcessor.flush();
        if (ElasticProcessor.getError()) {
            //说明es本身问题
            throw new ElasticErrorException("[elastic]本身存在问题");
        }
        if (!ElasticProcessor.getSuccess()) {
            logger.error("[elastic]部分信息操作失败");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
