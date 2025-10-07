package com.youlai.boot.shared.sms.aliyun;

import com.youlai.boot.shared.sms.config.AliyunSmsProperties;
import com.youlai.boot.shared.sms.service.SmsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 阿里云短信服务自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@EnableConfigurationProperties(AliyunSmsProperties.class)
@ConditionalOnProperty(name = "aliyun.sms.accessKeyId")
public class AliyunSmsAutoConfiguration {

    @Bean("smsService")
    @Primary
    public SmsService smsService(AliyunSmsProperties aliyunSmsProperties) {
        return new AliyunSmsService(aliyunSmsProperties);
    }
}
