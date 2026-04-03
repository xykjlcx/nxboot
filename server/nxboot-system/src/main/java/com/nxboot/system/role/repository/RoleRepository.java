package com.nxboot.system.role.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.role.model.RoleVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.nxboot.generated.jooq.tables.SysRole.SYS_ROLE;
import static com.nxboot.generated.jooq.tables.SysRoleMenu.SYS_ROLE_MENU;

/**
 * 角色数据访问
 */
@Repository
public class RoleRepository {

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public RoleRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    /**
     * 分页查询
     */
    public PageResult<RoleVO> page(int offset, int size, String keyword) {
        Condition extra = JooqHelper.keywordCondition(keyword, SYS_ROLE.ROLE_KEY, SYS_ROLE.ROLE_NAME);
        return JooqHelper.page(dsl, SYS_ROLE, extra, offset, size, r -> toVO(r, null));
    }

    /**
     * 查询所有角色
     */
    public List<RoleVO> findAll() {
        return dsl.select()
                .from(SYS_ROLE)
                .where(JooqHelper.notDeleted())
                .orderBy(SYS_ROLE.SORT_ORDER.asc())
                .fetch(r -> toVO(r, null));
    }

    /**
     * 根据 ID 查询
     */
    public RoleVO findById(Long id) {
        Record record = JooqHelper.findById(dsl, SYS_ROLE, id);
        if (record == null) {
            return null;
        }

        List<Long> menuIds = getMenuIds(id);
        return toVO(record, menuIds);
    }

    /**
     * 检查角色标识是否已存在
     */
    public boolean existsByRoleKey(String roleKey) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(SYS_ROLE)
                        .where(SYS_ROLE.ROLE_KEY.eq(roleKey))
                        .and(JooqHelper.notDeleted())
        );
    }

    /**
     * 插入角色，返回 ID
     */
    public Long insert(String roleKey, String roleName, Integer sortOrder,
                       String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(SYS_ROLE)
                .set(SYS_ROLE.ID, id)
                .set(SYS_ROLE.ROLE_KEY, roleKey)
                .set(SYS_ROLE.ROLE_NAME, roleName)
                .set(SYS_ROLE.SORT_ORDER, sortOrder != null ? sortOrder : 0)
                .set(SYS_ROLE.ENABLED, Constants.ENABLED)
                .set(SYS_ROLE.REMARK, remark)
                .set(SYS_ROLE.CREATE_BY, operator)
                .set(SYS_ROLE.CREATE_TIME, now)
                .set(SYS_ROLE.UPDATE_BY, operator)
                .set(SYS_ROLE.UPDATE_TIME, now)
                .set(SYS_ROLE.DELETED, Constants.NOT_DELETED)
                .execute();

        return id;
    }

    /**
     * 更新角色
     */
    public void update(Long id, String roleName, Integer sortOrder,
                       Integer enabled, String remark, String operator) {
        var step = dsl.update(SYS_ROLE)
                .set(SYS_ROLE.UPDATE_BY, operator)
                .set(SYS_ROLE.UPDATE_TIME, LocalDateTime.now());

        if (roleName != null) step = step.set(SYS_ROLE.ROLE_NAME, roleName);
        if (sortOrder != null) step = step.set(SYS_ROLE.SORT_ORDER, sortOrder);
        if (enabled != null) step = step.set(SYS_ROLE.ENABLED, enabled);
        if (remark != null) step = step.set(SYS_ROLE.REMARK, remark);

        step.where(SYS_ROLE.ID.eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_ROLE, id, operator);
    }

    /**
     * 获取角色菜单ID列表
     */
    public List<Long> getMenuIds(Long roleId) {
        return dsl.select(SYS_ROLE_MENU.MENU_ID)
                .from(SYS_ROLE_MENU)
                .where(SYS_ROLE_MENU.ROLE_ID.eq(roleId))
                .fetchInto(Long.class);
    }

    /**
     * 保存角色菜单关联
     */
    public void saveRoleMenus(Long roleId, List<Long> menuIds) {
        dsl.deleteFrom(SYS_ROLE_MENU)
                .where(SYS_ROLE_MENU.ROLE_ID.eq(roleId))
                .execute();

        if (menuIds != null && !menuIds.isEmpty()) {
            var insertStep = dsl.insertInto(SYS_ROLE_MENU,
                    SYS_ROLE_MENU.ROLE_ID, SYS_ROLE_MENU.MENU_ID);
            for (Long menuId : menuIds) {
                insertStep = insertStep.values(roleId, menuId);
            }
            insertStep.execute();
        }
    }

    private RoleVO toVO(Record r, List<Long> menuIds) {
        Integer enabledVal = r.get(SYS_ROLE.ENABLED);
        return new RoleVO(
                r.get(SYS_ROLE.ID),
                r.get(SYS_ROLE.ROLE_KEY),
                r.get(SYS_ROLE.ROLE_NAME),
                r.get(SYS_ROLE.SORT_ORDER),
                enabledVal != null && enabledVal == 1,
                r.get(SYS_ROLE.REMARK),
                r.get(SYS_ROLE.CREATE_TIME),
                menuIds != null ? menuIds : Collections.emptyList()
        );
    }
}
