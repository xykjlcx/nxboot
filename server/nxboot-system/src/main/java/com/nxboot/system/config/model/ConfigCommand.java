package com.nxboot.system.config.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 系统配置操作命令
 */
public final class ConfigCommand {

    private ConfigCommand() {
    }

    public record Create(
            @NotBlank(message = "配置键不能为空")
            String configKey,

            String configValue,

            @NotBlank(message = "配置名称不能为空")
            String configName,

            String remark
    ) {
    }

    public record Update(
            String configValue,
            String configName,
            String remark
    ) {
    }
}
