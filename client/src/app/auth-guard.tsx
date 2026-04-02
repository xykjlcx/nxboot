import { useEffect } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Spin } from "antd";
import { useAuth } from "@/shared/hooks/useAuth";

/** 路由守卫：未登录跳 /login，已登录但未加载用户信息则 fetchUser */
export function AuthGuard() {
  const token = useAuth((s) => s.token);
  const user = useAuth((s) => s.user);
  const fetchUser = useAuth((s) => s.fetchUser);
  const logout = useAuth((s) => s.logout);

  useEffect(() => {
    if (token && !user) {
      fetchUser().catch(() => {
        // 获取用户信息失败，跳转登录页
        logout();
      });
    }
  }, [token, user, fetchUser, logout]);

  // 未登录，跳转登录页
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // 已登录但用户信息还没加载完
  if (!user) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  return <Outlet />;
}
