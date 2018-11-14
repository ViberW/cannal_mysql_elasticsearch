package com.veelur.sync.elasticsearch.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.veelur.sync.elasticsearch.event.CanalEvent;
import com.veelur.sync.elasticsearch.model.ConnectModel;
import com.veelur.sync.elasticsearch.model.VerDatabaseTableModel;
import com.veelur.sync.elasticsearch.model.VerIndexTypeModel;
import com.veelur.sync.elasticsearch.service.VerMappingService;
import com.veelur.sync.elasticsearch.service.VerSyncService;
import com.veelur.sync.elasticsearch.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public abstract class VerAbstractCanalListener<EVENT extends CanalEvent> implements ApplicationListener<EVENT> {
    private static final Logger logger = LoggerFactory.getLogger(VerAbstractCanalListener.class);

    @Autowired
    private VerMappingService verMappingService;
    @Autowired
    private VerSyncService verSyncService;

    @Override
    public void onApplicationEvent(EVENT event) {
        CanalEntry.Entry entry = event.getEntry();
        String database = entry.getHeader().getSchemaName();
        String table = entry.getHeader().getTableName();
        ConnectModel connectModel = verMappingService.getColumnWithData(database, table);
        if (connectModel == null) {
            return;
        }
        VerDatabaseTableModel dbModel = connectModel.getDbModel();
        VerIndexTypeModel esModel = connectModel.getEsModel();
        CanalEntry.RowChange change;
        try {
            change = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        } catch (InvalidProtocolBufferException e) {
            logger.error("canalEntry_parser_error,根据CanalEntry获取RowChange失败！", e);
            return;
        }
        // 封装model
        change.getRowDatasList().forEach(rowData -> dealSync(dbModel, esModel, rowData));
    }

    protected Map<String, Object> parseColumnsToMap(VerDatabaseTableModel dbModel, List<CanalEntry.Column> columns,
                                                    Map<String, Object> updateMap) {
        Map<String, Object> jsonMap = new HashMap<>();
        columns.forEach(column -> {
            if (column == null) {
                return;
            }
            String esField = verSyncService.convertColumnAndEsName(column.getName(), dbModel);
            if (StringUtils.isNotEmpty(esField)) {
                Object value = column.getIsNull() ? null : verMappingService.getElasticsearchTypeObject(column.getMysqlType(), column.getValue());
                jsonMap.put(esField, value);
                if (null != updateMap && column.getUpdated()) {
                    updateMap.put(esField, value);
                }
            }
        });
        return jsonMap;
    }

    protected Map<String, Object> parseColumnsToNullMap(VerDatabaseTableModel dbModel,
                                                        List<CanalEntry.Column> columns) {
        Map<String, Object> jsonMap = new HashMap<>();
        String primaryKey = Optional.ofNullable(dbModel.getPkStr()).orElse("id");
        columns.forEach(column -> {
            if (column == null) {
                return;
            }
            String esField = verSyncService.convertColumnAndEsName(column.getName(), dbModel);
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

    /**
     * 前置处理
     *
     * @param dbModel
     * @param esModel
     * @param rowData
     */
    protected void dealSync(VerDatabaseTableModel dbModel, VerIndexTypeModel esModel, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> columns = rowData.getBeforeColumnsList();
        String primaryKey = Optional.ofNullable(dbModel.getPkStr()).orElse("id");
        CanalEntry.Column idColumn = columns.stream().filter(column ->
                primaryKey.equals(column.getName())).findFirst().orElse(null);
        if (idColumn == null || StringUtils.isBlank(idColumn.getValue())) {
            logger.error("insert_column_find_null_warn insert从column中找不到主键" +
                    ",database=" + dbModel.getDatabase() + ",table=" + dbModel.getTable() +
                    ",pkStr=" + dbModel.getPkStr());
            return;
        }
        if (CollectionUtils.isNotEmpty(dbModel.getAttchs())) {
            //判定和是否为该值
            for (Map.Entry<String, String> entry : dbModel.getAttchs().entrySet()) {
                if (!entry.getValue().equals(columns.stream().filter(column ->
                        entry.getKey().equals(column.getName())).findFirst().orElse(null))) {
                    return;
                }
            }
        }
        doSync(dbModel, esModel, columns, idColumn);
    }

    protected abstract void doSync(VerDatabaseTableModel dbModel, VerIndexTypeModel esModel,
                                   List<CanalEntry.Column> columns, CanalEntry.Column idColumn);
}
