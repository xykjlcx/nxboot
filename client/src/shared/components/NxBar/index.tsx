import type { ReactNode, CSSProperties } from "react";
import styles from "./index.module.css";

interface NxBarProps {
  /** 左侧内容（新增按钮等） */
  left?: ReactNode;
  /** 右侧内容（刷新按钮等） */
  right?: ReactNode;
  style?: CSSProperties;
  className?: string;
}

/** 操作栏——统一工具栏布局，左右分区 */
export function NxBar({ left, right, style, className }: NxBarProps) {
  return (
    <div className={`${styles.bar} ${className ?? ""}`.trim()} style={style}>
      <div className={styles.left}>{left}</div>
      <div className={styles.right}>{right}</div>
    </div>
  );
}
