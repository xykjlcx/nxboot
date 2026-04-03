package com.nxboot.system.log;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 操作日志拦截器注册——在 /api/** 路径上生效。
 */
@Configuration
public class AccessLogWebConfig implements WebMvcConfigurer {

    private final AccessLogInterceptor accessLogInterceptor;

    public AccessLogWebConfig(AccessLogInterceptor accessLogInterceptor) {
        this.accessLogInterceptor = accessLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // order=10 保证在 RateLimitInterceptor 之后执行
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/api/**")
                .order(10);
    }
}
