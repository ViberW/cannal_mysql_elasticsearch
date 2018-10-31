package com.veelur.sync.elasticsearch.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.veelur.sync.elasticsearch.common.MainTypeEnum;
import com.veelur.sync.elasticsearch.event.VerInsertCanalEvent;
import com.veelur.sync.elasticsearch.model.VerIndexTypeModel;
import com.veelur.sync.elasticsearch.model.VerDatabaseTableModel;
import com.veelur.sync.elasticsearch.service.VerElasticsearchService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Veelur
 * @version 1.0
 */
@Component
public class VerInsertCanalListenerVer extends VerAbstractCanalListener<VerInsertCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(VerInsertCanalListenerVer.class);

    @Autowired
    private VerElasticsearchService verElasticsearchService;

    @Override
    protected void doSync(VerDatabaseTableModel dbModel, VerIndexTypeModel esModel,
                          List<CanalEntry.Column> columns, CanalEntry.Column idColumn) {
        //构建元数据map
        Map<String, Object> dataMap = parseColumnsToMap(dbModel, columns, null);
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
