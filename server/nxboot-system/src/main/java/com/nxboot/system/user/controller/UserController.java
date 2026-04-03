package com.nxboot.system.user.controller;

import com.nxboot.common.annotation.Log;
import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.framework.excel.ExcelHelper;
import com.nxboot.system.user.model.UserCommand;
import com.nxboot.system.user.model.UserExcelVO;
import com.nxboot.system.user.model.UserVO;
import com.nxboot.system.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
    @Log(module = "用户管理", operation = "新增用户")
    @PostMapping
    @PreAuthorize("@perm.has('system:user:create')")
    public R<Long> create(@Valid @RequestBody UserCommand.Create cmd) {
        return R.ok(userService.create(cmd));
    }

    /**
     * 更新用户
     */
    @Log(module = "用户管理", operation = "修改用户")
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:update')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserCommand.Update cmd) {
        userService.update(id, cmd);
        return R.ok();
    }

    /**
     * 重置密码
     */
    @Log(module = "用户管理", operation = "重置密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("@perm.has('system:user:resetPwd')")
    public R<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserCommand.ResetPassword cmd) {
        userService.resetPassword(id, cmd);
        return R.ok();
    }

    /**
     * 导出用户列表
     */
    @Log(module = "用户管理", operation = "导出用户")
    @GetMapping("/export")
    @PreAuthorize("@perm.has('system:user:list')")
    public void export(HttpServletResponse response, @RequestParam(required = false) String keyword) throws IOException {
        List<UserVO> list = userService.listAll(keyword);
        List<UserExcelVO> excelData = list.stream().map(UserExcelVO::from).toList();
        ExcelHelper.write(response, "用户列表", UserExcelVO.class, excelData);
    }

    /**
     * 删除用户
     */
    @Log(module = "用户管理", operation = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }
}
