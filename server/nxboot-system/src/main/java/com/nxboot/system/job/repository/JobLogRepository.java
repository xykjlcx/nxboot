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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 定时任务执行日志数据访问
 */
@Repository
public class JobLogRepository {

    private static final String TABLE = "sys_job_log";

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
            condition = condition.and(field("job_id").eq(jobId));
        }

        // 按执行状态过滤
        if (status != null) {
            condition = condition.and(field("status").eq(status));
        }

        // 关键词模糊搜索
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword + "%";
            condition = condition.and(
                    field("job_name").likeIgnoreCase(pattern)
                            .or(field("invoke_target").likeIgnoreCase(pattern))
            );
        }

        long total = dsl.selectCount()
                .from(table(TABLE))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) {
            return PageResult.empty();
        }

        List<JobLogVO> list = dsl.select()
                .from(table(TABLE))
                .where(condition)
                .orderBy(field("start_time").desc())
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
        dsl.insertInto(table(TABLE))
                .set(field("id"), idGenerator.nextId())
                .set(field("job_id"), jobId)
                .set(field("job_name"), jobName)
                .set(field("job_group"), jobGroup)
                .set(field("invoke_target"), invokeTarget)
                .set(field("status"), status)
                .set(field("error_msg"), errorMsg)
                .set(field("start_time"), startTime)
                .set(field("end_time"), endTime)
                .set(field("duration"), duration)
                .execute();
    }

    private JobLogVO toVO(Record r) {
        return new JobLogVO(
                r.get("id", Long.class),
                r.get("job_id", Long.class),
                r.get("job_name", String.class),
                r.get("job_group", String.class),
                r.get("invoke_target", String.class),
                r.get("status", Integer.class),
                r.get("error_msg", String.class),
                r.get("start_time", LocalDateTime.class),
                r.get("end_time", LocalDateTime.class),
                r.get("duration", Long.class)
        );
    }
}
