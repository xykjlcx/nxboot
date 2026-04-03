import { useEffect } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Result, Spin } from "antd";
import { useAuth } from "@/shared/hooks/useAuth";

/** 路由守卫：未登录跳 /login，已登录但未加载用户信息则 fetchUser + fetchMenus */
export function AuthGuard() {
  const token = useAuth((s) => s.token);
  const user = useAuth((s) => s.user);
  const menusFetched = useAuth((s) => s.menusFetched);
  const menus = useAuth((s) => s.menus);
  const fetchUser = useAuth((s) => s.fetchUser);
  const fetchMenus = useAuth((s) => s.fetchMenus);
  const logout = useAuth((s) => s.logout);

  useEffect(() => {
    if (token && !user) {
      Promise.all([fetchUser(), fetchMenus()]).catch(() => {
        logout();
      });
    }
  }, [token, user, fetchUser, fetchMenus, logout]);

  // 未登录
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // 用户信息或菜单还没加载完
  if (!user || !menusFetched) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <Spin size="large" tip="加载中..."><div /></Spin>
      </div>
    );
  }

  // 菜单已加载但为空——用户没有任何菜单权限
  if (menus.length === 0) {
    return (
      <Result
        status="403"
        title="无菜单权限"
        subTitle="当前账号未分配任何菜单，请联系管理员"
      />
    );
  }

  return <Outlet />;
}
