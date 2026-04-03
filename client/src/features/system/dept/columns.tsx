import { Tag } from "antd";
import type { NxColumn } from "@/shared/components/NxTable";
import type { DeptVO } from "./types";

export const deptColumns: NxColumn<DeptVO>[] = [
  { field: "deptName", title: "部门名称", width: 200 },
  { field: "sortOrder", title: "排序", width: 80, align: "center" },
  { field: "leader", title: "负责人", width: 120 },
  { field: "phone", title: "电话", width: 140 },
  { field: "email", title: "邮箱", width: 180 },
  {
    field: "enabled",
    title: "状态",
    width: 90,
    render: (value: boolean) => (
      <Tag color={value ? "success" : "error"}>{value ? "正常" : "停用"}</Tag>
    ),
  },
];
