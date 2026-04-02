import type { PageQuery } from "@/types/api";

/** 操作日志 */
export interface OperationLogVO {
  id: string;
  module: string;
  operation: string;
  method: string;
  requestUrl: string;
  requestMethod: string;
  requestParams: string;
  responseBody: string;
  operator: string;
  operatorIp: string;
  status: number;
  errorMsg: string;
  duration: number;
  createTime: string;
}

/** 日志查询参数 */
export interface LogQuery extends PageQuery {
  keyword?: string;
  status?: number;
}
