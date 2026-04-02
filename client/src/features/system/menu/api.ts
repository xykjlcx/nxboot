import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, post, put, del } from "@/app/request";
import type { MenuVO, MenuCommand } from "./types";

const KEYS = { list: ["system", "menu"] as const };

/** 菜单树列表 */
export function useMenuTree() {
  return useQuery({
    queryKey: [...KEYS.list, "tree"],
    queryFn: () => get<MenuVO[]>("/api/v1/system/menus/tree"),
  });
}

/** 菜单详情 */
export function useMenu(id: string | null) {
  return useQuery({
    queryKey: [...KEYS.list, id],
    queryFn: () => get<MenuVO>(`/api/v1/system/menus/${id}`),
    enabled: !!id,
  });
}

/** 创建菜单 */
export function useCreateMenu() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: MenuCommand.Create) => post<void>("/api/v1/system/menus", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

/** 更新菜单 */
export function useUpdateMenu() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: MenuCommand.Update) => put<void>(`/api/v1/system/menus/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

/** 删除菜单 */
export function useDeleteMenu() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/menus/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}
