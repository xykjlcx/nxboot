import { Navigate } from "react-router-dom";
import { useAuth } from "@/shared/hooks/useAuth";

/**
 * 根据用户实际菜单权限动态选择落地页。
 * 查找第一个 C 类型（页面）菜单的 path 并跳转。
 * 如果用户没有任何可用菜单，跳转到 403。
 */
export function DefaultRedirect() {
  const { menus } = useAuth();

  // 从菜单树中找到第一个 C 类型菜单
  for (const sub of menus) {
    for (const child of sub.children ?? []) {
      if (child.menuType === "C" && child.path) {
        // 绝对路径直接用，相对路径拼接父级
        const target = child.path.startsWith("/")
          ? child.path
          : `${sub.path}/${child.path}`;
        return <Navigate to={target} replace />;
      }
      // 二级目录下的菜单
      for (const grandchild of child.children ?? []) {
        if (grandchild.menuType === "C" && grandchild.path) {
          const target = grandchild.path.startsWith("/")
            ? grandchild.path
            : `${sub.path}/${child.path}/${grandchild.path}`;
          return <Navigate to={target} replace />;
        }
      }
    }
  }

  // 无可用菜单
  return <Navigate to="/403" replace />;
}

export default DefaultRedirect;
