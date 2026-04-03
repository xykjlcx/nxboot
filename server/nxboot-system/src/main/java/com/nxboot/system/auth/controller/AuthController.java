package com.nxboot.system.auth.controller;

import com.nxboot.common.annotation.Log;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.result.R;
import com.nxboot.framework.security.LoginUser;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.auth.model.LoginRequest;
import com.nxboot.system.auth.model.LoginResponse;
import com.nxboot.system.auth.model.RefreshTokenRequest;
import com.nxboot.system.auth.model.RefreshTokenResponse;
import com.nxboot.system.auth.service.AuthService;
import com.nxboot.system.menu.model.MenuVO;
import com.nxboot.system.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MenuService menuService;

    public AuthController(AuthService authService, MenuService menuService) {
        this.authService = authService;
        this.menuService = menuService;
    }

    /**
     * 登录
     */
    @Log(module = "认证管理", operation = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public R<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return R.ok(authService.refreshToken(request));
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

    /**
     * 获取当前用户的菜单树（根据角色权限过滤）
     */
    @GetMapping("/menus")
    public R<List<MenuVO>> userMenus() {
        LoginUser user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return R.ok(menuService.getUserMenuTree(user.getUserId()));
    }
}
