<template>
  <div class="bg-[#F3F6FF] min-h-100vh w-full">
    <KbHeader />
    <div class="flex min-h-[calc(100vh-88px)] pt-88px">
      <KbSidebar :kbDetail="kbDetail" />
      <div class="py-40px pl-32px w-[calc(100%-240px)] pr-80px">
        <div class="flex text-xs mb-36px">
          <a href="/knowledge-base" class="text-brand-1">{{ t('knowledgeBases.title') }}</a>
          <ArrowRight class="mx-12px" />
          <div class="text-brand-2">{{ t('knowledgeBases.dataset.title') }}</div>
        </div>
        <h1 class="text-32px leading-48px font-600 text-brand-0 mb-2px">{{ t('knowledgeBases.dataset.title') }}</h1>
        <div class="text-brand-0 text-sm mt-4px mb-32px">{{ t('knowledgeBases.dataset.desc') }}</div>
        <div class="flex justify-between mb-24px">
          <div @click.stop="checkBulk" :class="(selectFiles.length && role !== 'viewer') ? 'cursor-pointer' : 'cursor-not-allowed text-brand-1 bg-[#CED6E133]'" class="flex items-center justify-between text-xs min-w-160px relative border border-[#CED6E1] py-10px px-12px rounded-4px">
            {{ t('knowledgeBases.dataset.bulk') }}
            <Arrow class="transitions ml-12px text-brand-4" />
            <div v-show="bulk" class="absolute shadows bg-white py-4px px-8px whitespace-nowrap top-52px left-0 rounded-4px z-5 text-xs">
              <div @click="bulkChangeStatus(1)" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                <Enable class="mr-4px" />
                {{ t('knowledgeBases.dataset.enableFile') }}
              </div>
              <div @click="bulkChangeStatus(0)" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                <Disable class="mr-4px" />
                {{ t('knowledgeBases.dataset.disableFile') }}
              </div>
              <div class="h-1px w-full bg-[#E1E3E8]"></div>
              <div @click="startOrCancelParse('start', selectFiles)" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                <Start class="mr-4px" />
                {{ t('knowledgeBases.dataset.start') }}
              </div>
              <div @click="startOrCancelParse('cancel', selectFiles)" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                <Cancel class="mr-4px" />
                {{ t('knowledgeBases.dataset.cancelParsing') }}
              </div>
              <div class="h-1px w-full bg-[#E1E3E8]"></div>
              <div @click="deleteFile(selectFiles)" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                <Delete class="mr-4px svg delete" />
                {{ t('knowledgeBases.dataset.delete') }}
              </div>
            </div>
          </div>
          <div class="flex items-center">
            <el-input v-model="searchQuery" clearable @clear="getTableData" @keyup.enter="getTableData" :placeholder="t('knowledgeBases.dataset.search')">
              <template #prefix>
                <Search />
              </template>
            </el-input>
            <div :class="role === 'viewer' && 'cursor-not-allowed opacity-60'" @click="openUpload" class="ml-12px whitespace-nowrap rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center justify-center font-500 hover:bg-[#244FF0]">
              {{ t('knowledgeBases.dataset.upload') }}
            </div>
            <div class="flex justify-end">
              <div :class="role === 'viewer' && 'cursor-not-allowed opacity-60'" @click="openUploadUrl" class="ml-12px whitespace-nowrap rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center font-500 hover:bg-[#244FF0] w-fit">
                {{ t('knowledgeBases.dataset.uploadUrl') }}
              </div>
            </div>
          </div>
        </div>
        <div class="bg-white shadows">
          <el-table ref="tableRef" :data="paginatedData" @selection-change="handleSelectionChange" :row-key="rowKey">
            <el-table-column type="selection" width="50" align="center" :selectable="selectable" />
            <el-table-column :label="t('knowledgeBases.dataset.name')" align="left" min-width="180px" show-overflow-tooltip>
              <template #default="scope">
                <a v-if="scope.row.progress === 1" :href="`/knowledge-base/dataset/chunk?id=${route.query.id}&doc_id=${scope.row.id}`" class="flex items-center text-brand-2">
                  <Document class="min-w-20px mr-4px" />
                  <div class="truncate">{{ scope.row.name.split('.').slice(0, -1).join('.') }}</div>
                </a>
                <div v-else class="flex items-center text-brand-3">
                  <Document class="min-w-20px mr-4px" />
                  <div class="truncate">{{ scope.row.name.split('.').slice(0, -1).join('.') }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="type" :label="t('knowledgeBases.dataset.format')" align="center" width="100px">
              <template #default="scope">
                {{ scope.row.name?.split('.').pop().toUpperCase() }}
              </template>
            </el-table-column>
            <el-table-column prop="update_time" :label="t('knowledgeBases.dataset.updated')" align="center" width="160px">
              <template #default="scope">
                <div class="flex justify-start whitespace-nowrap">{{ dayjs.utc(scope.row.update_date).format('DD/MM/YYYY HH:mm:ss') }}</div>
              </template>
            </el-table-column>
            <el-table-column :label="t('knowledgeBases.dataset.method')" align="center" width="160px">
              <template #default="scope">
                <div class="flex justify-center">
                  <el-select :disabled="role === 'viewer'" @change="changeMethod(scope.row.parser_id, scope.row.id)" v-model="scope.row.parser_id" class="table-select">
                    <el-option v-for="(parse, index) in getParsersByTypeDate(scope.row.name)" :key="index" :label="t(`knowledgeBases.configuration.${parse}`)" :value="parse"></el-option>
                  </el-select>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="chunk_num" :label="t('knowledgeBases.dataset.chunks')" align="center" width="100px" />
            <el-table-column :label="t('knowledgeBases.dataset.enable')" align="center" width="80px">
              <template #default="scope">
                <div class="flex justify-center">
                  <el-switch :disabled="scope.row.progress !== 1 || role === 'viewer'" v-model="scope.row.status" @change="changeStatus(Number(scope.row.status), scope.row.id)"
                    style="--el-switch-on-color: #396FFA; --el-switch-off-color: #CED6E1"
                    active-value="1" inactive-value="0">
                  </el-switch>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('knowledgeBases.dataset.status')" align="center" width="160px">
              <template #default="scope">
                <div class="flex justify-center items-center text-12px leading-16px">
                  <el-tooltip popper-class="box-item" effect="dark" :content="t('knowledgeBases.dataset.delete')" placement="top">
                    <template #content>
                      <div class="shadows text-xs text-black rounded-4px py-4px px-10px max-w-455px">
                        <div class="flex">
                          <div class="font-600 mr-8px whitespace-nowrap">{{ t('knowledgeBases.dataset.begin') }}:</div>
                          {{ scope.row.process_begin_at }}
                        </div>
                        <div class="flex">
                          <div class="font-600 mr-8px whitespace-nowrap">{{ t('knowledgeBases.dataset.duration') }}:</div>
                          {{ scope.row.process_duration }}
                        </div>
                        <div class="flex flex-col">
                          <div class="font-600 whitespace-nowrap">{{ t('knowledgeBases.dataset.progress') }}:<br></div>
                          <div class="whitespace-pre">{{ scope.row.progress_msg }}</div>
                        </div>
                      </div>
                    </template>
                    <div :class="getParseStatusType(scope.row.progress)" class="border-1 rounded-6px border-solid py-6px px-8px text-12px leading-16px min-w-115px py-6px mr-4px">
                      {{ formatParseStatus(scope.row.progress) }}
                    </div>
                  </el-tooltip>
                  <el-tooltip popper-class="box-item" v-if="scope.row.progress === 0" effect="dark" :content="t('knowledgeBases.dataset.start')" placement="top">  
                    <Start :class="role === 'viewer' && 'cursor-not-allowed'" @click="startOrCancelParse('start', [scope.row.id])" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip>
                  <el-tooltip popper-class="box-item" v-else-if="scope.row.progress === 1" effect="dark" :content="t('knowledgeBases.dataset.reParsing')" placement="top">  
                    <Refresh :class="role === 'viewer' && 'cursor-not-allowed'" @click="startOrCancelParse('start', [scope.row.id])" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip>
                  <el-tooltip popper-class="box-item" v-else-if="scope.row.progress === -1"  effect="dark" :content="t('knowledgeBases.dataset.reParsing')" placement="top">  
                    <Refresh :class="role === 'viewer' && 'cursor-not-allowed'" @click="startOrCancelParse('start', [scope.row.id])" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip>
                  <el-tooltip popper-class="box-item" v-else effect="dark" :content="t('knowledgeBases.dataset.cancelParsing')" placement="top">  
                    <Cancel :class="role === 'viewer' && 'cursor-not-allowed'" @click="startOrCancelParse('cancel', [scope.row.id])" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
            <el-table-column fixed="right" :label="t('knowledgeBases.dataset.action')" align="center" width="150px">
              <template #default="scope">
                <div class="flex items-center justify-center">
                  <el-tooltip popper-class="box-item" effect="dark" :content="t('knowledgeBases.dataset.rename')" placement="top">
                    <Rename :class="role === 'viewer' && 'cursor-not-allowed'" @click="openRename(scope.row.id, scope.row.name)" class="cursor-pointer svg" />
                  </el-tooltip>
                  <el-tooltip popper-class="box-item" effect="dark" :content="t('knowledgeBases.dataset.delete')" placement="top">
                    <Delete :class="role === 'viewer' && 'cursor-not-allowed'" @click="deleteFile([scope.row.id])" class="cursor-pointer svg mx-12px" />
                  </el-tooltip>
                  <el-tooltip popper-class="box-item" effect="dark" :content="t('knowledgeBases.dataset.download')" placement="top">
                    <Download :class="role === 'viewer' && 'cursor-not-allowed'" @click="download(scope.row)" class="cursor-pointer svg" />
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            background
            :total="total"
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
    <!-- 上传文件 -->
    <el-dialog v-model="uploadDialogVisible" align-center width="480px">
      <h3 class="text-sm font-bold text-[#43474D] py-4px mb-24px">
        {{ t('knowledgeBases.dataset.upload') }}
      </h3>
      <div @drop.prevent="onDrop" @dragover.prevent="dragover = true" @dragleave.prevent="leave" class="border border-[1.5px] border-dashed border-brand-2 cursor-pointer relative rounded-10px min-h-330px" :class="dragover && 'bg-[#F3F6FF]'">
        <div @click="input?.click" class="w-300px rounded-6px mx-auto mt-106px cursor-pointer bg-[#396FFA] text-white text-sm font-600 py-10px px-12px flex items-center justify-center hover:bg-[#244FF0]">
          <Upload class="mr-12px" />
          {{ t('knowledgeBases.dataset.selectFile[0]') }}
        </div>
        <div class="text-center my-8px text-xs text-brand-3">
          {{ t('knowledgeBases.dataset.selectFile[1]') }}
        </div>
        <div class="text-center text-xs text-brand-3">
          {{ t('knowledgeBases.dataset.selectFile[2]') }}
        </div>
        <div class="bg-[#F3F6FF] absolute bottom-0 py-8px text-center rounded-b-left-10px rounded-b-right-10px w-full">
          <p class="max-w-380px mx-auto">
            {{ t('knowledgeBases.dataset.support') }}
          </p>
        </div>
      </div>
      <input ref="input" class="hidden" type="file" accept=".doc, .docx, .xlsx, .xls, .ppt, .pdf, .txt, .jpeg, .jpg, .png, .csv, .json" name="file" multiple @change="handleChange">
      <div class="flex items-center text-xs text-brand-1 mt-8px">
        <div v-if="autoParse" class="privacy-box-active mr-8px">
          <img src="../assets/images/login/check_box.svg" class="cursor-pointer" @click="autoParse = false" />
        </div>
        <span v-else class="privacy-box cursor-pointer mr-8px" @click="autoParse = true"></span>
        {{ t('knowledgeBases.dataset.auto') }}
      </div>
      <div v-show="fileList.length" class="flex flex-col mt-24px adaptive overflow-auto text-sm text-brand-0 max-h-154px" :class="`h-[${fileList.length * 24}px]`">
        <div v-for="(file, index) in fileList" :key="index" :class="index && 'mt-16px'" class="flex justify-between">
          <div class="flex items-center">
            <Success class="mr-8px min-w-24px" />
            <div class="truncate w-[calc(452px-64px)]">{{ file.name }}</div>
          </div>
          <DeleteFile @click="deleteUploadFile(index)" class="cursor-pointer min-w-16px" />
        </div>
      </div>
      <div class="flex justify-center mt-24px">
        <div class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px mr-12px flex items-center justify-center hover:(bg-[#396FFA] text-white)" @click="uploadDialogVisible = false">
          {{ t('knowledgeBases.dataset.cancel') }}
        </div>
        <div v-loading="loading" @click="upload" :class="fileList?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
          class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center">
          {{ t('knowledgeBases.dataset.ok') }}
        </div>
      </div>
    </el-dialog>
    <!-- 文件重命名 -->
    <el-dialog v-model="dialogVisible" align-center width="400px">
      <h3 class="text-sm font-bold text-[#43474D] py-4px mb-12px">
        {{ t('knowledgeBases.dataset.rename') }}
      </h3>
      <div class="flex text-xs text-brand-0 font-600 mb-8px">
        <span class="text-[#FF5050] inline-block mr-4px font">*</span>
        {{ t('knowledgeBases.dataset.name') }}
      </div>
      <el-input v-model="filename" @input="filenameError = ''" :placeholder="t('knowledgeBases.dataset.namePlaceholder')" />
      <div v-show="filenameError" class="text-[#f56c6c] text-12px leading-12px mt-2px">
        {{ filenameError }}
      </div>
      <div class="flex justify-center mt-24px">
        <div class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)" @click="dialogVisible = false">
          {{ t('knowledgeBases.dataset.cancel') }}
        </div>
        <div v-loading="loading" @click="rename" class="w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]">
          {{ t('knowledgeBases.dataset.ok') }}
        </div>
      </div>
    </el-dialog>
    <el-dialog v-model="uploadUrlDialogVisible" align-center width="720px">
      <div v-loading="uploadUrlLoading" :element-loading-text="`${uploaded}/${urlList.length}`">
        <h3 class="text-sm font-bold text-[#43474D] py-4px mb-28px">
          {{ t('knowledgeBases.dataset.uploadUrl') }}
        </h3>
        <div class="flex items-center mb-12px">
          <el-input v-model="searchUrlQuery" max-length="50" @keyup.enter="searchUrl" :placeholder="t('knowledgeBases.dataset.search')" class="mr-12px">
            <template #prefix>
              <Search />
            </template>
          </el-input>
          <el-input v-model="urlDeep" type="number" max="5" min="0" :placeholder="t('knowledgeBases.dataset.urlDeep')"></el-input>
          <div v-loading="searchLoading" class="flex justify-end">
            <div :class="role === 'viewer' && 'cursor-not-allowed opacity-60'" @click="searchUrl" class="ml-12px whitespace-nowrap rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center font-500 hover:bg-[#244FF0] w-fit">
              {{ t('knowledgeBases.dataset.search') }}
            </div>
          </div>
        </div>
        <el-table max-height="622" :data="paginatedUrlData" @selection-change="handleSelectionChange">
          <el-table-column prop="url" label="URL" align="left" min-width="180px" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="text-sm text-brand-3">{{ row }}</div>
            </template>
          </el-table-column>
          <el-table-column align="center" :label="t('knowledgeBases.dataset.action')" width="150px">
            <template #default="{ row }">
              <div class="flex justify-center">
                <el-tooltip popper-class="box-item" effect="dark" :content="t('knowledgeBases.dataset.delete')" placement="top">
                  <Delete :class="role === 'viewer' && 'cursor-not-allowed'" @click="deleteUrl(row)" class="cursor-pointer svg mx-12px" />
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          :total="urlList.length"
          :page-size="urlPageSize"
          :page-sizes="urlPageSizes"
          :current-page="urlCurrentPage"
          @size-change="handleUrlSizeChange"
          @current-change="handleUrlCurrentChange"
          layout="total, prev, pager, next, sizes, jumper"
        />
        <div class="flex items-center text-xs text-brand-1 pt-8px">
          <div v-if="autoParse" class="privacy-box-active mr-8px">
            <img src="../assets/images/login/check_box.svg" class="cursor-pointer" @click="autoParse = false" />
          </div>
          <span v-else class="privacy-box cursor-pointer mr-8px" @click="autoParse = true"></span>
          {{ t('knowledgeBases.dataset.auto') }}
        </div>
        <div class="flex justify-end">
          <div v-loading="searchLoading" :class="(role === 'viewer' || urlList.length === 0) && 'cursor-not-allowed opacity-60'" @click="uploadUrl" class="ml-12px whitespace-nowrap rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center font-500 hover:bg-[#244FF0] w-fit">
            {{ t('knowledgeBases.dataset.uploadUrl') }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { DocumentParserType } from '../constants/types'
import type { ParserMap, FileExtension } from '../constants/types'
import { onMounted, ref, onBeforeUnmount, nextTick, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { get, post } from '../utils/request'
import ArrowRight from '../components/images/ArrowRight.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Delete from '../components/images/Delete.vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import Document from '../components/images/Document.vue'
import Start from '../components/images/Start.vue'
import Refresh from '../components/images/Refresh.vue'
import Download from '../components/images/Download.vue'
import Rename from '../components/images/Rename.vue'
import Upload from '../components/images/Upload.vue'
import Cancel from '../components/images/Cancel.vue'
import Success from '../components/images/Success.vue'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import { getSystemBaseUnit } from '../utils/tools'

const initParserMap = (): ParserMap => {
  return new Map([
    [
      ['pdf'],
      [
        DocumentParserType.Naive,
        DocumentParserType.Manual,
        DocumentParserType.Paper,
        DocumentParserType.Book,
        DocumentParserType.Laws,
        DocumentParserType.Presentation,
        DocumentParserType.One,
        DocumentParserType.Qa
      ]
    ],
    [
      ['doc', 'docx'],
      [
        DocumentParserType.Naive,
        DocumentParserType.Book,
        DocumentParserType.Laws,
        DocumentParserType.One,
        DocumentParserType.Qa,
        DocumentParserType.Manual
      ]
    ],
    [
      ['xlsx', 'xls'],
      [
        DocumentParserType.Naive,
        DocumentParserType.Qa,
        DocumentParserType.Table,
        DocumentParserType.One
      ]
    ],
    [['ppt', 'pptx'], [DocumentParserType.Presentation]],
    [
      ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'tif', 'webp', 'svg', 'ico'],
      [DocumentParserType.Picture]
    ],
    [
      ['txt'],
      [
        DocumentParserType.Naive,
        DocumentParserType.Book,
        DocumentParserType.Laws,
        DocumentParserType.One,
        DocumentParserType.Qa,
        DocumentParserType.Table
      ]
    ],
    [
      ['csv'],
      [
        DocumentParserType.Naive,
        DocumentParserType.Book,
        DocumentParserType.Laws,
        DocumentParserType.One,
        DocumentParserType.Qa,
        DocumentParserType.Table
      ]
    ],
    [
      ['md'],
      [
        DocumentParserType.Naive,
        DocumentParserType.Qa
      ]
    ],
    [['json'], [DocumentParserType.Naive]],
    [['eml'], []]
  ])
}
const { t } = useI18n()
const role = ref()
const total = ref(0)
const uploaded = ref(0)
const searchLoading = ref(false)
const uploadUrlLoading = ref(false)
const urlPageSize = ref(10)
const urlPageSizes = ref([5, 10, 20])
const urlCurrentPage = ref(1)
const doc_id = ref()
const tableRef = ref()
const bulk = ref(false)
const loading = ref(false)
const dragover = ref(false)
const filename = ref('')
const filenameError = ref('')
const fileType = ref('')
const route = useRoute()
const pageSize = ref(10)
const currentPage = ref(1)
const searchQuery = ref('')
const urlDeep = ref(1)
const searchUrlQuery = ref('')
const urlList = ref<string[]>([])
const autoParse = ref(true)
const fileList = ref<File[]>([])
const dialogVisible = ref(false)
const pageSizes = ref([5, 10, 20])
const paginatedData = ref<any[]>([])
const selectFiles = ref<string[]>([])
const uploadDialogVisible = ref(false)
const uploadUrlDialogVisible = ref(false)
const input = ref<HTMLInputElement | null>(null)
let intervalId: number | NodeJS.Timeout | undefined
watch(() => dialogVisible.value, (val: boolean) => {
  if (!val) {
    filenameError.value = ''
  }
})
watch(() => uploadDialogVisible.value, (val: boolean) => {
  if (!val) {
    fileList.value = []
  }
})
const getParsersByTypeDate = (val: string) => {
  return getParsersByType(val?.split('.')?.pop()?.toUpperCase() || '')
}
const paginatedUrlData = computed(() => {
  const start = (urlCurrentPage.value - 1) * urlPageSize.value
  const end = start + urlPageSize.value
  return urlList.value.slice(start, end)
})
const handleUrlSizeChange = (value: number) => {
  urlPageSize.value = value
  urlCurrentPage.value = 1
}
const handleUrlCurrentChange = (value: number) => {
  urlCurrentPage.value = value
}
const leave = () => {
  dragover.value = false
}
const useDocumentParsers = () => {
  const parserMap = ref<ParserMap>(initParserMap())
  
  // 创建倒排索引 { 扩展名 => 解析器数组 }
  const parserIndex = computed(() => {
    const index = new Map<FileExtension, DocumentParserType[]>()
    parserMap.value.forEach((parsers, exts) => {
      exts.forEach(ext => index.set(ext.toLowerCase(), parsers))
    })
    return index
  })

  // 根据文件类型获取解析器数组
  const getParsersByType = (fileType: string): DocumentParserType[] => {
    const normalizedType = fileType.toLowerCase()
    return parserIndex.value.get(normalizedType) || []
  }

  return {
    getParsersByType
  }
}
const openUpload = () => {
  if (role.value === 'viewer') return
  uploadDialogVisible.value = true
}
const openUploadUrl = () => {
  if (role.value === 'viewer') return
  uploadUrlDialogVisible.value = true
}
const searchUrl = async () => {
  if (role.value === 'viewer') return
  searchLoading.value = true
  const req = new FormData()
  req.append('url', searchUrlQuery.value)
  req.append('depth', String(urlDeep.value || 0))
  const { data } = await post('/v1/document/internal', req)
  searchLoading.value = false
  if (data.code === 0 && data.message === 'success') {
    urlList.value = Object.values(data.data.by_depth).flat() as string[]
  }
}
const deleteUrl = (val: string) => {
  urlList.value = urlList.value.filter(url => url !== val)
}
const uploadUrl = async () => {
  if (role.value === 'viewer' || urlList.value.length === 0) return
  uploadUrlLoading.value = true
  const req = new FormData()
  req.append('kb_id', route.query.id as string)
  urlList.value.forEach((url) => {
    req.append('urls', url)
  })
  const { data } = await post('/v1/document/web_upload', req)
  if (data.code === 0 && data.message === 'success') {
    intervalId = setInterval(async () => {
      const { data: progressData } = await get(`/v1/document/web_upload_progress?task_id=${data.data.task_id}`)
      uploaded.value = progressData.data.success
      if (progressData.data.current === urlList.value.length) {
        getTableData()
        uploaded.value = 0
        clearInterval(intervalId)
        uploadUrlLoading.value = false
        uploadUrlDialogVisible.value = false
        ElMessage.success(t('knowledgeBases.dataset.uploadSuccess'))
        if (autoParse.value) {
          const idArray = progressData.data.files.map((item: { id: string }) => item.id)
          startOrCancelParse('start', idArray)
        }
      }
    }, 1000)
  } else {
    ElMessage.error(t('knowledgeBases.dataset.uploadFail'))
  }
}
const openRename = (id: string, name: string) => {
  if (role.value === 'viewer') return
  dialogVisible.value = true
  doc_id.value = id
  const lastDotIndex = name.lastIndexOf('.')
  if (lastDotIndex > 0 && lastDotIndex < name.length - 1) {
    filename.value = name.slice(0, lastDotIndex)
    fileType.value = name.slice(lastDotIndex + 1)
  } else {
    filename.value = name
    fileType.value = ''
  }
}
const selectable = () => role.value !== 'viewer'
const { getParsersByType } = useDocumentParsers()
onMounted(async () => {
  const { data } = await get(`/v1/kb/get-mykb_role?kb_id=${route.query.id}`)
  if (data.code === 0 && data.message === 'success') {
    role.value = data.data.role
  }
  getTableData()
  getKbDetail()
  startProgressCheck()
  addEventListener('click', handleGlobalClick)
})
onBeforeUnmount(() => {
  clearInterval(intervalId)
  removeEventListener('click', handleGlobalClick)
})
// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getTableData()
}
const deleteUploadFile = (index: number) => {
  fileList.value.splice(index, 1)
}
const startOrCancelParse = async (type: 'start' | 'cancel', id: string[]) => {
  if (role.value === 'viewer') return
  let run = 0
  if (type === 'start') {
    run = 1
  } else if (type === 'cancel') {
    run = 2
  }
  
  const { data } = await post('/v1/document/run', {
    doc_ids: id,
    run
  })
  if (data.code === 0 && data.message === 'success') {
    getTableData()
    uploadDialogVisible.value = false
  }
}
const checkBulk = () => {
  if (selectFiles.value.length === 0) return
  bulk.value = !bulk.value
}
// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getTableData()
}
const handleGlobalClick = () => {
  bulk.value = false
}
const base = getSystemBaseUnit()
const MAX_SIZE = base * base * 100 // 10MB
const MAX_COUNT = 32
GlobalWorkerOptions.workerSrc = '/lib/pdf.worker.min.mjs'
const CMAP_URL = '/lib/cmaps/'
// 校验上传文件
const validateFiles = async (files: FileList): Promise<globalThis.File[] | null> => {
  const fileArray = Array.from(files)
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
// 判断文件是否重复：根据 name 和 size 判断
const isDuplicate = (file: any, list?: any[]): boolean => {
  if (!Array.isArray(list)) return false
  return list.some(item => item.name === file.name && item.size === file.size)
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

  return new Promise((resolve) => {
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

// 拖拽上传文件
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
// 知识库上传文件
const upload = async () => {
  if (loading.value || !fileList.value.length) return
  loading.value = true
  if (!fileList.value.length) return
  const formData = new FormData()
  formData.append('kb_id', route.query.id as string)
  fileList.value.forEach((file: any) => {
    formData.append('file', file)
  })
  const { data } = await post('/v1/document/upload', formData, {}, {
    headers: { 'Content-Type': 'multipart/form-data' } as any
  })
  loading.value = false
  if (data.code === 0 && data.message === 'success') {
    fileList.value = []
    if (autoParse.value) {
      let parseList: string[] = []
      data.data.forEach((item: any) => {
        item.available = 0
        parseList.push(item.id)
      })
      startOrCancelParse('start', parseList)
    }
    getTableData()
    uploadDialogVisible.value = false
    ElMessage.success(t('knowledgeBases.dataset.uploadSuccess'))
  } else if (data.code === 500) {
    ElMessage.error(t('knowledgeBases.dataset.uploadFail'))
  }
}
// 获取解析状态对应的标签类型
const getParseStatusType = (progress: number) => {
  if (progress === 0) return 'border-[#FFC0614D] bg-[#FFF4E5] text-[#F09004]'
  if (progress === 1) return 'border-[#00CF854D] bg-[#E2F7EF] text-[#00CF85]'
  if (progress === -1) return 'border-[#F871714D] bg-[#FBEDED] text-[#F87171]'
  return 'border-[#244FF04D] bg-[#EBF1FE] text-[#396FFA]'
}
// 格式化解析状态
const formatParseStatus = (progress: number) => {
  if (progress === 0) return t('knowledgeBases.dataset.pending')
  if (progress === 1) return t('knowledgeBases.dataset.success')
  if (progress === -1) return t('knowledgeBases.dataset.fail')
  return `${t('knowledgeBases.dataset.parsing')} ${(progress * 100).toFixed(2)}%`
}
// 定义知识库数据类型
interface KnowledgeBaseData {
  id: string
  name: string
  description: string
  doc_num: number
  create_time: number
  create_date: string
  avatar?: string
  language: string
  permission: string
  chunk_num: number
  token_num: number
}

const rowKey = (row: KnowledgeBaseData) => row.id

// 表格多选事件处理
const handleSelectionChange = (selection: KnowledgeBaseData[]) => {
  selectFiles.value = []
  selection.forEach((item: KnowledgeBaseData) => {
    selectFiles.value.push(item.id)
  })
}
// 修改知识库文件解析方式
const changeMethod = async (parser_id: string, id: string) => {
  interface ParserConfig {
    layout_recognize?: string
    chunk_token_num?: number
    delimiter?: string
    auto_keywords?: number
    auto_questions?: number
    html4excel?: boolean
    graphrag?: {
      use_graphrag: boolean
    }
  }

  // 公共默认配置
  const COMMON_CONFIG: ParserConfig = {
    layout_recognize: 'DeepDOC',
    auto_keywords: 0,
    auto_questions: 0
  }

  // 特殊配置差异部分（只写不同的部分）
  const parserConfigMap: Record<string, ParserConfig | null> = {
    naive: {
      chunk_token_num: 512,
      delimiter: '\n',
      html4excel: false
    },
    qa: {}, // 空对象表示不传 config
    resume: {},
    table: {},
    tag: {},
    manual: {},
    paper: {},
    laws: {},
    presentation: {},
    one: {},
    book: {
      graphrag: { use_graphrag: false }
    }
  }
  const chunkMethod = parser_id
  const specificConfig = parserConfigMap[chunkMethod] || {}
  // 合并公共配置和特定配置（为空则不传）
  const parser_config = Object.keys(specificConfig).length !== 0
    ? { ...COMMON_CONFIG, ...specificConfig }
    : {}
  const { data } = await post('/v1/document/change_parser', {
    doc_id: id,
    parser_config,
    parser_id: parser_id
  })
  if (data.code === 0 && data.message === 'success') {
    getTableData()
    ElMessage.success(t('knowledgeBases.dataset.modifiedSuccess'))
  } else {
    ElMessage.error(t('knowledgeBases.dataset.modifiedFail'))
  }
}
// 批量修改状态
const bulkChangeStatus = async (val: number) => {
  selectFiles.value.forEach((id: string) => {
    changeStatus(val, id)
  })
}
// 修改知识库文件状态
const changeStatus = async (status: number, id: string) => {
  const { data } = await post('/v1/document/change_status', {
    doc_id: id,
    status: status
  })
  if (data.code === 0 && data.message === 'success') {
    getTableData()
    ElMessage({
      grouping: true,
      type: 'success',
      message: t('knowledgeBases.dataset.modifiedSuccess')
    })
  } else {
    ElMessage.error(t('knowledgeBases.dataset.modifiedFail'))
  }
}
// 知识库文件重命名
const rename = async () => {
  if (filename.value === '') {
    filenameError.value = t('knowledgeBases.dataset.namePlaceholder')
  }
  const { data } = await post('/v1/document/rename', {
    doc_id: doc_id.value,
    name: fileType.value ? `${filename.value}.${fileType.value}` : filename.value
  })
  if (data.code === 0 && data.message === 'success') {
    getTableData()
    dialogVisible.value = false
    ElMessage.success(t('knowledgeBases.dataset.modifiedSuccess'))
  }
}
// 获取知识库文件列表
const getTableData = async () => {
  const { data } = await get(`/v1/document/list?kb_id=${route.query.id}&keywords=${searchQuery.value}&page_size=${pageSize.value}&page=${currentPage.value}`)
  // 保存当前选中的ID
  const selectedIds = selectFiles.value.map(id => id)
  paginatedData.value = data.data.docs
  total.value = data.data.total
  startProgressCheck()
  // 渲染完成后恢复选中状态（确保 row-key 正确）
  nextTick(() => {
    selectedIds.forEach(id => {
      const match = paginatedData.value.find(item => item.id === id)
      if (match) {
        tableRef.value.toggleRowSelection(match, true)
      }
    })
  })
}
const startProgressCheck = () => {
  // 检查是否有需要监控的项目
  const hasProgressItems = paginatedData.value.some(item => 
    item.progress !== 0 && item.progress !== 1 && item.progress !== -1
  )
  
  if (hasProgressItems) {
    // 如果已有定时器，先清除
    if (intervalId) {
      clearInterval(intervalId)
    }
    
    // 设置定时器，定时重新调用 getTableData
    intervalId = setInterval(() => {
      getTableData() // 直接调用原方法重新查询
    }, 3000) // 3秒检查一次
  } else {
    // 没有需要监控的项目，清除定时器
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = undefined
    }
  }
}
interface File {
  id: string
  name: string
}
// 删除文件
const deleteFile = async (id: string[]) => {
  if (role.value === 'viewer') return
  ElMessageBox.confirm(t('knowledgeBases.dataset.deleteTip'), '', {
    confirmButtonText: t('knowledgeBases.dataset.yes'),
    cancelButtonText: t('knowledgeBases.dataset.no'),
    type: 'warning'
  }).then(async () => {
    try {
      const { data } = await post('/v1/document/rm', {
        doc_id: id
      })
      if (data.code === 0 && data.message === 'success') {
        getTableData()
        ElMessage.success(t('knowledgeBases.dataset.deleteSuccess'))
      }
    } catch {
      ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
    }
  })
}
// 下载文件
const download = async (value: File) => {
  if (role.value === 'viewer') return
  const data = await get('/v1/document/get/' + value.id,
    {
      responseType: 'blob'
    } as any
  )
  // @ts-ignore
  const blob = new Blob([data.data], { type: data.data.type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = value.name
  link.click()
  URL.revokeObjectURL(url)
}
interface KbDetail {
  id: string
  name: string
  avatar: string
}
const kbDetail = ref<KbDetail>({
  name: '',
  avatar: '',
  id: ''
})
// 获取知识库详情
const getKbDetail = async () => {
  const { data } = await get(`/v1/kb/detail?kb_id=${route.query.id}`)
  if (data.code === 0 && data.message === 'success') {
    kbDetail.value.name = data.data.name
    kbDetail.value.avatar = data.data.avatar
    kbDetail.value.id = data.data.parser_id
  }
}
</script>

<style lang="scss" scoped>
.adaptive {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
    display: unset;
  }
  &::-webkit-scrollbar-track {
    display: unset;
  }
  &::-webkit-scrollbar-thumb {
    display: unset;
    border-radius: 4px;
    background-color: #c1c1c1;
  }
  &::-webkit-scrollbar-corner {
    display: unset;
    background-color: transparent;
  }
}
:deep(.delete) {
  rect.borders {
    stroke: transparent;
  }
  rect.bg {
    fill: transparent;
  }
}
.transitions {
  transform: rotateZ(-90deg);
}
.privacy-box-active {
  min-width: 16px;
  max-width: 16px;
  min-height: 16px;
  max-height: 16px;
  color: #1460F3;
  svg {
    width: 100%;
    height: 100%;
  }
}
.privacy-box {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  border-radius: 2px;
  display: inline-block;
  border: 1px solid #AAAEB2;
}
:deep(.el-select) {
  &.table-select {
    .el-select__wrapper {
      height: 28px;
      max-height: 28px;
      min-height: auto;
      .el-select__input {
        font-size: 14px;
        line-height: 20px;
      }
    }
  }
}
.shadows {
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0px 4px 35px 0px #8195C82E;
}
:deep(.el-tooltip__trigger.svg) {
  rect.borders {
    stroke: transparent;
  }
  rect.bg {
    fill: transparent;
  }
  &:hover {
    rect.borders {
      stroke: #396FFA;
    }
    rect.bg {
      fill: #EBF1FE;
    }
  }
  &:focus {
    outline: none;
  }
}
</style>
