# NxBoot 脚手架终极演进规范

> 日期：2026-04-03
> 版本：Final Spec v1.0
> 基于：11 个调研 agent + 8 个实施 agent + 7 轮 code review + 老项目源码分析

---

## 一、项目定位

**AI-first 全栈后台管理脚手架**。所有未来 AI 项目的基础底座。

核心差异化：
- React + Spring Boot 全栈（中国开源生态无竞品，RuoYi/JeecgBoot/Pig 全绑 Vue）
- jOOQ 替代 MyBatis（类型安全 SQL，无 XML）
- JDK Record 替代 Lombok（不可变、零魔法）
- CLAUDE.md + AGENTS.md（AI agent 可自主开发）
- 垂直切片架构（AI 最友好的代码组织方式）

---

## 二、架构原则

### 2.1 分层架构（来自老项目实践改良）

```
Controller（接收 HTTP 请求，参数校验，返回 R<T>）
    ↓
Service（业务编排 + 领域规则 + 事务边界）
    ↓
Repository（数据访问抽象层 —— Service 不关心底层实现）
    ↓
jOOQ DSL / JooqHelper（Repository 内部实现细节）
```

**关键约束**：
- Service 只调用 Repository 的公开方法，**禁止**出现 jOOQ 的 DSLContext/field()/table()
- Repository 是真正的仓储抽象，底层可以是 jOOQ、MyBatis 或其他技术栈
- 所有 SQL 构建、类型转换、分页实现封闭在 Repository 内部
- 未来技术栈变更只改 Repository 实现，Service 零改动

### 2.2 数据模型分层

```
Command（写入参数，嵌套 Record：Create / Update）
    → Service 接收，传递给 Repository
VO（查询返回，JDK Record，不可变）
    ← Repository 构建，Service 返回给 Controller
```

- 不使用 MapStruct（JDK Record + 手动构造足够透明）
- 不使用 Lombok（JDK Record 自带 equals/hashCode/toString）
- Command 内置 JSR-303 校验注解

### 2.3 明确不做的

| 决策 | 理由 |
|------|------|
| 不引入六边形架构接口 | Service 直接注入 Repository 具体类，路径短、AI 认知负担低 |
| 不做完整 DDD 战术模式 | 系统管理模块是 CRUD，Aggregate/Domain Events 过度。未来业务模块可按需引入 |
| 不做 CQRS / Event Sourcing | 50 用户 admin 不需要读写分离 |
| 不拆微服务 | Monolith 是 10 人以下团队的最优解 |
| 不做代码生成器（当前阶段） | 等架构完全稳定后再做，避免模板频繁变更 |
| 不做多租户 | 等 SaaS 需求驱动 |
| 不做工作流 | 独立产品级复杂度，按需引入 |

### 2.4 AI-First 设计原则

- 每个文件 50-200 行，超过 250 行主动拆分
- 垂直切片组织：每个领域自包含 model/controller/service/repository
- user/ 模块始终是最完整的参考实现，新模式先在 user/ 落地
- 命名可预测：AI 看到 `features/system/xxx/api.ts` 就知道是 API hooks
- CLAUDE.md 是 AI 的操作手册，保持 200 行以内

---

## 三、已完成基础设施（V2-V7）

### 前端

| 类别 | 能力 |
|------|------|
| 组件 | NxTable（引擎抽象）、NxBar、NxDrawer（全屏/骨架屏）、NxFilter、NxForm（Schema-Driven, 10 组件类型）、ApiSelect（远程数据源+缓存）、NxLoading、ErrorBoundary |
| Hooks | useAuth（JWT+RefreshToken+Menus）、usePerm、useDict（5 分钟缓存）、useTheme（暗色模式）、useGuardedSubmit（防重复提交） |
| 系统 | Preferences（3 布局 mix/sidebar/top + 主题色 + 配置持久化）、TokenBridge（antd 令牌→CSS 变量自动同步）、Watermark（用户名水印）、动态菜单（后端按角色过滤） |

### 后端

| 类别 | 能力 |
|------|------|
| 安全 | JWT + Refresh Token（7 天）、登录限流（5/10s）、CORS 可配置、XSS 过滤器、MDC 请求追踪 |
| 数据 | JooqHelper（page/findById/softDelete/keywordCondition）、7 个 Repository 重构、Snowflake ID |
| 存储 | FileStorage 接口 + LocalFileStorage（可扩展 OSS） |
| 日志 | @Log 操作日志（26 个写操作标注）+ 登录日志（独立表） |
| 导出 | EasyExcel + ExcelHelper（用户导出示例端点） |
| 异步 | @Async 线程池配置（4 核心/8 最大/200 队列） |
| 迁移 | Flyway V1-V13 |

### DevOps

- Docker Compose（PostgreSQL 16）
- CLAUDE.md + AGENTS.md
- 演进日志

---

## 四、功能清单（按 Tier 排列）

### Tier 1 — 脚手架核心 ✅ 已完成

#### T1.1 部门管理（组织架构树）✅

| 项 | 说明 |
|---|------|
| 后端 | sys_dept 表（id/parent_id/dept_name/sort_order/leader/phone/email/enabled）+ DDD 垂直切片（model/controller/service/repository） |
| 前端 | Tree 页面，复用 NxTable expandable |
| 迁移 | V14__create_sys_dept.sql + V15__init_dept_data.sql |
| 依赖 | 无 |
| 工作量 | 中 |

#### T1.2 数据权限（行级数据隔离）✅

| 项 | 说明 |
|---|------|
| 注解 | `@DataScope(deptAlias = "d", userAlias = "u")` 标注在 Service 方法上 |
| 实现 | AOP 切面 + JooqHelper 层 Condition 拼接 |
| 粒度 | 5 种：全部 / 自定义部门 / 本部门 / 本部门及下级 / 仅本人 |
| 角色配置 | sys_role 表增加 `data_scope` 字段 + 角色-部门关联表 `sys_role_dept` |
| 依赖 | 部门管理 |
| 工作量 | 中 |

### Tier 2 — 架构质量

#### T2.1 审计字段自动填充

| 项 | 说明 |
|---|------|
| 实现 | JooqHelper 增加 `auditInsert(idGenerator, operator)` 和 `auditUpdate(operator)` 方法，返回 Map<Field, Object> |
| 效果 | Repository insert/update 从 6 行审计字段 → 1 行调用 |
| 工作量 | 小 |

#### T2.2 数据库索引补全

| 项 | 说明 |
|---|------|
| 迁移 | V16__add_indexes.sql：username 唯一、role_key 唯一、config_key 唯一、dict_data.dict_type 索引 |
| 工作量 | 小 |

#### T2.3 错误处理契约统一

| 项 | 说明 |
|---|------|
| 问题 | R.code 与 HTTP status 双重编码，前端处理两次 |
| 方案 | 统一：业务异常 HTTP 200 + R(错误码, 消息)，系统异常 HTTP 5xx + R(500, 消息) |
| 前端 | request.ts 对齐：只检查 R.code，不再在 interceptor 中重复处理 |
| 工作量 | 小 |

#### T2.4 路由级 Error Boundary

| 项 | 说明 |
|---|------|
| 实现 | routes.tsx 中每个路由包裹 ErrorBoundary，单页面崩不影响其他页面 |
| Fallback | "页面加载失败" + 重试按钮 |
| 工作量 | 小 |

#### T2.5 乐观锁

| 项 | 说明 |
|---|------|
| 实现 | 核心表加 `version` 字段，update 时 `WHERE version = ?`，版本冲突抛异常 |
| 迁移 | V17__add_version_field.sql |
| JooqHelper | 增加 `optimisticUpdate()` 方法 |
| 工作量 | 中 |

#### T2.6 HikariCP 配置调优

| 项 | 说明 |
|---|------|
| 实现 | application.yml 显式配置连接池参数（max-pool-size/min-idle/timeout） |
| 工作量 | 小（纯配置） |

#### T2.7 Flyway 最佳实践补全

| 项 | 说明 |
|---|------|
| Baseline | 配置 `flyway.baseline-on-migrate=true`，新环境可从指定版本开始 |
| 回退约定 | 每个 V{N} 附带 `U{N}__rollback.sql` 作为手动回退参考（放 `db/rollback/` 目录，不被 Flyway 自动执行） |
| 可重复迁移 | `R__` 前缀脚本用于视图、函数、存储过程，修改后自动重新执行 |
| 工作量 | 小 |

#### T2.8 jOOQ Codegen（延后执行）

| 项 | 说明 |
|---|------|
| 时机 | 等 Tier 1 + Tier 2 其他项完成后 |
| 方式 | Maven 插件 + 开发时生成 + 提交到 Git |
| 影响 | 全部 Repository 从 `field("xxx")` 改为 `SYS_USER.XXX` |
| 工作量 | 大 |

### Tier 3 — 企业级高价值功能

| # | 功能 | 说明 | 工作量 |
|---|------|------|--------|
| T3.1 | 在线用户 + 强制下线 | Token 黑名单（先 ConcurrentHashMap） | 中 |
| T3.2 | 通知公告 | sys_notice + 已读追踪 + Tiptap 富文本编辑 | 中 |
| T3.3 | 服务器监控 | OSHI/Actuator → CPU/内存/磁盘/JVM Dashboard | 小 |
| T3.4 | 定时任务执行历史 | sys_job_log + Cron 可视化编辑器（react-js-cron） | 中 |
| T3.5 | 批量操作封装 | JooqHelper.batchInsert() | 小 |
| T3.6 | 慢查询日志 | jOOQ ExecuteListener 记录 >500ms SQL | 小 |
| T3.7 | CI/CD 模板 | GitHub Actions 构建部署 YAML | 小 |

### Tier 4 — 按需扩展（不在核心中）

| 功能 | 触发条件 |
|------|---------|
| i18n 国际化 | 有海外客户 |
| Redis 缓存层 | 引入 Redis 后 |
| 多租户 SaaS | 做 SaaS 产品 |
| 三方登录 OAuth2 | 具体项目需要 |
| 工作流 Flowable | 有审批流需求 |
| WebSocket | 站内信 / 在线用户 |
| 代码生成器 | 架构完全稳定后 |

---

## 五、执行路径

```
Phase 1: T1.1 部门管理 → T1.2 数据权限
Phase 2: T2.1 审计自动填充 → T2.2 索引补全 → T2.3 错误契约 → T2.4 路由 ErrorBoundary → T2.5 乐观锁 → T2.6 HikariCP → T2.7 Flyway 最佳实践
Phase 3: T3.x 按需挑选
Phase 4: T2.8 jOOQ Codegen（大改动，单独一个阶段）
Phase 5: T4.x 项目驱动
```

每个 Phase 完成后触发 code review，确保质量。

---

## 六、从老项目（nanxun_server）吸收的模式

| 模式 | 老项目实现 | NxBoot 采纳策略 |
|------|-----------|----------------|
| Repository 接口抽象 | BaseRepository extends JpaRepository + QuerydslPredicateExecutor | 认同原则：Repository 是仓储抽象层，Service 不关心底层实现。当前 NxBoot Repository 是具体类但公开方法签名稳定，效果等同 |
| Biz 层（业务逻辑层） | Controller → Biz → Service，Biz 处理领域规则 | 暂不引入独立 Biz 层（当前系统管理模块规则简单）。未来新增复杂业务模块时，可在该切片内引入 |
| Command 自校验 | `CompanyCreateCmd.checkParams()` | NxBoot 用 JSR-303 注解校验，等效且更标准 |
| Domain Events | Spring ApplicationEvent（UserRoleRelationChangeEvent） | 暂不引入（无跨领域事件需求）。引入时用 Spring Events，不造轮子 |
| DTO 分离 | BasicDTO（读）vs EditDTO（写） | NxBoot 的 VO（读）+ Command（写）是同样模式，用 JDK Record 更简洁 |
| MapStruct 映射 | 编译时生成双向转换 | 不引入（手动构造在 toVO 方法中，更透明，AI 更容易理解） |
| QueryDSL PredicateWrapper | 自动从 URL 参数构建查询条件 | NxBoot 用 JooqHelper.keywordCondition()。未来可扩展 filterCondition() 支持更丰富筛选 |
| 批量操作 | jdbcSaveAllEntity() + JdbcTemplate 批量写入 | 待实施（T3.5 JooqHelper.batchInsert()） |
| Excel 导出 | EasyExcel @ExcelProperty 注解 | 已实施（ExcelHelper + UserExcelVO） |

---

## 七、质量保证

- 每个 Phase 完成后触发 superpowers:code-reviewer review
- 后端 `mvn compile` + 前端 `tsc --noEmit` + `pnpm build` 零错误才算完成
- CLAUDE.md 每次迭代同步更新
- 项目日志记录每次变更

---

## 八、数据库 / ORM / 持久化层规范

### Flyway 迁移约定
- 正向迁移：`V{N}__description.sql`，当前到 V13
- 可重复迁移：`R__views_and_functions.sql`（视图、函数）
- 每个迁移脚本有 COMMENT ON 注释

### jOOQ 使用约定（Codegen 启用前）
- 字符串引用：`field("xxx")` / `table("xxx")`
- 类型转换：`r.get("field_name", Type.class)`（不用 `r.get(field("name", Type.class))`）
- 所有 jOOQ 操作限制在 Repository 内部
- Service 层禁止出现 jOOQ API

### 审计字段
- 所有业务表包含：create_by / create_time / update_by / update_time / deleted
- 当前手动填充，待 T2.1 封装后一行调用

### 索引策略
- 唯一业务标识：唯一索引（username、role_key、config_key）
- 高频查询字段：普通索引（dict_type、login_time）
- 软删除字段：不单独索引（查询时已与其他条件组合）
