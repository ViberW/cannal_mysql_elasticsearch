package com.veelur.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author <a href="mailto:wangchao.star@gmail.com">wangchao</a>
 * @version 1.0
 * @since 2017-08-25 17:26:00
 */
@SpringBootApplication
public class SyncApplication {

    public static void main(String[] args) {
        //适用于初始版本  基于starcwang/canal_mysql_elasticsearch_sync重新定义的处理模块
        SpringApplication.run(SyncApplication.class, args);
    }

}