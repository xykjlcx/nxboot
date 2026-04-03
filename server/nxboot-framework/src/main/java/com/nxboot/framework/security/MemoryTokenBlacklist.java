package com.nxboot.framework.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于内存的 Token 黑名单实现。
 * 适用于单实例部署。多实例部署请切换为 Redis 实现。
 * <p>
 * 通过 nxboot.security.token-blacklist=memory 启用（默认）。
 */
@Component
@ConditionalOnProperty(name = "nxboot.security.token-blacklist", havingValue = "memory", matchIfMissing = true)
public class MemoryTokenBlacklist implements TokenBlacklist {

    // key = token, value = 过期时间戳（用于自动清理）
    private final ConcurrentMap<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void add(String token, long expireAtMillis) {
        blacklist.put(token, expireAtMillis);
        cleanup();
    }

    @Override
    public boolean contains(String token) {
        Long expireAt = blacklist.get(token);
        if (expireAt == null) return false;
        if (System.currentTimeMillis() > expireAt) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /** 清理过期条目 */
    private void cleanup() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> now > e.getValue());
    }
}
