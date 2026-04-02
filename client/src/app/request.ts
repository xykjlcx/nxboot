import axios, { type AxiosRequestConfig } from "axios";
import { message } from "antd";
import type { R } from "@/types/api";

const http = axios.create({
  baseURL: "",
  timeout: 15000,
});

// 请求拦截器：自动携带 Token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一错误处理
http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 401) {
        localStorage.removeItem("token");
        window.location.href = "/login";
        return Promise.reject(error);
      }
      const msg = (error.response?.data as R<unknown>)?.msg ?? "请求失败";
      message.error(msg);
    } else {
      message.error("网络异常");
    }
    return Promise.reject(error);
  },
);

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
