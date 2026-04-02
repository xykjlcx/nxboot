import { Tag } from "antd";
import type { ColDef } from "ag-grid-community";
import type { JobVO } from "./types";

export const jobColumns: ColDef<JobVO>[] = [
  { field: "jobName", headerName: "任务名称", width: 180 },
  { field: "jobGroup", headerName: "任务分组", width: 120 },
  { field: "invokeTarget", headerName: "调用目标", width: 250 },
  { field: "cronExpression", headerName: "Cron 表达式", width: 160 },
  {
    field: "enabled",
    headerName: "状态",
    width: 90,
    cellRenderer: (params: { value: boolean }) => (
      <Tag color={params.value ? "success" : "error"}>{params.value ? "运行中" : "暂停"}</Tag>
    ),
  },
  { field: "remark", headerName: "备注", width: 200 },
  { field: "createTime", headerName: "创建时间", width: 180 },
];
