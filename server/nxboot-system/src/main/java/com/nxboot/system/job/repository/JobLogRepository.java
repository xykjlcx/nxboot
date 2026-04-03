package com.nxboot.system.job.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.system.job.model.JobLogVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.nxboot.generated.jooq.tables.SysJobLog.SYS_JOB_LOG;

/**
 * 定时任务执行日志数据访问
 */
@Repository
public class JobLogRepository {

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public JobLogRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    /**
     * 分页查询任务执行日志
     *
     * @param offset  偏移量
     * @param size    页大小
     * @param jobId   任务 ID（可选）
     * @param keyword 关键词（可选，搜索任务名称/调用目标）
     * @param status  执行状态（可选，0=成功 1=失败）
     */
    public PageResult<JobLogVO> page(int offset, int size, Long jobId, String keyword, Integer status) {
        Condition condition = DSL.trueCondition();

        // 按任务 ID 过滤
        if (jobId != null) {
            condition = condition.and(SYS_JOB_LOG.JOB_ID.eq(jobId));
        }

        // 按执行状态过滤
        if (status != null) {
            condition = condition.and(SYS_JOB_LOG.STATUS.eq(status));
        }

        // 关键词模糊搜索
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword + "%";
            condition = condition.and(
                    SYS_JOB_LOG.JOB_NAME.likeIgnoreCase(pattern)
                            .or(SYS_JOB_LOG.INVOKE_TARGET.likeIgnoreCase(pattern))
            );
        }

        long total = dsl.selectCount()
                .from(SYS_JOB_LOG)
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) {
            return PageResult.empty();
        }

        List<JobLogVO> list = dsl.select()
                .from(SYS_JOB_LOG)
                .where(condition)
                .orderBy(SYS_JOB_LOG.START_TIME.desc())
                .offset(offset)
                .limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    /**
     * 写入执行日志
     */
    public void insert(Long jobId, String jobName, String jobGroup, String invokeTarget,
                       int status, String errorMsg, LocalDateTime startTime, LocalDateTime endTime,
                       long duration) {
        dsl.insertInto(SYS_JOB_LOG)
                .set(SYS_JOB_LOG.ID, idGenerator.nextId())
                .set(SYS_JOB_LOG.JOB_ID, jobId)
                .set(SYS_JOB_LOG.JOB_NAME, jobName)
                .set(SYS_JOB_LOG.JOB_GROUP, jobGroup)
                .set(SYS_JOB_LOG.INVOKE_TARGET, invokeTarget)
                .set(SYS_JOB_LOG.STATUS, status)
                .set(SYS_JOB_LOG.ERROR_MSG, errorMsg)
                .set(SYS_JOB_LOG.START_TIME, startTime)
                .set(SYS_JOB_LOG.END_TIME, endTime)
                .set(SYS_JOB_LOG.DURATION, duration)
                .execute();
    }

    private JobLogVO toVO(Record r) {
        return new JobLogVO(
                r.get(SYS_JOB_LOG.ID),
                r.get(SYS_JOB_LOG.JOB_ID),
                r.get(SYS_JOB_LOG.JOB_NAME),
                r.get(SYS_JOB_LOG.JOB_GROUP),
                r.get(SYS_JOB_LOG.INVOKE_TARGET),
                r.get(SYS_JOB_LOG.STATUS),
                r.get(SYS_JOB_LOG.ERROR_MSG),
                r.get(SYS_JOB_LOG.START_TIME),
                r.get(SYS_JOB_LOG.END_TIME),
                r.get(SYS_JOB_LOG.DURATION)
        );
    }
}
