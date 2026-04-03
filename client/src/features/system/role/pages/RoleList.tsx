import { useState } from "react";
import { Button, Input, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useRoles, useDeleteRole } from "../api";
import { roleColumns } from "../columns";
import { RoleForm } from "./RoleForm";
import type { RoleVO, RoleQuery } from "../types";

const initialFilter: RoleQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function RoleList() {
  const [query, setQuery] = useState<RoleQuery>(initialFilter);
  const { data: pageData, isLoading, refetch } = useRoles(query);
  const deleteRole = useDeleteRole();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteRole.mutateAsync(id);
    message.success("删除成功");
  };

  const actionColumn: NxColumn<RoleVO> = {
    field: "_action",
    title: "操作",
    width: 160,
    render: (_, record) => (
      <NxDrawer.Trigger<RoleVO | null>>
        {({ show }) => (
          <Space>
            {has("system:role:update") && (
              <Button type="link" size="small" icon={<EditOutlined />} onClick={() => show(record)}>
                编辑
              </Button>
            )}
            {has("system:role:delete") && (
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

  const columns: NxColumn<RoleVO>[] = [...roleColumns, actionColumn];

  return (
    <NxDrawer.Root<RoleVO | null>>
      <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
        {(values, onChange) => (
          <Input
            placeholder="角色名称"
            value={values.keyword}
            onChange={(e) => onChange({ keyword: e.target.value })}
            style={{ width: 160 }}
            allowClear
          />
        )}
      </NxFilter>

      <NxBar
        left={
          has("system:role:create") && (
            <NxDrawer.Trigger<RoleVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增角色
                </Button>
              )}
            </NxDrawer.Trigger>
          )
        }
        right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>}
      />

      <NxTable<RoleVO>
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

      <NxDrawer.Content title="角色信息" width={600}>
        <RoleForm />
      </NxDrawer.Content>
    </NxDrawer.Root>
  );
}
