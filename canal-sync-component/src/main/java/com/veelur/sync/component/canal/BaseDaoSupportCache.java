package com.veelur.sync.component.canal;

import com.google.common.collect.BiMap;
import com.veelur.sync.common.util.CollectionUtils;
import com.veelur.sync.component.config.ParamsConfig;
import com.veelur.sync.dao.MapperEnum;
import org.apache.commons.lang.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Admin
 * @date: 2019/5/13
 * @Description: {相关描述}
 */
@Component
public class BaseDaoSupportCache {

    private static Map<String, BaseDaoSupport> SUPPORTS = new HashMap<>();
    /**
     * 这个缓存map是为了兼容之前根据datasource取dao的逻辑
     */
    private static Map<String, BaseDaoSupport> COMPATIBLE_SUPPORTS = new HashMap<>();

    @Autowired
    private CanalClient canalClient;
    @Autowired
    private ParamsConfig paramsConfig;

    @PostConstruct
    public void init() {
        //将所有SqlSessionTemplate取出来
        ApplicationContext applicationContext = paramsConfig.getApplicationContext();
        //获取所有destination和db的对应关系
        BiMap<String, String> destinationSources = canalClient.getDestinationSources();
        if (CollectionUtils.isNotEmpty(destinationSources)) {
            for (String destination : destinationSources.keySet()) {
                try {
                    if (StringUtils.isNotBlank(destinationSources.get(destination))) {
                        BaseDaoSupport baseDaoSupport = new BaseDaoSupport((SqlSessionTemplate) applicationContext
                                .getBean(destinationSources.get(destination) + "SqlSessionTemplate"));
                        SUPPORTS.put(destination, baseDaoSupport);

                        COMPATIBLE_SUPPORTS.put(destinationSources.get(destination), baseDaoSupport);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public BaseDaoSupport getSupport(String destination) {
        if (null != destination) {
            return SUPPORTS.get(destination);
        }
        return null;
    }

    public BaseDaoSupport getSupportByDataSource(String dataSource) {
        if (StringUtils.isEmpty(dataSource)) {
            return COMPATIBLE_SUPPORTS.get(MapperEnum.ABC.getCode());
        }
        return COMPATIBLE_SUPPORTS.get(dataSource);
    }
}
