import { useCallback, useSyncExternalStore } from "react";

type Theme = "light" | "dark";

const STORAGE_KEY = "nx-theme";

/** 读取初始主题：localStorage > 系统偏好 > light */
function getInitialTheme(): Theme {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === "dark" || stored === "light") return stored;
  if (window.matchMedia("(prefers-color-scheme: dark)").matches) return "dark";
  return "light";
}

let currentTheme: Theme = getInitialTheme();
const listeners = new Set<() => void>();

function applyTheme(theme: Theme) {
  document.documentElement.setAttribute("data-theme", theme);
}

// 初始化时应用
applyTheme(currentTheme);

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function getSnapshot(): Theme {
  return currentTheme;
}

/**
 * 主题切换 hook。
 *
 * @example
 * const { theme, toggle } = useTheme();
 * <Button onClick={toggle}>{theme === "dark" ? "浅色" : "深色"}</Button>
 */
export function useTheme() {
  const theme = useSyncExternalStore(subscribe, getSnapshot);

  const toggle = useCallback(() => {
    const next: Theme = currentTheme === "light" ? "dark" : "light";
    currentTheme = next;
    localStorage.setItem(STORAGE_KEY, next);
    applyTheme(next);
    for (const fn of listeners) fn();
  }, []);

  const isDark = theme === "dark";

  return { theme, isDark, toggle };
}
