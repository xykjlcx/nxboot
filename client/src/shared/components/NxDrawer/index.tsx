import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import { Drawer } from "antd";

/* ---- Context ---- */

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

/* ---- Root ---- */

interface RootProps<T> {
  children: ReactNode;
  /** 默认初始数据 */
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

  return <DrawerContext.Provider value={value as DrawerContextValue}>{children}</DrawerContext.Provider>;
}

/* ---- Trigger ---- */

interface TriggerProps<T> {
  children: (ctx: { show: (data?: T) => void }) => ReactNode;
}

function Trigger<T = unknown>({ children }: TriggerProps<T>) {
  const { show } = useDrawerContext<T>();
  return <>{children({ show })}</>;
}

/* ---- Content ---- */

interface ContentProps {
  title?: string;
  width?: number;
  children: ReactNode;
}

function Content({ title, width = 520, children }: ContentProps) {
  const { open, hide } = useDrawerContext();
  return (
    <Drawer title={title} width={width} open={open} onClose={hide} destroyOnClose>
      {children}
    </Drawer>
  );
}

/* ---- 导出复合组件 ---- */

export const NxDrawer = {
  Root,
  Trigger,
  Content,
  useContext: useDrawerContext,
};
