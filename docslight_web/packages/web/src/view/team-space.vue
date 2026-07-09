<template>
  <div class="document-extraction flex flex-col">
    <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8]">{{ t('dms.title') }}</h1>
    <div class="flex">
      <!-- 左侧文件夹列表 -->
      <div class="px-20px py-32px min-w-258px border-r border-[#E1E3E8] flex flex-col justify-between h-[calc(100vh-81px)] overflow-x-auto">
        <div class="w-218px flex flex-col">
          <el-input class="max-w-218px folder-search" clearable v-model="searchQueryFolder" @clear="getFolderList" @input="getFolderList" :placeholder="t('dms.scanner_inbox.search.by_folder_name')">            <template #prefix>
              <Search />
            </template>
          </el-input>
          <div class="h-1px w-full bg-[#B7BABF] my-20px"></div>
          <!-- <a href="/scanner-inbox" class="py-8px px-12px rounded-8px flex items-center justify-between text-sm font-500 cursor-pointer">
            <div class="flex items-center">
              <Scanner class="mr-12px" />
              {{ t('dms.scanner_inbox.title') }}
            </div>
            <div class="w-16px h-16px rounded-1/2 bg-[#D44040] text-8px leading-16px text-white flex items-center justify-center">99+</div>
          </a> -->
          <div @click="active = 'team', activeFolder = '', getFolderList()" :class="active === 'team' ? 'bg-[#1460F31A] text-brand-2' : 'text-[#0C131F]'" class="py-8px px-12px rounded-8px flex items-center justify-between text-sm font-500 cursor-pointer mt-8px">
            <div class="flex items-center">
              <TeamFile class="mr-12px" />
              {{ t('dms.team_space.title') }}
            </div>
            <DocArrow class="transform" :class="active === 'team' && 'rotate-[180deg]'" />
          </div>
          <div v-show="['team', 'folder'].includes(active)" class="">
            <div @click="openCreateFolderDialog(false)" v-if="['manager', 'admin'].includes(store.role)" class="border border-[#B7BABF] rounded-8px py-8px flex pl-24px items-center text-sm font-500 text-[#404653] cursor-pointer mt-8px hover:(bg-[#F6F6FB] border-[#0C131F])">
              <AddFolder class="mr-4px" />
              {{ t('dms.team_space.toolbar.new_folder') }}
            </div>
          </div>
          <template v-if="folderList.length > 0">
            <div v-for="(item, index) in folderList" :key="index" @click="changeFolder(item.id)" :class="activeFolder === item.id && 'bg-[#1460F31A] !text-brand-2 active'" class="relative template mt-8px cursor-pointer flex items-center justify-between rounded-12px pl-24px pr-12px py-4px text-[#888C94] font-500 text-sm hover:text-brand-2">
              <div class="flex items-center overflow-hidden min-w-0">
                <DocFolder class="mr-8px flex-shrink-0" />
                <span class="truncate">{{ ['Order', 'Invoice'].includes(item.name) ? t(`extraction.${item.name.toLowerCase()}`) : item.name }}</span>
              </div>
              <Option @click.stop="changeStatus(index)" class="option flex-shrink-0" />
              <div v-show="folderStatusArr[index].status" class="assistant-shadow absolute top-32px right-12px bg-white z-3 p-4px rounded-4px whitespace-normal">
                <!-- 上传文件：editor, manager 可用 -->
                <div v-if="item.role === 'editor' || item.role === 'manager'" @click.stop.prevent="changeFolder(item.id, true), folderStatusArr[index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px whitespace-normal hover:(text-brand-2 bg-[#1460F31A])">
                  {{ t('dms.team_space.file_actions_single.upload_file') }}
                </div>
                <!-- 权限设置：仅 manager 可用 -->
                <div v-if="item.role === 'manager'" @click.stop.prevent="folderRef?.openDialog(item.id, undefined, item), folderStatusArr[index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                  {{ t('dms.team_space.file_actions_single.set_permissions') }}
                </div>
                <!-- 下载：所有角色可用 -->
                <div @click.stop.prevent="downloadFile(item.id)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                  {{ t('dms.team_space.file_actions_single.download') }}
                </div>
                <!-- 重命名：editor, manager 可用 -->
                <div v-if="item.role === 'editor' || item.role === 'manager'" @click.stop.prevent="handleRenameFolder(item, index)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                  {{ t('dms.team_space.file_actions_single.rename') }}
                </div>
                <!-- 删除：仅 manager 可用 -->
                <div v-if="item.role === 'manager'" @click.stop.prevent="deleteFolder(item.id), folderStatusArr[index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                  {{ t('dms.team_space.file_actions_single.delete') }}
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
      <!-- 右侧文件列表区域 -->
      <div v-show="folderList.length === 0" class="w-full bg-[#F3F6FF] h-[calc(100vh-81px)] flex flex-col items-center justify-center">
        <img src="/images/kbEmpty.png" width="120" height="120" alt="Empty">
        <div class="text-sm text-brand-3 mt-8px mb-32px max-w-600px text-center">
          {{ t('dms.team_space.empty_state.description') }}
        </div>
        <div v-if="['editor', 'manager', 'admin'].includes(store.role)" @click="openCreateFolderDialog(false)" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-16px w-fit flex items-center justify-center font-500 hover:bg-[#244FF0]">
          {{ t('dms.team_space.empty_state.create_folder') }}
        </div>
      </div>
      <template v-if="folderList.length > 0">
        <DmsFolder ref="folderRef" v-show="active === 'team'" />
        <DmsFile ref="fileTableRef" v-show="active === 'folder'" />
      </template>
    </div>

    <!-- 上传文件 -->
    <el-dialog v-model="dialogVisible" align-center width="480px">
      <h3 class="text-sm font-600 text-[#43474D] py-4px mb-24px">
        {{ t('extraction.upload') }}
      </h3>
      <div @drop.prevent="onDrop" @dragover.prevent="dragover = true" @dragleave.prevent="leave" class="border border-[1.5px] border-dashed border-brand-2 cursor-pointer relative rounded-10px min-h-330px" :class="dragover && 'bg-[#F3F6FF]'">
        <div v-loading="loading" @click="input?.click" class="w-fit rounded-6px mx-auto mt-136px cursor-pointer bg-[#396FFA] text-white text-sm font-500 py-8px px-16px flex items-center justify-center hover:bg-[#244FF0]">
          <Upload class="mr-4px" />
          {{ t('extraction.selectFile[0]') }}
        </div>
        <div class="text-center my-8px text-xs text-brand-3">
          {{ t('extraction.selectFile[1]') }}
        </div>
        <div class="text-center text-xs text-brand-3">
          {{ t('extraction.selectFile[2]') }}
        </div>
        <div class="mt-12px rounded-6px bg-[#F6F6FB] px-12px py-8px text-xs text-[#8C8C8C] absolute bottom-0px left-0 rounded-10px w-full text-center">
          {{ t('extraction.support') }}
        </div>
      </div>
      <input ref="input" class="hidden" type="file" accept=".png,.jpg,.jpeg,.tiff,.bmp,.pdf,.doc,.docx,.xls,.xlsx,.csv,.ppt,.pptx,.txt" name="file" multiple @change="handleChange">
      <div v-show="fileList.length" class="flex flex-col mt-24px adaptive overflow-auto text-sm text-brand-0 max-h-154px" :class="`h-[${fileList.length * 24}px]`">
        <div v-for="(file, index) in fileList" :key="index" :class="index && 'mt-16px'" class="flex justify-between">
          <div class="flex items-center">
            <Success class="mr-8px min-w-24px" />
            <div class="truncate w-[calc(448px-64px)]">{{ file.name }}</div>
          </div>
          <DeleteFile @click="deleteUploadFile(index)" class="cursor-pointer min-w-16px" />
        </div>
      </div>
      <div class="flex justify-center mt-24px">
        <div @click="dialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
          {{ t('extraction.cancel') }}
        </div>
        <div v-loading="loading" @click="upload" :class="fileList?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
          class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
          {{ t('extraction.ok') }}
        </div>
      </div>
    </el-dialog>

    <!-- 创建文件夹 -->
    <el-dialog v-model="createFolderDialogVisible" align-center width="480px">
      <h3 class="text-sm font-600 text-[#0C131F] mb-24px">{{ t('dms.team_space.folder.new_folder.title') }}</h3>
      <div class="px-12px">
        <div class="text-sm font-500 text-[#404653] mb-12px">{{ t('dms.team_space.folder.new_folder.folder_name') }}</div>
        <el-input v-model="folderName" maxlength="50" :placeholder="t('dms.team_space.folder.new_folder.placeholder')" />
      </div>
      <div class="flex justify-center mt-24px">
        <div @click="createFolderDialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
          {{ t('dms.team_space.folder.new_folder.cancel') }}
        </div>
        <div v-loading="loading" @click="createFolder" :class="folderName ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
          class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
          {{ t('dms.team_space.folder.new_folder.ok') }}
        </div>
      </div>
    </el-dialog>

    <!-- 重命名文件夹 -->
    <el-dialog v-model="renameFolderDialogVisible" align-center width="480px">
      <h3 class="text-sm font-600 text-[#0C131F] mb-24px">
        {{ t('dms.team_space.rename.title') }}
      </h3>
      <div class="px-12px">
        <div class="text-sm font-500 text-[#404653] mb-12px">
          {{ t('dms.team_space.rename.name') }}
        </div>
        <el-input v-model="folderName" maxlength="50" :placeholder="t('dms.team_space.rename.placeholder')" @keyup.enter="renameFolder" />
      </div>
      <div class="flex justify-center mt-24px">
        <div @click="renameFolderDialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
          {{ t('dms.team_space.rename.cancel') }}
        </div>
        <div v-loading="loading" @click="renameFolder" :class="folderName ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
          class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
          {{ t('dms.team_space.rename.ok') }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, provide } from 'vue'
import { getSystemBaseUnit } from '../utils/tools'
import request, { post, get, _delete } from '../utils/request'
import { useStore } from '../stores'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useStore()
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import { saveAs } from 'file-saver'

const { t } = useI18n()
const total = ref(0)
const pageSize = ref(10)
const loading = ref(false)
const active = ref('team')
const currentPage = ref(1)
const folderName = ref('')
const dialogVisible = ref(false)
const searchQueryFolder = ref('')
const activeFolder = ref('Order')
const fileStatusArr = ref<status[]>([])
const folderList = ref<folderType[]>([])
const folderStatusArr = ref<status[]>([])
const folderTableStatusArr = ref<status[]>([])
const createFolderDialogVisible = ref(false)
const renameFolderDialogVisible = ref(false)
const folderFileList = ref<DocumentFile[]>([])

interface Creator {
  email: string
  id: string
  nickname: string
}

interface DocumentFile {
  create_date: string
  create_time: number
  creator: Creator
  file_type: string
  folder_id: string
  folder_name: string
  id: string
  name: string
  role: string
  size: string
  update_date: string
  update_time: number
}

interface folderType {
  create_time: number
  creator: Creator
  file_count: number
  id: string,
  name: string
  role: string
}


const selectFile = ref<DocumentFile | null>(null)
const selectFolder = ref<folderType | null>(null)

const getFolderList = async () => {
  let searchQuery = `?page=${currentPage.value}`
    + `&pageSize=${pageSize.value}`
    + `&startTime=${startTime.value}`
    + `&endTime=${endTime.value}`
    + `&keywords=${searchQueryFolder.value}`
    + `&file_type=${fileType.value}`
  const { data } = await get(`/v1/team_space/root_folders${searchQuery}`)
  total.value = data.data.total
  folderList.value = data.data.folders
  folderList.value.forEach(()=> {
    folderStatusArr.value.push({ status: false })
    folderTableStatusArr.value.push({ status: false })
  })
}

const endTime = ref('')
const fileType = ref('')
const startTime = ref('')
const onMove = ref(false)

const getFolderFileList = async (id?: string) => {
  loading.value = true
  const { data } = await get(`/v1/team_space/file/search`)
  loading.value = false
  folderFileList.value = data.data.files
  folderFileList.value.forEach(()=> {
    fileStatusArr.value.push({ status: false })
  })
}

onMounted(() => {
  getFolderList()
  addEventListener('click', () => {
    folderStatusArr.value.forEach(item => item.status = false)
  })
})

interface status {
 status: boolean
}

const changeStatus = async (index: number) => {
  folderStatusArr.value.forEach((status: status) => {
    status.status = false
  })
  folderStatusArr.value[index].status = !folderStatusArr.value[index].status
}

const fileTableRef = ref()
const folderRef = ref()

const changeFolder = async (id: string, open?: boolean) => {
  active.value = 'folder'
  activeFolder.value = id
  const folder = folderList.value.find(f => f.id === id)
  fileTableRef.value.changeFolderId(id, folder?.role)
  if (open) {
    fileTableRef.value.dialogVisible = true
  }
}

provide('changeFolder', changeFolder)

const openCreateFolderDialog = (isMove = false) => {
  folderName.value = ''
  onMove.value = isMove
  createFolderDialogVisible.value = true
}
provide('openCreateFolderDialog', openCreateFolderDialog)

// 删除文件夹
const deleteFolder = async (id: string) => {
  try {
    await ElMessageBox.confirm(
      t('dms.team_space.delete_confirmation_folder.description'),
      t('dms.team_space.delete_confirmation_folder.title'),
      {
        confirmButtonText: t('dms.team_space.delete_confirmation_folder.actions.ok'),
        cancelButtonText: t('dms.team_space.delete_confirmation_folder.actions.cancel'),
        type: 'warning',
        customClass: 'delete-file'
      }
    )
  } catch {
    return
  }

  const { data } = await _delete('/v1/team_space/folder',
    {
      folder_id: id
    }
  )
  if (data.code === 0 && data.message === 'success') {
    ElMessage.success(t('splitting.success'))
    await getFolderList()
    folderRef.value?.getFolderList()
    if (id === activeFolder.value) {
      activeFolder.value = folderList.value[0]?.id || ''
      if (activeFolder.value === '') {
        active.value = 'team'
      } else {
        const folder = folderList.value.find(f => f.id === activeFolder.value)
        fileTableRef.value.changeFolderId(activeFolder.value, folder?.role)
      }
    }
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}

const handleRenameFolder = (val: folderType, index: number) => {
  selectFolder.value = val
  folderName.value = val.name
  renameFolderDialogVisible.value = true
  folderStatusArr.value[index].status = false
}

// 重命名文件夹
const renameFolder = async () => {
  const { data } = await post('/v1/team_space/folder/rename',
    {
      folder_id: selectFolder.value?.id,
      name: folderName.value
    }
  )
  if (data.code === 0 && data.message === 'success') {
    getFolderList()
    folderRef.value?.getFolderList()
    renameFolderDialogVisible.value = false
    ElMessage.success(t('splitting.success'))
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}

// 创建文件夹
const createFolder = async () => {
  if (folderName.value === '') return
  const { data } = await post('/v1/team_space/folder',
    {
      parent_id: null,
      name: folderName.value
    }
  )
  if (data.code === 0 && data.message === 'success') {
    await getFolderList()
    folderRef.value?.getFolderList()
    folderName.value = ''
    createFolderDialogVisible.value = false
    ElMessage.success(t('splitting.success'))
    if (onMove.value) {
      fileTableRef.value.getFolderList()
    } else {
      activeFolder.value = data.data.id
      if (active.value === 'team') {
        active.value = 'folder'
      }
      const folder = folderList.value.find(f => f.id === activeFolder.value)
      fileTableRef.value.changeFolderId(activeFolder.value, folder?.role)
    }
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}

const provideChangeFolder = async (val: string) => {
  await getFolderList()
  changeFolder(val)
}
provide('provideGetFolderList', getFolderList)
provide('provideChangeFolder', provideChangeFolder)

// 下载文件
const downloadFile = async (id: string) => {
  try {
    const res = await request({
      method: 'post',
      url: '/v1/team_space/file/download',
      data: {
        folder_id: [id]
      },
      responseType: 'blob'
    })
    const contentType = (res.headers?.['content-type'] as string | undefined) ?? 'application/octet-stream'

    // 服务端返回 JSON 时说明是错误响应，解析后提示
    if (contentType.includes('application/json')) {
      const text = res.data instanceof Blob ? await res.data.text() : JSON.stringify(res.data)
      const json = JSON.parse(text)
      json.code === 102 && ElMessage.error(t('third_party_auth.folder_empty'))
      return
    }

    const disposition = (res.headers?.['content-disposition'] as string | undefined) ?? ''
    const filenameMatch = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
    const filenameRaw = filenameMatch?.[1] ?? filenameMatch?.[2]
    const fallbackName = folderList.value.find(f => f.id === id)?.name || 'download'
    const filename = filenameRaw ? decodeURIComponent(filenameRaw) : fallbackName

    const blob = res.data instanceof Blob ? res.data : new Blob([res.data], { type: contentType })
    saveAs(blob, filename)
    ElMessage.success(t('splitting.success'))
  } catch (error) {
    ElMessage.error(t('splitting.fail'))
  }
}

const dragover = ref(false)
const fileList = ref<File[]>([])
const input = ref<HTMLInputElement | null>(null)

const base = getSystemBaseUnit()
const MAX_SIZE = base * base * 100 // 10MB
const MAX_COUNT = 32
GlobalWorkerOptions.workerSrc = '/lib/pdf.worker.min.mjs'
const CMAP_URL = '/lib/cmaps/'
// 校验上传文件
const SUPPORTED_EXTENSIONS = ['.png', '.jpg', '.jpeg', '.tiff', '.bmp', '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.csv', '.ppt', '.pptx', '.txt']

const validateFiles = async (files: FileList): Promise<globalThis.File[] | null> => {
  const fileArray = Array.from(files)

  const unsupported = fileArray.filter(file => {
    const ext = file.name.slice(file.name.lastIndexOf('.')).toLowerCase()
    return !SUPPORTED_EXTENSIONS.includes(ext)
  })
  if (unsupported.length > 0) {
    ElMessage.error(t('bulkExtract.notSupport'))
    return null
  }

  const oversized = fileArray.filter(file => file.size > MAX_SIZE)
  if (oversized.length > 0) {
    ElMessage.error(t('knowledgeBases.dataset.larger'))
    return null
  }

  if (fileArray.length > MAX_COUNT) {
    ElMessage.error(t('knowledgeBases.dataset.max'))
    return null
  }

  const results = await Promise.all(fileArray.map(async file => {
    if (file.name.endsWith('.pdf')) {
      const isProtected = await checkPassword(file)
      return ({ file, isProtected })
    } else {
      return Promise.resolve({ file, isProtected: false })
    }
  }))
  const unprotectedFiles = results.filter(result => !result.isProtected).map(result => result.file)
  if (results.length !== unprotectedFiles.length) {
    ElMessage.warning(t('bulkExtract.encryptTip'))
  }

  return unprotectedFiles
}

// 检查文档是否受密码保护
const checkPassword = async (file: globalThis.File): Promise<boolean> => {
  const arrayBuffer = await file.arrayBuffer()
  const parameters = {
    cMapUrl: CMAP_URL,
    cMapPacked: true,
    enableXfa: true,
    data: arrayBuffer
  }
  const loadingTask = getDocument(parameters)

  return new Promise((resolve, _reject) => {
    loadingTask.promise.then(() => {
      resolve(false)
    }).catch(error => {
      if (error.name === 'PasswordException') {
        resolve(true)
      } else {
        console.warn('File ' + file.name + ': ' + error.message)
        resolve(false)
      }
    })
  })
}

// 判断文件是否重复：根据 name 和 size 判断
const isDuplicate = (file: any, list?: any[]): boolean => {
  if (!Array.isArray(list)) return false
  return list.some(item => item.name === file.name && item.size === file.size)
}

const onDrop = async (e: DragEvent) => {
  e.preventDefault()
  const files = e.dataTransfer?.files
  dragover.value = false
  if (!files || files.length === 0) return

  const validFiles = await validateFiles(files)
  if (!validFiles) {
    if (input.value) input.value.value = '' // 重置 file input
    return
  }

  // 去重后 push
  validFiles.forEach((file: any) => {
    if (!isDuplicate(file, fileList.value)) {
      fileList.value.push(file)
    }
  })
}

const leave = () => {
  dragover.value = false
}

const deleteUploadFile = (index: number) => {
  fileList.value.splice(index, 1)
}

// 点击上传文件
const handleChange = async (e: Event) => {
  const inputEl = e.target as HTMLInputElement
  const files = inputEl.files
  if (!files || files.length === 0) return

  const validFiles = await validateFiles(files)
  if (!validFiles) {
    inputEl.value = ''
    return
  }

  // 去重后 push
  validFiles.forEach((file: any) => {
    if (!isDuplicate(file, fileList.value)) {
      fileList.value.push(file)
    }
  })
  inputEl.value = ''
}

type UploadSummary = {
  total: number
  success: number
  failed: number
  allSucceeded: boolean
}

const uploadAllFiles = async (files: File[]): Promise<UploadSummary> => {
  const tasks = files.map((file) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folder_id', selectFile.value?.folder_id || '')
    return post('/v1/team_space/file/upload', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
  })

  const results = await Promise.allSettled(tasks)
  const success = results.filter(r => r.status === 'fulfilled').length
  const failed = results.length - success
  return {
    total: results.length,
    success,
    failed,
    allSucceeded: failed === 0
  }
}

// 拆分上传文件
const upload = async () => {
  if (loading.value || !fileList.value.length) return
  loading.value = true

  try {
    const summary = await uploadAllFiles(fileList.value)
    if (summary.allSucceeded) {
      ElMessage.success(t('splitting.success'))
      fileList.value = []
      dialogVisible.value = false
      getFolderFileList()
    } else {
      ElMessage.error(`${t('splitting.fail')} (${summary.failed}/${summary.total})`)
    }
  } catch {
    ElMessage.error(t('splitting.fail'))
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.folder-search {
  :deep(.el-input__wrapper) {
    position: relative;
  }
  :deep(.el-input__suffix) {
    position: absolute;
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
  }
}
.date {
  padding: 12px;
  text-align: left;
  position: relative;
  border-radius: 8px;
  .date-title {
    font-size: 14px;
    font-weight: 600;
    line-height: 20px;
    color: #0C131F;
    margin-bottom: 12px;
  }
  .tag-content {
    display: flex;
    .date-tag {
      font-size: 14px;
      cursor: pointer;
      font-weight: 500;
      padding: 4px 12px;
      line-height: 20px;
      color: #404653;
      white-space: nowrap;
      border-radius: 100px;
      background-color: #EBEDF0;
      & + .date-tag {
        margin-left: 12px;
      }
      &.active {
        color: white;
        background-color: #396FFA;
      }
    }
  }
  .select {
    display: flex;
    font-size: 14px;
    margin-top: 12px;
    line-height: 20px;
    color: #404653;
    font-weight: normal;
    align-items: center;
    white-space: nowrap;
    .input {
      width: 100%;
      cursor: pointer;
      font-size: 14px;
      margin-left: 8px;
      line-height: 20px;
      padding: 6px 12px;
      color: #888C94;
      position: relative;
      border-radius: 4px;
      padding-right: 28px;
      border: 1px solid #E2E3E5;
      svg {
        top: 8px;
        right: 12px;
        cursor: pointer;
        position: absolute;
      }
    }
  }
  .bottom {
    display: flex;
    font-size: 14px;
    margin-top: 20px;
    line-height: 20px;
    justify-content: flex-end;
    .ok {
      cursor: pointer;
      padding: 2px 8px;
      color: white;
      border-radius: 6px;
      background-color: #396FFA;
    }
    .clear {
      cursor: pointer;
      padding: 2px 8px;
      margin-left: 8px;
      color: #1F2633;
      border-radius: 6px;
      border: 1px solid #1F2633;
    }
  }
}
:deep(.el-checkbox-group) {
  display: flex;
  margin: 4px 0;
  flex-direction: column;
  .el-checkbox {
    height: auto;
    margin-right: 0;
    padding: 8px 16px;
    color: #404653;
    &:hover {
      background-color: #F6F6FB;
    }
    &.is-checked .el-checkbox__label {
      color: #404653;
    }
  }
}
.document-extraction * {
  font-family: 'Encode Sans';
}
:deep() {
   .el-input.max-w-218px .el-input__wrapper .el-input__inner {
    max-width: 165px;
    min-width: 165px;
    padding-right: 12px;
  }
}
.assistant-shadow {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
:deep() {
  svg.filter {
    rect {
      display: none;
    }
    path {
      fill: #94969D;
    }
    &:hover {
      path {
        fill: #396FFA;
      }
    }
    &.active {
      rect {
        display: unset;
      }
      path {
        fill: #396FFA;
      }
    }
  }
  .fileOption {
    rect:first-child {
      display: none;
    }
    rect:nth-child(2) {
      display: none;
    }
    &:hover {
      rect:first-child {
        display: unset;
      }
      rect:nth-child(2) {
        display: unset;
      }
    }
    &.active {
      rect:first-child {
        display: unset;
      }
      rect:nth-child(2) {
        display: unset;
      }
    }
  }
  .downloadFile {
    rect:first-child {
      display: none;
    }
    rect:nth-child(2) {
      display: none;
    }
    &:hover {
      rect:first-child {
        display: unset;
      }
      rect:nth-child(2) {
        display: unset;
      }
    }
  }
}
.template {
  :deep() {
    svg.option {
      rect {
        fill: white;
      }
      g path {
        fill: #888C94;
      }
      &:hover {
        rect {
          fill: #F3F6FF;
        }
      }
    }
  }
  &.active {
    :deep() {
      svg.option {
        rect {
          fill: #D7E2FE;
        }
        &:hover {
          rect {
            fill: #F3F6FF;
          }
        }
      }
    }
  }
}
</style>

<style lang="scss">
.el-popover {
  &.dateTip {
    padding: 0;
    margin-top: 16px !important;
  }
  &.action {
    padding: 0;
    margin-top: 0;
    white-space: nowrap;
  }
}
</style>
