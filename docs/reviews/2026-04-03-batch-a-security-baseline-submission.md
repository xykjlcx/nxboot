# Batch A: 安全与运行时基线 — Submission

## 基本信息

- 主题：P1-1 运行时默认配置安全化 + P2-1 CORS 默认值收紧
- 提交范围：配置文件 + CorsConfig + 文档
- 关联 commit：db4564c
- 当前状态：`READY_FOR_REVIEW`

## 目标

消除"不配置也能跑"的危险默认值，让 JWT secret 和 CORS 在非 dev 环境下必须显式配置。

## 用户场景

1. **开发者本地开发**：`--spring.profiles.active=dev` 一键启动，JWT secret 和 CORS 均由 dev profile 提供
2. **运维部署生产**：必须配置 `NXBOOT_JWT_SECRET` 和 `NXBOOT_CORS_ORIGINS` 环境变量，否则 JWT 启动失败 / CORS 禁止跨域
3. **误操作防护**：不指定 profile 直接 `java -jar` 时，因 JWT secret 未解析而启动失败

## 假设

- Spring Boot 不设 `spring.profiles.active` 时只加载 `application.yml`，不会加载任何 profile 文件
- `${NXBOOT_JWT_SECRET}` 无默认值且环境变量未设时，Spring 属性解析报错导致启动失败
- CORS 未配置时返回空 `UrlBasedCorsConfigurationSource`，等同于无跨域规则（浏览器同源策略生效）

## 改动范围

- 后端：
  - `application.yml`：去掉 `spring.profiles.active: dev`，JWT secret 去掉默认值，新增 `nxboot.cors.allowed-origins` 占位
  - `application-dev.yml`：新增 `nxboot.cors.allowed-origins: http://localhost:5173,http://localhost:5174`
  - `CorsConfig.java`：空值不注册 CORS 规则，`*` 打印 WARN 日志
- 前端：无改动
- 数据库 / Flyway：无改动
- 文档：`README.md` 和 `CLAUDE.md` 更新启动说明和环境变量表

## 非目标

- 不改 `application-prod.yml`（它不涉及 JWT/CORS 配置，靠 application.yml 兜底）
- 不加 `@Profile` 注解做 Bean 级控制（CorsFilter 通过配置值空/非空区分行为即可）
- 不动 SecurityConfig（CORS 在 CorsFilter 层处理，不需要改 Security chain）

## 风险点

- 已有的 CI/CD 脚本如果之前靠不指定 profile 默认跑 dev，现在会启动失败 → 需要显式加 `--spring.profiles.active=dev`
- 如果有人通过 `nxboot.cors.allowed-origins` 属性名（而非环境变量）配置 CORS，仍然兼容（`@Value` 注解未变）

## 验证命令

```bash
# 编译通过
mvn -f server/pom.xml compile -q

# 查看最终配置
cat server/nxboot-admin/src/main/resources/application.yml | grep -A2 'secret\|cors'
cat server/nxboot-admin/src/main/resources/application-dev.yml | grep -A2 'cors'
```

## 验证结果

- 编译通过（无输出 = 成功）
- `application.yml` 中 `secret: ${NXBOOT_JWT_SECRET}`（无兜底）
- `application.yml` 中 `allowed-origins: ${NXBOOT_CORS_ORIGINS:}`（空默认）
- `application-dev.yml` 中 `allowed-origins: http://localhost:5173,http://localhost:5174`
- `CorsConfig.java` 空值时 log.info + 返回空 source，`*` 时 log.warn

## 演示路径 / 接口验证

- **验证 1（无 profile 启动失败）**：`java -jar nxboot-admin.jar` → 启动失败，报 `NXBOOT_JWT_SECRET` 无法解析
- **验证 2（dev 一键启动）**：`java -jar nxboot-admin.jar --spring.profiles.active=dev` → 正常启动，CORS 允许 localhost:5173
- **验证 3（prod 无 secret 失败）**：`java -jar nxboot-admin.jar --spring.profiles.active=prod` → 启动失败（未设 NXBOOT_JWT_SECRET）
- **验证 4（CORS 未配置）**：prod 未设 NXBOOT_CORS_ORIGINS → 日志输出 INFO "CORS allowed-origins 未配置"，跨域请求被浏览器拒绝

> 注：验证 1-4 为设计行为推演，未实际打 jar 运行（编译已通过验证）

## 数据与迁移说明

- 无 Flyway 变更
- 不影响已有数据
- 无 codegen 变更

## 体验说明

- 对前端用户无感知变化
- 开发者体验：dev 环境行为不变，prod 环境启动变为"显式配置"模式

## 未解决事项

- 验证 1-4 的实际 jar 运行测试未执行（需要打包），后续可在 CI 中覆盖
- `application-prod.yml` 中未显式列出 CORS 配置注释（依赖 README 说明）

## 请求红队重点关注

1. `${NXBOOT_JWT_SECRET}` 无默认值时 Spring Boot 的行为是否确实是启动失败（而非注入空字符串）
2. CorsFilter 返回空 source 时是否等同于"无 CORS 规则"（即浏览器同源策略生效）
3. CORS 配置从 `nxboot.cors.allowed-origins` 属性到 `NXBOOT_CORS_ORIGINS` 环境变量的映射是否正确（Spring Boot relaxed binding）
