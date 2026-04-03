package com.nxboot.system.dept.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 部门操作命令
 */
public final class DeptCommand {

    private DeptCommand() {
    }

    /**
     * 创建部门
     */
    public record Create(
            @NotNull(message = "上级部门ID不能为空")
            Long parentId,

            @NotBlank(message = "部门名称不能为空")
            String deptName,

            Integer sortOrder,
            String leader,
            String phone,
            String email
    ) {
    }

    /**
     * 更新部门
     */
    public record Update(
            Long parentId,
            String deptName,
            Integer sortOrder,
            String leader,
            String phone,
            String email,
            Boolean enabled
    ) {
    }
}
