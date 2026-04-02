import { createBrowserRouter, Navigate } from "react-router-dom";
import { lazy, Suspense } from "react";
import { Spin } from "antd";
import { AuthGuard } from "./auth-guard";
import { BasicLayout } from "./layouts/BasicLayout";
import { BlankLayout } from "./layouts/BlankLayout";

// 懒加载页面
const Login = lazy(() => import("@/features/system/auth/pages/Login"));
const UserList = lazy(() => import("@/features/system/user/pages/UserList"));
const RoleList = lazy(() => import("@/features/system/role/pages/RoleList"));
const MenuList = lazy(() => import("@/features/system/menu/pages/MenuList"));
const DictList = lazy(() => import("@/features/system/dict/pages/DictList"));
const ConfigList = lazy(() => import("@/features/system/config/pages/ConfigList"));
const LogList = lazy(() => import("@/features/system/log/pages/LogList"));
const FileList = lazy(() => import("@/features/system/file/pages/FileList"));
const JobList = lazy(() => import("@/features/system/job/pages/JobList"));

/** 懒加载包装组件 */
function LazyLoad({ children }: { children: React.ReactNode }) {
  return (
    <Suspense
      fallback={
        <div style={{ display: "flex", justifyContent: "center", padding: 48 }}>
          <Spin />
        </div>
      }
    >
      {children}
    </Suspense>
  );
}

export const router = createBrowserRouter([
  {
    path: "/login",
    element: <BlankLayout />,
    children: [
      {
        index: true,
        element: (
          <LazyLoad>
            <Login />
          </LazyLoad>
        ),
      },
    ],
  },
  {
    path: "/",
    element: <AuthGuard />,
    children: [
      {
        element: <BasicLayout />,
        children: [
          { index: true, element: <Navigate to="/system/user" replace /> },
          {
            path: "system",
            children: [
              { index: true, element: <Navigate to="/system/user" replace /> },
              { path: "user", element: <LazyLoad><UserList /></LazyLoad> },
              { path: "role", element: <LazyLoad><RoleList /></LazyLoad> },
              { path: "menu", element: <LazyLoad><MenuList /></LazyLoad> },
              { path: "dict", element: <LazyLoad><DictList /></LazyLoad> },
              { path: "config", element: <LazyLoad><ConfigList /></LazyLoad> },
              { path: "log", element: <LazyLoad><LogList /></LazyLoad> },
              { path: "file", element: <LazyLoad><FileList /></LazyLoad> },
              { path: "job", element: <LazyLoad><JobList /></LazyLoad> },
            ],
          },
        ],
      },
    ],
  },
]);
