package com.nxboot.system.auth.model;

import java.time.LocalDateTime;

/**
 * 登录日志视图对象
 */
public record LoginLogVO(
        Long id,
        String username,
        String ip,
        String userAgent,
        Integer status,
        String message,
        LocalDateTime loginTime
) {
}
