package com.dadaabc.sync.elasticsearch.listener;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.dadaabc.sync.elasticsearch.event.DadaUpdateCanalEvent;
import com.star.sync.elasticsearch.event.UpdateCanalEvent;
import com.star.sync.elasticsearch.listener.AbstractCanalListener;
import com.star.sync.elasticsearch.service.ElasticsearchService;
import com.star.sync.elasticsearch.service.MappingService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Veelur
 * @version 1.0
 */
@Component
public class DadaUpdateCanalListener extends AbstractCanalListener<DadaUpdateCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DadaUpdateCanalListener.class);

    @Resource
    private MappingService mappingService;

    @Resource
    private ElasticsearchService elasticsearchService;

    @Override
    protected void doSync(String database, String table, String index, String type, RowData rowData) {
        List<Column> columns = rowData.getAfterColumnsList();
        String primaryKey = Optional.ofNullable(mappingService.getTablePrimaryKeyMap().get(database + "." + table)).orElse("id");
        Column idColumn = columns.stream().filter(column -> column.getIsKey() && primaryKey.equals(column.getName())).findFirst().orElse(null);
        if (idColumn == null || StringUtils.isBlank(idColumn.getValue())) {
            logger.warn("update_column_find_null_warn update从column中找不到主键,database=" + database + ",table=" + table);
            return;
        }
        logger.debug("update_column_id_info update主键id,database=" + database + ",table=" + table + ",id=" + idColumn.getValue());
        Map<String, Object> dataMap = parseColumnsToMap(columns);
        elasticsearchService.update(index, type, idColumn.getValue(), dataMap);
        logger.debug("update_es_info 同步es插入操作成功！database=" + database + ",table=" + table + ",data=" + dataMap);
    }
}
