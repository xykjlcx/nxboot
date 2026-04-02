package com.nxboot.system.menu.controller;

import com.nxboot.common.result.R;
import com.nxboot.system.menu.model.MenuCommand;
import com.nxboot.system.menu.model.MenuVO;
import com.nxboot.system.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理接口
 */
@RestController
@RequestMapping("/api/v1/system/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 查询菜单树
     */
    @GetMapping("/tree")
    @PreAuthorize("@perm.has('system:menu:list')")
    public R<List<MenuVO>> tree() {
        return R.ok(menuService.tree());
    }

    /**
     * 查询所有菜单（平铺列表）
     */
    @GetMapping
    @PreAuthorize("@perm.has('system:menu:list')")
    public R<List<MenuVO>> listAll() {
        return R.ok(menuService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:menu:list')")
    public R<MenuVO> getById(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@perm.has('system:menu:create')")
    public R<Long> create(@Valid @RequestBody MenuCommand.Create cmd) {
        return R.ok(menuService.create(cmd));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:menu:update')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MenuCommand.Update cmd) {
        menuService.update(id, cmd);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:menu:delete')")
    public R<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return R.ok();
    }
}
