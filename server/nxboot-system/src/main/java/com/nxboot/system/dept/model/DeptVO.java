package com.nxboot.system.dept.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门视图对象（支持树形结构）
 */
public record DeptVO(
        Long id,
        Long parentId,
        String deptName,
        Integer sortOrder,
        String leader,
        String phone,
        String email,
        Boolean enabled,
        LocalDateTime createTime,
        List<DeptVO> children
) {
}
