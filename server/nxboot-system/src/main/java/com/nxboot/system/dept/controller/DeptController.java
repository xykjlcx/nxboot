package com.nxboot.system.dept.controller;

import com.nxboot.common.annotation.Log;
import com.nxboot.common.result.R;
import com.nxboot.system.dept.model.DeptCommand;
import com.nxboot.system.dept.model.DeptVO;
import com.nxboot.system.dept.service.DeptService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理接口
 */
@RestController
@RequestMapping("/api/v1/system/depts")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    /**
     * 查询部门树
     */
    @GetMapping("/tree")
    @PreAuthorize("@perm.has('system:dept:list')")
    public R<List<DeptVO>> tree() {
        return R.ok(deptService.tree());
    }

    /**
     * 查询部门详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:dept:list')")
    public R<DeptVO> getById(@PathVariable Long id) {
        return R.ok(deptService.getById(id));
    }

    /**
     * 创建部门
     */
    @Log(module = "部门管理", operation = "新增部门")
    @PostMapping
    @PreAuthorize("@perm.has('system:dept:create')")
    public R<Long> create(@Valid @RequestBody DeptCommand.Create cmd) {
        return R.ok(deptService.create(cmd));
    }

    /**
     * 更新部门
     */
    @Log(module = "部门管理", operation = "修改部门")
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:dept:update')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DeptCommand.Update cmd) {
        deptService.update(id, cmd);
        return R.ok();
    }

    /**
     * 删除部门
     */
    @Log(module = "部门管理", operation = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:dept:delete')")
    public R<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return R.ok();
    }
}
