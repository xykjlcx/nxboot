# NxBoot 全项目审计报告（Codex）

- 审计日期：2026-04-03
- 审计角色：代码评审 + 产品评审 + 最终发布关卡
- 审计范围：整个仓库，不限于最近提交；覆盖后端、前端、迁移脚本、权限模型、配置、安全默认值、CI、AI 交接文档
- 当前裁决：`NO-GO`

---

## 1. 总结结论

这轮不是“代码能不能编译”的问题，而是“这套脚手架现在能不能作为高标准产品底座交付”的问题。

我给出的结论是：**现在还不能直接视为可发布的稳定底座**。原因不是单点 bug，而是三类基础问题仍未收口：

1. **运行时默认配置不安全**
2. **RBAC 权限模型存在结构性缺口**
3. **自动化质量门禁仍明显不足**

当前编译/构建状态：

- 后端：`mvn -f server/pom.xml compile -q` 通过
- 前端：`cd client && npx tsc --noEmit` 通过
- 前端构建：`cd client && pnpm build` 通过

但这些结果只能证明“项目当前可编译”，**不能证明权限、迁移、认证、菜单链路、文件能力和多轮迭代后的回归安全**。

---

## 2. 分级标准

- `P0`：严重错误，禁止继续发布或合并，必须立即修复
- `P1`：高风险问题，当前版本不应作为稳定底座交付
- `P2`：明显质量缺陷或产品能力缺口，建议在稳定化阶段优先处理
- `P3`：非阻塞但明确存在的技术债或交付风险，应登记并安排后续收口

---

## 3. 审计方法

本轮审计采用以下方式：

1. 通读仓库文档与结构：`README.md`、`CLAUDE.md`、`AGENTS.md`、`CODEGEN.md`、`REVIEW_PROTOCOL.md`
2. 复跑基础门禁：
   - `mvn -f server/pom.xml compile -q`
   - `cd client && npx tsc --noEmit`
   - `cd client && pnpm build`
3. 抽查核心风险面：
   - 认证与刷新令牌
   - 权限模型与菜单树
   - 文件上传/访问链路
   - CORS / JWT / profile 默认值
   - DataScope / jOOQ 工具层
   - CI 与测试覆盖
   - AI 操作文档是否与现状一致

限制说明：

- 本轮未做完整浏览器级 E2E 回归
- 本轮未做真实多实例部署验证
- 本轮未做生产环境压测或数据库迁移回滚演练

所以本报告更偏向：**架构、产品、权限、安全、工程化的发布级审查**。

---

## 4. Findings 总表

| 等级 | 标题 | 结论 |
|---|---|---|
| P1 | 运行时默认配置仍然是“开发模式优先” | 当前默认 profile 和 JWT secret 策略不适合稳定交付 |
| P1 | 权限目录（permission catalog）不完整，非 admin 写操作体系基本不可配置 | 多个模块声明了 create/update/delete/upload 权限，但种子数据没有完整定义 |
| P1 | 角色菜单分配允许出现“叶子菜单无父节点”，最终会产出空导航 | 角色编辑 UI 和菜单树构建规则不一致 |
| P2 | CORS 默认对任意来源放开，并同时允许 credentials | 作为脚手架默认值过于宽松 |
| P2 | 自动化质量门禁过弱，仓库实际上没有测试网 | 现在的 CI 只能防语法级错误，防不了权限/迁移/认证回归 |
| P2 | 文件管理能力链路不闭环 | 存储抽象暴露了访问 URL，但本地实现指向不存在的下载接口 |
| P2 | AI 交接文档已经和真实代码明显漂移 | 继续依赖这些文档驱动 Claude/Codex 会持续引入错误修改 |
| P3 | 在线用户、黑名单、限流都默认单机内存实现 | 如果目标是多实例部署，这些能力尚未达到可发布标准 |
| P3 | 前端产物仍有超大 chunk 警告 | 现在不是功能错误，但会影响长期首屏与缓存策略 |
| P3 | 仓库当前工作树不干净 | 存在 `tsbuildinfo` 变更和未纳入版本控制的 review 文件 |

---

## 5. 详细问题

### P1-1. 运行时默认配置仍然是“开发模式优先”

**结论**

当前项目默认会以 `dev` profile 启动，而且基础配置里仍然保留了 JWT secret 的兜底值。这对脚手架来说是危险默认值，不适合进入“稳定底座”状态。

**证据**

- `server/nxboot-admin/src/main/resources/application.yml:4-5`
  - `spring.profiles.active: dev`
- `server/nxboot-admin/src/main/resources/application.yml:35-39`
  - JWT secret 使用了可回退的默认值
- `server/nxboot-admin/src/main/resources/application-dev.yml:13-15`
  - dev secret 被硬编码进仓库

**为什么这是发布阻塞项**

1. 运维如果直接按 README 启动 jar，而没有显式指定 profile，系统会默认进入开发环境行为
2. JWT secret 的 fallback 让“忘记配置密钥”不会立刻失败，而是悄悄进入弱配置运行
3. 对脚手架项目来说，**默认值本身就是产品的一部分**，不能要求所有使用者都靠经验避坑

**Claude 应该怎么修**

1. 去掉 `application.yml` 里的 `spring.profiles.active: dev`
2. 将 JWT secret 改成：
   - dev 环境可以提供开发默认值
   - 非 dev 环境缺失时直接启动失败
3. 更新 README / CLAUDE.md 的启动说明，明确 profile 和 secret 的约束

**如何验证**

- 验证 1：不传 profile 启动时，不应再默认进入 dev
- 验证 2：非 dev 环境下如果未配置 `NXBOOT_JWT_SECRET`，应用必须 fail-fast
- 验证 3：dev 环境仍可一键启动
- 命令建议：
  - `java -jar nxboot-admin.jar`
  - `java -jar nxboot-admin.jar --spring.profiles.active=prod`
  - 分别观察 profile 与启动失败/成功行为

---

### P1-2. 权限目录不完整，非 admin 写操作体系基本不可配置

**结论**

项目已经在前后端声明了大量 `create / update / delete / upload` 级权限，但种子权限数据没有完整落地。结果是：**除 admin 外，大多数模块的写操作权限实际上无法通过角色系统正常配置出来。**

**证据**

- 权限实际来源：
  - `server/nxboot-system/src/main/java/com/nxboot/system/auth/service/UserDetailsServiceImpl.java:90-126`
  - 用户权限来自 `sys_role_menu -> sys_menu.permission`
- 现有种子按钮权限：
  - `server/nxboot-admin/src/main/resources/db/migration/V11__init_data.sql:27-39`
  - 这里只定义了用户管理按钮权限（`system:user:*`）
- 但后端多个模块已经要求写权限：
  - `server/nxboot-system/src/main/java/com/nxboot/system/role/controller/RoleController.java:50-68`
  - `server/nxboot-system/src/main/java/com/nxboot/system/menu/controller/MenuController.java:51-69`
  - `server/nxboot-system/src/main/java/com/nxboot/system/config/controller/ConfigController.java:48-66`
  - `server/nxboot-system/src/main/java/com/nxboot/system/job/controller/JobController.java:42-70`
  - `server/nxboot-system/src/main/java/com/nxboot/system/file/controller/FileController.java:29-31`
- 前端也在按这些权限隐藏/显示写按钮：
  - 例如 `client/src/features/system/role/pages/RoleList.tsx`
  - `client/src/features/system/menu/pages/MenuList.tsx`
  - `client/src/features/system/config/pages/ConfigList.tsx`
  - `client/src/features/system/job/pages/JobList.tsx`
  - `client/src/features/system/file/pages/FileList.tsx:79`
- 文件上传甚至还存在命名不一致：
  - 前端判断 `system:file:create`
  - 后端要求 `system:file:upload`
  - 种子数据里只存在 `system:file:list`

**为什么这是发布阻塞项**

这不是“权限细节没补齐”，而是 **RBAC 产品模型本身没有闭环**：

1. 非 admin 角色无法被精细授权到多数写操作
2. 前端按钮、后端接口、数据库菜单权限三者没有统一 catalog
3. 这会直接导致：
   - 按钮不显示
   - 接口永远 403
   - 角色配置页面看起来能配，实际无法生效

**Claude 应该怎么修**

1. 先建立一份完整权限目录（至少覆盖现有已实现模块）
2. 为每个模块补齐对应的 F 类型按钮权限种子数据
3. 统一前端、后端、迁移脚本里的 permission name
4. 对 `system:file:create` / `system:file:upload` 这种不一致做一次全链路收敛
5. 最好加一个“权限目录自检”，避免以后前后端再漂移

**如何验证**

- 验证 1：新建一个非 admin 角色，只授予某个模块的写权限，按钮应可见且接口不 403
- 验证 2：去掉写权限后，按钮隐藏且接口拒绝
- 验证 3：至少覆盖以下模块：
  - 角色管理
  - 菜单管理
  - 字典管理
  - 参数配置
  - 文件管理
  - 定时任务
- 建议做一个权限矩阵表作为验收物

---

### P1-3. 角色菜单分配允许出现“叶子菜单无父节点”，最终会产出空导航

**结论**

角色编辑页允许独立勾选叶子节点菜单，但后端构建用户菜单树时又依赖父节点存在。两边规则不一致，最终会产生“角色保存成功，但用户看不到任何导航”的结果。

**证据**

- 角色编辑 UI 允许独立选择节点：
  - `client/src/features/system/role/pages/RoleForm.tsx:107-118`
  - `Tree` 使用了 `checkStrictly`
- 后端保存的是精确勾选结果，不会自动补父节点：
  - `server/nxboot-system/src/main/java/com/nxboot/system/role/repository/RoleRepository.java:140-152`
- 非 admin 菜单树构建只取已分配菜单，再按 `parentId = 0` 构树：
  - `server/nxboot-system/src/main/java/com/nxboot/system/menu/repository/MenuRepository.java:80-101`

**为什么这是发布阻塞项**

这会让权限系统出现非常难排查的“伪成功”：

1. 管理员在角色树里勾选了页面菜单
2. 数据库存下来了
3. 用户权限字符串也可能拿到了
4. 但前端导航树因为缺父节点直接丢失

也就是说，**权限配置页不能保证配置结果可访问**。这已经是核心产品逻辑错误。

**Claude 应该怎么修**

二选一，但必须统一：

1. 保存角色菜单时自动补齐所有祖先目录
2. 或者前端取消 `checkStrictly`，强制树形级联选择

更稳妥的方式是：

- 后端保存时做 ancestor closure
- 前端展示时也保持父子联动，避免管理员误操作

**如何验证**

- 验证 1：只给角色勾选一个 C 页面菜单，不手动勾父节点
- 验证 2：登录该角色用户后，导航树仍然能正确显示并可访问该页面
- 验证 3：菜单接口 `GET /api/v1/auth/menus` 返回结果必须包含必需的祖先目录

---

### P2-1. CORS 默认对任意来源放开，并同时允许 credentials

**结论**

作为脚手架默认值，当前 CORS 配置过宽。

**证据**

- `server/nxboot-framework/src/main/java/com/nxboot/framework/web/CorsConfig.java:16-25`
  - 默认 `allowed-origins` 为 `*`
  - `setAllowCredentials(true)`
  - `addAllowedOriginPattern(...)`

**为什么这是高优先级问题**

即使当前主要使用 Bearer Token，这种配置也不是一个可接受的安全基线：

1. 它把跨域策略交成了“默认全开”
2. 一旦未来接入 cookie、第三方嵌入、浏览器凭证或某些新接口，风险会被放大
3. 脚手架默认值应该代表推荐实践，而不是“先全放开再说”

**Claude 应该怎么修**

1. 默认改成显式 allowlist
2. dev 环境允许本地前端来源，例如 `http://localhost:5173`
3. prod 环境缺少 allowlist 时应拒绝启动或至少打印高危告警

**如何验证**

- 用允许来源做 preflight，请求应通过
- 用未允许来源做 preflight，请求应被拒绝
- 检查响应头中 `Access-Control-Allow-Origin` 不再对任意来源开放

---

### P2-2. 自动化质量门禁过弱，仓库实际上没有测试网

**结论**

现在的 CI 只能防“编译不过”，防不了“功能回归、权限回归、迁移回归、认证回归”。

**证据**

- `/.github/workflows/ci.yml:34-58`
  - 后端只做 `mvn compile -q`
  - 前端只做 `tsc` + `build`
- 仓库检索未发现测试目录或测试文件
- `client/package.json:7-12`
  - `lint` 仍然是会写回文件的命令，不适合作为 CI gate

**为什么这是高优先级问题**

这个项目已经明显进入“复杂系统”阶段：

- 权限链路
- 菜单树
- Flyway 迁移
- JWT 刷新
- OAuth2 自动注册
- DataScope

这些能力靠人工 review 发现问题的成本太高，而且容易漏。

**Claude 应该怎么修**

1. 后端至少补三类自动化验证：
   - 认证 / 刷新 token
   - 菜单 / 角色 / 权限链路
   - 关键迁移 smoke test
2. 前端至少补一类：
   - 路由 / 权限守卫 / 默认跳转的集成测试
3. 把 `lint` 拆成：
   - `lint`（只检查，不写文件）
   - `lint:fix`（本地修复）
4. CI 必须跑非写入式门禁

**如何验证**

- 故意引入一个权限名不一致或菜单树断裂问题，CI 应该失败
- `pnpm lint` 不应写回文件
- 新 PR 不应只靠 compile/build 通过

---

### P2-3. 文件管理能力链路不闭环

**结论**

当前文件管理更像“上传记录表”，还不是完整产品能力。

**证据**

- 存储抽象暴露了访问 URL：
  - `server/nxboot-framework/src/main/java/com/nxboot/framework/storage/FileStorage.java`
- 本地实现返回下载地址：
  - `server/nxboot-framework/src/main/java/com/nxboot/framework/storage/LocalFileStorage.java:47-50`
  - URL 为 `/api/v1/system/files/download/{path}`
- 但 Controller 实际没有下载接口：
  - `server/nxboot-system/src/main/java/com/nxboot/system/file/controller/FileController.java:16-65`
- 前端页面也只有上传 / 列表 / 删除：
  - `client/src/features/system/file/pages/FileList.tsx:28-99`

**为什么这是高优先级问题**

从产品角度看，文件管理至少应该回答一个问题：

> 上传后的文件，用户如何再次访问？

现在答案是不清晰的：

- 架构层说可以通过 URL 访问
- 本地存储实现给了 URL
- 但 API 不存在
- 页面也没有下载/预览动作

这会让后续接入 OSS / S3 时边界继续变乱。

**Claude 应该怎么修**

二选一：

1. 如果当前版本要支持文件访问：
   - 实现下载/预览接口
   - FileList 增加操作按钮
   - 做权限控制和路径安全校验
2. 如果当前版本只支持“上传 + 记录”：
   - 删除或暂时收缩 `getUrl()` 这层承诺
   - 在 README / CLAUDE.md 中明确当前范围

**如何验证**

- 上传一个文件
- 在列表页可以下载或预览
- 未授权用户不能访问其他人的文件
- 路径穿越等非法路径被拒绝

---

### P2-4. AI 交接文档已经和真实代码明显漂移

**结论**

这个项目已经明确采用 AI 协作开发，但关键文档已经不再可信。对普通项目这是文档债；对 AI 驱动项目，这是直接的交付风险。

**证据**

- `AGENTS.md:27-32`
  - 仍写着后端数据访问是 `DSL.field()` / `DSL.table()`
- `CLAUDE.md:131-141`
  - 仍写着 `JooqHelper.notDeleted()` 等旧 API
- `server/CODEGEN.md:52-64`
  - 仍写着“只有 ConfigRepository 已迁移，其他 Repository + JooqHelper 待迁移”

**为什么这是高优先级问题**

后续 Claude / Codex / Cursor 会把这些文档当操作手册。如果手册是错的，AI 就会稳定地产生错误改动，甚至反向破坏刚完成的重构。

**Claude 应该怎么修**

1. 逐份校正文档，保证与当前代码一致
2. 特别更新：
   - JooqHelper 现状
   - codegen 迁移状态
   - 权限与菜单的真实约束
   - 运行与验证命令
3. 把“文档同步”视为每轮稳定化的验收项，而不是附带工作

**如何验证**

- 新开一个没有上下文的 AI 会话，只给文档，不给口头补充
- 看它是否能基于文档正确描述当前架构和操作方式
- 至少要求 `CLAUDE.md`、`AGENTS.md`、`CODEGEN.md` 三者相互一致

---

## 6. 非阻塞但需要登记的战略债（P3）

### P3-1. 单机内存实现仍然是默认前提

相关位置：

- `server/nxboot-framework/src/main/java/com/nxboot/framework/security/MemoryTokenBlacklist.java`
- `server/nxboot-system/src/main/java/com/nxboot/system/auth/service/OnlineUserService.java`
- `server/nxboot-framework/src/main/java/com/nxboot/framework/web/RateLimitInterceptor.java`

结论：

- Token 黑名单、在线用户、IP 限流都默认基于单机内存
- 如果目标是单实例后台，这可以接受
- 如果目标是“生产级脚手架”，则需要明确多实例路线（Redis / shared state）

建议：

- 文档里明确“当前默认是单实例架构”
- 不要把这部分包装成已经可水平扩展

---

### P3-2. 前端仍有超大 chunk 警告

证据来自本轮 `pnpm build` 输出：

- 仍有 `Some chunks are larger than 500 kB after minification`
- 主 bundle 体积偏大

这不是阻塞项，但说明：

- 路由级拆分还不够
- Ant Design / icons / 通用依赖的分包策略还可继续优化

---

### P3-3. 当前仓库工作树不干净

本轮审计时的状态：

- `client/tsconfig.tsbuildinfo` 已修改
- 若干 review 文件未纳入版本控制

这不是产品 bug，但会影响后续审查边界和提交洁净度。

---

## 7. Claude 的修复顺序建议

### 第一批：发布阻塞项（必须先做）

1. `P1-1` 运行时默认配置安全化
2. `P1-2` 权限目录补齐与统一
3. `P1-3` 菜单树父子闭环

只有这三项关闭，项目才有资格进入“稳定底座”的下一阶段。

### 第二批：稳定化优先项

4. `P2-1` CORS 默认值收紧
5. `P2-2` 自动化门禁补齐
6. `P2-3` 文件管理能力闭环
7. `P2-4` 文档同步

### 第三批：战略债收口

8. 单机内存能力的多实例路线
9. 前端 chunk 优化
10. 仓库 hygiene

---

## 8. 建议的验收方式

Claude 不要把这些问题混成一次“大重构”。建议按批次交卷：

### Batch A：安全与运行时基线

- 修 `P1-1` + `P2-1`
- 交付物：
  - 配置改动
  - README / 运维说明
  - 启动行为验证结果

### Batch B：权限模型闭环

- 修 `P1-2` + `P1-3`
- 交付物：
  - 权限目录表
  - Flyway 迁移
  - 非 admin 角色验证截图 / 步骤

### Batch C：质量门禁

- 修 `P2-2`
- 交付物：
  - 新增测试
  - CI 配置调整
  - 故障注入验证

### Batch D：产品闭环与文档同步

- 修 `P2-3` + `P2-4`
- 交付物：
  - 文件下载/访问链路
  - 更新后的 `CLAUDE.md` / `AGENTS.md` / `CODEGEN.md`

---

## 9. 最终判断

如果从“演示能跑”角度看，NxBoot 现在已经有明显进展。  
如果从“你要把它当作长期演化的后台脚手架基座”角度看，**现在还差最后一轮真正的稳定化收口**。

我的最终判断是：

- **现在不建议继续开发新功能**
- **先把 P1 全部关闭**
- **再把 P2 里与质量门禁、文件能力、AI 文档相关的部分补齐**
- **完成后再做一次全仓复审**

在此之前，这个项目更像“高完成度的候选底座”，还不能算“已经达标的最终底座”。
