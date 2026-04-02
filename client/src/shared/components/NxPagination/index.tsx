import { Pagination } from "antd";

interface NxPaginationProps {
  current: number;
  pageSize: number;
  total: number;
  onChange: (current: number, pageSize: number) => void;
}

/** 分页组件 */
export function NxPagination({ current, pageSize, total, onChange }: NxPaginationProps) {
  return (
    <div style={{ display: "flex", justifyContent: "flex-end", padding: "12px 0" }}>
      <Pagination
        current={current}
        pageSize={pageSize}
        total={total}
        showSizeChanger
        showQuickJumper
        showTotal={(t) => `共 ${t} 条`}
        onChange={onChange}
        pageSizeOptions={["10", "20", "50", "100"]}
      />
    </div>
  );
}
