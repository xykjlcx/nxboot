# NxBoot — AI Agent 操作指南

> 本文件供 AI Agent（Claude Code、Codex、Cursor 等）快速理解项目并自主开发。
> 详细规范见 CLAUDE.md。

## 快速定位

| 要做什么 | 看哪里 |
|---------|--------|
| 新增 CRUD 模块 | CLAUDE.md → "新增 CRUD 模块的标准流程" |
| 理解项目架构 | CLAUDE.md → "架构概览" + "项目结构" |
| 修改表格/列表页 | `client/src/features/system/user/` 参考实现 |
| 修改布局/导航 | `client/src/app/layouts/BasicLayout.tsx` |
| 添加 API 接口 | 后端 Controller → Service → Repository，前端 `api.ts` |
| 修改权限 | 后端 `@PreAuthorize`，前端 `usePerm().has()` |

## 项目约定（Agent 必读）

### 后端
- **模块依赖**：admin → system → framework → common（单向，禁止反向）
- **数据模型**：JDK Record（VO 查询返回 + Command 嵌套写入），不用 Lombok/MapStruct
- **数据访问**：jOOQ DSL 字符串引用（`DSL.field()` / `DSL.table()`），不用 MyBatis
- **操作日志**：写操作 Controller 方法加 `@Log(module = "xxx", operation = "xxx")`
- **时间字段**：用 `r.get("create_time", LocalDateTime.class)` 做类型转换

### 前端
- **列定义**：`NxColumn<T>`（`field` 而非 `dataIndex`），操作列 `field: "_action"`
- **表格**：`NxTable`（不直接用 antd Table）
- **工具栏**：`NxBar`（`left`/`right` 插槽）
- **抽屉**：`NxDrawer`（Root/Trigger/Content/useContext 复合组件）
- **搜索**：`NxFilter`（延迟搜索 + badge）
- **类型安全**：不使用 `any`，render value 除外（antd 限制）

### 命名
- API 路径：`/api/v1/system/{领域复数}`
- 权限标识：`system:{领域}:{操作}`
- 前端文件：`types.ts` → `api.ts` → `columns.tsx` → `pages/XxxList.tsx` + `XxxForm.tsx`

## 运行项目

```bash
# 后端
cd server && mvn clean package -DskipTests
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=dev

# 前端
cd client && pnpm install && pnpm dev
```

默认账号 `admin` / `admin123`，数据库 `nxboot`（PostgreSQL localhost:5432）

## 验证命令

```bash
# 后端编译
cd server && mvn compile -q

# 前端类型检查 + 构建
cd client && npx tsc --noEmit && pnpm build
```
