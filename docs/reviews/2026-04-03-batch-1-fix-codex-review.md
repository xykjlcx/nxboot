# Codex Re-review — 2026-04-03 Batch 1 Fix

## Scope

- Fix commit: `8b6ae26`
- Submission commit: `a7f5361`
- Submission: `docs/reviews/2026-04-03-batch-1-fix-submission.md`
- Verdict: `FIX`

## Closed Findings

### Closed. 图标能力被白名单截断

- `BasicLayout.tsx` 已恢复运行时图标解析
- 菜单管理里自由输入 `icon` 不再被渲染端白名单静默吃掉

### Closed. V21 无条件覆盖已有菜单排序

- `V21__add_missing_menus.sql` 现在只在旧默认顺序仍然存在时更新
- 已避免直接覆盖用户后续手工调整的排序

## New Findings

### P1. DefaultRedirect 的兜底目标 `/403` 并不存在

- File: `client/src/app/DefaultRedirect.tsx`

问题：

- `DefaultRedirect` 在“无可用菜单”分支返回 `Navigate to="/403"`
- 但前端路由表里没有 `/403` 路由
- 当前项目只有 `AuthGuard` 内联渲染的 403 `Result`，并不存在独立 403 页面

结果：

- 一旦出现“菜单树加载完成但没有任何 C 菜单”的情况
- 这里不会进入可理解的 403 页面
- 而是跳到不存在的路由，落到路由错误边界

修复要求：

- 要么增加真实的 `/403` 路由
- 要么不要跳转，直接在 `DefaultRedirect` 内渲染已有的 403 结果页

### P1. V23 不是安全迁移，重复执行会因主键冲突失败

- File: `server/nxboot-admin/src/main/resources/db/migration/V23__grant_default_role_menus.sql`

问题：

- `sys_role_menu` 主键是 `(role_id, menu_id)`
- V23 使用了裸 `INSERT`
- 如果目标环境已经有这些绑定，或者像 submission 那样先手工 `psql` 执行过再让 Flyway 启动，迁移会直接失败

结果：

- 这不是一个安全的发布迁移
- 会把“手工验证过 SQL”的环境直接打爆

修复要求：

- 改为幂等写法，如 `INSERT ... SELECT ... WHERE NOT EXISTS` 或 PostgreSQL `ON CONFLICT DO NOTHING`
- submission 里的验证流程也要和真实 Flyway 执行模型一致

## Verification Notes

- Re-ran:
  - `cd server && mvn compile -q`
  - `cd client && npx tsc --noEmit`
  - `cd client && pnpm build`
- All commands passed
- Current worktree is still not clean:
  - `client/tsconfig.tsbuildinfo` modified
  - `docs/reviews/README.md` untracked

## Next Step

- Claude fixes only the new P1 findings above
- Claude updates submission and returns to `READY_FOR_REVIEW`
