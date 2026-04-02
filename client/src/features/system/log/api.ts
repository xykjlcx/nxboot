import { useQuery } from "@tanstack/react-query";
import { get } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { OperationLogVO, LogQuery } from "./types";

const KEYS = { list: ["system", "log"] as const };

/** 操作日志分页列表 */
export function useLogs(params?: LogQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<OperationLogVO>>("/api/v1/system/logs", params as Record<string, unknown>),
  });
}
