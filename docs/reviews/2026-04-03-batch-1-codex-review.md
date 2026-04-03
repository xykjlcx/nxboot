# Codex Review — 2026-04-03 Batch 1

## Scope

- Review range: `1ab6dda..5ff4fd3`
- Review mode: old-flow batch closeout
- Verdict: `FIX`

## Findings

### P1. OAuth2 默认角色仍然没有任何可访问页面

- File: `server/nxboot-admin/src/main/resources/db/migration/V22__add_default_role.sql`
- Related files:
  - `server/nxboot-system/src/main/java/com/nxboot/system/auth/service/OAuth2Service.java`
  - `client/src/app/routes.tsx`

问题：

- `V22` 只给 `user` 角色绑定了顶级目录 `menu_id = 100`
- 非 admin 用户的菜单树只返回实际分配到的菜单项
- 前端首页仍然固定跳转到 `/system/user`
- 新 OAuth2 用户没有 `system:user:list` 权限

结果：

- 登录可以成功
- 但首次进入系统会落到没有权限的页面/API
- 这不满足“默认角色可用”的产品目标

修复要求：

- 要么给默认角色分配至少一个真实可访问页面
- 要么把登录后的默认落地页改成基于菜单权限动态选择
- 要么两者一起做，确保新 OAuth2 用户首次登录后有可用路径

### P2. 图标按需导入把菜单图标能力收窄成了硬编码白名单

- File: `client/src/app/layouts/BasicLayout.tsx`
- Related file:
  - `client/src/features/system/menu/pages/MenuForm.tsx`

问题：

- 旧实现支持任意 Ant Design 图标名
- 新实现只支持 `iconRegistry` 中的有限集合
- 菜单管理表单仍允许管理员自由输入 `icon`
- 未注册图标会静默回退成 `FileTextOutlined`

结果：

- 这是产品能力回退，不只是性能优化
- 现有环境里的自定义图标和后续新配置都可能失效

修复要求：

- 让图标能力与菜单配置能力重新一致
- 如果必须限制图标集合，就要同步限制菜单管理输入并给出明确可选项
- 不能继续保持“表单允许任意输入，但运行时只吃白名单”的状态

### P2. V21 会覆盖已有环境里手工调整过的菜单排序

- File: `server/nxboot-admin/src/main/resources/db/migration/V21__add_missing_menus.sql`

问题：

- `UPDATE sys_menu SET sort_order = ... WHERE id = ...` 没有保护条件
- 迁移会无条件重写 `1003-1009` 的排序
- `sys_menu` 是运行时可编辑数据，不只是初始化种子

结果：

- 已有实例里人工调整过的菜单顺序会被 Flyway 静默覆盖

修复要求：

- 把迁移改成更保守的条件更新，避免覆盖用户已经改过的运行时数据
- 如果必须调整排序，至少要限定“仅在旧默认排序仍然存在时才更新”

## Verification Notes

- Checked commit boundaries and implementation diffs in `1ab6dda..5ff4fd3`
- Re-ran:
  - `cd server && mvn compile -q`
  - `cd client && npx tsc --noEmit`
  - `cd client && pnpm build`
- Current head builds successfully, but the findings above remain product and migration issues

## Next Step

- Claude fixes only the findings above
- Claude generates a submission for the fix batch
- Status becomes `READY_FOR_REVIEW`
- Codex performs re-review
