package com.youlai.boot.shared.mail.spring;

import com.youlai.boot.shared.mail.config.MailProperties;
import com.youlai.boot.shared.mail.service.MailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Spring Mail邮件服务自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
@ConditionalOnProperty(name = "spring.mail.host")
public class MailSpringAutoConfiguration {

    @Bean("mailService")
    @Primary
    public MailService mailService(JavaMailSender mailSender, MailProperties mailProperties) {
        return new MailServiceImpl(mailSender, mailProperties);
    }
}
