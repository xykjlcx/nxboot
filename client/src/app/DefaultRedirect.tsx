import { Navigate } from "react-router-dom";
import { Result, Button } from "antd";
import { useAuth } from "@/shared/hooks/useAuth";
import { resolveMenuPath } from "@/shared/utils/menu";

/**
 * 根据用户实际菜单权限动态选择落地页。
 * 查找第一个 C 类型（页面）菜单的 path 并跳转。
 * 如果用户没有任何可用菜单，直接渲染 403 权限提示。
 */
export function DefaultRedirect() {
  const { menus, logout } = useAuth();

  // 从菜单树中找到第一个 C 类型菜单
  for (const sub of menus) {
    const subPath = sub.path || `/${sub.id}`;
    for (const child of sub.children ?? []) {
      if (child.menuType === "C" && child.path) {
        return <Navigate to={resolveMenuPath(child.path, subPath)} replace />;
      }
      for (const grandchild of child.children ?? []) {
        if (grandchild.menuType === "C" && grandchild.path) {
          return <Navigate to={resolveMenuPath(grandchild.path, subPath)} replace />;
        }
      }
    }
  }

  // 无可用菜单 — 直接渲染 403，不跳转到不存在的路由
  return (
    <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
      <Result
        status="403"
        title="暂无可访问页面"
        subTitle="当前账号未分配任何菜单权限，请联系管理员。"
        extra={<Button type="primary" onClick={logout}>退出登录</Button>}
      />
    </div>
  );
}

export default DefaultRedirect;
