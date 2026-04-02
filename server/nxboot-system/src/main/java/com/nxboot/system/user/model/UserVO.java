package com.nxboot.system.user.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 */
public record UserVO(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        Boolean enabled,
        String remark,
        LocalDateTime createTime,
        List<Long> roleIds
) {
}
