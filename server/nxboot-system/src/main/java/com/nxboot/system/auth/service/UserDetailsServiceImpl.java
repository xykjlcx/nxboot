package com.nxboot.system.auth.service;

import com.nxboot.common.constant.Constants;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.framework.security.LoginUser;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.nxboot.generated.jooq.tables.SysMenu.SYS_MENU;
import static com.nxboot.generated.jooq.tables.SysRole.SYS_ROLE;
import static com.nxboot.generated.jooq.tables.SysRoleMenu.SYS_ROLE_MENU;
import static com.nxboot.generated.jooq.tables.SysUser.SYS_USER;
import static com.nxboot.generated.jooq.tables.SysUserRole.SYS_USER_ROLE;

/**
 * 用户认证服务，实现 Spring Security UserDetailsService
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DSLContext dsl;

    public UserDetailsServiceImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        Record userRecord = dsl.select()
                .from(SYS_USER)
                .where(SYS_USER.USERNAME.eq(username))
                .and(SYS_USER.DELETED.eq(Constants.NOT_DELETED))
                .fetchOne();

        if (userRecord == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        Long userId = userRecord.get(SYS_USER.ID);
        String password = userRecord.get(SYS_USER.PASSWORD);
        boolean enabled = userRecord.get(SYS_USER.ENABLED) == Constants.ENABLED;
        Long deptId = userRecord.get(SYS_USER.DEPT_ID);

        if (!enabled) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        // 查询权限
        Set<String> permissions = loadPermissions(userId);

        return new LoginUser(userId, username, password, enabled, deptId, permissions);
    }

    /**
     * 根据用户ID加载用户信息
     */
    public LoginUser loadUserById(Long userId) {
        Record userRecord = dsl.select()
                .from(SYS_USER)
                .where(SYS_USER.ID.eq(userId))
                .and(SYS_USER.DELETED.eq(Constants.NOT_DELETED))
                .fetchOne();

        if (userRecord == null) {
            return null;
        }

        String username = userRecord.get(SYS_USER.USERNAME);
        String password = userRecord.get(SYS_USER.PASSWORD);
        boolean enabled = userRecord.get(SYS_USER.ENABLED) == Constants.ENABLED;
        Long deptId = userRecord.get(SYS_USER.DEPT_ID);

        Set<String> permissions = loadPermissions(userId);

        return new LoginUser(userId, username, password, enabled, deptId, permissions);
    }

    /**
     * 加载用户权限列表
     */
    private Set<String> loadPermissions(Long userId) {
        Set<String> permissions = new HashSet<>();

        // 查询用户角色
        var ur = SYS_USER_ROLE.as("ur");
        var r = SYS_ROLE.as("r");
        var roleKeys = dsl.select(r.ROLE_KEY)
                .from(ur)
                .join(r).on(ur.ROLE_ID.eq(r.ID))
                .where(ur.USER_ID.eq(userId))
                .and(r.DELETED.eq(Constants.NOT_DELETED))
                .and(r.ENABLED.eq(Constants.ENABLED))
                .fetch(r.ROLE_KEY);

        // admin 角色拥有所有权限
        if (roleKeys.contains(Constants.ROLE_ADMIN)) {
            permissions.add("*:*:*");
            return permissions;
        }

        // 查询角色关联的菜单权限
        var ur2 = SYS_USER_ROLE.as("ur2");
        var rm = SYS_ROLE_MENU.as("rm");
        var m = SYS_MENU.as("m");
        var menuPermissions = dsl.select(m.PERMISSION)
                .from(ur2)
                .join(rm).on(ur2.ROLE_ID.eq(rm.ROLE_ID))
                .join(m).on(rm.MENU_ID.eq(m.ID))
                .where(ur2.USER_ID.eq(userId))
                .and(m.DELETED.eq(Constants.NOT_DELETED))
                .and(m.ENABLED.eq(Constants.ENABLED))
                .and(m.PERMISSION.isNotNull())
                .and(m.PERMISSION.ne(""))
                .fetch(m.PERMISSION);

        permissions.addAll(menuPermissions);
        return permissions;
    }
}
