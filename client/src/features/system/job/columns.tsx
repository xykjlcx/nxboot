import { Tag } from "antd";
import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { JobVO } from "./types";

export const jobColumns: NxColumn<JobVO>[] = [
  { field: "jobName", title: "任务名称", width: 180 },
  { field: "jobGroup", title: "任务分组", width: 120 },
  { field: "invokeTarget", title: "调用目标", width: 250 },
  { field: "cronExpression", title: "Cron 表达式", width: 160 },
  {
    field: "enabled",
    title: "状态",
    width: 90,
    render: (value: boolean) => (
      <Tag color={value ? "success" : "error"}>{value ? "运行中" : "暂停"}</Tag>
    ),
  },
  { field: "remark", title: "备注", width: 200 },
  { field: "createTime", title: "创建时间", width: 170, render: (v: string) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-" },
];
