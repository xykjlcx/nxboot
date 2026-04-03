import dayjs from "dayjs";
import type { NxColumn } from "@/shared/components/NxTable";
import type { FileVO } from "./types";

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

export const fileColumns: NxColumn<FileVO>[] = [
  { field: "originalName", title: "文件名", width: 250 },
  { field: "fileType", title: "文件类型", width: 120 },
  {
    field: "fileSize",
    title: "文件大小",
    width: 120,
    render: (value: number) => formatSize(value),
  },
  { field: "filePath", title: "存储路径", width: 300 },
  { field: "createBy", title: "上传者", width: 120 },
  { field: "createTime", title: "上传时间", width: 170, render: (v: string) => v ? dayjs(v).format("YYYY-MM-DD HH:mm:ss") : "-" },
];
