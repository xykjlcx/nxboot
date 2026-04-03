package com.nxboot.framework.oauth2;

/**
 * 三方平台返回的统一用户信息。
 * 所有 Provider 最终都映射到这个 Record。
 */
public record OAuth2UserInfo(
        String providerId,    // 三方平台用户ID
        String provider,      // github/google/wechat
        String username,      // 用户名
        String email,         // 邮箱（可能为空）
        String avatar         // 头像 URL
) {
}
