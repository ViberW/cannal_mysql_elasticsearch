package com.veelur.sync.component.canal;

import com.alibaba.otter.canal.client.CanalConnector;

/**
 * @author: Admin
 * @date: 2019/1/25
 * @Description: {相关描述}
 */
public class ScheduleModel {

    private CanalConnector canalConnector;
    private Integer canalBatchSize;
    private Boolean zkPathNode = false;
    private Integer errorCount = 0;
    private Boolean init = false;
    private Class clazz;
    private String destination;

    public ScheduleModel() {
    }

    public ScheduleModel(CanalConnector canalConnector, Integer canalBatchSize, Class clazz, String destination) {
        this.canalConnector = canalConnector;
        this.canalBatchSize = canalBatchSize;
        this.clazz = clazz;
        this.destination = destination;
    }

    public CanalConnector getCanalConnector() {
        return canalConnector;
    }

    public void setCanalConnector(CanalConnector canalConnector) {
        this.canalConnector = canalConnector;
    }

    public Integer getCanalBatchSize() {
        return canalBatchSize;
    }

    public void setCanalBatchSize(Integer canalBatchSize) {
        this.canalBatchSize = canalBatchSize;
    }

    public Boolean getZkPathNode() {
        return zkPathNode;
    }

    public void setZkPathNode(Boolean zkPathNode) {
        this.zkPathNode = zkPathNode;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Boolean getInit() {
        return init;
    }

    public void setInit(Boolean init) {
        this.init = init;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
