import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '../types/api'

export const http = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  try {
    const response = await http.request<ApiResponse<T>>(config)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '请求失败')
    }
    return response.data.data as T
  } catch (error) {
    if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
      const message = error.response?.data?.message
      throw new Error(message || (error.response ? '请求失败' : '网络连接失败'))
    }
    throw error
  }
}

export function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : '请求失败'
}