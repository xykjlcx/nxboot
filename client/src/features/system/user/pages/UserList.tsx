import { useState } from "react";
import { Button, Input, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useUsers, useDeleteUser } from "../api";
import { userColumns } from "../columns";
import { UserForm } from "./UserForm";
import type { UserVO, UserQuery } from "../types";

const initialFilter: UserQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function UserList() {
  const [query, setQuery] = useState<UserQuery>(initialFilter);
  const { data: pageData, isLoading, refetch } = useUsers(query);
  const deleteUser = useDeleteUser();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteUser.mutateAsync(id);
    message.success("删除成功");
  };

  const actionColumn: NxColumn<UserVO> = {
    field: "_action",
    title: "操作",
    width: 160,
    render: (_, record) => (
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
    ),
  };

  const columns: NxColumn<UserVO>[] = [...userColumns, actionColumn];

  return (
    <NxDrawer.Root<UserVO | null>>
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

      <NxBar
        left={
          has("system:user:create") && (
            <NxDrawer.Trigger<UserVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增用户
                </Button>
              )}
            </NxDrawer.Trigger>
          )
        }
        right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>}
      />

      <NxTable<UserVO>
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

      <NxDrawer.Content title="用户信息">
        <UserForm />
      </NxDrawer.Content>
    </NxDrawer.Root>
  );
}
