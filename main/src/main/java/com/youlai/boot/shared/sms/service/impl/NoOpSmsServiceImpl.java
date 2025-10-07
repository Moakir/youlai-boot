package com.youlai.boot.shared.sms.service.impl;

import com.youlai.boot.shared.sms.enums.SmsTypeEnum;
import com.youlai.boot.shared.sms.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 短信服务 No-Op 实现
 * 当没有引入 shared-sms-aliyun 模块时使用此实现
 *
 * @author youlai
 * @since 3.0.0
 */
@Service
@ConditionalOnMissingBean(name = "smsService")
@Slf4j
public class NoOpSmsServiceImpl implements SmsService {

    @Override
    public boolean sendSms(String mobile, SmsTypeEnum smsType, Map<String, String> templateParams) {
        log.info("模拟发送短信 - 手机号: {}, 短信类型: {}, 模板参数: {}", mobile, smsType, templateParams);
        return true; // 模拟发送成功
    }
}
