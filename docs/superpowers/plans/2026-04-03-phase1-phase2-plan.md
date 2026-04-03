# NxBoot Phase 1 + Phase 2 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现部门管理 + 数据权限（Phase 1）和 7 项架构质量改进（Phase 2），让 NxBoot 对标 RuoYi 精简版。

**Architecture:** Phase 1 新增 dept 领域（DDD 垂直切片，与 user/role 对称）+ @DataScope AOP 拦截。Phase 2 在已有基础设施上做小幅增强（JooqHelper/ErrorBoundary/配置调优）。

**Tech Stack:** Spring Boot 3.4 + jOOQ + PostgreSQL + Flyway + React 18 + Ant Design 6 + TypeScript

---

## Phase 1: 核心业务功能

### Task 1: 部门管理 — 数据库迁移

**Files:**
- Create: `server/nxboot-admin/src/main/resources/db/migration/V14__create_sys_dept.sql`
- Create: `server/nxboot-admin/src/main/resources/db/migration/V15__init_dept_data.sql`

- [ ] **Step 1: 建表迁移脚本**

```sql
-- V14__create_sys_dept.sql
CREATE TABLE sys_dept (
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT NOT NULL DEFAULT 0,
    dept_name   VARCHAR(64) NOT NULL,
    sort_order  INT DEFAULT 0,
    leader      VARCHAR(64),
    phone       VARCHAR(20),
    email       VARCHAR(128),
    enabled     INT DEFAULT 1,
    create_by   VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INT DEFAULT 0
);

COMMENT ON TABLE sys_dept IS '部门';
COMMENT ON COLUMN sys_dept.parent_id IS '上级部门ID（0=顶级）';
CREATE INDEX idx_dept_parent ON sys_dept(parent_id);
```

- [ ] **Step 2: 初始数据**

```sql
-- V15__init_dept_data.sql
INSERT INTO sys_dept (id, parent_id, dept_name, sort_order, leader, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES
(100, 0, 'NxBoot科技', 0, 'admin', 1, 'admin', NOW(), 'admin', NOW(), 0),
(101, 100, '技术部', 1, NULL, 1, 'admin', NOW(), 'admin', NOW(), 0),
(102, 100, '产品部', 2, NULL, 1, 'admin', NOW(), 'admin', NOW(), 0),
(103, 100, '运营部', 3, NULL, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 用户表增加部门关联
ALTER TABLE sys_user ADD COLUMN dept_id BIGINT DEFAULT NULL;
COMMENT ON COLUMN sys_user.dept_id IS '所属部门ID';

-- 更新 admin 用户的部门
UPDATE sys_user SET dept_id = 100 WHERE username = 'admin';

-- 菜单：在系统管理下添加部门管理
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
SELECT 1080, id, '部门管理', 'C', '/system/dept', NULL, 'system:dept:list', 'TeamOutlined', 3, 1, 1, 'admin', NOW(), 'admin', NOW(), 0
FROM sys_menu WHERE menu_name = '系统管理' AND menu_type = 'M' LIMIT 1;
```

- [ ] **Step 3: 验证迁移**

Run: `cd server && mvn compile -q`

---

### Task 2: 部门管理 — 后端领域（model + repository + service + controller）

**Files:**
- Create: `server/nxboot-system/src/main/java/com/nxboot/system/dept/model/DeptVO.java`
- Create: `server/nxboot-system/src/main/java/com/nxboot/system/dept/model/DeptCommand.java`
- Create: `server/nxboot-system/src/main/java/com/nxboot/system/dept/repository/DeptRepository.java`
- Create: `server/nxboot-system/src/main/java/com/nxboot/system/dept/service/DeptService.java`
- Create: `server/nxboot-system/src/main/java/com/nxboot/system/dept/controller/DeptController.java`

- [ ] **Step 1: DeptVO**

```java
package com.nxboot.system.dept.model;

import java.time.LocalDateTime;
import java.util.List;

public record DeptVO(
    Long id,
    Long parentId,
    String deptName,
    Integer sortOrder,
    String leader,
    String phone,
    String email,
    Boolean enabled,
    LocalDateTime createTime,
    List<DeptVO> children
) {}
```

- [ ] **Step 2: DeptCommand**

```java
package com.nxboot.system.dept.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class DeptCommand {
    public record Create(
        @NotNull Long parentId,
        @NotBlank String deptName,
        Integer sortOrder,
        String leader,
        String phone,
        String email
    ) {}

    public record Update(
        Long parentId,
        String deptName,
        Integer sortOrder,
        String leader,
        String phone,
        String email,
        Boolean enabled
    ) {}

    private DeptCommand() {}
}
```

- [ ] **Step 3: DeptRepository**

参考 `MenuRepository` 的 buildTree 模式。使用 JooqHelper。
表名常量 `TABLE = "sys_dept"`。
方法：`buildTree()`、`findById(Long)`、`findAll()`、`insert(...)`、`update(...)`、`softDelete(...)`、`hasChildren(Long)`。
toVO 中类型转换用 `r.get("field", Type.class)` 模式。

- [ ] **Step 4: DeptService**

参考 `MenuService`。方法：`tree()`、`getById(Long)`、`create(DeptCommand.Create)`、`update(Long, DeptCommand.Update)`、`delete(Long)`。
删除前检查 `hasChildren`。

- [ ] **Step 5: DeptController**

路径 `/api/v1/system/depts`。权限前缀 `system:dept:*`。
所有写操作加 `@Log(module = "部门管理", operation = "xxx")`。
GET `/tree` — 查询部门树
GET `/{id}` — 详情
POST — 创建
PUT `/{id}` — 更新
DELETE `/{id}` — 删除

- [ ] **Step 6: 验证**

Run: `cd server && mvn compile -q`
Expected: 零错误

---

### Task 3: 部门管理 — 前端页面

**Files:**
- Create: `client/src/features/system/dept/types.ts`
- Create: `client/src/features/system/dept/api.ts`
- Create: `client/src/features/system/dept/columns.tsx`
- Create: `client/src/features/system/dept/pages/DeptList.tsx`
- Create: `client/src/features/system/dept/pages/DeptForm.tsx`
- Modify: `client/src/app/routes.tsx` — 添加 dept 路由

- [ ] **Step 1: types.ts** — DeptVO + DeptCommand（与后端对称）
- [ ] **Step 2: api.ts** — useDeptTree / useCreateDept / useUpdateDept / useDeleteDept
- [ ] **Step 3: columns.tsx** — NxColumn<DeptVO>[]，含 deptName/leader/phone/email/enabled/sortOrder
- [ ] **Step 4: DeptList.tsx** — NxTable expandable 树形表格（参考 MenuList）
- [ ] **Step 5: DeptForm.tsx** — NxDrawer 表单，parent 选择用 TreeSelect
- [ ] **Step 6: routes.tsx** — 添加 `/system/dept` 路由
- [ ] **Step 7: 验证**

Run: `cd client && npx tsc --noEmit && pnpm build`

---

### Task 4: 数据权限 — @DataScope 注解 + AOP

**Files:**
- Create: `server/nxboot-common/src/main/java/com/nxboot/common/annotation/DataScope.java`
- Create: `server/nxboot-system/src/main/java/com/nxboot/system/dept/DataScopeAspect.java`
- Create: `server/nxboot-admin/src/main/resources/db/migration/V16__add_data_scope.sql`
- Modify: `server/nxboot-framework/src/main/java/com/nxboot/framework/jooq/JooqHelper.java` — 增加 dataScopeCondition

- [ ] **Step 1: 迁移脚本** — sys_role 加 data_scope 字段 + sys_role_dept 关联表
- [ ] **Step 2: @DataScope 注解** — `module`（标注在 Service 方法上）
- [ ] **Step 3: DataScopeAspect** — AOP 切面，根据当前用户角色的 data_scope 构建 jOOQ Condition，通过 ThreadLocal 传递
- [ ] **Step 4: JooqHelper.dataScopeCondition()** — 从 ThreadLocal 读取 Condition 并拼接
- [ ] **Step 5: 给 UserService.page() 加 @DataScope** — 作为示例
- [ ] **Step 6: 验证**

Run: `cd server && mvn compile -q`

---

## Phase 2: 架构质量

### Task 5: T2.1 审计字段自动填充

**Files:**
- Modify: `server/nxboot-framework/src/main/java/com/nxboot/framework/jooq/JooqHelper.java`

- [ ] **Step 1: 添加 auditInsertFields 和 auditUpdateFields 方法**

```java
public static void setAuditInsert(org.jooq.InsertSetMoreStep<?> step,
                                   SnowflakeIdGenerator idGen, String operator) {
    LocalDateTime now = LocalDateTime.now();
    step.set(field("id"), idGen.nextId())
        .set(field("create_by"), operator)
        .set(field("create_time"), now)
        .set(field("update_by"), operator)
        .set(field("update_time"), now)
        .set(field("deleted"), Constants.NOT_DELETED);
}

public static void setAuditUpdate(org.jooq.UpdateSetMoreStep<?> step, String operator) {
    step.set(field("update_by"), operator)
        .set(field("update_time"), LocalDateTime.now());
}
```

- [ ] **Step 2: 验证编译**

Run: `cd server && mvn compile -q`

---

### Task 6: T2.2 数据库索引补全

**Files:**
- Create: `server/nxboot-admin/src/main/resources/db/migration/V17__add_indexes.sql`

- [ ] **Step 1: 索引迁移脚本**

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_username ON sys_user(username) WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_role_key ON sys_role(role_key) WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_config_key ON sys_config(config_key) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_dict_data_type ON sys_dict_data(dict_type);
```

- [ ] **Step 2: 验证编译**

Run: `cd server && mvn compile -q`

---

### Task 7: T2.3 错误处理契约统一

**Files:**
- Modify: `server/nxboot-framework/src/main/java/com/nxboot/framework/web/GlobalExceptionHandler.java`
- Modify: `client/src/app/request.ts`

- [ ] **Step 1: 后端统一** — 所有异常返回 HTTP 200 + R(错误码, 消息)，仅 AuthenticationException 返回 HTTP 401
- [ ] **Step 2: 前端统一** — request.ts 的 unwrap 检查 R.code，interceptor 只处理网络错误和 401
- [ ] **Step 3: 验证**

Run: `cd server && mvn compile -q && cd ../client && npx tsc --noEmit`

---

### Task 8: T2.4 路由级 Error Boundary

**Files:**
- Modify: `client/src/app/routes.tsx`

- [ ] **Step 1: 为每个路由子树包裹 ErrorBoundary**

在 `routes.tsx` 的 BasicLayout children 中，用自定义的 `RouteErrorBoundary` 组件包裹，提供"页面加载失败 + 重试"的 fallback UI。

- [ ] **Step 2: 验证**

Run: `cd client && npx tsc --noEmit && pnpm build`

---

### Task 9: T2.5 乐观锁

**Files:**
- Create: `server/nxboot-admin/src/main/resources/db/migration/V18__add_version_field.sql`
- Modify: `server/nxboot-framework/src/main/java/com/nxboot/framework/jooq/JooqHelper.java`

- [ ] **Step 1: 迁移脚本** — 给 sys_user/sys_role/sys_config 加 `version INT DEFAULT 0`
- [ ] **Step 2: JooqHelper 增加 optimisticUpdate()** — `WHERE id = ? AND version = ?`，更新 `version = version + 1`，返回影响行数，0 则抛 OptimisticLockException
- [ ] **Step 3: 验证**

Run: `cd server && mvn compile -q`

---

### Task 10: T2.6 HikariCP 配置 + T2.7 Flyway 最佳实践

**Files:**
- Modify: `server/nxboot-admin/src/main/resources/application.yml`
- Modify: `server/nxboot-admin/src/main/resources/application-dev.yml`

- [ ] **Step 1: HikariCP 配置**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

- [ ] **Step 2: Flyway baseline 配置**

```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
```

- [ ] **Step 3: 创建 db/rollback/ 目录** 并在 README 中说明约定

- [ ] **Step 4: 验证**

Run: `cd server && mvn compile -q`

---

### Task 11: CLAUDE.md + Spec 更新

**Files:**
- Modify: `CLAUDE.md` — 更新部门管理、数据权限、审计自动填充、乐观锁等文档
- Modify: `docs/superpowers/specs/2026-04-03-nxboot-final-evolution-spec.md` — 标记已完成项

- [ ] **Step 1: CLAUDE.md 更新**
- [ ] **Step 2: Spec 标记进度**
- [ ] **Step 3: 全栈最终验证**

Run: `cd server && mvn compile -q && cd ../client && npx tsc --noEmit && pnpm build`
