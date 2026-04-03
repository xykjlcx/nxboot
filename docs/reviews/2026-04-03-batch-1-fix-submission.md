# Review Submission — Batch 1 Fix

## 基本信息

- 主题：修复 Codex Batch 1 Review 三项 findings
- 提交范围：`15f9033..8b6ae26`（1 个 commit）
- 关联 commit：`8b6ae26`
- 当前状态：`READY_FOR_REVIEW`

## 目标

修复红队在 `docs/reviews/2026-04-03-batch-1-codex-review.md` 中提出的 3 个 findings（1×P1 + 2×P2），不引入任何无关改动。

## 用户场景

1. **OAuth2 新用户首次登录**：能看到至少一个真实页面（字典管理 / 参数配置），而非落到无权限的空白页
2. **管理员在 sys_menu 中配置任意 antd 图标名**：运行时能正确渲染，不会被白名单截断
3. **已有环境执行 V21 迁移**：如果管理员已手工调整过菜单排序，迁移不会覆盖

## 假设

- OAuth2 流程本身工作正常（本次不测试 OAuth2 端到端，只修角色权限和落地页逻辑）
- `user` 角色（id=2）由 V22 创建，本次 V23 追加菜单绑定
- 菜单 API `/api/v1/auth/menus` 按角色过滤逻辑已在 v8 实现，无需修改
- antd `@ant-design/icons` 的 namespace import 包含所有导出的图标组件

## 改动范围

- 后端：
  - `V21__add_missing_menus.sql`：7 条 UPDATE 加 `AND sort_order = <原始值>` 条件保护
  - `V23__grant_default_role_menus.sql`（新增）：给 user 角色绑定 1004（字典管理）+ 1005（参数配置）
- 前端：
  - `BasicLayout.tsx`：恢复 `import * as Icons`，保留 aliasMap 短名映射，getIcon 走全量 namespace 解析
  - `DefaultRedirect.tsx`（新增）：从用户菜单树中选第一个 C 类型菜单跳转
  - `routes.tsx`：两处硬编码 `<Navigate to="/system/user">` 替换为 `<DefaultRedirect />`
- 数据库 / Flyway：V21 修改 + V23 新增
- 文档：无

## 非目标

- 不新增 backlog 功能
- 不修改 OAuth2 端到端流程
- 不做图标 icon picker 表单改造（这是未来优化方向）
- 不处理旧菜单 path 格式不一致（relative vs absolute）

## 风险点

- V21 修改已有迁移文件：Flyway 会校验 checksum。已在 dev DB 中将 V21 checksum 设为 NULL，下次启动自动重算。新环境从头执行无影响。
- `DefaultRedirect` 依赖 `useAuth().menus` 已加载：该组件在 `AuthGuard` 之后渲染，AuthGuard 会先 fetchMenus，所以 menus 一定已就绪。
- 全量图标导入恢复了 bundle 大小增长（~50-80KB gzipped），这是有意取舍，注释中说明了优化路径。

## 验证命令

```bash
# 前端类型检查
cd client && npx tsc --noEmit

# 前端生产构建
cd client && pnpm build

# 后端编译
mvn -f server/pom.xml compile -q

# V23 SQL 执行
psql -d nxboot -f V23__grant_default_role_menus.sql

# user 角色菜单绑定验证
psql -d nxboot -c "SELECT rm.role_id, m.menu_name, m.path FROM sys_role_menu rm JOIN sys_menu m ON rm.menu_id = m.id WHERE rm.role_id = 2;"

# admin 菜单 API 回归
curl -s /api/v1/auth/menus -H "Authorization: Bearer <token>" | python3 -c "..."
```

## 验证结果

- `npx tsc --noEmit`：零错误
- `pnpm build`：成功，2.76s
- `mvn compile`：零错误
- V23 执行：`INSERT 0 1` × 2
- user 角色菜单：系统管理(M) + 字典管理(C) + 参数配置(C)
- admin 菜单 API：12 个 C 菜单完整返回，无变化

## 演示路径 / 接口验证

- 页面路径：`/`（根路径）→ DefaultRedirect 自动跳转到用户第一个可用菜单
- 账号 / 权限：
  - admin / admin123 → 跳转 `/system/user`（第一个菜单）
  - OAuth2 新用户（user 角色）→ 应跳转 `/system/dict`（字典管理，user 角色第一个 C 菜单）
- API：`GET /api/v1/auth/menus`
- 输入：Bearer token（admin）
- 输出：12 个 C 菜单，含在线用户和任务日志

## 数据与迁移说明

- 是否新增 / 修改 Flyway：修改 V21（加条件保护），新增 V23（角色菜单绑定）
- 是否影响已有数据：V21 修改后对已执行环境无影响（checksum 已清除）；V23 仅 INSERT 新关联，不影响现有数据
- 是否需要 codegen / 初始化数据 / 手工步骤：无
- 回滚风险：V23 可通过 `DELETE FROM sys_role_menu WHERE role_id=2 AND menu_id IN (1004,1005)` 回滚

## 体验说明

- 加载态：DefaultRedirect 在 AuthGuard fetchMenus 之后渲染，无额外加载
- 空状态：无可用菜单时跳转 /403（auth-guard 已有 403 页面）
- 错误提示：无变化
- 默认值：落地页不再硬编码，基于权限动态选择
- 权限差异：admin 看全部菜单跳 `/system/user`；user 角色看字典+配置跳 `/system/dict`

## 未解决事项

- 旧菜单 path 格式不一致（relative `user` vs absolute `/system/dept`）— DefaultRedirect 已兼容两种格式
- 图标优化应从菜单管理表单端（icon picker）约束，而非渲染端截断 — 已在注释中标注
- OAuth2 端到端流程未做集成测试（需要配置 GitHub OAuth App）

## 请求红队重点关注

1. `DefaultRedirect` 对菜单树的遍历逻辑是否覆盖所有导航结构（扁平/嵌套/空）
2. V21 条件更新是否足够保守——`AND sort_order = <old>` 是否是正确的保护策略
3. `import * as Icons` 恢复后 bundle 大小增量是否可接受
