package com.nxboot.system.auth.model;

/**
 * 在线用户信息
 */
public record OnlineUser(
    String sessionId,
    Long userId,
    String username,
    String ip,
    String userAgent,
    long loginTime
) {}
