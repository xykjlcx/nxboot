package com.nxboot.system.auth.controller;

import com.nxboot.common.result.R;
import com.nxboot.system.auth.model.OnlineUser;
import com.nxboot.system.auth.service.OnlineUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户管理接口
 */
@RestController
@RequestMapping("/api/v1/system/online-users")
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    public OnlineUserController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    /** 查询在线用户列表 */
    @GetMapping
    @PreAuthorize("@perm.has('system:online:list')")
    public R<List<OnlineUser>> list() {
        return R.ok(onlineUserService.list());
    }

    /** 强制下线 */
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("@perm.has('system:online:forceLogout')")
    public R<Void> forceLogout(@PathVariable String sessionId) {
        onlineUserService.forceLogout(sessionId);
        return R.ok();
    }
}
