export interface CpuInfo {
  name: string;
  cores: number;
  usage: number;
}
export interface MemoryInfo {
  total: string;
  used: string;
  free: string;
  usage: number;
}
export interface JvmInfo {
  heapUsed: string;
  heapMax: string;
  heapUsage: number;
  javaVersion: string;
  uptime: string;
}
export interface DiskInfo {
  total: string;
  used: string;
  free: string;
  usage: number;
}
export interface MonitorData {
  cpu: CpuInfo;
  memory: MemoryInfo;
  jvm: JvmInfo;
  disk: DiskInfo;
}
