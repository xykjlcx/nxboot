import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, post, put, del } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { RoleVO, RoleCommand, RoleQuery } from "./types";

const KEYS = { list: ["system", "role"] as const };

/** 角色分页列表 */
export function useRoles(params?: RoleQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<RoleVO>>("/api/v1/system/roles", params as Record<string, unknown>),
  });
}

/** 角色详情 */
export function useRole(id: string | null) {
  return useQuery({
    queryKey: [...KEYS.list, id],
    queryFn: () => get<RoleVO>(`/api/v1/system/roles/${id}`),
    enabled: !!id,
  });
}

/** 创建角色 */
export function useCreateRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: RoleCommand.Create) => post<void>("/api/v1/system/roles", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

/** 更新角色 */
export function useUpdateRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: RoleCommand.Update) => put<void>(`/api/v1/system/roles/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

/** 删除角色 */
export function useDeleteRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/roles/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}
