import { useAuth } from "./useAuth";

/** 判断当前用户是否拥有指定权限 */
export function usePerm() {
  const user = useAuth((s) => s.user);

  const has = (permission: string): boolean => {
    if (!user) return false;
    // admin 拥有所有权限
    if (user.permissions.includes("*:*:*")) return true;
    return user.permissions.includes(permission);
  };

  return { has };
}
