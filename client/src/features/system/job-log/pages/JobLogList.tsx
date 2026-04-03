import { useState } from "react";
import { Button, Input, Select, Modal } from "antd";
import { EyeOutlined, ReloadOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { useJobLogs } from "../api";
import { jobLogColumns } from "../columns";
import type { JobLogVO, JobLogQuery } from "../types";

const initialFilter: JobLogQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function JobLogList() {
  const [query, setQuery] = useState<JobLogQuery>(initialFilter);
  const { data: pageData, isLoading, refetch } = useJobLogs(query);
  const [detail, setDetail] = useState<JobLogVO | null>(null);

  const actionColumn: NxColumn<JobLogVO> = {
    field: "_action",
    title: "操作",
    width: 80,
    render: (_, record) => (
      <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => setDetail(record)}>
        详情
      </Button>
    ),
  };

  const columns: NxColumn<JobLogVO>[] = [...jobLogColumns, actionColumn];

  return (
    <>
      <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
        {(values, onChange) => (
          <>
            <Input
              placeholder="任务名称 / 调用目标"
              value={values.keyword}
              onChange={(e) => onChange({ keyword: e.target.value })}
              style={{ width: 220 }}
              allowClear
            />
            <Select
              placeholder="执行状态"
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

      <NxBar right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>} />

      <NxTable<JobLogVO>
        columns={columns}
        data={pageData?.list ?? []}
        loading={isLoading}
        pagination={{
          current: query.pageNum,
          pageSize: query.pageSize,
          total: pageData?.total ?? 0,
          onChange: (pageNum, pageSize) => setQuery((prev) => ({ ...prev, pageNum, pageSize })),
        }}
      />

      <Modal title="执行日志详情" open={!!detail} onCancel={() => setDetail(null)} footer={null} width={640}>
        {detail && (
          <div style={{ fontSize: 14, lineHeight: 2 }}>
            <p><strong>任务名称：</strong>{detail.jobName}</p>
            <p><strong>任务分组：</strong>{detail.jobGroup}</p>
            <p><strong>调用目标：</strong>{detail.invokeTarget}</p>
            <p><strong>执行状态：</strong>{detail.status === 0 ? "成功" : "失败"}</p>
            <p><strong>开始时间：</strong>{detail.startTime ? new Date(detail.startTime).toLocaleString() : "-"}</p>
            <p><strong>结束时间：</strong>{detail.endTime ? new Date(detail.endTime).toLocaleString() : "-"}</p>
            <p><strong>耗时：</strong>{detail.duration != null ? `${detail.duration}ms` : "-"}</p>
            {detail.errorMsg && (
              <>
                <p><strong>错误信息：</strong></p>
                <pre style={{ background: "var(--color-bg-pre-error, #fff2f0)", padding: 12, borderRadius: 4, color: "var(--color-text-error, #ff4d4f)", overflow: "auto", maxHeight: 200 }}>
                  {detail.errorMsg}
                </pre>
              </>
            )}
          </div>
        )}
      </Modal>
    </>
  );
}
