package com.nxboot.system.user.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.user.model.UserVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.nxboot.generated.jooq.tables.SysUser.SYS_USER;
import static com.nxboot.generated.jooq.tables.SysUserRole.SYS_USER_ROLE;

/**
 * 用户数据访问
 */
@Repository
public class UserRepository {

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public UserRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    /**
     * 分页查询用户
     */
    public PageResult<UserVO> page(int offset, int size, String keyword) {
        Condition extra = JooqHelper.keywordCondition(keyword, SYS_USER.USERNAME, SYS_USER.NICKNAME);
        // 合并数据权限条件
        Condition dataScope = JooqHelper.dataScopeCondition();
        if (dataScope != null) {
            extra = extra != null ? extra.and(dataScope) : dataScope;
        }
        return JooqHelper.page(dsl, SYS_USER, extra, offset, size, r -> toVO(r, null));
    }

    /**
     * 根据 ID 查询用户
     */
    public UserVO findById(Long id) {
        Record record = JooqHelper.findById(dsl, SYS_USER, id);
        if (record == null) {
            return null;
        }

        List<Long> roleIds = getRoleIds(id);
        return toVO(record, roleIds);
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean existsByUsername(String username) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(SYS_USER)
                        .where(SYS_USER.USERNAME.eq(username))
                        .and(SYS_USER.DELETED.eq(Constants.NOT_DELETED))
        );
    }

    /**
     * 插入用户，返回生成的 ID
     */
    public Long insert(String username, String password, String nickname,
                       String email, String phone, String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(SYS_USER)
                .set(SYS_USER.ID, id)
                .set(SYS_USER.USERNAME, username)
                .set(SYS_USER.PASSWORD, password)
                .set(SYS_USER.NICKNAME, nickname)
                .set(SYS_USER.EMAIL, email)
                .set(SYS_USER.PHONE, phone)
                .set(SYS_USER.REMARK, remark)
                .set(SYS_USER.ENABLED, Constants.ENABLED)
                .set(SYS_USER.CREATE_BY, operator)
                .set(SYS_USER.CREATE_TIME, now)
                .set(SYS_USER.UPDATE_BY, operator)
                .set(SYS_USER.UPDATE_TIME, now)
                .set(SYS_USER.DELETED, Constants.NOT_DELETED)
                .execute();

        return id;
    }

    /**
     * 更新用户
     */
    public void update(Long id, String nickname, String email, String phone,
                       String avatar, Integer enabled, String remark, String operator) {
        var step = dsl.update(SYS_USER)
                .set(SYS_USER.UPDATE_BY, operator)
                .set(SYS_USER.UPDATE_TIME, LocalDateTime.now());

        if (nickname != null) step = step.set(SYS_USER.NICKNAME, nickname);
        if (email != null) step = step.set(SYS_USER.EMAIL, email);
        if (phone != null) step = step.set(SYS_USER.PHONE, phone);
        if (avatar != null) step = step.set(SYS_USER.AVATAR, avatar);
        if (enabled != null) step = step.set(SYS_USER.ENABLED, enabled);
        if (remark != null) step = step.set(SYS_USER.REMARK, remark);

        step.where(SYS_USER.ID.eq(id)).and(SYS_USER.DELETED.eq(Constants.NOT_DELETED)).execute();
    }

    /**
     * 更新密码
     */
    public void updatePassword(Long id, String encodedPassword) {
        dsl.update(SYS_USER)
                .set(SYS_USER.PASSWORD, encodedPassword)
                .set(SYS_USER.UPDATE_TIME, LocalDateTime.now())
                .where(SYS_USER.ID.eq(id))
                .execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_USER, id, operator);
    }

    /**
     * 获取用户角色ID列表
     */
    public List<Long> getRoleIds(Long userId) {
        return dsl.select(SYS_USER_ROLE.ROLE_ID)
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .fetchInto(Long.class);
    }

    /**
     * 保存用户角色关联
     */
    public void saveUserRoles(Long userId, List<Long> roleIds) {
        // 先删除旧的关联
        dsl.deleteFrom(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .execute();

        // 再插入新的关联
        if (roleIds != null && !roleIds.isEmpty()) {
            var insertStep = dsl.insertInto(SYS_USER_ROLE,
                    SYS_USER_ROLE.USER_ID, SYS_USER_ROLE.ROLE_ID);
            for (Long roleId : roleIds) {
                insertStep = insertStep.values(userId, roleId);
            }
            insertStep.execute();
        }
    }

    private UserVO toVO(Record r, List<Long> roleIds) {
        Integer enabledVal = r.get(SYS_USER.ENABLED);
        return new UserVO(
                r.get(SYS_USER.ID),
                r.get(SYS_USER.USERNAME),
                r.get(SYS_USER.NICKNAME),
                r.get(SYS_USER.EMAIL),
                r.get(SYS_USER.PHONE),
                r.get(SYS_USER.AVATAR),
                r.get(SYS_USER.DEPT_ID),
                enabledVal != null && enabledVal == 1,
                r.get(SYS_USER.REMARK),
                r.get(SYS_USER.CREATE_TIME),
                roleIds != null ? roleIds : Collections.emptyList()
        );
    }
}
