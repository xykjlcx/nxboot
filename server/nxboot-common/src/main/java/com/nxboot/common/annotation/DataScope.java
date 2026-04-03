package com.nxboot.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解——标注在 Service 方法上，自动根据当前用户角色的数据范围过滤数据。
 * <p>
 * 使用方式：在 Service 的查询方法上加此注解。
 * AOP 切面会将过滤条件写入 ThreadLocal，Repository 通过 JooqHelper.dataScopeCondition() 读取。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /** 部门表别名（用于拼接条件），默认空字符串表示主表 */
    String deptAlias() default "";
    /** 用户表别名（用于拼接 create_by 条件），默认空字符串表示主表 */
    String userAlias() default "";
}
