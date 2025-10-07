# 配置管理指南

## 📋 概述

本项目采用简化的配置管理策略，提供最小配置和最全配置两种模式，便于快速启动和功能扩展。

## 🏗️ 配置结构

```
src/main/resources/
├── application.yml              # 基础配置 (默认使用dev)
├── application-dev.yml          # 开发环境配置 (默认)
├── application-prod.yml         # 生产环境配置
├── application-complete.yml     # 最全配置 (参考用)
└── config-templates/            # 配置模板目录 (已废弃，参考complete配置)
```

## 🚀 快速开始

### 1. 默认启动 (开发环境)
```bash
# 使用开发环境配置启动
mvn spring-boot:run
```

### 2. 生产环境启动
```bash
# 使用生产环境配置
mvn spring-boot:run -Dspring.profiles.active=prod
```

### 3. 最全配置启动 (参考用)
```bash
# 使用最全配置启动，包含所有功能
mvn spring-boot:run -Dspring.profiles.active=complete
```

## 🔧 添加功能模块

### 推荐方法：参考最全配置
1. 打开 `application-complete.yml` 文件
2. 找到需要的功能配置部分
3. 复制相关配置到 `application-dev.yml` 或 `application-prod.yml` 中
4. 重启应用

### 配置导入方式
在 `application-dev.yml` 或 `application-prod.yml` 中添加：
```yaml
spring:
  config:
    import:
      - classpath:config-templates/oss-minio.yml  # 导入MinIO配置
      - classpath:config-templates/cache-redis.yml  # 导入Redis配置
```

## 📝 配置模板说明

### 文件存储 (OSS)
- **oss-minio.yml**: MinIO 对象存储配置
- **oss-aliyun.yml**: 阿里云 OSS 配置
- **默认**: 使用本地文件存储

### 缓存服务
- **cache-redis.yml**: Redis 缓存配置
- **默认**: 使用 Caffeine 本地缓存

### 短信服务
- **sms-aliyun.yml**: 阿里云短信服务配置
- **默认**: 不启用短信服务

### 邮件服务
- **mail-spring.yml**: Spring Mail 配置
- **默认**: 不启用邮件服务

### 分布式锁
- **lock-redis.yml**: Redis 分布式锁
- **lock-database.yml**: 数据库分布式锁
- **默认**: 使用本地锁

### 定时任务
- **job-xxl.yml**: XXL-Job 定时任务配置
- **默认**: 不启用定时任务

### 微信小程序
- **wx-miniapp.yml**: 微信小程序配置
- **默认**: 不启用微信功能

## ⚙️ 环境配置说明

### application-local.yml
- **用途**: 本地开发最小配置
- **特点**: 只包含核心功能，快速启动
- **包含**: 数据库、基础缓存、安全、本地文件存储

### application-dev.yml
- **用途**: 开发环境配置
- **特点**: 基于 local 配置，添加开发特有配置
- **包含**: 调试日志、开发数据库等

### application-prod.yml
- **用途**: 生产环境配置
- **特点**: 基于 dev 配置，添加生产特有配置
- **包含**: 生产数据库、Redis、邮件、短信等

## 🔍 配置验证

### 检查配置是否正确
```bash
# 检查配置语法
mvn spring-boot:run --dry-run

# 查看激活的配置
mvn spring-boot:run -Ddebug
```

### 常见问题
1. **配置不生效**: 检查配置文件名称和路径
2. **模块未加载**: 确认相关依赖已添加到 pom.xml
3. **配置冲突**: 检查是否有重复的配置项

## 📚 最佳实践

1. **最小化原则**: 默认只配置必需的功能
2. **模块化配置**: 按功能模块组织配置
3. **环境分离**: 不同环境使用不同的配置文件
4. **配置模板**: 提供可复用的配置模板
5. **文档完善**: 每个配置项都有详细说明

## 🆘 获取帮助

如果遇到配置问题，请：
1. 查看本文档的常见问题部分
2. 检查 `config-templates/` 目录中的示例配置
3. 参考 Spring Boot 官方文档
4. 提交 Issue 或联系开发团队
