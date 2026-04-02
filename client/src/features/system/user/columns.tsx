import { Tag } from "antd";
import type { ColDef } from "ag-grid-community";
import type { UserVO } from "./types";

export const userColumns: ColDef<UserVO>[] = [
  { field: "username", headerName: "用户名", width: 120 },
  { field: "nickname", headerName: "昵称", width: 120 },
  { field: "email", headerName: "邮箱", width: 180 },
  { field: "phone", headerName: "手机号", width: 140 },
  {
    field: "enabled",
    headerName: "状态",
    width: 90,
    cellRenderer: (params: { value: boolean }) => (
      <Tag color={params.value ? "success" : "error"}>{params.value ? "正常" : "停用"}</Tag>
    ),
  },
  { field: "createTime", headerName: "创建时间", width: 180 },
];
