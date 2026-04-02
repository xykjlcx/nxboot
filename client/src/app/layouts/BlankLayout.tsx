import { Outlet } from "react-router-dom";

/** 空布局，直接渲染子路由 */
export function BlankLayout() {
  return <Outlet />;
}
