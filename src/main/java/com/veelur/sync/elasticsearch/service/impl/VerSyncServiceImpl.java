package com.veelur.sync.elasticsearch.service.impl;

import com.star.sync.elasticsearch.dao.BaseDao;
import com.veelur.sync.elasticsearch.common.BaseConstants;
import com.veelur.sync.elasticsearch.common.MainTypeEnum;
import com.veelur.sync.elasticsearch.config.ParamsConfig;
import com.veelur.sync.elasticsearch.exception.InfoNotRightException;
import com.veelur.sync.elasticsearch.model.DatabaseModel;
import com.veelur.sync.elasticsearch.model.VerDatabaseTableModel;
import com.veelur.sync.elasticsearch.model.ThreadExecModel;
import com.veelur.sync.elasticsearch.model.request.SyncByIndexRequest;
import com.veelur.sync.elasticsearch.service.VerElasticsearchService;
import com.veelur.sync.elasticsearch.service.VerMappingService;
import com.veelur.sync.elasticsearch.service.VerSyncService;
import com.veelur.sync.elasticsearch.util.CollectionUtils;
import com.veelur.sync.elasticsearch.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
public class VerSyncServiceImpl implements VerSyncService, InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(VerSyncServiceImpl.class);

    @Autowired
    private VerMappingService verMappingService;

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private VerElasticsearchService verElasticsearchService;

    private ExecutorService cachedThreadPool;

    @Autowired
    private ParamsConfig paramsConfig;

    @Override
    public void afterPropertiesSet() throws InfoNotRightException {
        if (paramsConfig.getThreadPoolSize() < 0 || paramsConfig.getThreadDownLatchSize() < 0) {
            throw new InfoNotRightException("参数设置异常");
        }
        cachedThreadPool = new ThreadPoolExecutor(paramsConfig.getThreadPoolSize(), paramsConfig.getThreadPoolSize(),
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);
    }

    @Override
    public void destroy() {
        if (cachedThreadPool != null) {
            cachedThreadPool.shutdown();
        }
    }

    @Override
    public boolean syncByIndex(SyncByIndexRequest request) throws InfoNotRightException {
        //根据index获取信息
        DatabaseModel databaseWithIndexType = verMappingService.getDatabaseWithIndexType(request.getIndex(), request.getType());
        List<VerDatabaseTableModel> models;
        if (null == databaseWithIndexType || CollectionUtils.isEmpty(models = databaseWithIndexType.getModels())) {
            logger.info("当前mapping信息错误");
            return false;
        }
        //根据对应的多个database获取数据
        VerDatabaseTableModel mainModel = models.stream().filter(column ->
                null != column.getMain() && MainTypeEnum.MAIN.getCode().equals(column.getMain()))
                .findFirst().orElse(null);
        if (null == mainModel) {
            logger.info("当前mapping信息没有main数据");
            return false;
        }
        List<VerDatabaseTableModel> insetDataTables = models.stream().filter(tableModel -> !mainModel.equals(tableModel)).collect(Collectors.toList());
        String pkStr = mainModel.getPkStr();
        List<Map<String, Object>> maps = baseDao.selectByPKWithPage(mainModel.getDatabase(), mainModel.getTable(),
                0, request.getLimit(),
                mainModel.getPkStr(), null, request.getOrderSign(),
                convertParam(request.getStart(), request.getOrderType()),
                convertParam(request.getEnd(), request.getOrderType()), buildAttchParams(mainModel));
        if (CollectionUtils.isEmpty(maps)) {
            logger.info("获取信息完毕");
            return true;
        }
        //查询其他的附表
        List<Object> pkStrs = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            pkStrs.add(map.get(pkStr));
        }
        if (CollectionUtils.isEmpty(pkStrs) || maps.size() != pkStrs.size()) {
            logger.info("mapping根据pkStr获取信息不符合");
            return false;
        }
        maps = parseColumnsToMapList(maps, mainModel, false);
        Map<String, Map<String, Object>> mapList = maps.stream().collect(Collectors.toMap(o -> String.valueOf(o.get(pkStr)), o -> o));
        dealToEs(insetDataTables, mapList, pkStrs, request.getIndex(), request.getType());
        logger.info("导入es信息第一次成功");
        poolDeals(mainModel, request, insetDataTables, maps.get(maps.size() - 1));
        return true;
    }

    private String buildAttchParams(VerDatabaseTableModel model) {
        Map<String, String> attchs = model.getAttchs();
        if (CollectionUtils.isNotEmpty(attchs)) {
            StringBuffer params = new StringBuffer();
            attchs.forEach((key, val) -> params.append(key).append(" = ").append(val).append(" and "));
            return params.substring(0, params.length() - 5);
        }
        return null;
    }

    private Object convertParam(String param, String orderType) throws InfoNotRightException {
        if (null != param && null != orderType) {
            switch (orderType) {
                case BaseConstants.TYPE_STRING:
                    return param;
                case BaseConstants.TYPE_LONG:
                    return Long.valueOf(param);
                case BaseConstants.TYPE_DOUBLE:
                    return Double.valueOf(param);
                case BaseConstants.TYPE_DATE:
                    return DateUtils.strToDate(param);
                default:
                    throw new InfoNotRightException("参数传递错误");
            }
        }
        return null;
    }

    private void poolDeals(VerDatabaseTableModel mainModel,
                           SyncByIndexRequest request, List<VerDatabaseTableModel> insetDataTables,
                           Map<String, Object> firstMap) {
        cachedThreadPool.execute(() -> {
            logger.info(">>>>>>>>>>[poolDeals]>>>>>>>> start");
            Object lock = new Object();
            long start = System.currentTimeMillis();
            final CountDownLatch begin = new CountDownLatch(paramsConfig.getThreadDownLatchSize());
            try {
                ThreadExecModel model = new ThreadExecModel(null == firstMap ? null : firstMap.get(mainModel.getPkStr()),
                        request.getIndex(), request.getType(), request.getOrderSign(),
                        convertParam(request.getStart(), request.getOrderType()),
                        convertParam(request.getEnd(), request.getOrderType()), request.getLimit());
                for (int i = 0; i < paramsConfig.getThreadDownLatchSize(); i++) {
                    new Thread(() -> {
                        logger.info(Thread.currentThread().getName() + ">>>[threadExec]>>> start");
                        long threadstart = System.currentTimeMillis();
                        try {
                            threadExec(mainModel, insetDataTables, model, lock);
                        } catch (Exception e) {
                            logger.error(Thread.currentThread().getName() + "threadExec异常", e);
                        }
                        logger.info(Thread.currentThread().getName() + ">>>[threadExec]>>> end,used:" + (System.currentTimeMillis() - threadstart) + "ms");
                        begin.countDown();
                    }).start();
                }
                begin.await();
            } catch (InfoNotRightException e) {
                logger.error("参数解析异常", e);
            } catch (InterruptedException e) {
                logger.error("countDownLatch异常", e);
            }
            logger.info(">>>>>>>>>>>>[poolDeals]>>>>>>>> end ,used:" + (System.currentTimeMillis() - start) + "ms");
        });

    }

    public void threadExec(VerDatabaseTableModel mainModel, List<VerDatabaseTableModel> insetDataTables,
                           ThreadExecModel model, final Object lock) {
        if (null == lock) {
            throw new RuntimeException("锁设置异常");
        }
        int count = 1;
        int _limit = model.getLimit();
        String pkStr = mainModel.getPkStr();
        Map<String, Map<String, Object>> mapList;
        List<Map<String, Object>> maps;
        List<Object> pkStrs;
        do {
            synchronized (lock) {
                maps = baseDao.selectByPKWithPage(mainModel.getDatabase(), mainModel.getTable(),
                        0, _limit,
                        mainModel.getPkStr(), model.getPkValue(),
                        model.getOrderSign(), model.getStart(), model.getEnd(), buildAttchParams(mainModel));
                if (CollectionUtils.isEmpty(maps)) {
                    logger.info("获取信息完毕");
                    return;
                }
                model.setPkValue(maps.get(maps.size() - 1).get(mainModel.getPkStr()));
            }
            //查询其他的附表
            pkStrs = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                pkStrs.add(map.get(pkStr));
            }
            if (CollectionUtils.isEmpty(pkStrs) || maps.size() != pkStrs.size()) {
                logger.info("mapping根据pkStr获取信息不符合");
                return;
            }
            maps = parseColumnsToMapList(maps, mainModel, false);
            mapList = maps.stream().collect(Collectors.toMap(o -> String.valueOf(o.get(pkStr)), o -> o));
            dealToEs(insetDataTables, mapList, pkStrs, model.getIndex(), model.getType());
            count++;
        } while (_limit == maps.size());
        logger.info("导入es信息成功,totalCount: {}", count);
    }

    private void dealToEs(List<VerDatabaseTableModel> models, Map<String, Map<String, Object>> mapList,
                          List<Object> pkStrs, String index, String type) {
        List<Map<String, Object>> subMaps;
        boolean oneToMore;
        Object orDefault;
        Map<String, Object> stringObjectMap;
        for (VerDatabaseTableModel tableModel : models) {
            oneToMore = MainTypeEnum.ONE_TO_MORE.getCode().equals(tableModel.getMain());
            try {
                subMaps = baseDao.selectByPKStr(tableModel.getDatabase(), tableModel.getTable(),
                        tableModel.getPkStr(), pkStrs, buildAttchParams(tableModel));
                if (!CollectionUtils.isEmpty(subMaps)) {
                    subMaps = parseColumnsToMapList(subMaps, tableModel, true);
                    if (oneToMore) {
                        for (Map<String, Object> subMap : subMaps) {
                            orDefault = mapList.get(String.valueOf(subMap.get(tableModel.getPkStr())))
                                    .get(tableModel.getListname());
                            if (null != orDefault) {
                                ((List<Map<String, Object>>) orDefault).add(subMap);
                            } else {
                                ArrayList<Map<String, Object>> maps = new ArrayList<>();
                                maps.add(subMap);
                                mapList.get(String.valueOf(subMap.get(BaseConstants._PKSTR_MAIN))).put(tableModel.getListname(), maps);
                            }
                            subMap.remove(BaseConstants._PKSTR_MAIN);
                        }
                    } else {
                        for (Map<String, Object> subMap : subMaps) {
                            if (null != subMap) {
                                stringObjectMap = mapList.get(String.valueOf(subMap.get(BaseConstants._PKSTR_MAIN)));
                                if (null != stringObjectMap) {
                                    subMap.remove(BaseConstants._PKSTR_MAIN);
                                    stringObjectMap.putAll(subMap);
                                }
                            }
                        }
                    }
                }
                //批量导入es中
                verElasticsearchService.batchInsertById(index, type, mapList);
            } catch (Exception e) {
                logger.error("处理导入es异常", e);
            }
        }
    }

    private List<Map<String, Object>> parseColumnsToMapList(List<Map<String, Object>> maps, VerDatabaseTableModel dbModel, boolean needPk) {
        List<Map<String, Object>> jsonMaps = new ArrayList<>();
        maps.forEach(map -> {
            if (map == null) {
                return;
            }
            Map<String, Object> jsonMap = new HashMap<>();
            map.forEach((s, o) -> {
                if (needPk && s.equals(dbModel.getPkStr())) {
                    if (o instanceof Timestamp) {
                        jsonMap.put(BaseConstants._PKSTR_MAIN, new Date(((Timestamp) o).getTime()));
                    } else if (o instanceof BigDecimal) {
                        jsonMap.put(BaseConstants._PKSTR_MAIN, Double.valueOf(String.valueOf(o)));
                    } else if (o instanceof BigInteger) {
                        jsonMap.put(BaseConstants._PKSTR_MAIN, Integer.valueOf(String.valueOf(o)));
                    } else if (o instanceof java.sql.Date) {
                        jsonMap.put(BaseConstants._PKSTR_MAIN, (Date) o);
                    } else {
                        jsonMap.put(BaseConstants._PKSTR_MAIN, o);
                    }
                }
                String esField = convertColumnAndEsName(s, dbModel);
                if (StringUtils.isNotEmpty(esField)) {
                    if (o instanceof Timestamp) {
                        jsonMap.put(esField, new Date(((Timestamp) o).getTime()));
                    } else if (o instanceof BigDecimal) {
                        jsonMap.put(esField, Double.valueOf(String.valueOf(o)));
                    } else if (o instanceof BigInteger) {
                        jsonMap.put(esField, Integer.valueOf(String.valueOf(o)));
                    } else if (o instanceof java.sql.Date) {
                        jsonMap.put(esField, (Date) o);
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
    public String convertColumnAndEsName(String columnName, VerDatabaseTableModel dbModel) {
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

    private String convertEsColumn(String columnName, VerDatabaseTableModel dbModel) {
        Map<String, String> fields = dbModel.getConvert();
        if (null != fields && !fields.isEmpty()) {
            if (fields.containsKey(columnName)) {
                return fields.get(columnName);
            }
        }
        return columnName;
    }
}
