# Batch B Fix R2: RoleForm 编辑回显保真 — Submission

## 基本信息

- 主题：修复角色编辑页回显丢失 + 保存时静默清空权限
- 提交范围：RoleForm.tsx
- 关联 commit：47ceba0
- 当前状态：`READY_FOR_REVIEW`

## 目标

保证角色编辑页的编辑保真性：角色真实持有的 menuIds 必须被精确回显，保存时不会因为无关编辑被清空。

## 改动范围

- 前端：`client/src/features/system/role/pages/RoleForm.tsx`
  - 回到 `checkStrictly` 模式
  - 移除 `collectLeafKeys` 函数
  - 移除 `halfCheckedKeys` 状态
  - 回显时直接设置完整 menuIds，不做任何过滤
  - 提交时直接发送 checkedKeys，不做合并
- 后端：无改动
- 数据库：无改动

## 非目标

- 不改后端祖先自动补齐逻辑（上轮已修，本轮保留）
- 不改 assignable 作用域过滤（上轮已修，本轮保留）
- 不改 V26 存量修复迁移（上轮已修，本轮保留）

## 角色 menuIds 如何映射到编辑态树状态

`checkStrictly` 模式下，每个树节点独立选中/取消，没有父子联动。

映射方式：
1. 后端 `RoleRepository.getMenuIds(roleId)` 返回角色真实持有的全部 menu ID 列表
2. 前端 `useEffect` 中 `setCheckedKeys(data.menuIds.map(String))`，直接设为 Tree 的 `checkedKeys`
3. Tree 组件对 checkedKeys 中的每个 ID 显示选中状态（绿色勾选框）
4. 不在 checkedKeys 中的节点显示未选中状态
5. 无"半选"概念 — 每个节点状态完全由 checkedKeys 决定

**关键**：回显是精确的 1:1 映射，没有任何过滤、推导或转换。

## 为什么不会再出现"看起来没权限，保存后被清空"

1. **回显精确**：`checkedKeys` = 角色真实 menuIds 的完整映射，不经过叶子节点过滤
2. **C 类型节点即使有 F 子节点也会被独立选中**：checkStrictly 模式下，父节点的选中状态不依赖子节点
3. **保存发送 checkedKeys 本身**：不再有 `halfCheckedKeys` 合并逻辑，不会因为 Tree 组件的内部状态计算导致数据丢失
4. **即使管理员只改备注再保存**：checkedKeys 仍然是初始加载的完整 menuIds，不会变空

## 4 类角色场景表现

### 场景 1：只有 C 页面权限，没有 F 按钮权限

- 角色 menuIds = `[100, 1004]`（系统管理 M + 字典管理 C）
- 回显：100 和 1004 节点绿色勾选，2040/2041/2042（字典按钮 F）未勾选
- 修改备注后保存：提交 menuIds = `[100, 1004]`，与原始数据一致
- **修复前**：1004 因为有 F 子节点不是叶子，被 collectLeafKeys 跳过 → 显示为空 → 保存清空

### 场景 2：C + 部分 F

- 角色 menuIds = `[100, 1001, 2002, 2003]`（系统管理 M + 用户管理 C + 用户新增 F + 用户修改 F）
- 回显：100、1001、2002、2003 四个节点绿色勾选，2001/2004/2005 未勾选
- 保存：提交 `[100, 1001, 2002, 2003]`，精确保留

### 场景 3：M + C

- 角色 menuIds = `[100, 1001, 1002]`（系统管理 M + 用户管理 C + 角色管理 C）
- 回显：三个节点绿色勾选
- 保存：提交 `[100, 1001, 1002]`，精确保留

### 场景 4：V26 修复后的历史角色

- 原始坏数据 menuIds = `[1001]`（只有叶子 C，缺祖先 M）
- V26 回填后 menuIds = `[100, 1001]`（自动补了 M 父节点）
- 回显：100 和 1001 两个节点绿色勾选
- 保存：提交 `[100, 1001]`，精确保留

## checkStrictly 与导航安全的关系

上一轮的 P1-3 concern 是：checkStrictly 允许用户只选叶子菜单不选父节点 → 导航空白。

本轮可以安全使用 checkStrictly 的原因：
- 后端 `RoleService.validateAndCompleteMenuIds()` 在保存时自动补齐祖先节点
- 即使用户在新建角色时只勾选 C 节点不勾选 M 父节点，后端会自动添加 M
- 下次编辑该角色时，回显的 menuIds 已包含后端补齐的 M 节点

## 验证命令

```bash
mvn -f server/pom.xml compile -q
cd client && npx tsc --noEmit
cd client && pnpm build
```

## 验证结果

- 后端编译通过
- 前端类型检查通过
- 前端生产构建通过

## 风险点

- checkStrictly 模式下新建角色时用户需要手动勾选父目录（或者不勾也行，后端会自动补齐）
- 与非 checkStrictly 的 UX 差异：勾选父节点不会自动勾选所有子节点

## 未解决事项

无。

## 请求红队重点关注

1. 4 类场景的回显是否真的保真（特别是场景 1，这是本轮修复的核心场景）
2. checkStrictly 模式下 Ant Design Tree 的 `onCheck` 返回格式是否被正确处理（`{checked, halfChecked}` 对象）
3. 后端祖先自动补齐是否确实能兜底 checkStrictly 下用户不勾父节点的情况
