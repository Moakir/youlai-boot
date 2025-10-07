# 后端模块化架构说明

## 项目结构概览

```
server/
├── pom.xml                           # 父POM，管理所有子模块
├── main/                             # 主应用模块
│   ├── pom.xml                       # 主模块POM
│   └── src/main/java/com/youlai/boot/
│       ├── shared/                   # 本地实现和No-Op实现
│       │   ├── cache/                # 本地缓存实现
│       │   │   ├── LocalCacheServiceImpl.java
│       │   │   ├── CacheAdapter.java
│       │   │   └── core/             # 缓存核心类
│       │   ├── file/                 # 本地文件存储实现
│       │   │   └── service/impl/
│       │   │       └── LocalFileService.java
│       │   ├── lock/                 # 本地锁实现
│       │   │   ├── LocalLockServiceImpl.java
│       │   │   └── LockAdapter.java
│       │   ├── mail/                 # No-Op邮件实现
│       │   │   └── service/impl/
│       │   │       └── NoOpMailServiceImpl.java
│       │   ├── sms/                  # No-Op短信实现
│       │   │   └── service/impl/
│       │   │       └── NoOpSmsServiceImpl.java
│       └── [其他业务代码...]
│   └── src/main/java/com/youlai/boot/shared/websocket/  # WebSocket模块（保留在主项目中）
│       ├── controller/WebsocketController.java
│       └── model/ChatMessage.java
├── shared-api/                       # 共享API模块
│   ├── pom.xml                       # API模块POM
│   └── src/main/java/com/youlai/boot/shared/
│       ├── cache/                    # 缓存接口
│       │   ├── ICacheService.java
│       │   └── CacheAdapter.java
│       ├── file/                     # 文件存储接口
│       │   ├── FileService.java
│       │   └── model/FileInfo.java
│       ├── lock/                     # 分布式锁接口
│       │   ├── ILockService.java
│       │   └── LockAdapter.java
│       ├── mail/                     # 邮件服务接口
│       │   └── MailService.java
│       ├── sms/                      # 短信服务接口
│       │   └── SmsService.java
├── shared-cache-redis/               # Redis缓存实现模块
│   ├── pom.xml
│   └── src/main/java/com/youlai/boot/shared/cache/redis/
│       ├── RedisCacheServiceImpl.java
│       └── RedisCacheAutoConfiguration.java
├── shared-file-minio/                # MinIO文件存储实现模块
│   ├── pom.xml
│   └── src/main/java/com/youlai/boot/shared/file/minio/
│       ├── MinioFileService.java
│       └── MinioFileAutoConfiguration.java
├── shared-file-aliyun/               # 阿里云OSS文件存储实现模块
│   ├── pom.xml
│   └── src/main/java/com/youlai/boot/shared/file/aliyun/
│       ├── AliyunFileService.java
│       └── AliyunFileAutoConfiguration.java
├── shared-mail-spring/               # Spring Mail邮件实现模块
│   ├── pom.xml
│   └── src/main/java/com/youlai/boot/shared/mail/spring/
│       ├── MailServiceImpl.java
│       └── MailSpringAutoConfiguration.java
├── shared-sms-aliyun/                # 阿里云短信实现模块
│   ├── pom.xml
│   └── src/main/java/com/youlai/boot/shared/sms/aliyun/
│       ├── AliyunSmsService.java
│       └── AliyunSmsAutoConfiguration.java
├── shared-codegen-mybatis/           # 代码生成完整模块（独立模块）
│   ├── pom.xml
│   └── src/main/java/com/youlai/boot/shared/codegen/
│       ├── controller/CodegenController.java
│       ├── service/CodegenService.java
│       ├── mapper/DatabaseMapper.java
│       ├── model/entity/GenConfig.java
│       ├── enums/FormTypeEnum.java
│       ├── converter/CodegenConverter.java
│       └── CodegenAutoConfiguration.java
├── shared-lock-redisson/             # Redisson分布式锁实现模块
│   ├── pom.xml
│   └── src/main/java/com/youlai/boot/shared/lock/redisson/
│       ├── RedisLockServiceImpl.java
│       └── RedissonLockAutoConfiguration.java
└── shared-lock-database/             # 数据库分布式锁实现模块
    ├── pom.xml
    └── src/main/java/com/youlai/boot/shared/lock/database/
        ├── DatabaseLockServiceImpl.java
        └── DatabaseLockAutoConfiguration.java
```

## 模块职责说明

### 核心模块
- **main**: 主应用模块，包含业务代码、本地实现和No-Op实现
- **shared-api**: 纯接口定义模块，所有其他模块都依赖此模块

### 本地实现模块（在main中）
- **缓存**: `LocalCacheServiceImpl` - 基于Caffeine的本地缓存
- **文件存储**: `LocalFileService` - 本地文件系统存储
- **分布式锁**: `LocalLockServiceImpl` - 基于Caffeine的本地锁

### No-Op实现模块（在main中）
- **邮件**: `NoOpMailServiceImpl` - 仅记录日志的邮件服务
- **短信**: `NoOpSmsServiceImpl` - 仅记录日志的短信服务
- **WebSocket**: `NoOpWebSocketServiceImpl` - 仅记录日志的WebSocket服务
- **代码生成**: `NoOpCodegenServiceImpl` - 仅记录日志的代码生成服务

### 外部依赖实现模块
- **shared-cache-redis**: Redis缓存实现
- **shared-file-minio**: MinIO文件存储实现
- **shared-file-aliyun**: 阿里云OSS文件存储实现
- **shared-mail-spring**: Spring Mail邮件实现
- **shared-sms-aliyun**: 阿里云短信实现
- **shared-lock-redisson**: Redisson分布式锁实现
- **shared-lock-database**: 数据库分布式锁实现

### 独立业务模块
- **shared-codegen-mybatis**: 代码生成完整模块（包含Controller、Service、Mapper、Model等完整功能）

### 主项目内置模块
- **websocket**: WebSocket模块（基于Spring WebSocket，保留在主项目中）

## 配置说明

### 默认配置（仅main + shared-api）
```yaml
# application.yml
spring:
  cache:
    type: caffeine  # 使用本地缓存
  websocket:
    enabled: true   # 启用WebSocket（基于Spring WebSocket）

oss:
  type: local      # 使用本地文件存储

lock:
  type: local      # 使用本地锁

# 代码生成模块默认禁用（独立模块）
codegen:
  enabled: false
```

### 引入外部模块后的配置
```yaml
# application.yml
spring:
  cache:
    type: redis     # 切换到Redis缓存
  mail:
    host: smtp.example.com
    username: user@example.com
    password: password

oss:
  type: minio       # 切换到MinIO
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket-name: youlai

lock:
  type: redis       # 切换到Redis锁

codegen:
  enabled: true     # 启用代码生成模块

aliyun:
  sms:
    accessKeyId: your-access-key-id
    accessKeySecret: your-access-key-secret
```

## 使用方式

### 1. 默认启动（最小依赖）
```xml
<!-- main/pom.xml 中只需要 -->
<dependency>
    <groupId>com.youlai</groupId>
    <artifactId>shared-api</artifactId>
</dependency>
```

### 2. 按需引入外部模块
```xml
<!-- 引入Redis缓存 -->
<dependency>
    <groupId>com.youlai</groupId>
    <artifactId>shared-cache-redis</artifactId>
</dependency>

<!-- 引入MinIO文件存储 -->
<dependency>
    <groupId>com.youlai</groupId>
    <artifactId>shared-file-minio</artifactId>
</dependency>

<!-- 引入Spring Mail -->
<dependency>
    <groupId>com.youlai</groupId>
    <artifactId>shared-mail-spring</artifactId>
</dependency>
```

### 3. 自动装配
所有实现模块都通过Spring Boot的自动配置机制进行装配：
- 使用`@ConditionalOnProperty`根据配置选择实现
- 使用`@ConditionalOnMissingBean`确保外部实现优先于No-Op实现
- 通过`META-INF/spring.factories`注册自动配置类

## 优势

1. **最小化修改**: 现有配置和API保持不变
2. **按需引入**: 可以按需引入外部依赖模块
3. **默认可用**: 默认情况下只需要main和shared-api即可启动
4. **向后兼容**: 现有功能在模块化后正常工作
5. **配置驱动**: 通过配置文件控制具体实现的选择
6. **松耦合**: 接口与实现分离，便于扩展和维护
