package com.youlai.boot.shared.file.aliyun;

import com.youlai.boot.shared.file.service.FileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS文件存储自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@ConditionalOnProperty(name = "oss.type", havingValue = "aliyun")
public class AliyunFileAutoConfiguration {

    @Bean
    public FileService aliyunFileService() {
        return new AliyunFileService();
    }
}
