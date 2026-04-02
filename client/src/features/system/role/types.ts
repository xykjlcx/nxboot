import type { PageQuery } from "@/types/api";

/** 角色列表项 */
export interface RoleVO {
  id: string;
  roleKey: string;
  roleName: string;
  sortOrder: number;
  enabled: boolean;
  remark: string;
  createTime: string;
  menuIds: string[];
}

/** 角色命令 */
export namespace RoleCommand {
  export interface Create {
    roleKey: string;
    roleName: string;
    sortOrder?: number;
    remark?: string;
    menuIds?: string[];
  }
  export interface Update {
    id: string;
    roleName?: string;
    sortOrder?: number;
    enabled?: boolean;
    remark?: string;
    menuIds?: string[];
  }
}

/** 角色查询参数 */
export interface RoleQuery extends PageQuery {
  keyword?: string;
}
