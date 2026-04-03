package com.nxboot.framework.web;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 IP 的请求限流拦截器
 * <p>
 * 使用 ConcurrentHashMap + 定期清理，防止长期运行导致内存泄漏
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    /** 超过 1 小时未访问的条目将被清理 */
    private static final long EXPIRE_MILLIS = 60 * 60 * 1000L;

    /** IP -> BucketEntry 映射（通用限流） */
    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    /** IP -> BucketEntry 映射（登录限流，更严格） */
    private final Map<String, BucketEntry> loginBuckets = new ConcurrentHashMap<>();

    /** 登录接口路径前缀 */
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = getClientIp(request);
        String uri = request.getRequestURI();

        // 登录接口使用更严格的限流策略
        Map<String, BucketEntry> targetBuckets = uri.startsWith(LOGIN_PATH) ? loginBuckets : buckets;
        boolean isLogin = uri.startsWith(LOGIN_PATH);

        BucketEntry entry = targetBuckets.compute(ip, (k, existing) -> {
            if (existing == null) {
                Bucket bucket = isLogin ? createLoginBucket() : createBucket();
                return new BucketEntry(bucket, System.currentTimeMillis());
            }
            return new BucketEntry(existing.bucket, System.currentTimeMillis());
        });

        if (entry.bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\"}");
        return false;
    }

    /**
     * 每 10 分钟清理超过 1 小时未访问的条目
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        cleanupMap(buckets, now);
        cleanupMap(loginBuckets, now);
    }

    private void cleanupMap(Map<String, BucketEntry> map, long now) {
        Iterator<Map.Entry<String, BucketEntry>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BucketEntry> entry = it.next();
            if (now - entry.getValue().lastAccessTime > EXPIRE_MILLIS) {
                it.remove();
            }
        }
    }

    /**
     * 创建令牌桶：每秒补充 20 个令牌，突发容量 50
     */
    private Bucket createBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(50)
                .refillGreedy(20, Duration.ofSeconds(1))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    /**
     * 创建登录限流令牌桶：10 秒内最多 5 次，防止暴力破解
     */
    private Bucket createLoginBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofSeconds(10))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 令牌桶条目，记录最后访问时间用于过期清理
     */
    private record BucketEntry(Bucket bucket, long lastAccessTime) {
    }
}
