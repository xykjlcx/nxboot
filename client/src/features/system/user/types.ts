import type { PageQuery } from "@/types/api";

/** 用户列表项 */
export interface UserVO {
  id: string;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
  enabled: boolean;
  remark: string;
  createTime: string;
  roleIds: string[];
}

/** 用户命令 */
export namespace UserCommand {
  export interface Create {
    username: string;
    password: string;
    nickname?: string;
    email?: string;
    phone?: string;
    remark?: string;
    roleIds?: string[];
  }
  export interface Update {
    id: string;
    nickname?: string;
    email?: string;
    phone?: string;
    remark?: string;
    enabled?: boolean;
    roleIds?: string[];
  }
  export interface ResetPassword {
    id: string;
    newPassword: string;
  }
}

/** 用户查询参数 */
export interface UserQuery extends PageQuery {
  keyword?: string;
}
