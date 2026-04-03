import { useCallback, useRef, useState } from "react";

/**
 * 防重复提交 hook——确保同一异步操作在完成前不会被再次触发。
 *
 * @example
 * const [submit, submitting] = useGuardedSubmit(async () => {
 *   await createUser.mutateAsync(values);
 *   message.success("创建成功");
 * });
 * <Button loading={submitting} onClick={submit}>提交</Button>
 */
export function useGuardedSubmit(fn: () => Promise<void>) {
  const [loading, setLoading] = useState(false);
  const lockRef = useRef(false);

  const execute = useCallback(async () => {
    if (lockRef.current) return;
    lockRef.current = true;
    setLoading(true);
    try {
      await fn();
    } finally {
      lockRef.current = false;
      setLoading(false);
    }
  }, [fn]);

  return [execute, loading] as const;
}
