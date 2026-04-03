import { createBrowserRouter } from "react-router-dom";
import { lazy, Suspense } from "react";
import { NxLoading } from "@/shared/components/NxLoading";
import { RouteErrorBoundary } from "./RouteErrorBoundary";
import { AuthGuard } from "./auth-guard";
import { BasicLayout } from "./layouts/BasicLayout";
import { BlankLayout } from "./layouts/BlankLayout";
import { DefaultRedirect } from "./DefaultRedirect";

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
const JobLogList = lazy(() => import("@/features/system/job-log/pages/JobLogList"));
const LoginLogList = lazy(() => import("@/features/system/login-log/pages/LoginLogList"));
const DeptList = lazy(() => import("@/features/system/dept/pages/DeptList"));
const OnlineUserList = lazy(() => import("@/features/system/online/pages/OnlineUserList"));
const Monitor = lazy(() => import("@/features/system/monitor/pages/Monitor"));
const Placeholder = lazy(() => import("@/app/Placeholder"));

/** 懒加载包装组件 */
function LazyLoad({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<NxLoading />}>{children}</Suspense>;
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
    errorElement: <RouteErrorBoundary />,
    children: [
      {
        element: <BasicLayout />,
        errorElement: <RouteErrorBoundary />,
        children: [
          { index: true, element: <DefaultRedirect />, errorElement: <RouteErrorBoundary /> },
          {
            path: "system",
            errorElement: <RouteErrorBoundary />,
            children: [
              { index: true, element: <DefaultRedirect />, errorElement: <RouteErrorBoundary /> },
              { path: "user", element: <LazyLoad><UserList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "role", element: <LazyLoad><RoleList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "menu", element: <LazyLoad><MenuList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "dict", element: <LazyLoad><DictList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "config", element: <LazyLoad><ConfigList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "log", element: <LazyLoad><LogList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "file", element: <LazyLoad><FileList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "job", element: <LazyLoad><JobList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "job-log", element: <LazyLoad><JobLogList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "login-log", element: <LazyLoad><LoginLogList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "dept", element: <LazyLoad><DeptList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "online", element: <LazyLoad><OnlineUserList /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
              { path: "monitor", element: <LazyLoad><Monitor /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
            ],
          },
          { path: "business/*", element: <LazyLoad><Placeholder /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
          { path: "analytics/*", element: <LazyLoad><Placeholder /></LazyLoad>, errorElement: <RouteErrorBoundary /> },
        ],
      },
    ],
  },
]);
