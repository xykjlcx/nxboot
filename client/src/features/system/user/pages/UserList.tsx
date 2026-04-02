import { useState } from "react";
import { Button, Input, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import type { ColDef } from "ag-grid-community";
import { NxTable } from "@/shared/components/NxTable";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxPagination } from "@/shared/components/NxPagination";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useUsers, useDeleteUser } from "../api";
import { userColumns } from "../columns";
import { UserForm } from "./UserForm";
import type { UserVO, UserQuery } from "../types";

const initialFilter: UserQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function UserList() {
  const [query, setQuery] = useState<UserQuery>(initialFilter);
  const { data: pageData, isLoading } = useUsers(query);
  const deleteUser = useDeleteUser();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteUser.mutateAsync(id);
    message.success("删除成功");
  };

  // 操作列
  const actionColumn: ColDef<UserVO> = {
    headerName: "操作",
    width: 160,
    pinned: "right",
    sortable: false,
    resizable: false,
    cellRenderer: (params: { data: UserVO | undefined }) => {
      const record = params.data;
      if (!record) return null;
      return (
        <NxDrawer.Trigger<UserVO | null>>
          {({ show }) => (
            <Space>
              {has("system:user:update") && (
                <Button type="link" size="small" icon={<EditOutlined />} onClick={() => show(record)}>
                  编辑
                </Button>
              )}
              {has("system:user:delete") && (
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
  };

  const columns = [...userColumns, actionColumn];

  return (
    <NxDrawer.Root<UserVO | null>>
      <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
        {/* 筛选栏 */}
        <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
          {(values, onChange) => (
            <Input
              placeholder="用户名/手机号"
              value={values.keyword}
              onChange={(e) => onChange({ keyword: e.target.value })}
              style={{ width: 200 }}
              allowClear
            />
          )}
        </NxFilter>

        {/* 工具栏 */}
        <div style={{ marginBottom: 12 }}>
          {has("system:user:create") && (
            <NxDrawer.Trigger<UserVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增用户
                </Button>
              )}
            </NxDrawer.Trigger>
          )}
        </div>

        {/* 表格 */}
        <NxTable<UserVO>
          storageKey="system-user"
          columnDefs={columns}
          rowData={pageData?.list ?? []}
          loading={isLoading}
          getRowId={(params) => params.data.id}
        />

        {/* 分页 */}
        <NxPagination
          current={query.pageNum ?? 1}
          pageSize={query.pageSize ?? 20}
          total={pageData?.total ?? 0}
          onChange={(pageNum, pageSize) => setQuery((prev) => ({ ...prev, pageNum, pageSize }))}
        />

        {/* Drawer 表单 */}
        <NxDrawer.Content title="用户信息">
          <UserForm />
        </NxDrawer.Content>
      </div>
    </NxDrawer.Root>
  );
}
