import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import { Drawer, Button, Tooltip, Skeleton } from "antd";
import { CloseOutlined, FullscreenOutlined, FullscreenExitOutlined } from "@ant-design/icons";
import styles from "./index.module.css";

/* ──────────── Context ──────────── */

interface DrawerContextValue<T = unknown> {
  open: boolean;
  data: T | null;
  show: (data?: T) => void;
  hide: () => void;
}

const DrawerContext = createContext<DrawerContextValue | null>(null);

function useDrawerContext<T = unknown>(): DrawerContextValue<T> {
  const ctx = useContext(DrawerContext);
  if (!ctx) throw new Error("NxDrawer 子组件必须在 NxDrawer.Root 内使用");
  return ctx as DrawerContextValue<T>;
}

/* ──────────── Root ──────────── */

interface RootProps<T> {
  children: ReactNode;
  defaultData?: T;
}

function Root<T = unknown>({ children, defaultData }: RootProps<T>) {
  const [open, setOpen] = useState(false);
  const [data, setData] = useState<T | null>(defaultData ?? null);

  const show = useCallback((d?: T) => {
    if (d !== undefined) setData(d);
    setOpen(true);
  }, []);

  const hide = useCallback(() => {
    setOpen(false);
    setData(null);
  }, []);

  const value = useMemo(() => ({ open, data, show, hide }), [open, data, show, hide]);

  return (
    <DrawerContext.Provider value={value as DrawerContextValue}>
      {children}
    </DrawerContext.Provider>
  );
}

/* ──────────── Trigger ──────────── */

interface TriggerProps<T> {
  children: (ctx: { show: (data?: T) => void }) => ReactNode;
}

function Trigger<T = unknown>({ children }: TriggerProps<T>) {
  const { show } = useDrawerContext<T>();
  return <>{children({ show })}</>;
}

/* ──────────── Content ──────────── */

interface ContentProps {
  title?: string;
  /** 宽度，支持数值(px)或字符串(百分比)，默认 520 */
  width?: number | string;
  children: ReactNode;
  /** 显示全屏按钮，默认 true */
  fullscreen?: boolean;
  /** 头部右侧额外操作 */
  extra?: ReactNode;
  /** 内容加载中，显示骨架屏 */
  loading?: boolean;
}

function Content({ title, width = 520, children, fullscreen = true, extra, loading }: ContentProps) {
  const { open, hide } = useDrawerContext();
  const [isFullscreen, setIsFullscreen] = useState(false);

  const drawerWidth = isFullscreen ? "100%" : width;

  return (
    <Drawer
      open={open}
      onClose={hide}
      destroyOnClose
      width={drawerWidth}
      closable={false}
      title={null}
      styles={{ body: { padding: 0, display: "flex", flexDirection: "column", height: "100%" } }}
    >
      {/* 自定义头部 */}
      <div className={styles.header}>
        <div className={styles.headerTitle}>{title}</div>
        <div className={styles.headerActions}>
          {extra}
          {fullscreen && (
            <Tooltip title={isFullscreen ? "退出全屏" : "全屏"}>
              <Button
                type="text"
                size="small"
                icon={isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
                onClick={() => setIsFullscreen((v) => !v)}
                className={styles.headerBtn}
              />
            </Tooltip>
          )}
          <Tooltip title="关闭">
            <Button
              type="text"
              size="small"
              icon={<CloseOutlined />}
              onClick={hide}
              className={styles.headerBtn}
            />
          </Tooltip>
        </div>
      </div>

      {/* 内容区 */}
      <div className={styles.body}>
        {loading ? <Skeleton active paragraph={{ rows: 6 }} /> : children}
      </div>
    </Drawer>
  );
}

/* ──────────── 导出复合组件 ──────────── */

export const NxDrawer = {
  Root,
  Trigger,
  Content,
  useContext: useDrawerContext,
};
