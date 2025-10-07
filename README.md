# Youlai Boot 模块化项目

## 项目结构

这是一个Maven多模块项目，将原有的shared目录拆分为独立的模块，实现按需引入和最小化修改。

### 模块说明

#### 核心模块
- **main**: 主应用模块，包含业务代码和local实现
- **shared-api**: 共享服务接口模块，包含纯接口定义

#### 实现模块（可选）
- **shared-cache-caffeine**: Caffeine缓存实现
- **shared-cache-redis**: Redis缓存实现
- **shared-file-minio**: MinIO文件存储实现
- **shared-file-aliyun**: 阿里云OSS文件存储实现
- **shared-mail-spring**: Spring Mail邮件实现
- **shared-sms-aliyun**: 阿里云短信实现
- **shared-websocket-spring**: Spring WebSocket实现
- **shared-codegen-mybatis**: MyBatis代码生成实现
- **shared-lock-redisson**: Redisson分布式锁实现
- **shared-lock-database**: 数据库分布式锁实现

## 使用方式

### 默认启动
只需要main和shared-api模块即可启动：
```bash
cd main
mvn spring-boot:run
```

### 按需引入外部实现
在main模块的pom.xml中添加需要的实现模块依赖：
```xml
<dependency>
    <groupId>com.youlai</groupId>
    <artifactId>shared-cache-redis</artifactId>
</dependency>
```

## 配置说明

所有配置保持原有格式不变，使用现有的配置前缀：
- 缓存配置：`spring.cache.*`
- 文件存储配置：`oss.*`
- 邮件配置：`spring.mail.*`
- 短信配置：`aliyun.sms.*`

## 特性

1. **最小化修改**: 保持现有配置和API不变
2. **向后兼容**: 确保现有功能正常工作
3. **按需引入**: 支持按需引入外部依赖模块
4. **默认可用**: 默认情况下只需要main和shared-api即可启动
5. **配置不变**: 使用现有的配置格式和前缀

## 开发状态

- [x] 阶段1：项目结构重构
  - [x] 创建Maven多模块结构
  - [x] 迁移接口到shared-api模块
  - [x] 重构main模块结构
  - [x] 创建No-Op实现
- [ ] 阶段2：外部依赖模块创建
- [ ] 阶段3：配置和依赖管理
- [ ] 阶段4：测试和验证