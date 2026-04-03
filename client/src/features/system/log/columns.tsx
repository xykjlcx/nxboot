import { Tag } from "antd";
import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { OperationLogVO } from "./types";

export const logColumns: NxColumn<OperationLogVO>[] = [
  { field: "module", title: "操作模块", width: 150 },
  { field: "operation", title: "操作类型", width: 120 },
  { field: "requestMethod", title: "请求方式", width: 100 },
  { field: "operator", title: "操作人员", width: 120 },
  { field: "operatorIp", title: "操作 IP", width: 140 },
  { field: "requestUrl", title: "请求地址", width: 250 },
  {
    field: "status",
    title: "状态",
    width: 90,
    render: (value: number) => (
      <Tag color={value === 0 ? "success" : "error"}>{value === 0 ? "成功" : "失败"}</Tag>
    ),
  },
  {
    field: "duration",
    title: "耗时",
    width: 100,
    render: (value: number) => `${value}ms`,
  },
  { field: "createTime", title: "操作时间", width: 170, render: (v: string) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-" },
];
