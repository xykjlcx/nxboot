import { Empty, Table } from "antd";
import type { TableProps } from "antd";
import type { ColumnsType } from "antd/es/table";
import type { NxColumn, NxTableProps } from "./types";
import styles from "./index.module.css";

/* ---- 适配器：NxColumn → Ant Design ColumnsType ---- */

function toAntdColumns<T>(columns: NxColumn<T>[]): ColumnsType<T> {
  return columns.map((col) => ({
    dataIndex: col.field,
    key: col.field,
    title: col.title,
    width: col.width,
    fixed: col.fixed,
    align: col.align,
    ellipsis: col.ellipsis ? { showTitle: true } : undefined,
    render: col.render,
    sorter: col.sorter === true ? true : typeof col.sorter === "function" ? col.sorter : undefined,
    children: col.children ? toAntdColumns(col.children) : undefined,
  }));
}

/**
 * 统一表格组件——屏蔽底层引擎差异。
 *
 * 当前引擎：Ant Design Table。
 * 后续可按需接入 AG Grid（复杂编辑场景），对外 API 不变。
 */
export function NxTable<T extends object>({
  columns,
  data,
  rowKey = "id",
  loading,
  pagination,
  scroll,
  size = "small",
  onRow,
  rowSelection,
  expandable,
  className,
}: NxTableProps<T>) {
  const antdColumns = toAntdColumns(columns);

  const paginationConfig =
    pagination === false
      ? false
      : pagination
        ? {
            current: pagination.current ?? 1,
            pageSize: pagination.pageSize ?? 20,
            total: pagination.total ?? 0,
            onChange: pagination.onChange,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total: number) => `共 ${total} 条`,
          }
        : false;

  return (
    <Table<T>
      columns={antdColumns}
      dataSource={data}
      rowKey={rowKey}
      loading={loading}
      pagination={paginationConfig}
      scroll={scroll}
      size={size}
      onRow={onRow as TableProps<T>["onRow"]}
      rowSelection={rowSelection}
      expandable={expandable as TableProps<T>["expandable"]}
      locale={{ emptyText: <Empty description="暂无数据" /> }}
      className={`${styles.table} ${className ?? ""}`.trim()}
    />
  );
}

export type { NxColumn, NxPagination, NxTableProps } from "./types";
