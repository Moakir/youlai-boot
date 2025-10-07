package com.youlai.boot.shared.codegen;

import com.youlai.boot.shared.codegen.service.CodegenService;
import com.youlai.boot.shared.codegen.service.GenConfigService;
import com.youlai.boot.shared.codegen.service.GenFieldConfigService;
import com.youlai.boot.shared.codegen.service.impl.CodegenServiceImpl;
import com.youlai.boot.shared.codegen.service.impl.GenConfigServiceImpl;
import com.youlai.boot.shared.codegen.service.impl.GenFieldConfigServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 代码生成服务自动配置
 *
 * @author youlai
 * @since 3.0.0
 */
@Configuration
@ConditionalOnProperty(name = "codegen.enabled", havingValue = "true")
public class CodegenAutoConfiguration {

    @Bean
    public CodegenService codegenService() {
        return new CodegenServiceImpl();
    }

    @Bean
    public GenConfigService genConfigService() {
        return new GenConfigServiceImpl();
    }

    @Bean
    public GenFieldConfigService genFieldConfigService() {
        return new GenFieldConfigServiceImpl();
    }
}
