import { create } from "zustand";
import { get } from "@/app/request";

export interface UserInfo {
  userId: string;
  username: string;
  permissions: string[];
}

interface AuthState {
  token: string | null;
  user: UserInfo | null;
  setToken: (token: string) => void;
  fetchUser: () => Promise<void>;
  logout: () => void;
}

export const useAuth = create<AuthState>((set) => ({
  token: localStorage.getItem("token"),
  user: null,

  setToken: (token: string) => {
    localStorage.setItem("token", token);
    set({ token });
  },

  fetchUser: async () => {
    const user = await get<UserInfo>("/api/v1/auth/me");
    set({ user });
  },

  logout: () => {
    localStorage.removeItem("token");
    set({ token: null, user: null });
    window.location.href = "/login";
  },
}));
