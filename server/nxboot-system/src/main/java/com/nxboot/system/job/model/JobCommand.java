package com.nxboot.system.job.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 定时任务操作命令
 */
public final class JobCommand {

    private JobCommand() {
    }

    public record Create(
            @NotBlank(message = "任务名称不能为空")
            String jobName,

            String jobGroup,

            @NotBlank(message = "调用目标不能为空")
            String invokeTarget,

            @NotBlank(message = "Cron 表达式不能为空")
            String cronExpression,

            Integer misfirePolicy,
            Integer concurrent,
            Integer enabled,
            String remark
    ) {
    }

    public record Update(
            String jobName,
            String jobGroup,
            String invokeTarget,
            String cronExpression,
            Integer misfirePolicy,
            Integer concurrent,
            Integer enabled,
            String remark
    ) {
    }
}
