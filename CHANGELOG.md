# Changelog

本项目遵循 [Semantic Versioning](https://semver.org/)。

## [0.9.0] - 2026-04-03

稳定化整改阶段，通过红蓝演练（Codex 审计）完成安全与权限模型修复。

### Security

- 去掉 `application.yml` 默认 dev profile，未指定 profile 时启动失败
- JWT secret 无兜底默认值，非 dev 环境必须通过环境变量配置
- CORS 默认禁止跨域，dev 环境白名单 localhost，`*` 打印安全告警

### Fixed

- RBAC 权限目录补齐：为 role/menu/dict/config/file/job/dept 7 个模块新增 18 个 F 类型按钮权限种子数据
- 修复前端 `system:file:create` 与后端 `system:file:upload` 权限命名不一致
- 角色菜单保存时自动补齐祖先目录节点，杜绝"配置成功但导航空白"
- 分级授权：角色编辑页菜单树只展示当前用户拥有的菜单，后端校验授权作用域
- assignable 作用域对齐真实权限语义（role.enabled/deleted + menu.enabled/deleted）
- V26 迁移回填历史环境中 role_menu 缺失的祖先节点
- 角色编辑页回显保真：checkStrictly 模式精确还原角色真实 menuIds，消除数据丢失风险
- 品牌化全局加载页 + 修复高频刷新被登出 + 统一认证错误处理
- Monitor 页面加载骨架屏 + 全局加载覆盖到布局就绪

### Changed

- `.gitignore` 添加 `*.tsbuildinfo`，移除已追踪的构建产物

## [0.8.0] - 2026-04-03

jOOQ Codegen 全量迁移 + 监控 Dashboard。

### Added

- 服务器监控 Dashboard 页面（CPU/内存/JVM/磁盘 4 卡片 + 10s 自动轮询）
- 监控菜单 V24 种子数据

### Changed

- jOOQ Codegen 全量迁移：11 个 Repository + JooqHelper + 5 个 Service/Aspect 全部使用类型安全引用
- JooqHelper 迁移到 `Table<?>` API，消除 7 个 `DSL.field()` 静态常量

## [0.7.0] - 2026-04-03

红蓝演练协议 + OAuth2 增强 + 性能优化。

### Added

- 红蓝演练协议（REVIEW_PROTOCOL.md + REVIEW_SUBMISSION_TEMPLATE.md）
- OAuth2 新用户自动分配默认角色
- V21-V23 补充缺失菜单记录 + 默认角色种子数据

### Changed

- MonitorController CPU 采样改为 `@Scheduled` 缓存，消除 `Thread.sleep(500)` 阻塞
- 前端图标按需导入优化（后因动态菜单需求回退为全量导入 + aliasMap）

## [0.6.0] - 2026-04-03

DevOps + 文档全面更新。

### Added

- Docker Compose（PostgreSQL + Redis）
- GitHub Actions CI（后端编译 + 前端类型检查 + 构建）

### Changed

- 项目文档全面更新（CLAUDE.md v8.0 / AGENTS.md / README.md）

## [0.5.0] - 2026-04-03

前端全模块重构。

### Added

- 新增页面：部门管理、登录日志、在线用户、任务日志
- 共享组件库：NxTable / NxBar / NxDrawer / NxForm / NxFilter / NxLoading / ApiSelect
- 前端基础设施：useAuth / usePerm / useDict / useTheme / Preferences / TokenBridge
- 三级导航布局（九宫格子系统 + Header Tab 模块 + Sider 菜单）
- Token 自动刷新 + 并发请求排队

### Changed

- 应用壳重构：动态菜单、ErrorBoundary、Suspense 懒加载

## [0.4.0] - 2026-04-03

后端系统模块全面增强。

### Added

- 操作日志系统（@Log 注解 + AOP 拦截器）
- 认证增强：Token 刷新、登录日志、在线用户管理、OAuth2 社会化登录框架
- 新增模块：部门管理、@DataScope 数据权限、服务器监控、定时任务执行日志
- jOOQ Codegen 初始配置 + ConfigRepository 类型安全迁移

### Changed

- JooqHelper 重构（通用分页/软删除/审计字段）
- 用户模块增强（Excel 导出、@DataScope）

## [0.3.0] - 2026-04-03

后端框架层核心基础设施。

### Added

- 核心基础设施：JooqHelper / i18n / 缓存抽象 / @Async 线程池 / XSS 过滤 / MDC 链路追踪
- 安全增强：Token 刷新机制 / JWT 黑名单 / 数据权限 / CORS / IP 限流
- 文件存储抽象（FileStorage 接口 + LocalFileStorage）
- Flyway V12-V20 迁移脚本

## [0.2.0] - 2026-04-03

依赖升级与构建配置优化。

### Changed

- 升级依赖与构建配置

### Fixed

- 修复 dev 环境数据库连接（macOS Homebrew PostgreSQL 用系统用户名）
- 修复 Timestamp → LocalDateTime 类型转换 + auth/info 接口路径

## [0.1.0] - 2026-04-03

NxBoot 全栈脚手架初始版本。

### Added

- 后端四模块架构：common → framework → system → admin
- 前端 Feature-based 目录结构
- 基础 CRUD：用户 / 角色 / 菜单 / 字典 / 配置 / 文件 / 定时任务
- JWT 认证 + RBAC 权限模型
- Flyway 数据库版本管理
- PostgreSQL + jOOQ 数据访问
