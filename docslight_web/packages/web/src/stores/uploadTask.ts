import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// 任务来源页面类型
export type UploadTaskType = 'extraction' | 'parsing' | 'splitting' | 'other'

// 单个文件的状态
export type UploadFileStatus =
  | 'uploading'   // 上传中（文件正在传输到服务器）
  | 'processing'  // 处理中（服务器解析/抽取中）
  | 'success'     // 成功
  | 'fail'        // 处理失败
  | 'uploadFail'  // 上传失败
  | 'pending'     // 等待中（已上传，等待开始处理）
  | 'paused'      // 已暂停（用户手动暂停抽取/解析）

export interface UploadFileItem {
  /** 唯一标识，服务端返回的 fileId；上传前可用临时 uuid */
  id: string
  /** 文件名 */
  name: string
  /** 当前状态 */
  status: UploadFileStatus
  /** 进度 0-100（可选，用于 processing 状态展示） */
  progress?: number
  /** 所属任务类型，用于 tab 筛选 */
  type: UploadTaskType
  /** 失败原因（可选） */
  failReason?: string
  /** 添加时间（用于排序） */
  addedAt: number
}

export const useUploadTaskStore = defineStore('uploadTask', () => {
  const fileList = ref<UploadFileItem[]>([])

  /**
   * 页面加载时调用：将因刷新而中断的 uploading 条目标记为 uploadFail。
   * pending/processing 状态保留，由各页面的 onMounted → getTableData 接管轮询。
   */
  const fixInterruptedOnLoad = () => {
    fileList.value.forEach(f => {
      if (f.status === 'uploading') {
        f.status = 'uploadFail'
      }
    })
  }

  // ---- Getters ----

  const totalCount = computed(() => fileList.value.length)

  const uploadingCount = computed(() =>
    fileList.value.filter(f => f.status === 'uploading' || f.status === 'processing').length
  )

  const getByType = (type: UploadTaskType | 'all') => {
    if (type === 'all') return fileList.value
    return fileList.value.filter(f => f.type === type)
  }

  // ---- Actions ----

  /**
   * 添加一个文件到上传列表（若同 id 已存在则忽略）
   */
  const addFile = (file: Omit<UploadFileItem, 'addedAt'>) => {
    if (fileList.value.find(f => f.id === file.id)) return
    fileList.value.unshift({ ...file, addedAt: Date.now() })
  }

  /**
   * 批量添加文件
   */
  const addFiles = (files: Omit<UploadFileItem, 'addedAt'>[]) => {
    files.forEach(f => addFile(f))
  }

  /**
   * 更新某个文件的状态/进度
   */
  const updateFile = (id: string, patch: Partial<Pick<UploadFileItem, 'status' | 'progress' | 'failReason'>>) => {
    const target = fileList.value.find(f => f.id === id)
    if (target) {
      Object.assign(target, patch)
      if (patch.status === 'success') {
        setTimeout(() => removeFile(id), 3000)
      }
    }
  }

  /**
   * 删除某个文件
   */
  const removeFile = (id: string) => {
    const idx = fileList.value.findIndex(f => f.id === id)
    if (idx !== -1) fileList.value.splice(idx, 1)
  }

  /**
   * 清除所有已完成（success/fail/uploadFail）的文件
   */
  const clearFinished = () => {
    fileList.value = fileList.value.filter(
      f => f.status === 'uploading' || f.status === 'processing' || f.status === 'pending' || f.status === 'paused'
    )
  }

  /**
   * 清除全部
   */
  const clearAll = () => {
    fileList.value = []
  }

  /**
   * 按类型清除文件：'all' 清除全部，否则只清除指定类型
   */
  const clearByType = (type: UploadTaskType | 'all') => {
    if (type === 'all') {
      fileList.value = []
    } else {
      fileList.value = fileList.value.filter(f => f.type !== type)
    }
  }

  /**
   * 暂停文件的抽取/解析处理。
   * 调用后端 /api/idp/file-pause 接口，成功后将状态更新为 paused。
   * 仅对 processing 状态的文件有效。
   */
  const pauseFile = async (id: string): Promise<boolean> => {
    const target = fileList.value.find(f => f.id === id)
    if (!target || target.status !== 'processing') return false
    try {
      const { get } = await import('@/utils/request')
      await get(`/api/idp/file-pause?fileIds=${id}`)
      target.status = 'paused'
      return true
    } catch (err) {
      console.error('[uploadTask] pauseFile failed:', err)
      return false
    }
  }

  return {
    fileList,
    totalCount,
    uploadingCount,
    getByType,
    addFile,
    addFiles,
    updateFile,
    removeFile,
    clearFinished,
    clearAll,
    clearByType,
    pauseFile,
    fixInterruptedOnLoad,
  }
}, {
  persist: true,
})
