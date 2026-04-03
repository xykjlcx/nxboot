package com.nxboot.system.dept.service;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.dept.model.DeptCommand;
import com.nxboot.system.dept.model.DeptVO;
import com.nxboot.system.dept.repository.DeptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门服务
 */
@Service
public class DeptService {

    private final DeptRepository deptRepository;

    public DeptService(DeptRepository deptRepository) {
        this.deptRepository = deptRepository;
    }

    /**
     * 查询部门树
     */
    public List<DeptVO> tree() {
        return deptRepository.buildTree();
    }

    /**
     * 根据 ID 查询部门
     */
    public DeptVO getById(Long id) {
        DeptVO dept = deptRepository.findById(id);
        AssertUtils.notNull(dept, "部门", id);
        return dept;
    }

    /**
     * 创建部门
     */
    @Transactional
    public Long create(DeptCommand.Create cmd) {
        String operator = SecurityUtils.getCurrentUsername();
        return deptRepository.insert(cmd.parentId(), cmd.deptName(), cmd.sortOrder(),
                cmd.leader(), cmd.phone(), cmd.email(), operator);
    }

    /**
     * 更新部门
     */
    @Transactional
    public void update(Long id, DeptCommand.Update cmd) {
        AssertUtils.notNull(deptRepository.findById(id), "部门", id);
        String operator = SecurityUtils.getCurrentUsername();
        deptRepository.update(id, cmd.parentId(), cmd.deptName(), cmd.sortOrder(),
                cmd.leader(), cmd.phone(), cmd.email(), cmd.enabled(), operator);
    }

    /**
     * 删除部门
     */
    @Transactional
    public void delete(Long id) {
        AssertUtils.notNull(deptRepository.findById(id), "部门", id);

        // 检查是否有子部门
        if (deptRepository.hasChildren(id)) {
            throw new BusinessException(ErrorCode.BIZ_REFERENCED, "存在子部门，无法删除");
        }

        String operator = SecurityUtils.getCurrentUsername();
        deptRepository.softDelete(id, operator);
    }
}
