package com.nxboot.framework.security;

/**
 * JWT Token 黑名单接口。
 * 用于强制下线功能——被加入黑名单的 token 会被 JwtAuthenticationFilter 拒绝。
 */
public interface TokenBlacklist {

    /** 将 token 加入黑名单 */
    void add(String token, long expireAtMillis);

    /** 检查 token 是否在黑名单中 */
    boolean contains(String token);
}
