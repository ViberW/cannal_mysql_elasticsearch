package com.dadaabc.sync.elasticsearch.service.impl;

import com.dadaabc.sync.elasticsearch.model.DadaDatabaseModel;
import com.dadaabc.sync.elasticsearch.model.request.SyncByIndexRequest;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
import com.dadaabc.sync.elasticsearch.service.DadaSyncService;
import com.star.sync.elasticsearch.dao.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public boolean syncByIndex(SyncByIndexRequest request) {
        // TODO: 18-9-25 分页获取信息并处理
        //根据index获取信息
        DadaDatabaseModel databaseWithIndexType = mappingService.getDatabaseWithIndexType(request.getIndex(), request.getType());
        //根据对应的多个database获取数据
        //根据信息进行封装
        //批量插入es
        return false;
    }
}
