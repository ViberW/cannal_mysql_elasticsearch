package com.veelur.sync.parse.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.veelur.sync.parse.service.VerElasticsearchService;
import com.veelur.sync.canal.event.VerInsertCanalEvent;
import com.veelur.sync.common.constant.MainTypeEnum;
import com.veelur.sync.common.model.VerDatabaseTableModel;
import com.veelur.sync.common.model.VerIndexTypeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
    protected List<CanalEntry.Column> getColumns(CanalEntry.RowData rowData) {
        return rowData.getAfterColumnsList();
    }

    @Override
    protected void doSync(VerDatabaseTableModel dbModel, VerIndexTypeModel esModel,
                          List<CanalEntry.Column> columns, CanalEntry.Column idColumn) {
        //构建元数据map
        Map<String, Object> dataMap = parseColumnsToMap(esModel.getIndex(), dbModel, columns, null);
        Integer main = dbModel.getMain();
        if (MainTypeEnum.ONE_TO_MORE.getCode().equals(main)) {
            //放入嵌套数组中
            verElasticsearchService.insertList(esModel.getIndex(), esModel.getType(), idColumn.getValue(),
                    dataMap, dbModel.getListname(), dbModel.getMainKey());
        } else if (MainTypeEnum.ONE_TO_ONE.getCode().equals(main) && dbModel.getAddition()) {
            verElasticsearchService.updateSet(esModel.getIndex(), esModel.getType(), idColumn.getValue(),
                    Collections.singletonMap(dbModel.getAdditionField(), dataMap));
        } else {
            verElasticsearchService.updateSet(esModel.getIndex(), esModel.getType(), idColumn.getValue(), dataMap);
        }
    }
}
