/**
 * 将菜单 path 规范化为绝对路径。
 *
 * 历史菜单数据中 path 有两种格式：
 * - 相对路径：`user`、`role`、`login-log`（V11 种子数据）
 * - 绝对路径：`/system/monitor`、`/system/online`（V21+ 新增）
 *
 * 本函数统一规范为绝对路径，供导航、菜单匹配、默认跳转使用。
 */
export function resolveMenuPath(path: string, parentPath: string): string {
  if (!path) return parentPath;
  if (path.startsWith("/")) return path;
  // 相对路径拼接父级
  const base = parentPath.endsWith("/") ? parentPath.slice(0, -1) : parentPath;
  return `${base}/${path}`;
}
