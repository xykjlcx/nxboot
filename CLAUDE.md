# NxBoot 全栈脚手架

> AI 友好型后台管理系统脚手架。本文件是 AI 的操作手册——读完即可按规范写代码。

---

## 架构概览

单仓库，后端四模块（common → framework → system → admin），前端 Feature-based 目录。系统领域按 DDD 风格垂直切片，每个领域自包含 model/controller/service/repository。前后端领域一一对称。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | JDK 21 + Spring Boot 3.4 + jOOQ + PostgreSQL + Flyway + JWT (JJWT) + SpringDoc |
| 前端 | Vite + React 18 + TypeScript strict + Ant Design + AG Grid + TanStack Query + Zustand |
| 样式 | Ant Design Token + CSS Modules + CSS 变量 |
| 规范 | Biome (lint + format)，pnpm |

## 项目结构

```
nxboot/
├── server/                          ← 后端
│   ├── nxboot-common/               ← 纯工具层（零 Spring 依赖）
│   ├── nxboot-framework/            ← 框架配置（Security/jOOQ/Web）
│   ├── nxboot-system/               ← 系统领域（DDD 垂直切片）
│   └── nxboot-admin/                ← 启动模块（Application + Flyway + 配置）
└── client/                          ← 前端
    └── src/
        ├── app/                     ← 应用壳（路由/布局/请求/权限守卫）
        ├── features/system/         ← 业务模块（与后端领域 1:1 对应）
        ├── shared/                  ← 共享组件和 hooks
        └── types/                   ← 全局类型
```

## 模块职责

| 模块 | 职责 | 依赖 |
|------|------|------|
| nxboot-common | 工具类、常量、异常、R\<T\>、PageQuery/PageResult、SnowflakeId | 无（零 Spring 依赖） |
| nxboot-framework | Spring Security + JWT、jOOQ 配置、全局异常处理、CORS、限流、Jackson | common |
| nxboot-system | 9 个领域包：auth/user/role/menu/dict/config/log/file/job | framework |
| nxboot-admin | NxBootApplication 启动类 + Flyway 迁移脚本 + 多环境配置 | system |

依赖方向：**admin → system → framework → common**（单向，禁止反向依赖）

---

## 新增 CRUD 模块的标准流程

> 这是最重要的部分。新增一个业务模块（如"公告管理"）需要以下步骤：

### 后端（5 个文件 + 1 个 SQL）

在 `server/nxboot-system/src/main/java/com/nxboot/system/` 下新建领域包：

```
notice/
├── model/
│   ├── NoticeVO.java            ← JDK Record，查询返回
│   └── NoticeCommand.java       ← 嵌套 Record（Create / Update），含 JSR-303 校验
├── controller/
│   └── NoticeController.java    ← REST 接口 + @PreAuthorize 权限控制
├── service/
│   └── NoticeService.java       ← 业务逻辑 + @Transactional
└── repository/
    └── NoticeRepository.java    ← jOOQ 数据访问
```

在 `server/nxboot-admin/src/main/resources/db/migration/` 下新增 Flyway 脚本：
```
V12__create_notice.sql
```

在 `V11__init_data.sql` 或新的迁移脚本中插入菜单和权限数据。

### 前端（5 个文件）

在 `client/src/features/system/` 下新建：

```
notice/
├── types.ts              ← NoticeVO、NoticeCommand（与后端对称）
├── api.ts                ← useNotices、useCreateNotice、useUpdateNotice、useDeleteNotice
├── columns.tsx           ← AG Grid 列定义
└── pages/
    ├── NoticeList.tsx     ← 列表页（搜索 + AG Grid + 权限按钮）
    └── NoticeForm.tsx     ← Drawer 表单（新增/编辑切换）
```

在 `client/src/app/routes.tsx` 中添加路由。

### 核心模式参考

**以 `system/user/` 为模板**——这是最完整的参考实现，其他模块照着抄。

---

## 命名规范

| 类别 | 模式 | 示例 |
|------|------|------|
| VO（查询返回） | `XxxVO` | `UserVO`, `RoleVO` |
| Command（写入参数） | `XxxCommand.Create` / `XxxCommand.Update` | `UserCommand.Create` |
| Controller | `XxxController` | `UserController` |
| Service | `XxxService` | `UserService` |
| Repository | `XxxRepository` | `UserRepository` |
| API 路径 | `/api/v1/system/{领域复数}` | `/api/v1/system/users` |
| 权限标识 | `system:{领域}:{操作}` | `system:user:create` |
| 前端类型 | 与后端对称 | `UserVO`, `UserCommand` |
| 前端 API hook | `useXxxs` / `useCreateXxx` | `useUsers`, `useCreateUser` |

**注意：手写类不加 `Sys` 前缀**（包路径已提供命名空间）。

## 数据模型规范

```java
// VO — JDK Record，不可变，用于查询返回
public record UserVO(
    Long id,
    String username,
    String nickname,
    Boolean enabled,
    LocalDateTime createTime
) {}

// Command — 嵌套 Record，用于写入参数
public final class UserCommand {
    public record Create(
        @NotBlank String username,
        @NotBlank @Size(min = 6) String password
    ) {}
    public record Update(
        @NotNull Long id,
        String nickname,
        Boolean enabled
    ) {}
    private UserCommand() {}
}
```

- **不手写数据库实体类**——jOOQ 通过 DSL 直接操作表字段
- **不使用 MapStruct/Lombok**——JDK Record 自带 equals/hashCode/toString
- 每个领域只需 **2 个模型文件**（VO + Command）

## jOOQ 使用规范

当前版本使用 `DSL.field()` / `DSL.table()` 字符串引用。后续启用 codegen 后将切换到类型安全的生成类。

### 查询（投影到 VO）

```java
public PageResult<UserVO> page(PageQuery pageQuery, String keyword) {
    var table = DSL.table("sys_user");
    var where = DSL.field("deleted", Boolean.class).eq(false);
    if (keyword != null && !keyword.isBlank()) {
        where = where.and(DSL.field("username").likeIgnoreCase("%" + keyword + "%"));
    }
    long total = dsl.selectCount().from(table).where(where).fetchOne(0, long.class);
    List<UserVO> list = dsl.select(...)
        .from(table).where(where)
        .offset(pageQuery.offset()).limit(pageQuery.pageSize())
        .fetch(r -> new UserVO(...));
    return PageResult.of(list, total);
}
```

### 写入

```java
public Long insert(String username, String encodedPassword) {
    long id = snowflakeIdGenerator.nextId();
    dsl.insertInto(DSL.table("sys_user"))
        .set(DSL.field("id", Long.class), id)
        .set(DSL.field("username"), username)
        .set(DSL.field("password"), encodedPassword)
        // 审计字段手动填充
        .set(DSL.field("create_by"), SecurityUtils.getCurrentUsername())
        .set(DSL.field("create_time", LocalDateTime.class), LocalDateTime.now())
        .set(DSL.field("deleted", Boolean.class), false)
        .execute();
    return id;
}
```

### 审计字段

当前需要在每次 insert/update 时手动填充 `create_by`、`create_time`、`update_by`、`update_time`、`deleted`。

## 前端开发规范

### API 层（TanStack Query）

```typescript
// features/system/xxx/api.ts
const KEYS = { list: ['system', 'xxx'] as const };

export function useXxxList(params?: PageQuery & { keyword?: string }) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<XxxVO>>('/api/v1/system/xxxs', params),
  });
}

export function useCreateXxx() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: XxxCommand.Create) => post('/api/v1/system/xxxs', data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}
```

### 权限控制

```typescript
import { usePerm } from '@/shared/hooks/usePerm';

const { has } = usePerm();
{has('system:xxx:create') && <Button type="primary">新增</Button>}
```

### 页面模式

| 场景 | 组件选择 |
|------|---------|
| 普通列表页（用户/角色/配置/日志/文件/任务） | AG Grid (NxTable) + columns.tsx |
| 树形结构（菜单） | Ant Design Table expandable |
| 主从关系（字典类型+数据） | Ant Design List/Table 左右分栏 |

### 请求封装

`src/app/request.ts` 导出 `get<T>`, `post<T>`, `put<T>`, `del<T>` 四个函数，自动解包 `R<T>`，直接返回 `data`。401 自动跳登录页。

---

## 构建与运行

### 后端

```bash
cd server
mvn clean package -DskipTests                    # 构建
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=dev  # 运行（开发）
java -jar nxboot-admin/target/nxboot-admin.jar --spring.profiles.active=prod # 运行（生产）
```

### 前端

```bash
cd client
pnpm install    # 安装依赖
pnpm dev        # 开发服务器（代理 /api → localhost:8080）
pnpm build      # 生产构建
```

### 环境变量（生产必配）

| 变量 | 说明 |
|------|------|
| `NXBOOT_JWT_SECRET` | JWT 签名密钥（至少 256 位） |
| `DB_URL` | PostgreSQL 连接地址 |
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |

---

## 禁止事项

- **不要**手写 SQL 字符串拼接（用 jOOQ DSL 的参数化查询）
- **不要**在 Controller 里写业务逻辑（业务逻辑在 Service 层）
- **不要**跨领域包直接调用 Repository（通过 Service 调）
- **不要**用 Lombok（用 JDK Record）
- **不要**用 MapStruct（jOOQ fetchInto 或手动构造）
- **不要**在前端用 `any` 类型
- **不要**硬编码颜色/间距值（用 Design Tokens CSS 变量）
- **不要**绕过 `request.ts` 直接用 axios
- **不要**在 commit 中包含 `application-prod.yml` 的真实密码

## 已知限制（v1.0）

- jOOQ 使用字符串引用（`DSL.field()`），未启用 codegen 类型安全。后续迭代将启用
- 审计字段需手动填充（启用 codegen 后可恢复 RecordListener 自动填充）
- 操作日志只有查询壳子，未实现写入（缺 AccessLogInterceptor）
- CorsConfig 允许所有来源（生产环境需修改为具体域名）
