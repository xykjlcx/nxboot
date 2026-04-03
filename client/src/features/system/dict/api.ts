import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, post, put, del } from "@/app/request";
import type { PageResult } from "@/types/api";
import type { DictTypeVO, DictDataVO, DictTypeCommand, DictDataCommand, DictQuery } from "./types";

const KEYS = {
  types: ["system", "dict", "type"] as const,
  data: ["system", "dict", "data"] as const,
};

/* ---- 字典类型 ---- */

export function useDictTypes(params?: DictQuery) {
  return useQuery({
    queryKey: [...KEYS.types, params],
    queryFn: () => get<PageResult<DictTypeVO>>("/api/v1/system/dicts/types", params as Record<string, unknown>),
  });
}

export function useCreateDictType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DictTypeCommand.Create) => post<void>("/api/v1/system/dicts/types", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.types }),
  });
}

export function useUpdateDictType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DictTypeCommand.Update) => put<void>(`/api/v1/system/dicts/types/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.types }),
  });
}

export function useDeleteDictType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/dicts/types/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.types }),
  });
}

/* ---- 字典数据 ---- */

export function useDictData(dictType: string | null) {
  return useQuery({
    queryKey: [...KEYS.data, dictType],
    queryFn: () => get<DictDataVO[]>(`/api/v1/system/dicts/data/${dictType}`),
    enabled: !!dictType,
  });
}

export function useCreateDictData() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DictDataCommand.Create) => post<void>("/api/v1/system/dicts/data", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.data }),
  });
}

export function useUpdateDictData() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DictDataCommand.Update) => put<void>(`/api/v1/system/dicts/data/${data.id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.data }),
  });
}

export function useDeleteDictData() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => del<void>(`/api/v1/system/dicts/data/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.data }),
  });
}
