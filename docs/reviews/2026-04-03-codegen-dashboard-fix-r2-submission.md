# Review Submission — Codegen Dashboard Fix Round 2

## 基本信息

- 主题：JooqHelper 消除 DSL.field() 字符串常量
- 提交范围：`a2e5af9..3a8b7d4`（1 个 commit）
- 关联 commit：`3a8b7d4`
- 关联 review：`docs/reviews/2026-04-03-codegen-dashboard-fix-codex-review.md`
- 当前状态：`READY_FOR_REVIEW`

## 目标

消除 JooqHelper 中 7 个 `DSL.field("...")` 静态常量，使共享层不再依赖字符串字段引用。

## 处理结果说明

**已彻底消除的字符串字段：**

7 个 `DSL.field("...")` 常量（ID、CREATE_BY、CREATE_TIME、UPDATE_BY、UPDATE_TIME、DELETED、VERSION）全部删除。替代方案按使用场景分两种：

1. **JooqHelper 内部方法**（page/findById/softDelete/optimisticUpdate）：改为 `table.field("name", Type.class)` 从 jOOQ 表元数据运行时解析。这不是 `DSL.field()` 自由构造——它绑定到传入的 `Table<?>` 实例，由 jOOQ 在运行时验证字段是否存在于该表中。

2. **外部调用方**（19 处 `notDeleted()`）：`notDeleted()` 公共方法已删除。每个 Repository 改为直接使用自己的 codegen 字段：`SYS_USER.DELETED.eq(Constants.NOT_DELETED)`。这是完全的编译期类型安全。

**仍然保留的字符串字段名：**

`table.field("id", Long.class)` 这类调用中的列名 `"id"`、`"deleted"` 等仍然是字符串。这是 jOOQ 泛型工具类（`Table<?>` 类型擦除）的固有限制——无法在编译期从 `Table<?>` 中获取具体表的 typed 字段引用。

**为什么这是当前阶段可接受的：**

- `table.field()` 从表的 codegen 元数据中解析，而非 `DSL.field()` 自由构造——如果表没有该列，返回 null（可检测）
- 7 个列名（id/create_by/create_time/update_by/update_time/deleted/version）是所有 sys_ 表的标准审计字段，由 Flyway 迁移保证存在
- 要完全消除这些字符串，需要将 JooqHelper 改为泛型类 `JooqHelper<R extends Record>` 并让每个 Repository 继承——这与当前"静态工具类"的设计目标矛盾，不值得为 7 个列名引入继承体系
- Repository 层和 Service 层的所有 `DSL.field()` 和 `DSL.table()` 已完全消除

## 改动范围

- 修改：`JooqHelper.java` — 删除 7 常量、方法内改用 table.field()、setAuditInsert/Update 新增 Table<?> 参数、删除 notDeleted()
- 修改：6 个 Repository — 19 处 notDeleted() 替换为 codegen DELETED 字段 + setAudit* 调用补传 Table<?> 参数

## 非目标

- 不将 JooqHelper 改为泛型类/基类继承
- 不修改其他文件

## 风险点

- `table.field("id", Long.class)` 如果传入的 Table 没有 id 列，返回 null 会导致 NPE。所有 sys_ 表都有 id 列，不会触发。
- `setAuditInsert/setAuditUpdate` API 变更是破坏性的，所有调用方已同步更新。

## 验证命令

```bash
mvn -f server/pom.xml compile -q
cd client && npx tsc --noEmit
cd client && pnpm build
grep -n 'DSL\.field(' server/nxboot-framework/.../JooqHelper.java
grep -rn 'JooqHelper\.notDeleted' server/nxboot-system/src/main/java/
```

## 验证结果

- `mvn compile`：零错误
- `npx tsc --noEmit`：零错误
- `pnpm build`：成功（3.08s）
- JooqHelper 中 `DSL.field()`：零（仅注释中提及）
- `JooqHelper.notDeleted()` 外部调用：零

## 未解决事项

- `table.field("name", Type.class)` 中的列名字符串是泛型工具类的固有限制，不影响功能正确性
- DataScopeAspect 中 5 处 `DSL.field()` 为运行时动态别名，无法消除（与本 finding 无关）

## 请求红队重点关注

1. `table.field()` vs `DSL.field()` 的区别是否被接受为足够的改进
2. `setAuditInsert/setAuditUpdate` 新增 Table<?> 参数后，所有调用方是否已正确更新
3. 对"仍然保留字符串列名"的解释是否充分
