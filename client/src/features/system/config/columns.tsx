import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { ConfigVO } from "./types";

export const configColumns: NxColumn<ConfigVO>[] = [
  { field: "configName", title: "配置名称", width: 180 },
  { field: "configKey", title: "配置键", width: 200 },
  { field: "configValue", title: "配置值", width: 200 },
  { field: "remark", title: "备注", width: 200 },
  { field: "createTime", title: "创建时间", width: 170, render: (v: string) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-" },
];
