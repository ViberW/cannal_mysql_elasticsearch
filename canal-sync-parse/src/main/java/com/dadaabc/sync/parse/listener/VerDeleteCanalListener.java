package com.dadaabc.sync.parse.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.veelur.sync.canal.event.VerDeleteCanalEvent;
import com.veelur.sync.common.constant.MainTypeEnum;
import com.veelur.sync.common.model.VerDatabaseTableModel;
import com.veelur.sync.common.model.VerIndexTypeModel;
import com.dadaabc.sync.parse.service.VerElasticsearchService;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Veelur
 * @version 1.0
 */
@Component
public class VerDeleteCanalListener extends VerAbstractCanalListener<VerDeleteCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(VerDeleteCanalListener.class);

    @Autowired
    private VerElasticsearchService verElasticsearchService;

    @Override
    protected List<CanalEntry.Column> getColumns(CanalEntry.RowData rowData) {
        return rowData.getBeforeColumnsList();
    }

    @Override
    protected void doSync(VerDatabaseTableModel dbModel, VerIndexTypeModel esModel,
                          List<CanalEntry.Column> columns, CanalEntry.Column idColumn) {
        Integer main = dbModel.getMain();
        if (MainTypeEnum.MAIN.getCode().equals(main)) {
            verElasticsearchService.deleteById(esModel.getIndex(), esModel.getType(), idColumn.getValue());
        } else if (MainTypeEnum.ONE_TO_ONE.getCode().equals(main)) {
            Map<String, Object> dataMap = parseColumnsToNullMap(esModel.getIndex(), dbModel, columns);
            if (dbModel.getAddition()) {
                verElasticsearchService.deleteByQuerySet(esModel.getIndex(), esModel.getType(), idColumn.getValue(),
                        Collections.singletonMap(dbModel.getAdditionField(), dataMap));
            } else {
                //删除es中的部分字段信息,置为null
                verElasticsearchService.deleteByQuerySet(esModel.getIndex(), esModel.getType(), idColumn.getValue(), dataMap);
            }
        } else if (MainTypeEnum.ONE_TO_MORE.getCode().equals(main)) {
            //将对应的mapping的信息删除
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(dbModel.getMainKey(), parseColumnsByKey(columns, dbModel.getMainKey()));
            verElasticsearchService.deleteList(esModel.getIndex(), esModel.getType(), idColumn.getValue(),
                    dataMap, dbModel.getListname(), dbModel.getMainKey());
        }
    }
}
