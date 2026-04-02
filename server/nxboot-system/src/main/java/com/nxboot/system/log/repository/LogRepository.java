package com.nxboot.system.log.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.system.log.model.OperationLogVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 操作日志数据访问
 */
@Repository
public class LogRepository {

    private final DSLContext dsl;

    public LogRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PageResult<OperationLogVO> page(int offset, int size, String keyword) {
        Condition condition = DSL.trueCondition();
        if (keyword != null && !keyword.isBlank()) {
            condition = condition.and(
                    field("module").likeIgnoreCase("%" + keyword + "%")
                            .or(field("operation").likeIgnoreCase("%" + keyword + "%"))
                            .or(field("operator").likeIgnoreCase("%" + keyword + "%"))
            );
        }

        long total = dsl.selectCount()
                .from(table("sys_log"))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<OperationLogVO> list = dsl.select()
                .from(table("sys_log"))
                .where(condition)
                .orderBy(field("create_time").desc())
                .offset(offset).limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    public OperationLogVO findById(Long id) {
        Record r = dsl.select().from(table("sys_log"))
                .where(field("id").eq(id))
                .fetchOne();
        return r != null ? toVO(r) : null;
    }

    private OperationLogVO toVO(Record r) {
        return new OperationLogVO(
                r.get(field("id", Long.class)),
                r.get(field("module", String.class)),
                r.get(field("operation", String.class)),
                r.get(field("method", String.class)),
                r.get(field("request_url", String.class)),
                r.get(field("request_method", String.class)),
                r.get(field("request_params", String.class)),
                r.get(field("response_body", String.class)),
                r.get(field("operator", String.class)),
                r.get(field("operator_ip", String.class)),
                r.get(field("status", Integer.class)),
                r.get(field("error_msg", String.class)),
                r.get(field("duration", Long.class)),
                r.get("create_time", LocalDateTime.class)
        );
    }
}
