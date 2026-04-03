package com.nxboot.system.user.service;

import com.nxboot.common.annotation.DataScope;
import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;

import java.util.List;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.user.model.UserCommand;
import com.nxboot.system.user.model.UserVO;
import com.nxboot.system.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 分页查询（数据权限过滤）
     */
    @DataScope
    public PageResult<UserVO> page(PageQuery query, String keyword) {
        return userRepository.page(query.offset(), query.pageSize(), keyword);
    }

    /**
     * 查询全部（导出用，不分页）
     */
    public List<UserVO> listAll(String keyword) {
        return userRepository.page(0, Integer.MAX_VALUE, keyword).list();
    }

    /**
     * 根据 ID 查询
     */
    public UserVO getById(Long id) {
        UserVO user = userRepository.findById(id);
        AssertUtils.notNull(user, "用户", id);
        return user;
    }

    /**
     * 创建用户
     */
    @Transactional
    public Long create(UserCommand.Create cmd) {
        AssertUtils.isTrue(!userRepository.existsByUsername(cmd.username()),
                ErrorCode.BIZ_DUPLICATE, "用户名已存在: " + cmd.username());

        String encodedPassword = passwordEncoder.encode(cmd.password());
        String operator = SecurityUtils.getCurrentUsername();

        Long userId = userRepository.insert(
                cmd.username(), encodedPassword, cmd.nickname(),
                cmd.email(), cmd.phone(), cmd.remark(), operator);

        // 保存角色关联
        if (cmd.roleIds() != null && !cmd.roleIds().isEmpty()) {
            userRepository.saveUserRoles(userId, cmd.roleIds());
        }

        return userId;
    }

    /**
     * 更新用户
     */
    @Transactional
    public void update(Long id, UserCommand.Update cmd) {
        AssertUtils.notNull(userRepository.findById(id), "用户", id);
        String operator = SecurityUtils.getCurrentUsername();

        userRepository.update(id, cmd.nickname(), cmd.email(), cmd.phone(),
                cmd.avatar(), cmd.enabled(), cmd.remark(), operator);

        // 更新角色关联
        if (cmd.roleIds() != null) {
            userRepository.saveUserRoles(id, cmd.roleIds());
        }
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long id, UserCommand.ResetPassword cmd) {
        AssertUtils.notNull(userRepository.findById(id), "用户", id);
        String encodedPassword = passwordEncoder.encode(cmd.newPassword());
        userRepository.updatePassword(id, encodedPassword);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Transactional
    public void delete(Long id) {
        AssertUtils.notNull(userRepository.findById(id), "用户", id);
        String operator = SecurityUtils.getCurrentUsername();
        userRepository.softDelete(id, operator);
    }
}
