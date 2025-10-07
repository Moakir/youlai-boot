package com.youlai.boot.auth.wechat.config;

import com.youlai.boot.auth.wechat.security.WxMiniAppCodeAuthenticationProvider;
import com.youlai.boot.auth.wechat.security.WxMiniAppPhoneAuthenticationProvider;
import com.youlai.boot.shared.user.service.UserService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信认证自动配置
 *
 * @author Ray
 * @since 2.10.0
 */
@Configuration
@ConditionalOnProperty(prefix = "wx", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WechatAuthAutoConfiguration {

    @Bean
    public WxMiniAppCodeAuthenticationProvider wxMiniAppCodeAuthenticationProvider(
            UserService userService, 
            WxMaService wxMaService) {
        return new WxMiniAppCodeAuthenticationProvider(userService, wxMaService);
    }

    @Bean
    public WxMiniAppPhoneAuthenticationProvider wxMiniAppPhoneAuthenticationProvider(
            UserService userService, 
            WxMaService wxMaService) {
        return new WxMiniAppPhoneAuthenticationProvider(userService, wxMaService);
    }
}
