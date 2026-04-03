import { useQuery } from "@tanstack/react-query";
import { get } from "@/app/request";
import type { MonitorData } from "./types";

export function useMonitorServer() {
  return useQuery({
    queryKey: ["monitor", "server"],
    queryFn: () => get<MonitorData>("/api/v1/system/monitor/server"),
    refetchInterval: 10_000,
  });
}
