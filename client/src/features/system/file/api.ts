import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, del } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { FileVO, FileQuery } from "./types";

const KEYS = { list: ["system", "file"] as const };

export function useFiles(params?: FileQuery) {
  return useQuery({
    queryKey: [...KEYS.list, params],
    queryFn: () => get<PageResult<FileVO>>("/api/v1/system/files", params as Record<string, unknown>),
  });
}

export function useDeleteFile() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/files/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}
