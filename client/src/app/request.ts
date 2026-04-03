import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig } from "axios";
import { message } from "antd";
import type { R } from "@/types/api";
import { useAuth } from "@/shared/hooks/useAuth";

const http = axios.create({
  baseURL: "",
  timeout: 15000,
});

// ---------- Token 刷新队列 ----------

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (reason: unknown) => void;
  config: InternalAxiosRequestConfig;
}> = [];

/** 处理队列中等待刷新的请求 */
function processQueue(error: Error | null, token: string | null) {
  for (const { resolve, reject, config } of failedQueue) {
    if (error) {
      reject(error);
    } else {
      config.headers.Authorization = `Bearer ${token}`;
      resolve(http.request(config));
    }
  }
  failedQueue = [];
}

/** 清除认证状态并跳转登录页（通过 Zustand store 统一管理） */
function forceLogout() {
  useAuth.getState().logout();
}

// ---------- 拦截器 ----------

// 请求拦截器：自动携带 Token + Accept-Language
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // 国际化：将当前语言环境传递给后端
  const locale = localStorage.getItem("nx-locale") ?? "zh-CN";
  config.headers["Accept-Language"] = locale;
  return config;
});

// 响应拦截器：401 时尝试刷新令牌，其他错误统一提示
http.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (!axios.isAxiosError(error)) {
      message.error("网络异常");
      return Promise.reject(error);
    }

    const originalConfig = error.config as InternalAxiosRequestConfig | undefined;

    // 非 401 错误，直接提示
    if (error.response?.status !== 401) {
      const msg = (error.response?.data as R<unknown>)?.msg ?? "请求失败";
      message.error(msg);
      return Promise.reject(error);
    }

    // 刷新接口本身返回 401，直接跳登录
    if (originalConfig?.url === "/api/v1/auth/refresh") {
      forceLogout();
      return Promise.reject(error);
    }

    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
      forceLogout();
      return Promise.reject(error);
    }

    // 已有刷新请求在进行中，将当前请求排队等待
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject, config: originalConfig! });
      });
    }

    isRefreshing = true;

    try {
      // 用原始 axios 发起刷新请求，避免触发拦截器死循环
      const res = await axios.post<R<{ token: string; refreshToken: string }>>(
        "/api/v1/auth/refresh",
        { refreshToken },
      );
      const { token: newToken, refreshToken: newRefresh } = res.data.data;
      localStorage.setItem("token", newToken);
      localStorage.setItem("refreshToken", newRefresh);

      // 重放队列中的请求
      processQueue(null, newToken);

      // 重试原始请求
      originalConfig!.headers.Authorization = `Bearer ${newToken}`;
      return http.request(originalConfig!);
    } catch (refreshError) {
      processQueue(new Error("令牌刷新失败"), null);
      forceLogout();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

// ---------- 通用请求方法 ----------

/** 自动解包 R<T>，直接返回 data */
async function unwrap<T>(config: AxiosRequestConfig): Promise<T> {
  const res = await http.request<R<T>>(config);
  if (res.data.code !== 200) {
    message.error(res.data.msg ?? "操作失败");
    return Promise.reject(new Error(res.data.msg));
  }
  return res.data.data;
}

export function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return unwrap<T>({ method: "GET", url, params });
}

export function post<T>(url: string, data?: unknown): Promise<T> {
  return unwrap<T>({ method: "POST", url, data });
}

export function put<T>(url: string, data?: unknown): Promise<T> {
  return unwrap<T>({ method: "PUT", url, data });
}

export function del<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return unwrap<T>({ method: "DELETE", url, params });
}

export { http };
