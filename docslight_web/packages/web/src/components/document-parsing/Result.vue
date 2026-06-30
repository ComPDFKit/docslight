<template>
  <div v-loading="loading" class="parsing-result-detail">
    <aside class="detail-sidebar">
      <div class="detail-sidebar__head">
        <button class="detail-sidebar__back" type="button" @click="changeActive('list')">
          <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
          {{ t('parsing.parsingDetails') }}
        </button>
      </div>
      <div class="detail-sidebar__divider"></div>
      <div class="detail-sidebar__tabs">
        <button
          v-for="tab in detailFilterTabs"
          :key="tab.value"
          type="button"
          :class="detailFilter === tab.value && 'is-active'"
          @click="detailFilter = tab.value"
        >{{ t(tab.labelKey) }}</button>
      </div>
      <label class="detail-sidebar__search">
        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
        <input v-model="detailSearch" type="text" :placeholder="t('parsing.searchFile')" @keyup.enter="getDetailList">
      </label>
      <div class="detail-sidebar__divider"></div>
      <div class="detail-sidebar__list">
        <button
          v-for="row in filteredDetailList"
          :key="row.fileId"
          class="detail-file-item"
          :class="id === row.fileId && 'is-selected'"
          type="button"
          @click="openFile(row)"
        >
          <div class="detail-file-item__name">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2"/><path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
            <span>{{ row.fileName }}</span>
          </div>
          <div class="detail-file-item__desc">{{ getFileExtension(row.fileName) || t('parsing.document') }}</div>
          <div class="detail-file-item__tags">
            <span class="mini-tag" :class="statusTone(row.status)">{{ statusText(row.status) }}</span>
            <span class="mini-tag" :class="reviewTone(row)">{{ reviewStatusText(row) }}</span>
          </div>
        </button>
      </div>
    </aside>

    <section class="detail-preview">
      <div class="detail-preview__file">
        <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2"/><path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
        <el-tooltip popper-class="tip-item tip-item--filename" effect="dark" :content="fileName" placement="top">
          <span>{{ fileName }}</span>
        </el-tooltip>
        <span class="mini-tag" :class="statusTone(currentFile?.status)">{{ statusText(currentFile?.status) }}</span>
        <span class="mini-tag" :class="reviewTone(currentFile)">{{ reviewStatusText(currentFile) }}</span>
      </div>
      <div class="detail-preview__label">{{ t('parsing.preview') }}</div>
      <div id="webviewer" ref="viewer" class="detail-preview__viewer"></div>
    </section>

    <section class="detail-output" :class="outputType === 'json' && 'is-json'">
      <div class="detail-output__head">
        <div class="detail-output__format-tabs">
          <button type="button" @click="changeConvert('md')" :class="outputType === 'md' && 'is-active'">Markdown</button>
          <button type="button" @click="changeConvert('json')" :class="outputType === 'json' && 'is-active'">JSON</button>
          <button type="button" @click="changeConvert('txt')" :class="outputType === 'txt' && 'is-active'">{{ t('parsing.text') }}</button>
        </div>
        <div class="detail-output__actions">
          <button type="button" class="detail-export" :disabled="!jsonResult" @click="downloadFile">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 2.5v7M5.25 7.25 8 10l2.75-2.75" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 12.5h10" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
            {{ t('parsing.export') }}
          </button>
          <button type="button" class="detail-icon-btn" aria-label="Adjust" @click="configDialogVisible = true">
            <svg viewBox="0 0 11.6667 11.6667" fill="none" aria-hidden="true"><path d="M3.5 1.16667C3.17783 1.16667 2.91667 1.42783 2.91667 1.75C2.91667 2.07217 3.17783 2.33333 3.5 2.33333C3.82217 2.33333 4.08333 2.07217 4.08333 1.75C4.08333 1.42783 3.82217 1.16667 3.5 1.16667ZM1.84958 1.16667C2.08982 0.486971 2.73804 0 3.5 0C4.26196 0 4.91018 0.486971 5.15042 1.16667L11.6667 1.16667V2.33333L5.15042 2.33333C4.91018 3.01303 4.26196 3.5 3.5 3.5C2.73804 3.5 2.08982 3.01303 1.84958 2.33333L6.95387e-08 2.33333L1.04308e-07 1.16667L1.84958 1.16667ZM6.51624 5.25C6.75648 4.5703 7.40471 4.08333 8.16667 4.08333C8.92863 4.08333 9.57685 4.5703 9.81709 5.25H11.6667V6.41667H9.81709C9.57685 7.09636 8.92863 7.58333 8.16667 7.58333C7.40471 7.58333 6.75648 7.09636 6.51624 6.41667L1.04308e-07 6.41667L0 5.25L6.51624 5.25ZM8.16667 5.25C7.8445 5.25 7.58333 5.51117 7.58333 5.83333C7.58333 6.1555 7.8445 6.41667 8.16667 6.41667C8.48883 6.41667 8.75 6.1555 8.75 5.83333C8.75 5.51117 8.48883 5.25 8.16667 5.25ZM3.5 9.33333C3.17783 9.33333 2.91667 9.5945 2.91667 9.91667C2.91667 10.2388 3.17783 10.5 3.5 10.5C3.82217 10.5 4.08333 10.2388 4.08333 9.91667C4.08333 9.5945 3.82217 9.33333 3.5 9.33333ZM1.84958 9.33333C2.08982 8.65364 2.73804 8.16667 3.5 8.16667C4.26196 8.16667 4.91018 8.65364 5.15042 9.33333L11.6667 9.33333V10.5L5.15042 10.5C4.91018 11.1797 4.26196 11.6667 3.5 11.6667C2.73804 11.6667 2.08982 11.1797 1.84958 10.5H6.95387e-08V9.33333H1.84958Z" fill="currentColor"/></svg>
          </button>
        </div>
      </div>
      <div class="detail-output__divider"></div>
      <div v-if="outputType === 'md'" class="detail-output__category-tabs">
        <button
          v-for="tab in resultCategoryTabs"
          :key="tab.value"
          type="button"
          :class="activeResultCategory === tab.value && 'is-active'"
          @click="activeResultCategory = tab.value"
        >{{ t(tab.labelKey) }}</button>
      </div>
      <div v-if="resultEmptyState" class="detail-empty-result" :class="`is-${resultEmptyState.type}`">
        <div class="detail-empty-result__main">
          <div class="detail-empty-result__icon">
            <svg v-if="resultEmptyState.type === 'pending'" viewBox="0 0 32 32" fill="none" aria-hidden="true"><circle cx="16" cy="16" r="14" stroke="currentColor" stroke-width="1.5"/><path d="M16 8.5v8l5 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <svg v-else-if="resultEmptyState.type === 'processing'" viewBox="0 0 32 32" fill="none" aria-hidden="true"><path d="M9 5h10l4 4v18H9V5Z" stroke="currentColor" stroke-width="1.6"/><path d="M19 5v5h4M13 15h3M13 20h6M18 15h2v2h-2v-2Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <svg v-else viewBox="0 0 32 32" fill="none" aria-hidden="true"><circle cx="16" cy="16" r="14" stroke="currentColor" stroke-width="1.5"/><path d="M16 8.5v9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><circle cx="16" cy="22.5" r="1.4" fill="currentColor"/></svg>
          </div>
          <p>{{ resultEmptyState.message }}</p>
          <button v-if="resultEmptyState.type === 'failed'" type="button" class="detail-empty-result__retry" @click="startParsing">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M13 8a5 5 0 1 1-1.46-3.54M13 3.5V7h-3.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
            {{ t('parsing.retry') }}
          </button>
        </div>
      </div>
      <JsonViewer v-else-if="outputType === 'json' && jsonResult" :key="jsonViewerKey" boxed expanded :expandDepth="7" sort theme="dark" :value="displayJsonResult"/>
      <div v-show="!resultEmptyState && outputType === 'md'" class="detail-md-wrap">
        <!-- Loading -->
        <div v-show="loading" class="detail-state">
          <DemoLoading class="transform scale-70" />
        </div>
        <!-- 初始界面显示 -->
        <div v-show="init" class="detail-state">
          <img src="/images/idp/init.png" alt="init" width="240" height="150">
        </div>
        <template v-if="jsonResult && !init">
          <div ref="content" @scroll="handleScroll" v-show="outputType === 'md'" class="md-content md detail-md-content">
            <div v-if="!filteredResultDetails.length" class="detail-category-empty">{{ t('parsing.noCategoryContent') }}</div>
            <div class="my-4px" v-for="detail in filteredResultDetails" :key="detail.index" :data-page-id="detail.item.page_id" :data-result-index="detail.index">
              <template v-if="detail.item.type === 'table'">
                <div class="editable-table-block">
                  <div class="grid pr-12px cursor-pointer"
                    @click="handleTableClick(detail.index, detail.item, $event)"
                    @input="updateResultText(detail.index, $event, 'html')"
                    :id="detail.item.position.join('-')"
                    :data-table-index="detail.index"
                    :contenteditable="!isResultReadOnly"
                    spellcheck="false"
                    :class="active.join('-') === [(detail.item.position[0] / dpiScale), (detail.item.position[1] / dpiScale), (detail.item.position[2] / dpiScale), (detail.item.position[5] / dpiScale)].join('-') && 'text-[#396FFA] borderActive'" v-html="renderTableContent(detail.item.text)">
                  </div>
                  <template v-if="!isResultReadOnly && tableActionPosition.blockIndex === detail.index">
                    <button
                      type="button"
                      class="editable-table-cell-action"
                      :style="{ top: `${tableActionPosition.top}px`, left: `${tableActionPosition.left}px` }"
                      aria-label="Table actions"
                      @mousedown.prevent.stop
                      @click.stop="tableActionMenuVisible = !tableActionMenuVisible"
                    >
                      <svg viewBox="0 0 14 14" fill="none" aria-hidden="true">
                        <path d="M7 3.1v.1M7 7v.1M7 10.9v.1" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                      </svg>
                    </button>
                    <div
                      v-show="tableActionMenuVisible"
                      class="editable-table-action-menu"
                      :style="{ top: `${tableActionPosition.top + 24}px`, left: `${tableActionPosition.menuLeft}px` }"
                      @mousedown.prevent.stop
                      @click.stop
                    >
                      <button type="button" @click="runTableAction(detail.index, 'insert-row-above')">{{ t('parsing.tableActions.insertRowAbove') }}</button>
                      <button type="button" @click="runTableAction(detail.index, 'insert-row-below')">{{ t('parsing.tableActions.insertRowBelow') }}</button>
                      <button type="button" @click="runTableAction(detail.index, 'insert-column-above')">{{ t('parsing.tableActions.insertColumnAbove') }}</button>
                      <button type="button" @click="runTableAction(detail.index, 'insert-column-below')">{{ t('parsing.tableActions.insertColumnBelow') }}</button>
                      <button type="button" @click="runTableAction(detail.index, 'delete-row')">{{ t('parsing.tableActions.deleteRow') }}</button>
                      <button type="button" @click="runTableAction(detail.index, 'delete-column')">{{ t('parsing.tableActions.deleteColumn') }}</button>
                    </div>
                  </template>
                </div>
              </template>
              <template v-else-if="detail.item.type === 'image'">
                <img :src="getImageSrc(detail.item)" alt="image" :class="active.join('-') === [(detail.item.position[0] / dpiScale), (detail.item.position[1] / dpiScale), (detail.item.position[2] / dpiScale), (detail.item.position[5] / dpiScale)].join('-') && 'border-[#396FFA]'" @click="handleClick(detail.item.position, detail.item.page_id)" class="cursor-pointer border-1">
              </template>
              <template v-else>
                <div
                  class="cursor-pointer inline-flex"
                  @click="handleClick(detail.item.position, detail.item.page_id)"
                  :class="active.join('-') === [(detail.item.position[0] / dpiScale), (detail.item.position[1] / dpiScale), (detail.item.position[2] / dpiScale), (detail.item.position[5] / dpiScale)].join('-') && 'text-[#396FFA]'"
                  :id="detail.item.position.join('-')"
                >
                  <span class="mr-8px">•</span>
                  <div
                    class="editable-result-text"
                    :contenteditable="!isResultReadOnly"
                    spellcheck="false"
                    @input="updateResultText(detail.index, $event, 'text')"
                    v-html="renderedContent(detail.item.text)"
                  ></div>
                </div>
              </template>
            </div>
          </div>
        </template>
      </div>
      <div v-show="!resultEmptyState && outputType === 'txt'" class="detail-txt-content">
        {{ displayResultText }}
      </div>
      <div class="detail-bottom-bar">
        <div aria-hidden="true"></div>
        <div v-if="resultEmptyState" class="detail-confirm-actions">
          <span v-if="resultEmptyState.type !== 'failed'" class="detail-bottom-status">{{ t('parsing.pendingParsingShort') }}</span>
          <button v-else type="button" class="detail-confirm" @click="startParsing">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M13 8a5 5 0 1 1-1.46-3.54M13 3.5V7h-3.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
            {{ t('parsing.retry') }}
          </button>
        </div>
        <div v-else class="detail-confirm-actions">
          <button type="button" class="detail-reparse" @click="startParsing">{{ t('parsing.reParse') }}</button>
          <button type="button" class="detail-confirm" @click="confirmResult">{{ confirmActionText }}</button>
        </div>
      </div>
    </section>

    <LocalModeApiBanner :key="localBannerKey" class="parsing-result-detail__local-banner" />

    <div v-if="configDialogVisible" class="parsing-config-layer">
      <div class="parsing-config-dialog" role="dialog" aria-modal="true" aria-labelledby="parsing-config-title">
        <div class="parsing-config-dialog__header">
          <h2 id="parsing-config-title">{{ t('parsing.parsingConfiguration') }}</h2>
          <button type="button" class="parsing-config-dialog__close" aria-label="Close" @click="configDialogVisible = false">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M4.25 4.25 11.75 11.75M11.75 4.25 4.25 11.75" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
          </button>
        </div>
        <div class="parsing-config-dialog__content">
          <section class="parsing-config-section">
            <div class="parsing-config-section__head">
              <h3>{{ t('parsing.auxiliaryContentParsing') }}</h3>
              <p>{{ t('parsing.auxiliaryContentParsingDesc') }}</p>
            </div>
            <div class="parsing-config-grid">
              <button v-for="item in auxiliaryOptions" :key="item.key" type="button" class="parsing-config-item" @click="item.enabled = !item.enabled">
                <span>{{ t(item.labelKey) }}</span>
                <span class="figma-switch" :class="item.enabled && 'is-checked'"><i></i></span>
              </button>
            </div>
          </section>
          <section class="parsing-config-section">
            <div class="parsing-config-section__head">
              <h3>{{ t('parsing.modelParameters') }}</h3>
            </div>
            <div class="parsing-config-grid">
              <button v-for="item in modelOptions" :key="item.key" type="button" class="parsing-config-item" @click="item.enabled = !item.enabled">
                <span class="parsing-config-item__label">{{ t(item.labelKey) }}<em>?</em></span>
                <span class="figma-switch" :class="item.enabled && 'is-checked'"><i></i></span>
              </button>
            </div>
          </section>
        </div>
        <div class="parsing-config-dialog__footer">
          <button type="button" class="config-cancel" @click="configDialogVisible = false">{{ t('parsing.cancel') }}</button>
          <button type="button" class="config-reparse" @click="handleConfigReparse">{{ t('parsing.reParse') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import { ElMessage } from 'element-plus'
import 'vue3-json-viewer/dist/vue3-json-viewer.css'
import { JsonViewer } from 'vue3-json-viewer'
import { computed, ref, onMounted, nextTick, inject, onUnmounted, watch } from 'vue'
import ComPDFKitViewer from "../../assets/@compdfkit/webviewer"
import DemoLoading from "../DemoLoading.vue"
import JSZip from 'jszip'
import { get, post } from '../../utils/request'
import LocalModeApiBanner from '../LocalModeApiBanner.vue'
import { getEnv } from '../../utils/env'
import MarkdownIt from 'markdown-it'
import mk from 'markdown-it-katex'
import 'katex/dist/katex.min.css'
import jsPDF from 'jspdf'

const { t } = useI18n()
const init = ref(true)
const initTxt = ref(true)
const outputType = ref('md')
const loading = ref(false)
const localBannerKey = ref(0)
const isDownload = ref(false)
const jsonResult = ref()
const jsonViewerKey = ref(0)
const viewer = ref()
const resultText = ref('')
const selectedTableCell = ref({ blockIndex: -1, rowIndex: 0, cellIndex: 0 })
const tableActionPosition = ref({ blockIndex: -1, top: 0, left: 0, menuLeft: 0 })
const tableActionMenuVisible = ref(false)
const editUndoStack = ref<string[]>([])
const editRedoStack = ref<string[]>([])
const MAX_EDIT_HISTORY = 50
const confirmedJsonSnapshot = ref('')
const currentFile = ref<FileData | null>(null)
const detailList = ref<FileData[]>([])
const detailSearch = ref('')
const configDialogVisible = ref(false)
const auxiliaryOptions = ref([
  { key: 'number', labelKey: 'parsing.config.pageNumber', enabled: true },
  { key: 'footnote', labelKey: 'parsing.config.footnotes', enabled: true },
  { key: 'header', labelKey: 'parsing.config.header', enabled: true },
  { key: 'header_image', labelKey: 'parsing.config.headerImages', enabled: true },
  { key: 'footer', labelKey: 'parsing.config.footer', enabled: true },
  { key: 'footer_image', labelKey: 'parsing.config.footerImages', enabled: true },
  { key: 'aside_text', labelKey: 'parsing.config.marginalNotes', enabled: true }
])
const modelOptions = ref([
  { key: 'imageOrientationCorrection', labelKey: 'parsing.config.imageOrientationCorrection', enabled: true },
  { key: 'layoutAnalysis', labelKey: 'parsing.config.layoutAnalysis', enabled: false },
  { key: 'stampRecognition', labelKey: 'parsing.config.stampRecognition', enabled: true },
  { key: 'crossPageTableMerging', labelKey: 'parsing.config.crossPageTableMerging', enabled: true },
  { key: 'imageDistortionCorrection', labelKey: 'parsing.config.imageDistortionCorrection', enabled: true },
  { key: 'chartRecognition', labelKey: 'parsing.config.chartRecognition', enabled: true },
  { key: 'imageTextRecognition', labelKey: 'parsing.config.imageTextRecognition', enabled: true },
  { key: 'paragraphHeadingLevelRecognition', labelKey: 'parsing.config.paragraphHeadingLevelRecognition', enabled: true }
])
const detailFilterTabs = [
  { labelKey: 'parsing.all', value: 'all' },
  { labelKey: 'parsing.confirmed', value: 'confirmed' },
  { labelKey: 'parsing.unconfirmed', value: 'unconfirmed' }
] as const
type DetailFilter = typeof detailFilterTabs[number]['value']
const detailFilter = ref<DetailFilter>('all')
const resultCategoryTabs = [
  { labelKey: 'parsing.all', value: 'all' },
  { labelKey: 'parsing.categories.tables', value: 'tables' },
  { labelKey: 'parsing.categories.headings', value: 'headings' },
  { labelKey: 'parsing.categories.tableOfContents', value: 'toc' },
  { labelKey: 'parsing.categories.images', value: 'images' },
  { labelKey: 'parsing.categories.formulas', value: 'formulas' }
] as const
type ResultCategory = typeof resultCategoryTabs[number]['value']
const activeResultCategory = ref<ResultCategory>('all')
let UI = <any>null
const active = ref<number[]>([])
let docViewer = <any>null
const content = ref()

interface ZipFileData {
  name: string
  blob: Blob
  fullPath?: string
}

interface ExtractedFiles {
  json: any
  images: ZipFileData[],
  fileBlobs: { [key: string]: ZipFileData }
}

let resizeRedrawTimer: ReturnType<typeof setTimeout> | null = null
const TABLE_ACTION_MENU_WIDTH = 204

const fileName = ref('')

const changeActive = inject('changeActive') as (tab: 'list' | 'result') => void

const imageData = ref<ZipFileData[]>([])
const blobUrlCache = new Map<string, string>()
const dpiScale = ref(1)
const annotationDetails = ref<any[]>([])
const ANNOTATION_NAME_PREFIX = 'parse-anno-'
const md = new MarkdownIt('default', {
  html: true
})
md.use(mk)
let extractFiles = <any>null
const currentViewerPage = ref(1)
const isPageChangeFromMd = ref(false)
const suppressMdJumpOnAnnotationSelect = ref(false)
let mdClickLockTimer: ReturnType<typeof setTimeout> | null = null
const mdClickSyncLocked = ref(false)
let mdProgrammaticScrollTimer: ReturnType<typeof setTimeout> | null = null
const mdProgrammaticScrollLocked = ref(false)
const selectedAnnotationTarget = ref<{ page: number, position: number[] } | null>(null)

const startMdClickSyncLock = () => {
  mdClickSyncLocked.value = true
  if (mdClickLockTimer) {
    clearTimeout(mdClickLockTimer)
  }
  mdClickLockTimer = setTimeout(() => {
    mdClickSyncLocked.value = false
    mdClickLockTimer = null
  }, 300)
}


const renderNearPageAnnotations = (currentPage: number, isInitial = false) => {
  if (!docViewer || !annotationDetails.value.length) return

  const pageSet = isInitial
    ? new Set([1, 2, 3])
    : new Set([currentPage - 1, currentPage, currentPage + 1])

  const annotationManager = docViewer.getAnnotationManager()
  const annotations = annotationManager.getAnnotationsList()
  if (annotations.length) {
    annotationManager.deleteAnnotations(annotations)
  }
  annotationDetails.value.forEach((element: any, index: number) => {
    if (element.page_id < 1 || !pageSet.has(element.page_id)) return
    docViewer.addAnnotations({
      name: `${ANNOTATION_NAME_PREFIX}${element.page_id}-${index}`,
      type: 'custom',
      pageIndex: element.page_id - 1,
      borderWidth: 1,
      borderColor: '#396FFA',
      rect: {
        left: element.position[0] / dpiScale.value,
        top: element.position[1] / dpiScale.value,
        right: element.position[2] / dpiScale.value,
        bottom: element.position[5] / dpiScale.value
      },
      selectedStyle: {
        border: '2px solid #396FFA',
        background: '#396FFA14'
      }
    })
  })
  nextTick(() => {
    const target = selectedAnnotationTarget.value
    if (!target || !pageSet.has(target.page)) return
    selectAnnotationByPosition(target.position, target.page, false)
  })
}

const handleScroll = (e: Event) => {
  if (outputType.value !== 'md') return
  if (mdProgrammaticScrollLocked.value) return
  nextTick(() => {
    if (mdProgrammaticScrollLocked.value) return
    const target = e.target as HTMLDivElement
    const pageIdElements = target.querySelectorAll<HTMLElement>('[data-page-id]')
    if (!pageIdElements.length) return

    const containerTop = target.getBoundingClientRect().top
    const firstVisible = Array.from(pageIdElements).find((el) => {
      const rect = el.getBoundingClientRect()
      return rect.bottom >= containerTop + 8
    }) || pageIdElements[0]

    const currentPage = Number(firstVisible.dataset.pageId || 1)
    if (!currentPage || currentPage === currentViewerPage.value) return

    isPageChangeFromMd.value = true
    currentViewerPage.value = currentPage
    docViewer.pageNumberChanged(currentPage)
  })
}

const lockMdProgrammaticScroll = () => {
  mdProgrammaticScrollLocked.value = true
  if (mdProgrammaticScrollTimer) {
    clearTimeout(mdProgrammaticScrollTimer)
  }
  mdProgrammaticScrollTimer = setTimeout(() => {
    mdProgrammaticScrollLocked.value = false
    mdProgrammaticScrollTimer = null
  }, 350)
}

const findMdPageElement = (pageNumber: number) => {
  if (!content.value) return null
  const elements = Array.from(content.value.querySelectorAll<HTMLElement>('[data-page-id]'))
  if (!elements.length) return null

  const exact = elements.find(el => Number(el.dataset.pageId) === pageNumber)
  if (exact) return exact

  const pageElements = elements
    .map(el => ({ el, page: Number(el.dataset.pageId) }))
    .filter(item => Number.isFinite(item.page))
    .sort((a, b) => a.page - b.page)
  return pageElements.find(item => item.page > pageNumber)?.el
    || [...pageElements].reverse().find(item => item.page < pageNumber)?.el
    || null
}

const scrollMdToPage = (pageNumber: number) => {
  if (!content.value) return
  const targetEl = findMdPageElement(pageNumber)
  if (!targetEl) return
  lockMdProgrammaticScroll()
  content.value.scrollTo({
    top: Math.max(targetEl.offsetTop - 12, 0),
    behavior: 'auto'
  })
}

const findResultElementByAnnotationRect = (rect: { left: number, top: number, right: number, bottom: number }) => {
  if (!content.value) return null
  const targetRect = [rect.left, rect.top, rect.right, rect.bottom]
  const targetDetail = filteredResultDetails.value.find((detail) => {
    const position = detail.item?.position
    if (!Array.isArray(position)) return false
    return isSameAnnotationRect(
      {
        x1: position[0] / dpiScale.value,
        y1: position[1] / dpiScale.value,
        x2: position[2] / dpiScale.value,
        y2: position[5] / dpiScale.value
      },
      targetRect
    )
  })
  if (!targetDetail) return null
  return content.value.querySelector(`[data-result-index="${targetDetail.index}"]`) as HTMLElement | null
}

onUnmounted(() => {
  window.removeEventListener('keydown', handleEditHistoryShortcut)
  window.removeEventListener('click', hideTableActionMenu)
  if (resizeRedrawTimer) {
    clearTimeout(resizeRedrawTimer)
    resizeRedrawTimer = null
  }
  if (mdClickLockTimer) {
    clearTimeout(mdClickLockTimer)
    mdClickLockTimer = null
  }
  if (mdProgrammaticScrollTimer) {
    clearTimeout(mdProgrammaticScrollTimer)
    mdProgrammaticScrollTimer = null
  }
})

onMounted(async () => {
  window.addEventListener('keydown', handleEditHistoryShortcut)
  window.addEventListener('click', hideTableActionMenu)
  const license = getEnv('LICENSE_KEY') || 'Ki6UpWkucL6aKcocIWVc/f6fUYgKpAYSp1jNWm6aAaDr7ADonPnxyKmJSP86hxQgdB6bwzmTgbXe/NRg5JjmxeQKrjYOA6aQH/NUE0p/YfVny07PfmMU7SX6+AQxlTbk+of2WJbt6wf69JxpfjO9Aj2iTq3eR1Vu8+Ue99Z3b/GKSoMjgmjBaSN21lScTJ230yeyVZc0rjdt+QVuDpwBJZfSzpQbBL+/tbYRUhex05kFAtBRUT0d0mNKb4NCTLwr/oPY3u+fZQNI1OwCN8MaeD0ozqfq+itk+tx8s0a3MS3QCBX39TsNqcDi/a5Vt5H04GbID51WuEKkb799UN7SB68kD+Q9C95FZo3W7DLPF5Id3tVLjwj02FGNgeewpeIdNgRNpzdDAHO+UDvFjQ41jdGQ4tgb2bpMiMt/INJeLobLnkbPIwad7n6f7KhGyOTDrhxz9BO+lj2kqK576aB5pF+vmAl2+odMJncYhWcfj8JC5BpjcgCtCkhzbU9v11R07ByAbYqOaoeXnOVdXCbwJZG/RtoaHdnu7QRtPR1L8IZQuqtYbmLAOPf/MKcZJNqiQ8d9Wf3kFPKfscpfcawvNc3nKDL98eIvaPVl9IniKvGs7pTFLtnXIbTW88FCzyKw/aXqrQ6Uhea+RDLGmQJTIojMr4vkPz6c/9gm/RtO/NOyxDGwhy7sHiAcwhIkwl7Zg9s6QB8YBY20hAMGEzV0IZjg27eaqBaClfh1dpIXutHIupoN7O0iH2Jm0duAoYGnMkmhDaatl0gIsUdFyLyd7MnnfM1/PN7JeZhPr1ZPbK6tx9N6XiFMi2eRPwL5TAyN2MIz0ggkq1jjnGXYXa6rdSrdVSe/zA9bYRrB2comG+xb98yVV9hO9gRfyBAAGHFKlkUdj1g1SrbTNwHG164RIhBoP12s3knqc8f8GjpGGk7G5BqgnSydp+Hzc38kd13p'

  if (window.innerWidth > 930) {
    ComPDFKitViewer.init({
      license,
      pdfUrl: '',
      path: '/',
      enableDefaultFont: true,
      showToolbarControl: false
    }, viewer.value).then((core: any) => {
      docViewer = core.docViewer
      UI = core.UI
      docViewer.addEvent('annotationSelected', (data: any) => {
        active.value = [data.rect.left, data.rect.top, data.rect.right, data.rect.bottom]
        if (mdClickSyncLocked.value) return
        if (suppressMdJumpOnAnnotationSelect.value) {
          suppressMdJumpOnAnnotationSelect.value = false
          return
        }
        const dom = findResultElementByAnnotationRect(data.rect)
        if (dom && content.value) {
          const top = dom.offsetTop - (content.value.clientHeight / 2) + (dom.clientHeight / 2)
          lockMdProgrammaticScroll()
          content.value.scrollTo({
            top: Math.max(top, 0),
            behavior: 'auto'
          })
        }
      })

      docViewer.addEvent('scalechanging', () => {
        renderNearPageAnnotations(currentViewerPage.value)
      })

      const loadAnnotations = () => {
        addAnnotations(jsonResult.value)
        docViewer.removeEvent('documentloaded', loadAnnotations)
      }
      docViewer.addEvent('documentloaded', loadAnnotations)
      
      docViewer.addEvent('onPageNumberUpdated', (data: { pageNumber: number } | number) => {
        const pageNumber = typeof data === 'number' ? data : data?.pageNumber
        if (!pageNumber) return
        currentViewerPage.value = pageNumber
        renderNearPageAnnotations(pageNumber)
        if (mdClickSyncLocked.value) return
        if (outputType.value !== 'md') return
        if (isPageChangeFromMd.value) {
          isPageChangeFromMd.value = false
          return
        }
        nextTick(() => {
          scrollMdToPage(pageNumber)
        })
      })
    })
  }
})

interface FileData {
  fileId: string
  fileName: string
  pageCount: number
  uploadTime: string
  fileDownUrl: string
  resultDownUrl: string
  status?: number
}

const id = ref('')

const openFile = (val: FileData) => {
  localBannerKey.value += 1
  currentFile.value = val
  id.value = val.fileId
  loading.value = true
  loadDocument(val)
  fileName.value = val.fileName
  if (!detailList.value.some(item => item.fileId === val.fileId)) {
    detailList.value = [val, ...detailList.value]
  }
  getDetailList()
}

const getDetailList = async () => {
  try {
    const { data }: any = await get(`/api/idp/getFileList?page=1&pageSize=20&fileName=${detailSearch.value}&taskType=LAYOUT&startTime=&endTime=&status=`)
    const records = Array.isArray(data?.data?.records) ? data.data.records : []
    detailList.value = records.length ? records : currentFile.value ? [currentFile.value] : []
  } catch {
    if (currentFile.value && !detailList.value.length) detailList.value = [currentFile.value]
  }
}

const getFileExtension = (name?: string) => {
  if (!name) return ''
  const index = name.lastIndexOf('.')
  if (index <= 0 || index === name.length - 1) return ''
  return name.slice(index + 1).toUpperCase()
}

const reviewStatusValue = (row: any): Exclude<DetailFilter, 'all'> => {
  if (!isSuccessfulParsingStatus(row?.status)) return 'unconfirmed'
  const reviewStatus = row?.reviewStatus ?? row?.confirmStatus ?? row?.auditStatus ?? row?.review_status
  if (reviewStatus === 1 || reviewStatus === true || reviewStatus === 'confirmed' || reviewStatus === 'CONFIRMED') return 'confirmed'
  return 'unconfirmed'
}

const filteredDetailList = computed(() => {
  if (detailFilter.value === 'all') return detailList.value
  return detailList.value.filter(row => isSuccessfulParsingStatus(row?.status) && reviewStatusValue(row) === detailFilter.value)
})

const resultEmptyState = computed(() => {
  if (jsonResult.value || loading.value) return null
  const status = currentFile.value?.status
  if (status === 1) {
    return { type: 'processing', message: t('parsing.parsingInProgressTip') }
  }
  if (status === 3) {
    return { type: 'failed', message: t('parsing.parsingFailedTip') }
  }
  if (currentFile.value) {
    return { type: 'pending', message: t('parsing.pendingParsingTip') }
  }
  return null
})

const getResultDetails = () => {
  return Array.isArray(jsonResult.value?.result?.detail) ? jsonResult.value.result.detail : []
}

const normalizeCategoryText = (value: any) => String(value ?? '').toLowerCase().replace(/[\s-]+/g, '_')

const getResultItemMeta = (item: any) => {
  return [item?.type, item?.label, item?.category, item?.subType, item?.sub_type, item?.name, item?.tag]
    .map(normalizeCategoryText)
    .filter(Boolean)
}

const getResultItemTags = (item: any) => {
  const rawTags = item?.tags
  if (Array.isArray(rawTags)) {
    return rawTags.map(normalizeCategoryText).filter(Boolean)
  }
  if (rawTags && typeof rawTags === 'object') {
    return Object.values(rawTags).map(normalizeCategoryText).filter(Boolean)
  }
  if (rawTags !== undefined && rawTags !== null) {
    return [normalizeCategoryText(rawTags)].filter(Boolean)
  }
  return []
}

const isResultItemInCategory = (item: any, category: ResultCategory) => {
  if (category === 'all') return true

  const meta = getResultItemMeta(item)
  const tags = getResultItemTags(item)
  const type = normalizeCategoryText(item?.type)
  const text = normalizeCategoryText(item?.text)
  const isTableOfContents = meta.some(value => value.includes('toc') || value.includes('table_of_contents') || value.includes('contents') || value.includes('目录'))

  if (category === 'tables') return !isTableOfContents && meta.some(value => value === 'table' || value.includes('table'))
  if (category === 'headings') return meta.some(value => ['heading', 'title', 'section_title', 'section_header'].includes(value) || value.includes('heading')) || /^#+_/.test(text)
  if (category === 'toc') return isTableOfContents
  if (category === 'images') return meta.some(value => ['image', 'figure', 'picture'].includes(value) || value.includes('image'))
  if (category === 'formulas') return tags.includes('formula')

  return false
}

const filteredResultDetails = computed(() => {
  return getResultDetails()
    .map((item: any, index: number) => ({ item, index }))
    .filter(detail => isResultItemInCategory(detail.item, activeResultCategory.value))
})

watch([activeResultCategory, outputType], () => {
  if (outputType.value !== 'md') return
  nextTick(() => {
    scrollMdToPage(currentViewerPage.value)
  })
})

const displayJsonResult = computed(() => {
  if (!jsonResult.value || activeResultCategory.value === 'all') return jsonResult.value
  return {
    ...jsonResult.value,
    result: {
      ...jsonResult.value.result,
      detail: filteredResultDetails.value.map(detail => detail.item)
    }
  }
})

const displayResultText = computed(() => {
  if (activeResultCategory.value === 'all') return resultText.value
  return filteredResultDetails.value
    .map(detail => detail.item?.type === 'table' ? htmlToPlainText(detail.item?.text || '') : detail.item?.text || '')
    .filter(Boolean)
    .join('\n\n')
})

const statusText = (status?: number) => {
  if (status === 2) return t('parsing.completed')
  if (status === 1) return t('parsing.parsing')
  if (status === 3) return t('parsing.fail')
  return t('parsing.pending')
}

const statusTone = (status?: number) => {
  if (status === 2) return 'is-success'
  if (status === 1) return 'is-brand'
  if (status === 3) return 'is-error'
  return 'is-warning'
}

const isSuccessfulParsingStatus = (status?: number) => status === 2

const reviewStatusText = (row: any) => {
  if (!isSuccessfulParsingStatus(row?.status)) return '--'
  return reviewStatusValue(row) === 'confirmed' ? t('parsing.confirmed') : t('parsing.unconfirmed')
}

const isCurrentFileConfirmed = computed(() => reviewStatusValue(currentFile.value) === 'confirmed')
const isResultReadOnly = computed(() => isCurrentFileConfirmed.value)

const confirmActionText = computed(() => isCurrentFileConfirmed.value ? t('parsing.revokeConfirmed') : t('parsing.confirm'))

const reviewTone = (row: any) => {
  if (!isSuccessfulParsingStatus(row?.status)) return 'is-neutral'
  if (reviewStatusValue(row) === 'confirmed') return 'is-success'
  if (reviewStatusValue(row) === 'unconfirmed') return 'is-warning'
  return 'is-neutral'
}

const getModelOptionEnabled = (key: string) => {
  return !!modelOptions.value.find(item => item.key === key)?.enabled
}

const buildParsingConfig = () => {
  const ignoreLabels = auxiliaryOptions.value
    .filter(item => !item.enabled)
    .map(item => item.key)

  return {
    use_doc_unwarping: getModelOptionEnabled('imageDistortionCorrection'),
    use_chart_recognition: getModelOptionEnabled('chartRecognition'),
    use_seal_recognition: getModelOptionEnabled('stampRecognition'),
    use_ocr_for_image_block: getModelOptionEnabled('imageTextRecognition'),
    use_layout_detection: getModelOptionEnabled('layoutAnalysis'),
    layout_shape_mode: 'auto',
    merge_tables: getModelOptionEnabled('crossPageTableMerging'),
    relevel_titles: getModelOptionEnabled('paragraphHeadingLevelRecognition'),
    concatenate_pages: false,
    ignore_labels: ignoreLabels
  }
}

const handleConfigReparse = () => {
  configDialogVisible.value = false
  startParsing(buildParsingConfig())
}

// 轮询定时器
let pollTimer: ReturnType<typeof setTimeout> | null = null
const POLL_INTERVAL = 3000 // 轮询间隔 3 秒

// 轮询查询文件处理状态
const pollFileStatus = async (fileIds: string[], previousSuccessCount = 0) => {
  // 清除之前的轮询
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }

  // 没有需要轮询的文件则停止
  if (!Array.isArray(fileIds) || fileIds.length === 0) return

  const queryString = fileIds.map(id => `fileIds=${id}`).join('&')

  try {
    const { data } = await get(`/api/idp/get-file-by-ids?${queryString}`)

    if (data.code === 200 && data.data) {
      const files = Array.isArray(data.data) ? data.data : [data.data]
      const currentSuccessCount = files.filter((file: any) => file.status === 2).length
      const failedFiles = files.filter((file: any) => file.status === 3)
      files.forEach((file: any) => {
        if (file?.fileId) updateFileInDetailList(file)
        if (file?.fileId === id.value) currentFile.value = { ...currentFile.value, ...file }
      })

      // 检查是否所有文件的 status 都是 2 或 3
      const allCompleted = files.every((file: any) => file.status === 2 || file.status === 3)

      if (allCompleted) {
        loading.value = false
        if (failedFiles.length) {
          ElMessage.error(t('parsing.parseFail'))
        } else {
          ElMessage.success(t('parsing.success'))
        }
      } else {
        // 继续轮询
        pollTimer = setTimeout(() => pollFileStatus(fileIds, currentSuccessCount), POLL_INTERVAL)
      }
    }
  } catch {
    // 请求失败时继续轮询
    pollTimer = setTimeout(() => pollFileStatus(fileIds, previousSuccessCount), POLL_INTERVAL)
  }
}

const startParsing = async (parsingConfig?: Record<string, any>) => {
  loading.value = true

  try {
    const formData = new FormData()
    formData.append('idpFileIds', id.value)
    formData.append('type', 'LAYOUT')
    if (parsingConfig) {
      Object.entries(parsingConfig).forEach(([key, value]) => {
        formData.append(key, Array.isArray(value) ? JSON.stringify(value) : String(value))
      })
    }

    const { data } = await post('/api/idp/files-start', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
    if (data.code === 200 && data.message === 'success') {
      // 开始轮询查询文件处理状态
      pollFileStatus([id.value])
    }
  } catch {
    ElMessage.error(t('parsing.fail'))
  }
}

const urlToFile = async (url: string, fileName: string, type: string): Promise<File> => {
  const res = await fetch(url)
  const blob = await res.blob()
  return new File([blob], fileName, { type: 'application/' + type })
}

const loadDocument = async (val: FileData) => {
  deselectClick()
  resetEditHistory()
  currentViewerPage.value = 1
  isPageChangeFromMd.value = false
  mdProgrammaticScrollLocked.value = false
  selectedAnnotationTarget.value = null
  annotationDetails.value = []
  jsonResult.value = undefined
  confirmedJsonSnapshot.value = ''
  resultText.value = ''
  imageData.value = []
  const postFile = await urlToFile(val.fileDownUrl, val.fileName, 'pdf')
  UI.loadDocument(postFile)
  init.value = false
  loading.value = false
  if (!val.resultDownUrl || val.status !== 2) return
  try {
    const response = await fetch(val.resultDownUrl)
    clearBlobUrls()
    const result = await extractFilesFromZip(response)
    extractFiles = result
    // 读取 txt 文件内容
    if (result.fileBlobs.txt) {
      const txtContent = await result.fileBlobs.txt.blob.text()
      resultText.value = txtContent
    }
    jsonResult.value = result.json
    confirmedJsonSnapshot.value = getJsonSnapshot()
    resetEditHistory()
    imageData.value = result.images
    dpiScale.value = result.json.metrics[0].dpi / 72
    addAnnotations(result.json)
    loading.value = false
  } catch (error) {
    ElMessage.error(t('parsing.convertFailed'))
    console.error(error)
  }
}

interface annType {
  pageIndex: number
  name: string
  getRect: () => {
    x1: number,
    y1: number,
    x2: number,
    y2: number,
  }
}


const getAnnotationRectFromPosition = (position: number[]) => [
  position[0] / dpiScale.value,
  position[1] / dpiScale.value,
  position[2] / dpiScale.value,
  position[5] / dpiScale.value
]

const isSameAnnotationRect = (rect: { x1: number, y1: number, x2: number, y2: number }, targetRect: number[]) => {
  const tolerance = 0.01
  return Math.abs(rect.x1 - targetRect[0]) <= tolerance
    && Math.abs(rect.y1 - targetRect[1]) <= tolerance
    && Math.abs(rect.x2 - targetRect[2]) <= tolerance
    && Math.abs(rect.y2 - targetRect[3]) <= tolerance
}

const findAnnotationByPosition = (position: number[], page: number) => {
  if (!docViewer) return null
  const targetRect = getAnnotationRectFromPosition(position)
  const annotationManager = docViewer.getAnnotationManager()
  const annotations = annotationManager.getAnnotationsList()
  return annotations.find((ele: annType) => {
    return ele.pageIndex === Number(page - 1) && isSameAnnotationRect(ele.getRect(), targetRect)
  }) || null
}

const selectAnnotationByPosition = (position: number[], page: number, shouldRender = true) => {
  if (!docViewer) return false
  if (shouldRender) {
    renderNearPageAnnotations(page)
  }
  const annotationManager = docViewer.getAnnotationManager()
  const annotation = findAnnotationByPosition(position, page)

  if (!annotation) return false
  suppressMdJumpOnAnnotationSelect.value = false
  annotationManager.selectAnnotation(annotation)
  if (docViewer.annotationStore) {
    docViewer.annotationStore.selectedElementName = annotation.name
  }
  return true
}

const scheduleSelectAnnotationByPosition = (position: number[], page: number) => {
  nextTick(() => {
    selectAnnotationByPosition(position, page, false)
  })
  window.requestAnimationFrame(() => {
    selectAnnotationByPosition(position, page, false)
  })
  window.setTimeout(() => {
    selectAnnotationByPosition(position, page, false)
  }, 120)
}

// 点击文本
const handleClick = (val: number[], page: number) => {
  if (!docViewer) return
  startMdClickSyncLock()
  selectedAnnotationTarget.value = { page, position: [...val] }
  active.value = getAnnotationRectFromPosition(val)
  if (currentViewerPage.value !== page) {
    isPageChangeFromMd.value = true
    currentViewerPage.value = page
    docViewer.pageNumberChanged(page)
  }
  selectAnnotationByPosition(val, page)
  scheduleSelectAnnotationByPosition(val, page)
}

// 取消选中注释
const deselectClick = () => {
  if (!docViewer) return
  const annotationManager = docViewer.getAnnotationManager()
  const selectedName = docViewer.annotationStore.selectedElementName
  if (!selectedName) return
  const selectedAnnotation = annotationManager.getAnnotationById(selectedName)
  if (selectedAnnotation?.handleOutside) {
    selectedAnnotation.handleOutside()
    return
  }
  const annotations = annotationManager.getAnnotationsList()
  for (const ele of annotations) {
    if (selectedName && ele.name === selectedName) {
      const annotation = annotationManager.getAnnotationById(ele.name)
      annotation.handleOutside && annotation.handleOutside()
      break
    }
  }
  docViewer.annotationStore.selectedElementName = null
}

// 显示切换
const changeConvert = (val: string) => {
  if (outputType.value === val) return
  outputType.value = val
  if (initTxt.value) return
}

// 解压缩包获取结果
const extractFilesFromZip = async (response: Response): Promise<ExtractedFiles> => {
  const result: ExtractedFiles = {
    json: null,
    images: [],
    fileBlobs: {}
  }
  
  try {
    const zipBuffer = await response.arrayBuffer()
    const zip = new JSZip()
    await zip.loadAsync(zipBuffer)

    const jsonZip = new JSZip()
    const mdZip = new JSZip()

    let jsonZipName = ''
    let mdZipName = ''

    for (const [fullPath, fileEntry] of Object.entries(zip.files)) {
      if (fileEntry.dir) continue

      const fileName = fullPath.split('/').pop()!
      const extension = fileName.toLowerCase().split('.').pop() || ''

      if (fileName.toLowerCase().endsWith('.json')) {
        result.json = JSON.parse(await fileEntry.async('text'))

        jsonZip.file(fullPath, await fileEntry.async('blob'))
        jsonZipName = fileName.replace('.json', '.zip')
      }
      else if (/\.(png|jpg|jpeg)$/i.test(fileName)) {
        const blob = await fileEntry.async('blob')
        result.images.push({
          fullPath: fullPath,
          name: fileName,
          blob
        })

        const blobBuffer = await blob.arrayBuffer()
        jsonZip.file(fullPath, blobBuffer)
        mdZip.file(fullPath, blobBuffer)
      }
      else if (fileName.toLowerCase().endsWith('.md')) {
        mdZip.file(fullPath, await fileEntry.async('blob'))
        mdZipName = fileName.replace('.md', '.zip')
      }

      if (['txt'].includes(extension)) {
        const blob = await fileEntry.async('blob')
        result.fileBlobs[extension] = { name: fileName, blob }
      }
    }

    // 生成压缩包blob
    const generateZipBlob = async (zipInstance: JSZip, name: string) => {
      if (Object.keys(zipInstance.files).length > 0) {
        const blob = await zipInstance.generateAsync({ type: 'blob' })
        return { name, blob }
      }
      return null
    }

    // 生成各种类型的压缩包
    const jsonZipData = await generateZipBlob(jsonZip, jsonZipName)
    const mdZipData = await generateZipBlob(mdZip, mdZipName)

    if (jsonZipData) result.fileBlobs.json = jsonZipData
    if (mdZipData) result.fileBlobs.md = mdZipData

    return result
  } catch (error) {
    console.error('ZIP处理错误:', error)
    throw error
  }
}

// 渲染解析块
const addAnnotations = (json: any) => {
  annotationDetails.value = Array.isArray(json?.result?.detail) ? json.result.detail : []
  renderNearPageAnnotations(1, true)
}

const getImageSrc = (item: any) => {
  const targetImg = imageData.value.find((img: ZipFileData) => img.fullPath === item.image_url)
  let imageUrl = ''
  if (targetImg && targetImg.blob) {
    imageUrl = getBlobUrl(targetImg.fullPath, targetImg.blob)
  }
  return imageUrl
}

const resolveImageSrcByPath = (src: string) => {
  if (!src || /^https?:\/\//i.test(src) || src.startsWith('blob:') || src.startsWith('data:')) return src

  const normalized = src.replace(/^\.\//, '').replace(/^\//, '')
  const targetImg = imageData.value.find((img: ZipFileData) => {
    const fullPath = (img.fullPath || '').replace(/^\//, '')
    return fullPath === normalized || fullPath.endsWith(`/${normalized}`) || img.name === normalized
  })

  if (!targetImg?.blob) return src
  return getBlobUrl(targetImg.fullPath || targetImg.name, targetImg.blob)
}

const renderTableContent = (rawHtml: string) => {
  if (!rawHtml) return ''
  const container = document.createElement('div')
  container.innerHTML = rawHtml
  const images = container.querySelectorAll('img')
  images.forEach((img) => {
    const originalSrc = img.getAttribute('src') || ''
    const resolvedSrc = resolveImageSrcByPath(originalSrc)
    if (resolvedSrc) {
      img.setAttribute('src', resolvedSrc)
    }
  })
  return container.innerHTML
}

/** 创建 Blob URL 并做缓存 */
const getBlobUrl = (fullPath: string = '', blob: Blob) => {
  if (!blobUrlCache.has(fullPath)) {
    blobUrlCache.set(fullPath, URL.createObjectURL(blob))
  }
  return blobUrlCache.get(fullPath)!
}

/** 释放旧缓存 */
const clearBlobUrls = () => {
  for (const url of blobUrlCache.values()) {
    URL.revokeObjectURL(url)
  }
  blobUrlCache.clear()
}

// 根据当前 Tab 格式直接下载文件
const downloadFile = () => {
  isDownload.value = true
  const file = extractFiles.fileBlobs[outputType.value as keyof typeof extractFiles.fileBlobs]
  const blobUrl = getBlobUrl(file.name, file.blob)
  downloadClick(blobUrl, file.name)
}

// 下载文件
const downloadClick = (blobUrl: string, filename: string) => {
  const a = document.createElement('a')
  if (!a.click) {
    throw new Error('DownloadManager: "a.click()" is not supported.')
  }
  a.href = blobUrl
  a.target = '_parent'
  if ('download' in a) {
    a.download = filename
  }
  (document.body || document.documentElement).appendChild(a)
  a.click()
  a.remove()
}

const renderedContent = (val: string) => {
  return md.render(normalizeMarkdownFormula(val || ''))
}

const normalizeMarkdownFormula = (value: string) => {
  return value
    .replace(/\$\$\s*([\s\S]*?)\s*\$\$/g, (_, formula) => `$$${String(formula).trim()}$$`)
    .replace(/(^|[^$])\$\s+([^$\n]+?)\s+\$/g, (_, prefix, formula) => `${prefix}$${String(formula).trim()}$`)
}

const getEditableCaretOffset = (root: HTMLElement) => {
  const selection = window.getSelection()
  if (!selection?.rangeCount) return null
  const range = selection.getRangeAt(0)
  if (!root.contains(range.startContainer)) return null
  const preRange = range.cloneRange()
  preRange.selectNodeContents(root)
  preRange.setEnd(range.startContainer, range.startOffset)
  return preRange.toString().length
}

const restoreEditableCaretOffset = (root: HTMLElement, offset: number) => {
  const range = document.createRange()
  const selection = window.getSelection()
  let currentOffset = offset
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT)
  let node = walker.nextNode()

  while (node) {
    const textLength = node.textContent?.length || 0
    if (currentOffset <= textLength) {
      range.setStart(node, currentOffset)
      range.collapse(true)
      selection?.removeAllRanges()
      selection?.addRange(range)
      return
    }
    currentOffset -= textLength
    node = walker.nextNode()
  }

  range.selectNodeContents(root)
  range.collapse(false)
  selection?.removeAllRanges()
  selection?.addRange(range)
}

const htmlToPlainText = (html: string) => {
  const container = document.createElement('div')
  container.innerHTML = html || ''
  return container.innerText.trim()
}

const syncTextResultFromDetail = () => {
  const detail = jsonResult.value?.result?.detail
  if (!Array.isArray(detail)) return
  resultText.value = detail
    .map((item: any) => item?.type === 'table' ? htmlToPlainText(item.text) : `${item?.text || ''}`.trim())
    .filter(Boolean)
    .join('\n')
}

const refreshEditableResults = () => {
  jsonViewerKey.value += 1
  syncTextResultFromDetail()
}

const getJsonSnapshot = () => {
  return jsonResult.value ? JSON.stringify(jsonResult.value) : ''
}

const hasResultEdits = () => {
  return !!jsonResult.value && getJsonSnapshot() !== confirmedJsonSnapshot.value
}

const applyJsonSnapshot = (snapshot: string) => {
  if (!snapshot) return
  jsonResult.value = JSON.parse(snapshot)
  annotationDetails.value = Array.isArray(jsonResult.value?.result?.detail) ? jsonResult.value.result.detail : []
  refreshEditableResults()
}

const resetEditHistory = () => {
  editUndoStack.value = []
  editRedoStack.value = []
}

const updateFileInDetailList = (fileInfo: FileData) => {
  const index = detailList.value.findIndex(item => item.fileId === fileInfo.fileId)
  if (index >= 0) {
    detailList.value.splice(index, 1, { ...detailList.value[index], ...fileInfo })
  } else {
    detailList.value = [fileInfo, ...detailList.value]
  }
}

const refreshCurrentFileInfo = async () => {
  if (!id.value) return null
  const { data } = await get(`/api/idp/get-file-by-id?fileId=${id.value}`)
  if (Number(data.code) !== 200 || !data.data) return null
  const fileInfo = data.data as FileData
  currentFile.value = fileInfo
  updateFileInDetailList(fileInfo)
  return fileInfo
}

const confirmResult = async () => {
  if (!id.value) return
  try {
    loading.value = true
    const formData = new FormData()
    formData.append('fileId', id.value)
    const isCancelConfirm = isCurrentFileConfirmed.value
    if (!isCancelConfirm && hasResultEdits()) {
      formData.append('newResult', getJsonSnapshot())
    }
    const { data } = await post(isCancelConfirm ? '/api/idp/cancel-confirm-file-result' : '/api/idp/confirm-file-result', formData)
    if (Number(data.code) !== 200) {
      ElMessage.error(data.message || (isCancelConfirm ? t('parsing.revokeConfirmedFailed') : t('parsing.confirmFailed')))
      return
    }
    ElMessage.success(isCancelConfirm ? t('parsing.revokeConfirmedSuccess') : t('parsing.confirmSuccess'))
    const fileInfo = await refreshCurrentFileInfo()
    if (fileInfo) {
      await loadDocument(fileInfo)
      currentFile.value = fileInfo
      updateFileInDetailList(fileInfo)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error(isCurrentFileConfirmed.value ? t('parsing.revokeConfirmedFailed') : t('parsing.confirmFailed'))
  } finally {
    loading.value = false
  }
}

const pushEditHistory = (snapshot: string) => {
  if (isResultReadOnly.value) return
  if (!snapshot) return
  if (editUndoStack.value[editUndoStack.value.length - 1] === snapshot) return
  editUndoStack.value.push(snapshot)
  if (editUndoStack.value.length > MAX_EDIT_HISTORY) {
    editUndoStack.value.shift()
  }
  editRedoStack.value = []
}

const pushUndoHistoryOnly = (snapshot: string) => {
  if (isResultReadOnly.value) return
  if (!snapshot) return
  editUndoStack.value.push(snapshot)
  if (editUndoStack.value.length > MAX_EDIT_HISTORY) {
    editUndoStack.value.shift()
  }
}

const pushRedoHistory = (snapshot: string) => {
  if (isResultReadOnly.value) return
  if (!snapshot) return
  editRedoStack.value.push(snapshot)
  if (editRedoStack.value.length > MAX_EDIT_HISTORY) {
    editRedoStack.value.shift()
  }
}

const undoEdit = () => {
  if (isResultReadOnly.value) return
  const previousSnapshot = editUndoStack.value.pop()
  if (!previousSnapshot) return
  const currentSnapshot = getJsonSnapshot()
  pushRedoHistory(currentSnapshot)
  applyJsonSnapshot(previousSnapshot)
}

const redoEdit = () => {
  if (isResultReadOnly.value) return
  const nextSnapshot = editRedoStack.value.pop()
  if (!nextSnapshot) return
  const currentSnapshot = getJsonSnapshot()
  pushUndoHistoryOnly(currentSnapshot)
  applyJsonSnapshot(nextSnapshot)
}

const handleEditHistoryShortcut = (event: KeyboardEvent) => {
  const isMac = /mac/i.test(navigator.platform)
  const modifierPressed = isMac ? event.metaKey : event.ctrlKey
  if (!modifierPressed) return
  if (isResultReadOnly.value) return
  const key = event.key.toLowerCase()
  const isUndo = key === 'z' && !event.shiftKey
  const isRedo = key === 'y' || (isMac && key === 'z' && event.shiftKey)
  if (!isUndo && !isRedo) return
  event.preventDefault()
  if (isUndo) undoEdit()
  else redoEdit()
}

const commitJsonResultDetail = (detail: any[], previousSnapshot = getJsonSnapshot()) => {
  if (isResultReadOnly.value) return
  const result = jsonResult.value?.result
  if (!result) return
  pushEditHistory(previousSnapshot)
  jsonResult.value = {
    ...jsonResult.value,
    result: {
      ...result,
      detail: [...detail]
    }
  }
  refreshEditableResults()
}

const createEditableTableCell = (tagName = 'td') => {
  const cell = document.createElement(tagName)
  cell.innerHTML = '<br>'
  cell.setAttribute('style', `${cell.getAttribute('style') || ''}; min-width: 50px;`)
  return cell
}

const getEditableTableCaretState = (index: number, event: Event) => {
  const selection = window.getSelection()
  if (!selection?.rangeCount) return null
  const range = selection.getRangeAt(0)
  const selectionElement = range.startContainer.nodeType === Node.TEXT_NODE
    ? range.startContainer.parentElement
    : range.startContainer as HTMLElement
  const eventElement = event.target as HTMLElement | null
  const cell = (selectionElement?.closest('td,th') || eventElement?.closest('td,th')) as HTMLTableCellElement | null
  const table = cell?.closest('table')
  if (!cell || !table) return null
  const gridCell = findTableGridCell(table, cell)
  const caretOffset = getEditableCaretOffset(cell)
  return {
    blockIndex: index,
    rowIndex: gridCell?.rowIndex ?? 0,
    cellIndex: gridCell?.colIndex ?? 0,
    caretOffset: caretOffset ?? 0
  }
}

const restoreEditableTableCaretState = (index: number, caretState: { rowIndex: number, cellIndex: number, caretOffset: number }) => {
  const container = content.value?.querySelector(`[data-table-index="${index}"]`) as HTMLElement | null
  const table = container?.querySelector('table') as HTMLTableElement | null
  if (!table) return
  const grid = buildTableGrid(table)
  const cell = grid[caretState.rowIndex]?.[caretState.cellIndex]?.cell
  if (!cell) return
  restoreEditableCaretOffset(cell, caretState.caretOffset)
}

const updateResultText = (index: number, event: Event, mode: 'text' | 'html') => {
  if (isResultReadOnly.value) return
  const detail = jsonResult.value?.result?.detail
  if (!Array.isArray(detail) || !detail[index]) return
  const target = event.currentTarget as HTMLElement
  const tableCaretState = mode === 'html' ? getEditableTableCaretState(index, event) : null
  const caretOffset = mode === 'text' ? getEditableCaretOffset(target) : null
  const previousSnapshot = getJsonSnapshot()
  selectEditableTableCell(index, event)
  detail[index].text = mode === 'html' ? target.innerHTML : target.innerText
  commitJsonResultDetail(detail, previousSnapshot)
  if (tableCaretState) {
    nextTick(() => restoreEditableTableCaretState(index, tableCaretState))
  } else if (caretOffset !== null) {
    nextTick(() => restoreEditableCaretOffset(target, caretOffset))
  }
}

const selectEditableTableCell = (index: number, event: Event) => {
  if (isResultReadOnly.value) return
  const target = event.target as HTMLElement | null
  const cell = target?.closest('td,th') as HTMLTableCellElement | null
  const row = cell?.parentElement as HTMLTableRowElement | null
  const table = row?.closest('table')
  if (!cell || !row || !table) return
  const gridCell = findTableGridCell(table, cell)
  selectedTableCell.value = {
    blockIndex: index,
    rowIndex: gridCell?.rowIndex ?? Array.from(table.rows).indexOf(row),
    cellIndex: gridCell?.colIndex ?? Array.from(row.cells).indexOf(cell)
  }
  updateTableActionPosition(index, cell)
}

const updateTableActionPosition = (index: number, cell: HTMLTableCellElement) => {
  if (isResultReadOnly.value) return
  const block = cell.closest('.editable-table-block') as HTMLElement | null
  if (!block) return
  const blockRect = block.getBoundingClientRect()
  const cellRect = cell.getBoundingClientRect()
  const contentRect = (content.value as HTMLElement | undefined)?.getBoundingClientRect()
  const actionLeft = Math.max(cellRect.right - blockRect.left - 22, 0)
  const visibleLeft = contentRect ? Math.max(contentRect.left - blockRect.left, 0) : 0
  const visibleRight = contentRect ? contentRect.right - blockRect.left : block.clientWidth
  const maxMenuLeft = Math.max(visibleRight - TABLE_ACTION_MENU_WIDTH - 8, visibleLeft)
  tableActionPosition.value = {
    blockIndex: index,
    top: Math.max(cellRect.top - blockRect.top + 2, 0),
    left: actionLeft,
    menuLeft: Math.max(Math.min(actionLeft, maxMenuLeft), visibleLeft)
  }
}

const hideTableActionMenu = () => {
  tableActionMenuVisible.value = false
}

type TableAction =
  | 'insert-row-above'
  | 'insert-row-below'
  | 'insert-column-above'
  | 'insert-column-below'
  | 'delete-row'
  | 'delete-column'

const runTableAction = (index: number, action: TableAction) => {
  if (isResultReadOnly.value) {
    tableActionMenuVisible.value = false
    return
  }
  if (action === 'insert-row-above') insertTableRow(index, 'above')
  if (action === 'insert-row-below') insertTableRow(index, 'below')
  if (action === 'insert-column-above') insertTableColumn(index, 'left')
  if (action === 'insert-column-below') insertTableColumn(index, 'right')
  if (action === 'delete-row') deleteTableRow(index)
  if (action === 'delete-column') deleteTableColumn(index)
  tableActionMenuVisible.value = false
}

const handleTableClick = (index: number, item: any, event: MouseEvent) => {
  if (!isResultReadOnly.value) {
    selectEditableTableCell(index, event)
  }
  handleClick(item.position, item.page_id)
}

const syncSelectedTableCellFromCaret = (index: number) => {
  if (isResultReadOnly.value) return
  const selection = window.getSelection()
  if (!selection?.rangeCount) return
  const container = content.value?.querySelector(`[data-table-index="${index}"]`) as HTMLElement | null
  if (!container) return
  const node = selection.getRangeAt(0).startContainer
  const element = node.nodeType === Node.TEXT_NODE ? node.parentElement : node as HTMLElement
  if (!element || !container.contains(element)) return
  const cell = element.closest('td,th') as HTMLTableCellElement | null
  const row = cell?.parentElement as HTMLTableRowElement | null
  const table = row?.closest('table')
  if (!cell || !row || !table) return
  const gridCell = findTableGridCell(table, cell)
  selectedTableCell.value = {
    blockIndex: index,
    rowIndex: gridCell?.rowIndex ?? Array.from(table.rows).indexOf(row),
    cellIndex: gridCell?.colIndex ?? Array.from(row.cells).indexOf(cell)
  }
}

const getEditableTableContext = (index: number) => {
  const detail = jsonResult.value?.result?.detail
  if (!Array.isArray(detail) || !detail[index]) return null
  const container = document.createElement('div')
  container.innerHTML = detail[index].text || '<table><tbody><tr><td><br></td></tr></tbody></table>'
  let table = container.querySelector('table')
  if (!table) {
    table = document.createElement('table')
    const tbody = document.createElement('tbody')
    const row = document.createElement('tr')
    row.appendChild(document.createElement('td'))
    tbody.appendChild(row)
    table.appendChild(tbody)
    container.innerHTML = ''
    container.appendChild(table)
  }
  return { detail, container, table }
}

interface TableGridCell {
  cell: HTMLTableCellElement
  row: HTMLTableRowElement
  rowIndex: number
  physicalCellIndex: number
  colIndex: number
  rowSpan: number
  colSpan: number
  isAnchor: boolean
}

const buildTableGrid = (table: HTMLTableElement) => {
  const grid: TableGridCell[][] = []
  Array.from(table.rows).forEach((row, rowIndex) => {
    grid[rowIndex] ||= []
    let colIndex = 0
    Array.from(row.cells).forEach((cell, physicalCellIndex) => {
      while (grid[rowIndex][colIndex]) colIndex += 1
      const rowSpan = Math.max(cell.rowSpan || 1, 1)
      const colSpan = Math.max(cell.colSpan || 1, 1)
      for (let rowOffset = 0; rowOffset < rowSpan; rowOffset++) {
        const targetRowIndex = rowIndex + rowOffset
        grid[targetRowIndex] ||= []
        for (let colOffset = 0; colOffset < colSpan; colOffset++) {
          grid[targetRowIndex][colIndex + colOffset] = {
            cell,
            row,
            rowIndex,
            physicalCellIndex,
            colIndex,
            rowSpan,
            colSpan,
            isAnchor: rowOffset === 0 && colOffset === 0
          }
        }
      }
      colIndex += colSpan
    })
  })
  return grid
}

const findTableGridCell = (table: HTMLTableElement, cell: HTMLTableCellElement) => {
  return buildTableGrid(table).flat().find(gridCell => gridCell?.cell === cell && gridCell.isAnchor)
}

const getTableColumnCount = (grid: TableGridCell[][]) => {
  return Math.max(...grid.map(row => row.length), 1)
}

const getSelectedTableCell = (index: number, table: HTMLTableElement) => {
  const selection = selectedTableCell.value.blockIndex === index
    ? selectedTableCell.value
    : { blockIndex: index, rowIndex: 0, cellIndex: 0 }
  const rows = Array.from(table.rows)
  const grid = buildTableGrid(table)
  const rowIndex = Math.min(Math.max(selection.rowIndex, 0), Math.max(rows.length - 1, 0))
  const row = rows[rowIndex]
  const cellIndex = Math.min(Math.max(selection.cellIndex, 0), Math.max(getTableColumnCount(grid) - 1, 0))
  return { rows, row, rowIndex, cellIndex, grid }
}

const commitEditableTable = (index: number, context: NonNullable<ReturnType<typeof getEditableTableContext>>) => {
  const previousSnapshot = getJsonSnapshot()
  context.detail[index].text = context.container.innerHTML
  commitJsonResultDetail(context.detail, previousSnapshot)
}

const insertTableRow = (index: number, direction: 'above' | 'below') => {
  if (isResultReadOnly.value) return
  syncSelectedTableCellFromCaret(index)
  const context = getEditableTableContext(index)
  if (!context) return
  const { rows, row: selectedRow, rowIndex, grid } = getSelectedTableCell(index, context.table)
  const insertAt = direction === 'above' ? rowIndex : rowIndex + 1
  const columnCount = getTableColumnCount(grid)
  const coveredColumns = new Set<number>()

  grid.flat().forEach((gridCell) => {
    if (!gridCell?.isAnchor) return
    const rowStart = gridCell.rowIndex
    const rowEnd = gridCell.rowIndex + gridCell.rowSpan
    if (rowStart < insertAt && insertAt < rowEnd) {
      gridCell.cell.rowSpan += 1
      for (let colOffset = 0; colOffset < gridCell.colSpan; colOffset++) {
        coveredColumns.add(gridCell.colIndex + colOffset)
      }
    }
  })

  const newRow = document.createElement('tr')
  for (let i = 0; i < columnCount; i++) {
    if (coveredColumns.has(i)) continue
    newRow.appendChild(createEditableTableCell())
  }
  const targetSection = selectedRow?.parentElement || context.table.tBodies[0] || context.table.createTBody()
  targetSection.insertBefore(newRow, rows[insertAt] || null)
  selectedTableCell.value = { blockIndex: index, rowIndex: direction === 'above' ? rowIndex : rowIndex + 1, cellIndex: 0 }
  commitEditableTable(index, context)
}

const insertTableColumn = (index: number, direction: 'left' | 'right') => {
  if (isResultReadOnly.value) return
  syncSelectedTableCellFromCaret(index)
  const context = getEditableTableContext(index)
  if (!context) return
  const { rows, cellIndex, grid } = getSelectedTableCell(index, context.table)
  const insertAt = direction === 'left' ? cellIndex : cellIndex + 1
  const skipRows = new Set<number>()

  grid.flat().forEach((gridCell) => {
    if (!gridCell?.isAnchor) return
    const colStart = gridCell.colIndex
    const colEnd = gridCell.colIndex + gridCell.colSpan
    if (colStart < insertAt && insertAt < colEnd) {
      gridCell.cell.colSpan += 1
      for (let rowOffset = 0; rowOffset < gridCell.rowSpan; rowOffset++) {
        skipRows.add(gridCell.rowIndex + rowOffset)
      }
    }
  })

  rows.forEach((row, rowIndex) => {
    if (skipRows.has(rowIndex)) return
    const cell = createEditableTableCell(row.parentElement?.tagName === 'THEAD' ? 'th' : 'td')
    const rowGrid = buildTableGrid(context.table)[rowIndex] || []
    const nextCell = rowGrid.find(gridCell => gridCell?.isAnchor && gridCell.colIndex >= insertAt)?.cell || null
    row.insertBefore(cell, nextCell)
  })
  selectedTableCell.value = { blockIndex: index, rowIndex: selectedTableCell.value.rowIndex, cellIndex: direction === 'left' ? cellIndex : cellIndex + 1 }
  commitEditableTable(index, context)
}

const deleteTableRow = (index: number) => {
  if (isResultReadOnly.value) return
  syncSelectedTableCellFromCaret(index)
  const context = getEditableTableContext(index)
  if (!context) return
  const { rows, rowIndex, grid } = getSelectedTableCell(index, context.table)
  if (rows.length <= 1) {
    Array.from(rows[0]?.cells || []).forEach(cell => { cell.innerHTML = '<br>' })
  } else {
    grid.flat().forEach((gridCell) => {
      if (!gridCell?.isAnchor) return
      const rowStart = gridCell.rowIndex
      const rowEnd = gridCell.rowIndex + gridCell.rowSpan
      if (rowStart === rowIndex && gridCell.rowSpan > 1) {
        const nextRow = rows[rowIndex + 1]
        const nextRowGrid = grid[rowIndex + 1] || []
        const nextCell = nextRowGrid.find(cell => cell?.isAnchor && cell.colIndex > gridCell.colIndex)?.cell || null
        gridCell.cell.rowSpan = Math.max(gridCell.cell.rowSpan - 1, 1)
        nextRow?.insertBefore(gridCell.cell, nextCell)
        return
      }
      if (rowStart < rowIndex && rowIndex < rowEnd) {
        gridCell.cell.rowSpan = Math.max(gridCell.cell.rowSpan - 1, 1)
      }
    })
    rows[rowIndex].remove()
  }
  selectedTableCell.value = { blockIndex: index, rowIndex: Math.max(rowIndex - 1, 0), cellIndex: selectedTableCell.value.cellIndex }
  commitEditableTable(index, context)
}

const deleteTableColumn = (index: number) => {
  if (isResultReadOnly.value) return
  syncSelectedTableCellFromCaret(index)
  const context = getEditableTableContext(index)
  if (!context) return
  const { rows, cellIndex, grid } = getSelectedTableCell(index, context.table)
  const maxColumnCount = getTableColumnCount(grid)
  const removedCells = new Set<HTMLTableCellElement>()
  rows.forEach((row, rowIndex) => {
    if (maxColumnCount <= 1) {
      Array.from(row.cells).forEach(cell => { cell.innerHTML = '<br>' })
      return
    }
    const gridCell = grid[rowIndex]?.[cellIndex]
    if (!gridCell) return
    const colStart = gridCell.colIndex
    const colEnd = gridCell.colIndex + gridCell.colSpan
    if (gridCell.colSpan > 1 && colStart <= cellIndex && cellIndex < colEnd) {
      gridCell.cell.colSpan = Math.max(gridCell.cell.colSpan - 1, 1)
      return
    }
    if (removedCells.has(gridCell.cell)) return
    gridCell.cell.remove()
    removedCells.add(gridCell.cell)
  })
  selectedTableCell.value = { blockIndex: index, rowIndex: selectedTableCell.value.rowIndex, cellIndex: Math.max(cellIndex - 1, 0) }
  commitEditableTable(index, context)
}

const imageToPDF = (file: File) => {
  return new Promise<File>((resolve, reject) => {
    const img = new Image()
    const imgUrl = URL.createObjectURL(file)
    
    img.onload = function() {
      try {
        // 计算图片像素面积
        const pixelArea = img.width * img.height
        const maxPixelArea = 4194304 // 最大像素面积

        let finalWidth = img.width
        let finalHeight = img.height

        // 如果像素面积超过限制，计算缩放因子
        if (pixelArea > maxPixelArea) {
          // 计算缩放因子：sqrt(当前面积 / 最大面积)
          const scaleFactor = Math.sqrt(pixelArea / maxPixelArea)

          // 缩放长和宽
          const scaledWidth = Math.round(img.width / scaleFactor)
          const scaledHeight = Math.round(img.height / scaleFactor)

          // 调整为28的倍数
          finalWidth = Math.round(scaledWidth / 28) * 28
          finalHeight = Math.round(scaledHeight / 28) * 28

          // 确保调整后的尺寸至少为28的倍数的最小值
          finalWidth = Math.max(finalWidth, 28)
          finalHeight = Math.max(finalHeight, 28)
        }

        // 计算PDF页面尺寸（mm）
        const pdfWidthMM = finalWidth * 0.264583 // px转mm (96dpi)
        const pdfHeightMM = finalHeight * 0.264583

        // 使用自定义设置创建PDF
        const pdf = new jsPDF({
          orientation: img.width > img.height ? 'landscape' : 'portrait',
          unit: 'mm',
          format: [pdfWidthMM, pdfHeightMM] // 使用调整后的尺寸
        })

        const pageWidth = pdf.internal.pageSize.getWidth()
        const pageHeight = pdf.internal.pageSize.getHeight()

        // 计算图片在PDF中的尺寸（转换为mm）
        const imgWidthMM = finalWidth * 0.264583 // px转mm (96dpi)
        const imgHeightMM = finalHeight * 0.264583

        // 如果图片比页面大，按比例缩放（理论上不会发生，但保留安全机制）
        let displayWidth = imgWidthMM
        let displayHeight = imgHeightMM

        if (imgWidthMM > pageWidth || imgHeightMM > pageHeight) {
          const widthRatio = pageWidth / imgWidthMM
          const heightRatio = pageHeight / imgHeightMM
          const ratio = Math.min(widthRatio, heightRatio, 1)
          displayWidth = imgWidthMM * ratio
          displayHeight = imgHeightMM * ratio
        }

        const x = (pageWidth - displayWidth) / 2
        const y = (pageHeight - displayHeight) / 2

        // 创建Canvas用于高质量渲染
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')!

        // 设置Canvas尺寸为最终尺寸
        canvas.width = finalWidth
        canvas.height = finalHeight

        // 高质量绘制（如果尺寸有变化，会进行缩放）
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, finalWidth, finalHeight)

        // 获取高质量图片数据
        const nameArray = file.name.split('.')
        const imageType = nameArray.pop()?.toUpperCase() || 'JPEG'
        const dataUrl = canvas.toDataURL(`image/${imageType.toLowerCase()}`, 1.0)

        // 添加图片到PDF
        pdf.addImage({
          imageData: dataUrl,
          x: x,
          y: y,
          width: displayWidth,
          height: displayHeight,
          compression: 'MEDIUM', // 可以尝试 'NONE', 'FAST', 'MEDIUM', 'SLOW'
          rotation: 0,
          alias: 'alias'
        })

        URL.revokeObjectURL(imgUrl)

        // 生成PDF文件
        const pdfBlob = pdf.output('blob')
        const pdfFile = new File([pdfBlob], nameArray[0] + '.pdf', { type: 'application/pdf' })

        resolve(pdfFile)
      } catch (error) {
        URL.revokeObjectURL(imgUrl)
        reject(error)
      }
    }

    img.onerror = () => {
      URL.revokeObjectURL(imgUrl)
      reject(new Error('图片加载失败'))
    }

    img.src = imgUrl
  })
}

defineExpose({
  openFile,
  imageToPDF
})
</script>
<style lang="scss" scoped>
.parsing-result-detail {
  height: calc(100vh - 56px);
  padding: 16px;
  display: grid;
  grid-template-columns: 230px minmax(0, 1.05fr) minmax(0, 1fr);
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 8px;
  overflow: hidden;
  background: #f5f7ff;
  color: #0c131f;
  font-family: 'Encode Sans', 'Microsoft YaHei', sans-serif;
}

.detail-sidebar {
  width: 230px;
  height: 100%;
  padding: 16px 12px;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 230px;
  grid-column: 1;
  grid-row: 1 / span 2;
}

.detail-sidebar__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.detail-sidebar__back {
  height: 32px;
  padding: 5px 0;
  border: 0;
  background: transparent;
  color: #0c131f;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
  }
}

.detail-sidebar__filter {
  width: 32px;
  height: 32px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  background: #fff;
  color: #0c131f;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }
}

.detail-sidebar__divider {
  height: 1px;
  background: #e7e8e8;
  flex: 0 0 auto;
}

.detail-sidebar__tabs {
  padding: 4px;
  border: 1px solid #d7e2fe;
  border-radius: 3px;
  background: #f5f7ff;
  display: flex;
  gap: 4px;
  flex: 0 0 auto;

  button {
    min-width: 0;
    flex: 0 0 auto;
    height: 24px;
    padding: 2px 8px;
    border: 0;
    border-radius: 3px;
    background: transparent;
    color: #0c131f;
    font-size: 12px;
    line-height: 20px;
    cursor: pointer;
    white-space: nowrap;

    &.is-active {
      flex: 1 1 0;
      background: #fff;
      color: #396ffa;
    }
  }
}

.detail-sidebar__search {
  height: 32px;
  padding: 4px 8px 4px 4px;
  border-radius: 3px;
  background: #f3f3f4;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(12, 19, 31, 0.4);
  flex: 0 0 auto;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
  }

  input {
    min-width: 0;
    flex: 1 1 auto;
    border: 0;
    outline: 0;
    background: transparent;
    color: #0c131f;
    font-size: 14px;
    line-height: 22px;

    &::placeholder {
      color: rgba(12, 19, 31, 0.4);
    }
  }
}

.detail-sidebar__list {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-file-item {
  width: 100%;
  padding: 4px 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #fff;
  color: rgba(12, 19, 31, 0.6);
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;

  &.is-selected {
    border-color: #d7e2fe;
    background: #f5f7ff;
    color: #396ffa;
  }
}

.detail-file-item__name,
.detail-preview__file {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
    color: #396ffa;
    flex: 0 0 auto;
  }

  span {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.detail-file-item__name {
  font-weight: 600;
}

.detail-file-item__desc,
.detail-file-item__tags {
  margin-left: 20px;
}

.detail-file-item__desc {
  width: 122px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  line-height: 20px;
}

.detail-file-item__tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-tag {
  display: inline-flex;
  align-items: center;
  padding: 0 4px;
  border-radius: 3px;
  font-size: 12px;
  line-height: 20px;

  &.is-success {
    color: #67d1a0;
    background: #ecf9f3;
  }

  &.is-brand {
    color: #396ffa;
    background: #f5f7ff;
  }

  &.is-error {
    color: #d44040;
    background: #fbecec;
  }

  &.is-warning {
    color: #f5a13a;
    background: #fef3e6;
  }

  &.is-neutral {
    color: #0c131f;
    background: #f3f3f4;
  }
}

.detail-preview,
.detail-output {
  height: 100%;
  min-width: 0;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.parsing-result-detail__local-banner {
  grid-column: 2 / span 2;
  grid-row: 2;
}

.detail-preview {
  flex: 1.05 1 0;
  grid-column: 2;
  grid-row: 1;
}

.detail-output {
  flex: 1 1 0;
  padding: 12px 16px 16px;
  grid-column: 3;
  grid-row: 1;
}

.detail-preview__file {
  min-height: 40px;
  padding: 4px 12px;
  border-bottom: 1px solid #e7e8e8;
}

.detail-preview__label {
  min-height: 29px;
  padding: 4px 12px;
  border-bottom: 1px solid #e7e8e8;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  line-height: 20px;
}

.detail-preview__viewer {
  flex: 1 1 auto;
  min-height: 0;
  background: #f3f3f4;
}

.detail-output__head,
.detail-output__actions,
.detail-output__format-tabs,
.detail-output__category-tabs,
.detail-bottom-bar,
.detail-page-actions,
.detail-confirm-actions {
  display: flex;
  align-items: center;
}

.detail-output__head {
  justify-content: space-between;
  gap: 16px;
}

.detail-output__format-tabs {
  padding: 4px;
  border: 1px solid #d7e2fe;
  border-radius: 3px;
  background: #f5f7ff;

  button {
    min-height: 24px;
    padding: 2px 8px;
    border: 0;
    border-radius: 3px;
    background: transparent;
    color: #0c131f;
    font-size: 12px;
    line-height: 20px;
    cursor: pointer;

    &.is-active {
      background: #fff;
      color: #396ffa;
    }
  }
}

.detail-output__actions,
.detail-page-actions,
.detail-confirm-actions {
  gap: 8px;
}

.detail-export,
.detail-icon-btn,
.detail-page-actions button,
.detail-reparse,
.detail-confirm {
  border-radius: 3px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 14px;
    height: 14px;
  }
}

.detail-export {
  min-height: 24px;
  gap: 4px;
  padding: 2px 8px;
  border: 1px solid #396ffa;
  background: #fff;
  color: #396ffa;
  font-size: 12px;
  line-height: 20px;
}

.detail-icon-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 1px solid #dcdde1;
  background: #fff;
  color: #0c131f;
}

.parsing-config-layer {
  position: fixed;
  inset: 0;
  z-index: 2100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
}

.parsing-config-dialog {
  width: 635px;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
  color: #0c131f;
  box-shadow: 0 2px 4px -1px rgba(0, 0, 0, 0.12), 0 4px 5px rgba(0, 0, 0, 0.08), 0 1px 10px rgba(0, 0, 0, 0.05);
}

.parsing-config-dialog__header {
  height: 56px;
  padding: 16px 32px;
  background: #f3f3f4;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
  }
}

.parsing-config-dialog__close {
  width: 20px;
  height: 20px;
  padding: 2px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: rgba(12, 19, 31, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }
}

.parsing-config-dialog__content {
  width: 635px;
  padding: 24px 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.parsing-config-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.parsing-config-section__head {
  display: flex;
  flex-direction: column;
  gap: 4px;

  h3,
  p {
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  h3 {
    font-size: 14px;
    font-weight: 600;
    line-height: 22px;
    color: #0c131f;
  }

  p {
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    color: rgba(12, 19, 31, 0.4);
  }
}

.parsing-config-grid {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.parsing-config-item {
  width: 280px;
  padding: 8px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(12, 19, 31, 0.6);
  text-align: left;
  cursor: pointer;

  > span:first-child {
    min-width: 0;
    flex: 1 1 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
    line-height: 22px;
    font-weight: 400;
  }
}

.parsing-config-item__label {
  display: inline-flex;
  align-items: center;
  gap: 8px;

  em {
    width: 16px;
    height: 16px;
    border: 1px solid rgba(12, 19, 31, 0.4);
    border-radius: 50%;
    color: rgba(12, 19, 31, 0.4);
    font-style: normal;
    font-size: 12px;
    line-height: 14px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 auto;
  }
}

.figma-switch {
  width: 32px;
  height: 20px;
  border-radius: 12px;
  background: #dcdde1;
  position: relative;
  flex: 0 0 auto;

  i {
    position: absolute;
    left: 4px;
    top: 4px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.18);
  }

  &.is-checked {
    background: #396ffa;

    i {
      left: auto;
      right: 2.5px;
      top: 2.5px;
      width: 15px;
      height: 15px;
    }
  }
}

.parsing-config-dialog__footer {
  padding: 16px 32px 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;

  button {
    min-height: 32px;
    padding: 5px 16px;
    border: 0;
    border-radius: 3px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    line-height: 22px;
    cursor: pointer;
  }
}

.config-cancel {
  background: #e7e8e8;
  color: #0c131f;
}

.config-reparse {
  background: #396ffa;
  color: #fff;
}

.detail-output__divider {
  height: 1px;
  margin-top: 16px;
  background: #e7e8e8;
}

.detail-output__category-tabs {
  display: flex;
  align-items: center;
  min-height: 46px;
  border-bottom: 1px solid #e7e8e8;
  overflow-x: auto;

  button {
    flex: 1 1 0;
    min-width: max-content;
    height: 46px;
    padding: 8px;
    border: 0;
    border-bottom: 3px solid transparent;
    background: #fff;
    color: rgba(12, 19, 31, 0.6);
    font-size: 14px;
    line-height: 22px;
    white-space: nowrap;
    cursor: pointer;

    &.is-active {
      color: #396ffa;
      border-bottom-color: #396ffa;
    }
  }
}

.detail-category-empty {
  padding: 32px 12px;
  color: rgba(12, 19, 31, 0.45);
  font-size: 14px;
  line-height: 22px;
}

.detail-empty-result,
.detail-md-wrap,
.detail-txt-content {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}

.detail-empty-result {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(12, 19, 31, 0.6);
}

.detail-empty-result__main {
  margin-top: -18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;

  p {
    margin: 0;
    font-size: 14px;
    line-height: 22px;
    font-weight: 400;
    text-align: center;
  }
}

.detail-empty-result__icon {
  width: 64px;
  height: 64px;
  border: 1px solid currentColor;
  border-radius: 50%;
  background: #f5f7ff;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 32px;
    height: 32px;
  }
}

.detail-empty-result.is-pending,
.detail-empty-result.is-processing {
  color: #396ffa;

  .detail-empty-result__main p {
    color: rgba(12, 19, 31, 0.6);
  }
}

.detail-empty-result.is-failed {
  color: #d44040;

  .detail-empty-result__icon {
    background: #fbecec;
  }

  .detail-empty-result__main p {
    color: rgba(12, 19, 31, 0.6);
  }
}

.detail-empty-result__retry {
  height: 32px;
  padding: 5px 16px;
  border: 1px solid #396ffa;
  border-radius: 3px;
  background: #396ffa;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }
}

.detail-md-content,
.detail-txt-content {
  height: 100%;
  padding: 12px 12px 12px 0;
  overflow: auto;
  color: #0c131f;
}

.detail-md-content {
  position: relative;
}

.detail-md-content [contenteditable="true"] {
  min-height: 20px;
  border-radius: 4px;
  outline: none;
}

.detail-md-content [contenteditable="true"]:focus {
  box-shadow: 0 0 0 1px rgba(57, 111, 250, 0.35);
  background: rgba(57, 111, 250, 0.06);
}

.editable-table-block {
  position: relative;
  display: grid;
}

.editable-table-cell-action {
  position: absolute;
  z-index: 8;
  width: 20px;
  height: 20px;
  padding: 0;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  background: #fff;
  color: #0c131f;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  &:hover,
  &:focus-visible {
    border-color: #396ffa;
    color: #396ffa;
    outline: none;
  }

  svg {
    width: 14px;
    height: 14px;
  }
}

.editable-table-action-menu {
  position: absolute;
  z-index: 9;
  width: 204px;
  padding: 6px;
  border: 1px solid #dcdde1;
  border-radius: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 2px;
  box-shadow: 0 8px 20px rgba(12, 19, 31, 0.08);
  box-sizing: border-box;

  button {
    width: 192px;
    height: 28px;
    padding: 3px 8px;
    border: 0;
    border-radius: 3px;
    background: #fff;
    color: #0c131f;
    font-family: 'Encode Sans', sans-serif;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    text-align: left;
    box-sizing: border-box;
    white-space: nowrap;
    cursor: pointer;

    &:hover,
    &:focus-visible {
      background: #f5f7ff;
      color: #396ffa;
      outline: none;
    }
  }
}

.editable-table-block :deep(table) {
  width: max-content;
  min-width: 100%;
}

.editable-table-block :deep(th),
.editable-table-block :deep(td) {
  min-width: 50px;
}

.editable-result-text {
  white-space: pre-wrap;
}

.detail-state {
  height: 100%;
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-bottom-bar {
  min-height: 45px;
  padding-top: 12px;
  border-top: 1px solid #e7e8e8;
  justify-content: space-between;
  gap: 16px;
}

.detail-page-actions button {
  width: 32px;
  height: 32px;
  border: 1px solid #396ffa;
  background: #fff;
  color: #396ffa;
}

.detail-reparse,
.detail-confirm {
  height: 32px;
  padding: 5px 16px;
  font-size: 14px;
  line-height: 22px;
}

.detail-reparse {
  border: 1px solid #396ffa;
  background: #fff;
  color: #396ffa;
}

.detail-confirm {
  border: 1px solid #396ffa;
  background: #396ffa;
  color: #fff;

  svg {
    margin-right: 8px;
  }
}

.detail-bottom-status {
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
}

:deep(.jv-container) {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
}

@keyframes translateLeftRow {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(6px);
  }
  100% {
    transform: translateY(0);
  }
}
@keyframes rotate360 {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
@keyframes translateTopRow {
  0% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(6px);
  }
  100% {
    transform: translateX(0);
  }
}
.contactBtn {
  position: relative;
  background: linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
}
:deep(.md) {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-thumb {
    border-radius:10px;
    background: #232748;
  }
  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }
}
.history {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
    display: block;
  }
  &::-webkit-scrollbar-thumb {
    border-radius:10px;
    background: #232748;
  }
  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }
}
:deep() {
  table,
  th,
  td {
    border-color: #999;
    border-width: 1px !important;
  }
}
.borderActive {
  :deep() {
    table,
      th,
      td {
        border-color: #396FFA;
      }
  }
}
.after {
  position: relative;
  &::after {
    content: " ";
    display: block;
    position: absolute;
    bottom: -1px;
    left: 0;
    width: 100%;
    height: 3px;
    background: #396FFA;
  }
}
:deep(.el-switch) {
  &.is-checked .el-switch__core {
    background: #396FFA;
  }
}
:deep(.el-select.extractSelect) {
  .el-select__wrapper {
    max-width: 200px;
    &.is-focused {
      box-shadow: 0 0 0 1px #396FFA inset;
    }
  }
  input {
    height: 30px;
  }
}
.shadow {
  box-shadow: 0px 4px 32px 0px #8195C852;
  &::after {
    content: "";
    position: absolute;
    top: 72px;
    left: -6px;
    border-top: 6px solid transparent;
    border-right: 6px solid white;
    border-bottom: 6px solid transparent;
  }
}
.progress-bar {
  top: 0;
  left: 0;
  z-index: 1;
  width: 100%;
  max-width: 0;
  height: 100%;
  position: absolute;
  border-radius: 6px;
  transition: width .6s ease;
}
.login:hover {
  .decs {
    animation: none;
    display: inline-block !important;
  }
}
.decs {
  animation: translateLeftRow 1s ease-in-out infinite;
  &::after {
    content: '';
    top: -9px;
    right: 24px;
    position: absolute;
    border-bottom: 10px solid #596177;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
  }
}
.free {
  box-shadow: 2px 6px 18px 0px #00000033;
  &::after {
    content: '';
    bottom: -10px;
    left: 50%;
    position: absolute;
    border-top: 10px solid white;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
  }
}
:deep(.el-overlay-dialog) {
  .el-dialog {
    .loading {
      animation: rotate360 2s linear infinite;
    }
    .el-dialog__header {
      display: none;
    }
    .el-dialog__body {
      display: flex;
      word-break: initial;
      flex-direction: column;
    }
  }
}
:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  flex-direction: row;
  .el-radio {
    height: 16px;
    &:hover {
      background: transparent;
    }
    &:nth-child(3), &:nth-child(4) {
      margin-top: 20px;
    }
    width: 50%;
    margin-right: 0;
    color: #232748;
    .el-radio__inner {
      width: 12px;
      height: 12px;
      background: transparent;
      border: 1.5px solid #666666;
    }
    .el-radio__input.is-checked + .el-radio__label {
      color: #232748;
    }
    .el-radio, .el-radio__input {
      white-space: normal !important
    }
    .el-radio__input.is-checked {
      .el-radio__inner {
        border-color: #1460F3;
        background-color: #1460F3;
        &::after {
          width: 5px;
          height: 5px;
          background-color: white !important;
        }
      }
    }
  }
}
:deep(.upload) {
  .el-upload {
    width: 100%;
    .el-button {
      width: 100%;
      color: #94969D;
      padding: 12px 16px;
      border-color: #E1E3E8;
      &:hover {
        background-color: unset;
        border-color: #396FFA;
      }
      &:focus {
        background-color: unset;
      }
      span {
        display: flex;
        font-size: 14px;
        font-weight: 400;
        line-height: 18px;
        align-items: center;
        svg { 
          margin-right: 8px;
        }
      }
    }
    .el-button.is-disabled {
      color: #CCCCCC;
      border-color: #E1E3E8;
      background-color: #F0F1F2;
    }
  }
  &.disabled .el-upload {
    pointer-events: none;
  }
  .el-upload-list {
    .el-upload-list__item {
      font-size: 16px;
      margin-top: 16px;
      line-height: 24px;
      &:hover {
        background-color: unset;
      }
      .el-upload-list__item-name {
        padding-left: 0;
        cursor: default;
        color: #232748;
        margin-bottom: 4px;
        &:hover {
          color: #232748;
        }
        .el-icon-document {
          width: 20px;
          height: 20px;
          vertical-align: middle;
          &::before {
            content: none;
          }
        }
      }
      &.is-success .el-upload-list__item-name .el-icon-document {
        flex-shrink: 0;
        background: url('~/assets/images/support/success.svg');
      }
      &.is-fail .el-upload-list__item-name {
        color: #94969D;
        .el-icon-document {
          background: url('~/assets/images/support/fail.svg');
        }
      }
      .el-progress-bar {
        .el-progress-bar__outer {
          height: 6px !important;
          background-color: #F3F6FF;
          border-radius: 0;
          .el-progress-bar__inner {
            border-radius: 0;
            background-color: #396FFA;
          }
        }
      }
      .el-progress__text {
        top: -20px;
        font-size: 16px !important;
        line-height: 24px;
        color: #396FFA;
      }
      .el-icon-close {
        display: inline-block;
        width: 16px;
        height: 16px;
        background: url('~/assets/images/support/delete.svg');
        &::before {
          content: none;
        }
      }
      &.is-uploading .el-icon-close {
        display: none;
      }
      &.is-uploading:hover .el-icon-close {
        display: inline-block;
      }
      .el-icon-close-tip {
        display: none;
      }
      .el-upload-list__item-status-label {
        display: none;
      }
      &.is-ready .el-icon-document,
      &.is-uploading .el-icon-document {
        display: none;
      }
    }
  }
}
.extract {
  padding-top: 16px;
  padding-left: 16px;
  padding-right: 16px;
  background: #030D26;
}
:deep(.jv-container) {
  width: 100%;
  float: right;
  border: none;
  overflow: auto;
  font-size: 16px;
  color: #01fef4;
  line-height: 20px;
  padding-left: 10px;
  white-space: nowrap;
  background: #030D26;
  font-family: 'Encode Sans';
  height: calc(100vh - 216px);
  &::-webkit-scrollbar{
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-thumb{
    border-radius:10px;
    background: rgba(255, 255, 255, 0.32);
  }
  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }
  .jv-more {
    display: none;
  }
  &.jv-dark {
    background: transparent;
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
</style>
<style lang="scss">
.md-content {
  h1, h2, h3, h4 {
    font-weight: 600;
    line-height: var(--line-height-headings);
  }

  h1 { font-size: var(--font-size-h1); }
  h2 { font-size: var(--font-size-h2); }
  h3 { font-size: var(--font-size-h3); }
  h4 { font-size: var(--font-size-h4); }
}

.el-popper.is-pure.is-light.el-select__popper {
  .el-select-dropdown__item.is-selected {
    color: #396FFA;
    font-weight: 600;
  }
}
.el-popper.is-dark {
  border: none;
  padding: 8px 8px 12px;
  background: #596177;
  box-shadow: 2px 6px 18px 0px #00000033;
  .popper__arrow {
    border-top-color: #596177;
    &::after {
      border-top-color: #596177;
    }
  }
  .el-popper__arrow {
    display: inherit;
    &::before {
      background-color: #596177;
    }
  }
}
.el-message {
  min-width: unset;
  top: 112px !important;
  padding: 8px 16px;
  border-color: #F871714D;
  background-color: #FBEDED;
  .el-message__content {
    font-family: 'Encode Sans';
    font-size: 16px;
    line-height: 24px;
    color: #000;
  }
  .el-icon-error {
    color: #F87171;
    font-size: 20px;
  }
}
.el-message--error {
  .el-message__content {
    color: #F87171;
  }
}
</style>
