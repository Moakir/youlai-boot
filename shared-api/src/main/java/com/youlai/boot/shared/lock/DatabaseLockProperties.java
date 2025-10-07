package com.youlai.boot.shared.lock;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 数据库锁配置属性
 *
 * @author hejz
 * @since 2025/9/27
 */
@Data
@ConfigurationProperties(prefix = "lock.database")
public class DatabaseLockProperties {
    
    /**
     * 锁表名
     */
    private String tableName = "database_lock";
    
    /**
     * 默认过期时间
     */
    private Duration defaultExpireTime = Duration.ofSeconds(30);
    
    /**
     * 续约间隔
     */
    private Duration renewalInterval = Duration.ofSeconds(10);
    
    /**
     * 清理间隔
     */
    private Duration cleanupInterval = Duration.ofMinutes(1);
}
