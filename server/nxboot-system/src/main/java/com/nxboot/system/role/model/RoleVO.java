package com.nxboot.system.role.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色视图对象
 */
public record RoleVO(
        Long id,
        String roleKey,
        String roleName,
        Integer sortOrder,
        Boolean enabled,
        String remark,
        LocalDateTime createTime,
        List<Long> menuIds
) {
}
