import type { ColDef } from "ag-grid-community";
import type { FileVO } from "./types";

/** 格式化文件大小 */
function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

export const fileColumns: ColDef<FileVO>[] = [
  { field: "originalName", headerName: "文件名", width: 250 },
  { field: "fileType", headerName: "文件类型", width: 120 },
  {
    field: "fileSize",
    headerName: "文件大小",
    width: 120,
    valueFormatter: (params) => formatSize(params.value as number),
  },
  { field: "filePath", headerName: "存储路径", width: 300 },
  { field: "createBy", headerName: "上传者", width: 120 },
  { field: "createTime", headerName: "上传时间", width: 180 },
];
