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

import static com.nxboot.generated.jooq.tables.SysLog.SYS_LOG;

/**
 * 操作日志数据访问
 */
@Repository
public class LogRepository {

    private final DSLContext dsl;

    public LogRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PageResult<OperationLogVO> page(int offset, int size, String keyword, Integer status) {
        // 日志表没有 deleted 字段，用自定义条件组合
        Condition extra = JooqHelper.keywordCondition(keyword, SYS_LOG.MODULE, SYS_LOG.OPERATION, SYS_LOG.OPERATOR);
        if (status != null) {
            Condition statusCond = SYS_LOG.STATUS.eq(status);
            extra = extra != null ? extra.and(statusCond) : statusCond;
        }

        // 日志表无软删除，直接用 DSL.trueCondition 做基础条件
        Condition condition = DSL.trueCondition();
        if (extra != null) {
            condition = condition.and(extra);
        }

        long total = dsl.selectCount()
                .from(SYS_LOG)
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<OperationLogVO> list = dsl.select()
                .from(SYS_LOG)
                .where(condition)
                .orderBy(SYS_LOG.CREATE_TIME.desc())
                .offset(offset).limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    public OperationLogVO findById(Long id) {
        Record r = dsl.select().from(SYS_LOG)
                .where(SYS_LOG.ID.eq(id))
                .fetchOne();
        return r != null ? toVO(r) : null;
    }

    /**
     * 插入操作日志
     */
    public void insert(String module, String operation, String method, String requestUrl,
                       String requestMethod, String requestParams, String responseBody,
                       String operator, String operatorIp, Integer status, String errorMsg, Long duration) {
        dsl.insertInto(SYS_LOG)
                .set(SYS_LOG.ID, SnowflakeIdGenerator.getInstance().nextId())
                .set(SYS_LOG.MODULE, module)
                .set(SYS_LOG.OPERATION, operation)
                .set(SYS_LOG.METHOD, method)
                .set(SYS_LOG.REQUEST_URL, requestUrl)
                .set(SYS_LOG.REQUEST_METHOD, requestMethod)
                .set(SYS_LOG.REQUEST_PARAMS, requestParams)
                .set(SYS_LOG.RESPONSE_BODY, responseBody)
                .set(SYS_LOG.OPERATOR, operator)
                .set(SYS_LOG.OPERATOR_IP, operatorIp)
                .set(SYS_LOG.STATUS, status)
                .set(SYS_LOG.ERROR_MSG, errorMsg)
                .set(SYS_LOG.DURATION, duration)
                .set(SYS_LOG.CREATE_TIME, LocalDateTime.now())
                .execute();
    }

    private OperationLogVO toVO(Record r) {
        return new OperationLogVO(
                r.get(SYS_LOG.ID),
                r.get(SYS_LOG.MODULE),
                r.get(SYS_LOG.OPERATION),
                r.get(SYS_LOG.METHOD),
                r.get(SYS_LOG.REQUEST_URL),
                r.get(SYS_LOG.REQUEST_METHOD),
                r.get(SYS_LOG.REQUEST_PARAMS),
                r.get(SYS_LOG.RESPONSE_BODY),
                r.get(SYS_LOG.OPERATOR),
                r.get(SYS_LOG.OPERATOR_IP),
                r.get(SYS_LOG.STATUS),
                r.get(SYS_LOG.ERROR_MSG),
                r.get(SYS_LOG.DURATION),
                r.get(SYS_LOG.CREATE_TIME)
        );
    }
}
