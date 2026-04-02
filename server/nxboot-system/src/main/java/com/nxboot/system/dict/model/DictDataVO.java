package com.nxboot.system.dict.model;

import java.time.LocalDateTime;

/**
 * 字典数据视图对象
 */
public record DictDataVO(
        Long id,
        String dictType,
        String dictLabel,
        String dictValue,
        Integer sortOrder,
        Boolean enabled,
        String remark,
        LocalDateTime createTime
) {
}
