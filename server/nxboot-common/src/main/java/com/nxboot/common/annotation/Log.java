package com.nxboot.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解——标注在 Controller 方法上，自动记录操作日志。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 操作模块 */
    String module() default "";
    /** 操作类型 */
    String operation() default "";
}
