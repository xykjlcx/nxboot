import { useState } from "react";
import { Button, Input, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useConfigs, useDeleteConfig } from "../api";
import { configColumns } from "../columns";
import { ConfigForm } from "./ConfigForm";
import type { ConfigVO, ConfigQuery } from "../types";

const initialFilter: ConfigQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function ConfigList() {
  const [query, setQuery] = useState<ConfigQuery>(initialFilter);
  const { data: pageData, isLoading, refetch } = useConfigs(query);
  const deleteConfig = useDeleteConfig();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteConfig.mutateAsync(id);
    message.success("删除成功");
  };

  const actionColumn: NxColumn<ConfigVO> = {
    field: "_action",
    title: "操作",
    width: 160,
    render: (_, record) => (
      <NxDrawer.Trigger<ConfigVO | null>>
        {({ show }) => (
          <Space>
            {has("system:config:update") && (
              <Button type="link" size="small" icon={<EditOutlined />} onClick={() => show(record)}>
                编辑
              </Button>
            )}
            {has("system:config:delete") && (
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

  const columns: NxColumn<ConfigVO>[] = [...configColumns, actionColumn];

  return (
    <NxDrawer.Root<ConfigVO | null>>
      <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
        {(values, onChange) => (
          <Input
            placeholder="配置名称/键"
            value={values.keyword}
            onChange={(e) => onChange({ keyword: e.target.value })}
            style={{ width: 200 }}
            allowClear
          />
        )}
      </NxFilter>

      <NxBar
        left={
          has("system:config:create") && (
            <NxDrawer.Trigger<ConfigVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增配置
                </Button>
              )}
            </NxDrawer.Trigger>
          )
        }
        right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>}
      />

      <NxTable<ConfigVO>
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

      <NxDrawer.Content title="系统配置">
        <ConfigForm />
      </NxDrawer.Content>
    </NxDrawer.Root>
  );
}
