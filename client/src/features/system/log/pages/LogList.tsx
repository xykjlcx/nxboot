import { useState, useMemo } from "react";
import { Button, Input, Select, Modal } from "antd";
import { EyeOutlined } from "@ant-design/icons";
import type { ColDef } from "ag-grid-community";
import { NxTable } from "@/shared/components/NxTable";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxPagination } from "@/shared/components/NxPagination";
import { useLogs } from "../api";
import { logColumns } from "../columns";
import type { OperationLogVO, LogQuery } from "../types";

const initialFilter: LogQuery = { pageNum: 1, pageSize: 20, keyword: "" };

/** 操作日志（只读） */
export default function LogList() {
  const [query, setQuery] = useState<LogQuery>(initialFilter);
  const { data: pageData, isLoading } = useLogs(query);
  const [detail, setDetail] = useState<OperationLogVO | null>(null);

  const actionColumn: ColDef<OperationLogVO> = useMemo(
    () => ({
      headerName: "操作",
      width: 80,
      pinned: "right",
      sortable: false,
      resizable: false,
      cellRenderer: (params: { data: OperationLogVO | undefined }) => {
        const record = params.data;
        if (!record) return null;
        return (
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => setDetail(record)}>
            详情
          </Button>
        );
      },
    }),
    [],
  );

  const columns = useMemo(() => [...logColumns, actionColumn], [actionColumn]);

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
        {(values, onChange) => (
          <>
            <Input
              placeholder="模块/操作人"
              value={values.keyword}
              onChange={(e) => onChange({ keyword: e.target.value })}
              style={{ width: 200 }}
              allowClear
            />
            <Select
              placeholder="状态"
              value={values.status}
              onChange={(v) => onChange({ status: v })}
              style={{ width: 120 }}
              allowClear
              options={[
                { value: 0, label: "成功" },
                { value: 1, label: "失败" },
              ]}
            />
          </>
        )}
      </NxFilter>

      <NxTable<OperationLogVO>
        storageKey="system-log"
        columnDefs={columns}
        rowData={pageData?.list ?? []}
        loading={isLoading}
        getRowId={(params) => params.data.id}
      />

      <NxPagination
        current={query.pageNum ?? 1}
        pageSize={query.pageSize ?? 20}
        total={pageData?.total ?? 0}
        onChange={(pageNum, pageSize) => setQuery((prev) => ({ ...prev, pageNum, pageSize }))}
      />

      {/* 详情 Modal */}
      <Modal title="日志详情" open={!!detail} onCancel={() => setDetail(null)} footer={null} width={640}>
        {detail && (
          <div style={{ fontSize: 14, lineHeight: 2 }}>
            <p>
              <strong>操作模块：</strong>
              {detail.module}
            </p>
            <p>
              <strong>操作类型：</strong>
              {detail.operation}
            </p>
            <p>
              <strong>请求方式：</strong>
              {detail.requestMethod}
            </p>
            <p>
              <strong>请求地址：</strong>
              {detail.requestUrl}
            </p>
            <p>
              <strong>操作人员：</strong>
              {detail.operator}
            </p>
            <p>
              <strong>操作 IP：</strong>
              {detail.operatorIp}
            </p>
            <p>
              <strong>请求参数：</strong>
            </p>
            <pre style={{ background: "#f5f5f5", padding: 12, borderRadius: 4, overflow: "auto", maxHeight: 200 }}>
              {detail.requestParams}
            </pre>
            <p>
              <strong>返回结果：</strong>
            </p>
            <pre style={{ background: "#f5f5f5", padding: 12, borderRadius: 4, overflow: "auto", maxHeight: 200 }}>
              {detail.responseBody}
            </pre>
            {detail.errorMsg && (
              <>
                <p>
                  <strong>错误信息：</strong>
                </p>
                <pre style={{ background: "#fff2f0", padding: 12, borderRadius: 4, color: "#ff4d4f" }}>
                  {detail.errorMsg}
                </pre>
              </>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
