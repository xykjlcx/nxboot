package com.nxboot.system.job.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.job.model.JobVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.nxboot.generated.jooq.tables.SysJob.SYS_JOB;

/**
 * 定时任务数据访问
 */
@Repository
public class JobRepository {

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public JobRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    public PageResult<JobVO> page(int offset, int size, String keyword) {
        Condition extra = JooqHelper.keywordCondition(keyword, SYS_JOB.JOB_NAME, SYS_JOB.INVOKE_TARGET);
        return JooqHelper.page(dsl, SYS_JOB, extra, offset, size, this::toVO);
    }

    public List<JobVO> findAllEnabled() {
        return dsl.select()
                .from(SYS_JOB)
                .where(SYS_JOB.DELETED.eq(Constants.NOT_DELETED))
                .and(SYS_JOB.ENABLED.eq(Constants.ENABLED))
                .fetch(this::toVO);
    }

    public JobVO findById(Long id) {
        Record r = JooqHelper.findById(dsl, SYS_JOB, id);
        return r != null ? toVO(r) : null;
    }

    public Long insert(String jobName, String jobGroup, String invokeTarget, String cronExpression,
                       Integer misfirePolicy, Integer concurrent, Integer enabled,
                       String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(SYS_JOB)
                .set(SYS_JOB.ID, id)
                .set(SYS_JOB.JOB_NAME, jobName)
                .set(SYS_JOB.JOB_GROUP, jobGroup != null ? jobGroup : "DEFAULT")
                .set(SYS_JOB.INVOKE_TARGET, invokeTarget)
                .set(SYS_JOB.CRON_EXPRESSION, cronExpression)
                .set(SYS_JOB.MISFIRE_POLICY, misfirePolicy != null ? misfirePolicy : 1)
                .set(SYS_JOB.CONCURRENT, concurrent != null ? concurrent : 0)
                .set(SYS_JOB.ENABLED, enabled != null ? enabled : Constants.ENABLED)
                .set(SYS_JOB.REMARK, remark)
                .set(SYS_JOB.CREATE_BY, operator)
                .set(SYS_JOB.CREATE_TIME, now)
                .set(SYS_JOB.UPDATE_BY, operator)
                .set(SYS_JOB.UPDATE_TIME, now)
                .set(SYS_JOB.DELETED, Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void update(Long id, String jobName, String jobGroup, String invokeTarget,
                       String cronExpression, Integer misfirePolicy, Integer concurrent,
                       Integer enabled, String remark, String operator) {
        var step = dsl.update(SYS_JOB)
                .set(SYS_JOB.UPDATE_BY, operator)
                .set(SYS_JOB.UPDATE_TIME, LocalDateTime.now());

        if (jobName != null) step = step.set(SYS_JOB.JOB_NAME, jobName);
        if (jobGroup != null) step = step.set(SYS_JOB.JOB_GROUP, jobGroup);
        if (invokeTarget != null) step = step.set(SYS_JOB.INVOKE_TARGET, invokeTarget);
        if (cronExpression != null) step = step.set(SYS_JOB.CRON_EXPRESSION, cronExpression);
        if (misfirePolicy != null) step = step.set(SYS_JOB.MISFIRE_POLICY, misfirePolicy);
        if (concurrent != null) step = step.set(SYS_JOB.CONCURRENT, concurrent);
        if (enabled != null) step = step.set(SYS_JOB.ENABLED, enabled);
        if (remark != null) step = step.set(SYS_JOB.REMARK, remark);

        step.where(SYS_JOB.ID.eq(id)).and(SYS_JOB.DELETED.eq(Constants.NOT_DELETED)).execute();
    }

    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_JOB, id, operator);
    }

    private JobVO toVO(Record r) {
        Integer enabledVal = r.get(SYS_JOB.ENABLED);
        return new JobVO(
                r.get(SYS_JOB.ID),
                r.get(SYS_JOB.JOB_NAME),
                r.get(SYS_JOB.JOB_GROUP),
                r.get(SYS_JOB.INVOKE_TARGET),
                r.get(SYS_JOB.CRON_EXPRESSION),
                r.get(SYS_JOB.MISFIRE_POLICY),
                r.get(SYS_JOB.CONCURRENT),
                enabledVal != null && enabledVal == 1,
                r.get(SYS_JOB.REMARK),
                r.get(SYS_JOB.CREATE_TIME)
        );
    }
}
