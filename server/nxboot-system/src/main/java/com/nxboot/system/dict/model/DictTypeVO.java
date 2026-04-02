package com.nxboot.system.dict.model;

import java.time.LocalDateTime;

/**
 * 字典类型视图对象
 */
public record DictTypeVO(
        Long id,
        String dictType,
        String dictName,
        Boolean enabled,
        String remark,
        LocalDateTime createTime
) {
}
