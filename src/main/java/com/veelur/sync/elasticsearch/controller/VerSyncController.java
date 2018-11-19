package com.veelur.sync.elasticsearch.controller;

import com.veelur.sync.elasticsearch.exception.InfoNotRightException;
import com.veelur.sync.elasticsearch.model.request.SyncByIndexRequest;
import com.veelur.sync.elasticsearch.model.response.Response;
import com.veelur.sync.elasticsearch.service.VerSyncService;
import com.veelur.sync.elasticsearch.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
@RestController
@RequestMapping("/ver/sync")
public class VerSyncController {
    private static final Logger logger = LoggerFactory.getLogger(VerSyncController.class);

    @Autowired
    private VerSyncService verSyncService;


    /**
     * @param request
     * @return
     */
    @RequestMapping("/byIndex")
    public Response<Boolean> syncTable(@Validated SyncByIndexRequest request, BindingResult bindingResult) throws InfoNotRightException {
        if (bindingResult.hasErrors()) {
            logger.info("全量同步信息错误" + bindingResult.getFieldErrors().toString());
            return Response.fail(1, bindingResult.getFieldErrors().toString());
        }
        logger.info("request_info: " + JsonUtils.toJson(request));
        Response<Boolean> response = Response.success(verSyncService.syncByIndex(request));
        logger.info("response_info: " + JsonUtils.toJson(request));
        return response;
    }
}
