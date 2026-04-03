# Review Submission — Batch 1 Fix Round 2

## 基本信息

- 主题：修复 Codex Re-review 两项 P1 findings
- 提交范围：`a7f5361..9174aa0`（1 个 commit）
- 关联 commit：`9174aa0`
- 关联 review：`docs/reviews/2026-04-03-batch-1-fix-codex-review.md`
- 当前状态：`READY_FOR_REVIEW`

## 目标

修复红队在 Re-review 中提出的 2 个 P1：DefaultRedirect 兜底目标不存在 + V23 非幂等迁移。

## 用户场景

1. **无菜单权限的用户登录**：看到 antd 403 结果页（"暂无可访问页面，请联系管理员"）+ 退出登录按钮，而非路由错误边界的技术错误页
2. **V23 迁移在已有数据环境重复执行**：ON CONFLICT DO NOTHING 保证不报错，Flyway 正常通过

## 假设

- `useAuth()` 导出 `logout` 方法（已在 useAuth.ts 中确认存在）
- antd `Result` 组件在 BasicLayout 外也能正常渲染（DefaultRedirect 在 BasicLayout children 内，antd ConfigProvider 已包裹）
- PostgreSQL `ON CONFLICT DO NOTHING` 语法在 PG 16 可用（PG 9.5+ 支持）

## 改动范围

- 前端：`DefaultRedirect.tsx` — 无菜单分支从 `<Navigate to="/403">` 改为原地渲染 `<Result status="403">`
- 数据库：`V23__grant_default_role_menus.sql` — 两条 INSERT 加 `ON CONFLICT DO NOTHING`

## 非目标

- 不新增 /403 路由页面（直接渲染更简单且不依赖路由配置）
- 不修改其他文件
- 不处理 backlog

## 风险点

- DefaultRedirect 渲染 403 时处于 BasicLayout 内部，页面会同时显示侧边栏（如果有）+ 403 内容区。对于"无任何菜单"的用户，侧边栏为空，实际效果是全屏 403，可接受。
- V23 文件内容修改后需要清除 Flyway checksum（已执行 `UPDATE flyway_schema_history SET checksum = NULL WHERE version = '23'`）。新环境从头执行无影响。

## 验证命令

```bash
# 前端类型检查
cd client && npx tsc --noEmit

# 前端生产构建
cd client && pnpm build

# 后端编译
cd /path/to/nxboot && mvn -f server/pom.xml compile -q

# V23 幂等验证（当前 DB 已有 role_id=2,menu_id=1004/1005）
psql -d nxboot -c "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1004) ON CONFLICT DO NOTHING;"
psql -d nxboot -c "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1005) ON CONFLICT DO NOTHING;"
```

## 验证结果

- `npx tsc --noEmit`：零错误
- `pnpm build`：成功，2.59s
- `mvn compile -q`：零错误
- V23 幂等验证：`INSERT 0 0` × 2（已有数据，跳过插入，无报错）

## 演示路径 / 接口验证

- 正常用户（admin）：`/` → DefaultRedirect → 遍历菜单找到 `user` → `Navigate to="/system/user"`
- 无菜单用户：`/` → DefaultRedirect → 遍历菜单无 C 菜单 → 直接渲染 `Result status="403"` + 退出登录按钮
- 退出登录按钮：调用 `useAuth().logout()` → 清除 token → 跳转 `/login`

## 数据与迁移说明

- 是否新增 / 修改 Flyway：修改 V23（加 ON CONFLICT DO NOTHING）
- 是否影响已有数据：不影响，已有绑定被跳过
- 是否需要手工步骤：已执行环境需清除 V23 checksum（`UPDATE flyway_schema_history SET checksum = NULL WHERE version = '23'`）
- 回滚风险：无

## 体验说明

- 无菜单用户看到：居中 403 图标 + "暂无可访问页面" 标题 + "当前账号未分配任何菜单权限，请联系管理员" 副标题 + 蓝色"退出登录"按钮
- 不会看到路由错误边界的技术错误信息
- 退出登录后回到登录页，流程闭环

## 未解决事项

- `client/tsconfig.tsbuildinfo` 仍有未追踪变更（TypeScript 增量编译缓存，不影响功能）
- DefaultRedirect 的 403 页面在 BasicLayout 内渲染——如果未来 BasicLayout 侧边栏在空菜单时有异常表现，需要调整

## 请求红队重点关注

1. DefaultRedirect 在 BasicLayout children 内渲染 403 是否在视觉上可接受（vs 独立全屏 403 页面）
2. ON CONFLICT DO NOTHING 是否满足"幂等迁移"要求，还是需要更严格的幂等保证
