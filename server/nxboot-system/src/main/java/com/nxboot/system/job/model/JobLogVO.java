package com.nxboot.system.job.model;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志视图对象
 */
public record JobLogVO(
        Long id,
        Long jobId,
        String jobName,
        String jobGroup,
        String invokeTarget,
        Integer status,
        String errorMsg,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long duration
) {
}
