package com.youlai.boot.shared.cache.redis;

import com.youlai.boot.shared.cache.CacheAdapter;
import com.youlai.boot.shared.cache.ICacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis缓存自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheAutoConfiguration {

    @Bean
    public ICacheService redisCacheService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheServiceImpl(redisTemplate);
    }

    @Bean
    public CacheAdapter cacheAdapter(ICacheService cacheService) {
        return new CacheAdapter(cacheService);
    }
}
