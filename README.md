# NxBoot

AI 友好型全栈管理系统脚手架。基于 JDK 21 + Spring Boot 3 + jOOQ + PostgreSQL + Vite + React 18 + Ant Design 6。

## 快速开始

### 环境要求

- JDK 21+
- PostgreSQL 16+
- Node.js 20+
- pnpm 9+
- Maven 3.9+

### 后端

```bash
cd server
mvn clean package -DskipTests
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=dev
```

### 前端

```bash
cd client
pnpm install
pnpm dev
```

访问 http://localhost:5173，默认账号 `admin` / `admin123`。

## 项目结构

```
nxboot/
├── server/                  ← 后端（Maven 多模块）
│   ├── nxboot-common/       ← 纯工具层（零框架依赖）
│   ├── nxboot-framework/    ← 框架配置层（Security/jOOQ/Web）
│   ├── nxboot-system/       ← 系统领域层（DDD 领域分包）
│   └── nxboot-admin/        ← 启动模块（Application + Flyway）
└── client/                  ← 前端（Vite + React）
    └── src/
        ├── app/             ← 应用壳（三级导航布局/路由/请求）
        ├── features/        ← 业务模块（与后端领域对称）
        ├── shared/          ← 共享组件和 hooks
        └── types/           ← 全局类型
```

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户管理 | 增删改查 + 状态切换 |
| 角色管理 | 角色分配 + 权限关联 |
| 菜单管理 | 树形菜单 + 按钮权限 |
| 字典管理 | 字典类型 + 字典数据 |
| 系统配置 | 键值对配置 |
| 操作日志 | 只读查看 |
| 文件管理 | 上传 + 列表 |
| 定时任务 | 任务管理 + 手动执行 |

## 技术选型

| 层 | 技术 | 选型理由 |
|---|---|---|
| 后端框架 | Spring Boot 3.4 | 生态成熟，社区活跃 |
| ORM | jOOQ | 类型安全 SQL，灵活度高 |
| 数据库 | PostgreSQL | 功能强大，开源 |
| 数据库版本控制 | Flyway | 声明式迁移，团队协作友好 |
| 认证 | JWT (JJWT) | 无状态，适合前后端分离 |
| 前端框架 | React 18 + TypeScript | 生态最大，类型安全 |
| 构建工具 | Vite | 快速 HMR，原生 ESM |
| UI 组件库 | Ant Design 6 | 企业级组件，开箱即用 |
| 数据请求 | TanStack Query | 缓存 + 自动刷新 + 乐观更新 |
| 状态管理 | Zustand | 轻量，无 boilerplate |

## 前端布局

三级导航架构，参考企业级 ERP 系统：

```
子系统（九宫格切换）→ 模块（Header Tab）→ 页面（左侧菜单）
```

- 顶部：Logo + 模块 Tab（圆角矩形 pill）+ 用户下拉
- 左侧：白底菜单，选中项蓝色指示条
- 内容区：面包屑 + 页面内容

## License

MIT
