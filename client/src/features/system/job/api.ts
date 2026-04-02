import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, post, put, del } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { JobVO, JobCommand, JobQuery } from "./types";

const KEYS = { list: ["system", "job"] as const };

export function useJobs(params?: JobQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<JobVO>>("/api/v1/system/jobs", params as Record<string, unknown>),
  });
}

export function useCreateJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: JobCommand.Create) => post<void>("/api/v1/system/jobs", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

export function useUpdateJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: JobCommand.Update) => put<void>(`/api/v1/system/jobs/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

export function useDeleteJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/jobs/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

/** 立即执行一次 */
export function useRunJob() {
  return useMutation({
    mutationFn: (id: string) => post<void>(`/api/v1/system/jobs/${id}/run`),
  });
}
