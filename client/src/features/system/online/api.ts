import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { get, del } from "@/app/request";
import type { OnlineUserVO } from "./types";

const KEYS = { list: ["system", "online-users"] as const };

/** 在线用户列表 */
export function useOnlineUsers() {
  return useQuery({
    queryKey: [...KEYS.list],
    queryFn: () => get<OnlineUserVO[]>("/api/v1/system/online-users"),
    refetchInterval: 30000, // 每 30 秒自动刷新
  });
}

/** 强制下线 */
export function useForceLogout() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (sessionId: string) => del<void>(`/api/v1/system/online-users/${sessionId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.list }),
  });
}
