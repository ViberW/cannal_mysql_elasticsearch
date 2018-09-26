package com.dadaabc.sync.elasticsearch.controller;

import com.dadaabc.sync.elasticsearch.model.request.SyncByIndexRequest;
import com.dadaabc.sync.elasticsearch.model.response.Response;
import com.dadaabc.sync.elasticsearch.service.DadaSyncService;
import com.star.sync.elasticsearch.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
@RestController
@RequestMapping("/dada/sync")
public class DadaSyncController {
    private static final Logger logger = LoggerFactory.getLogger(DadaSyncController.class);

    @Autowired
    private DadaSyncService syncService;


    /**
     * @param request
     * @return
     */
    @RequestMapping("/byIndex")
    public Response<Boolean> syncTable(@Validated SyncByIndexRequest request) {
        logger.debug("request_info: " + JsonUtil.toJson(request));
        Response<Boolean> response = Response.success(syncService.syncByIndex(request));
        logger.debug("response_info: " + JsonUtil.toJson(request));
        return response;
    }
}
