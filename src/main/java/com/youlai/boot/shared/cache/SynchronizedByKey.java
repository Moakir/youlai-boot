package com.youlai.boot.shared.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 按键同步执行的工具类
 * 用于保证同一个键的操作串行执行，不同键的操作并行执行
 *
 * @author hjz
 * @since 2025/3/11
 **/
@Slf4j
public class SynchronizedByKey {

    private static final Map<String, ReentrantLock> mutexCache = new ConcurrentHashMap<>();

    private SynchronizedByKey() {
    }

    /**
     * 根据业务Key加锁
     *
     * @param key      业务Key
     * @param function 业务处理函数调用
     * @param <T>      返回值类型
     * @return T 返回值类型
     */
    public static <T> T exec(String key, Function<String, T> function) {
        ReentrantLock mutex = getLock(key);
        try {
            return function.apply(key);
        } finally {
            releaseLock(key, mutex);
        }
    }

    /**
     * 根据业务Key加锁
     *
     * @param key      业务Key
     * @param consumer 业务处理函数调用
     */
    public static void exec(String key, Consumer<String> consumer) {
        ReentrantLock mutex = getLock(key);
        try {
            consumer.accept(key);
        } finally {
            releaseLock(key, mutex);
        }
    }

    /**
     * 获取指定键的锁
     *
     * @param key 键
     * @return 获取到的锁
     */
    private static ReentrantLock getLock(String key) {
        ReentrantLock mutex4Key = null;
        ReentrantLock mutexInCache;
        //避免极端情况下重入进来的锁与之前的不是同一把，或者获取到锁，但锁已经被删除了
        //例如：
        //1. 线程1 先获取到锁
        //2. 线程2 从缓存中获取锁
        //3. 线程1 在线程2加锁前，发现mutex.getQueueLength()==0，所以释放锁，清除了缓存中的锁
        //4. 线程2 使用不在缓存中的锁进行加锁
        //问题分析：此时其它线程进入可能使用同一个key，获取到新的锁，导致线程2加锁无意义
        //解决方案：
        //5. 线程2 加锁后，比对锁，如果不是同一把，则循环释放锁，直到获取到同一把锁
        //可能的竟态问题：多个线程一直循环上面的过程，导致效率降低（不会导致死锁，问题的出现必须需要缓存中的锁被清除，也就是有任务执行完毕）
        do {
            if (mutex4Key != null) {
                mutex4Key.unlock();
            }
            mutex4Key = mutexCache.computeIfAbsent(key, k -> new ReentrantLock());
            mutex4Key.lock();
            mutexInCache = mutexCache.get(key);
        } while (mutexInCache == null || mutex4Key != mutexInCache);

        return mutex4Key;
    }

    /**
     * 释放锁，并在没有等待线程时从缓存中移除锁对象
     *
     * @param key   键
     * @param mutex 锁对象
     */
    private static void releaseLock(String key, ReentrantLock mutex) {
        // 为了避免缓存中的锁无上限增长，需要在无等待队列时将其删除
        if (mutex.getQueueLength() == 0) {
            mutexCache.remove(key);
        }
        mutex.unlock();
    }

    /**
     * 测试方法 - 仅做示例，实际项目中请使用日志而不是打印堆栈
     */
    public static void test(String orderId, int i) {
        SynchronizedByKey.exec(orderId, (s) -> {
            try {
                log.info("线程号：{} 开始处理订单 {}", i, orderId);
                // 模拟业务处理
                log.info("线程号：{} 结束处理订单 {}", i, orderId);
            } catch (Exception e) {
                log.error("线程号：{} 处理订单过程中发生异常: {}", i, orderId, e);
            }
        });
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            int finalI1 = i;
            new Thread(() -> test(String.valueOf(finalI % 2), finalI1), "TestThread_" + i).start();
        }
    }
}
