package com.nxboot.system.role.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 角色操作命令
 */
public final class RoleCommand {

    private RoleCommand() {
    }

    /**
     * 创建角色
     */
    public record Create(
            @NotBlank(message = "角色标识不能为空")
            String roleKey,

            @NotBlank(message = "角色名称不能为空")
            String roleName,

            Integer sortOrder,
            String remark,
            List<Long> menuIds
    ) {
    }

    /**
     * 更新角色
     */
    public record Update(
            String roleName,
            Integer sortOrder,
            Integer enabled,
            String remark,
            List<Long> menuIds
    ) {
    }
}
