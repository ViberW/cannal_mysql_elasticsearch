package com.dadaabc.sync.elasticsearch.listener;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.dadaabc.sync.elasticsearch.common.MainTypeEnum;
import com.dadaabc.sync.elasticsearch.event.DadaInsertCanalEvent;
import com.dadaabc.sync.elasticsearch.model.DadaIndexTypeModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.service.DadaElasticsearchService;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
import com.star.sync.elasticsearch.util.JsonUtil;
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
public class DadaInsertCanalListener extends DadaAbstractCanalListener<DadaInsertCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DadaInsertCanalListener.class);

    @Autowired
    private DadaElasticsearchService elasticsearchService;

    @Override
    protected void doSync(DataDatabaseTableModel dbModel, DadaIndexTypeModel esModel, RowData rowData) {
        //获取行数据的信息
        List<Column> columns = rowData.getAfterColumnsList();
        String primaryKey = Optional.ofNullable(dbModel.getPkStr()).orElse("id");
        Column idColumn = columns.stream().filter(column ->
                column.getIsKey() && primaryKey.equals(column.getName())).findFirst().orElse(null);
        if (idColumn == null || StringUtils.isBlank(idColumn.getValue())) {
            logger.error("insert_column_find_null_warn insert从column中找不到主键" +
                    ",database=" + dbModel.getDatabase() + ",table=" + dbModel.getTable() +
                    ",pkStr=" + dbModel.getPkStr());
            return;
        }
        logger.info("insert_column_id_info insert主键id,database=" + dbModel.getDatabase()
                + ",table=" + dbModel.getTable() + ",id=" + idColumn.getValue());
        //构建元数据map
        Map<String, Object> dataMap = parseColumnsToMap(dbModel, columns);
        Integer main = dbModel.getMain();
        if (MainTypeEnum.ONE_TO_MORE.getCode().equals(main)) {
            //放入嵌套数组中
            elasticsearchService.updateList(esModel.getIndex(), esModel.getType(), idColumn.getValue(),
                    dataMap, dbModel.getListname(), dbModel.getMainKey());
        } else {
            elasticsearchService.updateSet(esModel.getIndex(), esModel.getType(), idColumn.getValue(), dataMap);
        }
        logger.info("insert_es_info 同步es插入操作成功！database=" + dbModel.getDatabase()
                + ",table=" + dbModel.getTable() + ",data=" + JsonUtil.toJson(dataMap));
    }
}
