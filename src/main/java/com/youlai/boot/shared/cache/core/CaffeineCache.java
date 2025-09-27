package com.youlai.boot.shared.cache.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自管理的 Caffeine 缓存，非常大且不过期，仅内部使用，需要自行管理其生命周期
 *
 * @author hejz
 * @since 2025/9/27
 */
public class CaffeineCache extends AbstractCache<Object, Object> {

    private final transient Cache<CacheKey<Object>, CacheValue<Object, Object>> cache;

    public CaffeineCache() {
        super(-1);
        this.cache = buildCaffeine();
    }

    /**
     * 构建Caffeine缓存对象，创建一个不过期的且足够大缓存对象
     */
    private Cache<CacheKey<Object>, CacheValue<Object, Object>> buildCaffeine() {
        return Caffeine.newBuilder()
            .maximumSize(5000000L)
            .recordStats() // 记录统计信息，便于监控
            .expireAfter(new Expiry<CacheKey<Object>, CacheValue<Object, Object>>() {
                @Override
                public long expireAfterCreate(@NonNull CacheKey<Object> key, @NonNull CacheValue<Object, Object> value, long currentTime) {
                    long timeout = value.getTtl();
                    return timeout == -1L ? Long.MAX_VALUE : timeout * 1000L * 1000L;
                }

                @Override
                public long expireAfterUpdate(@NonNull CacheKey<Object> key, @NonNull CacheValue<Object, Object> value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(@NonNull CacheKey<Object> key, @NonNull CacheValue<Object, Object> value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }
            })
            .build();
    }

    @Override
    protected void put(CacheKey<Object> key, CacheValue<Object, Object> obj) {
        if (obj == null || obj.getValue() == null) {
            this.remove(key);
            return;
        }
        this.cache.put(key, obj);
    }

    @Override
    protected CacheValue<Object, Object> get(CacheKey<Object> key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    protected void remove(CacheKey<Object> key) {
        this.cache.invalidate(key);
    }

    @Override
    protected boolean hasKey(CacheKey<Object> key) {
        return this.cache.asMap().containsKey(key);
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    public Map<Object, Object> asMap() {
        return this.cache.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getValue(),
                        entry -> entry.getValue().getValue()
                ));
    }
}
