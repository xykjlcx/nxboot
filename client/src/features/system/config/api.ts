import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, post, put, del } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { ConfigVO, ConfigCommand, ConfigQuery } from "./types";

const KEYS = { list: ["system", "config"] as const };

export function useConfigs(params?: ConfigQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<ConfigVO>>("/api/v1/system/configs", params as Record<string, unknown>),
  });
}

export function useCreateConfig() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ConfigCommand.Create) => post<void>("/api/v1/system/configs", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

export function useUpdateConfig() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ConfigCommand.Update) => put<void>(`/api/v1/system/configs/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}

export function useDeleteConfig() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/configs/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}
