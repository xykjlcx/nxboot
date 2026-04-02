package com.nxboot.common.base;

/**
 * 分页查询参数
 */
public record PageQuery(int pageNum, int pageSize) {

    public PageQuery {
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 20;
    }

    public PageQuery() {
        this(1, 20);
    }

    /**
     * 计算偏移量
     */
    public int offset() {
        return (pageNum - 1) * pageSize;
    }
}
