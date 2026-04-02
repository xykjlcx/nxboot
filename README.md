# NxBoot

AI 友好型全栈脚手架。基于 JDK 21 + Spring Boot 3 + jOOQ + PostgreSQL + Vite + React 18 + Ant Design 5。

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

## 项目结构

```
nxboot/
├── server/                  ← 后端（Maven 多模块）
│   ├── nxboot-common/       ← 纯工具层（零框架依赖）
│   ├── nxboot-framework/    ← 框架配置层（Security/jOOQ/Web）
│   ├── nxboot-system/       ← 系统领域层（DDD 领域分包）
│   └── nxboot-admin/        ← 启动模块（Application + Flyway）
└── client/                  ← 前端（Vite + React）
```

## 默认账号

- 用户名：`admin`
- 密码：`admin123`
