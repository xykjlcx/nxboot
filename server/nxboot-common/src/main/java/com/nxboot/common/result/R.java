package com.nxboot.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应体
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record R<T>(int code, String msg, T data) {

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> ok() {
        return new R<>(200, "success", null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }
}
