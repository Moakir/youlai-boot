package com.youlai.boot.shared.cache;


import com.youlai.boot.shared.cache.core.CaffeineCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存实现
 *
 * @author hjz
 * @since 2025/3/11
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class LocalCacheServiceImpl implements ICacheService, DisposableBean {

    private final CaffeineCache cache;

    // 用于存储过期时间的Map
    private final Map<String, Long> expireTimeMap = new ConcurrentHashMap<>();

    // 定时任务执行器，用于清理过期的键
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LocalCacheServiceImpl() {
        this.cache = new CaffeineCache();

        // 每5秒检查一次过期的键
        this.scheduler.scheduleWithFixedDelay(
                this::checkAndRemoveExpiredKeys,
                5, 5, TimeUnit.SECONDS
        );

        log.info("本地缓存服务启动成功，使用Caffeine作为缓存实现");
    }

    @Override
    public void destroy() {
        log.info("关闭本地缓存服务的定时清理任务");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // 对应RedisTemplate的实现：redisTemplate.hasKey(key);
    @Override
    public Boolean hasKey(String key) {
        if (isExpired(key)) {
            delete(key);
            return false;
        }
        return cache.hasKey(key);
    }

    // 对应RedisTemplate的实现：redisTemplate.delete(key);
    @Override
    public Boolean delete(String key) {
        return SynchronizedByKey.exec(key, k -> {
            cache.remove(k);
            expireTimeMap.remove(k);
            return true;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.delete(keys);
    @Override
    public Long delete(Collection<String> keys) {
        long count = 0;
        for (String key : keys) {
            if (Boolean.TRUE.equals(delete(key))) {
                count++;
            }
        }
        return count;
    }

    // 对应RedisTemplate的实现：redisTemplate.expire(key, timeout, unit);
    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return SynchronizedByKey.exec(key, k -> {
            if (!cache.hasKey(k)) {
                return false;
            }

            // 转换为毫秒
            long timeoutMillis = unit.toMillis(timeout);
            expireTimeMap.put(k, System.currentTimeMillis() + timeoutMillis);
            return true;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().get(key);
    @Override
    public String get(String key) {
        if (isExpired(key)) {
            delete(key);
            return null;
        }
        Object value = cache.get(key);
        return value != null ? value.toString() : null;
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().get(key);
    @Override
    public Object getObject(String key) {
        if (isExpired(key)) {
            delete(key);
            return null;
        }
        return cache.get(key);
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().multiGet(keys);
    @Override
    public List<String> multiGet(List<String> keys) {
        List<String> result = new ArrayList<>();
        for (String key : keys) {
            String value = get(key);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().set(key, value);
    @Override
    public void set(String key, String value) {
        SynchronizedByKey.exec(key, k -> {
            cache.put(k, value);
            expireTimeMap.remove(k); // 移除之前的过期时间（如果有）
            return null;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().set(key, value, timeout, unit);
    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        SynchronizedByKey.exec(key, k -> {
            cache.put(k, value);
            long timeoutMillis = unit.toMillis(timeout);
            expireTimeMap.put(k, System.currentTimeMillis() + timeoutMillis);
            return null;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().set(key, value);
    @Override
    public void setObject(String key, Object value) {
        SynchronizedByKey.exec(key, k -> {
            cache.put(k, value);
            expireTimeMap.remove(k); // 移除之前的过期时间（如果有）
            return null;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().set(key, value, timeout, unit);
    @Override
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        SynchronizedByKey.exec(key, k -> {
            cache.put(k, value);
            long timeoutMillis = unit.toMillis(timeout);
            expireTimeMap.put(k, System.currentTimeMillis() + timeoutMillis);
            return null;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().increment(key);
    @Override
    public Long increment(String key) {
        return increment(key, 1L);
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForValue().increment(key, delta);
    @Override
    public Long increment(String key, long delta) {
        return SynchronizedByKey.exec(key, k -> {
            if (isExpired(k)) {
                delete(k);
                cache.put(k, String.valueOf(delta));
                return delta;
            }

            Object value = cache.get(k);
            long result;

            if (value == null) {
                result = delta;
            } else {
                try {
                    result = Long.parseLong(value.toString()) + delta;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Value is not a number: " + value);
                }
            }

            cache.put(k, Long.toString(result));
            return result;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForHash().get(key, field);
    @Override
    public Object hashGet(String key, String field) {
        if (isExpired(key)) {
            delete(key);
            return null;
        }

        Object hashMap = cache.get(key);
        if (hashMap instanceof Map) {
            return ((Map<?, ?>) hashMap).get(field);
        }
        return null;
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForHash().multiGet(key, fields);
    @Override
    public List<Object> hashMultiGet(String key, Collection<Object> fields) {
        if (isExpired(key)) {
            delete(key);
            return new ArrayList<>();
        }

        List<Object> result = new ArrayList<>();
        Object hashMap = cache.get(key);
        if (hashMap instanceof Map<?, ?> map) {
            for (Object field : fields) {
                result.add(map.get(field));
            }
        }
        return result;
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForHash().entries(key);
    @Override
    public Map<Object, Object> hashEntries(String key) {
        if (isExpired(key)) {
            delete(key);
            return new HashMap<>();
        }

        Object hashMap = cache.get(key);
        if (hashMap instanceof Map<?, ?> map) {
            return new HashMap<>(map);
        }

        return new HashMap<>();
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForHash().put(key, field, value);
    @Override
    public void hashPut(String key, String field, Object value) {
        SynchronizedByKey.exec(key, k -> {
            if (isExpired(k)) {
                delete(k);
                Map<String, Object> newMap = new HashMap<>();
                newMap.put(field, value);
                cache.put(k, newMap);
                return null;
            }

            Map<String, Object> map = safeGetMap(cache.get(k));
            if (map.isEmpty()) {
                cache.put(k, map = new HashMap<>());
            }
            map.put(field, value);
            return null;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForHash().putAll(key, map);
    @Override
    public void hashPutAll(String key, Map<String, Object> map) {
        SynchronizedByKey.exec(key, k -> {
            if (isExpired(k)) {
                delete(k);
                cache.put(k, new HashMap<>(map));
                return null;
            }

            Map<String, Object> existingMap = safeGetMap(cache.get(k));
            if (existingMap.isEmpty()) {
                cache.put(k, existingMap = new HashMap<>());
            }
            existingMap.putAll(map);
            return null;
        });
    }

    // 对应RedisTemplate的实现：Map<String, Object> objectMap = new HashMap<>(map); redisTemplate.opsForHash().putAll(key, objectMap);
    @Override
    public void hashPutAllString(String key, Map<String, String> map) {
        SynchronizedByKey.exec(key, k -> {
            if (isExpired(k)) {
                delete(k);
                cache.put(k, new HashMap<>(map));
                return null;
            }

            Map<String, String> existingMap = safeGetMap(cache.get(k));
            if (existingMap.isEmpty()) {
                cache.put(k, existingMap = new HashMap<>());
            }

            existingMap.putAll(map);
            return null;
        });
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForHash().hasKey(key, field);
    @Override
    public Boolean hashHasKey(String key, String field) {
        if (isExpired(key)) {
            delete(key);
            return false;
        }

        Object hashMap = cache.get(key);
        if (hashMap instanceof Map) {
            return ((Map<?, ?>) hashMap).containsKey(field);
        }

        return false;
    }

    // 对应RedisTemplate的实现：redisTemplate.opsForHash().delete(key, fields);
    @Override
    public Long hashDelete(String key, Object... fields) {
        return SynchronizedByKey.exec(key, k -> {
            if (isExpired(k)) {
                delete(k);
                return 0L;
            }

            Map<Object, Object> map = safeGetMap(cache.get(k));
            if (map.isEmpty()) {
                return 0L;
            }

            long count = 0;
            for (Object field : fields) {
                if (map.remove(field) != null) {
                    count++;
                }
            }
            return count;
        });
    }

    // 辅助方法：安全获取Map
    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> safeGetMap(Object hashMap) {
        return hashMap instanceof Map ? (Map<K, V>) hashMap : new HashMap<>();
    }

    // 辅助方法：检查键是否过期
    private boolean isExpired(String key) {
        Long expireTime = expireTimeMap.get(key);
        return expireTime != null && System.currentTimeMillis() > expireTime;
    }

    // 辅助方法：检查并删除所有过期的键
    private void checkAndRemoveExpiredKeys() {
        try {
            long startTime = System.currentTimeMillis();
            int count = 0;

            Iterator<Map.Entry<String, Long>> iterator = expireTimeMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                if (System.currentTimeMillis() > entry.getValue()) {
                    cache.remove(entry.getKey());
                    iterator.remove();
                    count++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            if (count > 0) {
                log.debug("已清理{}个过期的键，耗时{}ms", count, duration);
            }
        } catch (Exception e) {
            log.error("清理过期键时发生错误", e);
        }
    }
}
