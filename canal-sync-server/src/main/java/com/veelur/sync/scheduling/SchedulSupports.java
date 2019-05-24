package com.veelur.sync.scheduling;

import com.alibaba.otter.canal.client.CanalConnector;
import com.veelur.sync.common.constant.ModelEnum;
import com.veelur.sync.common.exception.InfoNotRightException;
import com.veelur.sync.common.exception.SystemExecErrorException;
import com.veelur.sync.common.model.canal.DataSourceModel;
import com.veelur.sync.common.util.CollectionUtils;
import com.veelur.sync.component.canal.CanalClient;
import com.veelur.sync.component.canal.ScheduleModel;
import com.veelur.sync.component.config.ParamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Admin
 * @date: 2019/1/24
 * @Description: {相关描述}
 */
@Component
public class SchedulSupports {

    private static final Logger logger = LoggerFactory.getLogger(SchedulSupports.class);

    @Autowired
    private CanalClient canalClient;

    @EventListener(ApplicationReadyEvent.class)
    public void run() throws InfoNotRightException {
        List<String> destinations = canalClient.getDestinations();
        if (CollectionUtils.isNotEmpty(destinations)) {
            DataSourceModel model;
            CanalConnector canalConnector;
            ModelEnum modelEnum;
            BaseCanalScheduling bean;
            for (String destination : destinations) {
                canalConnector = canalClient.getCanalElasticConnector(destination);
                model = canalClient.getModelByDestination(destination);
                if (null == model) {
                    throw new SystemExecErrorException("empty model");
                }
                modelEnum = ModelEnum.convert(model.getModel());
                if (null == modelEnum) {
                    throw new SystemExecErrorException("empty output");
                }
                if (ModelEnum.ELASTIC.equals(modelEnum)) {
                    bean = ParamsConfig.getBean(CanalElasticScheduling.class);
                } /*else if (ModelEnum.KAFKA.equals(modelEnum)) {
                    bean = ParamsConfig.getBean(CanalKafKaScheduling.class);
                }*/ else {
                    throw new SystemExecErrorException("error output");
                }
                ScheduleModel scheduleModel = new ScheduleModel(canalConnector, model.getBatchSize(), bean.getClass(),destination);
                ScheduleThread.start(bean, scheduleModel);
            }
        }
    }
}
