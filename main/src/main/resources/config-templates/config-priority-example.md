# 配置优先级示例

## 配置文件加载顺序

```
1. application-{profile}.yml  (最高优先级)
2. application.yml
3. config-templates/*.yml     (通过 spring.config.import 导入)
```

## 配置覆盖示例

### 场景：使用 MinIO 作为文件存储

#### 方法一：直接修改主配置文件
```yaml
# application-local.yml
oss:
  type: minio  # 直接覆盖默认的 local
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket-name: youlai
```

#### 方法二：使用配置导入
```yaml
# application.yml
spring:
  config:
    import:
      - classpath:config-templates/oss-minio.yml  # 导入MinIO配置

# application-local.yml
oss:
  type: local  # 默认配置
  local:
    storage-path: ./uploads/

# config-templates/oss-minio.yml
oss:
  type: minio  # 会覆盖 application-local.yml 中的配置
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket-name: youlai
```

## 配置合并规则

### 相同配置项：覆盖
```yaml
# 低优先级
oss:
  type: local

# 高优先级
oss:
  type: minio  # 覆盖 local
```

### 不同配置项：合并
```yaml
# 低优先级
oss:
  type: minio
  minio:
    endpoint: http://localhost:9000

# 高优先级
oss:
  minio:
    access-key: minioadmin  # 合并到 minio 配置中
    secret-key: minioadmin
```

## 最佳实践

1. **直接修改主配置文件**：适合常用配置
2. **使用配置导入**：适合可选功能模块
3. **环境分离**：不同环境使用不同配置
4. **配置验证**：启动时检查配置是否正确
