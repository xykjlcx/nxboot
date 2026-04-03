import { useState, useEffect, useMemo } from "react";
import { Select, Spin, type SelectProps } from "antd";

interface ApiSelectProps extends Omit<SelectProps, "options" | "loading"> {
  /** 远程数据请求函数 */
  request: () => Promise<{ label: string; value: string | number }[]>;
  /** 缓存 key（必须唯一标识数据源，避免 minification 后函数 toString 碰撞） */
  cacheKey?: string;
  /** 缓存时间（ms），默认 5 分钟。0 表示不缓存 */
  cacheTime?: number;
}

// 简单的内存缓存
const cache = new Map<string, { data: { label: string; value: string | number }[]; timestamp: number }>();

/**
 * 远程数据源 Select——自动加载远程选项数据，带 loading 和内存缓存。
 *
 * @example
 * <ApiSelect
 *   request={() => get<Option[]>("/api/v1/system/roles/options")}
 *   placeholder="请选择角色"
 *   mode="multiple"
 * />
 */
export function ApiSelect({ request, cacheKey: explicitKey, cacheTime = 5 * 60 * 1000, ...rest }: ApiSelectProps) {
  const [options, setOptions] = useState<{ label: string; value: string | number }[]>([]);
  const [loading, setLoading] = useState(false);

  const cacheKey = useMemo(() => explicitKey ?? request.toString().slice(0, 200), [explicitKey, request]);

  useEffect(() => {
    // 检查缓存
    if (cacheTime > 0) {
      const cached = cache.get(cacheKey);
      if (cached && Date.now() - cached.timestamp < cacheTime) {
        setOptions(cached.data);
        return;
      }
    }

    let cancelled = false;
    setLoading(true);
    request()
      .then((data) => {
        if (!cancelled) {
          setOptions(data);
          if (cacheTime > 0) {
            cache.set(cacheKey, { data, timestamp: Date.now() });
          }
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => { cancelled = true; };
  }, [request, cacheKey, cacheTime]);

  return (
    <Select
      options={options}
      loading={loading}
      notFoundContent={loading ? <Spin size="small" /> : undefined}
      showSearch
      optionFilterProp="label"
      {...rest}
    />
  );
}
