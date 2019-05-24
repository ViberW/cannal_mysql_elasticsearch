package com.veelur.sync.scheduling;

import com.veelur.sync.parse.service.impl.ElasticProcessor;
import com.veelur.sync.common.exception.SystemExecErrorException;
import com.veelur.sync.common.util.ThreadUtil;
import com.veelur.sync.component.canal.ScheduleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Admin
 * @date: 2019/1/24
 * @Description: {相关描述}
 */
public class ScheduleThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleThread.class);

    private BaseCanalScheduling scheduling;
    private ScheduleModel model;

    public ScheduleThread(BaseCanalScheduling scheduling, ScheduleModel model) {
        this.scheduling = scheduling;
        this.model = model;
    }

    @Override
    public void run() {
        if (null != model.getClazz() && model.getClazz().equals(CanalElasticScheduling.class)) {
            ElasticProcessor.build();
        }
        while (true) {
            try {
                scheduling.run(model);
            } catch (Exception e) {
                logger.error(">>>ScheduleThread error", e);
                ThreadUtil.sleep(4000);
            }
            ThreadUtil.sleep(100);
        }
    }

    public static void start(BaseCanalScheduling scheduling, ScheduleModel model) {
        if (null == scheduling) {
            throw new SystemExecErrorException("null scheduling");
        }
        ScheduleThread scheduleThread = new ScheduleThread(scheduling, model);
        new Thread(scheduleThread).start();
    }
}
