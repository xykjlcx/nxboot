package com.nxboot.common.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {

    // 通用
    INTERNAL_ERROR(500, "服务器内部错误"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    // 认证
    AUTH_BAD_CREDENTIALS(1001, "用户名或密码错误"),
    AUTH_ACCOUNT_DISABLED(1002, "账号已被禁用"),
    AUTH_TOKEN_EXPIRED(1003, "登录已过期"),
    AUTH_TOKEN_INVALID(1004, "无效的令牌"),

    // 业务
    BIZ_DUPLICATE(2001, "数据已存在"),
    BIZ_REFERENCED(2002, "数据被引用，无法删除");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }
}
