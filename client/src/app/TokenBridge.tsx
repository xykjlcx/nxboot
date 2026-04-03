import { theme } from "antd";
import { useEffect } from "react";

/**
 * 令牌桥接——将 Ant Design 主题令牌同步到 CSS 自定义属性。
 *
 * 好处：tokens.css 中不再需要手动维护 [data-theme="dark"] 的颜色值，
 * 所有颜色由 antd 的 darkAlgorithm 统一派生，保证组件和自定义样式一致。
 */
export function TokenBridge() {
  const { token } = theme.useToken();

  useEffect(() => {
    const root = document.documentElement;

    // 主色
    root.style.setProperty("--color-primary", token.colorPrimary);
    root.style.setProperty("--color-primary-bg", token.colorPrimaryBg);
    root.style.setProperty("--color-primary-border", token.colorPrimaryBorder);

    // 中性色
    root.style.setProperty("--color-bg-page", token.colorBgLayout);
    root.style.setProperty("--color-bg-card", token.colorBgContainer);
    root.style.setProperty("--color-bg-elevated", token.colorBgElevated);
    root.style.setProperty("--color-border", token.colorBorderSecondary);
    root.style.setProperty("--color-text-primary", token.colorText);
    root.style.setProperty("--color-text-secondary", token.colorTextSecondary);

    // 功能色
    root.style.setProperty("--color-bg-pre", token.colorFillTertiary);
    root.style.setProperty("--color-bg-pre-error", token.colorErrorBg);
    root.style.setProperty("--color-text-error", token.colorError);

    // 阴影
    root.style.setProperty("--shadow-card", token.boxShadowTertiary);
  }, [token]);

  return null;
}
