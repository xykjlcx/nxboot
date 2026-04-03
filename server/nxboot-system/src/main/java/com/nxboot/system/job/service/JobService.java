package com.nxboot.system.job.service;

import com.nxboot.common.annotation.JobHandler;
import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.job.model.JobCommand;
import com.nxboot.system.job.model.JobVO;
import com.nxboot.system.job.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务服务
 * <p>
 * 使用 Spring TaskScheduler 动态注册/取消任务
 */
@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final TaskScheduler taskScheduler;
    private final ApplicationContext applicationContext;

    /** 运行中的任务 */
    private final Map<Long, ScheduledFuture<?>> runningJobs = new ConcurrentHashMap<>();

    private final JobLogService jobLogService;

    public JobService(JobRepository jobRepository, TaskScheduler taskScheduler,
                      ApplicationContext applicationContext, JobLogService jobLogService) {
        this.jobRepository = jobRepository;
        this.taskScheduler = taskScheduler;
        this.applicationContext = applicationContext;
        this.jobLogService = jobLogService;
    }

    /**
     * 启动时加载所有启用的任务
     */
    @PostConstruct
    public void init() {
        jobRepository.findAllEnabled().forEach(this::scheduleJob);
    }

    public PageResult<JobVO> page(PageQuery query, String keyword) {
        return jobRepository.page(query.offset(), query.pageSize(), keyword);
    }

    public JobVO getById(Long id) {
        JobVO job = jobRepository.findById(id);
        AssertUtils.notNull(job, "定时任务", id);
        return job;
    }

    @Transactional
    public Long create(JobCommand.Create cmd) {
        String operator = SecurityUtils.getCurrentUsername();
        Long id = jobRepository.insert(cmd.jobName(), cmd.jobGroup(), cmd.invokeTarget(),
                cmd.cronExpression(), cmd.misfirePolicy(), cmd.concurrent(),
                cmd.enabled(), cmd.remark(), operator);

        // 如果创建时就启用，立即调度
        if (cmd.enabled() == null || cmd.enabled() == 1) {
            JobVO job = jobRepository.findById(id);
            scheduleJob(job);
        }

        return id;
    }

    @Transactional
    public void update(Long id, JobCommand.Update cmd) {
        AssertUtils.notNull(jobRepository.findById(id), "定时任务", id);
        String operator = SecurityUtils.getCurrentUsername();

        // 先取消旧的调度
        cancelJob(id);

        jobRepository.update(id, cmd.jobName(), cmd.jobGroup(), cmd.invokeTarget(),
                cmd.cronExpression(), cmd.misfirePolicy(), cmd.concurrent(),
                cmd.enabled(), cmd.remark(), operator);

        // 如果更新后仍启用，重新调度
        JobVO updated = jobRepository.findById(id);
        if (updated != null && Boolean.TRUE.equals(updated.enabled())) {
            scheduleJob(updated);
        }
    }

    @Transactional
    public void delete(Long id) {
        AssertUtils.notNull(jobRepository.findById(id), "定时任务", id);
        cancelJob(id);
        String operator = SecurityUtils.getCurrentUsername();
        jobRepository.softDelete(id, operator);
    }

    /**
     * 立即执行一次
     */
    public void runOnce(Long id) {
        JobVO job = jobRepository.findById(id);
        AssertUtils.notNull(job, "定时任务", id);
        executeJob(job);
    }

    /**
     * 注册定时任务
     */
    private void scheduleJob(JobVO job) {
        try {
            Runnable task = () -> executeJob(job);
            ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(job.cronExpression()));
            runningJobs.put(job.id(), future);
            log.info("定时任务已注册: {} [{}]", job.jobName(), job.cronExpression());
        } catch (Exception e) {
            log.error("定时任务注册失败: {} - {}", job.jobName(), e.getMessage());
        }
    }

    /**
     * 取消定时任务
     */
    private void cancelJob(Long jobId) {
        ScheduledFuture<?> future = runningJobs.remove(jobId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 执行任务：解析 invokeTarget（beanName.methodName）并反射调用，同时记录执行日志
     * <p>
     * 目标方法必须标注 @JobHandler 注解，否则拒绝执行
     */
    private void executeJob(JobVO job) {
        String invokeTarget = job.invokeTarget();
        LocalDateTime startTime = LocalDateTime.now();
        try {
            int dotIndex = invokeTarget.lastIndexOf('.');
            if (dotIndex <= 0 || dotIndex >= invokeTarget.length() - 1) {
                log.error("定时任务调用目标格式错误: {}", invokeTarget);
                jobLogService.record(job.id(), job.jobName(), job.jobGroup(), invokeTarget,
                        1, "调用目标格式错误: " + invokeTarget, startTime, LocalDateTime.now());
                return;
            }
            String beanName = invokeTarget.substring(0, dotIndex);
            String methodName = invokeTarget.substring(dotIndex + 1);

            Object bean = applicationContext.getBean(beanName);
            Method method = bean.getClass().getMethod(methodName);

            // 安全校验：目标方法必须标注 @JobHandler
            if (!method.isAnnotationPresent(JobHandler.class)) {
                log.error("定时任务安全拦截：方法 {} 未标注 @JobHandler，拒绝执行", invokeTarget);
                jobLogService.record(job.id(), job.jobName(), job.jobGroup(), invokeTarget,
                        1, "方法未标注 @JobHandler，拒绝执行", startTime, LocalDateTime.now());
                return;
            }

            method.invoke(bean);

            LocalDateTime endTime = LocalDateTime.now();
            log.info("定时任务执行成功: {}", job.jobName());
            jobLogService.record(job.id(), job.jobName(), job.jobGroup(), invokeTarget,
                    0, null, startTime, endTime);
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            log.error("定时任务执行失败: {} - {}", job.jobName(), e.getMessage());
            jobLogService.record(job.id(), job.jobName(), job.jobGroup(), invokeTarget,
                    1, e.getMessage(), startTime, endTime);
        }
    }
}
