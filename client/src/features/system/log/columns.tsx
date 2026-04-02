import { Tag } from "antd";
import type { ColDef } from "ag-grid-community";
import type { OperationLogVO } from "./types";

export const logColumns: ColDef<OperationLogVO>[] = [
  { field: "module", headerName: "操作模块", width: 150 },
  { field: "operation", headerName: "操作类型", width: 120 },
  { field: "requestMethod", headerName: "请求方式", width: 100 },
  { field: "operator", headerName: "操作人员", width: 120 },
  { field: "operatorIp", headerName: "操作 IP", width: 140 },
  { field: "requestUrl", headerName: "请求地址", width: 250 },
  {
    field: "status",
    headerName: "状态",
    width: 90,
    cellRenderer: (params: { value: number }) => (
      <Tag color={params.value === 0 ? "success" : "error"}>{params.value === 0 ? "成功" : "失败"}</Tag>
    ),
  },
  {
    field: "duration",
    headerName: "耗时",
    width: 100,
    valueFormatter: (params) => `${params.value as number}ms`,
  },
  { field: "createTime", headerName: "操作时间", width: 180 },
];
