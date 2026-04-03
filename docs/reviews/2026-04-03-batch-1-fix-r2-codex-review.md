# Codex Re-review — 2026-04-03 Batch 1 Fix Round 2

## Scope

- Fix commit: `9174aa0`
- Submission commit: `a49d8a9`
- Submission: `docs/reviews/2026-04-03-batch-1-fix-r2-submission.md`
- Verdict: `PASS`

## Closed Findings

### Closed. DefaultRedirect 兜底目标不存在

- `DefaultRedirect.tsx` 不再跳转到不存在的 `/403`
- 无菜单分支现在直接渲染明确的 403 提示和退出登录按钮
- 用户不会再落到路由错误边界

### Closed. V23 迁移不是幂等的

- `V23__grant_default_role_menus.sql` 已改为 `ON CONFLICT DO NOTHING`
- 重复执行不会因主键冲突失败
- 迁移具备可重复执行的安全性

## Verification Notes

- Re-ran:
  - `cd server && mvn compile -q`
  - `cd client && npx tsc --noEmit`
  - `cd client && pnpm build`
- All commands passed

## Residual Notes

- Current worktree still contains non-functional leftovers:
  - `client/tsconfig.tsbuildinfo` modified
  - `docs/reviews/README.md` untracked
- These do not block closing Batch 1, but should be cleaned up before future review rounds

## Result

- Batch 1 findings are now closed
- This review chain is complete
