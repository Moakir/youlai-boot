package com.youlai.boot.auth.wechat.controller;

import com.youlai.boot.auth.wechat.dto.WxMiniAppCodeLoginDTO;
import com.youlai.boot.auth.wechat.dto.WxMiniAppPhoneLoginDTO;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.shared.auth.model.AuthenticationToken;
import com.youlai.boot.common.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 微信认证控制层
 *
 * @author Ray.Hao
 * @since 2.10.0
 */
@Tag(name = "微信认证")
@RestController
@RequestMapping("/api/v1/auth/wechat")
@RequiredArgsConstructor
@Slf4j
public class WechatAuthController {

    private final com.youlai.boot.auth.wechat.service.WechatAuthService wechatAuthService;

    @Operation(summary = "微信授权登录(Web)")
    @PostMapping("/login")
    @Log(value = "微信登录", module = LogModuleEnum.LOGIN)
    public Result<AuthenticationToken> loginByWechat(
            @Parameter(description = "微信授权码", example = "code") @RequestParam String code
    ) {
        AuthenticationToken loginResult = wechatAuthService.loginByWechat(code);
        return Result.success(loginResult);
    }

    @Operation(summary = "微信小程序登录(Code)")
    @PostMapping("/miniapp/code-login")
    public Result<AuthenticationToken> loginByWxMiniAppCode(@RequestBody @Valid WxMiniAppCodeLoginDTO loginDTO) {
        AuthenticationToken token = wechatAuthService.loginByWxMiniAppCode(loginDTO);
        return Result.success(token);
    }

    @Operation(summary = "微信小程序登录(手机号)")
    @PostMapping("/miniapp/phone-login")
    public Result<AuthenticationToken> loginByWxMiniAppPhone(@RequestBody @Valid WxMiniAppPhoneLoginDTO loginDTO) {
        AuthenticationToken token = wechatAuthService.loginByWxMiniAppPhone(loginDTO);
        return Result.success(token);
    }
}
