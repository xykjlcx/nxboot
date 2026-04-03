package com.nxboot.framework.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Redis 的 Token 黑名单实现。
 * 适用于多实例部署，token 过期后 Redis 自动清理。
 * <p>
 * 通过 nxboot.security.token-blacklist=redis 启用。
 */
@Component
@ConditionalOnProperty(name = "nxboot.security.token-blacklist", havingValue = "redis")
public class RedisTokenBlacklist implements TokenBlacklist {

    private static final String KEY_PREFIX = "nxboot:token:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklist(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(String token, long expireAtMillis) {
        long ttlMillis = expireAtMillis - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            // token 已过期，无需加入黑名单
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean contains(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + token));
    }
}
