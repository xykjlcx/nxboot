# Contributing to NxBoot

感谢你对 NxBoot 的关注！欢迎提交 Issue、PR 或参与讨论。

## 开发环境

### 环境要求

- JDK 21+
- PostgreSQL 16+
- Node.js 20+
- pnpm 9+
- Maven 3.9+

### 快速启动

```bash
# 启动中间件（PostgreSQL + Redis）
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

访问 http://localhost:5173，默认账号 `admin` / `admin123`。

## 提交规范

### Commit Message

格式：`<type>：<description>`（注意是中文冒号）

| type | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| docs | 文档变更 |
| refactor | 重构（不改变功能） |
| perf | 性能优化 |
| chore | 构建/工具链变更 |
| build | 依赖/构建配置 |

示例：
- `feat(system)：新增通知公告模块`
- `fix(client)：修复角色编辑页回显丢失`
- `docs：更新 README 启动说明`

### 分支策略

- `main`：稳定分支，保持可发布状态
- 功能开发：从 `main` 创建 feature 分支，完成后 PR 合并

## Pull Request

1. Fork 仓库并创建分支
2. 确保通过 CI 检查：
   - `mvn -f server/pom.xml compile -q`
   - `cd client && npx tsc --noEmit`
   - `cd client && pnpm build`
3. 每个 PR 聚焦一个主题，避免混合不相关改动
4. 填写 PR 模板中的描述和测试说明

## 代码规范

### 后端

- 使用 JDK Record（不使用 Lombok/MapStruct）
- 使用 jOOQ Codegen 类型安全引用（不使用字符串 SQL）
- Controller 只做参数校验和转发，业务逻辑在 Service 层
- 新增模块参考 `system/user/` 的目录结构

### 前端

- TypeScript strict 模式，禁止使用 `any`
- 使用 `NxColumn` 而非 antd `ColumnsType`
- 使用 `request.ts` 封装（不直接使用 axios）
- 使用 CSS 变量（不硬编码颜色/间距）
- Biome 格式化：`pnpm lint`

## Issue

- Bug 报告请使用 Bug Report 模板
- 功能建议请使用 Feature Request 模板
- 提交前请搜索是否已有相同 Issue
