package com.nxboot.system.menu.service;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.menu.model.MenuCommand;
import com.nxboot.system.menu.model.MenuVO;
import com.nxboot.system.menu.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单服务
 */
@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * 查询菜单树（全量，管理用）
     */
    public List<MenuVO> tree() {
        return menuRepository.buildTree();
    }

    /**
     * 获取用户可访问的菜单树（根据角色过滤，排除按钮类型）
     */
    public List<MenuVO> getUserMenuTree(Long userId) {
        return menuRepository.buildUserMenuTree(userId);
    }

    /**
     * 当前用户可分配的菜单树（含按钮，用于角色授权表单）
     * admin 看全量，非 admin 只看自己拥有的菜单
     */
    public List<MenuVO> assignableTree() {
        Long userId = SecurityUtils.getCurrentUserId();
        return menuRepository.buildAssignableMenuTree(userId);
    }

    /**
     * 查询所有菜单（平铺）
     */
    public List<MenuVO> listAll() {
        return menuRepository.findAll();
    }

    public MenuVO getById(Long id) {
        MenuVO menu = menuRepository.findById(id);
        AssertUtils.notNull(menu, "菜单", id);
        return menu;
    }

    @Transactional
    public Long create(MenuCommand.Create cmd) {
        String operator = SecurityUtils.getCurrentUsername();
        return menuRepository.insert(cmd.parentId(), cmd.menuName(), cmd.menuType(),
                cmd.path(), cmd.component(), cmd.permission(), cmd.icon(),
                cmd.sortOrder(), cmd.visible(), cmd.enabled(), operator);
    }

    @Transactional
    public void update(Long id, MenuCommand.Update cmd) {
        AssertUtils.notNull(menuRepository.findById(id), "菜单", id);
        String operator = SecurityUtils.getCurrentUsername();
        menuRepository.update(id, cmd.parentId(), cmd.menuName(), cmd.menuType(),
                cmd.path(), cmd.component(), cmd.permission(), cmd.icon(),
                cmd.sortOrder(), cmd.visible(), cmd.enabled(), operator);
    }

    @Transactional
    public void delete(Long id) {
        AssertUtils.notNull(menuRepository.findById(id), "菜单", id);

        // 检查是否有子菜单
        if (menuRepository.hasChildren(id)) {
            throw new BusinessException(ErrorCode.BIZ_REFERENCED, "存在子菜单，无法删除");
        }

        String operator = SecurityUtils.getCurrentUsername();
        menuRepository.softDelete(id, operator);
    }
}
