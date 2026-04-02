import type { PageQuery } from "@/types/api";

/** 文件信息 */
export interface FileVO {
  id: string;
  fileName: string;
  originalName: string;
  filePath: string;
  fileSize: number;
  fileType: string;
  createBy: string;
  createTime: string;
}

/** 文件查询参数 */
export interface FileQuery extends PageQuery {
  keyword?: string;
}
