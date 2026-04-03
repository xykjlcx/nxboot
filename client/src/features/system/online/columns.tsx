import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { OnlineUserVO } from "./types";

export const onlineUserColumns: NxColumn<OnlineUserVO>[] = [
  { field: "username", title: "用户名", width: 120 },
  { field: "ip", title: "登录 IP", width: 150 },
  {
    field: "userAgent",
    title: "浏览器/终端",
    width: 250,
    ellipsis: true,
    render: (v: string) => parseUserAgent(v),
  },
  {
    field: "loginTime",
    title: "登录时间",
    width: 170,
    render: (v: number) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-",
  },
];

/** 简化 User-Agent 显示 */
function parseUserAgent(ua: string): string {
  if (!ua) return "-";
  // 提取浏览器信息
  const chrome = ua.match(/Chrome\/([\d.]+)/);
  const firefox = ua.match(/Firefox\/([\d.]+)/);
  const safari = ua.match(/Safari\/([\d.]+)/);
  const edge = ua.match(/Edg\/([\d.]+)/);

  if (edge) return `Edge ${edge[1]}`;
  if (chrome) return `Chrome ${chrome[1]}`;
  if (firefox) return `Firefox ${firefox[1]}`;
  if (safari) return `Safari ${safari[1]}`;
  // 超过 50 字符截断
  return ua.length > 50 ? `${ua.slice(0, 50)}...` : ua;
}
