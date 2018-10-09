package com.dadaabc.sync.elasticsearch.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.dadaabc.sync.elasticsearch.model.DadaConnectModel;
import com.dadaabc.sync.elasticsearch.model.DadaIndexTypeModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
import com.dadaabc.sync.elasticsearch.service.DadaSyncService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.star.sync.elasticsearch.event.CanalEvent;
import com.star.sync.elasticsearch.listener.AbstractCanalListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public abstract class DadaAbstractCanalListener<EVENT extends CanalEvent> implements ApplicationListener<EVENT> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCanalListener.class);

    @Autowired
    private DadaMappingService mappingService;
    @Autowired
    private DadaSyncService dadaSyncService;

    @Override
    public void onApplicationEvent(EVENT event) {
        CanalEntry.Entry entry = event.getEntry();
        String database = entry.getHeader().getSchemaName();
        String table = entry.getHeader().getTableName();
        DadaConnectModel connectModel = mappingService.getColumnWithData(database, table);
        if (connectModel == null) {
            return;
        }
        DataDatabaseTableModel dbModel = connectModel.getDbModel();
        DadaIndexTypeModel esModel = connectModel.getEsModel();
        CanalEntry.RowChange change;
        try {
            change = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        } catch (InvalidProtocolBufferException e) {
            logger.error("canalEntry_parser_error,根据CanalEntry获取RowChange失败！", e);
            return;
        }
        // 封装model
        change.getRowDatasList().forEach(rowData -> doSync(dbModel, esModel, rowData));
    }

    protected Map<String, Object> parseColumnsToMap(DataDatabaseTableModel dbModel, List<CanalEntry.Column> columns) {
        Map<String, Object> jsonMap = new HashMap<>();
        columns.forEach(column -> {
            if (column == null) {
                return;
            }
            String esField = dadaSyncService.convertColumnAndEsName(column.getName(), dbModel);
            if (StringUtils.isNotEmpty(esField)) {
                jsonMap.put(esField, column.getIsNull() ? null : mappingService.getElasticsearchTypeObject(column.getMysqlType(), column.getValue()));
            }
        });
        return jsonMap;
    }

    protected Map<String, Object> parseColumnsToNullMap(DataDatabaseTableModel dbModel,
                                                        List<CanalEntry.Column> columns, String primaryKey) {
        Map<String, Object> jsonMap = new HashMap<>();
        columns.forEach(column -> {
            if (column == null) {
                return;
            }
            String esField = dadaSyncService.convertColumnAndEsName(column.getName(), dbModel);
            if (!primaryKey.equals(column.getName()) && StringUtils.isNotEmpty(esField)) {
                jsonMap.put(esField, null);
            }
        });
        return jsonMap;
    }

    protected Object parseColumnsByKey(List<CanalEntry.Column> columns, String key) {
        CanalEntry.Column column1 = columns.stream().filter(column ->
                key.equals(column.getName())).findFirst().orElse(null);
        if (null != column1) {
            return column1.getValue();
        }
        return null;
    }

    protected abstract void doSync(DataDatabaseTableModel dbModel, DadaIndexTypeModel esModel, CanalEntry.RowData rowData);
}
