package com.veelur.sync.scheduling;

import com.alibaba.otter.canal.protocol.Message;
import com.veelur.sync.worker.BasicWorker;
import com.veelur.sync.common.util.ThreadUtil;
import com.veelur.sync.component.canal.ScheduleModel;
import com.veelur.sync.component.config.ParamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author: veelur
 * @date: 18-10-31
 * @Description: {相关描述}
 */
public abstract class BaseCanalScheduling {

    @Autowired
    private BasicWorker basicWorker;

    @Autowired
    private ParamsConfig paramsConfig;

    private static final Logger logger = LoggerFactory.getLogger(BaseCanalScheduling.class);

    private String currentClassName = this.getClass().getSimpleName();

    public void run(ScheduleModel model) {
        if (!checkNeedExec(model)) return;
        if (!isRun()) {
            //若是暂停，则休眠10秒钟
            ThreadUtil.sleep(10000);
            return;
        }
        try {
            Message message = model.getCanalConnector().getWithoutAck(model.getCanalBatchSize());
            long batchId = message.getId();
            try {
                int size = message.getEntries().size();
                if (batchId != -1 && size != 0) {
                    long begin = System.currentTimeMillis();
                    dealCanalLogs(message, model);
                    logger.info(currentClassName + ":当次获取binlog条数,size:" + size + ",druid:" + (System.currentTimeMillis() - begin));
                }
                model.getCanalConnector().ack(batchId);
                model.setErrorCount(0);
            } catch (Exception e) {
                logger.error(currentClassName + ":发送监听事件失败！batchId回滚,batchId=" + batchId, e);
                model.getCanalConnector().rollback(batchId);
                model.setErrorCount(model.getErrorCount() + 1);
                /*if (model.getErrorCount() > paramsConfig.getThreadErrorCount()) {
                    //休眠时间在次数大于errorCount后,开始乘于2^n次方秒休眠,和1分钟之间的最小值
                    ThreadUtil.sleep(Math.min(60000, 1000 << (model.getErrorCount() - paramsConfig.getThreadErrorCount())));
                }*/
            }
        } catch (Exception e) {
            logger.error(currentClassName + ",canal_scheduled异常！", e);
        }
    }

    private boolean checkNeedExec(ScheduleModel model) {
        if (!model.getZkPathNode()) {
            if (!basicWorker.checkZookeeper()) {
                ThreadUtil.sleep(10000);//休眠10秒钟
                return false;
            }
            model.setZkPathNode(true);
            // 指定filter，格式 {database}.{table}，这里不做过滤，过滤操作留给用户
            model.getCanalConnector().subscribe();
            // 回滚寻找上次中断的位置
            model.getCanalConnector().rollback();
        }
        return true;
    }

    protected abstract boolean isRun();

    protected abstract void dealCanalLogs(Message message, ScheduleModel model);

}
