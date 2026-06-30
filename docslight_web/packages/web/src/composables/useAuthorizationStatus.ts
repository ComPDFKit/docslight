import { ref } from 'vue'
import { get } from '../utils/request'

export type ProviderKey = 'google_drive' | 'aws_oss' | 'nas_smb' | 'notion' | 'trello' | 'gmail' | 'gcs'

interface ConnectionItem {
  source: string
  is_active: boolean
}

export interface AuthorizationItem {
  source: ProviderKey
  is_active: boolean
}

// 模块级缓存，跨路由共享，避免切换 tab 时重新请求导致闪烁
const authorizationList = ref<Partial<Record<ProviderKey, AuthorizationItem>>>({})

const isProviderKey = (source: string): source is ProviderKey => {
  return ['google_drive', 'aws_oss', 'nas_smb', 'notion', 'trello', 'gmail', 'gcs'].includes(source)
}

const getAuthorizationStatus = async () => {
  const { data: { data } } = await get('/v1/dms/auth/credentials')
  const next: Partial<Record<ProviderKey, AuthorizationItem>> = {}
  data.connections.forEach((element: ConnectionItem) => {
    if (!isProviderKey(element.source)) return
    next[element.source] = {
      source: element.source,
      is_active: element.is_active
    }
  })
  authorizationList.value = next
}

export const useAuthorizationStatus = () => {
  return { authorizationList, getAuthorizationStatus }
}
