package com.youlai.boot.shared.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存实现
 *
 * @author hejz
 * @since 2025/9/27
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheServiceImpl implements ICacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("Redis缓存服务启动成功，使用Redis作为缓存实现");
    }

    // ========== 通用操作 ==========

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    // ========== String 操作 ==========

    @Override
    public String get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<String> multiGet(List<String> keys) {
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(value -> value != null ? value.toString() : null)
                .toList();
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public void setObject(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ========== Hash 操作 ==========

    @Override
    public Object hashGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public List<Object> hashMultiGet(String key, Collection<Object> fields) {
        return redisTemplate.opsForHash().multiGet(key, fields);
    }

    @Override
    public Map<Object, Object> hashEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public void hashPut(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    @Override
    public void hashPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public void hashPutAllString(String key, Map<String, String> map) {
        // Redis 的 Hash 操作可以接受 Map<String, String>，会自动转换
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public Boolean hashHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    @Override
    public Long hashDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }
}
