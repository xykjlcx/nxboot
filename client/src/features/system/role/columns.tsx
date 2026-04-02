import { Tag } from "antd";
import type { ColDef } from "ag-grid-community";
import type { RoleVO } from "./types";

export const roleColumns: ColDef<RoleVO>[] = [
  { field: "roleName", headerName: "角色名称", width: 150 },
  { field: "roleKey", headerName: "权限标识", width: 150 },
  { field: "sortOrder", headerName: "排序", width: 80 },
  {
    field: "enabled",
    headerName: "状态",
    width: 90,
    cellRenderer: (params: { value: boolean }) => (
      <Tag color={params.value ? "success" : "error"}>{params.value ? "正常" : "停用"}</Tag>
    ),
  },
  { field: "remark", headerName: "备注", width: 200 },
  { field: "createTime", headerName: "创建时间", width: 180 },
];
