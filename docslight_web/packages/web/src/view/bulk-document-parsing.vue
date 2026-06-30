<template>
  <div class="relative flex bg-[#F2F2F2] <lg:(bg-[#F3F6FF] h-100vh)" @keydown="handleKeyDown">
    <div class="fixed z-2 flex items-center border-b border-[#E1E3E8] bg-white w-full py-22px pl-32px">
      <div class="text-20px leading-28px text-brand-0 mr-12px">{{ t('common.idp') }}</div>
      <div class="flex rounded-8px text-xs p-4px bg-[#EBF1FE]">
        <a href="/single-document-parsing" class="flex px-12px py-4px items-center rounded-4px cursor-pointer">
          <Document class="mr-8px" />{{ t('bulkExtract.single') }}
        </a>
        <a class="flex px-12px py-4px items-center rounded-4px cursor-pointer bg-white">
          <Bulk class="mr-8px" />{{ t('bulkExtract.bulk') }}
        </a>
      </div>
    </div>

    <div class="<lg:hidden w-full h-[calc(100vh-80px)] mt-80px">
      <el-splitter>
        <el-splitter-panel size="50%">
          <div v-if="!fileList.length" class="w-full h-full p-20px">
            <div @drop.prevent="onDrop" @dragover.prevent="dragover = true" @dragleave.prevent="dragover = false" :class="dragover && 'bg-[#F3F6FF]'" class="w-full h-full rounded-12px border-dashed border-1 border-brand-2 flex justify-center items-center flex-col">
              <div class="cursor-pointer relative text-sm">
                <div @click="input.click" class="w-300px rounded-6px mx-auto mt-106px cursor-pointer bg-[#396FFA] text-white text-sm font-600 py-10px px-12px flex items-center justify-center hover:bg-[#244FF0]">
                  <Upload class="mr-12px w-20px h-20px" />
                  {{ t('knowledgeBases.dataset.selectFile[0]') }}
                </div>
                <div class="text-center my-8px text-xs text-brand-3">
                  {{ t('knowledgeBases.dataset.selectFile[1]') }}
                </div>
                <div class="text-center text-xs text-brand-3">
                  {{ t('knowledgeBases.dataset.selectFile[2]') }}
                </div>
              </div>
              <input ref="input" class="hidden" type="file" name="file" accept=".pdf" @change="handleChange" multiple>
            </div>
          </div>

          <div v-else class="w-full h-full">
            <BulkFileList :fileList="fileList" :processStatus="processStatus" :taskId="taskId" @changeExtractStatus="changeExtractStatus" />
          </div>
        </el-splitter-panel>

        <el-splitter-panel size="50%">
          <div class="w-full h-full flex flex-col justify-between bg-white">
            <div class="w-full h-full border-b border-[#E1E3E8] p-40px overflow-y-auto">
              <div class="flex items-center mb-20px">
                <span @click="menuActive = 0" class="text-14px leading-20px cursor-pointer hover:text-[#2E59C8]" :class="menuActive === 0 ? 'text-[#396FFA]' : 'text-[#94969D]'">{{ t('bulkParse.setParams') }}</span>
                <ArrowRight class="mx-12px" />
                <span @click="menuActive = 1" class="text-14px leading-20px cursor-pointer hover:text-[#2E59C8]" :class="{ 'text-[#396FFA]': menuActive === 1, 'text-[#94969D]': menuActive !== 1, 'text-[#CCC] pointer-events-none': !processStatus }">{{ t('bulkExtract.processingProgress') }}</span>
              </div>

              <div v-show="menuActive === 0" class="py-20px">
                <p class="mb-12px text-20px leading-28px font-600 text-[#43474D]">{{ t('bulkParse.setParams') }}</p>
                <p class="mt-28px mb-20px text-16px leading-24px font-600 text-[#232748]"><span class="mr-4px text-14px leading-20px text-[#FF5050]">*</span>{{ t('bulkParse.includeContent') }}</p>
                <el-radio-group v-model="editableTabs.resolveType" :disabled="isDisableParams">
                  <el-radio value="all">{{ t('bulkParse.allContent') }}</el-radio>
                  <el-radio value="text">{{ t('bulkParse.onlyText') }}</el-radio>
                  <el-radio value="table">{{ t('bulkParse.onlyTable') }}</el-radio>
                  <el-radio value="image">{{ t('bulkParse.onlyImage') }}</el-radio>
                </el-radio-group>

                <template v-if="editableTabs.resolveType === 'image'">
                  <p class="mt-20px mb-8px py-8px text-16px leading-24px font-600 text-[#232748]"><span class="mr-4px text-14px leading-20px text-[#FF5050]">*</span>{{ t('bulkParse.imageObject') }}</p>
                  <div class="flex flex-col items-start gap-12px">
                    <el-checkbox v-model="editableTabs.pageImageObj" :label="t('bulkParse.embeddedImages')" :disabled="isDisableParams" class="set-params" />
                    <el-checkbox v-model="editableTabs.pageToImage" :label="t('bulkParse.pageToImage')" :disabled="isDisableParams" class="set-params" />
                  </div>
                  <template v-if="editableTabs.pageImageObj">
                    <p class="mt-20px mb-8px py-8px text-16px leading-24px font-600 text-[#232748]"><span class="mr-4px text-14px leading-20px text-[#FF5050]">*</span>{{ t('singleExtract.download') }}</p>
                    <el-radio-group v-model="editableTabs.isOnlyImage" :disabled="isDisableParams" class="flex !flex-col !gap-12px">
                      <el-radio :value="true">{{ t('bulkParse.onlyImage') }}{{ t('bulkParse.png') }}</el-radio>
                      <el-radio :value="false">{{ t('bulkParse.imageInfo') }}</el-radio>
                    </el-radio-group>
                  </template>
                </template>

                <p class="mt-20px mb-8px py-8px text-16px leading-24px font-600 text-[#232748]"><span class="mr-4px text-14px leading-20px text-[#FF5050]">*</span>{{ t('bulkParse.ocrLanguage') }}</p>
                <div class="h-36px mb-8px flex items-center">
                  <span class="inline-block min-w-120px mr-24px text-14px leading-20px text-[#52555F]">{{ t('bulkParse.allowOcr') }}</span>
                  <el-switch v-model="editableTabs.enableOCR" style="--el-switch-on-color: #396FFA; --el-switch-off-color: #CED6E1"
                    :active-value="true" :inactive-value="false" :disabled="isDisableParams">
                  </el-switch>
                </div>
                <div class="h-40px mb-8px flex items-center">
                  <span class="inline-block min-w-120px mr-12px text-14px leading-20px text-[#52555F]">{{ t('bulkParse.ocrLanguage') }}</span>
                  <el-select v-model="editableTabs.ocrLanguage" :disabled="isDisableParams || !editableTabs.enableOCR">
                    <el-option
                      v-for="item in ocrLanguageOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </div>

                <template v-if="editableTabs.resolveType !== 'image'">
                  <p class="mt-28px mb-20px text-16px leading-24px font-600 text-[#232748]"><span class="mr-4px text-14px leading-20px text-[#FF5050]">*</span>{{ t('bulkExtract.saveIn') }}</p>
                  <el-radio-group v-model="editableTabs.outType" :disabled="isDisableParams">
                    <template v-if="editableTabs.resolveType === 'table'">
                      <el-radio value="excel">Excel</el-radio>
                      <el-radio value="csv">CSV</el-radio>
                    </template>
                    <el-radio value="txt" v-if="editableTabs.resolveType === 'text'">TXT</el-radio>
                    <el-radio value="json">JSON</el-radio>
                    <el-radio value="md">Markdown</el-radio>
                    <el-radio value="txt" v-if="editableTabs.resolveType === 'table'">TXT</el-radio>
                  </el-radio-group>
                </template>
              </div>

              <div v-show="menuActive === 1" class="py-20px">
                <p class="mb-12px text-20px leading-28px font-600 text-[#43474D]">{{ t('bulkExtract.processingProgress') }}</p>
                <div class="mb-24px flex items-center">
                  <span class="text-16px leading-24px text-[#232748]">{{ t('bulkExtract.filesProcessed') }}<span :class="process.percent === 100 && 'text-[#00CF85]'">{{ process.percent }}%</span></span>
                  <div class="ml-16px relative">
                    <img src="../assets/images/icons/info.svg" @mouseenter="showInfo = true" @mouseleave="showInfo = false" />
                    <div class="info absolute bottom-26px left-0 p-4px w-177px bg-[#F2F3F5] border border-[#D9D9D9] rounded-4px text-11px leading-16px text-[#43474D]" :class="showInfo && '!opacity-100'">{{ t('bulkExtract.filesProcessedDesc') }}</div>
                  </div>
                </div>
                <div class="border border-[#E1E3E8]">
                  <div class="py-10px pr-20px bg-[#F5F7F9] flex justify-between">
                    <div class="flex-1 h-32px text-center border-r border-r-[#E1E3E8] text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.pending') }}</div>
                    <div class="flex-1 h-32px text-center border-r border-r-[#E1E3E8] text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.success') }}</div>
                    <div class="flex-1 h-32px text-center border-r border-r-[#E1E3E8] text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.fail') }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.stopped') }}</div>
                  </div>
                  <div class="py-10px pr-20px flex justify-between">
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F]">{{ process.pendingAmount }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F]">{{ process.successAmount }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px" :class="process.failAmount ? 'text-[#F87171]' : 'text-[#52555F]'">{{ process.failAmount }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F]">{{ process.stoppedAmount }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="processStatus !== 1" class="min-h-80px py-10px w-full flex justify-center items-center <lg:hidden">
              <div v-if="processStatus === 2" class="flex justify-center">
                <div @click="download" class="cursor-pointer w-187px py-12px text-center rounded-6px text-16px leading-24px font-500 bg-[#396FFA] text-white border border-[#396FFA] hover:opacity-80 mr-16px">{{ isDownloaded ? t('bulkExtract.reDownload') : t('knowledgeBases.dataset.download') }}</div>
                <div v-show="isDownloaded" @click="fileList.length = 0" class="cursor-pointer w-187px py-12px text-center rounded-6px text-16px leading-24px font-500 bg-white text-[#396FFA] border border-[#396FFA] hover:(bg-[#396FFA] text-white)">{{ t('bulkExtract.reUpload') }}</div>
              </div>
              <div @click="uploadClick" v-if="!processStatus" class="relative z-1 w-162px flex justify-center items-center rounded-2px" :class="(!taskId || process.noUploadAmount || process.uploadFailAmount === fileList.length) && 'opacity-40 pointer-events-none'">
                <button type="button" class="button">
                  <div class="points_wrapper">
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                  </div>
                  <span class="inner"><Light class="icon" />{{ t('bulkParse.startParsing') }}</span>
                </button>
              </div>
            </div>
          </div>
        </el-splitter-panel>
      </el-splitter>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, provide, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElLoading } from 'element-plus'
import { get, post } from '@/utils/request'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import { getSystemBaseUnit } from '../utils/tools'

interface FileEntry {
  id: string
  index: number
  name: string
  status: string
  file?: File
  failureReason?: string
  totalPageCount?: number
  currentPageCount?: number
}

GlobalWorkerOptions.workerSrc = '/lib/pdf.worker.min.mjs'
const CMAP_URL = '/lib/cmaps/'

const { t } = useI18n()
const fileList = ref<FileEntry[]>([])
const dragover = ref(false)
const input = ref()
const menuActive = ref(0)
const editableTabs = ref({
  resolveType: 'all',
  enableOCR: false,
  ocrLanguage: 'auto',
  outType: 'json',
  pageImageObj: true,
  pageToImage: false,
  isOnlyImage: false,
})
const showInfo = ref(false)
const processStatus = ref(0) // 0:未开始处理 1:处理中 2:处理完成
const taskId = ref('')
const isDownloaded = ref(false)
const statusMap = {
  0: 'pending',
  1: 'processing',
  2: 'success',
  3: 'fail',
  4: 'delete',
  5: 'stopped'
}
const ocrLanguageOptions = [
  {
    value: 'auto',
    label: 'Auto',
  },
  {
    value: 'english',
    label: 'English',
  },
  {
    value: 'chinese',
    label: 'Simplified Chinese',
  },
  {
    value: 'chinese_tra',
    label: 'Traditional Chinese',
  },
  {
    value: 'korean',
    label: 'Korean',
  },
  {
    value: 'japanese',
    label: 'Japanese',
  },
  {
    value: 'latin',
    label: 'Latin',
  },
  {
    value: 'devanagari',
    label: 'Sanskrit Alphabet',
  },
]

const process = computed(() => {
  let pending = 0
  let success = 0
  let fail = 0
  let uploadFail = 0
  let stopped = 0
  let percent = 0
  let noUpload = 0

  fileList.value.forEach(item => {
    if (item.status === 'pending' || item.status === 'processing') pending++
    if (item.status === 'success') success++
    if (item.status === 'fail') fail++
    if (item.status === 'uploadFail') uploadFail++
    if (item.status === 'stopped') stopped++
    if (!item.status || item.status === 'uploading') noUpload++
  })
  percent = Math.round((fileList.value.length - pending - uploadFail) / (fileList.value.length - uploadFail) * 100)

  if (processStatus.value === 1 && pending === 0) processStatus.value = 2

  return {
    percent,
    pendingAmount: pending,
    successAmount: success,
    failAmount: fail,
    uploadFailAmount: uploadFail,
    stoppedAmount: stopped,
    noUploadAmount: noUpload
  }
})
const isDisableParams = computed(() => !fileList.value.length || !!processStatus.value || process.value.uploadFailAmount === fileList.value.length)

watch(() => fileList, (newList) => {
  if (!newList.value.length) {
    processStatus.value = 0
    menuActive.value = 0
    taskId.value = ''
  }
}, { deep: true })
watch(() => editableTabs.value.resolveType, (newVal) => {
  const defaultValueMap = {
    all: 'json',
    text: 'txt',
    table: 'excel'
  }
  editableTabs.value.outType = defaultValueMap[newVal as keyof typeof defaultValueMap] || editableTabs.value.outType
})
watch(() => editableTabs.value.enableOCR, (newVal) => {
  if (!newVal) editableTabs.value.ocrLanguage = 'auto'
})

onMounted(async () => {
  const res = await get('/api/idp/get-task-list?pageNum=1&pageSize=1')
  if (!res.data || res.data.code !== 200) return

  const id = res.data.data.records[0].id
  const params = JSON.parse(res.data.data.records[0].params)

  const taskRes = await get(`/api/idp/get-task-file-list?taskId=${id}`)
  if (!taskRes.data || taskRes.data.code !== 200) return

  const files = taskRes.data.data
  const extracting = files.some((file: any) => file.status === 0 || file.status === 1)
  if (!extracting) return

  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    fileList.value.push({
      id: file.fileId,
      index: i,
      name: file.fileName,
      status: statusMap[file.status as keyof typeof statusMap],
      totalPageCount: file.fileSchedule.totalPageCount,
      currentPageCount: file.fileSchedule.currentPageCount
    })
  }

  editableTabs.value.resolveType = params.resolveType
  editableTabs.value.outType = params.outType
  editableTabs.value.enableOCR = params.enableOCR
  editableTabs.value.ocrLanguage = params.ocrLanguage
  if (params.imageType === 'all') {
    editableTabs.value.pageImageObj = true
    editableTabs.value.pageToImage = true
  } else if (params.imageType === 'page_image_object') {
    editableTabs.value.pageImageObj = true
  } else if (params.imageType === 'page_png') {
    editableTabs.value.pageToImage = true
  }

  setTimeout(() => {
    processStatus.value = 1
    menuActive.value = 1
    taskId.value = id
  })
})

const handleKeyDown = (event: any) => {
  if (event.keyCode === 9) {
    event.preventDefault()
  }
}

// 拖拽上传文件
const onDrop = async (e: DragEvent) => {
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  pushToFileList(files)
}

// 选择上传文件
const handleChange = async (e: any) => {
  const files = e.target.files
  if (!files || files.length === 0) return
  pushToFileList(files)
}

// 将上传的文件传入列表
const pushToFileList = async (files: FileList) => {
  if (files.length > 32) {
    ElMessage.error(t('bulkExtract.fileLenTip'))
    return
  }
  const fileArr = Array.from(files)
  const totalSize = fileArr.reduce((sum, file) => sum + file.size, 0)
  const base = getSystemBaseUnit()

  if (totalSize > 100 * base * base) {
    ElMessage.error(t('bulkExtract.fileSizeTip'))
    return
  }

  const results = await Promise.all(fileArr.map(async file => {
    const nameArray = file.name.split('.')
    const fileType = nameArray[nameArray.length - 1].toLowerCase()

    if (fileType === 'pdf') {
      const isProtected = await checkPassword(file)
      return ({ file, isProtected })
    } else {
      ElMessage.error(t('bulkExtract.notSupport'))
      return null
    }
  }))
  const filteredResults = results.filter(result => result !== null)
  const unprotectedFiles = filteredResults.filter(result => !result.isProtected)
  if (filteredResults.length !== unprotectedFiles.length) {
    ElMessage.warning(t('bulkExtract.encryptTip'))
  }

  for (let i = 0; i < unprotectedFiles.length; i++) {
    const { file } = unprotectedFiles[i]
    fileList.value.push({
      id: '',
      index: i,
      name: file.name,
      status: '',
      file
    })
  }
  upload()
}

// 检查文档是否受密码保护
const checkPassword = async (file: File): Promise<boolean> => {
  const arrayBuffer = await file.arrayBuffer()
  const parameters = {
    cMapUrl: CMAP_URL,
    cMapPacked: true,
    enableXfa: true,
    data: arrayBuffer
  }
  const loadingTask = getDocument(parameters)

  return new Promise((resolve, reject) => {
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
provide('checkPassword', checkPassword)

// 接口创建任务并上传文件
const upload = async () => {
  const res = await get('/api/idp/create-task?taskType=LAYOUT')
  if (!res.data.data) return
  taskId.value = res.data.data

  fileList.value.forEach((item, index: number) => {
    item.status = 'uploading'
    const formdata = new FormData()
    formdata.append('taskId', taskId.value)
    formdata.append('order', index as any)
    item.file && formdata.append('file', item.file, item.name)
    post('/api/idp/file-upload', formdata).then((res: any) => {
      if (res.data.code === 200) {
        item.id = res.data.data
        item.status = 'pending'
      } else {
        item.status = 'uploadFail'
        item.failureReason = t('bulkExtract.uploadFail')
      }
    }).catch((err: any) => {
      console.log(err)
    })
  })
}

// 点击开始解析
const uploadClick = async () => {
  const data = {
    'taskId': taskId.value,
    // 'pages': [],
    'resolveType': editableTabs.value.resolveType,
    'outType': editableTabs.value.outType,
    'enableOCR': editableTabs.value.enableOCR,
    'ocrLanguage': editableTabs.value.ocrLanguage,
    'pdfPwd': '',
    'imageType': ''
  }
  if (editableTabs.value.resolveType === 'image') {
    data.outType = editableTabs.value.isOnlyImage ? 'isOnlyImage' : ''
    if (editableTabs.value.pageImageObj && editableTabs.value.pageToImage) {
      data.imageType = 'all'
    } else if (editableTabs.value.pageImageObj) {
      data.imageType = 'page_image_object'
    } else if (editableTabs.value.pageToImage) {
      data.imageType = 'page_png'
    }
  }
  const res = await post('/api/idp/task-start', data)
  if (res.data.code === 200 && res.data.message === 'success') {
    processStatus.value = 1
    menuActive.value = 1
  }
}

// 下载所有文件结果
const download = () => {
  const loadingInstance = ElLoading.service()

  get(`/api/idp/down-all-files?taskId=${taskId.value}`, {
    responseType: 'blob'
  }).then(async (res: any) => {
    const disposition = res.headers['content-disposition']
    const regExp = /filename=([^;]*)/i
    const match = disposition.match(regExp)
    const filename = match ? match[1].replace(/"/g, '').trim() : 'compdf_ai-batch-parsing-' + getCurrentDate()

    const blob = new Blob([res.data], { type: res.headers['content-type'] })
    const url = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    isDownloaded.value = true
    loadingInstance.close()
  }).catch((err: any) => {
    console.log(err)
    loadingInstance.close()
    ElMessage.error(t('singleParse.downloadFailed'))
  })
}

// 获取当前日期 YYYYMMDD
const getCurrentDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  
  return `${year}${month}${day}`
}

// 子组件修改提取状态
const changeExtractStatus = (val: number) => {
  processStatus.value = val
}
</script>

<style lang="scss" scoped>
.info {
  box-shadow: 0px 4px 4px 0px #00000033;
  transition: all 0.5s ease-out;
  opacity: 0;
  pointer-events: none;
}
/* From Uiverse.io by ilkhoeri */ 
.button {
  width: 100%;
  height: 40px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.25s ease;
  background: radial-gradient(65.28% 65.28% at 50% 100%, #7199FF, rgba(223, 113, 255, 0) 100%), linear-gradient(0deg, #396FFA, #396FFA);
  border-radius: 2px;
  border: none;
  outline: none;
}
.button::before,
.button::after {
  content: "";
  position: absolute;
  inset: var(--space);
  transition: all 0.5s ease-in-out;
  border-radius: calc(var(--round) - var(--space));
  z-index: 0;
}
.button::before {
  --space: 1px;
  background: linear-gradient(
    177.95deg,
    rgba(255, 255, 255, 0.19) 0%,
    rgba(255, 255, 255, 0) 100%
  );
}
.button::after {
  --space: 2px;
  background: radial-gradient(
      65.28% 65.28% at 50% 100%,
      #7199FF,
      rgba(223, 113, 255, 0) 100%
    ),
    linear-gradient(0deg, #396FFA, #396FFA);
}
.button:active {
  transform: scale(0.95);
}
.button:focus svg.icon {
  fill: white;
}
.button:hover svg.icon {
  fill: transparent;
  animation:
    dasharray 1s linear forwards,
    filled 0.1s linear forwards 0.95s;
}
@keyframes dasharray {
  from {
    stroke-dasharray: 0 0 0 0;
  }
  to {
    stroke-dasharray: 68 68 0 0;
  }
}
@keyframes filled {
  to {
    fill: white;
  }
}
.inner {
  z-index: 2;
  gap: 6px;
  position: relative;
  width: 100%;
  color: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  line-height: 16px;
  transition: color 0.2s ease-in-out;
}
.inner svg.icon {
  width: 18px;
  height: 18px;
  transition: fill 0.1s linear;
}
.points_wrapper {
  overflow: hidden;
  width: 100%;
  height: 100%;
  pointer-events: none;
  position: absolute;
  z-index: 1;
}

.points_wrapper .point {
  bottom: -10px;
  position: absolute;
  animation: floating-points infinite ease-in-out;
  pointer-events: none;
  width: 4px;
  height: 4px;
  background-color: #fff;
  border-radius: 9999px;
}
@keyframes floating-points {
  0% {
    transform: translateY(0);
  }
  85% {
    opacity: 0;
  }
  100% {
    transform: translateY(-55px);
    opacity: 0;
  }
}
.points_wrapper .point:nth-child(1) {
  left: 10%;
  opacity: 1;
  animation-duration: 2.35s;
  animation-delay: 0.2s;
}
.points_wrapper .point:nth-child(2) {
  left: 30%;
  opacity: 0.7;
  animation-duration: 2.5s;
  animation-delay: 0.5s;
}
.points_wrapper .point:nth-child(3) {
  left: 25%;
  opacity: 0.8;
  animation-duration: 2.2s;
  animation-delay: 0.1s;
}
.points_wrapper .point:nth-child(4) {
  left: 44%;
  opacity: 0.6;
  animation-duration: 2.05s;
}
.points_wrapper .point:nth-child(5) {
  left: 50%;
  opacity: 1;
  animation-duration: 1.9s;
}
.points_wrapper .point:nth-child(6) {
  left: 75%;
  opacity: 0.5;
  animation-duration: 1.5s;
  animation-delay: 1.5s;
}
.points_wrapper .point:nth-child(7) {
  left: 88%;
  opacity: 0.9;
  animation-duration: 2.2s;
  animation-delay: 0.2s;
}
.points_wrapper .point:nth-child(8) {
  left: 58%;
  opacity: 0.8;
  animation-duration: 2.25s;
  animation-delay: 0.2s;
}
.points_wrapper .point:nth-child(9) {
  left: 98%;
  opacity: 0.6;
  animation-duration: 2.6s;
  animation-delay: 0.1s;
}
.points_wrapper .point:nth-child(10) {
  left: 65%;
  opacity: 1;
  animation-duration: 2.5s;
  animation-delay: 0.2s;
}

:deep(.el-splitter) {
  .el-splitter-bar {
    width: 12px !important;
    background-color: #D7D7D7;
  }
  .el-splitter-bar__dragger {
    height: 60px !important;
    &:before {
      background-color: #666;
    }
    &:hover:before {
      background-color: #A0CFFF;
    }
  }
  .el-splitter-bar__dragger-active:before {
    background-color: #A0CFFF;
  }
  .el-splitter-bar__dragger-horizontal:before {
    width: 4px;
  }
}

:deep(.el-radio-group) {
  display: flex;
  flex-direction: row;
  gap: 20px;
  .el-radio {
    padding: 0;
    min-width: 120px;
    height: 20px;
    &:hover {
      background-color: unset;
    }
    .el-radio__inner {
      border: 2px solid #AAAEB2;
    }
    .el-radio__label {
      color: #52555F;
      font-weight: 400;
    }
    .el-radio__input.is-checked:not(.is-checked) + .el-radio__label,
    .el-radio__input.is-checked:not(.is-disabled) + .el-radio__label {
      color: #52555F;
    }
    &.is-disabled {
      cursor: not-allowed;
    }
    &.is-disabled:not(.is-checked) .el-radio__inner {
      background-color: transparent;
      opacity: 0.5;
    }
    &.is-disabled .el-radio__label {
      color: #97999F;
    }
  }
  .el-radio__input.is-disabled {
    cursor: not-allowed;
    &.is-checked {
      opacity: 0.5;
    }
  }
}

:deep(.el-select) {
  max-width: 416px;
  .el-select__placeholder, .el-select__caret {
    color: #232748;
  }
}

:deep(.el-checkbox.set-params) {
  height: 20px;
  .el-checkbox__label {
    color: #52555F;
    font-weight: 400;
  }
  .el-checkbox__input.is-checked:not(.is-disabled) + .el-checkbox__label {
    color: #52555F;
  }
  &.is-disabled:not(.is-checked) .el-checkbox__inner {
    background-color: transparent;
    opacity: 0.5;
  }
  &.is-disabled .el-checkbox__label {
    color: #97999F;
  }
  .el-checkbox__input.is-disabled {
    cursor: not-allowed;
    &.is-checked {
      opacity: 0.5;
    }
  }
}
</style>

<style lang="scss">
.el-message {
  min-width: unset;
  top: 112px !important;
  padding: 8px 16px;

  .el-message__content {
    font-family: 'Encode Sans';
    font-size: 16px;
    line-height: 24px;
    color: #232748;
  }

  .el-icon-error {
    color: #F87171;
    font-size: 20px;
  }
}
</style>