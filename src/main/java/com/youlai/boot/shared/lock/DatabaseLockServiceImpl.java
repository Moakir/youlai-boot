package com.youlai.boot.shared.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 数据库锁实现
 * <p>
 * 使用数据库的 INSERT ... ON DUPLICATE KEY UPDATE 机制实现分布式锁
 * 支持可重入锁、自动续约机制和精确的锁释放
 * 适用于没有Redis但需要分布式锁的场景
 *
 * @author hejz
 * @since 2025/9/27
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "lock.type", havingValue = "database")
@EnableConfigurationProperties(DatabaseLockProperties.class)
public class DatabaseLockServiceImpl implements ILockService, InitializingBean, DisposableBean {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseLockProperties properties;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> renewalTasks = new ConcurrentHashMap<>();
    
    // 使用ThreadLocal存储当前线程的holder信息，优化unlock性能
    private static final ThreadLocal<String> CURRENT_HOLDER = new ThreadLocal<>();

    public DatabaseLockServiceImpl(JdbcTemplate jdbcTemplate, DatabaseLockProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
        this.scheduler = java.util.concurrent.Executors.newScheduledThreadPool(10, r -> {
            Thread t = new Thread(r, "database-lock-renewal-");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void afterPropertiesSet() {
        try {
            // 创建锁表（如果不存在）
            jdbcTemplate.execute(getCreateTableSql());
            log.info("数据库锁服务启动成功，使用数据库作为锁实现，表名: {}", properties.getTableName());
        } catch (Exception e) {
            log.warn("自动创建锁表失败，请确保表 {} 已存在: {}", properties.getTableName(), e.getMessage());
            // 不抛出异常，允许服务继续启动，但记录警告
        }
    }

    @Override
    public void destroy() {
        // 停止所有续约任务
        renewalTasks.values().forEach(task -> task.cancel(true));
        renewalTasks.clear();
        scheduler.shutdown();
        log.info("数据库锁服务已关闭");
    }

    // 创建锁表的SQL
    private String getCreateTableSql() {
        return String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                resource_id VARCHAR(64) NOT NULL COMMENT '锁定的资源标识',
                holder VARCHAR(64) NOT NULL COMMENT '资源的持有者标识',
                lock_count INT(11) NOT NULL DEFAULT 1 COMMENT '锁定的次数，可重复获取锁后会累加，到0会释放锁',
                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                expire_time DATETIME NOT NULL COMMENT '过期时间',
                description VARCHAR(256) DEFAULT '' COMMENT '描述',
                PRIMARY KEY `uiq_idx_resource` (`resource_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库分布式锁表'
            """, properties.getTableName());
    }

    // 尝试获取锁的SQL - 支持可重入
    private String getTryLockSql() {
        return String.format("""
            INSERT INTO %s (resource_id, holder, lock_count, expire_time) 
            VALUES (?, ?, 1, ?)
            ON DUPLICATE KEY UPDATE 
            holder = CASE 
                WHEN expire_time < NOW() THEN VALUES(holder)
                WHEN holder = VALUES(holder) THEN holder
                ELSE holder 
            END,
            lock_count = CASE 
                WHEN expire_time < NOW() THEN 1
                WHEN holder = VALUES(holder) THEN lock_count + 1
                ELSE lock_count 
            END,
            expire_time = CASE 
                WHEN expire_time < NOW() THEN VALUES(expire_time)
                WHEN holder = VALUES(holder) THEN VALUES(expire_time)
                ELSE expire_time 
            END
            """, properties.getTableName());
    }

    // 检查锁是否被当前持有者持有
    private String getCheckHolderSql() {
        return String.format("""
            SELECT COUNT(*) FROM %s 
            WHERE resource_id = ? AND holder = ? AND expire_time > NOW()
            """, properties.getTableName());
    }

    // 释放锁的SQL - 支持锁计数递减
    private String getUnlockSql() {
        return String.format("""
            UPDATE %s 
            SET lock_count = lock_count - 1 
            WHERE resource_id = ? AND holder = ? AND lock_count > 0
            """, properties.getTableName());
    }

    // 删除锁记录的SQL
    private String getDeleteLockSql() {
        return String.format("""
            DELETE FROM %s 
            WHERE resource_id = ? AND holder = ? AND lock_count <= 0
            """, properties.getTableName());
    }

    // 续约锁的SQL
    private String getRenewalSql() {
        return String.format("""
            UPDATE %s 
            SET expire_time = ? 
            WHERE resource_id = ? AND holder = ? AND expire_time > NOW()
            """, properties.getTableName());
    }

    // 清理过期锁的SQL
    private String getCleanupSql() {
        return String.format("""
            DELETE FROM %s 
            WHERE expire_time < NOW()
            """, properties.getTableName());
    }


    @Override
    @Transactional
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        LockHolder lockHolder = new LockHolder();
        String holder = lockHolder.getHolder();
        
        LocalDateTime expireTimeDateTime = LocalDateTime.now().plus(expireTime, 
            switch (timeUnit) {
                case NANOSECONDS -> ChronoUnit.NANOS;
                case MICROSECONDS -> ChronoUnit.MICROS;
                case MILLISECONDS -> ChronoUnit.MILLIS;
                case SECONDS -> ChronoUnit.SECONDS;
                case MINUTES -> ChronoUnit.MINUTES;
                case HOURS -> ChronoUnit.HOURS;
                case DAYS -> ChronoUnit.DAYS;
            });

        try {
            int updatedRows = jdbcTemplate.update(getTryLockSql(), key, holder, expireTimeDateTime);
            
            if (updatedRows > 0) {
                // 检查是否成功获取锁
                Integer count = jdbcTemplate.queryForObject(getCheckHolderSql(), Integer.class, key, holder);
                if (count != null && count > 0) {
                    // 存储holder信息到ThreadLocal，优化unlock性能
                    CURRENT_HOLDER.set(holder);
                    // 启动续约任务
                    startRenewalTask(key, holder);
                    log.debug("成功获取数据库锁: {}, holder: {}", key, holder);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("获取数据库锁失败: {}", key, e);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        try {
            // 优先使用ThreadLocal中存储的holder信息，避免数据库查询
            String holder = CURRENT_HOLDER.get();
            if (holder != null) {
                // 使用ThreadLocal中的holder信息直接释放锁
                if (releaseLock(key, holder)) {
                    // 释放成功后清除ThreadLocal
                    CURRENT_HOLDER.remove();
                    log.debug("使用ThreadLocal holder成功释放锁: {}, holder: {}", key, holder);
                    return;
                }
            }
            
            // 如果ThreadLocal中没有holder信息，回退到数据库查询方式
            // 这种情况通常发生在RepeatSubmit场景或异常情况下
            String currentThreadId = String.valueOf(Thread.currentThread().getId());
            String findHolderSql = String.format("""
                SELECT holder FROM %s 
                WHERE resource_id = ? AND holder LIKE ? AND expire_time > NOW()
                """, properties.getTableName());
            
            holder = jdbcTemplate.queryForObject(findHolderSql, String.class, key, "%:" + currentThreadId);
            if (holder != null) {
                releaseLock(key, holder);
                log.debug("通过数据库查询成功释放锁: {}, holder: {}", key, holder);
            } else {
                // 如果找不到当前线程的锁，可能是RepeatSubmit场景下的自然过期
                // 这种情况下不需要特殊处理，让锁自然过期即可
                log.debug("未找到当前线程持有的锁: {}, 可能已自然过期", key);
            }
        } catch (Exception e) {
            log.warn("释放数据库锁失败: {}", key, e);
        } finally {
            // 确保ThreadLocal被清理，避免内存泄漏
            CURRENT_HOLDER.remove();
        }
    }

    @Override
    public boolean isLocked(String key) {
        try {
            String checkSql = String.format("""
                SELECT COUNT(*) FROM %s 
                WHERE resource_id = ? AND expire_time > NOW()
                """, properties.getTableName());
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, key);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("检查数据库锁状态失败: {}", key, e);
            return false;
        }
    }

    /**
     * 启动续约任务
     */
    private void startRenewalTask(String key, String holder) {
        // 停止现有的续约任务
        stopRenewalTask(key);
        
        // 启动新的续约任务
        LockRenewalTask renewalTask = new LockRenewalTask(key, holder, jdbcTemplate, getRenewalSql());
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(
            renewalTask,
            properties.getRenewalInterval().toSeconds(),
            properties.getRenewalInterval().toSeconds(),
            TimeUnit.SECONDS
        );
        renewalTasks.put(key, task);
        log.debug("启动锁续约任务: {}, holder: {}", key, holder);
    }

    /**
     * 停止续约任务
     */
    private void stopRenewalTask(String key) {
        ScheduledFuture<?> task = renewalTasks.remove(key);
        if (task != null) {
            task.cancel(true);
            log.debug("停止锁续约任务: {}", key);
        }
    }

    /**
     * 释放锁（支持锁计数递减）
     */
    private boolean releaseLock(String key, String holder) {
        try {
            // 锁计数递减
            int updated = jdbcTemplate.update(getUnlockSql(), key, holder);
            if (updated > 0) {
                // 检查锁计数是否为0，如果是则删除记录
                String checkCountSql = String.format("""
                    SELECT lock_count FROM %s 
                    WHERE resource_id = ? AND holder = ?
                    """, properties.getTableName());
                Integer lockCount = jdbcTemplate.queryForObject(checkCountSql, Integer.class, key, holder);
                
                if (lockCount != null && lockCount <= 0) {
                    jdbcTemplate.update(getDeleteLockSql(), key, holder);
                    log.debug("锁计数为0，删除锁记录: {}, holder: {}", key, holder);
                }
                
                // 停止续约任务
                stopRenewalTask(key);
                log.debug("成功释放数据库锁: {}, holder: {}", key, holder);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("释放数据库锁失败: {}, holder: {}", key, holder, e);
            return false;
        }
    }

    /**
     * 清理过期的锁（可定期调用）
     */
    public void cleanupExpiredLocks() {
        try {
            int deletedCount = jdbcTemplate.update(getCleanupSql());
            if (deletedCount > 0) {
                log.debug("清理了 {} 个过期的数据库锁", deletedCount);
            }
        } catch (Exception e) {
            log.warn("清理过期锁失败", e);
        }
    }

    /**
     * 获取锁的详细信息
     */
    public String getLockInfo(String key) {
        try {
            String infoSql = String.format("""
                SELECT resource_id, holder, lock_count, create_time, expire_time, description 
                FROM %s 
                WHERE resource_id = ? AND expire_time > NOW()
                """, properties.getTableName());
            
            return jdbcTemplate.queryForObject(infoSql, (rs, rowNum) -> 
                String.format("Lock{resourceId='%s', holder='%s', lockCount=%d, createTime=%s, expireTime=%s, description='%s'}", 
                    rs.getString("resource_id"),
                    rs.getString("holder"),
                    rs.getInt("lock_count"),
                    rs.getTimestamp("create_time"),
                    rs.getTimestamp("expire_time"),
                    rs.getString("description")
                ), key);
        } catch (Exception e) {
            log.warn("获取锁信息失败: {}", key, e);
            return null;
        }
    }

    /**
     * 为RepeatSubmit场景优化的锁释放方法
     * <p>
     * 在RepeatSubmit场景下，通常不需要主动释放锁，让锁自然过期即可
     * 但为了支持可重入锁的正确释放，提供此方法
     */
    public void unlockForRepeatSubmit(String key) {
        try {
            // 查找当前线程持有的锁
            String currentThreadId = String.valueOf(Thread.currentThread().getId());
            String findHolderSql = String.format("""
                SELECT holder FROM %s 
                WHERE resource_id = ? AND holder LIKE ? AND expire_time > NOW()
                """, properties.getTableName());
            
            String holder = jdbcTemplate.queryForObject(findHolderSql, String.class, key, "%:" + currentThreadId);
            if (holder != null) {
                releaseLock(key, holder);
                log.debug("RepeatSubmit场景下成功释放锁: {}, holder: {}", key, holder);
            } else {
                log.debug("RepeatSubmit场景下未找到当前线程持有的锁: {}, 可能已自然过期", key);
            }
        } catch (Exception e) {
            log.warn("RepeatSubmit场景下释放锁失败: {}", key, e);
        }
    }
}
