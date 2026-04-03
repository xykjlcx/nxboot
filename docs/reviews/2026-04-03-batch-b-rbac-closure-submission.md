# Batch B: 权限模型闭环 — Submission

## 基本信息

- 主题：P1-2 权限目录补齐 + P1-3 菜单父子闭环 + 分级授权
- 提交范围：Flyway 迁移 + 后端 Service/Repository/Controller + 前端 RoleForm + FileList
- 关联 commit：ea8b99d（B-1 权限目录）+ 8545e02（B-2 父子闭环）
- 当前状态：`READY_FOR_REVIEW`

## 目标

1. 让非 admin 角色的写操作权限可以通过角色系统正常配置
2. 保存角色菜单时自动补齐祖先节点，杜绝"配置成功但导航空白"
3. 实现分级授权：每级管理员只能分配自己拥有的菜单

## 用户场景

1. **超级管理员**给管理员 A 分配 80 个菜单 → A 登录看到 80 个菜单
2. **管理员 A** 给下属 B 分配 30 个菜单 → B 登录看到 30 个菜单
3. A 在角色编辑页只能看到自己拥有的 80 个菜单，无法看到或分配其余 20 个
4. 只勾选一个 C 类型页面菜单（不手动勾父节点）→ 后端自动补齐祖先 M 节点 → 用户导航正常

## 假设

- admin 角色（role_key='admin'）拥有 `*:*:*` 权限，绕过作用域限制，可分配全量菜单
- 非 admin 用户的可分配菜单 = 其角色关联的 role_menu 记录集合
- Ant Design Tree 非 checkStrictly 模式下，onCheck 的 `info.halfCheckedKeys` 包含半选父节点
- 后端祖先补齐是幂等的：即使前端已经传了父节点，补齐逻辑不会重复添加

## 改动范围

- 后端：
  - `MenuRepository`：新增 `buildAssignableMenuTree()`、`getAssignableMenuIds()`、`getParentIdMap()` 三个方法
  - `MenuService`：新增 `assignableTree()` 方法
  - `MenuController`：新增 `GET /menus/assignable` 接口（权限：`system:role:list`）
  - `RoleService`：新增 `validateAndCompleteMenuIds()` 私有方法，create/update 调用
- 前端：
  - `RoleForm.tsx`：去掉 `checkStrictly`，API 改用 `/menus/assignable`，提交时合并 checkedKeys + halfCheckedKeys
  - `FileList.tsx`：`system:file:create` → `system:file:upload`
- 数据库 / Flyway：
  - `V25__add_button_permissions.sql`：18 个 F 类型按钮权限 + admin 绑定

## 非目标

- 不改 admin 角色的特殊处理逻辑（admin 仍然绕过所有权限和作用域检查）
- 不改 `buildUserMenuTree`（导航菜单树仍排除 F 类型）
- 不做前端菜单管理页的作用域限制（菜单管理是结构管理，不是权限分配）

## 风险点

- V25 迁移使用 `ON CONFLICT (id) DO NOTHING`，已有环境不会重复插入，但如果用户手动分配了相同 ID 会被跳过
- 祖先补齐依赖 `getParentIdMap()` 一次查全表，大菜单量时有性能开销（当前数量级可忽略）
- 前端 Tree 的 halfCheckedKeys 行为依赖 Ant Design 实现，版本升级需关注

## 验证命令

```bash
# 后端编译
mvn -f server/pom.xml compile -q

# 前端类型检查
cd client && npx tsc --noEmit
```

## 验证结果

- 后端编译通过
- 前端类型检查通过

## 演示路径 / 接口验证

### 验证 1：权限目录完整性
- 启动 dev 环境 → V25 迁移自动执行
- 查看菜单管理页 → 每个 C 类型菜单下应该有对应的 F 类型按钮子节点
- 新建角色 → 菜单树应显示所有按钮权限可勾选

### 验证 2：父子闭环
- 编辑角色 → 只勾选"用户管理"（C 类型，id=1001）
- 保存后查看 role_menu 表 → 应包含 id=100（系统管理 M 父节点）
- 用该角色登录 → 导航树正常显示"系统管理 > 用户管理"

### 验证 3：分级授权
- admin 创建角色 A，分配 80 个菜单
- 用角色 A 的用户登录 → 调用 `GET /api/v1/system/menus/assignable` → 只返回 80 个
- 角色 A 用户编辑下级角色 → 菜单树只显示自己有的 80 个
- 角色 A 用户尝试分配自己没有的菜单 → 后端返回 403

### 验证 4：权限命名一致性
- 前端 FileList 上传按钮检查 `system:file:upload`（与后端一致）
- 为非 admin 角色分配 `system:file:upload` 按钮权限 → 上传按钮可见且接口不 403

## 权限目录表

| 模块 | list (C) | create (F) | update (F) | delete (F) | 其他 (F) |
|------|----------|------------|------------|------------|----------|
| 用户 | system:user:list | system:user:create | system:user:update | system:user:delete | system:user:resetPwd, system:user:query |
| 角色 | system:role:list | system:role:create | system:role:update | system:role:delete | - |
| 菜单 | system:menu:list | system:menu:create | system:menu:update | system:menu:delete | - |
| 字典 | system:dict:list | system:dict:create | system:dict:update | system:dict:delete | - |
| 配置 | system:config:list | system:config:create | system:config:update | system:config:delete | - |
| 日志 | system:log:list | - | - | - | - |
| 文件 | system:file:list | system:file:upload | - | system:file:delete | - |
| 任务 | system:job:list | system:job:create | system:job:update | system:job:delete | - |
| 部门 | system:dept:list | system:dept:create | system:dept:update | system:dept:delete | - |
| 在线用户 | system:online:list | - | - | - | system:online:forceLogout |
| 登录日志 | system:loginLog:list | - | - | - | - |
| 任务日志 | system:jobLog:list | - | - | - | - |
| 监控 | system:monitor:list | - | - | - | - |

## 数据与迁移说明

- 新增 V25 迁移：18 个 F 类型菜单 + 18 条 admin role_menu 绑定
- 全部使用 ON CONFLICT DO NOTHING，幂等安全
- 不影响已有数据（新增记录，不修改已有行）
- 无 codegen 变更
- 回滚风险：低（仅新增数据，DELETE FROM sys_menu WHERE id IN (2020..2162) 即可回滚）

## 体验说明

- 角色编辑页菜单树改为父子联动（不再需要手动勾选父节点）
- 非 admin 用户在角色编辑页只能看到自己拥有的菜单
- 上传按钮权限名修正为 `system:file:upload`（与后端一致）

## 未解决事项

- `system:online:query`（F-2010）为 V21 中引入的孤立权限，后端未使用，本轮未清理（非阻塞）
- 部分后端权限未在前端做 `has()` 检查（如 `system:user:resetPwd`、`system:log:list`、`system:monitor:list`），本轮不扩展前端改动

## 请求红队重点关注

1. 祖先补齐逻辑是否会补入不该补的节点（如已被禁用的父节点）
2. Ant Design Tree 的 halfCheckedKeys 是否稳定包含所有半选祖先
3. `/menus/assignable` 接口的权限设为 `system:role:list` 是否合适（而不是单独定义）
4. V25 的 ID 分配（2020-2162）是否与现有或未来 ID 冲突
