package com.veelur.sync.component.canal;

import com.veelur.sync.common.model.canal.DataBaseModel;
import com.veelur.sync.common.util.CollectionUtils;
import com.veelur.sync.component.config.ParamsConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 帮助生成sessionfactorybean等类,就不需要一个个的添加config了
 *
 * @author: Admin
 * @date: 2019/5/13
 * @Description: {相关描述}
 */
@Component
public class MapperCreateHandler implements BeanDefinitionRegistryPostProcessor {

    private BeanDefinitionRegistry registry;

    public void register(Set<DataBaseModel> dataSourceModelSet, ParamsConfig paramsConfig) {
        if (CollectionUtils.isEmpty(dataSourceModelSet)) {
            return;
        }
        List<DataBaseModel> list = new ArrayList<>(dataSourceModelSet);
        for (DataBaseModel model : list) {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl(generateJdbcUrl(model.getJdbcUrl()));
            dataSource.setUsername(model.getUsername());
            dataSource.setPassword(model.getPassword());
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setMaximumPoolSize(10);
            dataSource.setPoolName(model.getDatasource() + "-db");

            BeanDefinitionBuilder factoryBeanBulider = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class)
                    .addPropertyValue("dataSource", dataSource)
                    .addPropertyValue("mapperLocations", paramsConfig.getMapperLocations())
                    .addPropertyValue("configLocation", paramsConfig.getConfigLocation())
                    //.addPropertyValue("typeAliasesPackage", "com.dadaabc.sync.elasticsearch.dao")
                    ;
            BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(factoryBeanBulider.getBeanDefinition(),
                    model.getDatasource() + "SqlSessionFactoryBean"), registry);

            BeanDefinitionBuilder beanBulider = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class)
                    .addConstructorArgReference(model.getDatasource() + "SqlSessionFactoryBean");
            BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(beanBulider.getBeanDefinition(),
                    model.getDatasource() + "SqlSessionTemplate"), registry);
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {

    }

    private String generateJdbcUrl(String jdbcUrl) {
        return "jdbc:mysql://" + jdbcUrl + "?characterSet=utf8mb4&useSSL=false&zeroDateTimeBehavior=ROUND&tinyInt1isBit=false&serverTimezone=Asia/Shanghai";
    }

}
