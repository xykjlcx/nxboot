import { useState } from "react";
import { Button, Input, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined, ReloadOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useJobs, useDeleteJob, useRunJob } from "../api";
import { jobColumns } from "../columns";
import { JobForm } from "./JobForm";
import type { JobVO, JobQuery } from "../types";

const initialFilter: JobQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function JobList() {
  const [query, setQuery] = useState<JobQuery>(initialFilter);
  const { data: pageData, isLoading, refetch } = useJobs(query);
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

  const actionColumn: NxColumn<JobVO> = {
    field: "_action",
    title: "操作",
    width: 220,
    render: (_, record) => (
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
    ),
  };

  const columns: NxColumn<JobVO>[] = [...jobColumns, actionColumn];

  return (
    <NxDrawer.Root<JobVO | null>>
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

      <NxBar
        left={
          has("system:job:create") && (
            <NxDrawer.Trigger<JobVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增任务
                </Button>
              )}
            </NxDrawer.Trigger>
          )
        }
        right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>}
      />

      <NxTable<JobVO>
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

      <NxDrawer.Content title="定时任务">
        <JobForm />
      </NxDrawer.Content>
    </NxDrawer.Root>
  );
}
