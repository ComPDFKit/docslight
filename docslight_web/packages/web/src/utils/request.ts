import axios from 'axios'
import type { AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useCookies } from 'vue3-cookies'
import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import { i18n } from '../i18n'
import { syncFixedUserSession } from './mockAuth'

const { cookies } = useCookies()
const { t } = i18n.global


// 定义接口，可根据你的项目需求调整
interface UserCookie {
  token?: string
  [key: string]: any
}

const getUser = (): UserCookie | null => {
  const cookieUser = cookies.get('idp_user') || JSON.parse(sessionStorage.getItem('idp_user') || '{}')
  if (!cookieUser) return null
  if (typeof cookieUser === 'string') {
    try {
      return JSON.parse(cookieUser) as UserCookie
    } catch {
      return null
    }
  }
  return cookieUser as UserCookie
}

// 创建axios实例
const instance: AxiosInstance = axios.create({
  timeout: 1200000,
  withCredentials: false,
})

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    const user = getUser()
    if (user?.token && config.headers) {
      config.headers['Authorization'] = user.token
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.data?.code === 401) {
      syncFixedUserSession()
    }
    return response
  },
  (error: AxiosError) => {
    if (error.response?.data) {
      // 服务器有返回错误信息
      return Promise.reject(error)
    }
    if (error.request) {
      // 网络错误提示
      ElMessage({
        duration: 5000,
        message: t('common.networkError'),
        type: 'error',
      })
    }
    // if (error.status === 500) {
    //   cookies.remove('idp_user')
    //   location.href = '/login'
    // }
    console.error(error.message)
    return Promise.reject(error)
  }
)

// 定义通用请求返回类型，方便泛型使用
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// post请求函数，支持泛型
export function post<T = any>(
  url: string,
  data: any = {},
  params: Record<string, any> = {},
  config?: InternalAxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return instance({
    method: 'post',
    url,
    data,
    params,
    ...config,
  })
}

// put请求函数，支持泛型
export function put<T = any>(
  url: string,
  data: any = {},
  params: Record<string, any> = {},
  config?: InternalAxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return instance({
    method: 'put',
    url,
    data,
    params,
    ...config,
  })
}

// get请求函数，支持泛型
export function get<T = any>(
  url: string,
  config?: Partial<InternalAxiosRequestConfig>
): Promise<AxiosResponse<ApiResponse<T>>> {
  return instance({
    method: 'get',
    url,
    ...config,
  })
}

// delete请求函数，支持泛型
export function _delete<T = any>(
  url: string,
  data: Record<string, any> = {}
): Promise<AxiosResponse<ApiResponse<T>>> {
  return instance({
    method: 'delete',
    url,
    data,
  })
}

export default instance
