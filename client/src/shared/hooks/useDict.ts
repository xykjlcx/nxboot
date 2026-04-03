import { useQuery } from "@tanstack/react-query";
import { get } from "@/app/request";
import type { DictDataVO } from "@/features/system/dict/types";

/**
 * 通用字典数据 hook——任意页面可用，长缓存（字典数据极少变动）。
 *
 * @example
 * const { items, labelOf } = useDict("sys_status");
 * // items: DictDataVO[]
 * // labelOf("1") → "正常"
 */
export function useDict(dictType: string) {
  const { data: items = [] } = useQuery({
    queryKey: ["system", "dict", "shared", dictType],
    queryFn: () => get<DictDataVO[]>(`/api/v1/system/dicts/data/${dictType}`),
    staleTime: 5 * 60 * 1000, // 5 分钟缓存
    enabled: !!dictType,
  });

  /** 根据 dictValue 查 dictLabel */
  const labelOf = (value: string): string => {
    const item = items.find((d) => d.dictValue === value);
    return item?.dictLabel ?? value;
  };

  /** 转为 Select/Radio 的 options 格式 */
  const options = items.map((d) => ({
    label: d.dictLabel,
    value: d.dictValue,
  }));

  return { items, labelOf, options };
}
