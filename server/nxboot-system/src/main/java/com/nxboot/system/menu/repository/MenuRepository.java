package com.nxboot.system.menu.repository;

import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.menu.model.MenuVO;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nxboot.generated.jooq.tables.SysMenu.SYS_MENU;
import static com.nxboot.generated.jooq.tables.SysRole.SYS_ROLE;
import static com.nxboot.generated.jooq.tables.SysRoleMenu.SYS_ROLE_MENU;
import static com.nxboot.generated.jooq.tables.SysUserRole.SYS_USER_ROLE;

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
                .from(SYS_MENU)
                .where(SYS_MENU.DELETED.eq(Constants.NOT_DELETED))
                .orderBy(SYS_MENU.SORT_ORDER.asc())
                .fetch(r -> toVO(r));
    }

    /**
     * 构建菜单树（全量）
     */
    public List<MenuVO> buildTree() {
        List<MenuVO> allMenus = findAll();
        return buildTree(allMenus, 0L);
    }

    /**
     * 构建用户可访问的菜单树（根据角色过滤，排除按钮类型）
     */
    public List<MenuVO> buildUserMenuTree(Long userId) {
        // 判断是否为管理员
        boolean isAdmin = dsl.fetchExists(
                dsl.selectOne()
                        .from(SYS_USER_ROLE)
                        .join(SYS_ROLE).on(SYS_USER_ROLE.ROLE_ID.eq(SYS_ROLE.ID))
                        .where(SYS_USER_ROLE.USER_ID.eq(userId))
                        .and(SYS_ROLE.ROLE_KEY.eq(Constants.ROLE_ADMIN))
                        .and(SYS_ROLE.DELETED.eq(Constants.NOT_DELETED))
        );

        List<MenuVO> menus;
        if (isAdmin) {
            // 管理员：返回所有目录和菜单（排除按钮）
            menus = dsl.select()
                    .from(SYS_MENU)
                    .where(SYS_MENU.DELETED.eq(Constants.NOT_DELETED))
                    .and(SYS_MENU.MENU_TYPE.ne("F"))
                    .and(SYS_MENU.ENABLED.eq(Constants.ENABLED))
                    .orderBy(SYS_MENU.SORT_ORDER.asc())
                    .fetch(this::toVO);
        } else {
            // 非管理员：先查角色关联的菜单 ID，再查菜单
            List<Long> menuIds = dsl.selectDistinct(SYS_ROLE_MENU.MENU_ID)
                    .from(SYS_ROLE_MENU)
                    .join(SYS_USER_ROLE).on(SYS_ROLE_MENU.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))
                    .where(SYS_USER_ROLE.USER_ID.eq(userId))
                    .fetchInto(Long.class);

            if (menuIds.isEmpty()) {
                return List.of();
            }

            menus = dsl.select()
                    .from(SYS_MENU)
                    .where(SYS_MENU.ID.in(menuIds))
                    .and(SYS_MENU.DELETED.eq(Constants.NOT_DELETED))
                    .and(SYS_MENU.MENU_TYPE.ne("F"))
                    .and(SYS_MENU.ENABLED.eq(Constants.ENABLED))
                    .orderBy(SYS_MENU.SORT_ORDER.asc())
                    .fetch(this::toVO);
        }

        return buildTree(menus, 0L);
    }

    /**
     * 构建当前用户可分配的菜单树（含 F 类型按钮，用于角色授权）
     * admin 返回全量，非 admin 只返回自己拥有的菜单
     */
    public List<MenuVO> buildAssignableMenuTree(Long userId) {
        boolean isAdmin = dsl.fetchExists(
                dsl.selectOne()
                        .from(SYS_USER_ROLE)
                        .join(SYS_ROLE).on(SYS_USER_ROLE.ROLE_ID.eq(SYS_ROLE.ID))
                        .where(SYS_USER_ROLE.USER_ID.eq(userId))
                        .and(SYS_ROLE.ROLE_KEY.eq(Constants.ROLE_ADMIN))
                        .and(SYS_ROLE.DELETED.eq(Constants.NOT_DELETED))
        );

        List<MenuVO> menus;
        if (isAdmin) {
            menus = findAll();
        } else {
            List<Long> menuIds = dsl.selectDistinct(SYS_ROLE_MENU.MENU_ID)
                    .from(SYS_ROLE_MENU)
                    .join(SYS_USER_ROLE).on(SYS_ROLE_MENU.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))
                    .where(SYS_USER_ROLE.USER_ID.eq(userId))
                    .fetchInto(Long.class);

            if (menuIds.isEmpty()) {
                return List.of();
            }

            menus = dsl.select()
                    .from(SYS_MENU)
                    .where(SYS_MENU.ID.in(menuIds))
                    .and(SYS_MENU.DELETED.eq(Constants.NOT_DELETED))
                    .and(SYS_MENU.ENABLED.eq(Constants.ENABLED))
                    .orderBy(SYS_MENU.SORT_ORDER.asc())
                    .fetch(this::toVO);
        }

        return buildTree(menus, 0L);
    }

    /**
     * 查询当前用户可分配的菜单 ID 集合（用于后端校验授权作用域）
     */
    public Set<Long> getAssignableMenuIds(Long userId) {
        boolean isAdmin = dsl.fetchExists(
                dsl.selectOne()
                        .from(SYS_USER_ROLE)
                        .join(SYS_ROLE).on(SYS_USER_ROLE.ROLE_ID.eq(SYS_ROLE.ID))
                        .where(SYS_USER_ROLE.USER_ID.eq(userId))
                        .and(SYS_ROLE.ROLE_KEY.eq(Constants.ROLE_ADMIN))
                        .and(SYS_ROLE.DELETED.eq(Constants.NOT_DELETED))
        );

        if (isAdmin) {
            return new HashSet<>(dsl.select(SYS_MENU.ID)
                    .from(SYS_MENU)
                    .where(SYS_MENU.DELETED.eq(Constants.NOT_DELETED))
                    .fetchInto(Long.class));
        }

        return new HashSet<>(dsl.selectDistinct(SYS_ROLE_MENU.MENU_ID)
                .from(SYS_ROLE_MENU)
                .join(SYS_USER_ROLE).on(SYS_ROLE_MENU.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .fetchInto(Long.class));
    }

    /**
     * 查询所有菜单的 id → parentId 映射（用于祖先补齐计算）
     */
    public Map<Long, Long> getParentIdMap() {
        return dsl.select(SYS_MENU.ID, SYS_MENU.PARENT_ID)
                .from(SYS_MENU)
                .where(SYS_MENU.DELETED.eq(Constants.NOT_DELETED))
                .fetchMap(SYS_MENU.ID, SYS_MENU.PARENT_ID);
    }

    /**
     * 根据 ID 查询
     */
    public MenuVO findById(Long id) {
        Record record = JooqHelper.findById(dsl, SYS_MENU, id);
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

        dsl.insertInto(SYS_MENU)
                .set(SYS_MENU.ID, id)
                .set(SYS_MENU.PARENT_ID, parentId != null ? parentId : 0L)
                .set(SYS_MENU.MENU_NAME, menuName)
                .set(SYS_MENU.MENU_TYPE, menuType)
                .set(SYS_MENU.PATH, path)
                .set(SYS_MENU.COMPONENT, component)
                .set(SYS_MENU.PERMISSION, permission)
                .set(SYS_MENU.ICON, icon)
                .set(SYS_MENU.SORT_ORDER, sortOrder != null ? sortOrder : 0)
                .set(SYS_MENU.VISIBLE, visible != null ? visible : 1)
                .set(SYS_MENU.ENABLED, enabled != null ? enabled : 1)
                .set(SYS_MENU.CREATE_BY, operator)
                .set(SYS_MENU.CREATE_TIME, now)
                .set(SYS_MENU.UPDATE_BY, operator)
                .set(SYS_MENU.UPDATE_TIME, now)
                .set(SYS_MENU.DELETED, Constants.NOT_DELETED)
                .execute();

        return id;
    }

    /**
     * 更新菜单
     */
    public void update(Long id, Long parentId, String menuName, String menuType, String path,
                       String component, String permission, String icon,
                       Integer sortOrder, Integer visible, Integer enabled, String operator) {
        var step = dsl.update(SYS_MENU)
                .set(SYS_MENU.UPDATE_BY, operator)
                .set(SYS_MENU.UPDATE_TIME, LocalDateTime.now());

        if (parentId != null) step = step.set(SYS_MENU.PARENT_ID, parentId);
        if (menuName != null) step = step.set(SYS_MENU.MENU_NAME, menuName);
        if (menuType != null) step = step.set(SYS_MENU.MENU_TYPE, menuType);
        if (path != null) step = step.set(SYS_MENU.PATH, path);
        if (component != null) step = step.set(SYS_MENU.COMPONENT, component);
        if (permission != null) step = step.set(SYS_MENU.PERMISSION, permission);
        if (icon != null) step = step.set(SYS_MENU.ICON, icon);
        if (sortOrder != null) step = step.set(SYS_MENU.SORT_ORDER, sortOrder);
        if (visible != null) step = step.set(SYS_MENU.VISIBLE, visible);
        if (enabled != null) step = step.set(SYS_MENU.ENABLED, enabled);

        step.where(SYS_MENU.ID.eq(id)).and(SYS_MENU.DELETED.eq(Constants.NOT_DELETED)).execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_MENU, id, operator);
    }

    /**
     * 检查是否有子菜单
     */
    public boolean hasChildren(Long parentId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(SYS_MENU)
                        .where(SYS_MENU.PARENT_ID.eq(parentId))
                        .and(SYS_MENU.DELETED.eq(Constants.NOT_DELETED))
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
        Integer visibleVal = r.get(SYS_MENU.VISIBLE);
        Integer enabledVal = r.get(SYS_MENU.ENABLED);
        return new MenuVO(
                r.get(SYS_MENU.ID),
                r.get(SYS_MENU.PARENT_ID),
                r.get(SYS_MENU.MENU_NAME),
                r.get(SYS_MENU.MENU_TYPE),
                r.get(SYS_MENU.PATH),
                r.get(SYS_MENU.COMPONENT),
                r.get(SYS_MENU.PERMISSION),
                r.get(SYS_MENU.ICON),
                r.get(SYS_MENU.SORT_ORDER),
                visibleVal != null && visibleVal == 1,
                enabledVal != null && enabledVal == 1,
                r.get(SYS_MENU.CREATE_TIME),
                null
        );
    }
}
