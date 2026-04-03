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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 角色数据访问
 */
@Repository
public class RoleRepository {

    private static final String TABLE = "sys_role";

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
        Condition extra = JooqHelper.keywordCondition(keyword, "role_key", "role_name");
        return JooqHelper.page(dsl, TABLE, extra, offset, size, r -> toVO(r, null));
    }

    /**
     * 查询所有角色
     */
    public List<RoleVO> findAll() {
        return dsl.select()
                .from(table(TABLE))
                .where(JooqHelper.notDeleted())
                .orderBy(field("sort_order").asc())
                .fetch(r -> toVO(r, null));
    }

    /**
     * 根据 ID 查询
     */
    public RoleVO findById(Long id) {
        Record record = JooqHelper.findById(dsl, TABLE, id);
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
                        .from(table(TABLE))
                        .where(field("role_key").eq(roleKey))
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

        dsl.insertInto(table(TABLE))
                .set(field("id"), id)
                .set(field("role_key"), roleKey)
                .set(field("role_name"), roleName)
                .set(field("sort_order"), sortOrder != null ? sortOrder : 0)
                .set(field("enabled"), Constants.ENABLED)
                .set(field("remark"), remark)
                .set(field("create_by"), operator)
                .set(field("create_time"), now)
                .set(field("update_by"), operator)
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        return id;
    }

    /**
     * 更新角色
     */
    public void update(Long id, String roleName, Integer sortOrder,
                       Integer enabled, String remark, String operator) {
        var step = dsl.update(table(TABLE))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (roleName != null) step = step.set(field("role_name"), roleName);
        if (sortOrder != null) step = step.set(field("sort_order"), sortOrder);
        if (enabled != null) step = step.set(field("enabled"), enabled);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, TABLE, id, operator);
    }

    /**
     * 获取角色菜单ID列表
     */
    public List<Long> getMenuIds(Long roleId) {
        return dsl.select(field("menu_id", Long.class))
                .from(table("sys_role_menu"))
                .where(field("role_id").eq(roleId))
                .fetchInto(Long.class);
    }

    /**
     * 保存角色菜单关联
     */
    public void saveRoleMenus(Long roleId, List<Long> menuIds) {
        dsl.deleteFrom(table("sys_role_menu"))
                .where(field("role_id").eq(roleId))
                .execute();

        if (menuIds != null && !menuIds.isEmpty()) {
            var insertStep = dsl.insertInto(table("sys_role_menu"),
                    field("role_id"), field("menu_id"));
            for (Long menuId : menuIds) {
                insertStep = insertStep.values(roleId, menuId);
            }
            insertStep.execute();
        }
    }

    private RoleVO toVO(Record r, List<Long> menuIds) {
        Integer enabledVal = r.get("enabled", Integer.class);
        return new RoleVO(
                r.get("id", Long.class),
                r.get("role_key", String.class),
                r.get("role_name", String.class),
                r.get("sort_order", Integer.class),
                enabledVal != null && enabledVal == 1,
                r.get("remark", String.class),
                r.get("create_time", LocalDateTime.class),
                menuIds != null ? menuIds : Collections.emptyList()
        );
    }
}
