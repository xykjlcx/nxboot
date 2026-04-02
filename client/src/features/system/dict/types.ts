import type { PageQuery } from "@/types/api";

/** 字典类型 */
export interface DictTypeVO {
  id: string;
  dictType: string;
  dictName: string;
  enabled: boolean;
  remark: string;
  createTime: string;
}

/** 字典数据 */
export interface DictDataVO {
  id: string;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sortOrder: number;
  enabled: boolean;
  remark: string;
  createTime: string;
}

/** 字典类型命令 */
export namespace DictTypeCommand {
  export interface Create {
    dictName: string;
    dictType: string;
    remark?: string;
  }
  export interface Update {
    id: string;
    dictName?: string;
    enabled?: boolean;
    remark?: string;
  }
}

/** 字典数据命令 */
export namespace DictDataCommand {
  export interface Create {
    dictType: string;
    dictLabel: string;
    dictValue: string;
    sortOrder?: number;
    remark?: string;
  }
  export interface Update {
    id: string;
    dictLabel?: string;
    dictValue?: string;
    sortOrder?: number;
    enabled?: boolean;
    remark?: string;
  }
}

/** 字典查询参数 */
export interface DictQuery extends PageQuery {
  keyword?: string;
}
