# Review Submission — Codegen 全量迁移 + 监控 Dashboard

## 基本信息

- 主题：jOOQ Codegen 全量迁移（JooqHelper + 11 Repository + 5 Service/Aspect）+ 监控 Dashboard 前端页面
- 提交范围：`c5ad6e9..HEAD`（8 个 commit）
- 关联 commit：`ffb1730`, `d7832fd`, `a04f159`, `46d1b0d`, `62a6c94`, `a322f94`, + Service/Aspect commit, + CLAUDE.md commit
- 当前状态：`READY_FOR_REVIEW`

## 目标

1. 消除 JooqHelper + 全部 Repository + Service/Aspect 层的 `field("xxx")` / `table("xxx")` 字符串引用，迁移到 jOOQ codegen 类型安全引用
2. 补全前端监控 Dashboard 页面（后端 API 已有，缺前端）

## 用户场景

1. **开发者新增/修改 Repository 代码**：所有字段引用在编译期检查，拼错字段名会编译报错而非运行时失败
2. **管理员访问服务器监控**：导航菜单出现"服务器监控"，点击进入 Dashboard 看到 CPU/内存/JVM/磁盘 4 张卡片，10 秒自动刷新

## 假设

- 所有 sys_ 表的 codegen 类已生成（16 张表，commit `ef0ae21`）
- DataScopeAspect 中的 `field()` 是运行时动态别名（`deptAlias + ".dept_id"`），无法用 codegen 替代，属于合理残留
- 监控 API `GET /api/v1/system/monitor/server` 已在 MonitorController 中实现并正常工作

## 改动范围

- 后端 framework 层：`JooqHelper.java` — API 从 `String tableName` 改为 `Table<?>` + 7 个 typed 审计字段常量
- 后端 system 层 Repository（11 个文件）：
  - LoginLogRepository, UserSocialRepository, JobLogRepository, LogRepository
  - JobRepository, DeptRepository, RoleRepository, UserRepository
  - MenuRepository, DictRepository, ConfigRepository
- 后端 system 层 Service/Aspect（5 个文件）：
  - FileService, OAuth2Service, AuthService, UserDetailsServiceImpl, DataScopeAspect
- 前端：`monitor/types.ts` + `monitor/api.ts` + `monitor/pages/Monitor.tsx` + `routes.tsx`
- 数据库：`V24__add_monitor_menu.sql`（幂等，ON CONFLICT DO NOTHING）
- 文档：`CLAUDE.md` 已知限制更新

## 非目标

- 不迁移 DataScopeAspect 中的运行时动态字段引用（5 处，合理残留）
- 不做图标 icon picker
- 不做 i18n 业务页面迁移
- 不做单元测试

## 风险点

- JooqHelper API 签名变更是破坏性改动——所有下游 Repository 必须同步更新。本次已全部更新。
- `keywordCondition` 参数从 `String...` 改为 `Field<String>...`，调用方需要传 codegen 字段引用而非字符串。已全部更新。
- V24 使用 `ON CONFLICT (id) DO NOTHING`，依赖 sys_menu 的 id 主键。已验证。

## 验证命令

```bash
# 后端全量编译
mvn -f server/pom.xml compile -q

# DSL.field/table 静态导入残留检查
grep -rn 'import static org.jooq.impl.DSL.field' server/nxboot-system/src/ server/nxboot-framework/src/ --include="*.java" | grep -v target
grep -rn 'import static org.jooq.impl.DSL.table' server/nxboot-system/src/ server/nxboot-framework/src/ --include="*.java" | grep -v target

# 前端类型检查 + 构建
cd client && npx tsc --noEmit
cd client && pnpm build
```

## 验证结果

- `mvn compile`：零错误
- `DSL.field` 残留：仅 `DataScopeAspect.java:21`（运行时动态别名，合理）
- `DSL.table` 残留：零
- `npx tsc --noEmit`：零错误
- `pnpm build`：成功（2.60s）

## 演示路径 / 接口验证

- 监控页面路径：`/system/monitor`
- 账号：admin / admin123
- API：`GET /api/v1/system/monitor/server`
- 权限：`system:monitor:list`
- 预期：4 张卡片（CPU/内存/JVM/磁盘），每张显示环形进度图 + 关键指标，10 秒刷新

## 数据与迁移说明

- V24：新增服务器监控菜单（id=1012）+ admin 角色绑定，ON CONFLICT DO NOTHING 幂等
- 无表结构变更
- 回滚：`DELETE FROM sys_role_menu WHERE menu_id=1012; DELETE FROM sys_menu WHERE id=1012;`

## 体验说明

- Dashboard 加载态：antd Spin 居中显示
- 数据刷新：10 秒 TanStack Query 轮询
- 色彩语义：绿色 <70%，黄色 70-90%，红色 >90%
- 响应式：xs=24（单列）→ sm=12（两列）→ xl=6（四列）

## 未解决事项

- DataScopeAspect 保留 5 处 `DSL.field()` 运行时调用（无法消除）
- 监控 Dashboard 暂无历史趋势图（当前只有实时快照）
- 需重新打包后端 jar 并重启才能验证 API 回归（编译已通过）

## 请求红队重点关注

1. JooqHelper `Table<?>` API 改造是否完整——特别是 `keywordCondition` 的 `@SafeVarargs` 和 `batchInsert` 的 `List<Field<?>>` 参数
2. Repository 迁移中多表关联（RoleRepository 4 表、UserDetailsServiceImpl 5 表、DictRepository 2 表）的字段引用是否正确区分
3. DataScopeAspect 的 `field()` 残留是否真的无法消除
4. V24 的 `ON CONFLICT (id) DO NOTHING` 是否足够幂等
