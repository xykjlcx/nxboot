import { useState } from "react";
import { Button, Input, Popconfirm, message } from "antd";
import { DisconnectOutlined, ReloadOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { usePerm } from "@/shared/hooks/usePerm";
import { useOnlineUsers, useForceLogout } from "../api";
import { onlineUserColumns } from "../columns";
import type { OnlineUserVO } from "../types";

interface FilterValues {
  keyword: string;
}

const initialFilter: FilterValues = { keyword: "" };

export default function OnlineUserList() {
  const [filter, setFilter] = useState<FilterValues>(initialFilter);
  const { data: list, isLoading, refetch } = useOnlineUsers();
  const forceLogout = useForceLogout();
  const { has } = usePerm();

  // 前端过滤（在线用户数据量小，无需后端分页）
  const filteredList = (list ?? []).filter((item) => {
    if (!filter.keyword) return true;
    const kw = filter.keyword.toLowerCase();
    return item.username.toLowerCase().includes(kw) || (item.ip && item.ip.includes(kw));
  });

  const handleForceLogout = async (sessionId: string) => {
    await forceLogout.mutateAsync(sessionId);
    message.success("已强制下线");
  };

  const actionColumn: NxColumn<OnlineUserVO> = {
    field: "_action",
    title: "操作",
    width: 120,
    render: (_, record) =>
      has("system:online:forceLogout") ? (
        <Popconfirm title="确认强制下线该用户？" onConfirm={() => handleForceLogout(record.sessionId)}>
          <Button type="link" size="small" danger icon={<DisconnectOutlined />}>
            强制下线
          </Button>
        </Popconfirm>
      ) : null,
  };

  const columns: NxColumn<OnlineUserVO>[] = [...onlineUserColumns, actionColumn];

  return (
    <>
      <NxFilter initialValues={initialFilter} onSearch={(v) => setFilter(v)}>
        {(values, onChange) => (
          <Input
            placeholder="用户名/IP"
            value={values.keyword}
            onChange={(e) => onChange({ keyword: e.target.value })}
            style={{ width: 200 }}
            allowClear
          />
        )}
      </NxFilter>

      <NxBar right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>} />

      <NxTable<OnlineUserVO>
        columns={columns}
        data={filteredList}
        rowKey="sessionId"
        loading={isLoading}
        pagination={false}
      />
    </>
  );
}
