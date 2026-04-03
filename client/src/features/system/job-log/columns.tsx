import { Tag } from "antd";
import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { JobLogVO } from "./types";

/** 格式化耗时：< 1s 显示 ms，否则显示秒 */
function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  return `${(ms / 1000).toFixed(1)}s`;
}

export const jobLogColumns: NxColumn<JobLogVO>[] = [
  { field: "jobName", title: "任务名称", width: 180 },
  { field: "jobGroup", title: "任务分组", width: 120 },
  { field: "invokeTarget", title: "调用目标", width: 250 },
  {
    field: "status",
    title: "执行状态",
    width: 100,
    render: (value: number) => (
      <Tag color={value === 0 ? "success" : "error"}>{value === 0 ? "成功" : "失败"}</Tag>
    ),
  },
  {
    field: "duration",
    title: "耗时",
    width: 100,
    render: (value: number) => (value != null ? formatDuration(value) : "-"),
  },
  {
    field: "startTime",
    title: "开始时间",
    width: 170,
    render: (v: string) => (v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-"),
  },
  {
    field: "endTime",
    title: "结束时间",
    width: 170,
    render: (v: string) => (v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-"),
  },
  {
    field: "errorMsg",
    title: "错误信息",
    width: 250,
    ellipsis: true,
    render: (v: string) => v || "-",
  },
];
