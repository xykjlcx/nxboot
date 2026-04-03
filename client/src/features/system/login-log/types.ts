import type { PageQuery } from "@/types/api";

/** 登录日志 */
export interface LoginLogVO {
  id: string;
  username: string;
  ip: string;
  userAgent: string;
  status: number;
  message: string;
  loginTime: string;
}

/** 登录日志查询参数 */
export interface LoginLogQuery extends PageQuery {
  keyword?: string;
  status?: number;
  beginTime?: string;
  endTime?: string;
}
