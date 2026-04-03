import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App, ConfigProvider, theme as antdTheme } from "antd";
import antdZhCN from "antd/locale/zh_CN";
import antdEnUS from "antd/locale/en_US";
import { RouterProvider } from "react-router-dom";
import { ErrorBoundary } from "./ErrorBoundary";
import { TokenBridge } from "./TokenBridge";
import { useTheme } from "@/shared/hooks/useTheme";
import { usePreferences } from "@/shared/stores/preferences";
import { useI18n } from "@/shared/i18n";
import { I18nProvider } from "@/shared/i18n/I18nProvider";
import { router } from "./routes";
import type { Locale as AntdLocale } from "antd/es/locale";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 30_000,
    },
  },
});

/** antd 语言包映射 */
const antdLocaleMap: Record<string, AntdLocale> = {
  "zh-CN": antdZhCN,
  "en-US": antdEnUS,
};

function AntdProvider({ children }: { children: React.ReactNode }) {
  const { isDark } = useTheme();
  const colorPrimary = usePreferences((s) => s.colorPrimary);
  const { locale } = useI18n();

  return (
    <ConfigProvider
      locale={antdLocaleMap[locale] ?? antdZhCN}
      theme={{
        token: { fontSize: 13, colorPrimary },
        algorithm: isDark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
      }}
    >
      <App>
        <TokenBridge />
        {children}
      </App>
    </ConfigProvider>
  );
}

export function Providers() {
  return (
    <ErrorBoundary>
      <I18nProvider>
        <QueryClientProvider client={queryClient}>
          <AntdProvider>
            <RouterProvider router={router} />
          </AntdProvider>
        </QueryClientProvider>
      </I18nProvider>
    </ErrorBoundary>
  );
}
