package com.nxboot.framework.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 *
 * 安全策略：
 * - 未配置 allowed-origins → 不注册跨域规则（仅允许同源访问）
 * - 配置为 * → 允许但打印安全告警
 * - 显式域名列表 → 正常白名单模式
 */
@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${nxboot.cors.allowed-origins:}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            log.info("CORS allowed-origins 未配置，跨域请求将被浏览器同源策略拦截");
            return new CorsFilter(source);
        }

        if ("*".equals(allowedOrigins.trim())) {
            log.warn("CORS allowed-origins 配置为 '*'，生产环境请改为显式域名白名单");
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        for (String origin : allowedOrigins.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                config.addAllowedOriginPattern(trimmed);
            }
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
