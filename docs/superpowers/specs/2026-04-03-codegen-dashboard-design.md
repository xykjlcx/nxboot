# Design Spec: jOOQ Codegen 全量迁移 + 监控 Dashboard

## 1. jOOQ Codegen 全量迁移

### 目标

消除所有 `field("xxx")` / `table("xxx")` 字符串引用，包括 JooqHelper 内部和全部 Repository。

### JooqHelper API 改造

**公共 API：`String tableName` → `Table<?>`**

| 方法 | 旧签名 | 新签名 |
|------|--------|--------|
| page | `page(dsl, String tableName, ...)` | `page(dsl, Table<?> table, ...)` |
| findById | `findById(dsl, String tableName, id)` | `findById(dsl, Table<?> table, id)` |
| softDelete | `softDelete(dsl, String tableName, id, op)` | `softDelete(dsl, Table<?> table, id, op)` |
| optimisticUpdate | `optimisticUpdate(dsl, String tableName, ...)` | `optimisticUpdate(dsl, Table<?> table, ...)` |
| batchInsert | `batchInsert(dsl, String tableName, List<String>, ...)` | `batchInsert(dsl, Table<?> table, List<Field<?>>, ...)` |
| keywordCondition | `keywordCondition(keyword, String... fields)` | `keywordCondition(keyword, Field<String>... fields)` |

**内部审计字段：定义 typed 常量**

```java
// JooqHelper 内部常量——所有 sys_ 表共享的审计字段
private static final Field<Long> ID = DSL.field("id", Long.class);
private static final Field<String> CREATE_BY = DSL.field("create_by", String.class);
private static final Field<LocalDateTime> CREATE_TIME = DSL.field("create_time", LocalDateTime.class);
private static final Field<String> UPDATE_BY = DSL.field("update_by", String.class);
private static final Field<LocalDateTime> UPDATE_TIME = DSL.field("update_time", LocalDateTime.class);
private static final Field<Integer> DELETED = DSL.field("deleted", Integer.class);
private static final Field<Integer> VERSION = DSL.field("version", Integer.class);
```

`setAuditInsert` / `setAuditUpdate` / `notDeleted` / `optimisticUpdate` 内部全部用这些常量替代 `field("xxx")`。

**不变的方法：** `dataScopeCondition()` 无字符串引用，不需要改。

### Repository 迁移

10 个 Repository 全部按 ConfigRepository 模式迁移：

| Repository | 表引用 | 预计 field() 替换数 |
|---|---|---|
| UserRepository | SYS_USER | 20 |
| RoleRepository | SYS_ROLE + SYS_ROLE_MENU + SYS_USER_ROLE | 18 |
| MenuRepository | SYS_MENU + SYS_ROLE_MENU | 47 |
| DeptRepository | SYS_DEPT | 16 |
| DictRepository | SYS_DICT_TYPE + SYS_DICT_DATA | 25 |
| LogRepository | SYS_LOG | 17 |
| JobRepository | SYS_JOB | 18 |
| JobLogRepository | SYS_JOB_LOG | 10 |
| LoginLogRepository | SYS_LOGIN_LOG | 11 |
| UserSocialRepository | SYS_USER_SOCIAL | 10 |

**迁移模式（每个文件内）：**
1. 添加 `import static com.nxboot.generated.jooq.tables.SysXxx.SYS_XXX;`
2. `table("sys_xxx")` → `SYS_XXX`
3. `field("column_name")` → `SYS_XXX.COLUMN_NAME`
4. `r.get("column", Type.class)` → `r.get(SYS_XXX.COLUMN)`
5. `JooqHelper.xxx(dsl, "sys_xxx", ...)` → `JooqHelper.xxx(dsl, SYS_XXX, ...)`

**ConfigRepository 同步更新：** 当前 ConfigRepository 仍然向 JooqHelper 传字符串 `"sys_config"`，改为 `SYS_CONFIG`。

### 验证标准

- `mvn compile` 零错误
- 全文搜索 `field("` 和 `table("` 在 Repository + JooqHelper 中为零结果
- 现有 API 行为不变（接口不改、返回值不改）

---

## 2. 监控 Dashboard

### 目标

给已有的 `GET /api/v1/system/monitor/server` API 补上前端页面。

### API 响应结构

```json
{
  "cpu":    { "name": "...", "cores": 8, "usage": 45.2 },
  "memory": { "total": "16.0 GB", "used": "8.3 GB", "free": "7.7 GB", "usage": 51.9 },
  "jvm":    { "heapUsed": "512 MB", "heapMax": "1024 MB", "heapUsage": 50.0, "javaVersion": "21", "uptime": "12h 34m" },
  "disk":   { "total": "500 GB", "used": "250 GB", "free": "250 GB", "usage": 50.0 }
}
```

### 前端设计

**页面结构：** 4 张 Card（CPU / 内存 / JVM / 磁盘），每张包含 Progress 环形图 + 关键指标。

```
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  🖥️ CPU      │ │  💾 内存     │ │  ☕ JVM      │ │  💿 磁盘     │
│  [===] 45%  │ │  [===] 52%  │ │  [===] 50%  │ │  [===] 50%  │
│  8 核       │ │  8.3/16 GB  │ │  512/1024MB │ │  250/500 GB │
│  Intel...   │ │             │ │  Java 21    │ │             │
│             │ │             │ │  运行 12h   │ │             │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
```

**技术选型：**
- antd `Card` + `Progress type="dashboard"` + `Descriptions`
- TanStack Query `useQuery` 10 秒轮询
- 权限：`system:monitor:list`

### 新增文件

```
client/src/features/system/monitor/
├── types.ts           ← MonitorData 接口
├── api.ts             ← useMonitorServer hook
└── pages/Monitor.tsx  ← Dashboard 页面
```

### 补充配置

- `V24__add_monitor_menu.sql`：菜单记录 + admin 角色绑定（ON CONFLICT DO NOTHING 幂等）
- `routes.tsx`：新增 `/system/monitor` 路由
- `BasicLayout.tsx` iconRegistry 无需改（MonitorOutlined 已在 antd namespace 中可解析）

### 验证标准

- 页面可访问，4 张卡片正确显示
- 10 秒自动刷新
- `pnpm build` 成功
