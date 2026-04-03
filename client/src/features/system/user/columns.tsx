import { Tag } from "antd";
import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { UserVO } from "./types";

export const userColumns: NxColumn<UserVO>[] = [
  { field: "username", title: "用户名", width: 120 },
  { field: "nickname", title: "昵称", width: 120 },
  { field: "email", title: "邮箱", width: 180 },
  { field: "phone", title: "手机号", width: 140 },
  {
    field: "enabled",
    title: "状态",
    width: 90,
    render: (value: boolean) => (
      <Tag color={value ? "success" : "error"}>{value ? "正常" : "停用"}</Tag>
    ),
  },
  { field: "createTime", title: "创建时间", width: 170, render: (v: string) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-" },
];
