package com.nxboot.system.menu.repository;

import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.system.menu.model.MenuVO;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 菜单数据访问
 */
@Repository
public class MenuRepository {

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public MenuRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    /**
     * 查询所有菜单（平铺列表）
     */
    public List<MenuVO> findAll() {
        return dsl.select()
                .from(table("sys_menu"))
                .where(field("deleted").eq(Constants.NOT_DELETED))
                .orderBy(field("sort_order").asc())
                .fetch(r -> toVO(r));
    }

    /**
     * 构建菜单树
     */
    public List<MenuVO> buildTree() {
        List<MenuVO> allMenus = findAll();
        return buildTree(allMenus, 0L);
    }

    /**
     * 根据 ID 查询
     */
    public MenuVO findById(Long id) {
        Record record = dsl.select()
                .from(table("sys_menu"))
                .where(field("id").eq(id))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();
        return record != null ? toVO(record) : null;
    }

    /**
     * 插入菜单，返回 ID
     */
    public Long insert(Long parentId, String menuName, String menuType, String path,
                       String component, String permission, String icon,
                       Integer sortOrder, Integer visible, Integer enabled, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table("sys_menu"))
                .set(field("id"), id)
                .set(field("parent_id"), parentId != null ? parentId : 0L)
                .set(field("menu_name"), menuName)
                .set(field("menu_type"), menuType)
                .set(field("path"), path)
                .set(field("component"), component)
                .set(field("permission"), permission)
                .set(field("icon"), icon)
                .set(field("sort_order"), sortOrder != null ? sortOrder : 0)
                .set(field("visible"), visible != null ? visible : 1)
                .set(field("enabled"), enabled != null ? enabled : 1)
                .set(field("create_by"), operator)
                .set(field("create_time"), now)
                .set(field("update_by"), operator)
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        return id;
    }

    /**
     * 更新菜单
     */
    public void update(Long id, Long parentId, String menuName, String menuType, String path,
                       String component, String permission, String icon,
                       Integer sortOrder, Integer visible, Integer enabled, String operator) {
        var step = dsl.update(table("sys_menu"))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (parentId != null) step = step.set(field("parent_id"), parentId);
        if (menuName != null) step = step.set(field("menu_name"), menuName);
        if (menuType != null) step = step.set(field("menu_type"), menuType);
        if (path != null) step = step.set(field("path"), path);
        if (component != null) step = step.set(field("component"), component);
        if (permission != null) step = step.set(field("permission"), permission);
        if (icon != null) step = step.set(field("icon"), icon);
        if (sortOrder != null) step = step.set(field("sort_order"), sortOrder);
        if (visible != null) step = step.set(field("visible"), visible);
        if (enabled != null) step = step.set(field("enabled"), enabled);

        step.where(field("id").eq(id))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        dsl.update(table("sys_menu"))
                .set(field("deleted"), Constants.DELETED)
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }

    /**
     * 检查是否有子菜单
     */
    public boolean hasChildren(Long parentId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(table("sys_menu"))
                        .where(field("parent_id").eq(parentId))
                        .and(field("deleted").eq(Constants.NOT_DELETED))
        );
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildTree(List<MenuVO> allMenus, Long parentId) {
        Map<Long, List<MenuVO>> grouped = allMenus.stream()
                .collect(Collectors.groupingBy(MenuVO::parentId));

        return buildChildren(grouped, parentId);
    }

    private List<MenuVO> buildChildren(Map<Long, List<MenuVO>> grouped, Long parentId) {
        List<MenuVO> children = grouped.getOrDefault(parentId, new ArrayList<>());
        return children.stream()
                .map(menu -> new MenuVO(
                        menu.id(), menu.parentId(), menu.menuName(), menu.menuType(),
                        menu.path(), menu.component(), menu.permission(), menu.icon(),
                        menu.sortOrder(), menu.visible(), menu.enabled(), menu.createTime(),
                        buildChildren(grouped, menu.id())
                ))
                .toList();
    }

    private MenuVO toVO(Record r) {
        Integer visibleVal = r.get(field("visible", Integer.class));
        Integer enabledVal = r.get(field("enabled", Integer.class));
        return new MenuVO(
                r.get(field("id", Long.class)),
                r.get(field("parent_id", Long.class)),
                r.get(field("menu_name", String.class)),
                r.get(field("menu_type", String.class)),
                r.get(field("path", String.class)),
                r.get(field("component", String.class)),
                r.get(field("permission", String.class)),
                r.get(field("icon", String.class)),
                r.get(field("sort_order", Integer.class)),
                visibleVal != null && visibleVal == 1,
                enabledVal != null && enabledVal == 1,
                r.get(field("create_time", LocalDateTime.class)),
                null
        );
    }
}
