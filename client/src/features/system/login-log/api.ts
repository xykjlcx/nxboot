import { useQuery } from "@tanstack/react-query";
import { get } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { LoginLogVO, LoginLogQuery } from "./types";

const KEYS = { list: ["system", "login-log"] as const };

/** 登录日志分页列表 */
export function useLoginLogs(params?: LoginLogQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<LoginLogVO>>("/api/v1/system/login-logs", params as Record<string, unknown>),
  });
}
