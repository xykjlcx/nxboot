# jOOQ Codegen 全量迁移 + 监控 Dashboard 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 消除所有 `field("xxx")` / `table("xxx")` 字符串引用（JooqHelper + 全部 Repository + Service），并补全前端监控 Dashboard 页面。

**Architecture:** JooqHelper 公共 API 从 `String tableName` 改为 `Table<?>` + 内部审计字段用 typed 常量。11 个 Repository + 4 个 Service/Aspect 全量迁移到 codegen 引用。监控 Dashboard 新增 3 个前端文件 + 1 个 Flyway 迁移。

**Tech Stack:** jOOQ codegen (generated classes) / Spring Boot / React + antd (Card/Progress/Descriptions) / TanStack Query

---

## Part A: jOOQ Codegen 全量迁移

### Task 1: JooqHelper — Table<?> API + typed 审计字段常量

**Files:**
- Modify: `server/nxboot-framework/src/main/java/com/nxboot/framework/jooq/JooqHelper.java`

- [ ] **Step 1: 替换 imports + 添加 typed 常量**

移除 `import static org.jooq.impl.DSL.field` 和 `import static org.jooq.impl.DSL.table`。

添加 imports:
```java
import org.jooq.Field;
import org.jooq.Table;
```

在类顶部添加常量:
```java
// 所有 sys_ 表共享的审计字段
private static final Field<Long> ID = DSL.field("id", Long.class);
private static final Field<String> CREATE_BY = DSL.field("create_by", String.class);
private static final Field<LocalDateTime> CREATE_TIME = DSL.field("create_time", LocalDateTime.class);
private static final Field<String> UPDATE_BY = DSL.field("update_by", String.class);
private static final Field<LocalDateTime> UPDATE_TIME = DSL.field("update_time", LocalDateTime.class);
private static final Field<Integer> DELETED = DSL.field("deleted", Integer.class);
private static final Field<Integer> VERSION = DSL.field("version", Integer.class);
```

- [ ] **Step 2: 迁移 page() 方法**

```java
public static <T> PageResult<T> page(DSLContext dsl, Table<?> table,
                                      Condition extra, int offset, int size,
                                      Function<Record, T> mapper) {
    Condition condition = notDeleted();
    if (extra != null) condition = condition.and(extra);

    long total = dsl.selectCount().from(table).where(condition).fetchOneInto(Long.class);
    if (total == 0) return PageResult.empty();

    List<T> list = dsl.select().from(table).where(condition)
            .orderBy(CREATE_TIME.desc())
            .offset(offset).limit(size)
            .fetch(mapper::apply);
    return PageResult.of(list, total);
}
```

- [ ] **Step 3: 迁移 findById() 方法**

```java
public static Record findById(DSLContext dsl, Table<?> table, Long id) {
    return dsl.select().from(table)
            .where(ID.eq(id)).and(notDeleted())
            .fetchOne();
}
```

- [ ] **Step 4: 迁移 softDelete() 方法**

```java
public static void softDelete(DSLContext dsl, Table<?> table, Long id, String operator) {
    dsl.update(table)
            .set(DELETED, Constants.DELETED)
            .set(UPDATE_BY, operator)
            .set(UPDATE_TIME, LocalDateTime.now())
            .where(ID.eq(id))
            .execute();
}
```

- [ ] **Step 5: 迁移 setAuditInsert() + setAuditUpdate()**

```java
public static Long setAuditInsert(InsertSetMoreStep<?> step,
                                   SnowflakeIdGenerator idGen, String operator) {
    Long id = idGen.nextId();
    LocalDateTime now = LocalDateTime.now();
    step.set(ID, id)
        .set(CREATE_BY, operator)
        .set(CREATE_TIME, now)
        .set(UPDATE_BY, operator)
        .set(UPDATE_TIME, now)
        .set(DELETED, Constants.NOT_DELETED);
    return id;
}

public static void setAuditUpdate(UpdateSetMoreStep<?> step, String operator) {
    step.set(UPDATE_BY, operator)
        .set(UPDATE_TIME, LocalDateTime.now());
}
```

- [ ] **Step 6: 迁移 optimisticUpdate()**

```java
public static int optimisticUpdate(DSLContext dsl, Table<?> table, Long id, int currentVersion,
                                    Consumer<UpdateSetMoreStep<?>> setter) {
    var step = dsl.update(table).set(VERSION, currentVersion + 1);
    setter.accept(step);
    int rows = step.where(ID.eq(id))
            .and(VERSION.eq(currentVersion))
            .and(notDeleted())
            .execute();
    if (rows == 0) {
        throw new BusinessException(ErrorCode.BIZ_REFERENCED, "数据已被其他用户修改，请刷新后重试");
    }
    return currentVersion + 1;
}
```

- [ ] **Step 7: 迁移 keywordCondition() + batchInsert() + notDeleted()**

```java
@SafeVarargs
public static Condition keywordCondition(String keyword, Field<String>... fields) {
    if (keyword == null || keyword.isBlank() || fields.length == 0) return null;
    String pattern = "%" + keyword + "%";
    Condition result = DSL.falseCondition();
    for (Field<String> f : fields) {
        result = result.or(f.likeIgnoreCase(pattern));
    }
    return result;
}

public static void batchInsert(DSLContext dsl, Table<?> table,
                                List<Field<?>> fields, List<List<Object>> records) {
    if (records == null || records.isEmpty()) return;
    var insert = dsl.insertInto(table, fields);
    for (List<Object> values : records) {
        insert = insert.values(values);
    }
    insert.execute();
}

public static Condition notDeleted() {
    return DELETED.eq(Constants.NOT_DELETED);
}
```

- [ ] **Step 8: 编译验证**

Run: `mvn -f server/pom.xml compile -q -pl nxboot-framework -am`

预期：编译失败（下游 Repository 调用签名不匹配），这是正确的——下面的任务修复它们。

- [ ] **Step 9: Commit**

```bash
git add server/nxboot-framework/src/main/java/com/nxboot/framework/jooq/JooqHelper.java
git commit -m "refactor(framework)：JooqHelper 迁移到 Table<?> API + typed 审计字段常量"
```

---

### Task 2: 低复杂度 Repository 迁移（LoginLog / UserSocial / JobLog / Log）

**Files:**
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/auth/repository/LoginLogRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/auth/repository/UserSocialRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/job/repository/JobLogRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/log/repository/LogRepository.java`

**每个文件的迁移模式（以 LoginLogRepository 为例）：**

- [ ] **Step 1: 替换 imports**

移除:
```java
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
```

添加:
```java
import static com.nxboot.generated.jooq.tables.SysLoginLog.SYS_LOGIN_LOG;
```

- [ ] **Step 2: 替换 table() → codegen 表引用**

`table("sys_login_log")` → `SYS_LOGIN_LOG`

如果有 `private static final String TABLE = "sys_login_log";` 常量，改为：
`private static final var TABLE = SYS_LOGIN_LOG;` 或直接内联。

- [ ] **Step 3: 替换 field() → codegen 字段引用**

`field("username")` → `SYS_LOGIN_LOG.USERNAME`
`field("ip")` → `SYS_LOGIN_LOG.IP`
以此类推，所有 `field("xxx")` 都改为 `SYS_XXX.XXX`。

- [ ] **Step 4: 替换 r.get() 调用**

`r.get("username", String.class)` → `r.get(SYS_LOGIN_LOG.USERNAME)`
`r.get("id", Long.class)` → `r.get(SYS_LOGIN_LOG.ID)`

- [ ] **Step 5: 替换 JooqHelper 调用参数**

`JooqHelper.keywordCondition(keyword, "username", "ip")` → `JooqHelper.keywordCondition(keyword, SYS_LOGIN_LOG.USERNAME, SYS_LOGIN_LOG.IP)`

- [ ] **Step 6: 对 UserSocialRepository / JobLogRepository / LogRepository 重复步骤 1-5**

各自的 codegen 表引用:
- UserSocialRepository: `SYS_USER_SOCIAL`
- JobLogRepository: `SYS_JOB_LOG`
- LogRepository: `SYS_LOG`

- [ ] **Step 7: Commit**

```bash
git add server/nxboot-system/src/main/java/com/nxboot/system/auth/repository/LoginLogRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/auth/repository/UserSocialRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/job/repository/JobLogRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/log/repository/LogRepository.java
git commit -m "refactor(system)：LoginLog/UserSocial/JobLog/Log Repository codegen 迁移"
```

---

### Task 3: 中复杂度 Repository 迁移（Job / Dept / Role / User）

**Files:**
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/job/repository/JobRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/dept/repository/DeptRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/role/repository/RoleRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/user/repository/UserRepository.java`

- [ ] **Step 1: 迁移 JobRepository**

codegen: `SYS_JOB`。注意 `JooqHelper.page(dsl, TABLE, ...)` 和 `JooqHelper.softDelete(dsl, TABLE, ...)` 的 TABLE 参数改为 `SYS_JOB`。

- [ ] **Step 2: 迁移 DeptRepository**

codegen: `SYS_DEPT`。注意树形查询中的 `field("parent_id")` → `SYS_DEPT.PARENT_ID`。

- [ ] **Step 3: 迁移 RoleRepository**

codegen: `SYS_ROLE` + `SYS_ROLE_MENU` + `SYS_USER_ROLE`。注意多表关联查询中的 field 引用需要对应各自的表。

- [ ] **Step 4: 迁移 UserRepository**

codegen: `SYS_USER` + `SYS_USER_ROLE`。注意 existsByUsername 等方法中的 field 引用。

- [ ] **Step 5: Commit**

```bash
git add server/nxboot-system/src/main/java/com/nxboot/system/job/repository/JobRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/dept/repository/DeptRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/role/repository/RoleRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/user/repository/UserRepository.java
git commit -m "refactor(system)：Job/Dept/Role/User Repository codegen 迁移"
```

---

### Task 4: 高复杂度 Repository 迁移（Menu / Dict）+ ConfigRepository 补齐

**Files:**
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/menu/repository/MenuRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/dict/repository/DictRepository.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/config/repository/ConfigRepository.java`

- [ ] **Step 1: 迁移 MenuRepository**

codegen: `SYS_MENU` + `SYS_ROLE_MENU`。47 个 field() 引用，是最大的文件。注意:
- `getUserMenuTree` 中按角色过滤的子查询要正确引用 `SYS_ROLE_MENU.ROLE_ID` / `SYS_ROLE_MENU.MENU_ID`
- menu_type / sort_order / visible / enabled 等字段全部改为 codegen

- [ ] **Step 2: 迁移 DictRepository**

codegen: `SYS_DICT_TYPE` + `SYS_DICT_DATA`。注意双表各自有独立的 field 引用，不要混淆。

- [ ] **Step 3: ConfigRepository 补齐 JooqHelper 调用**

当前 ConfigRepository 对 JooqHelper 仍传字符串:
- `JooqHelper.page(dsl, "sys_config", ...)` → `JooqHelper.page(dsl, SYS_CONFIG, ...)`
- `JooqHelper.findById(dsl, "sys_config", id)` → `JooqHelper.findById(dsl, SYS_CONFIG, id)`
- `JooqHelper.softDelete(dsl, "sys_config", id, op)` → `JooqHelper.softDelete(dsl, SYS_CONFIG, id, op)`
- `JooqHelper.keywordCondition(keyword, "config_key", "config_name")` → `JooqHelper.keywordCondition(keyword, SYS_CONFIG.CONFIG_KEY, SYS_CONFIG.CONFIG_NAME)`

- [ ] **Step 4: Commit**

```bash
git add server/nxboot-system/src/main/java/com/nxboot/system/menu/repository/MenuRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/dict/repository/DictRepository.java \
  server/nxboot-system/src/main/java/com/nxboot/system/config/repository/ConfigRepository.java
git commit -m "refactor(system)：Menu/Dict/Config Repository codegen 迁移"
```

---

### Task 5: Service / Aspect 层 codegen 迁移

**Files:**
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/file/service/FileService.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/auth/service/OAuth2Service.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/auth/service/AuthService.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/auth/service/UserDetailsServiceImpl.java`
- Modify: `server/nxboot-system/src/main/java/com/nxboot/system/dept/DataScopeAspect.java`

- [ ] **Step 1: 迁移 FileService**

使用 `SYS_FILE`。替换所有 `field("xxx")` / `table("xxx")`。

- [ ] **Step 2: 迁移 OAuth2Service**

使用 `SYS_USER` + `SYS_USER_ROLE`。替换 `createLocalUser` 和 `existsByUsername` 中的字符串引用。

- [ ] **Step 3: 迁移 AuthService**

使用 `SYS_USER`（如果有直接 DSL 查询）。

- [ ] **Step 4: 迁移 UserDetailsServiceImpl**

使用 `SYS_USER` + `SYS_USER_ROLE` + `SYS_ROLE` + `SYS_ROLE_MENU` + `SYS_MENU`。

- [ ] **Step 5: 迁移 DataScopeAspect**

使用 `SYS_DEPT` + `SYS_ROLE_DEPT`。注意 AOP 切面中构建 Condition 时的字段引用。

- [ ] **Step 6: Commit**

```bash
git add server/nxboot-system/src/main/java/com/nxboot/system/file/service/FileService.java \
  server/nxboot-system/src/main/java/com/nxboot/system/auth/service/OAuth2Service.java \
  server/nxboot-system/src/main/java/com/nxboot/system/auth/service/AuthService.java \
  server/nxboot-system/src/main/java/com/nxboot/system/auth/service/UserDetailsServiceImpl.java \
  server/nxboot-system/src/main/java/com/nxboot/system/dept/DataScopeAspect.java
git commit -m "refactor(system)：Service/Aspect 层 codegen 迁移"
```

---

### Task 6: 全量验证 — 零字符串引用

- [ ] **Step 1: 编译验证**

Run: `mvn -f server/pom.xml compile -q`

Expected: 零错误

- [ ] **Step 2: 字符串引用 grep 验证**

Run:
```bash
grep -rn 'import static org.jooq.impl.DSL.field' server/nxboot-system/src/main/java/ server/nxboot-framework/src/main/java/ | grep -v target
grep -rn 'import static org.jooq.impl.DSL.table' server/nxboot-system/src/main/java/ server/nxboot-framework/src/main/java/ | grep -v target
```

Expected: 零结果（所有 DSL.field / DSL.table 静态导入已消除）

- [ ] **Step 3: API 回归验证**

```bash
TOKEN=$(curl -s http://localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["token"])')
curl -s "http://localhost:8080/api/v1/system/users?pageNum=1&pageSize=5" -H "Authorization: Bearer $TOKEN" | python3 -m json.tool | head -20
```

Expected: 用户列表正常返回

注意：此步需要重新打包并重启后端才能验证 API。如果后端未重启，跳过此步，在 submission 中标注。

- [ ] **Step 4: 更新 CLAUDE.md 已知限制**

从"已知限制"中移除 "jOOQ codegen 已启用，但仅 ConfigRepository 完成类型安全迁移"。

- [ ] **Step 5: Commit**

```bash
git add CLAUDE.md
git commit -m "docs：更新 CLAUDE.md 移除 codegen 迁移限制项"
```

---

## Part B: 监控 Dashboard

### Task 7: V24 菜单记录 + 前端路由

**Files:**
- Create: `server/nxboot-admin/src/main/resources/db/migration/V24__add_monitor_menu.sql`
- Modify: `client/src/app/routes.tsx`

- [ ] **Step 1: 创建 V24 迁移**

```sql
-- 服务器监控菜单（幂等写法）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (1012, 100, '服务器监控', 'C', '/system/monitor', NULL, 'system:monitor:list', 'DashboardOutlined', 13, 1, 1, 1, NOW(), 1, NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- admin 角色绑定
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1012) ON CONFLICT DO NOTHING;
```

- [ ] **Step 2: 执行迁移验证**

Run: `psql -d nxboot -f V24__add_monitor_menu.sql`

Expected: INSERT 成功或 ON CONFLICT 跳过

- [ ] **Step 3: 添加前端路由**

在 `routes.tsx` 添加:
```tsx
const Monitor = lazy(() => import("@/features/system/monitor/pages/Monitor"));
```

在 system children 中添加:
```tsx
{ path: "monitor", element: <LazyLoad><Monitor /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
```

- [ ] **Step 4: Commit**

```bash
git add server/nxboot-admin/src/main/resources/db/migration/V24__add_monitor_menu.sql client/src/app/routes.tsx
git commit -m "feat(db+client)：V24 监控菜单 + 前端路由"
```

---

### Task 8: 前端监控 Dashboard 页面

**Files:**
- Create: `client/src/features/system/monitor/types.ts`
- Create: `client/src/features/system/monitor/api.ts`
- Create: `client/src/features/system/monitor/pages/Monitor.tsx`

- [ ] **Step 1: 创建 types.ts**

```typescript
export interface CpuInfo {
  name: string;
  cores: number;
  usage: number;
}

export interface MemoryInfo {
  total: string;
  used: string;
  free: string;
  usage: number;
}

export interface JvmInfo {
  heapUsed: string;
  heapMax: string;
  heapUsage: number;
  javaVersion: string;
  uptime: string;
}

export interface DiskInfo {
  total: string;
  used: string;
  free: string;
  usage: number;
}

export interface MonitorData {
  cpu: CpuInfo;
  memory: MemoryInfo;
  jvm: JvmInfo;
  disk: DiskInfo;
}
```

- [ ] **Step 2: 创建 api.ts**

```typescript
import { useQuery } from "@tanstack/react-query";
import { get } from "@/app/request";
import type { MonitorData } from "./types";

export function useMonitorServer() {
  return useQuery({
    queryKey: ["monitor", "server"],
    queryFn: () => get<MonitorData>("/api/v1/system/monitor/server"),
    refetchInterval: 10_000, // 10 秒轮询
  });
}
```

- [ ] **Step 3: 创建 Monitor.tsx**

4 张 Card，每张包含 `Progress type="dashboard"` 环形图 + 描述信息。

```tsx
import { Row, Col, Card, Progress, Descriptions, Spin } from "antd";
import { useMonitorServer } from "../api";

function Monitor() {
  const { data, isLoading } = useMonitorServer();

  if (isLoading || !data) return <Spin tip="加载中..." style={{ display: "block", marginTop: 100 }} />;

  const { cpu, memory, jvm, disk } = data;

  const usageColor = (v: number) => (v > 90 ? "#ff4d4f" : v > 70 ? "#faad14" : "#52c41a");

  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} sm={12} xl={6}>
        <Card title="CPU">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={cpu.usage} strokeColor={usageColor(cpu.usage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="型号">{cpu.name}</Descriptions.Item>
            <Descriptions.Item label="核心数">{cpu.cores}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
      <Col xs={24} sm={12} xl={6}>
        <Card title="内存">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={memory.usage} strokeColor={usageColor(memory.usage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="已用">{memory.used}</Descriptions.Item>
            <Descriptions.Item label="空闲">{memory.free}</Descriptions.Item>
            <Descriptions.Item label="总量">{memory.total}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
      <Col xs={24} sm={12} xl={6}>
        <Card title="JVM">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={jvm.heapUsage} strokeColor={usageColor(jvm.heapUsage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="堆内存">{jvm.heapUsed} / {jvm.heapMax}</Descriptions.Item>
            <Descriptions.Item label="Java 版本">{jvm.javaVersion}</Descriptions.Item>
            <Descriptions.Item label="运行时间">{jvm.uptime}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
      <Col xs={24} sm={12} xl={6}>
        <Card title="磁盘">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={disk.usage} strokeColor={usageColor(disk.usage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="已用">{disk.used}</Descriptions.Item>
            <Descriptions.Item label="空闲">{disk.free}</Descriptions.Item>
            <Descriptions.Item label="总量">{disk.total}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
    </Row>
  );
}

export default Monitor;
```

- [ ] **Step 4: 编译验证**

Run: `cd client && npx tsc --noEmit && pnpm build`

Expected: 零错误 + build 成功

- [ ] **Step 5: Commit**

```bash
git add client/src/features/system/monitor/
git commit -m "feat(client)：监控 Dashboard 页面（CPU/内存/JVM/磁盘 4 卡片 + 10s 轮询）"
```

---

### Task 9: 生成 Review Submission

- [ ] **Step 1: 生成 submission**

文件路径: `docs/reviews/2026-04-03-codegen-dashboard-submission.md`

使用 REVIEW_SUBMISSION_TEMPLATE.md 模板，包含全部验证命令和结果。

- [ ] **Step 2: Commit + push**

```bash
git add docs/reviews/2026-04-03-codegen-dashboard-submission.md
git commit -m "docs：Codegen + Dashboard Submission（READY_FOR_REVIEW）"
git push
```
