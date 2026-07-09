import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { useUserStore } from '@/pinia/stores/user'
import { getToken } from '@@/utils/cache/cookies'
import axios from 'axios'
import { get, merge } from 'lodash-es'
import { getEnv } from '@@/utils/env'

/** 退出登录并强制刷新页面（会重定向到登录页） */
function logout() {
  useUserStore().logout()
  location.href = LOGIN_PATH
}
let domain = getEnv('COMIDP_HOST')
if (domain.indexOf(':') === 0) {
  domain = window.location.protocol + '//' + window.location.hostname + domain
}
const LOGIN_PATH = domain + '/login'

/** 创建请求实例 */
function createInstance() {
  // 创建一个 axios 实例命名为 instance
  const instance = axios.create({
    // 指定请求超时的毫秒数
    timeout: 10000,
    // 表示跨域请求时是否需要使用凭证
    withCredentials: false
  })
  // 请求拦截器
  instance.interceptors.request.use(
    // 发送之前
    config => config,
    // 发送失败
    error => Promise.reject(error)
  )
  // 响应拦截器（可根据具体业务作出相应的调整）
  instance.interceptors.response.use(
    (response) => {
      // apiData 是 api 返回的数据
      const apiData = response.data
      // 二进制数据则直接返回
      const responseType = response.request?.responseType
      if (responseType === 'blob' || responseType === 'arraybuffer') return apiData
      // 这个 code 是和后端约定的业务 code
      const code = apiData.code
      // 如果没有 code, 代表这不是项目后端开发的 api
      if (code === undefined) {
        ElMessage.error('非本系统的接口')
        return Promise.reject(new Error('非本系统的接口'))
      }
      switch (code) {
        case 0:
          // 本系统采用 code === 0 来表示没有业务错误
          return apiData
        case (/^(昵称).*(已存在)$/.test(apiData.message) && 400):
          return apiData
        case (/^(邮箱).*(已存在)$/.test(apiData.message) && 400):
          return apiData
        case 101:
          return apiData
        case 500:
          return apiData
        case 102:
          return apiData
        case 400:
          return apiData
        case 401:
          // Token 过期时
          return logout()
        default:
          // 不是正确的 code
          ElMessage.error(apiData.message || 'Error')
          return Promise.reject(new Error('Error'))
      }
    },
    (error) => {
      // status 是 HTTP 状态码
      const status = get(error, 'response.status')
      const message = get(error, 'response.data.message')
      switch (status) {
        case 401:
          // Token 过期时
          error.message = message || '未授权'
          logout()
          break
        case 403:
          error.message = message || '拒绝访问'
          break
        case 404:
          error.message = '请求地址出错'
          break
        case 408:
          error.message = '请求超时'
          break
        case 500:
          error.message = '服务器内部错误'
          break
        case 501:
          error.message = '服务未实现'
          break
        case 502:
          error.message = '网关错误'
          break
        case 503:
          error.message = '服务不可用'
          break
        case 504:
          error.message = '网关超时'
          break
        case 505:
          error.message = 'HTTP 版本不受支持'
          break
      }
      // ElMessage.error(error.message)
      return Promise.reject(error)
    }
  )
  return instance
}

/** 创建请求方法 */
function createRequest(instance: AxiosInstance) {
  return <T>(config: AxiosRequestConfig): Promise<T> => {
    const token = getToken()
    // console.log('Request config:', config)
    // 默认配置
    const defaultConfig: AxiosRequestConfig = {
      // 请求头
      headers: {
        // 携带 Token
        'Authorization': token ? `Bearer ${token}` : undefined,
        'Content-Type': 'application/json'
      },
      // 请求体
      data: {},
      // 请求超时
      timeout: 10000,
      // 跨域请求时是否携带 Cookies
      withCredentials: false
    }
    // 将默认配置 defaultConfig 和传入的自定义配置 config 进行合并成为 mergeConfig
    const mergeConfig = merge(defaultConfig, config)
    return instance(mergeConfig)
  }
}

/** 用于请求的实例 */
const instance = createInstance()

/** 用于请求的方法 */
export const request = createRequest(instance)
