package com.nxboot.common.exception;

/**
 * 资源不存在异常
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String resource, Object id) {
        super(ErrorCode.NOT_FOUND, resource + " 不存在: " + id);
    }

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
