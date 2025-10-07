# 统一锁服务架构

## 概述

本模块提供了统一的锁服务接口，支持多种锁实现方式，适用于不同的部署场景。

## 架构设计

### 核心组件

1. **ILockService** - 统一锁服务接口
2. **LocalLockServiceImpl** - 本地锁实现（基于Caffeine）
3. **RedisLockServiceImpl** - Redis分布式锁实现（基于Redisson）
4. **DatabaseLockServiceImpl** - 数据库锁实现
5. **LockAdapter** - 锁服务适配器

### 自动选择机制

通过 `@ConditionalOnProperty` 注解根据配置自动选择实现：

```yaml
lock:
  type: local  # local, redis, database
```

## 使用方式

### 1. 在业务代码中使用

```java
@Service
public class UserService {
    
    @Resource
    private LockAdapter lockAdapter;
    
    public void processUser(Long userId) {
        String lockKey = "user:process:" + userId;
        
        if (lockAdapter.tryLock(lockKey, 30, TimeUnit.SECONDS)) {
            try {
                // 业务逻辑
                doProcessUser(userId);
            } finally {
                lockAdapter.unlock(lockKey);
            }
        } else {
            throw new BusinessException("用户正在处理中，请稍后再试");
        }
    }
}
```

### 2. 在AOP中使用

```java
@RepeatSubmit(expire = 10) // 10秒内防重复提交
@PostMapping("/submit")
public Result submit(@RequestBody SubmitRequest request) {
    // 业务逻辑
    return Result.success();
}
```

## 模块化拆分

### 模块结构

```
youlai-boot-lock/
├── youlai-lock-api/           # 接口模块
│   ├── ILockService.java
│   └── LockAdapter.java
├── youlai-lock-local/         # 本地锁模块
│   └── LocalLockServiceImpl.java
├── youlai-lock-redis/         # Redis锁模块
│   └── RedisLockServiceImpl.java
└── youlai-lock-database/      # 数据库锁模块
    └── DatabaseLockServiceImpl.java
```

### 依赖配置

**业务模块的 pom.xml：**
```xml
<dependencies>
    <!-- 只引入需要的锁实现 -->
    <dependency>
        <groupId>com.youlai.boot</groupId>
        <artifactId>youlai-lock-local</artifactId>
    </dependency>
    
    <!-- 或者引入Redis锁 -->
    <dependency>
        <groupId>com.youlai.boot</groupId>
        <artifactId>youlai-lock-redis</artifactId>
    </dependency>
</dependencies>
```

## 配置说明

### 本地锁配置
```yaml
lock:
  type: local
  local:
    max-size: 10000  # 最大缓存条目数
```

### Redis锁配置
```yaml
lock:
  type: redis
  # 使用现有的 spring.data.redis 配置
```

### 数据库锁配置
```yaml
lock:
  type: database
  database:
    table-name: database_lock
    default-expire-time: 30s
    renewal-interval: 10s
    cleanup-interval: 60s
```

## 性能对比

| 锁类型 | 适用场景 | 性能 | 分布式支持 | 依赖 |
|--------|----------|------|------------|------|
| 本地锁 | 单机应用 | 最高 | ❌ | Caffeine |
| Redis锁 | 分布式应用 | 高 | ✅ | Redis + Redisson |
| 数据库锁 | 无Redis环境 | 中等 | ✅ | 数据库 |

## 数据库锁改进功能

### 新增特性

1. **可重入锁支持**：同一线程可以多次获取同一个锁，避免死锁
2. **自动续约机制**：长时间运行的任务会自动续约锁的过期时间
3. **精确锁释放**：支持锁计数递减，只有计数为0时才真正释放锁
4. **标准表结构**：使用标准的数据库表结构，支持完整的锁信息管理

### 数据库表结构

#### 方式一：使用SQL脚本（推荐生产环境）

在 `server/sql/mysql/youlai_boot.sql` 中已包含建表语句：

```sql
CREATE TABLE `database_lock` (
  `resource_id` varchar(64) NOT NULL COMMENT '锁定的资源标识',
  `holder` varchar(64) NOT NULL COMMENT '资源的持有者标识',
  `lock_count` int(11) NOT NULL DEFAULT 1 COMMENT '锁定的次数，可重复获取锁后会累加，到0会释放锁',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `description` varchar(256) DEFAULT '' COMMENT '描述',
  PRIMARY KEY `uiq_idx_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库分布式锁表';
```

#### 方式二：自动建表（开发环境）

应用启动时会自动创建表，支持可配置的表名：

```yaml
lock:
  database:
    table-name: database_lock  # 可配置表名
```

**注意**：如果自动建表失败，请确保数据库用户有CREATE TABLE权限，或手动执行SQL脚本。

### 使用示例

```java
@Service
public class OrderService {
    
    @Resource
    private LockAdapter lockAdapter;
    
    public void processOrder(Long orderId) {
        String lockKey = "order:process:" + orderId;
        
        if (lockAdapter.tryLock(lockKey, 30, TimeUnit.SECONDS)) {
            try {
                // 业务逻辑 - 支持嵌套调用
                doProcessOrder(orderId);
            } finally {
                lockAdapter.unlock(lockKey);
            }
        } else {
            throw new BusinessException("订单正在处理中，请稍后再试");
        }
    }
    
    private void doProcessOrder(Long orderId) {
        // 嵌套调用也会成功获取锁（可重入）
        String lockKey = "order:process:" + orderId;
        if (lockAdapter.tryLock(lockKey, 30, TimeUnit.SECONDS)) {
            try {
                // 嵌套业务逻辑
                updateOrderStatus(orderId);
            } finally {
                lockAdapter.unlock(lockKey);
            }
        }
    }
}
```

### 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `table-name` | `database_lock` | 锁表名 |
| `default-expire-time` | `30s` | 默认锁过期时间 |
| `renewal-interval` | `10s` | 续约间隔时间 |
| `cleanup-interval` | `60s` | 清理过期锁间隔 |

### 工作原理

1. **获取锁**：使用`INSERT ... ON DUPLICATE KEY UPDATE`确保原子性
2. **可重入**：同一线程多次获取锁时，`lock_count`递增
3. **自动续约**：获取锁后启动守护线程，定期续约过期时间
4. **释放锁**：每次释放时`lock_count`递减，为0时删除记录
5. **异常处理**：续约失败时自动停止，锁会在过期时间后自动释放

### RepeatSubmit场景优化

针对防重复提交场景，数据库锁实现进行了特殊优化：

1. **自然过期机制**：RepeatSubmitAspect通常让锁自然过期，不主动释放
2. **智能释放**：如果检测到当前线程持有锁，会正确释放；否则让锁自然过期
3. **性能优化**：避免不必要的锁释放操作，提高防重复提交的性能
4. **兼容性**：完全兼容现有的RepeatSubmitAspect实现

```java
// RepeatSubmitAspect中的使用方式
@RepeatSubmit(expire = 5) // 5秒内防重复提交
@PostMapping("/submit")
public Result submit(@RequestBody Request request) {
    // 业务逻辑
    return Result.success();
}
```

## 注意事项

1. **本地锁**：仅适用于单机环境，不支持分布式
2. **Redis锁**：需要Redis服务，支持分布式
3. **数据库锁**：性能相对较低，但无需额外依赖，支持可重入和自动续约
4. **锁释放**：建议让锁自然过期，避免手动释放时的并发问题
5. **死锁预防**：设置合理的过期时间，避免死锁
6. **可重入锁**：同一线程多次获取锁时，必须对应多次释放
7. **续约机制**：长时间运行的任务会自动续约，无需手动处理
