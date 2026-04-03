# jOOQ Code Generation

## 生成命令

```bash
cd server
mvn generate-sources -P codegen -pl nxboot-admin
```

前提：本地 PostgreSQL 数据库 `nxboot` 正在运行，且 Flyway 迁移已执行过。

## 何时重新生成

- 新增/修改数据库表结构后
- Flyway 迁移脚本变更后

## 生成的代码位置

```
nxboot-system/src/main/java/com/nxboot/generated/jooq/
├── DefaultCatalog.java
├── Keys.java
├── Public.java
├── Tables.java              ← 所有表的静态引用入口
└── tables/
    ├── SysConfig.java       ← 表定义（字段、主键、索引）
    ├── SysUser.java
    ├── ...
    └── records/
        ├── SysConfigRecord.java   ← 可变记录（用于 insert/update）
        └── ...
```

## 迁移指南（字符串引用 → 类型安全）

```java
// 导入
import static com.nxboot.generated.jooq.tables.SysConfig.SYS_CONFIG;

// 表引用
table("sys_config")          → SYS_CONFIG

// 字段引用
field("config_key")          → SYS_CONFIG.CONFIG_KEY
field("id")                  → SYS_CONFIG.ID

// 读取字段值
r.get("id", Long.class)     → r.get(SYS_CONFIG.ID)
r.get("config_key", String.class) → r.get(SYS_CONFIG.CONFIG_KEY)
```

**已完成**：`ConfigRepository`（POC 示例）
**待迁移**：其他 Repository + JooqHelper 工具方法

## 配置文件

- 插件配置：`nxboot-admin/pom.xml`（`codegen` profile）
- 生成配置：`nxboot-admin/src/main/resources/jooq-codegen.xml`
- 数据库连接默认：`localhost:5432/nxboot`，用户 `ocean`，无密码

## 注意事项

- codegen 是 opt-in 的（Maven profile `codegen`），不会在普通构建时执行
- 生成的代码提交到 Git，不在 .gitignore 中排除
- JooqHelper 的 page/findById/softDelete 仍使用字符串表名，后续迁移时统一改造
