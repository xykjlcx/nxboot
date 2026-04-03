import { create } from "zustand";
import { persist } from "zustand/middleware";

/* ──────────── 布局模式 ──────────── */

export type LayoutMode =
  /** 经典：左侧菜单 + 右侧内容 */
  | "sidebar"
  /** 顶部导航：水平菜单 + 下方内容 */
  | "top"
  /** 混合：顶部模块选择 + 左侧子菜单（NxBoot 默认） */
  | "mix";

/* ──────────── Preferences 类型 ──────────── */

export interface Preferences {
  // ── 布局 ──
  /** 布局模式 */
  layoutMode: LayoutMode;
  /** 侧边栏宽度（px） */
  sidebarWidth: number;
  /** 侧边栏折叠后宽度（px） */
  sidebarCollapsedWidth: number;
  /** 侧边栏是否折叠 */
  sidebarCollapsed: boolean;
  /** Header 高度（px） */
  headerHeight: number;

  // ── 主题 ──
  /** 主题色（Ant Design colorPrimary） */
  colorPrimary: string;
  /** Header 背景色 */
  headerBgColor: string;
  /** 深色模式（由 useTheme 管理，此处只做 bridge） */
  darkMode: boolean;

  // ── 功能开关 ──
  /** 显示面包屑 */
  showBreadcrumb: boolean;
  /** 显示页脚 */
  showFooter: boolean;
  /** 内容区圆角（px） */
  contentBorderRadius: number;

  // ── 导航 ──
  /** 首页路径 */
  homePath: string;
}

/* ──────────── 默认值 ──────────── */

export const defaultPreferences: Preferences = {
  layoutMode: "mix",
  sidebarWidth: 180,
  sidebarCollapsedWidth: 48,
  sidebarCollapsed: false,
  headerHeight: 48,

  colorPrimary: "#1677ff",
  headerBgColor: "#2563eb",
  darkMode: false,

  showBreadcrumb: true,
  showFooter: false,
  contentBorderRadius: 6,

  homePath: "/system/user",
};

/* ──────────── Store ──────────── */

interface PreferencesStore extends Preferences {
  /** 批量更新 */
  update: (patch: Partial<Preferences>) => void;
  /** 重置为默认值 */
  reset: () => void;
}

export const usePreferences = create<PreferencesStore>()(
  persist(
    (set) => ({
      ...defaultPreferences,
      update: (patch) => set(patch),
      reset: () => set(defaultPreferences),
    }),
    {
      name: "nx-preferences",
    },
  ),
);
