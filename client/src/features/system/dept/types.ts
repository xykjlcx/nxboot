/** 部门视图对象 */
export interface DeptVO {
  id: string;
  parentId: string;
  deptName: string;
  sortOrder: number;
  leader: string;
  phone: string;
  email: string;
  enabled: boolean;
  createTime: string;
  children: DeptVO[] | null;
}

/** 部门命令 */
export namespace DeptCommand {
  export interface Create {
    parentId: string;
    deptName: string;
    sortOrder?: number;
    leader?: string;
    phone?: string;
    email?: string;
  }
  export interface Update {
    parentId?: string;
    deptName?: string;
    sortOrder?: number;
    leader?: string;
    phone?: string;
    email?: string;
    enabled?: boolean;
  }
}
