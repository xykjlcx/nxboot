# Review Submission — Codegen + Dashboard Fix

## 基本信息

- 主题：修复 Monitor 错误态 + 历史菜单相对路径导致路由错误
- 提交范围：`b23c6a0..41b5da6`（1 个 commit）
- 关联 commit：`41b5da6`
- 当前状态：`READY_FOR_REVIEW`

## 目标

1. Monitor 页面区分 loading / error / no-data，不再把接口失败伪装成"加载中"
2. 统一菜单 path 解析逻辑，兼容历史相对路径和新绝对路径，修复旧页面跳错路由的问题

## 用户场景

1. **Monitor 接口 403/500/断网**：看到 Result error 页 + "加载失败" + 重试按钮，而非永久 Spin
2. **点击旧菜单（用户/角色/菜单/字典/配置/日志/文件/任务/登录日志）**：跳转到 `/system/xxx`，不再跳到 `/xxx` 触发错误边界
3. **点击新菜单（在线用户/任务日志/监控）**：继续正常
4. **根路径 `/`**：DefaultRedirect 正确跳转到第一个可用菜单（admin → `/system/user`）
5. **侧边栏高亮**：`selectedKeys` 与 `location.pathname` 一致，当前页面菜单正确高亮

## 假设

- 所有 sys_menu C 类型菜单的 path 要么是绝对路径（以 `/` 开头），要么是相对路径（不含 `/`）
- 父级子系统的 path 是绝对路径（如 `/system`），这在 V11 种子数据中保证
- `resolveMenuPath` 不需要处理 `../` 或多级相对路径——菜单 path 都是单级名称

## 改动范围

- 新增：`client/src/shared/utils/menu.ts` — `resolveMenuPath(path, parentPath)` 共享 helper
- 修改：`client/src/app/layouts/BasicLayout.tsx` —
  - `menusToSubsystems` 所有 menu key 通过 `resolveMenuPath` 规范化
  - `activeSubsystem` 匹配从 `pathname.split("/")[1]` 改为 `pathname.startsWith(s.key)`
- 修改：`client/src/app/DefaultRedirect.tsx` — 复用 `resolveMenuPath` 替代内联拼接
- 修改：`client/src/features/system/monitor/pages/Monitor.tsx` — 区分 loading / error / no-data

## 非目标

- 不修改数据库中的历史菜单 path（前端兼容处理）
- 不新增 backlog 功能

## 风险点

- `resolveMenuPath` 对空 path 返回 parentPath——如果某个 C 菜单 path 为空，会导航到子系统根路径而非具体页面。实际数据中所有 C 菜单都有 path，不会触发。
- `activeSubsystem` 用 `startsWith` 匹配——如果存在 `/sys` 和 `/system` 两个子系统，可能误匹配。当前只有 `/system` 一个子系统，不会触发。

## 验证命令

```bash
mvn -f server/pom.xml compile -q
cd client && npx tsc --noEmit
cd client && pnpm build

# 菜单 path 规范化验证
curl -s /api/v1/auth/menus -H "Authorization: Bearer <token>" | python3 验证脚本
```

## 验证结果

- `mvn compile`：零错误
- `npx tsc --noEmit`：零错误
- `pnpm build`：成功（2.89s）
- 菜单 path 规范化：13 个菜单全部 resolved 为 `/system/xxx`

```
用户管理      raw=user                  resolved=/system/user         ✓
角色管理      raw=role                  resolved=/system/role         ✓
部门管理      raw=/system/dept          resolved=/system/dept         ✓
菜单管理      raw=menu                  resolved=/system/menu         ✓
字典管理      raw=dict                  resolved=/system/dict         ✓
参数配置      raw=config                resolved=/system/config       ✓
操作日志      raw=log                   resolved=/system/log          ✓
文件管理      raw=file                  resolved=/system/file         ✓
任务管理      raw=job                   resolved=/system/job          ✓
在线用户      raw=/system/online        resolved=/system/online       ✓
任务日志      raw=/system/job-log       resolved=/system/job-log      ✓
登录日志      raw=login-log             resolved=/system/login-log    ✓
服务器监控     raw=/system/monitor       resolved=/system/monitor      ✓
```

## 演示路径 / 接口验证

以下页面都应正常打开（旧页面 + 新页面）：

| 路径 | 菜单 path 类型 | 预期 |
|------|---------------|------|
| /system/user | 相对 `user` | 正常 |
| /system/role | 相对 `role` | 正常 |
| /system/menu | 相对 `menu` | 正常 |
| /system/dict | 相对 `dict` | 正常 |
| /system/config | 相对 `config` | 正常 |
| /system/log | 相对 `log` | 正常 |
| /system/file | 相对 `file` | 正常 |
| /system/job | 相对 `job` | 正常 |
| /system/login-log | 相对 `login-log` | 正常 |
| /system/online | 绝对 | 正常 |
| /system/job-log | 绝对 | 正常 |
| /system/monitor | 绝对 | 正常 |
| / | DefaultRedirect | 跳转 /system/user |

Monitor 错误态验证：断开后端后访问 /system/monitor → 显示"加载失败" + 重试按钮

## 数据与迁移说明

- 无数据库变更
- 无 Flyway 迁移
- 纯前端修复

## 体验说明

- Monitor loading：Spin "加载中..."
- Monitor error：Result status="error" + 错误信息 + "重试"按钮
- Monitor no data：Result status="warning" "暂无数据"
- 菜单点击：navigate() 目标现在是绝对路径，正确进入 /system/xxx
- selectedKeys：与 location.pathname 一致，侧边栏高亮正确
- 面包屑：currentMenu.key 是绝对路径，startsWith 匹配正确

## 未解决事项

- 数据库中历史菜单的相对 path 仍然存在（前端已兼容，不需要改数据库）

## 请求红队重点关注

1. `resolveMenuPath` 是否覆盖所有边界情况（空 path、已绝对、带尾斜杠的 parentPath）
2. `activeSubsystem` 的 `startsWith` 匹配在多子系统场景下是否安全
3. Monitor 的 error 展示是否足够——是否需要展示 HTTP status code
