import type { PageQuery } from "@/types/api";

/** 定时任务 */
export interface JobVO {
  id: string;
  jobName: string;
  jobGroup: string;
  invokeTarget: string;
  cronExpression: string;
  misfirePolicy: number;
  concurrent: boolean;
  enabled: boolean;
  remark: string;
  createTime: string;
}

/** 定时任务命令 */
export namespace JobCommand {
  export interface Create {
    jobName: string;
    jobGroup?: string;
    invokeTarget: string;
    cronExpression: string;
    misfirePolicy?: number;
    concurrent?: boolean;
    remark?: string;
  }
  export interface Update {
    id: string;
    jobName?: string;
    jobGroup?: string;
    invokeTarget?: string;
    cronExpression?: string;
    misfirePolicy?: number;
    concurrent?: boolean;
    enabled?: boolean;
    remark?: string;
  }
}

/** 任务查询参数 */
export interface JobQuery extends PageQuery {
  keyword?: string;
}
