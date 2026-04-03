package com.nxboot.system.auth.model;

/**
 * 刷新令牌响应
 */
public record RefreshTokenResponse(
        String token,
        String refreshToken
) {
}
