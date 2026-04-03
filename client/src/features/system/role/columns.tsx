import { Tag } from "antd";
import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { RoleVO } from "./types";

export const roleColumns: NxColumn<RoleVO>[] = [
  { field: "roleName", title: "角色名称", width: 150 },
  { field: "roleKey", title: "权限标识", width: 150 },
  { field: "sortOrder", title: "排序", width: 80 },
  {
    field: "enabled",
    title: "状态",
    width: 90,
    render: (value: boolean) => (
      <Tag color={value ? "success" : "error"}>{value ? "正常" : "停用"}</Tag>
    ),
  },
  { field: "remark", title: "备注", width: 200 },
  { field: "createTime", title: "创建时间", width: 170, render: (v: string) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-" },
];
