package com.youlai.boot.shared.mail.service.impl;

import com.youlai.boot.shared.mail.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * 邮件服务 No-Op 实现
 * 当没有引入 shared-mail-spring 模块时使用此实现
 *
 * @author youlai
 * @since 3.0.0
 */
@Service
@ConditionalOnMissingBean(name = "mailService")
@Slf4j
public class NoOpMailServiceImpl implements MailService {

    @Override
    public void sendMail(String to, String subject, String text) {
        log.info("模拟发送邮件 - 收件人: {}, 主题: {}, 内容: {}", to, subject, text);
    }

    @Override
    public void sendMailWithAttachment(String to, String subject, String text, String filePath) {
        log.info("模拟发送带附件邮件 - 收件人: {}, 主题: {}, 内容: {}, 附件: {}", to, subject, text, filePath);
    }
}
