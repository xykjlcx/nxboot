package com.nxboot.system.auth.service;

import com.nxboot.framework.security.JwtTokenProvider;
import com.nxboot.framework.security.TokenBlacklist;
import com.nxboot.system.auth.model.OnlineUser;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 在线用户管理服务。
 * 维护活跃会话映射，支持查询在线用户和强制下线。
 */
@Service
public class OnlineUserService {

    private final ConcurrentMap<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    private final TokenBlacklist tokenBlacklist;
    private final JwtTokenProvider tokenProvider;

    public OnlineUserService(TokenBlacklist tokenBlacklist, JwtTokenProvider tokenProvider) {
        this.tokenBlacklist = tokenBlacklist;
        this.tokenProvider = tokenProvider;
    }

    /** 登录时注册在线用户 */
    public void login(String token, Long userId, String username, String ip, String userAgent) {
        String sessionId = toSessionId(token);
        long expireAt = tokenProvider.getExpirationTime(token);
        OnlineUser user = new OnlineUser(sessionId, userId, username, ip, userAgent, System.currentTimeMillis());
        sessions.put(sessionId, new SessionEntry(token, user, expireAt));
        cleanup();
    }

    /** 获取所有在线用户 */
    public List<OnlineUser> list() {
        cleanup();
        return sessions.values().stream()
                .map(SessionEntry::onlineUser)
                .toList();
    }

    /** 强制下线：将 token 加入黑名单并移除会话 */
    public void forceLogout(String sessionId) {
        SessionEntry entry = sessions.remove(sessionId);
        if (entry != null) {
            tokenBlacklist.add(entry.token(), entry.expireAtMillis());
        }
    }

    /** 清理过期会话（纯时间戳比较，不解析 JWT） */
    private void cleanup() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> now > e.getValue().expireAtMillis());
    }

    /** SHA-256 前 16 字符作为 sessionId，避免 hashCode 碰撞 */
    private static String toSessionId(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 所有 JDK 都支持，不会发生
            throw new RuntimeException(e);
        }
    }

    /** 内部记录：保存原始 token + 过期时间，避免重复解析 JWT */
    private record SessionEntry(String token, OnlineUser onlineUser, long expireAtMillis) {}
}
