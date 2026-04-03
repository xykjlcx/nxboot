import { useCallback, useRef, useState, type ReactNode } from "react";
import { Badge, Button, Space } from "antd";
import { SearchOutlined, ReloadOutlined } from "@ant-design/icons";

interface NxFilterProps<T extends object> {
  /** 初始筛选值 */
  initialValues: T;
  /** 筛选变更回调（延迟触发） */
  onSearch: (values: T) => void;
  /** 延迟时间(ms) */
  delay?: number;
  /** 渲染筛选表单项 */
  children: (values: T, onChange: (patch: Partial<T>) => void) => ReactNode;
}

/** 筛选栏组件：延迟触发搜索 + 筛选计数 */
export function NxFilter<T extends object>({
  initialValues,
  onSearch,
  delay = 300,
  children,
}: NxFilterProps<T>) {
  const [values, setValues] = useState<T>(initialValues);
  const timerRef = useRef<ReturnType<typeof setTimeout>>();

  // 计算有效筛选项数量（排除与初始值相同的字段，避免 pageNum/pageSize 被计入）
  const activeCount = Object.entries(values as Record<string, unknown>).filter(
    ([key, v]) => {
      const init = (initialValues as Record<string, unknown>)[key];
      return v !== init && v !== undefined && v !== null && v !== "";
    },
  ).length;

  const handleChange = useCallback(
    (patch: Partial<T>) => {
      setValues((prev) => {
        const next = { ...prev, ...patch };
        // 延迟触发搜索
        clearTimeout(timerRef.current);
        timerRef.current = setTimeout(() => onSearch(next), delay);
        return next;
      });
    },
    [onSearch, delay],
  );

  const handleReset = useCallback(() => {
    setValues(initialValues);
    onSearch(initialValues);
  }, [initialValues, onSearch]);

  const handleSearch = useCallback(() => {
    clearTimeout(timerRef.current);
    onSearch(values);
  }, [onSearch, values]);

  return (
    <Space wrap style={{ marginBottom: 16 }}>
      {children(values, handleChange)}
      <Badge count={activeCount} size="small">
        <Button icon={<SearchOutlined />} type="primary" onClick={handleSearch}>
          搜索
        </Button>
      </Badge>
      <Button icon={<ReloadOutlined />} onClick={handleReset}>
        重置
      </Button>
    </Space>
  );
}
