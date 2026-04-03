import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, post, put, del } from "@/app/request";
import type { DeptVO, DeptCommand } from "./types";

const KEYS = {
  tree: ["system", "dept", "tree"] as const,
};

/** 查询部门树 */
export function useDeptTree() {
  return useQuery({
    queryKey: [...KEYS.tree],
    queryFn: () => get<DeptVO[]>("/api/v1/system/depts/tree"),
  });
}

/** 新增部门 */
export function useCreateDept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DeptCommand.Create) => post<void>("/api/v1/system/depts", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.tree }),
  });
}

/** 更新部门 */
export function useUpdateDept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DeptCommand.Update & { id: string }) =>
      put<void>(`/api/v1/system/depts/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.tree }),
  });
}

/** 删除部门 */
export function useDeleteDept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/depts/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.tree }),
  });
}
