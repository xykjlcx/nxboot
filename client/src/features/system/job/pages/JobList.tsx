import { useState, useMemo } from "react";
import { Button, Input, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined } from "@ant-design/icons";
import type { ColDef } from "ag-grid-community";
import { NxTable } from "@/shared/components/NxTable";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxPagination } from "@/shared/components/NxPagination";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useJobs, useDeleteJob, useRunJob } from "../api";
import { jobColumns } from "../columns";
import { JobForm } from "./JobForm";
import type { JobVO, JobQuery } from "../types";

const initialFilter: JobQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function JobList() {
  const [query, setQuery] = useState<JobQuery>(initialFilter);
  const { data: pageData, isLoading } = useJobs(query);
  const deleteJob = useDeleteJob();
  const runJob = useRunJob();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteJob.mutateAsync(id);
    message.success("删除成功");
  };

  const handleRun = async (id: string) => {
    await runJob.mutateAsync(id);
    message.success("执行成功");
  };

  const actionColumn: ColDef<JobVO> = useMemo(
    () => ({
      headerName: "操作",
      width: 220,
      pinned: "right",
      sortable: false,
      resizable: false,
      cellRenderer: (params: { data: JobVO | undefined }) => {
        const record = params.data;
        if (!record) return null;
        return (
          <NxDrawer.Trigger<JobVO | null>>
            {({ show }) => (
              <Space>
                {has("system:job:update") && (
                  <Button type="link" size="small" icon={<EditOutlined />} onClick={() => show(record)}>
                    编辑
                  </Button>
                )}
                {has("system:job:update") && (
                  <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={() => handleRun(record.id)}>
                    执行
                  </Button>
                )}
                {has("system:job:delete") && (
                  <Popconfirm title="确认删除?" onConfirm={() => handleDelete(record.id)}>
                    <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                      删除
                    </Button>
                  </Popconfirm>
                )}
              </Space>
            )}
          </NxDrawer.Trigger>
        );
      },
    }),
    [],
  );

  const columns = useMemo(() => [...jobColumns, actionColumn], [actionColumn]);

  return (
    <NxDrawer.Root<JobVO | null>>
      <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
        <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
          {(values, onChange) => (
            <Input
              placeholder="任务名称"
              value={values.keyword}
              onChange={(e) => onChange({ keyword: e.target.value })}
              style={{ width: 200 }}
              allowClear
            />
          )}
        </NxFilter>

        <div style={{ marginBottom: 12 }}>
          {has("system:job:create") && (
            <NxDrawer.Trigger<JobVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增任务
                </Button>
              )}
            </NxDrawer.Trigger>
          )}
        </div>

        <NxTable<JobVO>
          storageKey="system-job"
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

        <NxDrawer.Content title="定时任务">
          <JobForm />
        </NxDrawer.Content>
      </div>
    </NxDrawer.Root>
  );
}
