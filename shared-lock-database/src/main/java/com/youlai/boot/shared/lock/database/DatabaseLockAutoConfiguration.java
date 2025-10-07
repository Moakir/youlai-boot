package com.youlai.boot.shared.lock.database;

import com.youlai.boot.shared.lock.DatabaseLockProperties;
import com.youlai.boot.shared.lock.ILockService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库分布式锁自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@ConditionalOnProperty(name = "lock.type", havingValue = "database")
@EnableConfigurationProperties(DatabaseLockProperties.class)
public class DatabaseLockAutoConfiguration {

    @Bean
    public ILockService databaseLockService() {
        return new DatabaseLockServiceImpl();
    }
}
