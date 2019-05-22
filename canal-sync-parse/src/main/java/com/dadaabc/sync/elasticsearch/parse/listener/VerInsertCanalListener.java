package com.dadaabc.sync.elasticsearch.parse.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.dadaabc.sync.elasticsearch.parse.service.VerElasticsearchService;
import com.veelur.sync.canal.event.VerInsertCanalEvent;
import com.veelur.sync.common.constant.MainTypeEnum;
import com.veelur.sync.common.model.VerDatabaseTableModel;
import com.veelur.sync.common.model.VerIndexTypeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Veelur
 * @version 1.0
 */
@Component
public class VerInsertCanalListener extends VerAbstractCanalListener<VerInsertCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(VerInsertCanalListener.class);

    @Autowired
    private VerElasticsearchService verElasticsearchService;

    @Override
    protected void doSync(VerDatabaseTableModel dbModel, VerIndexTypeModel esModel,
                          List<CanalEntry.Column> columns, CanalEntry.Column idColumn) {
        //构建元数据map
        Map<String, Object> dataMap = parseColumnsToMap(esModel.getIndex(),dbModel, columns, null);
        Integer main = dbModel.getMain();
        if (MainTypeEnum.ONE_TO_MORE.getCode().equals(main)) {
            //放入嵌套数组中
            verElasticsearchService.insertList(esModel.getIndex(), esModel.getType(), idColumn.getValue(),
                    dataMap, dbModel.getListname(), dbModel.getMainKey());
        } else {
            verElasticsearchService.updateSet(esModel.getIndex(), esModel.getType(), idColumn.getValue(), dataMap);
        }
    }
}
