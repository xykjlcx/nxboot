# Codex Review — 2026-04-03 Codegen Dashboard

## Scope

- Review range: `ffb1730..b23c6a0`
- Submission: `docs/reviews/2026-04-03-codegen-dashboard-submission.md`
- Verdict: `FIX`

## Findings

### P2. Monitor 页面在接口失败时会一直卡在“加载中”

- File: `client/src/features/system/monitor/pages/Monitor.tsx`

问题：

- 页面只读取了 `isLoading` 和 `data`
- 条件 `isLoading || !data` 会把“请求失败、403、后端未重启、接口异常”全部吞成同一个 loading 分支
- 当前实现没有任何错误态、重试入口或权限提示

结果：

- 一旦 `/api/v1/system/monitor/server` 返回错误
- 用户看到的不是失败原因，而是一个永远不消失的 `Spin`
- 这和 submission 里承诺的“产品级可用页面”不一致，尤其在你们自己也写明“需重启后端 jar 才能验证 API 回归”的前提下更容易发生

修复要求：

- 区分 loading 和 error
- 至少给出明确错误态（如 `Result` / `Alert`）和重试方式
- 对 403 或接口不可用场景，不能继续伪装成“正在加载”

### P2. JooqHelper 的核心字段仍然是字符串引用，codegen 迁移并未真正完成

- File: `server/nxboot-framework/src/main/java/com/nxboot/framework/jooq/JooqHelper.java`

问题：

- `ID`、`CREATE_BY`、`CREATE_TIME`、`UPDATE_BY`、`UPDATE_TIME`、`DELETED`、`VERSION`
  这 7 个字段常量仍然通过 `DSL.field("...")` 构造
- `page`、`findById`、`softDelete`、`setAuditInsert`、`setAuditUpdate`、`optimisticUpdate`
  这些核心 helper 仍然依赖这组字符串字段
- submission 里写的是“消除 JooqHelper + 全部 Repository + Service/Aspect 层的 field/table 字符串引用”，并声称 `DSL.field` 只剩 DataScopeAspect 一处，这和实际代码不一致

结果：

- 这轮迁移并没有把共享层真正提升到编译期可校验
- 审计字段或软删字段一旦改名，helper 仍然会静默通过编译、留到运行时炸
- 这不是文档措辞问题，而是迁移目标没有真正达成

修复要求：

- 要么把 `JooqHelper` 收敛成真正的 codegen/typed 方案
- 要么下调 submission 里的目标与验收结论，如实说明 JooqHelper 仍保留字符串字段依赖
- 不能继续把“helper 里还在用 `DSL.field(\"...\")`”描述成“已完成全量迁移”

## Verification Notes

- Re-ran:
  - `mvn -f server/pom.xml compile -q`
  - `cd client && npx tsc --noEmit`
  - `cd client && pnpm build`
- All commands passed
- Frontend build still warns about a large main chunk, but I did not treat that as a blocking finding in this round
- Current worktree is not clean:
  - `client/tsconfig.tsbuildinfo` modified
  - `docs/reviews/2026-04-03-batch-1-fix-r2-codex-review.md` untracked
  - `docs/reviews/README.md` untracked

## Next Step

- Claude fixes only the findings above
- Claude updates the submission and returns to `READY_FOR_REVIEW`
- Codex performs re-review
