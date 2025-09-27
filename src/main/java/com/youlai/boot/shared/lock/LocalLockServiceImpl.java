package com.youlai.boot.shared.lock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 本地锁实现（基于Caffeine）
 * <p>
 * 利用 Caffeine 的 variable expiration 功能，实现每个 key 的过期时间由传入的 expireTime 决定，
 * 并通过原子 putIfAbsent 操作模拟 tryLock 逻辑：
 * - 如果 key 不存在，则插入成功，返回 true；
 * - 如果 key 已存在，则说明在锁定时段内，返回 false。
 * <p>
 * 注意：本实现适用于单机防重复提交场景。
 *
 * @author hejz
 * @since 2025/9/27
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "lock.type", havingValue = "local", matchIfMissing = true)
public class LocalLockServiceImpl implements ILockService {

    // 使用 Caffeine 缓存存储锁的 key，对应的 value 包装了锁定时长
    private final Cache<String, LockCacheEntry> localLockCache;

    public LocalLockServiceImpl() {
        this.localLockCache = Caffeine.newBuilder()
                // 使用 variable expiration，每个 entry 的过期时间由其自身的 expireTime 决定
                .expireAfter(new Expiry<String, LockCacheEntry>() {
                    @Override
                    public long expireAfterCreate(String key, LockCacheEntry value, long currentTime) {
                        // 将 expireTime 转换为纳秒
                        return value.expireTimeNanos();
                    }

                    @Override
                    public long expireAfterUpdate(String key, LockCacheEntry value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(String key, LockCacheEntry value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .maximumSize(10000)
                .build();

        log.info("本地锁服务启动成功，使用Caffeine作为锁实现");
    }

    @Override
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        long expireTimeNanos = timeUnit.toNanos(expireTime);
        LockCacheEntry newEntry = new LockCacheEntry(expireTimeNanos);
        LockCacheEntry previous = localLockCache.asMap().putIfAbsent(key, newEntry);
        return previous == null;
    }

    @Override
    public void unlock(String key) {
        localLockCache.invalidate(key);
    }

    @Override
    public boolean isLocked(String key) {
        return localLockCache.getIfPresent(key) != null;
    }

    /**
     * 内部缓存包装类，用于保存每个 entry 的锁定时长（纳秒）。
     */
    public record LockCacheEntry(long expireTimeNanos) {
    }
}
