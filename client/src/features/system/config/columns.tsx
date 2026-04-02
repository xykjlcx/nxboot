import type { ColDef } from "ag-grid-community";
import type { ConfigVO } from "./types";

export const configColumns: ColDef<ConfigVO>[] = [
  { field: "configName", headerName: "配置名称", width: 180 },
  { field: "configKey", headerName: "配置键", width: 200 },
  { field: "configValue", headerName: "配置值", width: 200 },
  { field: "remark", headerName: "备注", width: 200 },
  { field: "createTime", headerName: "创建时间", width: 180 },
];
