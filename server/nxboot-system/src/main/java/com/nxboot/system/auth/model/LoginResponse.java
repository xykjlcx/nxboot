package com.nxboot.system.auth.model;

/**
 * 登录响应
 */
public record LoginResponse(
        String token,
        Long userId,
        String username,
        String nickname
) {
}
