package com.veelur.sync.parse.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author: veelur
 * @date: 18-10-22
 * @Description: {相关描述}
 */
@Component
public class PostInitService implements InitializingBean {

    @Autowired
    private VerElasticsearchService elasticsearchService;
    @Autowired
    private VerMappingService mappingService;

    @Override
    public void afterPropertiesSet() throws Exception {
        //初始化script
        elasticsearchService.checkAndSetStoredScript();
        /*//初始化索引的分片
        Set<VerIndexTypeModel> indexTypeModels = mappingService.getIndexTypeModels();
        if (CollectionUtils.isNotEmpty(indexTypeModels)) {
            for (VerIndexTypeModel model : indexTypeModels) {
                elasticsearchService.checkAndSetIndex(model.getIndex(), model.getType());
            }
        }*/
    }
}
