package com.youlai.boot.shared.lock.redisson;

import com.youlai.boot.shared.lock.ILockService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson分布式锁自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@ConditionalOnProperty(name = "lock.type", havingValue = "redis")
public class RedissonLockAutoConfiguration {

    @Bean
    public ILockService redisLockService() {
        return new RedisLockServiceImpl();
    }
}
