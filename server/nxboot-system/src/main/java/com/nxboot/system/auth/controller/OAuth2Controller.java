package com.nxboot.system.auth.controller;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.result.R;
import com.nxboot.system.auth.model.LoginResponse;
import com.nxboot.system.auth.service.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * OAuth2 社会化登录接口。
 * <p>
 * 三个端点：
 * - 列出已启用的 OAuth2 平台
 * - 获取授权 URL（前端引导用户跳转）
 * - 回调处理（用授权码换 JWT）
 */
@RestController
@RequestMapping("/api/v1/auth/oauth2")
public class OAuth2Controller {

    /** 内存 state 存储，value 为过期时间戳（毫秒），TTL 5 分钟 */
    private final ConcurrentMap<String, Long> stateStore = new ConcurrentHashMap<>();

    private final OAuth2Service oauth2Service;

    public OAuth2Controller(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    /**
     * 列出已启用的 OAuth2 平台
     */
    @GetMapping("/providers")
    public R<List<String>> listProviders() {
        return R.ok(oauth2Service.listProviders());
    }

    /**
     * 获取授权 URL。
     * 前端拿到 URL 后引导用户跳转到三方平台。
     *
     * @param provider    平台标识（github/google 等）
     * @param redirectUri 前端回调地址
     */
    @GetMapping("/{provider}/authorize")
    public R<Map<String, String>> authorize(
            @PathVariable String provider,
            @RequestParam("redirect_uri") String redirectUri) {
        // 清理过期 state
        long now = System.currentTimeMillis();
        stateStore.entrySet().removeIf(e -> now > e.getValue());

        String state = UUID.randomUUID().toString().replace("-", "");
        // 存储 state，5 分钟过期
        stateStore.put(state, now + 5 * 60 * 1000);

        String authorizationUrl = oauth2Service.buildAuthorizationUrl(provider, redirectUri, state);
        return R.ok(Map.of(
                "authorizationUrl", authorizationUrl,
                "state", state
        ));
    }

    /**
     * OAuth2 回调：用授权码换取 JWT。
     * <p>
     * 前端收到三方平台回调后，将 code 发到此接口。
     *
     * @param provider 平台标识
     */
    @PostMapping("/{provider}/callback")
    public R<LoginResponse> callback(
            @PathVariable String provider,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String code = body.get("code");
        String redirectUri = body.get("redirectUri");
        String state = body.get("state");

        // 校验授权码不能为空
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "授权码不能为空");
        }

        // 校验 state 防止 CSRF 攻击
        if (state == null || state.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少授权状态参数");
        }
        Long expireAt = stateStore.remove(state);
        if (expireAt == null || System.currentTimeMillis() > expireAt) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效或已过期的授权状态");
        }

        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        LoginResponse response = oauth2Service.handleCallback(
                provider, code, redirectUri, ip, userAgent);
        return R.ok(response);
    }

    /**
     * 从请求中获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
