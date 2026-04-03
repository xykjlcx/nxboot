package com.nxboot.system.log.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
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

    private static final String TABLE = "sys_log";

    private final DSLContext dsl;

    public LogRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PageResult<OperationLogVO> page(int offset, int size, String keyword, Integer status) {
        // 日志表没有 deleted 字段，用自定义条件组合
        Condition extra = JooqHelper.keywordCondition(keyword, "module", "operation", "operator");
        if (status != null) {
            Condition statusCond = field("status").eq(status);
            extra = extra != null ? extra.and(statusCond) : statusCond;
        }

        // 日志表无软删除，直接用 DSL.trueCondition 做基础条件
        Condition condition = DSL.trueCondition();
        if (extra != null) {
            condition = condition.and(extra);
        }

        long total = dsl.selectCount()
                .from(table(TABLE))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<OperationLogVO> list = dsl.select()
                .from(table(TABLE))
                .where(condition)
                .orderBy(field("create_time").desc())
                .offset(offset).limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    public OperationLogVO findById(Long id) {
        Record r = dsl.select().from(table(TABLE))
                .where(field("id").eq(id))
                .fetchOne();
        return r != null ? toVO(r) : null;
    }

    /**
     * 插入操作日志
     */
    public void insert(String module, String operation, String method, String requestUrl,
                       String requestMethod, String requestParams, String responseBody,
                       String operator, String operatorIp, Integer status, String errorMsg, Long duration) {
        dsl.insertInto(table(TABLE))
                .set(field("id"), SnowflakeIdGenerator.getInstance().nextId())
                .set(field("module"), module)
                .set(field("operation"), operation)
                .set(field("method"), method)
                .set(field("request_url"), requestUrl)
                .set(field("request_method"), requestMethod)
                .set(field("request_params"), requestParams)
                .set(field("response_body"), responseBody)
                .set(field("operator"), operator)
                .set(field("operator_ip"), operatorIp)
                .set(field("status"), status)
                .set(field("error_msg"), errorMsg)
                .set(field("duration"), duration)
                .set(field("create_time"), LocalDateTime.now())
                .execute();
    }

    private OperationLogVO toVO(Record r) {
        return new OperationLogVO(
                r.get("id", Long.class),
                r.get("module", String.class),
                r.get("operation", String.class),
                r.get("method", String.class),
                r.get("request_url", String.class),
                r.get("request_method", String.class),
                r.get("request_params", String.class),
                r.get("response_body", String.class),
                r.get("operator", String.class),
                r.get("operator_ip", String.class),
                r.get("status", Integer.class),
                r.get("error_msg", String.class),
                r.get("duration", Long.class),
                r.get("create_time", LocalDateTime.class)
        );
    }
}
