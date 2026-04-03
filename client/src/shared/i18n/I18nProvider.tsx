import { useState, useCallback, useMemo } from "react";
import { I18nContext } from "./index";
import type { Locale } from "./index";
import zhCN from "./locales/zh-CN";
import enUS from "./locales/en-US";

const STORAGE_KEY = "nx-locale";

const locales: Record<Locale, Record<string, string>> = {
  "zh-CN": zhCN,
  "en-US": enUS,
};

/**
 * 国际化 Provider，包裹在应用最外层
 */
export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [locale, setLocaleState] = useState<Locale>(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === "zh-CN" || stored === "en-US") return stored;
    return "zh-CN";
  });

  const setLocale = useCallback((l: Locale) => {
    localStorage.setItem(STORAGE_KEY, l);
    setLocaleState(l);
  }, []);

  const t = useCallback(
    (key: string, vars?: Record<string, string | number>) => {
      let msg = locales[locale]?.[key] ?? key;
      if (vars) {
        for (const [k, v] of Object.entries(vars)) {
          msg = msg.replace(`{${k}}`, String(v));
        }
      }
      return msg;
    },
    [locale],
  );

  const value = useMemo(() => ({ locale, setLocale, t }), [locale, setLocale, t]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}
