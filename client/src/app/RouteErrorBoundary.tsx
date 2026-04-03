import { useRouteError } from "react-router-dom";
import { Button, Result } from "antd";

/**
 * 路由级错误边界——单个页面崩溃不影响其他页面。
 * 用在 React Router 的 errorElement 上。
 */
export function RouteErrorBoundary() {
  const error = useRouteError();
  const isDev = import.meta.env.DEV;

  return (
    <Result
      status="error"
      title="页面加载失败"
      subTitle={isDev && error instanceof Error ? error.message : "请刷新页面或联系管理员"}
      extra={
        <Button type="primary" onClick={() => window.location.reload()}>
          刷新页面
        </Button>
      }
    />
  );
}
