package com.nxboot.system.dept;

import com.nxboot.common.annotation.DataScope;
import com.nxboot.common.constant.Constants;
import com.nxboot.framework.security.DataScopeContext;
import com.nxboot.framework.security.LoginUser;
import com.nxboot.framework.security.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.nxboot.generated.jooq.tables.SysRole.SYS_ROLE;
import static com.nxboot.generated.jooq.tables.SysRoleDept.SYS_ROLE_DEPT;
import static com.nxboot.generated.jooq.tables.SysUserRole.SYS_USER_ROLE;
import static org.jooq.impl.DSL.field;

/**
 * 数据权限 AOP 切面。
 * <p>
 * 拦截 @DataScope 注解的 Service 方法，根据当前用户角色的 data_scope 构建 jOOQ 过滤条件，
 * 写入 ThreadLocal，Repository 通过 JooqHelper.dataScopeCondition() 读取。
 * <p>
 * 数据权限范围：1=全部 2=自定义部门(TODO) 3=本部门 4=本部门及下级(TODO) 5=仅本人
 */
@Aspect
@Component
public class DataScopeAspect {

    /** 全部数据权限 */
    private static final int DATA_SCOPE_ALL = 1;
    /** 自定义部门数据权限 */
    private static final int DATA_SCOPE_CUSTOM = 2;
    /** 本部门数据权限 */
    private static final int DATA_SCOPE_DEPT = 3;
    /** 本部门及下级数据权限 */
    private static final int DATA_SCOPE_DEPT_AND_CHILDREN = 4;
    /** 仅本人数据权限 */
    private static final int DATA_SCOPE_SELF = 5;

    private final DSLContext dsl;

    public DataScopeAspect(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint point, DataScope dataScope) throws Throwable {
        try {
            Condition condition = buildCondition(dataScope);
            DataScopeContext.set(condition);
            return point.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }

    /**
     * 根据当前用户角色的数据权限范围构建过滤条件。
     * 取所有角色中最宽松（数值最小）的 data_scope。
     */
    private Condition buildCondition(DataScope dataScope) {
        LoginUser user = SecurityUtils.getCurrentUser();
        if (user == null) {
            // 未登录，返回不可能满足的条件（不返回任何数据）
            return DSL.falseCondition();
        }

        // admin 角色拥有全部数据权限
        if (user.getPermissions().contains("*:*:*")) {
            return null;
        }

        // 查询当前用户所有角色的 data_scope，取最宽松的（数值最小）
        var ur = SYS_USER_ROLE.as("ur");
        var r = SYS_ROLE.as("r");
        Integer minScope = dsl.select(DSL.min(r.DATA_SCOPE))
                .from(ur)
                .join(r).on(ur.ROLE_ID.eq(r.ID))
                .where(ur.USER_ID.eq(user.getUserId()))
                .and(r.DELETED.eq(Constants.NOT_DELETED))
                .and(r.ENABLED.eq(Constants.ENABLED))
                .fetchOneInto(Integer.class);

        if (minScope == null) {
            // 没有角色，禁止访问
            return DSL.falseCondition();
        }

        String deptAlias = dataScope.deptAlias();
        String userAlias = dataScope.userAlias();
        String deptField = deptAlias.isEmpty() ? "dept_id" : deptAlias + ".dept_id";
        String createByField = userAlias.isEmpty() ? "create_by" : userAlias + ".create_by";

        return switch (minScope) {
            case DATA_SCOPE_ALL -> null; // 无限制
            case DATA_SCOPE_CUSTOM -> buildCustomDeptCondition(user, deptField);
            case DATA_SCOPE_DEPT -> buildOwnDeptCondition(user, deptField);
            case DATA_SCOPE_DEPT_AND_CHILDREN -> buildDeptAndChildrenCondition(user, deptField);
            case DATA_SCOPE_SELF -> field(createByField).eq(user.getUsername());
            default -> null; // 未知范围，不限制
        };
    }

    /**
     * 自定义部门：dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id IN (...))
     */
    private Condition buildCustomDeptCondition(LoginUser user, String deptField) {
        // 查询用户所有角色 ID
        List<Long> roleIds = dsl.select(SYS_USER_ROLE.ROLE_ID)
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(user.getUserId()))
                .fetchInto(Long.class);

        if (roleIds.isEmpty()) {
            return DSL.falseCondition();
        }

        return field(deptField).in(
                dsl.select(SYS_ROLE_DEPT.DEPT_ID)
                        .from(SYS_ROLE_DEPT)
                        .where(SYS_ROLE_DEPT.ROLE_ID.in(roleIds))
        );
    }

    /**
     * 本部门：dept_id = 当前用户部门
     */
    private Condition buildOwnDeptCondition(LoginUser user, String deptField) {
        Long deptId = user.getDeptId();
        if (deptId == null) {
            return DSL.falseCondition();
        }
        return field(deptField).eq(deptId);
    }

    /**
     * 本部门及下级：dept_id IN (递归查询部门树)
     */
    private Condition buildDeptAndChildrenCondition(LoginUser user, String deptField) {
        Long deptId = user.getDeptId();
        if (deptId == null) {
            return DSL.falseCondition();
        }

        // 递归查询当前部门及所有子部门 ID
        List<Long> deptIds = dsl.fetch("""
                WITH RECURSIVE dept_tree AS (
                    SELECT id FROM sys_dept WHERE id = ? AND deleted = 0
                    UNION ALL
                    SELECT d.id FROM sys_dept d
                    JOIN dept_tree dt ON d.parent_id = dt.id
                    WHERE d.deleted = 0
                )
                SELECT id FROM dept_tree
                """, deptId)
                .getValues(field("id", Long.class));

        if (deptIds.isEmpty()) {
            return DSL.falseCondition();
        }
        return field(deptField).in(deptIds);
    }
}
