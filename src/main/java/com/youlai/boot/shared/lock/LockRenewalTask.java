package com.youlai.boot.shared.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 锁续约任务
 * <p>
 * 负责自动续约锁的过期时间，防止长时间运行的任务被意外中断
 *
 * @author hjz
 * @since 2025/3/11
 */
@Slf4j
public class LockRenewalTask implements Runnable {
    
    private final String key;
    private final String holder;
    private final JdbcTemplate jdbcTemplate;
    private final String renewalSql;
    
    public LockRenewalTask(String key, String holder, JdbcTemplate jdbcTemplate, String renewalSql) {
        this.key = key;
        this.holder = holder;
        this.jdbcTemplate = jdbcTemplate;
        this.renewalSql = renewalSql;
    }
    
    @Override
    public void run() {
        try {
            // 检查锁是否仍然被当前持有者持有
            String checkSql = "SELECT COUNT(*) FROM database_lock WHERE resource_id = ? AND holder = ? AND expire_time > NOW()";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, key, holder);
            
            if (count == null || count == 0) {
                log.debug("锁 {} 已不再被持有者 {} 持有，停止续约", key, holder);
                return;
            }
            
            // 续约锁的过期时间（延长10秒）
            LocalDateTime newExpireTime = LocalDateTime.now().plus(10, ChronoUnit.SECONDS);
            int updated = jdbcTemplate.update(renewalSql, newExpireTime, key, holder);
            
            if (updated > 0) {
                log.debug("成功续约锁 {}，新过期时间: {}", key, newExpireTime);
            } else {
                log.warn("续约锁 {} 失败，可能已被其他线程获取", key);
            }
            
        } catch (Exception e) {
            log.error("续约锁 {} 时发生异常", key, e);
        }
    }
}
