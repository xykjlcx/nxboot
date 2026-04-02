package com.nxboot.system.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 用户操作命令
 */
public final class UserCommand {

    private UserCommand() {
    }

    /**
     * 创建用户
     */
    public record Create(
            @NotBlank(message = "用户名不能为空")
            @Size(min = 2, max = 64, message = "用户名长度为 2-64 个字符")
            String username,

            @NotBlank(message = "密码不能为空")
            @Size(min = 6, max = 32, message = "密码长度为 6-32 个字符")
            String password,

            String nickname,
            String email,
            String phone,
            String remark,
            List<Long> roleIds
    ) {
    }

    /**
     * 更新用户
     */
    public record Update(
            String nickname,
            String email,
            String phone,
            String avatar,
            Integer enabled,
            String remark,
            List<Long> roleIds
    ) {
    }

    /**
     * 重置密码
     */
    public record ResetPassword(
            @NotBlank(message = "新密码不能为空")
            @Size(min = 6, max = 32, message = "密码长度为 6-32 个字符")
            String newPassword
    ) {
    }
}
