package com.nxboot.system.auth.controller;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.result.R;
import com.nxboot.framework.security.LoginUser;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.auth.model.LoginRequest;
import com.nxboot.system.auth.model.LoginResponse;
import com.nxboot.system.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        LoginUser user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return R.ok(Map.of(
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "permissions", user.getPermissions()
        ));
    }
}
