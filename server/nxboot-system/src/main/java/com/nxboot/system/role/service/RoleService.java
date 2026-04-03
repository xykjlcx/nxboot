package com.nxboot.system.role.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.menu.repository.MenuRepository;
import com.nxboot.system.role.model.RoleCommand;
import com.nxboot.system.role.model.RoleVO;
import com.nxboot.system.role.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色服务
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    public RoleService(RoleRepository roleRepository, MenuRepository menuRepository) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
    }

    public PageResult<RoleVO> page(PageQuery query, String keyword) {
        return roleRepository.page(query.offset(), query.pageSize(), keyword);
    }

    public List<RoleVO> listAll() {
        return roleRepository.findAll();
    }

    public RoleVO getById(Long id) {
        RoleVO role = roleRepository.findById(id);
        AssertUtils.notNull(role, "角色", id);
        return role;
    }

    @Transactional
    public Long create(RoleCommand.Create cmd) {
        AssertUtils.isTrue(!roleRepository.existsByRoleKey(cmd.roleKey()),
                ErrorCode.BIZ_DUPLICATE, "角色标识已存在: " + cmd.roleKey());

        String operator = SecurityUtils.getCurrentUsername();
        Long roleId = roleRepository.insert(cmd.roleKey(), cmd.roleName(),
                cmd.sortOrder(), cmd.remark(), operator);

        if (cmd.menuIds() != null && !cmd.menuIds().isEmpty()) {
            List<Long> safeMenuIds = validateAndCompleteMenuIds(cmd.menuIds());
            roleRepository.saveRoleMenus(roleId, safeMenuIds);
        }

        return roleId;
    }

    @Transactional
    public void update(Long id, RoleCommand.Update cmd) {
        AssertUtils.notNull(roleRepository.findById(id), "角色", id);
        String operator = SecurityUtils.getCurrentUsername();

        roleRepository.update(id, cmd.roleName(), cmd.sortOrder(),
                cmd.enabled(), cmd.remark(), operator);

        if (cmd.menuIds() != null) {
            List<Long> safeMenuIds = validateAndCompleteMenuIds(cmd.menuIds());
            roleRepository.saveRoleMenus(id, safeMenuIds);
        }
    }

    @Transactional
    public void delete(Long id) {
        AssertUtils.notNull(roleRepository.findById(id), "角色", id);
        String operator = SecurityUtils.getCurrentUsername();
        roleRepository.softDelete(id, operator);
    }

    /**
     * 校验授权作用域 + 自动补齐祖先节点
     *
     * 1. 校验：提交的 menuIds 必须在当前用户可分配范围内
     * 2. 补齐：自动添加缺失的祖先目录节点，确保菜单树完整
     */
    private List<Long> validateAndCompleteMenuIds(List<Long> menuIds) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 校验授权作用域：不能分配自己没有的菜单
        Set<Long> assignable = menuRepository.getAssignableMenuIds(currentUserId);
        for (Long menuId : menuIds) {
            if (!assignable.contains(menuId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN,
                        "无权分配菜单 ID: " + menuId);
            }
        }

        // 自动补齐祖先节点
        Map<Long, Long> parentIdMap = menuRepository.getParentIdMap();
        Set<Long> result = new LinkedHashSet<>(menuIds);

        for (Long menuId : menuIds) {
            Long current = menuId;
            while (current != null && current != 0L) {
                Long parentId = parentIdMap.get(current);
                if (parentId == null || parentId == 0L) break;
                if (!result.add(parentId)) break; // 已存在则停止向上
                current = parentId;
            }
        }

        return new ArrayList<>(result);
    }
}
