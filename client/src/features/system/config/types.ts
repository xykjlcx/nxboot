import type { PageQuery } from "@/types/api";

/** 系统配置项 */
export interface ConfigVO {
  id: string;
  configKey: string;
  configValue: string;
  configName: string;
  remark: string;
  createTime: string;
}

/** 系统配置命令 */
export namespace ConfigCommand {
  export interface Create {
    configName: string;
    configKey: string;
    configValue: string;
    remark?: string;
  }
  export interface Update {
    id: string;
    configName?: string;
    configKey?: string;
    configValue?: string;
    remark?: string;
  }
}

/** 配置查询参数 */
export interface ConfigQuery extends PageQuery {
  keyword?: string;
}
