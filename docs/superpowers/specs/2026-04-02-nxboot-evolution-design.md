# NxBoot 脚手架演进设计文档

> 日期：2026-04-02
> 定位：AI-first 全栈后台管理脚手架，所有未来 AI 项目的基础底座

---

## 一、调研结论

### 竞品格局

- **React + Spring Boot 全栈脚手架在中国开源生态中几乎没有直接竞品**（RuoYi/JeecgBoot/Pig 全部绑 Vue）
- NxBoot 的 jOOQ + JDK Record + DDD 垂直切片 + CLAUDE.md 是独特优势
- 所有主流脚手架的最小功能集：认证/RBAC/用户角色菜单/操作日志/数据字典/代码生成

### NxBoot 现状评估

**优势**：架构清晰、AI 可读性高、模块对称、技术栈现代
**缺口**：无 ErrorBoundary、无空状态、TS any 泄漏、后端安全缺口、无测试、无代码生成、无 i18n/暗色模式

### 老管理端可移植资产

- 字典 hook（useDictData）、文件下载工具（downloadFileByBlob）、动态表格高度（useTableHeight）
- 权限中间件模式、通知轮询架构、OSS 文件管理
- 系统切换 + 动态菜单 + 列配置持久化

---

## 二、版本规划

### V2.0 — 基础加固（安全 + 体验 + 一致性）

| # | 任务 | 类型 | 优先级 |
|---|------|------|--------|
| 1 | 全局 ErrorBoundary | 前端 | P0 |
| 2 | NxTable 空状态 + 统一 loading | 前端 | P0 |
| 3 | 侧边栏折叠/展开 | 前端 | P0 |
| 4 | 修复 NxTable TS any 泄漏 | 前端 | P1 |
| 5 | 后端 AuthenticationException 处理 | 后端 | P0 |
| 6 | 登录接口限流 | 后端 | P0 |
| 7 | CORS 生产环境可配置 | 后端 | P1 |
| 8 | 表单校验规范化（邮箱/手机号） | 前端 | P1 |
| 9 | 更新 CLAUDE.md 新组件文档 | 文档 | P1 |

### V3.0 — 功能完善（核心能力补齐）

| # | 任务 | 类型 |
|---|------|------|
| 1 | 操作日志写入（AccessLogInterceptor） | 后端 |
| 2 | useDictData hook + 字典缓存 | 前端 |
| 3 | 文件下载工具 + Blob 处理 | 前端 |
| 4 | NxDrawer 骨架屏 loading | 前端 |
| 5 | 响应式布局基础（移动端适配） | 前端 |
| 6 | @Transactional(readOnly) 优化 | 后端 |
| 7 | SpringDoc API 注解 | 后端 |

### V4.0 — AI 原生增强

| # | 任务 | 类型 |
|---|------|------|
| 1 | AGENTS.md 多 agent 支持文件 | 文档 |
| 2 | 代码生成器（模板化新增 CRUD 模块） | 工具 |
| 3 | i18n 基础设施 | 前后端 |
| 4 | 暗色模式 | 前端 |
| 5 | 单元测试基础框架 | 后端 |

---

## 三、设计原则

1. **AI 可读性优先**：每个文件职责单一、命名自解释、目录结构可预测
2. **约定大于配置**：新增模块只需"照猫画虎"，不需要理解框架内部
3. **渐进式复杂度**：基础场景极简（Ant Design Table），复杂场景可切换（AG Grid）
4. **零魔法**：不用 Lombok/MapStruct/ProComponents，所有逻辑透明可追踪
5. **持续迭代**：每个版本聚焦一个主题，做扎实再进入下一版本
