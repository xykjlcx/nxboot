import type { ReactNode } from "react";

/** 统一列定义——屏蔽 Ant Design Table / AG Grid 底层差异 */
export interface NxColumn<T = unknown> {
  /** 数据字段名（操作列等非数据列用任意标识如 "_action"） */
  field: string;
  /** 列标题 */
  title: ReactNode;
  /** 列宽（px） */
  width?: number;
  /** 固定列 */
  fixed?: "left" | "right";
  /** 对齐方式 */
  align?: "left" | "center" | "right";
  /** 超长省略 */
  ellipsis?: boolean;
  /** 自定义渲染 */
  render?: (value: any, record: T, index: number) => ReactNode;
  /** 可排序（true 使用默认排序，传函数自定义） */
  sorter?: boolean | ((a: T, b: T) => number);
  /** 分组表头子列 */
  children?: NxColumn<T>[];
}

/** 分页配置——current/pageSize/total 可选，内部提供默认值 */
export interface NxPagination {
  current?: number;
  pageSize?: number;
  total?: number;
  onChange: (page: number, pageSize: number) => void;
}

/** NxTable 属性 */
export interface NxTableProps<T = unknown> {
  /** 列定义 */
  columns: NxColumn<T>[];
  /** 数据源 */
  data: T[];
  /** 行唯一标识，默认 "id" */
  rowKey?: string | ((record: T) => string);
  /** 加载中 */
  loading?: boolean;
  /** 分页，传 false 隐藏 */
  pagination?: NxPagination | false;
  /** 滚动区域 */
  scroll?: { x?: number | string; y?: number | string };
  /** 尺寸 */
  size?: "small" | "middle" | "large";
  /** 行事件 */
  onRow?: (record: T, index: number) => React.HTMLAttributes<HTMLElement>;
  /** 行选择 */
  rowSelection?: {
    selectedRowKeys: React.Key[];
    onChange: (keys: React.Key[], rows: T[]) => void;
  };
  /** 可展开（树形表格） */
  expandable?: {
    childrenColumnName?: string;
    defaultExpandAllRows?: boolean;
    expandedRowRender?: (record: T) => ReactNode;
  };
  /** 附加 className */
  className?: string;
}
