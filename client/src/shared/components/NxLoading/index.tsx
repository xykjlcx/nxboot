import { Spin } from "antd";

interface NxLoadingProps {
  /** 加载提示文字 */
  tip?: string;
}

/**
 * 统一加载指示器——居中展示，可配置提示文字。
 * AuthGuard 和 Suspense fallback 共用，确保全局加载风格一致。
 */
export function NxLoading({ tip = "加载中..." }: NxLoadingProps) {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        padding: 48,
        minHeight: 200,
      }}
    >
      <Spin tip={tip}>
        {/* Spin 带 tip 时需要包裹子元素才能显示文字 */}
        <div style={{ width: 1, height: 1 }} />
      </Spin>
    </div>
  );
}
