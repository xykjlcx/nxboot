package com.nxboot.common.annotation;

import java.lang.annotation.*;

/**
 * 标记为可被定时任务调用的方法
 * <p>
 * 只有带此注解的方法才允许通过 JobService 反射执行，防止任意方法调用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobHandler {
}
