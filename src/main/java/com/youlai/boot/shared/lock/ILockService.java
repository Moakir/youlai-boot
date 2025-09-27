package com.youlai.boot.shared.lock;

import java.util.concurrent.TimeUnit;

/**
 * 统一锁服务接口
 *
 * @author hjz
 * @since 2025/3/11
 */
public interface ILockService {

    /**
     * 尝试获取锁
     *
     * @param key        锁的键
     * @param expireTime 锁的过期时间
     * @param timeUnit   时间单位
     * @return true 表示获取锁成功，false 表示获取锁失败
     */
    boolean tryLock(String key, long expireTime, TimeUnit timeUnit);

    /**
     * 释放锁
     *
     * @param key 锁的键
     */
    void unlock(String key);

    /**
     * 检查锁是否存在
     *
     * @param key 锁的键
     * @return true 表示锁存在，false 表示锁不存在
     */
    boolean isLocked(String key);
}
