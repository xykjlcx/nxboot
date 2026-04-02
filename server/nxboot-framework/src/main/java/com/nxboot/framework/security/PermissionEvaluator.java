package com.nxboot.framework.security;

import org.springframework.stereotype.Component;

/**
 * 权限判断器，供 @PreAuthorize("@perm.has('xxx')") 使用
 */
@Component("perm")
public class PermissionEvaluator {

    /** 全部权限标识 */
    private static final String ALL_PERMISSIONS = "*:*:*";

    /**
     * 判断当前用户是否拥有指定权限
     */
    public boolean has(String permission) {
        LoginUser user = SecurityUtils.getCurrentUser();
        if (user == null) {
            return false;
        }
        // admin 拥有所有权限
        if (user.getPermissions().contains(ALL_PERMISSIONS)) {
            return true;
        }
        return user.getPermissions().contains(permission);
    }
}
