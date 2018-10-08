package com.dadaabc.sync.elasticsearch.service.impl;

import com.dadaabc.sync.elasticsearch.common.MainTypeEnum;
import com.dadaabc.sync.elasticsearch.model.DadaDatabaseModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.model.request.SyncByIndexRequest;
import com.dadaabc.sync.elasticsearch.service.DadaElasticsearchService;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
import com.dadaabc.sync.elasticsearch.service.DadaSyncService;
import com.star.sync.elasticsearch.dao.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        Map<String, Map<String, Object>> mapList;
        int totalCount = 0;
        ExecutorService cachedThreadPool = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), (ThreadFactory) Thread::new);
        Object pk = null;
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
                mapList = maps.stream().collect(Collectors.toMap(o -> String.valueOf(o.get(pkStr)), o -> o));

                poolDeal(cachedThreadPool, models, mapList, pkStrs, request);

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
            for (DataDatabaseTableModel tableModel : models) {
                oneToMore = MainTypeEnum.ONE_TO_MORE.getCode().equals(tableModel.getMain());
                subMaps = baseDao.selectByPKStr(tableModel.getDatabase(), tableModel.getTable(), tableModel.getPkStr(), pkStrs);
                if (!CollectionUtils.isEmpty(subMaps)) {
                    if (oneToMore) {
                        for (Map<String, Object> subMap : subMaps) {

                            orDefault = mapList.get(tableModel.getPkStr())
                                    .get(tableModel.getListname());
                            if (null != orDefault) {
                                ((List<Map<String, Object>>) orDefault).add(subMap);
                            } else {
                                mapList.get(tableModel.getPkStr()).put(tableModel.getListname(),
                                        new ArrayList<Map<String, Object>>() {{
                                            add(subMap);
                                        }});
                            }
                        }
                    } else {
                        for (Map<String, Object> subMap : subMaps) {
                            mapList.get(tableModel.getPkStr()).putAll(subMap);
                        }
                    }
                }
            }
            //批量导入es中
            elasticsearchService.batchInsertById(request.getIndex(), request.getType(), mapList);
        });
    }
}
