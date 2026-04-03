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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 用户数据访问
 */
@Repository
public class UserRepository {

    private static final String TABLE = "sys_user";

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
        Condition extra = JooqHelper.keywordCondition(keyword, "username", "nickname");
        // 合并数据权限条件
        Condition dataScope = JooqHelper.dataScopeCondition();
        if (dataScope != null) {
            extra = extra != null ? extra.and(dataScope) : dataScope;
        }
        return JooqHelper.page(dsl, TABLE, extra, offset, size, r -> toVO(r, null));
    }

    /**
     * 根据 ID 查询用户
     */
    public UserVO findById(Long id) {
        Record record = JooqHelper.findById(dsl, TABLE, id);
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
                        .from(table(TABLE))
                        .where(field("username").eq(username))
                        .and(JooqHelper.notDeleted())
        );
    }

    /**
     * 插入用户，返回生成的 ID
     */
    public Long insert(String username, String password, String nickname,
                       String email, String phone, String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table(TABLE))
                .set(field("id"), id)
                .set(field("username"), username)
                .set(field("password"), password)
                .set(field("nickname"), nickname)
                .set(field("email"), email)
                .set(field("phone"), phone)
                .set(field("remark"), remark)
                .set(field("enabled"), Constants.ENABLED)
                .set(field("create_by"), operator)
                .set(field("create_time"), now)
                .set(field("update_by"), operator)
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        return id;
    }

    /**
     * 更新用户
     */
    public void update(Long id, String nickname, String email, String phone,
                       String avatar, Integer enabled, String remark, String operator) {
        var step = dsl.update(table(TABLE))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (nickname != null) step = step.set(field("nickname"), nickname);
        if (email != null) step = step.set(field("email"), email);
        if (phone != null) step = step.set(field("phone"), phone);
        if (avatar != null) step = step.set(field("avatar"), avatar);
        if (enabled != null) step = step.set(field("enabled"), enabled);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    /**
     * 更新密码
     */
    public void updatePassword(Long id, String encodedPassword) {
        dsl.update(table(TABLE))
                .set(field("password"), encodedPassword)
                .set(field("update_time"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, TABLE, id, operator);
    }

    /**
     * 获取用户角色ID列表
     */
    public List<Long> getRoleIds(Long userId) {
        return dsl.select(field("role_id", Long.class))
                .from(table("sys_user_role"))
                .where(field("user_id").eq(userId))
                .fetchInto(Long.class);
    }

    /**
     * 保存用户角色关联
     */
    public void saveUserRoles(Long userId, List<Long> roleIds) {
        // 先删除旧的关联
        dsl.deleteFrom(table("sys_user_role"))
                .where(field("user_id").eq(userId))
                .execute();

        // 再插入新的关联
        if (roleIds != null && !roleIds.isEmpty()) {
            var insertStep = dsl.insertInto(table("sys_user_role"),
                    field("user_id"), field("role_id"));
            for (Long roleId : roleIds) {
                insertStep = insertStep.values(userId, roleId);
            }
            insertStep.execute();
        }
    }

    private UserVO toVO(Record r, List<Long> roleIds) {
        Integer enabledVal = r.get("enabled", Integer.class);
        return new UserVO(
                r.get("id", Long.class),
                r.get("username", String.class),
                r.get("nickname", String.class),
                r.get("email", String.class),
                r.get("phone", String.class),
                r.get("avatar", String.class),
                r.get("dept_id", Long.class),
                enabledVal != null && enabledVal == 1,
                r.get("remark", String.class),
                r.get("create_time", LocalDateTime.class),
                roleIds != null ? roleIds : Collections.emptyList()
        );
    }
}
