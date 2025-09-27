package com.youlai.boot.shared.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁实现（基于Redisson）
 * <p>
 * 直接使用 RedissonClient 实现分布式锁，提供更好的性能和可靠性
 *
 * @author hejz
 * @since 2025/9/27
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "lock.type", havingValue = "redis")
public class RedisLockServiceImpl implements ILockService {

    private final RedissonClient redissonClient;
    
    // 存储已获取的锁，用于 unlock 时释放
    private final ConcurrentHashMap<String, RLock> acquiredLocks = new ConcurrentHashMap<>();

    public RedisLockServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        log.info("Redis分布式锁服务启动成功，使用Redisson作为锁实现");
    }

    @Override
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(key);
        try {
            // 使用 Redisson 的 tryLock 方法，0表示立即尝试，不等待
            boolean acquired = lock.tryLock(0, expireTime, timeUnit);
            if (acquired) {
                acquiredLocks.put(key, lock);
                log.debug("成功获取Redis锁: {}", key);
            } else {
                log.debug("获取Redis锁失败: {}", key);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("获取锁被中断: {}", key, e);
            return false;
        } catch (Exception e) {
            log.error("获取Redis锁异常: {}", key, e);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = acquiredLocks.remove(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
                log.debug("成功释放Redis锁: {}", key);
            } catch (Exception e) {
                log.warn("释放Redis锁失败: {}", key, e);
            }
        } else {
            log.warn("尝试释放未持有的锁: {}", key);
        }
    }

    @Override
    public boolean isLocked(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            return lock.isLocked();
        } catch (Exception e) {
            log.warn("检查Redis锁状态失败: {}", key, e);
            return false;
        }
    }
}
