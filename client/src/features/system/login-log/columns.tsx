import { Tag } from "antd";
import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { LoginLogVO } from "./types";

export const loginLogColumns: NxColumn<LoginLogVO>[] = [
  { field: "username", title: "用户名", width: 120 },
  { field: "ip", title: "登录 IP", width: 140 },
  {
    field: "status",
    title: "状态",
    width: 90,
    render: (value: number) => (
      <Tag color={value === 0 ? "success" : "error"}>{value === 0 ? "成功" : "失败"}</Tag>
    ),
  },
  { field: "message", title: "提示消息", width: 180 },
  { field: "userAgent", title: "浏览器", width: 250, ellipsis: true },
  {
    field: "loginTime",
    title: "登录时间",
    width: 170,
    render: (v: string) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-",
  },
];
