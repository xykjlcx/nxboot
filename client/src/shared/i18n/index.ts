import { createContext, useContext } from "react";
import zhCN from "./locales/zh-CN";

// 以中文为基准类型，英文必须实现相同 key
type Messages = typeof zhCN;
type MessageKey = keyof Messages;
type Locale = "zh-CN" | "en-US";

interface I18nContextValue {
  locale: Locale;
  setLocale: (locale: Locale) => void;
  t: (key: MessageKey, vars?: Record<string, string | number>) => string;
}

export type { Locale, Messages, MessageKey };

export const I18nContext = createContext<I18nContextValue | null>(null);

/**
 * 获取国际化上下文
 *
 * @example
 * const { t, locale, setLocale } = useI18n();
 * <span>{t("common.create")}</span>
 */
export function useI18n() {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useI18n must be used within I18nProvider");
  return ctx;
}
