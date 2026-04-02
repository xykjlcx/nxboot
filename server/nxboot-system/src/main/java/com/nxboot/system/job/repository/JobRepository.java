package com.nxboot.system.job.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.system.job.model.JobVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

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
        Condition condition = field("deleted").eq(Constants.NOT_DELETED);
        if (keyword != null && !keyword.isBlank()) {
            condition = condition.and(
                    field("job_name").likeIgnoreCase("%" + keyword + "%")
                            .or(field("invoke_target").likeIgnoreCase("%" + keyword + "%"))
            );
        }

        long total = dsl.selectCount()
                .from(table("sys_job"))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<JobVO> list = dsl.select()
                .from(table("sys_job"))
                .where(condition)
                .orderBy(field("create_time").desc())
                .offset(offset).limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    public List<JobVO> findAllEnabled() {
        return dsl.select()
                .from(table("sys_job"))
                .where(field("deleted").eq(Constants.NOT_DELETED))
                .and(field("enabled").eq(Constants.ENABLED))
                .fetch(this::toVO);
    }

    public JobVO findById(Long id) {
        Record r = dsl.select().from(table("sys_job"))
                .where(field("id").eq(id))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();
        return r != null ? toVO(r) : null;
    }

    public Long insert(String jobName, String jobGroup, String invokeTarget, String cronExpression,
                       Integer misfirePolicy, Integer concurrent, Integer enabled,
                       String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table("sys_job"))
                .set(field("id"), id)
                .set(field("job_name"), jobName)
                .set(field("job_group"), jobGroup != null ? jobGroup : "DEFAULT")
                .set(field("invoke_target"), invokeTarget)
                .set(field("cron_expression"), cronExpression)
                .set(field("misfire_policy"), misfirePolicy != null ? misfirePolicy : 1)
                .set(field("concurrent"), concurrent != null ? concurrent : 0)
                .set(field("enabled"), enabled != null ? enabled : Constants.ENABLED)
                .set(field("remark"), remark)
                .set(field("create_by"), operator)
                .set(field("create_time"), now)
                .set(field("update_by"), operator)
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void update(Long id, String jobName, String jobGroup, String invokeTarget,
                       String cronExpression, Integer misfirePolicy, Integer concurrent,
                       Integer enabled, String remark, String operator) {
        var step = dsl.update(table("sys_job"))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (jobName != null) step = step.set(field("job_name"), jobName);
        if (jobGroup != null) step = step.set(field("job_group"), jobGroup);
        if (invokeTarget != null) step = step.set(field("invoke_target"), invokeTarget);
        if (cronExpression != null) step = step.set(field("cron_expression"), cronExpression);
        if (misfirePolicy != null) step = step.set(field("misfire_policy"), misfirePolicy);
        if (concurrent != null) step = step.set(field("concurrent"), concurrent);
        if (enabled != null) step = step.set(field("enabled"), enabled);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(field("deleted").eq(Constants.NOT_DELETED)).execute();
    }

    public void softDelete(Long id, String operator) {
        dsl.update(table("sys_job"))
                .set(field("deleted"), Constants.DELETED)
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now())
                .where(field("id").eq(id)).execute();
    }

    private JobVO toVO(Record r) {
        Integer enabledVal = r.get(field("enabled", Integer.class));
        return new JobVO(
                r.get(field("id", Long.class)),
                r.get(field("job_name", String.class)),
                r.get(field("job_group", String.class)),
                r.get(field("invoke_target", String.class)),
                r.get(field("cron_expression", String.class)),
                r.get(field("misfire_policy", Integer.class)),
                r.get(field("concurrent", Integer.class)),
                enabledVal != null && enabledVal == 1,
                r.get(field("remark", String.class)),
                r.get("create_time", LocalDateTime.class)
        );
    }
}
