package com.nxboot.system.job.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.system.job.model.JobLogVO;
import com.nxboot.system.job.repository.JobLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志服务
 */
@Service
public class JobLogService {

    private final JobLogRepository jobLogRepository;

    public JobLogService(JobLogRepository jobLogRepository) {
        this.jobLogRepository = jobLogRepository;
    }

    /**
     * 分页查询执行日志
     */
    public PageResult<JobLogVO> page(PageQuery query, Long jobId, String keyword, Integer status) {
        return jobLogRepository.page(query.offset(), query.pageSize(), jobId, keyword, status);
    }

    /**
     * 记录任务执行日志
     */
    @Transactional
    public void record(Long jobId, String jobName, String jobGroup, String invokeTarget,
                       int status, String errorMsg, LocalDateTime startTime, LocalDateTime endTime) {
        long duration = java.time.Duration.between(startTime, endTime).toMillis();
        jobLogRepository.insert(jobId, jobName, jobGroup, invokeTarget, status, errorMsg,
                startTime, endTime, duration);
    }
}
