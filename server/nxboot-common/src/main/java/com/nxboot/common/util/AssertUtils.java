package com.nxboot.common.util;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.exception.NotFoundException;

/**
 * 业务断言工具
 */
public final class AssertUtils {

    private AssertUtils() {
    }

    /**
     * 断言对象不为 null，否则抛出 NotFoundException
     */
    public static void notNull(Object obj, String resource, Object id) {
        if (obj == null) {
            throw new NotFoundException(resource, id);
        }
    }

    /**
     * 断言表达式为 true，否则抛出 BusinessException
     */
    public static void isTrue(boolean expression, ErrorCode errorCode) {
        if (!expression) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言表达式为 true，否则抛出自定义消息的 BusinessException
     */
    public static void isTrue(boolean expression, ErrorCode errorCode, String message) {
        if (!expression) {
            throw new BusinessException(errorCode, message);
        }
    }
}
