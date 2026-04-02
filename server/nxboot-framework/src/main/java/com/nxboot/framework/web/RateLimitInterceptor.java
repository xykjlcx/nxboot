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

    /** IP -> BucketEntry 映射 */
    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = getClientIp(request);
        BucketEntry entry = buckets.compute(ip, (k, existing) -> {
            if (existing == null) {
                return new BucketEntry(createBucket(), System.currentTimeMillis());
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
        Iterator<Map.Entry<String, BucketEntry>> it = buckets.entrySet().iterator();
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
