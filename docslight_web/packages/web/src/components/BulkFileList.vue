<template>
  <div class="p-32px w-full h-full flex flex-col">
    <div class="mb-20px flex items-center justify-between">
      <p class="text-20px leading-28px font-600 text-[#43474D]">{{ t('bulkExtract.fileList') }}</p>
      <div v-if="!processStatus" class="flex">
        <div @click="deleteFiles" class="p-10px mr-12px flex items-center justify-center rounded-6px bg-[#F87171] text-white text-16px leading-24px font-500 cursor-pointer hover:opacity-80" :class="!indexSelection.length && 'opacity-50 pointer-events-none'"><DeleteBtn class="mr-10px" />{{ t('bulkExtract.deleteBulk') }}</div>
        <div @click="input.click" class="py-12px min-w-140px flex items-center justify-center rounded-6px bg-[#396FFA] text-white text-14px leading-16px font-700 cursor-pointer hover:opacity-80" :class="fileList.length === 32 && 'opacity-50 pointer-events-none'">
          <Upload class="mr-2px" />{{ t('bulkExtract.uploadFile') }}
          <input ref="input" class="hidden" type="file" name="file" accept=".pdf, .png, .jpg, .jpeg" @change="handleChange" multiple>
        </div>
      </div>
      <el-dropdown v-else trigger="click" popper-class="bulk-action">
        <div class="w-160px h-40px flex items-center justify-between px-12px py-10px bg-white rounded-4px text-14px leading-20px text-[#52555F] border hover:border-[#396FFA]" :class="!indexSelection.length && 'opacity-50 pointer-events-none'">{{ t('bulkExtract.bulkAction') }}<ArrowDown /></div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="deleteFiles"><DeleteNoOutLine /><span class="ml-4px text-14px leading-20px text-[#232748]">{{ t('bulkExtract.deleteBulk') }}</span></el-dropdown-item>
            <el-dropdown-item @click="startFiles"><Continue /><span class="ml-4px text-14px leading-20px text-[#232748]">{{ t('bulkExtract.startBulk') }}</span></el-dropdown-item>
            <el-dropdown-item @click="stopFiles"><Cancel /><span class="ml-4px text-14px leading-20px text-[#232748]">{{ t('bulkExtract.stopBulk') }}</span></el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="files-table w-full bg-white rounded-8px overflow-hidden flex-1 flex flex-col justify-between">
      <el-table
        :data="tablePageData"
        append-filter-panel-to=".files-table"
        @selection-change="handleSelectionChange"
        :tooltip-options="{ popperClass: 'box-item' }"
      >
        <el-table-column type="selection" :selectable="selectable" width="32" :class-name="noneSelected + 'checkbox-column'" />
        <el-table-column
          property="name"
          :label="t('knowledgeBases.dataset.name')"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="flex items-center"><Document class="mr-8px min-w-16px" /><span class="overflow-hidden overflow-ellipsis">{{ scope.row.name }}</span></div>
          </template>
        </el-table-column>
        <el-table-column
          :label="t('bulkExtract.status') + ' '"
          width="160"
          align="center"
          label-class-name="filter"
          :filters="[
            { text: t('bulkExtract.uploading'), value: 'uploading' },
            { text: t('bulkExtract.success'), value: 'success' },
            { text: t('bulkExtract.fail'), value: 'fail' },
            { text: t('bulkExtract.pending'), value: 'pending' }
          ]"
          :filter-method="filterStatus"
        >
          <template #filter-icon><Filter /></template>
          <template #default="scope">
            <div class="flex items-center justify-center w-120px h-28px border text-12px leading-16px rounded-6px" :class="{
              'border-[#244FF04D] bg-[#EBF1FE] text-[#396FFA]': scope.row.status === 'uploading' || scope.row.status === 'processing',
              'border-[#00CF854D] bg-[#E2F7EF] text-[#00CF85]': scope.row.status === 'success',
              'border-[#F871714D] bg-[#FBEDED] text-[#F87171]': scope.row.status === 'fail' || scope.row.status === 'uploadFail',
              'border-[#FFC0614D] bg-[#FFF4E5] text-[#F09004]': scope.row.status === 'stopped',
              'border-transparent text-[#52555F]': scope.row.status === 'pending',
            }">
              {{ scope.row.status ? t(`bulkExtract.${scope.row.status}`) : t('bulkExtract.noUpload') }}
              <span v-if="scope.row.status === 'processing'" class="ml-3px">{{ scope.row.currentPageCount }}/{{ scope.row.totalPageCount }}</span>
              <el-tooltip v-if="scope.row.status === 'fail' || scope.row.status === 'uploadFail'" popper-class="box-item" :content="scope.row.failureReason" placement="top">
                <FailTip class="ml-8px outline-none" />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('bulkExtract.action')" width="120" align="center">
          <template #default="scope">
            <div class="flex items-center justify-center gap-12px">
              <el-tooltip v-if="scope.row.status === 'processing'" popper-class="box-item" :content="t('bulkExtract.stop')" placement="top">
                <Cancel @click="stopFile(scope.row)" class="cursor-pointer outline-none" />
              </el-tooltip>
              <el-tooltip v-if="scope.row.status === 'stopped' || scope.row.status === 'fail'" popper-class="box-item" :content="t('bulkExtract.restart')" placement="top">
                <Continue @click="startFile(scope.row)" class="cursor-pointer outline-none" />
              </el-tooltip>
              <el-tooltip popper-class="box-item" :content="t('knowledgeBases.dataset.delete')" placement="top">
                <DeleteNoOutLine @click="deleteFile(scope.row.index)" class="cursor-pointer outline-none" />
              </el-tooltip>
              <el-tooltip v-if="scope.row.status === 'success'" popper-class="box-item" :content="t('bulkExtract.preview')" placement="top">
                <Preview @click="preview(scope.row)" class="cursor-pointer outline-none" />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="text-right overflow-x-scroll">
        <el-pagination
          background
          :total="fileList.length"
          :page-size="pageSize"
          :page-sizes="pageSizes"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          layout="total, prev, pager, next, sizes, jumper"
        />
      </div>
    </div>
  </div>

  <el-dialog v-model="dialogVisible" width="454px" align-center>
    <div class="mt-8px mb-24px flex items-center">
      <Warning />
      <div class="ml-24px text-16px leading-24px text-[#52555F]">{{ confirmDialogText }}</div>
    </div>
    <div class="flex justify-center">
      <div @click="dialogVisible = false" class="w-140px justify-center rounded-6px cursor-pointer bg-white text-[#396FFA] border border-[#396FFA] text-sm py-10px px-10px flex items-center font-500 hover:(bg-[#244FF0] text-white)">{{ t('knowledgeBases.dataset.no') }}</div>
      <div @click="comfirm" class="ml-12px w-140px justify-center rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-10px px-10px flex items-center font-500 hover:bg-[#244FF0]">{{ t('knowledgeBases.dataset.yes') }}</div>
    </div>
  </el-dialog>

  <div class="preview-overlay <lg:hidden" :class="previewDialogVisible ? 'show' : 'hide'">
    <div class="preview-dialog">
      <div class="header">
        <div class="flex items-center"><Document class="mr-8px min-w-16px" /><span class="overflow-hidden overflow-ellipsis">{{ viewingFile?.name }}</span></div>
        <Close class="cursor-pointer" @click="closePreview" />
      </div>
      <div class="body flex">
        <div class="w-[50%] relative">
          <div id="webviewer" ref="viewer" class="w-full h-full absolute top-0 left-0" :class="viewingFile?.fileType?.includes('pdf') ? 'show' : 'hide'"></div>
          <div class="w-full h-full absolute top-0 left-0 bg-[#F2F2F2] flex items-center justify-center" :class="['png', 'jpg', 'jpeg'].includes(viewingFile?.fileType) ? 'show' : 'hide'"><img :src="viewingFile?.fileDownUrl" alt=""></div>
        </div>
        <div class="w-[50%] relative">
          <JsonViewer boxed expanded :expandDepth="7" sort theme="dark" :value="jsonResult" />
          <div class="copy-tip w-24px h-24px border-1 border-[#E1E3E8] inline-block absolute p-3px rounded-4px cursor-pointer bg-white top-20px right-20px hover:border-[#396FFA]">
            <Copy @click="copy(jsonResult)" />
            <div class="tip absolute top-22px -left-6px hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] whitespace-nowrap">{{ t('knowledgeBases.chat.copy') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, inject } from 'vue'
import { get, post } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
// @ts-ignore
import ComPDFKitViewer from "../assets/@compdfkit/webviewer"
import { JsonViewer } from 'vue3-json-viewer'
import 'vue3-json-viewer/dist/vue3-json-viewer.css'
import clipboard from 'copy-to-clipboard'
import { getEnv } from '../utils/env'
import JSZip from 'jszip'
import { useRoute } from "vue-router"
import { getSystemBaseUnit } from '../utils/tools'

interface FileEntry {
  id: string
  index: number
  name: string
  status: string
  file: File
  totalPageCount: number
  currentPageCount: number
  failureReason: string
  fileDownUrl: string
  resultDownUrl: string
}

const props = defineProps({
  fileList: {
    type: Array,
    default: []
  },
  processStatus: {
    type: Number,
    default: 0
  },
  taskId: {
    type: String,
    default: ''
  }
})
const checkPassword: any = inject('checkPassword')
const emit = defineEmits(['changeExtractStatus'])

const { t } = useI18n()
const route = useRoute()
const pageSize = ref(10)
const pageSizes = ref([10, 15, 20])
const currentPage = ref(1)
const dialogVisible = ref(false)
const confirmDialogText = ref('')
const indexSelection = ref<number[]>([])
const idSelection = ref<string[]>([])
const currentAction = ref()
const input = ref()
let intervalId: number = 0
const statusMap = {
  0: 'pending',
  1: 'processing',
  2: 'success',
  3: 'fail',
  4: 'delete',
  5: 'stopped'
}
const previewDialogVisible = ref(false)
const viewer = ref()
const docViewer = ref()
const UI = ref()
const viewingFile = ref()
const jsonResult = ref({})
const fileNamesSet = new Set()

// 页数据
const tablePageData = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return props.fileList.slice(startIndex, endIndex)
})

// 是否有可选中的行
const noneSelected = computed(() => {
  for (let i = 0; i < props.fileList.length; i++) {
    const item: FileEntry = (props.fileList as FileEntry[])[i]
    if (!['uploading', 'success'].includes(item.status)) return ''
  }
  return 'disable-header-checkbox '
})

watch(() => props.processStatus, async (val) => {
  if (val === 1) {
    intervalId = window.setInterval(async () => {
      const taskRes = await get(`/api/idp/get-task-file-list?taskId=${props.taskId}`)
      const taskMap = new Map(taskRes.data.data.map((item: any) => [item.fileId, item]));

      (props.fileList as FileEntry[]).forEach(async (item: FileEntry) => {
        const target: any = taskMap.get(item.id)
        if (target) {
          item.status = statusMap[target.status as keyof typeof statusMap]
          item.totalPageCount = target.fileSchedule.totalPageCount
          item.currentPageCount = target.fileSchedule.currentPageCount

          if (item.status === 'fail') {
            if (target.failureCode === '08005' || route.path.includes('extraction')) {
              item.failureReason =  t('bulkExtract.extractFail')
            } else if (route.path.includes('parsing')) {
              if (target.failureCode === '08009') {
                item.failureReason =  t('bulkParse.parseFail')
              } else if (target.failureCode === '08010') {
                item.failureReason =  t('singleParse.emptyContent')
              } else if (target.failureCode === '02212') {
                item.failureReason =  t('singleParse.noTable')
                ElMessage.error(t('singleParse.noTable'))
              }
            } else {
              item.failureReason = target.failureReason
            }
          }
          if (item.status === 'success') {
            item.fileDownUrl = target.fileDownUrl
            item.resultDownUrl = target.resultDownUrl
          }
        }
      })

      // (props.fileList as FileEntry[]).forEach(async (item: FileEntry) => {
      //   const fileRes = await get(`/api/idp/get-file-schedule?fileId=${item.id}`)
      //   item.status = 'processing'
      //   item.totalPageCount = fileRes.data.data.totalPageCount
      //   item.currentPageCount = fileRes.data.data.currentPageCount

      //   const fileStatusRes = await get(`/api/idp/get-file-info?fileId=${item.id}`)
      // })
    }, 2000)
  } else {
    clearInterval(intervalId)
    intervalId = 0
  }
})

onMounted(() => {
  (props.fileList as FileEntry[]).forEach(item => {
    fileNamesSet.add(item.name)
  })

  const license = getEnv('LICENSE_KEY')
  ComPDFKitViewer.init({
    license,
    pdfUrl: '',
    path: '/',
    showToolbarControl: false,
    isRenderAnnotations: false,
    enableDefaultFont: true
  }, viewer.value).then((instance: any) => {
    docViewer.value = instance.docViewer
    UI.value = instance.UI
    instance.UI.textPopup.update([])
  })
})

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
}
// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
}
// 筛选状态
const filterStatus = (value: string, row: FileEntry) => {
  return row.status === value
}
// 不可被选中项
const selectable = (row: FileEntry) => {
  return !['uploading', 'success', 'uploadFail'].includes(row.status)
}
// 打开二次确认弹窗
const openConfirmDialog = (text: string = '', actionCallback: Function) => {
  confirmDialogText.value = text
  currentAction.value = actionCallback
  dialogVisible.value = true
}
// 弹窗点击确定
const comfirm = () => {
  if (currentAction.value) {
    currentAction.value()
    currentAction.value = null
  }
  dialogVisible.value = false
}
// 上传文件
const handleChange = async (e: any) => {
  const files = e.target.files
  if (!files) return
  if (files.length + props.fileList.length > 32) {
    ElMessage.error(t('bulkExtract.fileLenTip'))
    return
  }
  const fileArr = Array.from(files as FileList)
  const totalSize = fileArr.reduce((sum, file) => sum + file.size, 0)
  const base = getSystemBaseUnit()
  if (totalSize > 100 * base * base) {
    ElMessage.error(t('bulkExtract.fileSizeTip'))
    return
  }

  const uniqueFiles = fileArr.filter(file => !fileNamesSet.has(file.name))
  const results = await Promise.all(uniqueFiles.map(async file => {
    if (file.name.endsWith('.pdf')) {
      const isProtected = await checkPassword(file)
      return ({ file, isProtected })
    } else {
      return Promise.resolve({ file, isProtected: false })
    }
  }))
  const filteredResults = results.filter(result => result !== null)
  const unprotectedFiles = filteredResults.filter(result => !result.isProtected)
  if (filteredResults.length !== unprotectedFiles.length) {
    ElMessage.warning(t('bulkExtract.encryptTip'))
  }

  for (let i = 0; i < unprotectedFiles.length; i++) {
    const { file } = unprotectedFiles[i]
    fileNamesSet.add(file.name)
    props.fileList.push({
      id: '',
      index: i,
      name: file.name,
      status: '',
      file
    })
  }
  
  (props.fileList as FileEntry[]).forEach((item: FileEntry) => {
    if (item.status) return
    item.status = 'uploading'
    const formdata = new FormData()
    formdata.append('taskId', props.taskId)
    formdata.append('file', item.file, item.name)
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
// 更新选中
const handleSelectionChange = (val: FileEntry[]) => {
  indexSelection.value = []
  idSelection.value = []
  val.forEach(item => {
    indexSelection.value.push(item.index)
    idSelection.value.push(item.id)
  })
}
// 删除多个文件
const deleteFiles = () => {
  openConfirmDialog(t('bulkExtract.deleteThem'), () => {
    const selectionSet = new Set(indexSelection.value)
    for (let i = props.fileList.length - 1; i >= 0; i--) {
      const item = (props.fileList as FileEntry[])[i]
      if (selectionSet.has(item.index)) {
        fileNamesSet.delete(item.name)
        props.fileList.splice(i, 1)
      }
    }
    const query = idSelection.value.map(id => `fileIds=${id}`).join('&')
    get(`/api/idp/file-delete?${query}`).then(() => {
      ElMessage.success(t('knowledgeBases.dataset.deleteSuccess'))
      indexSelection.value = []
      idSelection.value = []
    }).catch((err: any) => {
      console.log(err)
      ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
    })
  })
}
// 删除单个文件
const deleteFile = (index: number) => {
  openConfirmDialog(t('bulkExtract.deleteIt'), () => {
    for (let i = 0; i < props.fileList.length; i++) {
      const item = (props.fileList as FileEntry[])[i]
      if (item.index === index) {
        fileNamesSet.delete(item.name)
        props.fileList.splice(i, 1)
        get(`/api/idp/file-delete?fileIds=${item.id}`).then(() => {
          ElMessage.success(t('knowledgeBases.dataset.deleteSuccess'))
        }).catch((err: any) => {
          console.log(err)
          ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
        })
        break
      }
    }
  })
}
// 停止多个文件
const stopFiles = () => {
  openConfirmDialog(t('bulkExtract.stopThem'), () => {
    const query = idSelection.value.map(id => `fileIds=${id}`).join('&')
    get(`/api/idp/file-pause?${query}`).then(() => {
      ElMessage.success(t('bulkExtract.stopped') + '!')
    }).catch((err: any) => {
      console.log(err)
    })
  })
}
// 停止单个文件
const stopFile = (row: FileEntry) => {
  openConfirmDialog(t('bulkExtract.stopIt'), () => {
    get(`/api/idp/file-pause?fileIds=${row.id}`).then(() => {
      row.status = 'stopped'
      ElMessage.success(t('bulkExtract.stopped') + '!')
    }).catch((err: any) => {
      console.log(err)
    })
  })
}
// 二次开始处理多个文件
const startFiles = () => {
  const formdata = new FormData()
  formdata.append('type', route.path.includes('extraction') ? 'EXTRACTION' : 'LAYOUT')
  idSelection.value.forEach(id => {
    formdata.append('idpFileIds', id)
  })

  post('/api/idp/files-start', formdata).then(() => {
    const selectionSet = new Set(indexSelection.value)
    for (let i = props.fileList.length - 1; i >= 0; i--) {
      const item = (props.fileList as FileEntry[])[i]
      if (selectionSet.has(item.index)) {
        item.status = 'pending'
      }
    }

    if (props.processStatus === 2) {
      emit('changeExtractStatus', 1)
    }
  }).catch((err: any) => {
    console.log(err)
  })
}
// 二次开始处理单个文件
const startFile = (row: FileEntry) => {
  const formdata = new FormData()
  formdata.append('idpFileIds', row.id)
  formdata.append('type', route.path.includes('extraction') ? 'EXTRACTION' : 'LAYOUT')

  post('/api/idp/files-start', formdata).then(() => {
    row.status = 'pending'

    if (props.processStatus === 2) {
      emit('changeExtractStatus', 1)
    }
  }).catch((err: any) => {
    console.log(err)
  })
}
// 预览文件抽取结果
const preview = async (row: FileEntry) => {
  viewingFile.value = row
  previewDialogVisible.value = true

  const nameArray = row.name.split('.')
  const fileType = nameArray[nameArray.length - 1].toLowerCase()
  viewingFile.value.fileType = fileType

  if (fileType.includes('pdf')) {
    UI.value.loadDocument(row.fileDownUrl)
  }

  const response = await fetch(row.resultDownUrl)
  const resNameArray = row.resultDownUrl.split('.')
  const resType = resNameArray[resNameArray.length - 1].toLowerCase()
  if (!response.ok || !response.body) {
    throw new Error('Network error or empty response body')
  }

  if (resType.includes('json')) {
    const data = await response.json()
    jsonResult.value = data
  }

  if (resType.includes('zip')) {
    try {
      jsonResult.value = await extractJsonFromZip(response)
    } catch (error) {
      console.error(error)
    }
  }
}
// 关闭预览弹窗
const closePreview = () => {
  viewingFile.value = false
  previewDialogVisible.value = false
}
// 复制
const copy = (content: any) => {
  clipboard(JSON.stringify(content, null, 2), {
    format: 'text/plain'
  })
  ElMessage.success(t('knowledgeBases.chat.copied'))
}
// 解压缩包获取json
const extractJsonFromZip = async (response: Response): Promise<any> => {
  try {
    const zipBuffer = await response.arrayBuffer();
    
    const zip = new JSZip();
    await zip.loadAsync(zipBuffer);
    
    const jsonFiles = Object.keys(zip.files)
      .filter(fileName => fileName.toLowerCase().endsWith('.json'))
      .sort();
    
    if (jsonFiles.length === 0) {
      throw new Error('ZIP 文件中未找到 JSON 文件');
    }
    
    const targetFile = zip.file(jsonFiles[0]);
    if (!targetFile) {
      throw new Error('无法访问 JSON 文件');
    }
    
    const jsonText = await targetFile.async('text');
    return JSON.parse(jsonText);
    
  } catch (error) {
    console.error('处理 ZIP 文件时出错:', error);
    throw error;
  }
}
</script>

<style lang="scss" scoped>
:deep(.el-table) {
  border: 1px solid white;
  border-radius: 8px;
  .el-table__inner-wrapper .el-table__header-wrapper .el-table__header .el-table__cell {
    border-bottom: none;
    &:first-child:before {
      display: none;
    }
  }
  th.el-table__cell.filter {
    .el-icon {
      width: 20px;
      height: 20px;
      font-size: 16px;
    }
    .cell.highlight {
      color: unset;
      .el-icon {
        border: 1px solid #396FFA;
        border-radius: 4px;
        background-color: #EBF1FE;
        color: #396FFA
      }
    }
  }
  .checkbox-column .cell {
    text-overflow: clip;
  }
  .el-table__header .disable-header-checkbox .cell {
      cursor: not-allowed;
    .el-checkbox {
      pointer-events: none;
    }
    .el-checkbox__input .el-checkbox__inner {
      background-color: #F5F7FA;
      border-color: #DCDFE6;
    }
  }
}

.preview-overlay {
  bottom: 0;
  height: 100%;
  left: 0;
  overflow: auto;
  position: fixed;
  right: 0;
  top: 0;
  z-index: 5;
  background-color: rgba(0, 0, 0, 0.5);
  transition: opacity 0.5s ease, visibility 0.5s ease;
  &.show {
    opacity: 1;
    visibility: visible;
    .preview-dialog {
      opacity: 1;
      transform: translateY(0);
    }
  }
  &.hide {
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    .preview-dialog {
      opacity: 0;
      transform: translateY(-15px);
    }
  }
  .preview-dialog {
    background: white;
    margin: 40px 32px 40px 108px;
    padding: 0;
    width: calc(100vw - 146px);
    height: calc(100vh - 80px);
    border-radius: 20px;
    overflow: hidden;
    transition: all 0.5s ease;
    .header {
      padding: 0 32px;
      height: 80px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .body {
      height: calc(100% - 80px);
      .show {
        opacity: 1;
        visibility: visible;
      }
      .hide {
        opacity: 0;
        visibility: hidden;
        pointer-events: none;
      }
    }
  }
}

:deep(.jv-container) {
  width: 100%;
  height: 100%;
  overflow: auto;
  font-size: 16px;
  color: #01fef4;
  line-height: 20px;
  padding: 0 10px;
  white-space: nowrap;
  background: #030D26;
  font-family: 'Encode Sans';

  &:hover {
    box-shadow: none;
  }
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.32);
  }

  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }

  .jv-more {
    display: none;
  }

  &.jv-dark {
    &.boxed {
      border: none;
    }
    .jv-ellipsis {
      display: inline-block;
      line-height: 0.9;
      font-size: 0.9em;
      padding: 0px 4px 2px 4px;
      border-radius: 3px;
      vertical-align: 2px;
      cursor: pointer;
      user-select: none;
    }
  
    .jv-button {
      color: #49b3ff;
    }
  
    .jv-ke {
      margin-right: 6px;
      color: #FFD686;
    }
  
    .jv-push {
      color: #fff;
    }
  
    .jv-array {
      color: #6BBF69;
    }
  
    .jv-boolean {
      color: #6BBF69;
    }
  
    .jv-function {
      color: #067bca;
    }
  
    .jv-item {
      &.jv-number {
        color: #6BBF69;
      }
  
      &.jv-array {
        color: #6BBF69;
      }
    }
  
    .jv-key {
      color: #FFD686;
    }
  
    .jv-number-float {
      color: #fc1e70;
    }
  
    .jv-number-integer {
      color: #fc1e70;
    }
  
    .jv-object {
      color: white;
    }
  
    .jv-undefine {
      color: #e08331;
    }
  
    .jv-string {
      color: #FFA15E;
      word-break: break-word;
      white-space: normal;
    }
  
    .jv-lin {
      color: #52ACF3;
      text-decoration: underline;
    }
  
    .jv-code {
      padding: 30px 0;
  
      .jv-toggle {
        color: #067bca;
  
        :before {
          padding: 0px 2px;
          border-radius: 2px;
        }
  
        :hover {
          :before {
            background: rgb(242, 5, 5);
          }
        }
      }
    }
  }
}

.copy-tip {
  &:hover {
    .tip {
      display: block;
    }
  }
}

:deep(.el-pagination) {
  display: inline-flex;
  overflow-x: scroll;
  justify-content: flex-start;
}
</style>

<style lang="scss">
.el-popper.bulk-action {
  box-shadow: 0px 4px 35px 0px #0029921A;
  margin-top: -11px;
  .el-dropdown-menu {
    padding: 4px 8px;
    .el-dropdown-menu__item {
      line-height: 20px;
      padding: 8px 15px 8px 12px;
    }
  }
  .el-popper__arrow {
    display: none;
  }
}
</style>