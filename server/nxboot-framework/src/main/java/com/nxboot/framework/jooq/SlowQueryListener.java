package com.nxboot.framework.jooq;

import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 慢查询监听器——记录执行超过阈值的 SQL。
 */
public class SlowQueryListener extends DefaultExecuteListener {

    private static final Logger log = LoggerFactory.getLogger(SlowQueryListener.class);
    private static final long THRESHOLD_MS = 500;

    @Override
    public void executeStart(ExecuteContext ctx) {
        ctx.data("nx-sql-start", System.currentTimeMillis());
    }

    @Override
    public void executeEnd(ExecuteContext ctx) {
        Long start = (Long) ctx.data("nx-sql-start");
        if (start != null) {
            long duration = System.currentTimeMillis() - start;
            if (duration >= THRESHOLD_MS) {
                log.warn("[慢查询] {}ms | SQL: {}", duration, ctx.query());
            }
        }
    }
}
