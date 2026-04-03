package com.nxboot.system.auth.service;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.framework.security.JwtTokenProvider;
import com.nxboot.framework.security.LoginUser;
import com.nxboot.system.auth.model.LoginRequest;
import com.nxboot.system.auth.model.LoginResponse;
import com.nxboot.system.auth.model.RefreshTokenRequest;
import com.nxboot.system.auth.model.RefreshTokenResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.jooq.DSLContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.nxboot.generated.jooq.tables.SysUser.SYS_USER;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final DSLContext dsl;
    private final LoginLogService loginLogService;
    private final OnlineUserService onlineUserService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       DSLContext dsl,
                       LoginLogService loginLogService,
                       OnlineUserService onlineUserService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.dsl = dsl;
        this.loginLogService = loginLogService;
        this.onlineUserService = onlineUserService;
    }

    /**
     * 登录认证
     */
    public LoginResponse login(LoginRequest request) {
        String ip = getClientIp();
        String userAgent = getUserAgent();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String token = tokenProvider.generateToken(loginUser.getUserId(), loginUser.getUsername());
            String refreshToken = tokenProvider.generateRefreshToken(loginUser.getUserId(), loginUser.getUsername());

            // 查询昵称
            String nickname = dsl.select(SYS_USER.NICKNAME)
                    .from(SYS_USER)
                    .where(SYS_USER.ID.eq(loginUser.getUserId()))
                    .fetchOneInto(String.class);

            // 记录登录成功日志
            loginLogService.record(request.username(), ip, userAgent, 0, "登录成功");

            // 注册在线用户
            onlineUserService.login(token, loginUser.getUserId(), loginUser.getUsername(), ip, userAgent);

            return new LoginResponse(token, refreshToken, loginUser.getUserId(), loginUser.getUsername(), nickname);
        } catch (BadCredentialsException e) {
            loginLogService.record(request.username(), ip, userAgent, 1, "用户名或密码错误");
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        } catch (DisabledException e) {
            loginLogService.record(request.username(), ip, userAgent, 1, "账号已被禁用");
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }
    }

    /**
     * 刷新令牌：校验 refreshToken，签发新的 access + refresh token
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        Claims claims = tokenProvider.validateRefreshToken(request.refreshToken());
        if (claims == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String username = claims.getSubject();
        Long userId = claims.get("userId", Long.class);

        // 校验用户是否仍然存在且启用
        Boolean enabled = dsl.select(SYS_USER.ENABLED)
                .from(SYS_USER)
                .where(SYS_USER.ID.eq(userId))
                .and(SYS_USER.DELETED.eq(0))
                .fetchOneInto(Boolean.class);

        if (enabled == null || !enabled) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        String newToken = tokenProvider.generateToken(userId, username);
        String newRefreshToken = tokenProvider.generateRefreshToken(userId, username);
        return new RefreshTokenResponse(newToken, newRefreshToken);
    }

    /**
     * 从当前请求中获取客户端 IP
     */
    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
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
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从当前请求中获取 User-Agent
     */
    private String getUserAgent() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }
}
