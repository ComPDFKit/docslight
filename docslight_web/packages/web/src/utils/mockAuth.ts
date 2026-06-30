import type { PermissionTree } from '../stores'
import { useCookies } from 'vue3-cookies'

export interface FixedAuthProfile {
  email: string
  id: string
  nickname: string
  role: string
  permissions: PermissionTree
  userPermissions: string[]
}

export interface FixedAuthUser {
  email: string
  username: string
  avatar: string
  token: string
  id: string
}

const STORAGE_KEY = 'idp_user'

export const FIXED_AUTH_PROFILE: FixedAuthProfile = {
  email: 'admin@admin.com',
  id: '187a8e06f80211f09ce810ffe0d13cd8',
  nickname: 'admin',
  role: 'admin',
  permissions: [
    {
      action_type: 0,
      code: 'extract',
      id: '01000000',
      level: 1,
      module_id: '1',
      name: 'extraction page',
      parent_id: null,
      status: '1',
      children: [
        {
          action_type: 2,
          code: 'extract:upload',
          id: '01010000',
          level: 2,
          module_id: '1',
          name: 'upload files',
          parent_id: '01000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'extract:check',
          id: '01020000',
          level: 2,
          module_id: '1',
          name: 'check result',
          parent_id: '01000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'extract:delete',
          id: '01030000',
          level: 2,
          module_id: '1',
          name: 'delete files',
          parent_id: '01000000',
          status: '1',
        },
        {
          action_type: 1,
          code: 'extract:template',
          id: '01040000',
          level: 2,
          module_id: '1',
          name: 'template management',
          parent_id: '01000000',
          status: '1',
          children: [
            {
              action_type: 2,
              code: 'extract:template:create',
              id: '01040100',
              level: 3,
              module_id: '1',
              name: 'create templates',
              parent_id: '01040000',
              status: '1',
            },
            {
              action_type: 2,
              code: 'extract:template:delete',
              id: '01040200',
              level: 3,
              module_id: '1',
              name: 'delete templates',
              parent_id: '01040000',
              status: '1',
            },
            {
              action_type: 2,
              code: 'extract:template:modify',
              id: '01040300',
              level: 3,
              module_id: '1',
              name: 'modify templates',
              parent_id: '01040000',
              status: '1',
            },
          ],
        },
        {
          action_type: 2,
          code: 'extract:export',
          id: '01050000',
          level: 2,
          module_id: '1',
          name: 'export result',
          parent_id: '01000000',
          status: '1',
        },
      ],
    },
    {
      action_type: 0,
      code: 'parse',
      id: '02000000',
      level: 1,
      module_id: '2',
      name: 'parse page',
      parent_id: null,
      status: '1',
      children: [
        {
          action_type: 2,
          code: 'parse:upload',
          id: '02010000',
          level: 2,
          module_id: '2',
          name: 'upload files',
          parent_id: '02000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'parse:check',
          id: '02020000',
          level: 2,
          module_id: '2',
          name: 'check result',
          parent_id: '02000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'parse:delete',
          id: '02030000',
          level: 2,
          module_id: '2',
          name: 'delete files',
          parent_id: '02000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'parse:export',
          id: '02040000',
          level: 2,
          module_id: '2',
          name: 'export files',
          parent_id: '02000000',
          status: '1',
        },
      ],
    },
    {
      action_type: 0,
      code: 'split',
      id: '03000000',
      level: 1,
      module_id: '3',
      name: 'split page',
      parent_id: null,
      status: '1',
      children: [
        {
          action_type: 2,
          code: 'split:upload',
          id: '03010000',
          level: 2,
          module_id: '3',
          name: 'upload files',
          parent_id: '03000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'split:split',
          id: '03020000',
          level: 2,
          module_id: '3',
          name: 'split files',
          parent_id: '03000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'split:delete',
          id: '03030000',
          level: 2,
          module_id: '3',
          name: 'delete files',
          parent_id: '03000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'split:export',
          id: '03040000',
          level: 2,
          module_id: '2',
          name: 'export result',
          parent_id: '03000000',
          status: '1',
        },
      ],
    },
    {
      action_type: 0,
      code: 'kb',
      id: '04000000',
      level: 1,
      module_id: '4',
      name: 'knowledgebase',
      parent_id: null,
      status: '1',
      children: [
        {
          action_type: 1,
          code: 'kb:create',
          id: '04010000',
          level: 2,
          module_id: '4',
          name: 'create a knowledgebase',
          parent_id: '04000000',
          status: '1',
        },
        {
          action_type: 1,
          code: 'kb:qa',
          id: '04020000',
          level: 2,
          module_id: '4',
          name: 'knowledgebase question and answer',
          parent_id: '04000000',
          status: '1',
        },
      ],
    },
    {
      action_type: 0,
      code: 'user',
      id: '05000000',
      level: 1,
      module_id: '5',
      name: 'user management page',
      parent_id: null,
      status: '1',
      children: [
        {
          action_type: 2,
          code: 'user:create',
          id: '05010000',
          level: 2,
          module_id: '5',
          name: 'create new users',
          parent_id: '05000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'user:modify',
          id: '05020000',
          level: 2,
          module_id: '5',
          name: 'modify a user',
          parent_id: '05000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'user:reset',
          id: '05030000',
          level: 2,
          module_id: '5',
          name: 'reset the password of a user',
          parent_id: '05000000',
          status: '1',
        },
        {
          action_type: 2,
          code: 'user:delete',
          id: '05040000',
          level: 2,
          module_id: '5',
          name: 'delete a user',
          parent_id: '05000000',
          status: '1',
        },
        {
          action_type: 1,
          code: 'user:charactor',
          id: '05050000',
          level: 2,
          module_id: '5',
          name: 'charactor management page',
          parent_id: '05000000',
          status: '1',
          children: [
            {
              action_type: 2,
              code: 'user:charactor:create',
              id: '05050100',
              level: 3,
              module_id: '5',
              name: 'create a charactor',
              parent_id: '05050000',
              status: '1',
            },
            {
              action_type: 2,
              code: 'user:charactor:permission',
              id: '05050200',
              level: 3,
              module_id: '5',
              name: 'charactor permission configration',
              parent_id: '05050000',
              status: '1',
            },
            {
              action_type: 2,
              code: 'user:charactor:modify',
              id: '05050300',
              level: 3,
              module_id: '5',
              name: 'modify a charactor',
              parent_id: '05050000',
              status: '1',
            },
            {
              action_type: 2,
              code: 'user:charactor:delete',
              id: '05050400',
              level: 3,
              module_id: '5',
              name: 'delete a charactor',
              parent_id: '05050000',
              status: '1',
            },
          ],
        },
        {
          action_type: 2,
          code: 'user:search',
          id: '05050600',
          level: 2,
          module_id: '5',
          name: 'search users',
          parent_id: '05000000',
          status: '1',
        },
      ],
    },
  ],
  userPermissions: [
    'EXTRACT_CHECK',
    'USER_CHARACTOR_PERMISSION',
    'USER_CHARACTOR_DELETE',
    'SPLIT',
    'KB',
    'EXTRACT_DELETE',
    'SPLIT_SPLIT',
    'KB_QA',
    'EXTRACT_TEMPLATE_DELETE',
    'PARSE_CHECK',
    'EXTRACT_TEMPLATE_CREATE',
    'USER_RESET',
    'EXTRACT',
    'USER_CHARACTOR_MODIFY',
    'USER_DELETE',
    'EXTRACT_TEMPLATE_MODIFY',
    'USER_CREATE',
    'PARSE_UPLOAD',
    'USER_CHARACTOR',
    'KB_CREATE',
    'EXTRACT_TEMPLATE',
    'SPLIT_UPLOAD',
    'PARSE',
    'PARSE_DELETE',
    'USER',
    'USER_CHARACTOR_CREATE',
    'EXTRACT_UPLOAD',
    'SPLIT_DELETE',
    'USER_MODIFY',
  ],
}

const getStoredUser = (): Partial<FixedAuthUser> | null => {
  const { cookies } = useCookies()
  const rawUser = cookies.get(STORAGE_KEY) || sessionStorage.getItem(STORAGE_KEY)
  if (!rawUser) return null
  if (typeof rawUser === 'string') {
    try {
      return JSON.parse(rawUser) as Partial<FixedAuthUser>
    } catch {
      return null
    }
  }
  return rawUser as Partial<FixedAuthUser>
}

export const getFixedUser = (): FixedAuthUser => {
  const storedUser = getStoredUser()
  return {
    email: FIXED_AUTH_PROFILE.email,
    username: FIXED_AUTH_PROFILE.nickname,
    avatar: String(storedUser?.avatar || localStorage.getItem('avatar') || ''),
    token: String(storedUser?.token || ''),
    id: FIXED_AUTH_PROFILE.id,
  }
}

export const getFixedUserSettings = () => {
  const locale = localStorage.getItem('locale') || 'zh-cn'
  const languageMap: Record<string, string> = {
    'zh-cn': 'Chinese',
    en: 'English',
    'zh-tw': 'Traditional Chinese',
    ja: 'Japanese',
  }
  const user = getFixedUser()
  return {
    email: user.email,
    nickname: user.username,
    avatar: user.avatar,
    language: languageMap[locale] || 'English',
  }
}

export const syncFixedUserSession = (overrides: Partial<FixedAuthUser> = {}) => {
  const { cookies } = useCookies()
  const mergedUser = {
    ...getFixedUser(),
    ...overrides,
  }
  const serialized = JSON.stringify(mergedUser)
  cookies.set(STORAGE_KEY, serialized)
  sessionStorage.setItem(STORAGE_KEY, serialized)
  if (mergedUser.avatar) {
    localStorage.setItem('avatar', mergedUser.avatar)
  }
  return mergedUser
}

export const clearFixedUserSession = () => {
  const { cookies } = useCookies()
  cookies.remove(STORAGE_KEY)
  sessionStorage.removeItem(STORAGE_KEY)
}

export const applyFixedAuthState = (store: { role: string }, permissionStore: { setPermissions: (tree: PermissionTree) => void }) => {
  store.role = FIXED_AUTH_PROFILE.role
  permissionStore.setPermissions(FIXED_AUTH_PROFILE.permissions)
  return syncFixedUserSession()
}
