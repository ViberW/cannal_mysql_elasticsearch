package com.dadaabc.sync.elasticsearch.service.impl;

import com.dadaabc.sync.elasticsearch.common.MainTypeEnum;
import com.dadaabc.sync.elasticsearch.model.DadaDatabaseModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.model.request.SyncByIndexRequest;
import com.dadaabc.sync.elasticsearch.service.DadaElasticsearchService;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
import com.dadaabc.sync.elasticsearch.service.DadaSyncService;
import com.star.sync.elasticsearch.dao.BaseDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
@Service
public class DadaSyncServiceImpl implements DadaSyncService {
    private static final Logger logger = LoggerFactory.getLogger(DadaSyncServiceImpl.class);

    @Autowired
    private DadaMappingService mappingService;

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private DadaElasticsearchService elasticsearchService;

    @Override
    public boolean syncByIndex(SyncByIndexRequest request) {
        //根据index获取信息
        DadaDatabaseModel databaseWithIndexType = mappingService.getDatabaseWithIndexType(request.getIndex(), request.getType());
        List<DataDatabaseTableModel> models;
        if (null == databaseWithIndexType || CollectionUtils.isEmpty(models = databaseWithIndexType.getModels())) {
            logger.info("当前mapping信息错误");
            return false;
        }
        //根据对应的多个database获取数据
        int start = 0;
        int limit = request.getLimit();
        DataDatabaseTableModel mainModel = models.stream().filter(column ->
                null != column.getMain() && MainTypeEnum.MAIN.getCode().equals(column.getMain()))
                .findFirst().orElse(null);
        if (null == mainModel) {
            logger.info("当前mapping信息没有main数据");
            return false;
        }
        String pkStr = mainModel.getPkStr();
        List<Object> pkStrs;
        List<Map<String, Object>> maps;
        int totalCount = 0;
        ExecutorService cachedThreadPool = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);
        Object pk = null;
        Map<String, Map<String, Object>> mapList;
        try {
            do {
                maps = baseDao.selectByPKWithPage(mainModel.getDatabase(), mainModel.getTable(), start, limit,
                        mainModel.getPkStr(), pk,
                        request.getOrderSign(), request.getStart(), request.getEnd());
                if (CollectionUtils.isEmpty(maps)) {
                    logger.info("获取信息完毕");
                    break;
                }
                pk = maps.get(maps.size() - 1).get(mainModel.getPkStr());
                //查询其他的附表
                pkStrs = new ArrayList<>();
                for (Map<String, Object> map : maps) {
                    pkStrs.add(map.get(pkStr));
                }
                if (CollectionUtils.isEmpty(pkStrs) || maps.size() != pkStrs.size()) {
                    logger.info("mapping根据pkStr获取信息不符合");
                    return false;
                }
                maps = parseColumnsToMapList(maps, mainModel);
                mapList = maps.stream().collect(Collectors.toMap(o -> String.valueOf(o.get(pkStr)), o -> o));
                poolDeal(cachedThreadPool, models.stream().filter(tableModel -> !mainModel.equals(tableModel)).collect(Collectors.toList()),
                        mapList, pkStrs, request);

                totalCount++;
                logger.info("导入es信息单词循环成功");
            } while (true);
            logger.info("全量同步es数据信息,totalCount:" + totalCount);
            return true;
        } finally {
            cachedThreadPool.shutdown();
        }
    }

    private void poolDeal(ExecutorService pool, List<DataDatabaseTableModel> models,
                          Map<String, Map<String, Object>> mapList, List<Object> pkStrs, SyncByIndexRequest request) {
        pool.submit(() -> {
            List<Map<String, Object>> subMaps;
            boolean oneToMore;
            Object orDefault;
            Map<String, Object> stringObjectMap;
            for (DataDatabaseTableModel tableModel : models) {
                oneToMore = MainTypeEnum.ONE_TO_MORE.getCode().equals(tableModel.getMain());
                try {
                    subMaps = baseDao.selectByPKStr(tableModel.getDatabase(), tableModel.getTable(), tableModel.getPkStr(), pkStrs);
                    subMaps = parseColumnsToMapList(subMaps, tableModel);
                    if (!CollectionUtils.isEmpty(subMaps)) {
                        subMaps = parseColumnsToMapList(subMaps, tableModel);
                        if (oneToMore) {
                            for (Map<String, Object> subMap : subMaps) {
                                orDefault = mapList.get(String.valueOf(subMap.get(tableModel.getPkStr())))
                                        .get(tableModel.getListname());
                                if (null != orDefault) {
                                    ((List<Map<String, Object>>) orDefault).add(subMap);
                                } else {
                                    ArrayList<Map<String, Object>> maps = new ArrayList<>();
                                    maps.add(subMap);
                                    mapList.get(String.valueOf(subMap.get(tableModel.getPkStr()))).put(tableModel.getListname(), maps);
                                }
                            }
                        } else {
                            for (Map<String, Object> subMap : subMaps) {
                                if (null != subMap) {
                                    stringObjectMap = mapList.get(String.valueOf(subMap.get(tableModel.getPkStr())));
                                    if (null != stringObjectMap) {
                                        stringObjectMap.putAll(subMap);
                                    }
                                }
                            }
                        }
                    }
                    //批量导入es中
                    elasticsearchService.batchInsertById(request.getIndex(), request.getType(), mapList);
                } catch (Exception e) {
                    logger.error("处理导入es异常", e);
                }
            }
            logger.info("单次县城处理结束");
        });
    }

    private List<Map<String, Object>> parseColumnsToMapList(List<Map<String, Object>> maps, DataDatabaseTableModel dbModel) {
        List<Map<String, Object>> jsonMaps = new ArrayList<>();
        maps.forEach(map -> {
            if (map == null) {
                return;
            }
            Map<String, Object> jsonMap = new HashMap<>();
            map.forEach((s, o) -> {
                String esField = convertColumnAndEsName(s, dbModel);
                if (StringUtils.isNotEmpty(esField)) {
                    if (o instanceof Timestamp) {
                        jsonMap.put(esField, new Date(((Timestamp) o).getTime()));
                    } else if (o instanceof BigDecimal) {
                        jsonMap.put(esField, Double.valueOf(String.valueOf(o)));
                    } else if (o instanceof BigInteger) {
                        jsonMap.put(esField, Integer.valueOf(String.valueOf(o)));
                    } else if (o instanceof java.sql.Date) {
                        jsonMap.put(esField, new Date(((java.sql.Date) o).getTime()));
                    } else {
                        jsonMap.put(esField, o);
                    }
                }
            });
            if (!CollectionUtils.isEmpty(jsonMap)) {
                jsonMaps.add(jsonMap);
            }
        });
        return jsonMaps;
    }

    @Override
    public String convertColumnAndEsName(String columnName, DataDatabaseTableModel dbModel) {
        if (StringUtils.isEmpty(columnName)) {
            return null;
        }
        List<String> includeField = dbModel.getIncludeField();
        if (null != includeField && includeField.contains(columnName.trim())) {
            return convertEsColumn(columnName.trim(), dbModel);
        }
        if (null == includeField || includeField.isEmpty()) {
            List<String> excludeField = dbModel.getExcludeField();
            if (null == excludeField || !excludeField.contains(columnName.trim())) {
                return convertEsColumn(columnName.trim(), dbModel);
            }
        }
        return null;
    }

    private String convertEsColumn(String columnName, DataDatabaseTableModel dbModel) {
        Map<String, String> fields = dbModel.getConvert();
        if (null != fields && !fields.isEmpty()) {
            if (fields.containsKey(columnName)) {
                return fields.get(columnName);
            }
        }
        return columnName;
    }
}
