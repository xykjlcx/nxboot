package com.nxboot.common.base;

import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果
 */
public record PageResult<T>(List<T> list, long total) {

    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0);
    }
}
