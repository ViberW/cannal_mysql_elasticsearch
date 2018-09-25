package com.dadaabc.sync.elasticsearch.listener;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.dadaabc.sync.elasticsearch.event.DadaUpdateCanalEvent;
import com.dadaabc.sync.elasticsearch.model.DadaIndexTypeModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.service.DadaElasticsearchService;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
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
public class DadaUpdateCanalListener extends DadaAbstractCanalListener<DadaUpdateCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DadaUpdateCanalListener.class);

    @Autowired
    private DadaElasticsearchService elasticsearchService;

    @Override
    protected void doSync(DataDatabaseTableModel dbModel, DadaIndexTypeModel esModel, RowData rowData) {
        List<Column> columns = rowData.getAfterColumnsList();
        String primaryKey = Optional.ofNullable(dbModel.getPkStr()).orElse("id");
        Column idColumn = columns.stream().filter(column ->
                column.getIsKey() && primaryKey.equals(column.getName())).findFirst().orElse(null);
        if (idColumn == null || StringUtils.isBlank(idColumn.getValue())) {
            logger.error("update_column_find_null_warn update从column中找不到主键" +
                    ",database=" + dbModel.getDatabase() + ",table=" + dbModel.getTable() +
                    ",pkStr=" + dbModel.getPkStr());
            return;
        }
        logger.info("column信息:"+columns.toString());
        logger.info("update_column_id_info update主键id,database=" + dbModel.getTable()
                + ",table=" + dbModel.getTable() + ",id=" + idColumn.getValue());
        Map<String, Object> dataMap = parseColumnsToMap(dbModel, columns);
        elasticsearchService.updateSet(esModel.getIndex(), esModel.getType(), idColumn.getValue(), dataMap);
        logger.info("update_es_info 同步es插入操作成功！database=" + dbModel.getDatabase()
                + ",table=" + dbModel.getTable() + ",data=" + dataMap);
    }
}
