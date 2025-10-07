package com.youlai.boot.shared.file.minio;

import com.youlai.boot.shared.file.service.FileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO文件存储自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@ConditionalOnProperty(name = "oss.type", havingValue = "minio")
public class MinioFileAutoConfiguration {

    @Bean
    public FileService minioFileService() {
        return new MinioFileService();
    }
}
