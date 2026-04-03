package com.nxboot.system.role.controller;

import com.nxboot.common.annotation.Log;
import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.role.model.RoleCommand;
import com.nxboot.system.role.model.RoleVO;
import com.nxboot.system.role.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口
 */
@RestController
@RequestMapping("/api/v1/system/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("@perm.has('system:role:list')")
    public R<PageResult<RoleVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(roleService.page(new PageQuery(pageNum, pageSize), keyword));
    }

    @GetMapping("/all")
    @PreAuthorize("@perm.has('system:role:list')")
    public R<List<RoleVO>> listAll() {
        return R.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:role:list')")
    public R<RoleVO> getById(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @Log(module = "角色管理", operation = "新增角色")
    @PostMapping
    @PreAuthorize("@perm.has('system:role:create')")
    public R<Long> create(@Valid @RequestBody RoleCommand.Create cmd) {
        return R.ok(roleService.create(cmd));
    }

    @Log(module = "角色管理", operation = "修改角色")
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:role:update')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleCommand.Update cmd) {
        roleService.update(id, cmd);
        return R.ok();
    }

    @Log(module = "角色管理", operation = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:role:delete')")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }
}
