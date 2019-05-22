package com.dadaabc.sync.elasticsearch.parse.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.dadaabc.sync.elasticsearch.parse.service.VerElasticsearchService;
import com.veelur.sync.canal.event.VerUpdateCanalEvent;
import com.veelur.sync.common.constant.MainTypeEnum;
import com.veelur.sync.common.model.VerDatabaseTableModel;
import com.veelur.sync.common.model.VerIndexTypeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Veelur
 * @version 1.0
 */
@Component
public class VerUpdateCanalListener extends VerAbstractCanalListener<VerUpdateCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(VerUpdateCanalListener.class);

    @Autowired
    private VerElasticsearchService verElasticsearchService;

    @Override
    protected void doSync(VerDatabaseTableModel dbModel, VerIndexTypeModel esModel,
                          List<CanalEntry.Column> columns, CanalEntry.Column idColumn) {
        Map<String, Object> updateMap = new HashMap<>();
        Map<String, Object> dataMap = parseColumnsToMap(esModel.getIndex(), dbModel, columns, updateMap);
        Integer main = dbModel.getMain();
        if (MainTypeEnum.ONE_TO_MORE.getCode().equals(main)) {
            //更新入嵌套数组中
            if (updateMap.isEmpty()) {
                return;
            }
            verElasticsearchService.updateList(esModel.getIndex(), esModel.getType(), idColumn.getValue(),
                    dataMap, updateMap, dbModel.getListname(), dbModel.getMainKey());
        } else {
            verElasticsearchService.updateSet(esModel.getIndex(), esModel.getType(), idColumn.getValue(), dataMap);
        }
    }
}
