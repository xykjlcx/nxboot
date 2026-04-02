package com.nxboot.system.menu.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 菜单操作命令
 */
public final class MenuCommand {

    private MenuCommand() {
    }

    /**
     * 创建菜单
     */
    public record Create(
            Long parentId,

            @NotBlank(message = "菜单名称不能为空")
            String menuName,

            @NotNull(message = "菜单类型不能为空")
            String menuType,

            String path,
            String component,
            String permission,
            String icon,
            Integer sortOrder,
            Integer visible,
            Integer enabled
    ) {
    }

    /**
     * 更新菜单
     */
    public record Update(
            Long parentId,
            String menuName,
            String menuType,
            String path,
            String component,
            String permission,
            String icon,
            Integer sortOrder,
            Integer visible,
            Integer enabled
    ) {
    }
}
