# Batch B Fix: assignable 作用域 + 存量数据修复 — Submission

## 基本信息

- 主题：修复 Codex Review P1（assignable 作用域过宽）+ P2（存量坏数据未修）
- 提交范围：MenuRepository.java + V26 Flyway 迁移
- 关联 commit：441f5cf
- 当前状态：`READY_FOR_REVIEW`

## 目标

1. 让 assignable 作用域与 `UserDetailsServiceImpl.loadPermissions()` 的真实权限语义完全一致
2. 自动修复历史环境中 sys_role_menu 缺失的祖先目录节点

## 改动范围

- 后端：
  - `MenuRepository.java`：重写 `buildAssignableMenuTree()` 和 `getAssignableMenuIds()` 的非 admin 查询，添加 `SYS_ROLE.DELETED`/`SYS_ROLE.ENABLED` + `SYS_MENU.DELETED`/`SYS_MENU.ENABLED` 四重过滤；提取公用 `isAdmin()` 私有方法（含 enabled 检查）
- 数据库 / Flyway：
  - `V26__backfill_role_menu_ancestors.sql`：递归 CTE 回填缺失祖先节点
- 前端：无改动

## 非目标

- 不改 `buildUserMenuTree()`（导航树构建，不在本轮 findings 范围内）
- 不改 `UserDetailsServiceImpl`（它已经是正确的，本轮是让 assignable 与它对齐）

## P1 Fix 说明：assignable 作用域对齐真实权限

### 修复前

`buildAssignableMenuTree()` 和 `getAssignableMenuIds()` 的非 admin 路径：
```sql
SELECT DISTINCT menu_id FROM sys_role_menu rm
JOIN sys_user_role ur ON rm.role_id = ur.role_id
WHERE ur.user_id = ?
```
**缺失**：不检查角色是否 enabled/deleted，不检查菜单是否 enabled/deleted。

### 修复后

```sql
SELECT DISTINCT menu_id FROM sys_role_menu rm
JOIN sys_user_role ur ON rm.role_id = ur.role_id
JOIN sys_role r ON ur.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE ur.user_id = ?
  AND r.deleted = 0 AND r.enabled = 1
  AND m.deleted = 0 AND m.enabled = 1
```

与 `loadPermissions()` 中的查询完全一致的过滤语义。

### admin 检查也同步修复

旧代码只检查 `role.deleted`，现在同时检查 `role.enabled`。提取为公用 `isAdmin()` 方法，两个 assignable 方法复用。

### 行为对比

| 场景 | 修复前 | 修复后 |
|------|--------|--------|
| 角色被禁用 | 用户仍能分配该角色下的菜单 | 菜单不在可分配范围内 |
| 角色被软删除 | 用户仍能分配该角色下的菜单 | 菜单不在可分配范围内 |
| 菜单被禁用 | 用户仍能分配该菜单 | 菜单不在可分配范围内 |
| 菜单被软删除 | 用户仍能分配该菜单 | 菜单不在可分配范围内 |
| admin 用户 | 全量（含禁用菜单） | 全量（含禁用菜单，与原行为一致） |

## P2 Fix 说明：V26 回填存量祖先节点

### 迁移逻辑

```sql
WITH RECURSIVE menu_ancestors AS (
    -- 起点：所有现有 role_menu 关联的直接父节点
    SELECT rm.role_id, m.parent_id AS ancestor_id
    FROM sys_role_menu rm
    JOIN sys_menu m ON rm.menu_id = m.id
    WHERE m.parent_id != 0 AND m.deleted = 0

    UNION

    -- 递归向上：祖先的父节点
    SELECT ma.role_id, m.parent_id AS ancestor_id
    FROM menu_ancestors ma
    JOIN sys_menu m ON ma.ancestor_id = m.id
    WHERE m.parent_id != 0 AND m.deleted = 0
)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT role_id, ancestor_id
FROM menu_ancestors WHERE ancestor_id != 0
ON CONFLICT DO NOTHING;
```

### 幂等性

- `ON CONFLICT DO NOTHING`：已存在的 (role_id, menu_id) 不会报错
- 重复执行 V26（如 baseline-on-migrate 场景）无副作用
- 递归 CTE 的 UNION（非 UNION ALL）自动去重，不会死循环

### 效果

- 升级到此版本后，Flyway 自动执行 V26
- 所有历史角色的 role_menu 自动补齐缺失的 M 类型父目录
- 无需管理员手动重新编辑角色

## 验证命令

```bash
mvn -f server/pom.xml compile -q
cd client && npx tsc --noEmit
cd client && pnpm build
```

## 验证结果

- 后端编译通过
- 前端类型检查通过
- 前端生产构建通过（chunk 警告为 P3 已知项，不影响功能）

## 风险点

- V26 递归 CTE 在 menu 层级极深时有性能开销，但实际系统菜单通常 3-4 层，无问题
- admin 的 `isAdmin()` 检查现在多了 `enabled` 条件——如果 admin 角色本身被禁用，该用户将不再被视为 admin。这是正确行为（与 loadPermissions 一致）

## 未解决事项

无。本轮只修两个 findings，无新增问题。

## 请求红队重点关注

1. assignable 的四重过滤是否与 `loadPermissions()` 完全等价（尤其是 JOIN 链路和 WHERE 条件）
2. V26 递归 CTE 的终止条件是否可靠（`parent_id != 0` + `deleted = 0`）
3. `isAdmin()` 加了 `enabled` 检查后，是否会有边界场景影响 admin 的正常操作
