/** 菜单列表项（树形结构） */
export interface MenuVO {
  id: string;
  parentId: string;
  menuName: string;
  menuType: string; // M=目录, C=菜单, F=按钮
  path: string;
  component: string;
  permission: string;
  icon: string;
  sortOrder: number;
  visible: boolean;
  enabled: boolean;
  createTime: string;
  children?: MenuVO[];
}

/** 菜单命令 */
export namespace MenuCommand {
  export interface Create {
    menuName: string;
    parentId?: string;
    menuType: string;
    path?: string;
    component?: string;
    permission?: string;
    icon?: string;
    sortOrder?: number;
    visible?: boolean;
    enabled?: boolean;
  }
  export interface Update {
    id: string;
    menuName?: string;
    parentId?: string;
    menuType?: string;
    path?: string;
    component?: string;
    permission?: string;
    icon?: string;
    sortOrder?: number;
    visible?: boolean;
    enabled?: boolean;
  }
}
