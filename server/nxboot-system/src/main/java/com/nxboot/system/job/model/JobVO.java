package com.nxboot.system.job.model;

import java.time.LocalDateTime;

/**
 * 定时任务视图对象
 */
public record JobVO(
        Long id,
        String jobName,
        String jobGroup,
        String invokeTarget,
        String cronExpression,
        Integer misfirePolicy,
        Integer concurrent,
        Boolean enabled,
        String remark,
        LocalDateTime createTime
) {
}
