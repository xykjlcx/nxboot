import { useEffect, useState, useCallback } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Result, Button } from "antd";
import axios from "axios";
import { useAuth } from "@/shared/hooks/useAuth";
import { NxLoading } from "@/shared/components/NxLoading";

/** 路由守卫：未登录跳 /login，已登录但未加载用户信息则 fetchUser + fetchMenus */
export function AuthGuard() {
  const token = useAuth((s) => s.token);
  const user = useAuth((s) => s.user);
  const menusFetched = useAuth((s) => s.menusFetched);
  const menus = useAuth((s) => s.menus);
  const fetchUser = useAuth((s) => s.fetchUser);
  const fetchMenus = useAuth((s) => s.fetchMenus);
  const logout = useAuth((s) => s.logout);

  const [loadError, setLoadError] = useState<string | null>(null);

  const loadAuth = useCallback(() => {
    setLoadError(null);
    Promise.all([fetchUser(), fetchMenus()]).catch((err) => {
      // 页面刷新/导航导致的请求中止 → 忽略（页面即将销毁，不应改变 localStorage）
      if (axios.isCancel(err) || (err instanceof DOMException && err.name === "AbortError")) {
        return;
      }
      // 401 已被 request.ts 拦截器统一处理（forceLogout），不需要再处理
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        return;
      }
      // 其他错误（网络/500/429）→ 显示重试，不 logout（session 可能仍有效）
      const msg = axios.isAxiosError(err)
        ? (err.response?.data as { msg?: string })?.msg ?? err.message
        : "网络异常，请检查连接";
      setLoadError(msg);
    });
  }, [fetchUser, fetchMenus]);

  useEffect(() => {
    if (token && !user) {
      loadAuth();
    }
  }, [token, user, loadAuth]);

  // 未登录
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // 加载失败（非认证错误）
  if (loadError) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <Result
          status="error"
          title="加载失败"
          subTitle={loadError}
          extra={[
            <Button key="retry" type="primary" onClick={loadAuth}>重试</Button>,
            <Button key="logout" onClick={logout}>退出登录</Button>,
          ]}
        />
      </div>
    );
  }

  // 用户信息或菜单还没加载完
  if (!user || !menusFetched) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <NxLoading tip="正在加载..." />
      </div>
    );
  }

  // 菜单已加载但为空——用户没有任何菜单权限
  if (menus.length === 0) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <Result
          status="403"
          title="无菜单权限"
          subTitle="当前账号未分配任何菜单，请联系管理员"
          extra={<Button type="primary" onClick={logout}>退出登录</Button>}
        />
      </div>
    );
  }

  return <Outlet />;
}
