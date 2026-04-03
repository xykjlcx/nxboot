# Batch A: 安全与运行时基线 — Codex Review

- 审查日期：2026-04-03
- 审查范围：`db4564c` + `docs/reviews/2026-04-03-batch-a-security-baseline-submission.md`
- 裁决：`PASS`

## 结论

本轮没有发现新的阻塞性 findings。

这次提交完成了两件关键事情：

1. 去掉了 `application.yml` 中默认启用 `dev` profile 的危险行为
2. 将 CORS 默认值从“全开”收紧为“未配置即不放行跨域”，并在 dev profile 中明确声明本地白名单

## 我补做的实际验证

Claude 的 submission 里这轮只有编译验证，没有真实启动验证；我补跑了运行时检查。

### 1. 无 profile 启动

命令：

```bash
java -jar server/nxboot-admin/target/nxboot-admin-1.0.0-SNAPSHOT.jar
```

结果：

- 启动失败
- 失败原因为 `NXBOOT_JWT_SECRET` 未解析
- 行为符合“默认不安全配置不允许启动”的目标

### 2. dev profile 启动

命令：

```bash
java -jar server/nxboot-admin/target/nxboot-admin-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=18080
```

结果：

- 正常启动
- Flyway 正常校验
- 应用成功监听 `18080`

### 3. dev CORS 白名单行为

允许来源：

```bash
curl -i -X OPTIONS 'http://127.0.0.1:18080/api/v1/auth/login' \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: Content-Type,Authorization'
```

结果：

- `200`
- 返回 `Access-Control-Allow-Origin: http://localhost:5173`
- 返回 `Access-Control-Allow-Credentials: true`

未授权来源：

```bash
curl -i -X OPTIONS 'http://127.0.0.1:18080/api/v1/auth/login' \
  -H 'Origin: http://evil.example' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: Content-Type,Authorization'
```

结果：

- `403`
- 返回 `Invalid CORS request`

### 4. prod profile 未配置 CORS 白名单

命令：

```bash
NXBOOT_JWT_SECRET=test-secret-which-is-long-enough-for-hs256-1234567890 \
DB_URL=jdbc:postgresql://localhost:5432/nxboot \
DB_USERNAME=ocean \
DB_PASSWORD='' \
java -jar server/nxboot-admin/target/nxboot-admin-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod --server.port=18081
```

结果：

- 正常启动
- 日志输出：`CORS allowed-origins 未配置，跨域请求将被浏览器同源策略拦截`
- 对跨域 preflight 请求返回 `403 Invalid CORS request`

## 审查说明

这轮和最初 submission 相比，真正成立的地方是：

- “不指定 profile 不能再悄悄以 dev 行为运行”
- “dev 本地白名单可用”
- “prod 未配置白名单时默认拒绝跨域，而不是默认放开”

## 残余风险（非阻塞）

1. 当前实现仍然允许操作者显式把 `NXBOOT_CORS_ORIGINS=*` 配成任意来源，只是会打印 WARN
2. 这对“安全默认值”已经足够，但如果后续目标升级为“强制禁止任何通配跨域”，可以在后续批次中继续收紧
3. submission 本身没有真实运行验证，这次是我补跑后确认通过；后续建议把这类启动级 smoke test 纳入 CI 或脚本化验收

## 结论

Batch A 可以关单，允许进入下一批整改。
