import { Spin } from "antd";

interface NxLoadingProps {
  /** 加载提示文字 */
  tip?: string;
}

/**
 * 统一加载指示器——内容区级别，用于 Suspense fallback 和页面内加载。
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
        {/* Spin 带 tip 时子元素需要足够宽度，否则文字会竖排 */}
        <div style={{ width: 100, height: 50 }} />
      </Spin>
    </div>
  );
}
