package com.youlai.boot.shared.lock;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 锁服务适配器，提供统一的锁访问方法
 *
 * @author hejz
 * @since 2025/9/27
 */
@Component
public class LockAdapter {

    @Resource
    private ILockService lockService;

    /**
     * 尝试获取锁
     *
     * @param key        锁的键
     * @param expireTime 锁的过期时间
     * @param timeUnit   时间单位
     * @return true 表示获取锁成功，false 表示获取锁失败
     */
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        return lockService.tryLock(key, expireTime, timeUnit);
    }

    /**
     * 释放锁
     *
     * @param key 锁的键
     */
    public void unlock(String key) {
        lockService.unlock(key);
    }

    /**
     * 检查锁是否存在
     *
     * @param key 锁的键
     * @return true 表示锁存在，false 表示锁不存在
     */
    public boolean isLocked(String key) {
        return lockService.isLocked(key);
    }
}
