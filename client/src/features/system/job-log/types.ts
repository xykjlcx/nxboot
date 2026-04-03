import type { PageQuery } from "@/types/api";

/** 定时任务执行日志 */
export interface JobLogVO {
  id: string;
  jobId: string;
  jobName: string;
  jobGroup: string;
  invokeTarget: string;
  status: number;
  errorMsg: string;
  startTime: string;
  endTime: string;
  duration: number;
}

/** 执行日志查询参数 */
export interface JobLogQuery extends PageQuery {
  jobId?: string;
  keyword?: string;
  status?: number;
}
