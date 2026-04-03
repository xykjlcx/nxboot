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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

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
                .from(table("sys_user"))
                .where(field("username").eq(username))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();

        if (userRecord == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        Long userId = userRecord.get(field("id", Long.class));
        String password = userRecord.get(field("password", String.class));
        boolean enabled = userRecord.get(field("enabled", Integer.class)) == Constants.ENABLED;
        Long deptId = userRecord.get(field("dept_id", Long.class));

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
                .from(table("sys_user"))
                .where(field("id").eq(userId))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();

        if (userRecord == null) {
            return null;
        }

        String username = userRecord.get(field("username", String.class));
        String password = userRecord.get(field("password", String.class));
        boolean enabled = userRecord.get(field("enabled", Integer.class)) == Constants.ENABLED;
        Long deptId = userRecord.get(field("dept_id", Long.class));

        Set<String> permissions = loadPermissions(userId);

        return new LoginUser(userId, username, password, enabled, deptId, permissions);
    }

    /**
     * 加载用户权限列表
     */
    private Set<String> loadPermissions(Long userId) {
        Set<String> permissions = new HashSet<>();

        // 查询用户角色
        var roleKeys = dsl.select(field("r.role_key"))
                .from(table("sys_user_role").as("ur"))
                .join(table("sys_role").as("r")).on(field("ur.role_id").eq(field("r.id")))
                .where(field("ur.user_id").eq(userId))
                .and(field("r.deleted").eq(Constants.NOT_DELETED))
                .and(field("r.enabled").eq(Constants.ENABLED))
                .fetch(field("r.role_key", String.class));

        // admin 角色拥有所有权限
        if (roleKeys.contains(Constants.ROLE_ADMIN)) {
            permissions.add("*:*:*");
            return permissions;
        }

        // 查询角色关联的菜单权限
        var menuPermissions = dsl.select(field("m.permission"))
                .from(table("sys_user_role").as("ur"))
                .join(table("sys_role_menu").as("rm")).on(field("ur.role_id").eq(field("rm.role_id")))
                .join(table("sys_menu").as("m")).on(field("rm.menu_id").eq(field("m.id")))
                .where(field("ur.user_id").eq(userId))
                .and(field("m.deleted").eq(Constants.NOT_DELETED))
                .and(field("m.enabled").eq(Constants.ENABLED))
                .and(field("m.permission").isNotNull())
                .and(field("m.permission").ne(""))
                .fetch(field("m.permission", String.class));

        permissions.addAll(menuPermissions);
        return permissions;
    }
}
