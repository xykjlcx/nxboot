import { Component } from "react";
import type { ErrorInfo, ReactNode } from "react";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

/**
 * 全局错误边界——捕获渲染阶段未处理的异常，展示友好回退 UI。
 */
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("[ErrorBoundary]", error, info.componentStack);
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (!this.state.hasError) {
      return this.props.children;
    }

    const isDev = import.meta.env.DEV;

    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          minHeight: "100vh",
          padding: 24,
          background: "var(--color-bg-page, #f5f5f5)",
        }}
      >
        <svg
          viewBox="0 0 1024 1024"
          width="64"
          height="64"
          style={{ marginBottom: 16, color: "var(--color-text-error, #ff4d4f)" }}
        >
          <path
            fill="currentColor"
            d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm-32 232c0-4.4 3.6-8 8-8h48c4.4 0 8 3.6 8 8v272c0 4.4-3.6 8-8 8h-48c-4.4 0-8-3.6-8-8V296zm32 440a48.01 48.01 0 0 1 0-96 48.01 48.01 0 0 1 0 96z"
          />
        </svg>

        <h1 style={{ fontSize: 20, fontWeight: 600, margin: "0 0 8px", color: "var(--color-text-primary)" }}>
          页面出错了
        </h1>

        {isDev && this.state.error && (
          <pre
            style={{
              maxWidth: 600,
              padding: 12,
              marginBottom: 16,
              background: "var(--color-bg-pre-error, #fff1f0)",
              border: "1px solid var(--color-border, #ffa39e)",
              borderRadius: 6,
              fontSize: 13,
              color: "var(--color-text-error, #cf1322)",
              whiteSpace: "pre-wrap",
              wordBreak: "break-word",
            }}
          >
            {this.state.error.message}
          </pre>
        )}

        <button
          type="button"
          onClick={this.handleReload}
          style={{
            padding: "8px 24px",
            fontSize: 14,
            color: "#fff",
            background: "var(--color-primary, #1677ff)",
            border: "none",
            borderRadius: 6,
            cursor: "pointer",
          }}
        >
          重新加载
        </button>
      </div>
    );
  }
}
