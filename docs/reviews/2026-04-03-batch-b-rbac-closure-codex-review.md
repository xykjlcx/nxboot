# Batch B: 权限模型闭环 — Codex Review

- 审查日期：2026-04-03
- 审查范围：`ea8b99d` + `8545e02` + `docs/reviews/2026-04-03-batch-b-rbac-closure-submission.md`
- 裁决：`FIX`

## Findings

### P1. 可分配菜单作用域按“原始关联”计算，而不是按“当前实际生效权限”计算

`MenuRepository.getAssignableMenuIds()` 和 `buildAssignableMenuTree()` 当前都只是从 `sys_user_role -> sys_role_menu` 直接取菜单集合，没有像认证加载那样过滤：

- 角色是否 `enabled`
- 角色是否 `deleted`
- 菜单是否 `enabled`
- 菜单是否 `deleted`

但系统真正的权限生效逻辑在 `UserDetailsServiceImpl.loadPermissions()` 中是有这些过滤条件的。

这会造成一个实际的授权边界问题：

1. 用户曾经拥有某个角色/菜单
2. 后来角色被禁用或软删除，或者菜单被禁用
3. 用户当前已经不再实际拥有这些权限
4. 但 `/menus/assignable` 和 `validateAndCompleteMenuIds()` 仍然会把它们视为“可分配”

结果就是：**当前用户可以分配自己已经不再实际拥有的菜单权限。**

这和 submission 里“每级管理员只能分配自己拥有的菜单”不一致。

### P2. 父子闭环只修了未来写入，没有修已有脏数据

这轮确实修了“以后保存角色时自动补齐祖先节点”，但没有处理历史上已经写坏的 `role_menu` 数据。

问题在于：

- `buildUserMenuTree()` 仍然依赖父节点存在才能构树
- V25 迁移只新增了按钮权限种子
- 没有任何 Flyway 或后台修复逻辑去补旧角色缺失的祖先菜单

所以如果某个非 admin 角色是在这次修复前创建的，并且当时只保存了叶子节点菜单，那么升级到这版后：

- 这个角色的历史 `role_menu` 仍然是坏的
- 导航树仍然可能为空
- 只有管理员手动重新编辑并保存一次，才会被新逻辑修正

这意味着 **P1-3 并没有对已有环境真正闭环**，只对新写入闭环。

## 我复跑的验证

执行通过：

- `mvn -f server/pom.xml compile -q`
- `cd client && npx tsc --noEmit`
- `cd client && pnpm build`

所以这轮不是“编译错误”，而是：

- 一条授权边界问题
- 一条升级/存量数据修复问题

## 建议修复方向

1. `assignable` 作用域统一复用“真实有效权限”判定规则
   - 至少与 `UserDetailsServiceImpl.loadPermissions()` 的过滤条件保持一致
   - 不要直接按裸 `sys_user_role` / `sys_role_menu` 关系推导可分配集合

2. 为历史坏数据补一次祖先修复
   - Flyway 迁移批量补齐 `sys_role_menu` 里缺失的祖先 M 节点
   - 或在读取用户菜单树前做一次向上闭环修补
   - 更推荐迁移修复，避免运行时长期背着隐式修复逻辑

## 结论

这轮还不能 `PASS`。

修完以上两条后再交下一轮 submission，我继续复审。
