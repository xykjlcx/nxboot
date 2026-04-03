import { useQuery } from "@tanstack/react-query";
import { get } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { JobLogVO, JobLogQuery } from "./types";

const KEYS = { list: ["system", "job-log"] as const };

/** 任务执行日志分页列表 */
export function useJobLogs(params?: JobLogQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<JobLogVO>>("/api/v1/system/job-logs", params as Record<string, unknown>),
  });
}
