package com.youlai.boot.auth.wechat.service;

import com.youlai.boot.auth.wechat.dto.WxMiniAppCodeLoginDTO;
import com.youlai.boot.auth.wechat.dto.WxMiniAppPhoneLoginDTO;
import com.youlai.boot.shared.auth.model.AuthenticationToken;

/**
 * 微信认证服务接口
 *
 * @author Ray.Hao
 * @since 2.10.0
 */
public interface WechatAuthService {

    /**
     * 微信一键授权登录
     *
     * @param code 微信登录code
     * @return 访问令牌
     */
    AuthenticationToken loginByWechat(String code);

    /**
     * 微信小程序Code登录
     *
     * @param loginDTO 登录参数
     * @return 访问令牌
     */
    AuthenticationToken loginByWxMiniAppCode(WxMiniAppCodeLoginDTO loginDTO);

    /**
     * 微信小程序手机号登录
     *
     * @param loginDTO 登录参数
     * @return 访问令牌
     */
    AuthenticationToken loginByWxMiniAppPhone(WxMiniAppPhoneLoginDTO loginDTO);
}
