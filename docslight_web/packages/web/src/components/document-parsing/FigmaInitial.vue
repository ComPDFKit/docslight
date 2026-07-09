<template>
  <div class="figma-parsing-page">
    <header class="top-bar">
      <div class="title-wrap">
        <div class="menu-icon" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <span class="title">Document List</span>
      </div>
      <div class="top-actions">
        <div class="divider"></div>
        <div class="language">
          <svg class="globe" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <path d="M10 17.5C14.1421 17.5 17.5 14.1421 17.5 10C17.5 5.85786 14.1421 2.5 10 2.5C5.85786 2.5 2.5 5.85786 2.5 10C2.5 14.1421 5.85786 17.5 10 17.5Z" stroke="currentColor" stroke-width="1.4"/>
            <path d="M2.91663 10H17.0833" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
            <path d="M10 2.5C11.875 4.55357 12.8125 7.05357 12.8125 10C12.8125 12.9464 11.875 15.4464 10 17.5C8.125 15.4464 7.1875 12.9464 7.1875 10C7.1875 7.05357 8.125 4.55357 10 2.5Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
          </svg>
          En
        </div>
      </div>
    </header>

    <main class="workspace">
      <aside class="details-panel">
        <div class="details-header">
          <button class="icon-btn" type="button" aria-label="Back to document list" @click="$emit('openList')">&lsaquo;</button>
          <span>Extraction Details</span>
          <button class="filter-btn" type="button" aria-label="Filter files">
            <span></span>
          </button>
        </div>

        <div class="status-tabs">
          <button
            v-for="tab in statusTabs"
            :key="tab.key"
            :class="{ active: activeStatusTab === tab.key }"
            type="button"
            @click="setStatusTab(tab.key)"
          >{{ tab.label }}</button>
        </div>

        <label class="search-box">
          <span></span>
          <input v-model="searchQuery" type="text" placeholder="Search by file name" @keyup.enter="fetchFileList">
        </label>

        <div v-if="loadingList" class="list-state">Loading...</div>
        <div v-else-if="files.length === 0" class="list-state">No documents</div>
        <div v-else class="file-list">
          <button
            v-for="file in files"
            :key="file.fileId"
            class="file-item"
            :class="{ active: selectedFile?.fileId === file.fileId }"
            type="button"
            @click="selectFile(file)"
          >
            <div class="file-title"><span class="doc-icon"></span>{{ file.fileName }}</div>
            <div class="file-type">{{ getFileExtension(file.fileName) || 'Document' }}</div>
            <div class="tags">
              <span :class="getStatusClass(file.status)">{{ getStatusText(file.status) }}</span>
              <span v-if="file.status === 2" class="success">Completed</span>
            </div>
          </button>
        </div>
      </aside>

      <section class="preview-panel">
        <div class="panel-head">
          <div class="file-meta"><span class="doc-icon muted"></span>{{ selectedFile?.fileName || 'Source file' }}</div>
          <span v-if="selectedFile" class="tag" :class="getStatusClass(selectedFile.status)">{{ getStatusText(selectedFile.status) }}</span>
          <span v-if="selectedFile?.status === 2" class="tag success">Completed</span>
        </div>
        <div class="preview-label">Preview</div>
        <div v-loading="loadingDocument" class="paper-scroll source-scroll">
          <div v-show="selectedFile" ref="viewer" class="source-viewer"></div>
          <div v-if="!selectedFile" class="empty-state">Select a document to preview</div>
        </div>
        <div class="floating-tools">
          <button type="button">&lsaquo;</button>
          <span><u>{{ currentPage }}</u>/{{ selectedFile?.pageCount || totalPages || 1 }}</span>
          <button type="button">&rsaquo;</button>
          <i></i>
          <button type="button">H</button>
          <i></i>
          <span class="zoom">78% v</span>
          <button type="button">-</button>
          <button type="button">+</button>
        </div>
      </section>

      <section class="result-panel">
        <div class="result-toolbar">
          <div class="format-tabs">
            <button :class="{ active: outputType === 'md' }" type="button" @click="outputType = 'md'">Markdown</button>
            <button :class="{ active: outputType === 'json' }" type="button" @click="outputType = 'json'">JSON</button>
            <button :class="{ active: outputType === 'txt' }" type="button" @click="outputType = 'txt'">Text</button>
          </div>
          <div class="toolbar-actions">
            <button class="export" type="button" :disabled="!canUseResult" @click="downloadResult">&darr; Export</button>
            <button class="settings" type="button" aria-label="Parsing configuration" @click="openParsingConfigDialog">
              <span></span>
            </button>
          </div>
        </div>

        <nav class="content-tabs">
          <button class="active" type="button">All</button>
          <button type="button">Tables</button>
          <button type="button">Headings</button>
          <button type="button">Table of Contents</button>
          <button type="button">Images</button>
          <button type="button">Formulas</button>
        </nav>

        <div v-loading="loadingResult" class="markdown-preview">
          <template v-if="selectedFile?.status === 2">
            <JsonViewer v-if="outputType === 'json' && jsonResult" boxed expanded :expandDepth="7" sort theme="dark" :value="jsonResult" />
            <pre v-else-if="outputType === 'txt' && textResult" class="text-result">{{ textResult }}</pre>
            <div v-else-if="outputType === 'md' && markdownHtml" class="api-markdown" v-html="markdownHtml"></div>
            <div v-else class="empty-state">No result available</div>
          </template>
          <div v-else-if="selectedFile?.status === 0" class="result-status result-status--pending">
            <div class="status-content">
              <div class="status-visual" aria-hidden="true">
                <img class="status-figma-icon" :src="filePasteIcon" alt="">
              </div>
              <p>Pending parsing...</p>
            </div>
          </div>
          <div v-else-if="selectedFile?.status === 1" class="result-status result-status--processing">
            <div class="status-content">
              <div class="status-visual" aria-hidden="true">
                <img class="status-figma-icon" :src="filePasteIcon" alt="">
                <img class="status-processing-badge" :src="processingBadgeIcon" alt="">
              </div>
              <p>Parsing in progress. Please wait...</p>
            </div>
          </div>
          <div v-else-if="selectedFile?.status === 3" class="result-status result-status--failed">
            <div class="status-content">
              <div class="status-visual" aria-hidden="true">
                <img class="status-figma-icon" :src="failedInfoIcon" alt="">
              </div>
              <p>Parsing failed. Please try again</p>
            </div>
            <button class="state-action" type="button" @click="openParsingConfigDialog">
              <img :src="retryRefreshIcon" alt="">
              <span>Retry</span>
            </button>
          </div>
          <div v-else class="empty-state">Select a document to preview the parsing result</div>
        </div>

        <div class="result-footer">
          <div class="pager">
            <button type="button">&lsaquo;</button>
            <button type="button">&rsaquo;</button>
          </div>
          <div v-if="selectedFile?.status === 2" class="confirm-actions">
            <button class="ghost" type="button" :disabled="!selectedFile" @click="openParsingConfigDialog">Re-parse</button>
            <button class="primary" type="button" :disabled="!canUseResult">Confirm</button>
          </div>
          <div v-else-if="selectedFile?.status === 0" class="confirm-actions">
            <button class="primary" type="button" @click="reparseWithConfig">Start parsing</button>
          </div>
          <div v-else-if="selectedFile?.status === 1" class="confirm-actions">
            <span class="status-footer-text">Pending parsing...</span>
          </div>
          <div v-else-if="selectedFile?.status === 3" class="confirm-actions">
            <button class="primary icon-primary" type="button" @click="openParsingConfigDialog">
              <img :src="retryRefreshIcon" alt="">
              <span>Retry</span>
            </button>
          </div>
        </div>
      </section>
    </main>

    <el-dialog v-model="parsingConfigDialogVisible" width="635px" top="14vh" class="parsing-config-dialog" :show-close="false">
      <div class="config-dialog-head">
        <div class="config-dialog-title">Parsing Configuration</div>
        <button class="dialog-close" type="button" aria-label="Close configuration" @click="parsingConfigDialogVisible = false">
          <IdpClose />
        </button>
      </div>

      <div class="config-dialog-body">
        <section class="config-section">
          <div class="config-section-head">
            <h3>Auxiliary Content Parsing</h3>
            <p>The model automatically identifies and filters auxiliary content. Enabling this will restore parsing.</p>
          </div>
          <div class="config-grid">
            <label v-for="item in auxiliaryConfig" :key="item.key" class="config-item">
              <span>{{ item.label }}</span>
              <input v-model="item.enabled" type="checkbox">
              <i></i>
            </label>
          </div>
        </section>

        <section class="config-section">
          <div class="config-section-head compact">
            <h3>Model Parameters</h3>
          </div>
          <div class="config-grid">
            <label v-for="item in modelConfig" :key="item.key" class="config-item">
              <span>{{ item.label }}</span>
              <b v-if="item.help" aria-hidden="true">?</b>
              <input v-model="item.enabled" type="checkbox">
              <i></i>
            </label>
          </div>
        </section>
      </div>

      <div class="config-dialog-actions">
        <button class="dialog-ghost" type="button" @click="parsingConfigDialogVisible = false">Cancel</button>
        <button class="dialog-primary" type="button" @click="reparseWithConfig">Re-parse</button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import { JsonViewer } from 'vue3-json-viewer'
import 'vue3-json-viewer/dist/vue3-json-viewer.css'
import MarkdownIt from 'markdown-it'
import mk from 'markdown-it-katex'
import JSZip from 'jszip'
import ComPDFKitViewer from '../../assets/@compdfkit/webviewer'
import failedInfoIcon from '../../assets/images/document-parsing-failed-info.svg'
import filePasteIcon from '../../assets/images/document-parsing-file-paste.svg'
import processingBadgeIcon from '../../assets/images/document-parsing-processing-badge.svg'
import retryRefreshIcon from '../../assets/images/document-parsing-retry-refresh.svg'
import { getEnv } from '../../utils/env'
import { get, post } from '../../utils/request'
import IdpClose from '../idp/IdpClose.vue'
import { sanitizeHtml, sanitizeTableHtml } from '../../utils/sanitizeHtml'

defineEmits<{
  openList: []
}>()

interface FileData {
  fileId: string
  fileName: string
  pageCount?: number
  uploadTime?: string
  fileDownUrl?: string
  resultDownUrl?: string
  status?: number
}

interface ResultImageData {
  fullPath: string
  name: string
  blob: Blob
}

type StatusTabKey = 'all' | 'confirmed' | 'unconfirmed'

const md = new MarkdownIt({ html: false })
md.use(mk)

const files = ref<FileData[]>([])
const selectedFile = ref<FileData | null>(null)
const searchQuery = ref('')
const activeStatusTab = ref<StatusTabKey>('all')
const loadingList = ref(false)
const loadingDocument = ref(false)
const loadingResult = ref(false)
const outputType = ref<'md' | 'json' | 'txt'>('md')
const parsingConfigDialogVisible = ref(false)
const jsonResult = ref<any>(null)
const markdownResult = ref('')
const textResult = ref('')
const imageData = ref<ResultImageData[]>([])
const viewer = ref<HTMLElement>()
const currentPage = ref(1)
const totalPages = ref(1)
let UI: any = null
let docViewer: any = null
let sourceLoadToken = 0
const blobUrlCache = new Map<string, string>()

const statusTabs: Array<{ key: StatusTabKey, label: string, statuses: number[] }> = [
  { key: 'all', label: 'All', statuses: [] },
  { key: 'confirmed', label: 'Confirmed', statuses: [2] },
  { key: 'unconfirmed', label: 'Unconfirmed', statuses: [0, 1, 3] }
]

const auxiliaryConfig = ref([
  { key: 'header', label: 'Header', enabled: true },
  { key: 'footer', label: 'Footer', enabled: true },
  { key: 'pageNumber', label: 'Page Number', enabled: true },
  { key: 'marginalNotes', label: 'Marginal Notes', enabled: true },
  { key: 'headerImages', label: 'Header Images', enabled: true },
  { key: 'footerImages', label: 'Footer Images', enabled: true },
  { key: 'footnotes', label: 'Footnotes', enabled: true }
])

const modelConfig = ref([
  { key: 'imageOrientationCorrection', label: 'Image Orientation Correction', enabled: true, help: true },
  { key: 'layoutAnalysis', label: 'Layout Analysis', enabled: false, help: true },
  { key: 'stampRecognition', label: 'Stamp Recognition', enabled: true, help: true },
  { key: 'crossPageTableMerging', label: 'Cross-page Table Merging', enabled: true, help: true },
  { key: 'imageDistortionCorrection', label: 'Image Distortion Correction', enabled: true, help: true },
  { key: 'chartRecognition', label: 'Chart Recognition', enabled: true, help: true },
  { key: 'imageTextRecognition', label: 'Image Text Recognition (OCR)', enabled: true, help: true },
  { key: 'paragraphHeadingLevelRecognition', label: 'Paragraph Heading Level Recognition', enabled: true, help: true }
])

const canUseResult = computed(() => selectedFile.value?.status === 2 && Boolean(selectedFile.value?.resultDownUrl))
const activeStatusValues = computed(() => statusTabs.find(tab => tab.key === activeStatusTab.value)?.statuses || [])

const setStatusTab = async (key: StatusTabKey) => {
  if (activeStatusTab.value === key) return
  activeStatusTab.value = key
  await fetchFileList()
}

const openParsingConfigDialog = () => {
  parsingConfigDialogVisible.value = true
}

const reparseWithConfig = async () => {
  if (!selectedFile.value?.fileId) return
  try {
    loadingResult.value = true
    const formData = new FormData()
    formData.append('idpFileIds', selectedFile.value.fileId)
    formData.append('type', 'LAYOUT')
    formData.append('parseConfig', JSON.stringify({
      auxiliaryContentParsing: Object.fromEntries(auxiliaryConfig.value.map(item => [item.key, item.enabled])),
      modelParameters: Object.fromEntries(modelConfig.value.map(item => [item.key, item.enabled]))
    }))
    const { data } = await post('/api/idp/files-start', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
    if (data.code === 200) {
      parsingConfigDialogVisible.value = false
      ElMessage.success('Re-parse started.')
      await fetchFileList()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('Failed to re-parse.')
  } finally {
    loadingResult.value = false
  }
}

const markdownHtml = computed(() => {
  if (markdownResult.value) return renderHtmlWithImages(md.render(markdownResult.value))
  const detail = jsonResult.value?.result?.detail
  if (!Array.isArray(detail)) return ''
  return detail.map((item: any) => renderResultBlock(item)).join('')
})

const renderResultBlock = (item: any) => {
  if (item?.type === 'image') {
    const imageSrc = resolveImageSrcByPath(item.image_url || item.text || '')
    if (imageSrc) return sanitizeHtml(`<div class="api-result-block api-image-block"><img src="${imageSrc}" alt="Image"></div>`)
  }
  if (!item?.text) return ''
  if (item.type === 'table') return `<div class="api-result-block">${renderHtmlWithImages(item.text, true)}</div>`
  return `<div class="api-result-block">${renderHtmlWithImages(md.render(item.text))}</div>`
}

const renderHtmlWithImages = (rawHtml: string, isTable = false) => {
  if (!rawHtml) return ''
  const sanitize = isTable ? sanitizeTableHtml : sanitizeHtml
  const container = document.createElement('div')
  container.innerHTML = sanitize(rawHtml)
  container.querySelectorAll('img').forEach((img) => {
    const originalSrc = img.getAttribute('src') || ''
    const resolvedSrc = resolveImageSrcByPath(originalSrc)
    if (resolvedSrc) img.setAttribute('src', resolvedSrc)
  })
  return sanitize(container.innerHTML)
}

const resolveImageSrcByPath = (src: string) => {
  if (!src) return ''
  if (/^(https?:|blob:|data:)/i.test(src)) return src

  const normalized = src.replace(/^\.\//, '').replace(/^\//, '')
  const targetImg = imageData.value.find((img) => {
    const fullPath = img.fullPath.replace(/^\//, '')
    return fullPath === normalized || fullPath.endsWith(`/${normalized}`) || img.name === normalized
  })

  if (!targetImg) return src
  return getBlobUrl(targetImg.fullPath || targetImg.name, targetImg.blob)
}

const getBlobUrl = (key: string, blob: Blob) => {
  if (!blobUrlCache.has(key)) {
    blobUrlCache.set(key, URL.createObjectURL(blob))
  }
  return blobUrlCache.get(key)!
}

const clearBlobUrls = () => {
  for (const url of blobUrlCache.values()) {
    URL.revokeObjectURL(url)
  }
  blobUrlCache.clear()
}

const getFileExtension = (fileName?: string) => {
  if (!fileName) return ''
  const index = fileName.lastIndexOf('.')
  if (index <= 0 || index === fileName.length - 1) return ''
  return fileName.slice(index + 1).toUpperCase()
}

const getStatusText = (status?: number) => {
  if (status === 0) return 'Pending'
  if (status === 1) return 'Processing'
  if (status === 2) return 'Confirmed'
  if (status === 3) return 'Failed'
  return 'Unknown'
}

const getStatusClass = (status?: number) => {
  if (status === 0) return 'warn'
  if (status === 1) return 'processing'
  if (status === 3) return 'error'
  return 'success'
}

const fetchFileList = async () => {
  loadingList.value = true
  try {
    const statusFilter = activeStatusValues.value.join(',')
    const params = new URLSearchParams({
      page: '1',
      pageSize: '20',
      fileName: searchQuery.value,
      taskType: 'LAYOUT',
      startTime: '',
      endTime: '',
      status: statusFilter
    })
    const { data }: any = await get(`/api/idp/getFileList?${params.toString()}`)
    const records = (Array.isArray(data?.data?.records) ? data.data.records : [])
      .filter((file: FileData) => !activeStatusValues.value.length || activeStatusValues.value.includes(Number(file.status)))
    files.value = records
    const nextSelected = records.find((file: FileData) => file.fileId === selectedFile.value?.fileId)
      || records.find((file: FileData) => file.status === 2 && file.resultDownUrl)
      || records[0]
      || null
    if (nextSelected) {
      await selectFile(nextSelected)
    } else {
      selectedFile.value = null
      resetResult()
    }
  } catch {
    ElMessage.error('Failed to load document list.')
  } finally {
    loadingList.value = false
  }
}

const selectFile = async (file: FileData) => {
  selectedFile.value = file
  currentPage.value = 1
  totalPages.value = file.pageCount || 1
  await nextTick()
  loadSourceFile(file)
  loadResultFile(file)
}

const initViewer = async () => {
  if (!viewer.value || UI) return
  const license = getEnv('LICENSE_KEY') || ''
  const core = await ComPDFKitViewer.init({
    license,
    pdfUrl: '',
    path: '/',
    enableDefaultFont: true,
    showToolbarControl: false
  }, viewer.value)
  UI = core.UI
  docViewer = core.docViewer
  docViewer.addEvent('onPageNumberUpdated', (data: { pageNumber: number }) => {
    currentPage.value = data.pageNumber
  })
  docViewer.addEvent('documentloaded', () => {
    const pageCount = docViewer.getPageCount?.()
    if (pageCount) totalPages.value = pageCount
    loadingDocument.value = false
  })
}

const loadSourceFile = async (file: FileData) => {
  const token = ++sourceLoadToken
  if (!file.fileDownUrl) return
  loadingDocument.value = true
  try {
    const response = await fetch(file.fileDownUrl)
    if (!response.ok) throw new Error('Failed to fetch source file')
    const blob = await response.blob()
    if (token !== sourceLoadToken) return

    await nextTick()
    await initViewer()
    const sourceName = file.fileName?.replace(/\.[^.]+$/, '.pdf') || 'source.pdf'
    const sourceFile = new File([blob], sourceName, { type: blob.type || 'application/pdf' })
    const loadTask = UI?.loadDocument(sourceFile)
    if (loadTask?.then) await loadTask
    if (token === sourceLoadToken) loadingDocument.value = false
  } catch {
    loadingDocument.value = false
    ElMessage.error('Failed to load source file.')
  }
}

const resetResult = () => {
  clearBlobUrls()
  jsonResult.value = null
  markdownResult.value = ''
  textResult.value = ''
  imageData.value = []
}

const loadResultFile = async (file: FileData) => {
  resetResult()
  if (!file.resultDownUrl || file.status !== 2) return
  loadingResult.value = true
  try {
    const response = await fetch(file.resultDownUrl)
    if (!response.ok) throw new Error('Failed to fetch result')
    const contentType = response.headers.get('content-type') || ''
    if (contentType.includes('application/json') || file.resultDownUrl.toLowerCase().endsWith('.json')) {
      jsonResult.value = await response.json()
      return
    }
    await extractResultZip(response)
  } catch (error) {
    console.error(error)
    ElMessage.error('Failed to load parsing result.')
  } finally {
    loadingResult.value = false
  }
}

const extractResultZip = async (response: Response) => {
  const zip = new JSZip()
  await zip.loadAsync(await response.arrayBuffer())
  for (const [fullPath, fileEntry] of Object.entries(zip.files)) {
    if (fileEntry.dir) continue
    const lowerName = fullPath.toLowerCase()
    if (lowerName.endsWith('.json')) {
      jsonResult.value = JSON.parse(await fileEntry.async('text'))
    } else if (/\.(png|jpg|jpeg|gif|webp|bmp)$/i.test(lowerName)) {
      imageData.value.push({
        fullPath,
        name: fullPath.split('/').pop() || fullPath,
        blob: await fileEntry.async('blob')
      })
    } else if (lowerName.endsWith('.md')) {
      markdownResult.value = await fileEntry.async('text')
    } else if (lowerName.endsWith('.txt')) {
      textResult.value = await fileEntry.async('text')
    }
  }
}

const downloadResult = () => {
  if (!selectedFile.value?.resultDownUrl) return
  const anchor = document.createElement('a')
  anchor.href = selectedFile.value.resultDownUrl
  anchor.download = `${selectedFile.value.fileName || 'result'}-result.zip`
  anchor.target = '_blank'
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
}

onMounted(() => {
  fetchFileList()
})

onUnmounted(() => {
  sourceLoadToken++
  clearBlobUrls()
})
</script>

<style lang="scss" scoped>
.figma-parsing-page {
  min-height: 100vh;
  background: #f3f6ff;
  color: #0c131f;
  font-family: 'Encode Sans', Helvetica, Arial, sans-serif;
}

button {
  font-family: inherit;
}

.top-bar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e7e8e8;
}

.title-wrap,
.top-actions,
.language,
.details-header,
.file-title,
.panel-head,
.result-toolbar,
.result-footer,
.pager,
.confirm-actions,
.toolbar-actions {
  display: flex;
  align-items: center;
}

.menu-icon {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;

  span {
    width: 11px;
    height: 1px;
    background: #0c131f;
  }
}

.title {
  font-size: 16px;
  line-height: 24px;
  font-weight: 400;
}

.top-actions {
  gap: 16px;
  padding-right: 44px;
  font-size: 14px;
  line-height: 22px;
  color: #0c131f;
}

.divider {
  width: 1px;
  height: 40px;
  background: #e7e8e8;
}

.language {
  min-height: 32px;
  padding: 5px 6px;
  gap: 8px;
}

.globe {
  width: 20px;
  height: 20px;
  margin-right: 0;
  display: block;
  flex: 0 0 auto;
}

.workspace {
  height: calc(100vh - 56px);
  display: grid;
  grid-template-columns: 230px minmax(360px, 1fr) minmax(360px, 1fr);
  gap: 8px;
  padding: 16px;
  overflow: hidden;
}

.details-panel,
.preview-panel,
.result-panel {
  min-height: 0;
  background: #ffffff;
  border-radius: 4px;
  overflow: hidden;
}

.details-panel {
  padding: 16px 12px;
}

.details-header {
  justify-content: flex-start;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;
  margin-bottom: 8px;

  > span {
    flex: 1;
    line-height: 32px;
  }
}

.icon-btn,
.filter-btn,
.pager button,
.floating-tools button,
.settings {
  border: 0;
  background: transparent;
  color: #0c131f;
  cursor: pointer;
}

.icon-btn {
  width: 16px;
  height: 32px;
  font-size: 16px;
  line-height: 16px;
}

.filter-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  position: relative;

  span,
  &::before,
  &::after {
    content: '';
    position: absolute;
    left: 10px;
    width: 12px;
    height: 1px;
    background: #0c131f;
  }

  span { top: 10px; }
  &::before { top: 15px; width: 9px; }
  &::after { top: 20px; width: 5px; }
}

.format-tabs {
  display: flex;
  border: 1px solid #d7e2fe;
  border-radius: 2px;
  overflow: hidden;

  button {
    border: 0;
    background: #ffffff;
    color: #0c131f;
    cursor: pointer;
  }

  .active {
    color: #396ffa;
    background: #f5f7ff;
  }
}

.status-tabs {
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px;
  margin-bottom: 8px;
  border: 1px solid #d7e2fe;
  border-radius: 3px;
  background: #f5f7ff;

  button {
    flex: 0 0 auto;
    height: 22px;
    padding: 2px 8px;
    border: 0;
    border-radius: 3px;
    background: transparent;
    color: #0c131f;
    font-size: 12px;
    line-height: 18px;
    cursor: pointer;
  }

  .active {
    color: #396ffa;
    background: #ffffff;
  }
}

.search-box {
  height: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  margin-bottom: 8px;
  border: 0;
  border-radius: 3px;
  background: #f3f3f4;
  color: rgba(12, 19, 31, 0.4);

  span {
    width: 12px;
    height: 12px;
    border: 1px solid currentColor;
    border-radius: 50%;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      width: 5px;
      height: 1px;
      background: currentColor;
      right: -4px;
      bottom: -2px;
      transform: rotate(45deg);
    }
  }

  input {
    min-width: 0;
    border: 0;
    background: transparent;
    outline: 0;
    font-size: 14px;
    line-height: 22px;
    color: rgba(12, 19, 31, 0.4);
  }
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  max-height: calc(100vh - 202px);
  overflow: auto;
}

.list-state,
.empty-state {
  color: rgba(12, 19, 31, 0.42);
  font-size: 12px;
  line-height: 20px;
  text-align: center;
}

.list-state {
  padding: 24px 8px;
}

.file-item {
  width: 100%;
  padding: 4px 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;

  &.active {
    background: #f5f7ff;
    border-color: #d7e2fe;

    .file-title {
      color: #396ffa;
    }
  }
}

.doc-icon {
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
  margin-right: 4px;
  border: 1px solid currentColor;
  border-radius: 1px;
  position: relative;

  &::before,
  &::after {
    content: '';
    position: absolute;
    left: 3px;
    right: 3px;
    height: 1px;
    background: currentColor;
  }

  &::before { top: 5px; }
  &::after { top: 8px; }
}

.file-title {
  min-width: 0;
  color: rgba(12, 19, 31, 0.6);
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-type {
  margin-left: 20px;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  line-height: 20px;
}

.tags {
  display: flex;
  gap: 8px;
  margin-left: 20px;

  span {
    padding: 0 4px;
    border-radius: 3px;
    font-size: 12px;
    line-height: 20px;
  }
}

.success,
.tag.success {
  color: #67d1a0;
  background: #ecf9f3;
}

.warn {
  color: #f5a13a;
  background: #fef3e6;
}

.error {
  color: #d44040;
  background: #fbecec;
}

.processing {
  color: #396ffa;
  background: #f5f7ff;
}

.preview-panel {
  position: relative;
  display: flex;
  flex-direction: column;
}

.panel-head {
  height: 28px;
  gap: 6px;
  padding: 0 10px;
  border-bottom: 1px solid #e7e8e8;
  font-size: 11px;
}

.file-meta {
  display: flex;
  align-items: center;
  margin-right: 2px;
}

.muted {
  color: rgba(12, 19, 31, 0.4);
}

.tag {
  padding: 0 4px;
  border-radius: 3px;
  font-size: 10px;
  line-height: 16px;
}

.preview-label {
  height: 18px;
  padding: 0 10px;
  color: rgba(12, 19, 31, 0.4);
  background: #f3f3f4;
  font-size: 10px;
  line-height: 18px;
}

.paper-scroll {
  min-height: 0;
  flex: 1;
  padding: 18px 0 78px;
  overflow: auto;
  background: #f3f3f4;
}

.source-scroll {
  padding: 0;
}

.source-viewer {
  width: 100%;
  height: 100%;
  min-height: 0;
  background: #f3f3f4;
}

.source-scroll .empty-state,
.markdown-preview .empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.paper {
  width: min(72%, 430px);
  min-width: 336px;
  margin: 0 auto;
  padding: 28px 28px 34px;
  background: #ffffff;
  color: #225aa4;
  box-shadow: 0 1px 2px rgba(12, 19, 31, 0.08);

  h1 {
    margin: 0 0 18px;
    font-size: 30px;
    line-height: 36px;
    letter-spacing: 0;
    color: #225aa4;
  }

  h2 {
    margin: 0 0 8px;
    color: #3b73d9;
    font-size: 16px;
    line-height: 22px;
    letter-spacing: 2px;
  }

  h3 {
    margin: 12px 0 5px;
    color: #2861bf;
    font-size: 13px;
    line-height: 18px;
    letter-spacing: 1px;
  }

  p {
    margin: 0;
    color: #225aa4;
    font-size: 8px;
    line-height: 11px;
    letter-spacing: 2px;
  }

  table {
    width: 100%;
    margin-top: 16px;
    border-collapse: collapse;
    color: #0c131f;
    font-size: 8px;
    line-height: 14px;
  }

  th {
    padding: 4px 6px;
    background: #214f8a;
    color: #ffffff;
    text-align: left;
  }

  td {
    padding: 5px 6px;
    border: 2px solid #214f8a;
  }
}

.hero-image,
.md-image {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #e8eef4 0%, #cad7e3 42%, #935733 100%);
}

.hero-image {
  height: 92px;
  margin-bottom: 8px;
}

.photo-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(34, 90, 164, 0.18);
  transform: rotate(-10deg);
}

.photo-card.one {
  width: 135px;
  height: 72px;
  left: 26px;
  top: 24px;
}

.photo-card.two {
  width: 120px;
  height: 58px;
  left: 72px;
  top: 10px;
}

.hand-shape {
  position: absolute;
  width: 150px;
  height: 140px;
  right: 28px;
  top: -30px;
  background: linear-gradient(135deg, #f2b476 0%, #9b5534 52%, #5b3329 100%);
  border-radius: 48% 42% 38% 56%;
  transform: rotate(-22deg);

  &::after {
    content: '';
    position: absolute;
    width: 12px;
    height: 132px;
    left: 62px;
    top: 18px;
    background: #161616;
    border-radius: 8px;
    transform: rotate(18deg);
  }
}

.floating-tools {
  position: absolute;
  left: 50%;
  bottom: 14px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 2px;
  background: rgba(0, 0, 0, 0.8);
  color: #ffffff;
  font-size: 12px;

  button {
    width: 20px;
    height: 20px;
    color: #ffffff;
  }

  i {
    width: 1px;
    height: 20px;
    background: rgba(255, 255, 255, 0.12);
  }

  .zoom {
    padding: 3px 8px;
    border: 1px solid rgba(255, 255, 255, 0.6);
    border-radius: 2px;
  }
}

.result-panel {
  display: flex;
  flex-direction: column;
  padding: 8px 12px 10px;
}

.result-toolbar {
  height: 32px;
  justify-content: space-between;
}

.format-tabs {
  height: 24px;

  button {
    min-width: 48px;
    padding: 0 8px;
    font-size: 10px;
  }
}

.toolbar-actions {
  gap: 6px;
}

.export {
  height: 24px;
  padding: 0 8px;
  border: 1px solid #396ffa;
  border-radius: 2px;
  background: #ffffff;
  color: #396ffa;
  font-size: 10px;
  cursor: pointer;
}

.export:disabled,
.confirm-actions button:disabled {
  border-color: #dcdde1;
  background: #f3f3f4;
  color: rgba(12, 19, 31, 0.3);
  cursor: not-allowed;
}

.settings {
  width: 24px;
  height: 24px;
  border: 1px solid #dcdde1;
  border-radius: 2px;
  position: relative;

  span,
  &::before,
  &::after {
    content: '';
    position: absolute;
    left: 7px;
    width: 10px;
    height: 1px;
    background: #0c131f;
  }

  span { top: 7px; }
  &::before { top: 11px; width: 7px; }
  &::after { top: 15px; width: 4px; }
}

.content-tabs {
  height: 42px;
  display: flex;
  align-items: flex-end;
  gap: 26px;
  border-bottom: 1px solid #e7e8e8;

  button {
    height: 32px;
    border: 0;
    border-bottom: 2px solid transparent;
    background: transparent;
    color: rgba(12, 19, 31, 0.4);
    font-size: 12px;
    cursor: pointer;
    white-space: nowrap;
  }

  .active {
    color: #396ffa;
    border-bottom-color: #396ffa;
  }
}

.markdown-preview {
  min-height: 0;
  flex: 1;
  padding-top: 16px;
  overflow: auto;
}

.result-status {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 24px 32px 44px;
  color: rgba(12, 19, 31, 0.6);
  text-align: center;

  p {
    margin: 0;
    color: rgba(12, 19, 31, 0.6);
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    white-space: nowrap;
  }
}

.status-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.status-visual {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  position: relative;
  background: #f5f7ff;
  border: 1px solid #b0c5fd;

  &::before,
  &::after,
  span {
    content: '';
    position: absolute;
    display: block;
  }
}

.result-status--pending {
  .status-visual {
    background: #f5f7ff;
    border-color: #b0c5fd;
  }
}

.result-status--processing {
  .status-visual {
    background: #f5f7ff;
    border-color: #b0c5fd;
  }
}

.result-status--failed {
  .status-visual {
    background: #fbecec;
    border-color: #eeb3b3;
  }
}

.status-figma-icon {
  width: 32px;
  height: 32px;
  display: block;
}

.status-processing-badge {
  width: 33px;
  height: 33px;
  position: absolute;
  right: -1px;
  bottom: -1px;
  display: block;
}

.state-action {
  min-width: 72px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 5px 16px;
  border: 0;
  border-radius: 3px;
  background: #396ffa;
  color: #ffffff;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;

  img {
    width: 16px;
    height: 16px;
    display: block;
  }
}

.text-result {
  margin: 0;
  color: #0c131f;
  font-family: Helvetica, Arial, sans-serif;
  font-size: 12px;
  line-height: 18px;
  white-space: pre-wrap;
  word-break: break-word;
}

.api-markdown {
  color: #0c131f;
  font-family: Helvetica, Arial, sans-serif;
  font-size: 12px;
  line-height: 18px;
}

.api-markdown :deep(.api-result-block) {
  margin-bottom: 4px;
  padding: 6px;
  border: 1px solid #396ffa;
  border-radius: 2px;
}

.api-markdown :deep(table) {
  width: 100%;
  border-collapse: collapse;
}

.api-markdown :deep(img) {
  max-width: 100%;
  height: auto;
  vertical-align: top;
}

.api-markdown :deep(.api-image-block) {
  display: inline-flex;
  max-width: 100%;
}

.api-markdown :deep(td),
.api-markdown :deep(th) {
  padding: 4px 6px;
  border: 1px solid #d7e2fe;
}

.md-block,
.md-image {
  border: 1px solid #396ffa;
  border-radius: 2px;
}

.title-block {
  padding: 2px 4px;
  color: #0c131f;
  font-size: 30px;
  line-height: 36px;
  font-weight: 600;
}

.heading-block {
  margin-top: 4px;
  padding: 2px 6px;
  color: #0c131f;
  font-size: 17px;
  line-height: 24px;
  font-weight: 600;
}

.md-image {
  height: 96px;
  margin-top: 4px;
}

.subhead {
  margin-top: 4px;
  padding: 3px 6px;
  color: #0c131f;
  font-size: 14px;
  line-height: 20px;
  font-weight: 600;
}

.paragraph {
  margin-top: 4px;
  padding: 6px;
  color: #0c131f;
  font-family: Helvetica, Arial, sans-serif;
  font-size: 10px;
  line-height: 13px;
  letter-spacing: 1px;
}

.result-footer {
  min-height: 45px;
  justify-content: space-between;
  border-top: 1px solid #f3f3f4;
  padding-top: 12px;
}

.pager {
  gap: 8px;

  button {
    width: 32px;
    height: 32px;
    border: 1px solid #396ffa;
    border-radius: 3px;
    background: #ffffff;
    color: #396ffa;
    font-size: 16px;
  }
}

.confirm-actions {
  gap: 8px;

  button {
    min-width: 72px;
    height: 32px;
    padding: 5px 16px;
    border-radius: 3px;
    font-size: 14px;
    line-height: 22px;
    cursor: pointer;
  }

  .ghost {
    border: 1px solid #396ffa;
    background: #ffffff;
    color: #396ffa;
  }

  .primary {
    border: 1px solid #396ffa;
    background: #396ffa;
    color: #ffffff;
  }

  .icon-primary {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    img {
      width: 16px;
      height: 16px;
      display: block;
    }
  }
}

.status-footer-text {
  display: flex;
  align-items: center;
  height: 32px;
  padding: 5px 16px;
  border-radius: 3px;
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
  white-space: nowrap;
}

:deep(.parsing-config-dialog) {
  border-radius: 6px;
  overflow: hidden;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
    color: #0c131f;
    font-family: 'Encode Sans', Helvetica, Arial, sans-serif;
  }
}

.config-dialog-head {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background: #f3f3f4;
}

.config-dialog-title {
  color: #0c131f;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.dialog-close {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.config-dialog-body {
  width: 635px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px 32px 16px;
  background: #ffffff;
}

.config-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.config-section-head {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.compact {
    gap: 0;
  }

  h3 {
    margin: 0;
    color: #0c131f;
    font-size: 14px;
    font-weight: 600;
    line-height: 22px;
  }

  p {
    margin: 0;
    color: rgba(12, 19, 31, 0.4);
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
  }
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(2, 280px);
  gap: 8px;
}

.config-item {
  width: 280px;
  height: 38px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 6px;
  color: rgba(12, 19, 31, 0.6);
  font-size: 14px;
  line-height: 22px;

  span {
    min-width: 0;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  b {
    width: 16px;
    height: 16px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #94969d;
    border-radius: 50%;
    color: #94969d;
    font-size: 11px;
    font-weight: 600;
    line-height: 16px;
  }

  input {
    position: absolute;
    opacity: 0;
    pointer-events: none;
  }

  i {
    width: 32px;
    height: 20px;
    flex: 0 0 32px;
    position: relative;
    border-radius: 12px;
    background: #dcdde1;
    cursor: pointer;
    transition: background 0.16s ease;

    &::after {
      content: '';
      position: absolute;
      width: 12px;
      height: 12px;
      left: 4px;
      top: 4px;
      border-radius: 50%;
      background: #ffffff;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.16);
      transition: transform 0.16s ease, width 0.16s ease, height 0.16s ease, top 0.16s ease;
    }
  }

  input:checked + i {
    background: #396ffa;

    &::after {
      width: 15px;
      height: 15px;
      top: 2.5px;
      transform: translateX(10.5px);
    }
  }
}

.config-dialog-actions {
  height: 70px;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 32px 32px;
  background: #ffffff;
}

.dialog-ghost,
.dialog-primary {
  min-width: 72px;
  height: 32px;
  padding: 5px 16px;
  border: 0;
  border-radius: 3px;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;
}

.dialog-ghost {
  background: #e7e8e8;
  color: #0c131f;
}

.dialog-primary {
  background: #396ffa;
  color: #ffffff;
}

@media (max-width: 1180px) {
  .workspace {
    grid-template-columns: 230px minmax(320px, 1fr) minmax(330px, 1fr);
  }

  .content-tabs {
    gap: 14px;
  }
}

@media (max-width: 930px) {
  .workspace {
    height: auto;
    min-height: calc(100vh - 40px);
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .details-panel,
  .preview-panel,
  .result-panel {
    min-height: 420px;
  }

  .paper {
    width: min(92%, 430px);
  }
}
</style>
