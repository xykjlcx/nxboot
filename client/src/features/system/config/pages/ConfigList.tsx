import { useState, useMemo } from "react";
import { Button, Input, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import type { ColDef } from "ag-grid-community";
import { NxTable } from "@/shared/components/NxTable";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxPagination } from "@/shared/components/NxPagination";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useConfigs, useDeleteConfig } from "../api";
import { configColumns } from "../columns";
import { ConfigForm } from "./ConfigForm";
import type { ConfigVO, ConfigQuery } from "../types";

const initialFilter: ConfigQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function ConfigList() {
  const [query, setQuery] = useState<ConfigQuery>(initialFilter);
  const { data: pageData, isLoading } = useConfigs(query);
  const deleteConfig = useDeleteConfig();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteConfig.mutateAsync(id);
    message.success("删除成功");
  };

  const actionColumn: ColDef<ConfigVO> = useMemo(
    () => ({
      headerName: "操作",
      width: 160,
      pinned: "right",
      sortable: false,
      resizable: false,
      cellRenderer: (params: { data: ConfigVO | undefined }) => {
        const record = params.data;
        if (!record) return null;
        return (
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
        );
      },
    }),
    [],
  );

  const columns = useMemo(() => [...configColumns, actionColumn], [actionColumn]);

  return (
    <NxDrawer.Root<ConfigVO | null>>
      <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
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

        <div style={{ marginBottom: 12 }}>
          {has("system:config:create") && (
            <NxDrawer.Trigger<ConfigVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增配置
                </Button>
              )}
            </NxDrawer.Trigger>
          )}
        </div>

        <NxTable<ConfigVO>
          storageKey="system-config"
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

        <NxDrawer.Content title="系统配置">
          <ConfigForm />
        </NxDrawer.Content>
      </div>
    </NxDrawer.Root>
  );
}
