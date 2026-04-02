import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, post, put, del } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { UserVO, UserCommand, UserQuery } from "./types";

const KEYS = { list: ["system", "user"] as const };

/** 用户分页列表 */
export function useUsers(params?: UserQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<UserVO>>("/api/v1/system/users", params as Record<string, unknown>),
  });
}

/** 用户详情 */
export function useUser(id: string | null) {
  return useQuery({
    queryKey: [...KEYS.list, id],
    queryFn: () => get<UserVO>(`/api/v1/system/users/${id}`),
    enabled: !!id,
  });
}

/** 创建用户 */
export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: UserCommand.Create) => post<void>("/api/v1/system/users", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

/** 更新用户 */
export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: UserCommand.Update) => put<void>(`/api/v1/system/users/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

/** 删除用户 */
export function useDeleteUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/users/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}
