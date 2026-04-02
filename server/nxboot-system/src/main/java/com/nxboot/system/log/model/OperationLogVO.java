package com.nxboot.system.log.model;

import java.time.LocalDateTime;

/**
 * 操作日志视图对象
 */
public record OperationLogVO(
        Long id,
        String module,
        String operation,
        String method,
        String requestUrl,
        String requestMethod,
        String requestParams,
        String responseBody,
        String operator,
        String operatorIp,
        Integer status,
        String errorMsg,
        Long duration,
        LocalDateTime createTime
) {
}
