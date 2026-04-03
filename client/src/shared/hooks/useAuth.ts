import { create } from "zustand";
import { get } from "@/app/request";

export interface UserInfo {
  userId: string;
  username: string;
  permissions: string[];
}

/** 后端菜单树节点 */
export interface MenuVO {
  id: number;
  parentId: number;
  menuName: string;
  menuType: string; // M=目录, C=菜单, F=按钮
  path: string | null;
  component: string | null;
  permission: string | null;
  icon: string | null;
  sortOrder: number;
  visible: boolean;
  enabled: boolean;
  createTime: string;
  children: MenuVO[] | null;
}

interface AuthState {
  token: string | null;
  user: UserInfo | null;
  menus: MenuVO[];
  /** 菜单是否已加载完成（区分"未加载"和"真的没权限"） */
  menusFetched: boolean;
  setToken: (token: string, refreshToken?: string) => void;
  fetchUser: () => Promise<void>;
  fetchMenus: () => Promise<void>;
  logout: () => void;
}

export const useAuth = create<AuthState>((set) => ({
  token: localStorage.getItem("token"),
  user: null,
  menus: [],
  menusFetched: false,

  setToken: (token: string, refreshToken?: string) => {
    localStorage.setItem("token", token);
    if (refreshToken) {
      localStorage.setItem("refreshToken", refreshToken);
    }
    set({ token });
  },

  fetchUser: async () => {
    const user = await get<UserInfo>("/api/v1/auth/info");
    set({ user });
  },

  fetchMenus: async () => {
    const menus = await get<MenuVO[]>("/api/v1/auth/menus");
    set({ menus, menusFetched: true });
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    set({ token: null, user: null, menus: [] });
    window.location.href = "/login";
  },
}));
