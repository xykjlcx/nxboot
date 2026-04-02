package com.nxboot.system.role.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.role.model.RoleCommand;
import com.nxboot.system.role.model.RoleVO;
import com.nxboot.system.role.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
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
            roleRepository.saveRoleMenus(roleId, cmd.menuIds());
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
            roleRepository.saveRoleMenus(id, cmd.menuIds());
        }
    }

    @Transactional
    public void delete(Long id) {
        AssertUtils.notNull(roleRepository.findById(id), "角色", id);
        String operator = SecurityUtils.getCurrentUsername();
        roleRepository.softDelete(id, operator);
    }
}
