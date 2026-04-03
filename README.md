<div align="center">

# NxBoot

**AI-Friendly Full-Stack Admin Scaffold**

A production-ready admin system scaffold built with modern tech stack. Designed for AI-assisted development — ships with comprehensive `CLAUDE.md` so AI agents can contribute code that follows your architecture from day one.

基于 JDK 21 + Spring Boot 3 + jOOQ + PostgreSQL + React 18 + Ant Design 6 的全栈管理系统脚手架。
AI 友好设计——自带完整的 `CLAUDE.md`，让 AI 从第一行代码就遵循你的架构规范。

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61dafb.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-strict-3178c6.svg)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg)](https://www.postgresql.org/)

[English](#english) · [中文](#中文)

</div>

---

<a id="english"></a>

## Why NxBoot?

| Problem | NxBoot's Answer |
|---------|-----------------|
| AI generates code that doesn't match your style | `CLAUDE.md` defines architecture, patterns, naming — AI reads it first |
| Boilerplate for every new CRUD module | 5 backend files + 5 frontend files, symmetric structure, copy and adapt |
| Permission system that "works for admin only" | Full RBAC with button-level permissions, scoped delegation, ancestor auto-fill |
| Unsafe defaults in production | No profile = fail to start. No JWT secret = fail to start. CORS locked down |
| ORM that fights you on complex queries | jOOQ with Codegen — type-safe SQL, no magic, no annotations |

## Features

### System Modules

| Module | Capabilities |
|--------|-------------|
| User Management | CRUD, enable/disable, password reset, department assignment |
| Role Management | RBAC, menu permission tree, scoped delegation (cascading authority) |
| Menu Management | Tree structure, 3 types: Directory (M) / Page (C) / Button (F) |
| Department Management | Tree structure, hierarchical organization |
| Dictionary Management | Type + Data, cached lookup |
| System Config | Key-value configuration |
| File Management | Upload, list, delete (pluggable storage backend) |
| Scheduled Jobs | Cron management, manual trigger, execution history |
| Operation Log | Auto-captured via `@Log` annotation |
| Login Log | Authentication audit trail |
| Online Users | Active session tracking, force logout |
| Server Monitor | CPU / Memory / JVM / Disk dashboard with auto-refresh |

### Architecture Highlights

- **Three-level navigation**: Subsystem (grid switcher) → Module (header tabs) → Page (sidebar menu)
- **Dynamic menus**: Menu tree from database, filtered by user's role permissions
- **Token refresh**: Access token (24h) + refresh token (7d), concurrent request queuing on 401
- **Data scope**: `@DataScope` annotation with 5 granularity levels (all / custom dept / own dept / dept & children / self only)
- **Dark mode**: Ant Design token bridge — theme tokens auto-sync to CSS variables
- **Branded loading**: CSS-only loading screen persists through React boot → auth → Suspense → layout render

### AI-Friendly Design

- **`CLAUDE.md`**: 18KB operation manual — architecture, patterns, naming conventions, do's and don'ts
- **`AGENTS.md`**: Multi-agent collaboration guide
- **Symmetric structure**: Backend domain package ↔ Frontend feature directory, 1:1 mapping
- **Predictable patterns**: Every module follows the same structure, AI can extrapolate from one example

## Quick Start

### Prerequisites

- JDK 21+, PostgreSQL 16+, Node.js 20+, pnpm 9+, Maven 3.9+

### Option 1: Docker Compose (recommended)

```bash
# Start PostgreSQL + Redis
docker compose up -d

# Backend
cd server
mvn clean package -DskipTests
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=dev

# Frontend
cd client
pnpm install
pnpm dev
```

### Option 2: Local PostgreSQL

```bash
# Backend (configure your DB connection in application-dev.yml)
cd server
mvn clean package -DskipTests
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=dev

# Frontend
cd client
pnpm install
pnpm dev
```

Visit http://localhost:5173 — default account: `admin` / `admin123`

> **Security by design**: Running without `--spring.profiles.active` will fail — JWT secret and database are not configured. This is intentional.

### Production Deployment

```bash
export NXBOOT_JWT_SECRET=your-secret-at-least-256-bits
export DB_URL=jdbc:postgresql://host:5432/nxboot
export DB_USERNAME=postgres
export DB_PASSWORD=your-password
export NXBOOT_CORS_ORIGINS=https://your-domain.com

java -jar nxboot-admin.jar --spring.profiles.active=prod
```

## Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Backend | Spring Boot 3.4 | Mature ecosystem, battle-tested |
| Data Access | jOOQ + Codegen | Type-safe SQL, no magic strings |
| Database | PostgreSQL 16 | Powerful, open source |
| Migration | Flyway | Declarative, team-friendly |
| Auth | JWT (JJWT) | Stateless, fits SPA architecture |
| Frontend | React 18 + TypeScript strict | Largest ecosystem, type safety |
| Build | Vite | Fast HMR, native ESM |
| UI | Ant Design 6 | Enterprise-grade components |
| Data Fetching | TanStack Query | Cache, auto-refresh, optimistic updates |
| State | Zustand | Lightweight, zero boilerplate |
| Lint/Format | Biome | Fast, all-in-one |

## Project Structure

```
nxboot/
├── server/                          # Backend (Maven multi-module)
│   ├── nxboot-common/               # Pure utilities (zero Spring dependency)
│   ├── nxboot-framework/            # Framework config (Security/jOOQ/Web)
│   ├── nxboot-system/               # System domain (DDD vertical slices)
│   └── nxboot-admin/                # Boot module (Application + Flyway)
└── client/                          # Frontend (Vite + React)
    └── src/
        ├── app/                     # App shell (layout/routes/request/auth guard)
        ├── features/system/         # Business modules (1:1 with backend domains)
        ├── shared/                  # Shared components & hooks
        └── types/                   # Global types (R<T>/PageResult/PageQuery)
```

## Adding a New CRUD Module

Backend: 5 files in `server/nxboot-system/src/main/java/com/nxboot/system/<domain>/`

```
notice/
├── model/NoticeVO.java              # JDK Record, query response
├── model/NoticeCommand.java         # Nested Record (Create/Update), JSR-303
├── controller/NoticeController.java # REST + @PreAuthorize
├── service/NoticeService.java       # Business logic + @Transactional
└── repository/NoticeRepository.java # jOOQ data access
```

Frontend: 5 files in `client/src/features/system/<domain>/`

```
notice/
├── types.ts                         # NoticeVO, NoticeCommand
├── api.ts                           # TanStack Query hooks
├── columns.tsx                      # NxColumn<NoticeVO>[]
└── pages/
    ├── NoticeList.tsx               # List page
    └── NoticeForm.tsx               # Drawer form
```

Plus: one Flyway migration + one route entry. See `system/user/` as the reference implementation.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, commit conventions, and PR guidelines.

## Security

See [SECURITY.md](SECURITY.md) for supported versions and vulnerability reporting.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history.

## License

[MIT](LICENSE)

---

<a id="中文"></a>

## 为什么选 NxBoot？

| 痛点 | NxBoot 的解法 |
|------|--------------|
| AI 生成的代码不符合项目风格 | `CLAUDE.md` 定义架构、模式、命名规范——AI 先读手册再写代码 |
| 每个 CRUD 模块都要写一堆样板 | 后端 5 个文件 + 前端 5 个文件，对称结构，复制改改就能用 |
| 权限系统"只有 admin 能用" | 完整 RBAC + 按钮级权限 + 分级授权 + 祖先节点自动补齐 |
| 生产环境默认配置不安全 | 不指定 profile = 启动失败，不配 JWT secret = 启动失败，CORS 默认锁定 |
| ORM 遇到复杂查询就跪了 | jOOQ + Codegen——类型安全 SQL，没有黑魔法，没有注解地狱 |

## 功能模块

| 模块 | 能力 |
|------|------|
| 用户管理 | 增删改查、启用/禁用、重置密码、部门分配 |
| 角色管理 | RBAC、菜单权限树、分级授权（级联作用域） |
| 菜单管理 | 树形结构，3 种类型：目录(M) / 页面(C) / 按钮(F) |
| 部门管理 | 树形组织架构 |
| 字典管理 | 类型 + 数据，带缓存查询 |
| 系统配置 | 键值对配置 |
| 文件管理 | 上传、列表、删除（可插拔存储后端） |
| 定时任务 | Cron 管理、手动触发、执行历史 |
| 操作日志 | `@Log` 注解自动采集 |
| 登录日志 | 认证审计追踪 |
| 在线用户 | 活跃会话追踪、强制下线 |
| 服务器监控 | CPU / 内存 / JVM / 磁盘仪表盘，自动刷新 |

### 架构亮点

- **三级导航**：子系统（九宫格切换）→ 模块（顶部 Tab）→ 页面（左侧菜单）
- **动态菜单**：菜单树来自数据库，按用户角色权限过滤
- **Token 自动刷新**：access token (24h) + refresh token (7d)，401 时并发请求排队
- **数据权限**：`@DataScope` 注解，5 种粒度（全部 / 自定义部门 / 本部门 / 本部门及下级 / 仅本人）
- **暗色模式**：Ant Design 主题令牌自动同步到 CSS 变量
- **品牌加载页**：纯 CSS 加载动画，贯穿 React 启动 → 认证 → 懒加载 → 布局渲染全过程

### AI 友好设计

- **`CLAUDE.md`**：18KB 操作手册——架构、模式、命名规范、禁止事项
- **`AGENTS.md`**：多 Agent 协作指南
- **对称结构**：后端领域包 ↔ 前端 feature 目录，1:1 映射
- **可预测模式**：每个模块遵循相同结构，AI 看一个例子就能推导全部

## 快速开始

### 环境要求

- JDK 21+、PostgreSQL 16+、Node.js 20+、pnpm 9+、Maven 3.9+

### 方式一：Docker Compose（推荐）

```bash
# 启动 PostgreSQL + Redis
docker compose up -d

# 后端
cd server
mvn clean package -DskipTests
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=dev

# 前端
cd client
pnpm install
pnpm dev
```

### 方式二：本地 PostgreSQL

```bash
# 后端（在 application-dev.yml 中配置你的数据库连接）
cd server
mvn clean package -DskipTests
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=dev

# 前端
cd client
pnpm install
pnpm dev
```

访问 http://localhost:5173，默认账号 `admin` / `admin123`

> **安全设计**：不指定 `--spring.profiles.active` 启动会失败——JWT secret 和数据库连接未配置。这是有意为之。

### 生产部署

```bash
export NXBOOT_JWT_SECRET=至少256位的密钥
export DB_URL=jdbc:postgresql://host:5432/nxboot
export DB_USERNAME=postgres
export DB_PASSWORD=your-password
export NXBOOT_CORS_ORIGINS=https://your-domain.com

java -jar nxboot-admin.jar --spring.profiles.active=prod
```

## 技术栈

| 层 | 技术 | 选型理由 |
|---|------|---------|
| 后端框架 | Spring Boot 3.4 | 生态成熟，久经考验 |
| 数据访问 | jOOQ + Codegen | 类型安全 SQL，告别魔法字符串 |
| 数据库 | PostgreSQL 16 | 功能强大，开源 |
| 数据库迁移 | Flyway | 声明式迁移，团队协作友好 |
| 认证 | JWT (JJWT) | 无状态，适合前后端分离 |
| 前端框架 | React 18 + TypeScript strict | 最大生态，类型安全 |
| 构建工具 | Vite | 快速 HMR，原生 ESM |
| UI 组件库 | Ant Design 6 | 企业级组件，开箱即用 |
| 数据请求 | TanStack Query | 缓存 + 自动刷新 + 乐观更新 |
| 状态管理 | Zustand | 轻量，零 boilerplate |
| 代码规范 | Biome | 快速，all-in-one |

## 项目结构

```
nxboot/
├── server/                          # 后端（Maven 多模块）
│   ├── nxboot-common/               # 纯工具层（零 Spring 依赖）
│   ├── nxboot-framework/            # 框架配置（Security/jOOQ/Web）
│   ├── nxboot-system/               # 系统领域（DDD 垂直切片）
│   └── nxboot-admin/                # 启动模块（Application + Flyway）
└── client/                          # 前端（Vite + React）
    └── src/
        ├── app/                     # 应用壳（布局/路由/请求/权限守卫）
        ├── features/system/         # 业务模块（与后端领域 1:1 对应）
        ├── shared/                  # 共享组件和 hooks
        └── types/                   # 全局类型（R<T>/PageResult/PageQuery）
```

## 新增 CRUD 模块

后端：在 `server/nxboot-system/src/main/java/com/nxboot/system/<领域>/` 下新建 5 个文件

```
notice/
├── model/NoticeVO.java              # JDK Record，查询返回
├── model/NoticeCommand.java         # 嵌套 Record（Create/Update），JSR-303 校验
├── controller/NoticeController.java # REST 接口 + @PreAuthorize
├── service/NoticeService.java       # 业务逻辑 + @Transactional
└── repository/NoticeRepository.java # jOOQ 数据访问
```

前端：在 `client/src/features/system/<领域>/` 下新建 5 个文件

```
notice/
├── types.ts                         # NoticeVO、NoticeCommand
├── api.ts                           # TanStack Query hooks
├── columns.tsx                      # NxColumn<NoticeVO>[]
└── pages/
    ├── NoticeList.tsx               # 列表页
    └── NoticeForm.tsx               # 抽屉表单
```

再加一个 Flyway 迁移 + 一条路由配置。参考 `system/user/` 作为标准实现。

## 参与贡献

查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解开发环境搭建、提交规范和 PR 指南。

## 安全策略

查看 [SECURITY.md](SECURITY.md) 了解支持版本和漏洞报告渠道。

## 更新日志

查看 [CHANGELOG.md](CHANGELOG.md) 了解版本历史。

## 开源协议

[MIT](LICENSE)
