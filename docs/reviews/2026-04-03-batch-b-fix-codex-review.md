# Batch B Fix: assignable 作用域 + 存量数据修复 — Codex Review

- 审查日期：2026-04-03
- 审查范围：`441f5cf` + `docs/reviews/2026-04-03-batch-b-fix-submission.md`
- 裁决：`FIX`

## Findings

### P1. 角色编辑页会把“只有 C 菜单权限、没有 F 按钮权限”的角色显示成空授权，并在保存时清空其权限

这轮对 `RoleForm` 的回显策略改成了“只回显叶子节点”，具体体现在：

- `collectLeafKeys()` 只收集树中的叶子节点
- `useEffect()` 中 `checkedKeys` 只用这些叶子节点初始化
- 提交时又直接把 `checkedKeys + halfCheckedKeys` 回写给后端

但 `RoleRepository.getMenuIds()` 返回的是角色真实持有的全部 `menuIds`，包括 C 类型页面菜单本身。

于是会出现一个明确的回归场景：

1. 某个角色是“只读角色”，只持有 `system:dict:list` 这类 C 菜单权限
2. 因为该 C 菜单下面现在有 F 按钮子节点，它在树里不再是叶子
3. `collectLeafKeys()` 不会把它回显出来
4. 角色编辑页看起来像“没有任何菜单权限”
5. 管理员如果只是修改备注/状态后点击保存，`menuIds` 会以空数组提交
6. `saveRoleMenus()` 先删后插，最终把该角色原有权限直接清空

这不是显示小问题，而是 **编辑角色时可能发生静默的数据丢失**。

## 已关闭的问题

本轮前一个 review 中的两条问题已经关闭：

1. `assignable` 作用域现在已经和 `loadPermissions()` 一样过滤 `role.enabled/deleted` 与 `menu.enabled/deleted`
2. `V26__backfill_role_menu_ancestors.sql` 已经为历史坏数据补祖先节点，且实现是幂等的

所以这轮不通过，不是因为原问题没修，而是因为新回显逻辑引入了一个新的高风险回归。

## 我复跑的验证

执行通过：

- `mvn -f server/pom.xml compile -q`
- `cd client && npx tsc --noEmit`
- `cd client && pnpm build`

所以问题仍然不是编译层，而是角色权限编辑流程的行为回归。

## 建议修复方向

1. 编辑态回显必须保真
   - 不能只按叶子节点回显
   - 必须保证“角色当前真实持有的权限”在树中可视且可保留

2. 至少覆盖以下角色场景：
   - 只有 C 菜单、没有 F 按钮
   - C + 部分 F
   - 只有祖先 M + C
   - 老数据经 V26 修复后的角色

3. 如果当前 Tree 组件在非 `checkStrictly` 模式下不好做精确回显，就应调整交互或数据模型，但不能牺牲编辑保真性

## 结论

这轮还不能 `PASS`。

修完这个角色编辑回显/保存回归后，再交下一轮 submission，我继续复审。
