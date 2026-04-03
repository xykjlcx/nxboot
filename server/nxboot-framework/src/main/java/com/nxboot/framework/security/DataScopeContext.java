package com.nxboot.framework.security;

import org.jooq.Condition;

/**
 * 数据权限条件的 ThreadLocal 持有者。
 * AOP 切面写入条件，JooqHelper 读取条件。
 */
public final class DataScopeContext {

    private static final ThreadLocal<Condition> CONDITION = new ThreadLocal<>();

    public static void set(Condition condition) {
        CONDITION.set(condition);
    }

    public static Condition get() {
        return CONDITION.get();
    }

    public static void clear() {
        CONDITION.remove();
    }

    private DataScopeContext() {
    }
}
