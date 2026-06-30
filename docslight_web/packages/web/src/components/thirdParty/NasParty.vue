<template>
  <div class="flex <2xl:flex-col">
    <!-- 第一步 -->
    <div class="bg-white rounded-8px p-24px 2xl:(mr-12px w-[calc(50%-6px)]) <2xl:(w-full mb-12px)">
      <div class="rounded-4px bg-[#D7E2FE] border border-[#88A9FC] text-[#2E59CA] mb-8px w-fit text-xs px-12px py-4px">
        {{ t('third_party_auth.steps.step_1') }}
      </div>
      <div class="text-sm text-[#404653] font-600 mb-8px">
        {{ t('third_party_auth.common.authorizing') }}
      </div>
      <template v-if="authorize">
        <div class="py-32px border border-[#32D99D] rounded-4px flex flex-col items-center justify-center mb-20px">
          <AuthorizationIcon />
          <div class="text-xs text-[#0C131F] my-12px font-500">{{ t('third_party_auth.common.authorized') }}</div>
          <div class="text-xs text-[#888C94] mb-12px">compdf@gmail.com</div>
          <div @click="unauthorize" class="border border-[#DD6666] px-12px py-6px text-[#AA3333] w-fit rounded-6px cursor-pointer hover:(bg-[#FBECEC] border-[#DD6666] text-[#DD6666])">
            {{ t('third_party_auth.common.unauthorize') }}
          </div>
        </div>
      </template>
      <template v-else>
        <div class="flex flex-col">
          <div class="flex <lg:flex-col">
            <div class="flex flex-col w-[calc(50%-6px)] mr-12px <lg:(w-full mr-0)">
              <div class="text-sm text-[#404653] mb-10px">
                {{ t('third_party_auth.wizards.nas.fields.nas_server_ip') }}
                <span class="text-[#D44040] text-18px pl-2px">*</span>
              </div>
              <el-input v-model="nasServer" :placeholder="t('third_party_auth.enter')"></el-input>
            </div>
            <div class="flex flex-col w-[calc(50%-6px)] <lg:(w-full mt-24px)">
              <div class="text-sm text-[#404653] mb-10px">
                {{ t('third_party_auth.wizards.nas.fields.share_folder_name_optional') }}
              </div>
              <el-input v-model="nasShare" :placeholder="t('third_party_auth.enter')"></el-input>
            </div>
          </div>
          <div class="flex mt-24px <lg:flex-col">
            <div class="flex flex-col w-[calc(50%-6px)] mr-12px <lg:(w-full mr-0)">
              <div class="text-sm text-[#404653] mb-10px">
                {{ t('third_party_auth.wizards.nas.fields.username') }}
                <span class="text-[#D44040] text-18px pl-2px">*</span>
              </div>
              <el-input v-model="nasUsername" :placeholder="t('third_party_auth.enter')"></el-input>
            </div>
            <div class="flex flex-col w-[calc(50%-6px)] <lg:(w-full mt-24px)">
              <div class="text-sm text-[#404653] mb-10px">
                {{ t('third_party_auth.wizards.nas.fields.password') }}
                <span class="text-[#D44040] text-18px pl-2px">*</span>
              </div>
              <el-input v-model="nasPassword" show-password type="password" :placeholder="t('third_party_auth.enter')"></el-input>
            </div>
          </div>
        </div>
        <div class="flex w-full justify-center mt-48px mb-20px">
          <div v-loading="loading" @click="authorizeHandle" :class="nasServer && nasUsername && nasPassword ? 'hover:bg-[#244FF0] cursor-pointer' : 'bg-[#88A9FC] text-[#FFFFFF66] cursor-not-allowed'"
            class="rounded-6px font-500 text-white bg-[#396FFA] text-xs py-6px px-12px flex items-center justify-center">
            {{ t('dms.team_space.upload.third_party.authorization_required.authorize_now') }}
          </div>
        </div>
      </template>
      <div class="border border-[#B0C5FD] rounded-8px p-12px">
        <div class="text-xs text-[#404653] mb-10px font-600">
          {{ t('third_party_auth.setup_wizard') }}
        </div>
        <div class="p-12px">
          <div class="flex">
            <div class="bg-[#D7E2FE] text-brand-2 border border-[#88A9FC] rounded-1/2 mr-14px flex items-center justify-center min-w-16px h-16px text-10px leading-16px mt-2px">1</div>
            <div class="text-xs text-[#404653]">
              {{ t('third_party_auth.wizards.nas.setup[0]') }}
            </div>
          </div>
          <div class="flex mt-8px">
            <div class="bg-[#D7E2FE] text-brand-2 border border-[#88A9FC] rounded-1/2 mr-14px flex items-center justify-center min-w-16px h-16px text-10px leading-16px mt-2px">2</div>
            <div class="text-xs text-[#404653]">
              {{ t('third_party_auth.wizards.nas.setup[1]') }}
            </div>
          </div>
          <div class="flex mt-8px">
            <div class="bg-[#D7E2FE] text-brand-2 border border-[#88A9FC] rounded-1/2 mr-14px flex items-center justify-center min-w-16px h-16px text-10px leading-16px mt-2px">3</div>
            <div class="text-xs text-[#404653]">
              {{ t('third_party_auth.wizards.nas.setup[2]') }}
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 第二步 -->
    <div class="bg-white rounded-8px p-24px flex flex-col 2xl:w-[calc(50%-6px)] <2xl:w-full">
      <div class="rounded-4px bg-[#D7E2FE] border border-[#88A9FC] text-[#2E59CA] mb-8px w-fit text-xs px-12px py-4px">
        {{ t('third_party_auth.steps.step_2') }}
      </div>
      <div class="text-sm text-[#404653] font-600 mb-8px">
        {{ t('third_party_auth.file_directory.title') }}
      </div>
      <div class="flex items-end justify-between mb-20px <lg:(flex-col items-start gap-12px)">
        <div class="text-xs text-[#888C94] max-w-295px">
          {{ t('third_party_auth.file_directory.subtitle') }}
        </div>
        <div v-loading="loading" @click="authorize && (dialogVisible = true)" :class="authorize ? 'hover:bg-[#244FF0] cursor-pointer' : 'bg-[#88A9FC] text-[#FFFFFF66] cursor-not-allowed'"
          class="rounded-6px font-500 text-white bg-[#396FFA] text-xs py-6px px-12px flex items-center justify-center">
          {{ t('third_party_auth.file_directory.select_button') }}
        </div>
      </div>
      <div v-show="!authorize" class="border border-[#E2E3E5] rounded-4px p-24px flex-1 flex flex-col items-center justify-center">
        <img src="/images/unAuthorization.png" alt="UnAuthorization" width="64" height="64">
        <div class="text-xs text-[#0C131F] font-500 my-12px">
          {{ t('third_party_auth.file_directory.authorization_required_first_title') }}
        </div>
        <div class="text-12px leading-16px text-[#888C94] text-center">
          {{ t('third_party_auth.file_directory.authorization_required_first_desc') }}
        </div>
      </div>
      <div v-loading="loading" v-show="authorize && fileList.length === 0" class="border border-[#E2E3E5] rounded-4px p-24px flex-1 flex flex-col items-center justify-center">
        <img src="/images/kbEmpty.png" alt="kbEmpty" width="64" height="64">
        <div class="text-xs text-[#0C131F] font-500 my-12px">
          {{ t('third_party_auth.file_directory.no_directory_selected_title') }}
        </div>
        <div class="text-12px leading-16px text-[#888C94] text-center">
          {{ t('third_party_auth.file_directory.no_directory_selected_desc') }}
        </div>
        <div @click="dialogVisible = true" class="rounded-6px font-500 text-white bg-[#396FFA] text-xs py-6px px-12px flex items-center justify-center mt-12px cursor-pointer hover:bg-[#244FF0]">
          {{ t('third_party_auth.file_directory.select_button') }}
        </div>
      </div>
      <div v-loading="loading" v-show="authorize && fileList.length > 0" class="border border-[#E2E3E5] rounded-4px p-24px flex-1 flex flex-col items-center max-h-480px overflow-y-auto">
        <div class="w-full flex flex-col">
          <div v-for="(file, index) in fileList" :key="`${file.path}-${index}`" class="flex items-center justify-between py-4px rounded-4px" :class="[index && 'mt-8px']">
            <div @click="openMainFolder(file, index)" class="flex items-center flex-1 min-w-0" :class="[file.is_dir && 'cursor-pointer']" :style="{ paddingLeft: `${file.level * 20}px` }">
              <FileArrow v-if="file.is_dir" class="shrink-0 transition-transform duration-200 min-w-16px" :style="{ transform: file.expanded ? 'rotate(0deg)' : 'rotate(-90deg)' }" />
              
              <DocFolder v-if="file.is_dir" class="text-[#888C94] mx-4px shrink-0 min-w-20px" />
              <Docs v-else class="text-[#888C94] mx-4px shrink-0 min-w-20px" />
              <div class="text-sm text-[#404653] truncate">{{ file.name }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" align-center width="480px">
      <h3 class="text-[#0C131F] text-sm font-600">{{ t('third_party_auth.file_directory.range_title') }}</h3>
      <div class="p-12px border border-[#E2E3E5] rounded-4px my-24px max-h-320px overflow-y-auto">
        <div class="w-full flex flex-col gap-8px">
          <div
            v-for="(file, index) in dialogFileList"
            :key="`${file.path}-${index}`"
            class="flex items-center justify-between py-4px px-8px rounded-4px"
            :class="[getCheckState(file) !== 'unchecked' ? 'bg-[#D7E2FE]' : '']"
          >
            <div
              @click="openDialogFolder(file, index)"
              class="flex items-center flex-1 min-w-0"
              :class="[file.is_dir && 'cursor-pointer']"
              :style="{ paddingLeft: `${file.level * 20}px` }"
            >
              <FileArrow
                v-if="file.is_dir"
                class="shrink-0 transition-transform duration-200 min-w-16px"
                :style="{ transform: file.expanded ? 'rotate(0deg)' : 'rotate(-90deg)' }"
              />
              <DocFolder v-if="file.is_dir" class="text-[#888C94] mx-4px shrink-0 min-w-20px" />
              <Docs v-else class="text-[#888C94] mx-4px shrink-0 min-w-20px" />
              <div class="text-sm text-[#404653] truncate">{{ file.name }}</div>
            </div>
            <div class="flex items-center shrink-0 ml-8px cursor-pointer" @click.stop="toggleSelect(file)">
              <Checked v-if="getCheckState(file) === 'checked'" class="w-16px h-16px" />
              <Indeterminate v-else-if="getCheckState(file) === 'indeterminate'" class="w-16px h-16px" />
              <Check v-else class="w-16px h-16px" />
            </div>
          </div>
        </div>
      </div>
      <div class="flex justify-between items-center mt-24px">
        <div @click="hasAnySelected && deSelectAll()" :class="hasAnySelected ? 'text-[#2E59CA] cursor-pointer hover:opacity-80' : 'text-[#B7BABF] cursor-not-allowed'" class="flex items-center font-500">
          <Deselect class="mr-4px" />
          {{ t('third_party_auth.file_directory.deselect_all') }}
        </div>
        <div class="flex justify-center">
          <div @click="dialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
            {{ t('extraction.cancel') }}
          </div>
          <div
            v-loading="loading"
            @click="chooseFile"
            :class="hasAnySelected ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
            class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px"
          >
            {{ t('extraction.ok') }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, inject, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { post, get, _delete, put } from '../../utils/request'
import { useCookies } from 'vue3-cookies'

const { cookies } = useCookies()
const userInfo = ref(cookies.get('idp_user') || JSON.parse(sessionStorage.getItem('idp_user') || '{}'))
const { t } = useI18n()
const nasServer = ref('')
const nasShare = ref('')
const nasUsername = ref('')
const nasPassword = ref('')
const loading = ref(false)
const authorize = ref(false)
const dialogVisible = ref(false)

// ─── Types ────────────────────────────────────────────────────────────────────

type ProviderKey = 'google_drive' | 'aws_oss' | 'nas_smb' | 'notion' | 'trello' | 'gmail' | 'gcs'

interface ConnectionItem {
  source: string
  is_active: boolean
}

interface AuthorizationItem {
  source: ProviderKey
  is_active: boolean
}

interface NasFileItem {
  // path doubles as unique id for NAS items
  path: string
  name: string
  is_dir: boolean
  size: number | null
  create_time: number
  update_time: number
  level: number
  parent_id: string | null  // parent path, null for root items
  expanded?: boolean
  selected?: boolean
}

interface AuthorizedItem {
  id: string
  name: string
  type: 'folder' | 'file'
  children?: AuthorizedItem[]
}

interface AuthorizedRawItem {
  id: string
  name: string
  type: 'folder' | 'file'
  children?: AuthorizedRawItem[]
}

// ─── State ────────────────────────────────────────────────────────────────────

// Main panel list (from authorized-items, supports expand/collapse via cache)
const fileList = ref<NasFileItem[]>([])
// Dialog list (independent selection state, loaded from API on open)
const dialogFileList = ref<NasFileItem[]>([])

// ─── Auth ─────────────────────────────────────────────────────────────────────

const authorizationList = ref<Partial<Record<ProviderKey, AuthorizationItem>>>({})

const isProviderKey = (source: string): source is ProviderKey => {
  return ['google_drive', 'aws_oss', 'nas_smb', 'notion', 'trello', 'gmail', 'gcs'].includes(source)
}

onMounted(async () => {
  const { data: { data } } = await get('/v1/dms/auth/credentials?source=nas_smb')
  data.connections.forEach((element: ConnectionItem) => {
    if (!isProviderKey(element.source)) return
    authorizationList.value[element.source] = {
      source: element.source,
      is_active: element.is_active
    }
  })
  authorize.value = authorizationList.value.nas_smb?.is_active as boolean
  if (authorize.value) {
    await getFileList()
  }
})

const getAuthorizationStatus = inject('getAuthorizationStatus', () => {})

const unauthorize = async () => {
  ElMessageBox.confirm(t('third_party_auth.unauthorize_tips.nas'), t('third_party_auth.common.unauthorize'), {
    confirmButtonText: t('third_party_auth.common.ok'),
    cancelButtonText: t('third_party_auth.common.cancel'),
    type: 'warning',
    customClass: 'delete-file'
  }).then(async () => {
    loading.value = true
    try {
      await _delete('/v1/dms/auth/credentials?source=nas_smb')
      ElMessage.success(t('third_party_auth.success'))
      authorize.value = false
      fileList.value = []
      getAuthorizationStatus()
    } catch (error) {
      ElMessage.error(t('third_party_auth.fail'))
    } finally {
      loading.value = false
    }
  }).catch(() => {
    // Cancelled
  })
}

const authorizeHandle = async () => {
  if (!nasServer.value || !nasUsername.value || !nasPassword.value) return
  loading.value = true
  try {
    const req = JSON.stringify({
      source: 'nas_smb',
      credentials: {
        server: nasServer.value,
        share: nasShare.value,
        username: nasUsername.value,
        password: nasPassword.value
      }
    })
    const { data } = await post('/v1/dms/auth/verify', req)
    if (data.code === 0 && data.message === 'success' && data.data?.valid !== false) {
      ElMessage.success(t('third_party_auth.success'))
      authorize.value = true
      getAuthorizationStatus()
      nasServer.value = ''
      nasShare.value = ''
      nasUsername.value = ''
      nasPassword.value = ''
    } else {
      ElMessage.error(data.data?.message || t('third_party_auth.fail'))
    }
  } catch (error) {
    console.error('Authorization failed', error)
    ElMessage.error(t('third_party_auth.fail'))
  } finally {
    loading.value = false
  }
}

// ─── File helpers ─────────────────────────────────────────────────────────────

const rootPath = '/'

const mapNasFiles = (files: any[] = [], level: number, parentId: string | null): NasFileItem[] => {
  return files.map((file) => ({
    path: file.path,
    name: file.name,
    is_dir: file.is_dir,
    size: file.size ?? null,
    create_time: file.create_time ?? 0,
    update_time: file.update_time ?? 0,
    level,
    parent_id: parentId,
    expanded: false,
    selected: false
  }))
}

const fetchNasFiles = async (path: string, level: number, parentId: string | null): Promise<NasFileItem[]> => {
  const req = { source: 'nas_smb', args: { path } }
  const { data } = await post('/v1/dms/files/list', req)
  return mapNasFiles(data.data, level, parentId)
}

// ─── Main panel: authorized-items → expandable list ──────────────────────────

const mainFolderCache = new Map<string, NasFileItem[]>()

const buildMainCache = (items: AuthorizedRawItem[], level = 0, parentId: string | null = null): NasFileItem[] => {
  const result: NasFileItem[] = []
  for (const item of items) {
    const isDir = item.type === 'folder'
    const flat: NasFileItem = {
      path: item.id,   // authorized-items stores path in the id field for NAS
      name: item.name,
      is_dir: isDir,
      size: null,
      create_time: 0,
      update_time: 0,
      level,
      parent_id: parentId,
      expanded: false,
      selected: false
    }
    result.push(flat)
    if (item.children?.length) {
      const children = buildMainCache(item.children, level + 1, item.id)
      mainFolderCache.set(item.id, children)
    }
  }
  return result
}

const getFileList = async () => {
  try {
    loading.value = true
    const { data: { data } } = await get('/v1/dms/auth/authorized-items?source=nas_smb')
    const items: AuthorizedRawItem[] = Array.isArray(data) ? data : []
    mainFolderCache.clear()
    fileList.value = buildMainCache(items)
  } catch (error) {
    console.error('Failed to get file list', error)
    ElMessage.error(t('third_party_auth.file_list_fail'))
  } finally {
    loading.value = false
  }
}

const getMainDescendantEndIndex = (startIndex: number): number => {
  const currentLevel = fileList.value[startIndex].level
  let endIndex = startIndex
  for (let i = startIndex + 1; i < fileList.value.length; i++) {
    if (fileList.value[i].level <= currentLevel) break
    endIndex = i
  }
  return endIndex
}

const openMainFolder = async (item: NasFileItem, index: number) => {
  if (!item.is_dir) return
  if (item.expanded) {
    const endIndex = getMainDescendantEndIndex(index)
    if (endIndex > index) {
      fileList.value.splice(index + 1, endIndex - index)
    }
    fileList.value[index].expanded = false
    return
  }
  const cached = mainFolderCache.get(item.path)
  if (cached && cached.length > 0) {
    fileList.value.splice(index + 1, 0, ...cached.map(c => ({ ...c, expanded: false })))
    fileList.value[index].expanded = true
    return
  }
  try {
    loading.value = true
    const childList = await fetchNasFiles(item.path, item.level + 1, item.path)
    if (childList.length === 0) {
      ElMessage.info(t('third_party_auth.file_directory.empty_folder'))
      return
    }
    mainFolderCache.set(item.path, childList)
    fileList.value.splice(index + 1, 0, ...childList)
    fileList.value[index].expanded = true
  } catch (error) {
    console.error('Failed to load folder', error)
  } finally {
    loading.value = false
  }
}

// ─── Dialog-specific logic ────────────────────────────────────────────────────

const dialogFolderChildrenCache = new Map<string, NasFileItem[]>()
// Leaf paths from the last saved authorized-items response (folder containers excluded)
const dialogSavedPaths = new Set<string>()

// Collect only leaf-node paths from the saved authorized-items tree.
// For NAS, item.path is used as the unique key (stored as item.id in the API response).
const getAuthorizedPaths = (): Set<string> => {
  const paths = new Set<string>()
  for (const item of fileList.value) {
    if (!mainFolderCache.has(item.path)) paths.add(item.path)
  }
  const addLeaves = (children: NasFileItem[]) => {
    for (const child of children) {
      if (mainFolderCache.has(child.path)) {
        addLeaves(mainFolderCache.get(child.path)!)
      } else {
        paths.add(child.path)
      }
    }
  }
  for (const children of mainFolderCache.values()) {
    addLeaves(children)
  }
  return paths
}

// Load dialog file list (root path) from API when dialog opens
watch(dialogVisible, async (val) => {
  if (val) {
    dialogFolderChildrenCache.clear()
    dialogFileList.value = []
    dialogSavedPaths.clear()
    try {
      loading.value = true
      const savedPaths = getAuthorizedPaths()
      savedPaths.forEach(p => dialogSavedPaths.add(p))
      const rootFiles = await fetchNasFiles(rootPath, 0, null)
      dialogFileList.value = rootFiles.map(item => ({ ...item, selected: savedPaths.has(item.path), expanded: false }))
    } catch (error) {
      console.error('Failed to load dialog file list', error)
    } finally {
      loading.value = false
    }
  }
})

const getDescendantEndIndex = (list: NasFileItem[], startIndex: number): number => {
  const currentLevel = list[startIndex].level
  let endIndex = startIndex
  for (let i = startIndex + 1; i < list.length; i++) {
    if (list[i].level <= currentLevel) break
    endIndex = i
  }
  return endIndex
}

const collapseDialogFolder = (index: number) => {
  const folderPath = dialogFileList.value[index].path
  const endIndex = getDescendantEndIndex(dialogFileList.value, index)
  if (endIndex > index) {
    const removed = dialogFileList.value.splice(index + 1, endIndex - index)
    for (let i = removed.length - 1; i >= 0; i--) {
      if (removed[i].is_dir && removed[i].expanded) {
        const subChildren: NasFileItem[] = []
        for (let j = i + 1; j < removed.length; j++) {
          if (removed[j].level <= removed[i].level) break
          subChildren.push(removed[j])
        }
        if (subChildren.length > 0) {
          dialogFolderChildrenCache.set(removed[i].path, subChildren)
          removed.splice(i + 1, subChildren.length)
        }
        removed[i].expanded = false
      }
    }
    dialogFolderChildrenCache.set(folderPath, removed)
  }
  dialogFileList.value[index].expanded = false
}

const openDialogFolder = async (item: NasFileItem, index: number) => {
  if (!item.is_dir) return

  if (item.expanded) {
    collapseDialogFolder(index)
    return
  }

  // Use dialog cache if available (preserves selection state)
  if (dialogFolderChildrenCache.has(item.path)) {
    const cached = dialogFolderChildrenCache.get(item.path)!
    dialogFileList.value.splice(index + 1, 0, ...cached)
    dialogFileList.value[index].expanded = true
    return
  }

  try {
    loading.value = true
    const childList = await fetchNasFiles(item.path, item.level + 1, item.path)
    if (childList.length === 0) {
      ElMessage.info(t('third_party_auth.folder_empty'))
      return
    }
    const mappedChildren = childList.map(c => ({ ...c, selected: dialogSavedPaths.has(c.path) }))
    dialogFolderChildrenCache.set(item.path, mappedChildren)
    dialogFileList.value.splice(index + 1, 0, ...mappedChildren)
    dialogFileList.value[index].expanded = true
  } catch (error) {
    console.error('Failed to open dialog folder', error)
  } finally {
    loading.value = false
  }
}

// ─── Checkbox state ───────────────────────────────────────────────────────────

const getVisibleDescendantIndices = (startIndex: number): number[] => {
  const list = dialogFileList.value
  const currentLevel = list[startIndex].level
  const indices: number[] = []
  for (let i = startIndex + 1; i < list.length; i++) {
    if (list[i].level <= currentLevel) break
    indices.push(i)
  }
  return indices
}

const collectLeafStates = (item: NasFileItem): { total: number; selected: number } => {
  if (!item.is_dir) {
    return { total: 1, selected: item.selected ? 1 : 0 }
  }
  let children: NasFileItem[]
  const directLevel = item.level + 1
  if (item.expanded) {
    const idx = dialogFileList.value.indexOf(item)
    if (idx === -1) {
      children = dialogFolderChildrenCache.get(item.path)?.filter(c => c.level === directLevel) ?? []
    } else {
      children = []
      for (let i = idx + 1; i < dialogFileList.value.length; i++) {
        if (dialogFileList.value[i].level < directLevel) break
        if (dialogFileList.value[i].level === directLevel) children.push(dialogFileList.value[i])
      }
    }
  } else {
    children = dialogFolderChildrenCache.get(item.path)?.filter(c => c.level === directLevel) ?? []
  }
  if (children.length === 0) {
    return { total: 1, selected: item.selected ? 1 : 0 }
  }
  return children.reduce((acc, child) => {
    const s = collectLeafStates(child)
    return { total: acc.total + s.total, selected: acc.selected + s.selected }
  }, { total: 0, selected: 0 })
}

const getCheckState = (item: NasFileItem): 'checked' | 'indeterminate' | 'unchecked' => {
  if (!item.is_dir) {
    return item.selected ? 'checked' : 'unchecked'
  }
  const hasCache = dialogFolderChildrenCache.has(item.path)
  if (!item.expanded && !hasCache) {
    return item.selected ? 'checked' : 'unchecked'
  }
  const { total, selected } = collectLeafStates(item)
  if (total === 0) return item.selected ? 'checked' : 'unchecked'
  if (selected === total) return 'checked'
  if (selected > 0) return 'indeterminate'
  return 'unchecked'
}

const syncAncestors = (fromIndex: number) => {
  const list = dialogFileList.value
  for (let i = fromIndex - 1; i >= 0; i--) {
    if (!list[i].expanded) continue
    const childIndices = getVisibleDescendantIndices(i)
    if (!childIndices.includes(fromIndex)) continue
    list[i].selected = childIndices.every(j => list[j].selected)
  }
}

const propagateSelectionToCache = (folderId: string, selected: boolean) => {
  const cached = dialogFolderChildrenCache.get(folderId)
  if (!cached) return
  for (const child of cached) {
    child.selected = selected
    if (child.is_dir) {
      propagateSelectionToCache(child.path, selected)
    }
  }
}

const toggleSelect = (item: NasFileItem) => {
  const idx = dialogFileList.value.indexOf(item)
  if (idx === -1) return

  const currentState = getCheckState(item)
  const newSelected = currentState !== 'checked'

  dialogFileList.value[idx].selected = newSelected

  // Propagate to visible descendants
  const descendantIndices = getVisibleDescendantIndices(idx)
  descendantIndices.forEach(i => {
    dialogFileList.value[i].selected = newSelected
    // Also propagate to cached children of any visible subfolder
    if (dialogFileList.value[i].is_dir) {
      propagateSelectionToCache(dialogFileList.value[i].path, newSelected)
    }
  })

  // Propagate to cached (collapsed) children
  if (item.is_dir) {
    propagateSelectionToCache(item.path, newSelected)
  }

  // Sync ancestor folders so collapsing them reflects current state
  syncAncestors(idx)
}

const deSelectAll = () => {
  dialogFileList.value.forEach(item => {
    item.selected = false
  })
  // Also clear selection in all cached (collapsed) children
  for (const cached of dialogFolderChildrenCache.values()) {
    cached.forEach(item => { item.selected = false })
  }
}

const hasAnySelected = computed(() => {
  if (dialogFileList.value.some(item => item.selected)) return true
  for (const cached of dialogFolderChildrenCache.values()) {
    if (cached.some(item => item.selected)) return true
  }
  return false
})

// ─── Build authorized tree & submit ──────────────────────────────────────────

/**
 * Build the nested authorized_items tree from the flat dialogFileList.
 * NAS uses path as the id field.
 */
const buildAuthorizedTree = (): AuthorizedItem[] => {
  const list = dialogFileList.value

  const buildChildren = (parentId: string | null): AuthorizedItem[] => {
    const result: AuthorizedItem[] = []
    for (const item of list) {
      if (item.parent_id !== parentId) continue

      if (item.is_dir && item.expanded) {
        const children = buildChildren(item.path)
        if (item.selected || children.length > 0) {
          const node: AuthorizedItem = { id: item.path, name: item.name, type: 'folder' }
          if (children.length > 0) node.children = children
          result.push(node)
        }
      } else if (item.is_dir && !item.expanded) {
        if (item.selected) {
          result.push({ id: item.path, name: item.name, type: 'folder' })
        }
      } else {
        if (item.selected) {
          result.push({ id: item.path, name: item.name, type: 'file' })
        }
      }
    }
    return result
  }

  return buildChildren(null)
}

const chooseFile = async () => {
  if (!hasAnySelected.value) return
  const authorizedItems = buildAuthorizedTree()
  ElMessageBox.confirm(t('third_party_auth.file_directory.confirm_update_desc'), t('third_party_auth.file_directory.confirm_update_title'), {
    confirmButtonText: t('third_party_auth.common.ok'),
    cancelButtonText: t('third_party_auth.common.cancel'),
    type: 'warning',
    customClass: 'delete-file'
  }).then(async () => {
    if (!authorizedItems.length) return
    const { data } = await put('/v1/dms/auth/authorized-items', {
      user_id: userInfo.value?.id,
      source: 'nas_smb',
      authorized_items: authorizedItems
    })
    if (data.code === 0 && data.message === 'success') {
      ElMessage.success(t('third_party_auth.file_directory.select_success'))
      dialogVisible.value = false
      await getFileList()
    } else {
      ElMessage.error(t('third_party_auth.file_directory.select_fail'))
    }
  }).catch(() => {
    // Cancelled
  })
}
</script>

<style lang="scss">
.el-overlay.is-message-box .el-overlay-message-box .el-message-box.delete-file {
  min-width: 520px;
  .el-message-box__header {
    height: auto;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    margin-left: 36px;
    color: #404653;
    margin-bottom: 8px;
  }
  .el-message-box__content .el-message-box__container {
    align-items: flex-start;
    .el-message-box-icon--warning {
      margin-top: -32px;
      svg path {
        fill: #F28909;
      }
    }
    .el-message-box__message {
      font-size: 14px;
      padding-right: 0;
      line-height: 20px;
      padding-left: 12px;
      color: #404653;
    }
  }
  .el-message-box__btns {
    justify-content: flex-end;
    .el-button {
      padding: 6px 12px;
      width: fit-content;
      font-weight: normal;
      &:first-child {
        color: #0C131F;
        background: white;
        border: 1px solid #E2E3E5;
        &:hover {
          background: #F6F6FB;
          color: #396FFA;
        }
        &:active {
          background: #EBEDF0;
          color: #88A9FC;
        }
      }
    }
  }
}
</style>
