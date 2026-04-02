package com.nxboot.system.menu.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单视图对象（支持树形结构）
 */
public record MenuVO(
        Long id,
        Long parentId,
        String menuName,
        String menuType,
        String path,
        String component,
        String permission,
        String icon,
        Integer sortOrder,
        Boolean visible,
        Boolean enabled,
        LocalDateTime createTime,
        List<MenuVO> children
) {
}
