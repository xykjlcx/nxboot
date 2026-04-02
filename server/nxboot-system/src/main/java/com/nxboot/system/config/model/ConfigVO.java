package com.nxboot.system.config.model;

import java.time.LocalDateTime;

/**
 * 系统配置视图对象
 */
public record ConfigVO(
        Long id,
        String configKey,
        String configValue,
        String configName,
        String remark,
        LocalDateTime createTime
) {
}
