package com.nxboot.system.user.controller;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.user.model.UserCommand;
import com.nxboot.system.user.model.UserVO;
import com.nxboot.system.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/v1/system/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页查询
     */
    @GetMapping
    @PreAuthorize("@perm.has('system:user:list')")
    public R<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(userService.page(new PageQuery(pageNum, pageSize), keyword));
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:list')")
    public R<UserVO> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("@perm.has('system:user:create')")
    public R<Long> create(@Valid @RequestBody UserCommand.Create cmd) {
        return R.ok(userService.create(cmd));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:update')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserCommand.Update cmd) {
        userService.update(id, cmd);
        return R.ok();
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("@perm.has('system:user:resetPwd')")
    public R<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserCommand.ResetPassword cmd) {
        userService.resetPassword(id, cmd);
        return R.ok();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }
}
