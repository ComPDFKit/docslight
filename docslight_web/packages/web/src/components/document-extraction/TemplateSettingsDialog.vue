<template>
  <div v-if="visible" class="template-settings-page">
    <input
      ref="sampleFileInput"
      class="sample-file-input"
      type="file"
      accept=".pdf,image/*"
      @change="handleSampleFileChange"
    />
    <aside class="app-menu">
      <div class="app-menu__logo" aria-hidden="true">
        <span class="app-menu__logo-blue"></span>
        <span class="app-menu__logo-green"></span>
      </div>
      <nav class="app-menu__nav" aria-label="Primary">
        <button v-for="item in navItems" :key="item" class="app-menu__item" type="button" :title="item">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path v-if="item === 'home'" d="M3.5 8.2 10 3l6.5 5.2v7.3H5.4V9.6h9.2v5.9" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
            <path v-else-if="item === 'extract'" d="M4 4h9l3 3v9H4V4Zm9 0v4h3M7 10h6M7 13h4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
            <path v-else-if="item === 'scan'" d="M5 4H3v4m14-4h-2M3 12v4h2m10 0h2v-4M6.5 10h7M8 7h4M8 13h4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
            <path v-else-if="item === 'help'" d="M10 17a7 7 0 1 0 0-14 7 7 0 0 0 0 14Zm-2-9a2 2 0 1 1 3.2 1.6c-.8.5-1.2 1-1.2 1.9m0 2h.01" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
            <path v-else d="M10 3.5 15.5 6.7v6.6L10 16.5l-5.5-3.2V6.7L10 3.5Zm0 4.2v4.6m-4-2.3h8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>
      </nav>
    </aside>

    <main class="settings-shell">
      <header class="settings-topbar">
        <div class="settings-topbar__left">
          <button class="icon-button icon-button--plain" type="button" @click="$emit('close')" title="Back">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M4 6h12M4 10h8M4 14h12" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
            </svg>
          </button>
          <span>{{ t('common.de') }}</span>
        </div>
        <div class="settings-topbar__right">
          <div class="topbar-language">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <circle cx="10" cy="10" r="7.2" stroke="currentColor" stroke-width="1.6" />
              <path d="M3 10h14M10 2.8c2 2 3 4.4 3 7.2s-1 5.2-3 7.2c-2-2-3-4.4-3-7.2s1-5.2 3-7.2Z" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
            </svg>
            <span>En</span>
          </div>
          <div class="settings-topbar__divider"></div>
          <button class="user-avatar" type="button" title="Admin">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M10 10a3.2 3.2 0 1 0 0-6.4 3.2 3.2 0 0 0 0 6.4Zm-5.2 6.2c.8-2.7 2.6-4.1 5.2-4.1s4.4 1.4 5.2 4.1" fill="currentColor" />
            </svg>
          </button>
        </div>
      </header>

      <section class="settings-workspace">
        <aside class="template-sidebar">
          <div class="template-sidebar__title">
            <button class="template-sidebar__back" type="button" @click="$emit('close')">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                <path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <span>{{ t('template.settings') }}</span>
            </button>
          </div>

          <div class="segmented-tabs">
            <button type="button" class="is-active">{{ t('template.team') }}</button>
          </div>

          <label class="template-search">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
            </svg>
            <input v-model="searchQuery" type="text" :placeholder="t('template.search')" />
          </label>

          <div class="template-sidebar__list">
            <div v-if="templateListLoading" class="template-sidebar__state">{{ t('singleParse.loading') }}</div>
            <div v-else-if="!filteredTemplates.length" class="template-sidebar__state">{{ t('template.noTemplates') }}</div>
            <article
              v-for="template in filteredTemplates"
              :key="template.id"
              class="template-item"
              :class="selectedTemplateId === template.id && 'is-selected'"
              @click="selectTemplate(template.id)"
            >
              <div class="template-item__main">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2" />
                  <path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
                </svg>
                <span>{{ template.name }}</span>
              </div>
              <button
                class="switch"
                :class="[template.enabled && 'is-on', statusUpdatingId === template.id && 'is-loading']"
                type="button"
                :disabled="statusUpdatingId === template.id"
                @click.stop="toggleTemplate(template.id)"
                :aria-pressed="template.enabled"
                :title="template.enabled ? t('template.disableTemplate') : t('template.enableTemplate')"
              >
                <span></span>
              </button>
              <div v-if="selectedTemplateId === template.id && template.source !== 'default'" class="template-item__actions">
                <button class="upload-sample" type="button" @click.stop="openSampleFilePicker">
                  <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                    <path d="M8 10.5V2.75M4.75 6 8 2.75 11.25 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                    <path d="M3 10.25v2.5h10v-2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  {{ t('template.uploadSample') }}
                </button>
                <button class="trash-button" type="button" @click.stop="deleteTemplate(template.id)" :title="t('template.delete')">
                  <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                    <path d="M3 4h10M6 4V2.6h4V4m-5 2v7h6V6M7.4 7.4v4.2M9.6 7.4v4.2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                </button>
              </div>
            </article>
          </div>

          <button class="add-template-button" type="button" @click="addTemplate">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
            {{ t('extraction.add') }}
          </button>
        </aside>

        <section class="preview-panel">
          <div class="preview-panel__filebar">
            <div v-if="sampleFile" class="preview-panel__file">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                <path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2" />
                <path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
              </svg>
              <span>{{ sampleFile.fileName }}</span>
              <span class="status-tag status-tag--confirmed">{{ t('extraction.confirmed') }}</span>
              <span class="status-tag status-tag--unconfirmed">{{ t('extraction.unconfirmed') }}</span>
            </div>
          </div>
          <div class="preview-panel__label">{{ t('extraction.preview') }}</div>
          <div class="preview-panel__canvas">
            <div v-if="sampleFile" class="sample-preview">
              <img v-if="sampleFile.previewType === 'image'" :src="sampleFile.fileDownUrl" :alt="sampleFile.fileName" />
              <div v-else-if="sampleFile.previewType === 'pdf'" ref="pdfViewer" class="sample-preview__pdf"></div>
              <div v-else class="sample-preview__unsupported">
                <span>{{ sampleFile.fileName }}</span>
                <a :href="sampleFile.fileDownUrl" target="_blank" rel="noopener">{{ t('singleExtract.open') }}</a>
              </div>
            </div>
            <div
              v-else-if="!isSelectedDefaultTemplate"
              class="upload-dropzone"
              :class="dragover && 'is-dragging'"
              @dragover.prevent="dragover = true"
              @dragleave.prevent="dragover = false"
              @drop.prevent="handleSampleDrop"
            >
              <button class="upload-files-button" type="button" @click="openSampleFilePicker">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <path d="M8 10.5V2.75M4.75 6 8 2.75 11.25 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M3 10.25v2.5h10v-2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                {{ t('extraction.upload') }}
              </button>
              <span>{{ t('extraction.selectFile[1]') }}</span>
              <p>{{ t('extraction.selectFile[2]') }}</p>
            </div>
            <div v-else class="sample-preview__unsupported">
              <span>{{ t('template.noSampleFile') }}</span>
            </div>
          </div>
        </section>

        <section class="settings-panel">
          <div class="settings-tabs">
            <button type="button" :class="activeSettingsTab === 'settings' && 'is-active'" @click="activeSettingsTab = 'settings'">{{ t('template.settings') }}</button>
            <button type="button" :class="activeSettingsTab === 'result' && 'is-active'" @click="activeSettingsTab = 'result'">{{ t('template.testResult') }}</button>
          </div>

          <div v-if="activeSettingsTab === 'settings'" class="settings-panel__content">
            <div class="current-template">
              <h2>{{ t('extraction.currentTemplate') }}</h2>
              <div class="current-template__row">
                <input v-model="currentTemplateName" type="text" />
                <button class="ai-setup-button" type="button">
                  <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                    <path d="M5 3.5 6.2 6 9 7.2 6.2 8.4 5 11 3.8 8.4 1 7.2 3.8 6 5 3.5Zm7-1 .7 1.5 1.6.7-1.6.7L12 7l-.7-1.6-1.6-.7 1.6-.7L12 2.5Zm-.7 7.5.8 1.6 1.7.8-1.7.8-.8 1.8-.8-1.8-1.7-.8 1.7-.8.8-1.6Z" stroke="currentColor" stroke-width="1.1" stroke-linejoin="round" />
                  </svg>
                  {{ t('template.aiOneClickSetup') }}
                </button>
              </div>
            </div>

            <div class="field-table">
              <div class="field-table__head">
                <span>{{ t('extraction.fieldType') }}</span>
                <span>{{ t('extraction.fieldName') }}</span>
                <span>{{ t('template.aliases') }} <InfoDot /></span>
                <span>{{ t('extraction.prompt') }} <InfoDot /></span>
                <span>{{ t('extraction.exportedFieldName') }} <InfoDot /></span>
                <span></span>
              </div>

              <div class="field-table__body">
                <template v-for="field in fields" :key="field.id">
                  <div class="field-row" :class="field.type === 'table' && 'field-row--table'">
                    <label class="select-cell">
                      <select v-model="field.type">
                        <option value="text">{{ t('template.fieldTypes.text') }}</option>
                        <option value="table">{{ t('template.fieldTypes.table') }}</option>
                      </select>
                      <ChevronDown class="select-cell__chevron" />
                    </label>
                    <input v-model="field.name" class="form-control" type="text" :placeholder="t('template.fieldTypes.text')" />
                    <div class="alias-cell">
                      <template v-if="field.type === 'text' && field.aliases.length">
                        <span v-for="tag in field.aliases" :key="tag" class="alias-tag">
                          {{ tag }}
                          <button type="button" @click="removeAlias(field.id, tag)">×</button>
                        </span>
                      </template>
                      <input v-else v-model="field.aliasInput" class="form-control" type="text" :placeholder="t('template.fieldTypes.text')" />
                    </div>
                    <textarea v-model="field.prompt" class="form-control form-control--prompt prompt-control" :placeholder="t('template.promptPlaceholder')" @input="adjustPromptHeight"></textarea>
                    <input v-model="field.exportedName" class="form-control" type="text" :placeholder="t('template.exportedNamePlaceholder')" />
                    <button class="delete-field" type="button" @click="removeField(field.id)" :title="t('template.delete')">
                      <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                        <path d="M3 4h10M6 4V2.6h4V4m-5 2v7h6V6M7.4 7.4v4.2M9.6 7.4v4.2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" />
                      </svg>
                    </button>
                  </div>

                  <div v-if="field.type === 'table'" class="table-children">
                    <div v-for="child in field.children" :key="child.id" class="field-row field-row--child">
                      <label class="select-cell">
                        <select v-model="child.type">
                          <option value="text">{{ t('template.fieldTypes.text') }}</option>
                        </select>
                        <ChevronDown class="select-cell__chevron" />
                      </label>
                      <input v-model="child.name" class="form-control" type="text" :placeholder="t('template.fieldTypes.text')" />
                      <input v-model="child.aliasInput" class="form-control" type="text" :placeholder="t('template.fieldTypes.text')" />
                      <textarea v-model="child.prompt" class="form-control form-control--prompt prompt-control" :placeholder="t('template.promptPlaceholder')" @input="adjustPromptHeight"></textarea>
                      <input v-model="child.exportedName" class="form-control" type="text" :placeholder="t('template.exportedNamePlaceholder')" />
                      <button class="delete-field" type="button" @click="removeChildField(field.id, child.id)" :title="t('template.delete')">
                        <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                          <path d="M3 4h10M6 4V2.6h4V4m-5 2v7h6V6M7.4 7.4v4.2M9.6 7.4v4.2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                      </button>
                    </div>
                    <button class="add-table-field" type="button" @click="addChildField(field.id)">
                      <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                        <path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                      </svg>
                      {{ t('extraction.addFields') }}
                    </button>
                  </div>
                </template>
              </div>

              <button class="add-field-button" type="button" @click="addField">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                </svg>
                {{ t('extraction.addNewField') }}
              </button>
            </div>
          </div>
          <div v-else class="settings-panel__content settings-panel__content--result">
            <div v-if="testLoading" class="test-result-state">{{ t('template.testing') }}</div>
            <div v-else-if="testResultError" class="test-result-state test-result-state--error">{{ testResultError }}</div>
            <div v-else-if="formattedTestResult" class="test-result-box">
              <pre>{{ formattedTestResult }}</pre>
            </div>
            <div v-else class="test-result-state">{{ t('template.noTestResults') }}</div>
          </div>

          <footer class="settings-panel__footer">
            <div class="pager-buttons">
              <button class="pager-button" type="button" title="Previous">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </button>
              <button class="pager-button" type="button" title="Next">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <path d="M6 3.5 10.5 8 6 12.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </button>
            </div>
            <div class="footer-actions">
              <button class="test-button" type="button" :disabled="testLoading" @click="testTemplate">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.4" />
                  <path d="M7 5.6 10.5 8 7 10.4V5.6Z" fill="currentColor" />
                </svg>
                {{ testLoading ? t('template.testing') : t('extraction.test') }}
              </button>
              <button class="save-button" type="button" :disabled="saveLoading" @click="saveTemplate">
                {{ saveLoading ? t('template.saving') : t('extraction.saveTemplate') }}
              </button>
            </div>
          </footer>
        </section>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import ComPDFKitViewer from '../../assets/@compdfkit/webviewer'
import { get, post } from '../../utils/request'
import { getEnv } from '../../utils/env'

const props = defineProps<{
  visible: boolean
  createOnOpen?: boolean
  initialTemplateIds?: string[]
}>()

defineEmits<{
  (e: 'close'): void
}>()

const { t } = useI18n()

interface TemplateItem {
  id: string
  name: string
  enabled: boolean
  status: TemplateStatus
  source: TemplateSourceType
  raw: TemplateSource
}

type TemplateStatus = 0 | 1 | 2
type TemplateSourceType = 'custom' | 'default' | 'new'

interface TemplateSource {
  id?: string | number
  templateId?: string | number
  groupTemplateId?: string | number
  name?: string
  templateName?: string
  status?: number | string | boolean
  enabled?: boolean
  available?: boolean
  [key: string]: unknown
}

interface FieldChild {
  id: string
  type: 'text'
  name: string
  aliasInput: string
  prompt: string
  exportedName: string
}

interface TemplateField {
  id: string
  type: 'text' | 'table'
  name: string
  aliases: string[]
  aliasInput: string
  prompt: string
  exportedName: string
  children: FieldChild[]
}

const InfoDot = defineComponent({
  name: 'InfoDot',
  setup() {
    return () => h('svg', {
      class: 'info-dot',
      viewBox: '0 0 16 16',
      fill: 'none',
      'aria-hidden': 'true'
    }, [
      h('circle', { cx: '8', cy: '8', r: '6.2', stroke: 'currentColor', 'stroke-width': '1.3' }),
      h('path', { d: 'M6.8 6.3a1.3 1.3 0 1 1 2.2.9c-.7.5-1 1-1 1.8', stroke: 'currentColor', 'stroke-width': '1.3', 'stroke-linecap': 'round' }),
      h('path', { d: 'M8 11.5h.01', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round' })
    ])
  }
})

const ChevronDown = defineComponent({
  name: 'ChevronDown',
  setup() {
    return () => h('svg', {
      viewBox: '0 0 16 16',
      fill: 'none',
      'aria-hidden': 'true'
    }, [
      h('path', {
        d: 'M3.39063 5.39062L8 10L12.6094 5.39062L11.6666 4.44781L8 8.11448L4.33344 4.44781L3.39063 5.39062Z',
        fill: 'currentColor'
      })
    ])
  }
})

const navItems = ['home', 'extract', 'scan', 'help', 'settings']
const activeSettingsTab = ref<'settings' | 'result'>('settings')
const searchQuery = ref('')
const selectedTemplateId = ref('')
const currentTemplateName = ref(t('template.untitledTemplate'))
const dragover = ref(false)
const templateListLoading = ref(false)
const templateDetailLoading = ref(false)
const testLoading = ref(false)
const saveLoading = ref(false)
const testResult = ref<unknown | null>(null)
const testResultError = ref('')
const statusUpdatingId = ref('')
const pdfViewer = ref<HTMLElement | null>(null)
const sampleFileInput = ref<HTMLInputElement | null>(null)

const templates = ref<TemplateItem[]>([])
const sampleFile = ref<SampleFile | null>(null)

const fields = ref<TemplateField[]>([])
const pendingCreateOnOpen = ref(false)

interface TemplateKeyValue {
  prompt?: string | null
  mapping?: string | null
}

interface TemplateDetail {
  id?: string | number
  name?: string
  fileId?: string | number
  keys?: Record<string, string | TemplateKeyValue>
  tableHeaders?: Record<string, Record<string, TemplateKeyValue>> | Record<string, TemplateKeyValue>[]
  tableHandles?: Record<string, string | TemplateKeyValue>
  [key: string]: unknown
}

interface ExtractFieldValue {
  prompt: string | null
  mapping: string | null
}

interface TestExtractTemplateDTO {
  keys: Record<string, ExtractFieldValue>
  tableHeaders: Record<string, Record<string, ExtractFieldValue>>
  name: string
  id?: string
  fileId?: string
  status?: TemplateStatus
}

interface SampleFile {
  fileId: string
  fileName: string
  fileDownUrl: string
  previewType: 'pdf' | 'image' | 'other'
  localFile?: File
  objectUrl?: string
}

interface FileDetail {
  fileId?: string | number
  id?: string | number
  fileName?: string
  name?: string
  fileDownUrl?: string
  [key: string]: unknown
}

const filteredTemplates = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return templates.value
  return templates.value.filter(template => template.name.toLowerCase().includes(query))
})

const selectedTemplate = computed(() => templates.value.find(item => item.id === selectedTemplateId.value))
const isSelectedDefaultTemplate = computed(() => selectedTemplate.value?.source === 'default')

const formattedTestResult = computed(() => {
  if (!testResult.value) return ''
  return JSON.stringify(testResult.value, null, 2)
})

let searchTimer: ReturnType<typeof setTimeout> | undefined
let pdfUI: any
let pdfViewerHost: HTMLElement | null = null
let pdfLoadToken = 0

function normalizeTemplateStatus(item: TemplateSource): TemplateStatus {
  const status = Number(item.status)
  if (status === 0 || status === 1 || status === 2) return status
  if (typeof item.enabled === 'boolean') return item.enabled ? 1 : 0
  if (typeof item.available === 'boolean') return item.available ? 1 : 0
  return 1
}

function mapTemplateItem(item: TemplateSource, index: number, source: TemplateSourceType): TemplateItem {
  const id = String(item.id ?? item.templateId ?? item.groupTemplateId ?? `template-${index}`)
  const status = normalizeTemplateStatus(item)
  return {
    id,
    name: String(item.name ?? item.templateName ?? t('template.untitledTemplate')),
    enabled: status === 1,
    status,
    source,
    raw: item
  }
}

async function getTemplateList(selectFirst = true) {
  templateListLoading.value = true
  try {
    const query = encodeURIComponent(searchQuery.value.trim())
    const [templateListResponse, defaultTemplateResponse] = await Promise.all([
      get<TemplateSource[]>(`/api/idp/get-template-list${query ? `?name=${query}` : ''}`),
      get<TemplateSource[]>('/api/idp/get-default-template')
    ])
    const customTemplates = templateListResponse.data?.data || []
    const defaultTemplates = defaultTemplateResponse.data?.data || []
    templates.value = [
      ...customTemplates
        .filter(item => normalizeTemplateStatus(item) !== 2)
        .map((item, index) => mapTemplateItem(item, index, 'custom')),
      ...defaultTemplates
        .filter(item => normalizeTemplateStatus(item) !== 2)
        .map((item, index) => mapTemplateItem(item, index, 'default'))
    ]

    const selectedStillExists = templates.value.some(item => item.id === selectedTemplateId.value)
    if (!selectedStillExists && selectFirst) {
      const firstTemplate = templates.value[0]
      if (firstTemplate) {
        selectTemplate(firstTemplate.id)
      } else {
        selectedTemplateId.value = ''
        currentTemplateName.value = ''
      }
    }
  } catch {
    ElMessage.error('Failed to load template list')
  } finally {
    templateListLoading.value = false
  }
}

function getTemplateId(template: TemplateItem) {
  return String(template.raw.templateId ?? template.raw.id ?? template.id)
}

function isTemplateMatched(template: TemplateItem, id: string) {
  if (!id) return false
  return [
    template.id,
    template.name,
    template.raw.id,
    template.raw.templateId,
    template.raw.groupTemplateId,
    template.raw.name,
    template.raw.templateName
  ].some(value => String(value || '') === id)
}

function selectTemplateByInitialIds(ids?: string[]) {
  const targetIds = (ids || []).map(item => item.trim()).filter(Boolean)
  if (!targetIds.length) return false
  const template = templates.value.find(item => targetIds.some(id => isTemplateMatched(item, id)))
  if (!template) return false
  selectTemplate(template.id)
  return true
}

function isSuccessCode(code: unknown) {
  return code === 0 || code === 200 || code === '0' || code === '200'
}

function getResponseMessage(data: { message?: string, msg?: string }) {
  return data.message || data.msg || ''
}

function getPreviewType(fileName: string): SampleFile['previewType'] {
  const extension = fileName.split('.').pop()?.toLowerCase()
  if (extension === 'pdf') return 'pdf'
  if (['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp'].includes(extension || '')) return 'image'
  return 'other'
}

function isSupportedSampleFile(file: File) {
  const fileName = file.name.toLowerCase()
  const mimeType = file.type.toLowerCase()
  return fileName.endsWith('.pdf')
    || mimeType === 'application/pdf'
    || mimeType.startsWith('image/')
    || ['.jpg', '.jpeg', '.png', '.webp', '.gif', '.bmp', '.svg', '.tif', '.tiff'].some(ext => fileName.endsWith(ext))
}

function readTemplateValue(value: string | TemplateKeyValue | undefined) {
  if (!value) return { prompt: '', mapping: '' }
  if (typeof value === 'string') return { prompt: value, mapping: '' }
  return {
    prompt: value.prompt || '',
    mapping: value.mapping || ''
  }
}

function buildField(id: string, name: string, value: string | TemplateKeyValue | undefined): TemplateField {
  const item = readTemplateValue(value)
  return {
    id,
    type: 'text',
    name,
    aliases: [],
    aliasInput: '',
    prompt: item.prompt,
    exportedName: item.mapping,
    children: []
  }
}

function buildChildField(id: string, name: string, value: TemplateKeyValue | string | undefined): FieldChild {
  const item = readTemplateValue(value)
  return {
    id,
    type: 'text',
    name,
    aliasInput: '',
    prompt: item.prompt,
    exportedName: item.mapping
  }
}

function mapTemplateFields(detail: TemplateDetail): TemplateField[] {
  const nextFields: TemplateField[] = []
  Object.entries(detail.keys || {}).forEach(([fieldName, value], index) => {
    nextFields.push(buildField(`field-key-${index}`, fieldName, value))
  })

  if (detail.tableHeaders) {
    if (Array.isArray(detail.tableHeaders)) {
      detail.tableHeaders.forEach((tableObj, tableIndex) => {
        nextFields.push({
          id: `field-table-${tableIndex}`,
          type: 'table',
          name: `Table ${tableIndex + 1}`,
          aliases: [],
          aliasInput: '',
          prompt: '',
          exportedName: '',
          children: Object.entries(tableObj).map(([fieldName, value], childIndex) => buildChildField(`child-${tableIndex}-${childIndex}`, fieldName, value))
        })
      })
    } else {
      Object.entries(detail.tableHeaders).forEach(([tableName, tableObj], tableIndex) => {
        nextFields.push({
          id: `field-table-${tableIndex}`,
          type: 'table',
          name: tableName,
          aliases: [],
          aliasInput: '',
          prompt: '',
          exportedName: '',
          children: Object.entries(tableObj).map(([fieldName, value], childIndex) => buildChildField(`child-${tableIndex}-${childIndex}`, fieldName, value))
        })
      })
    }
  } else if (detail.tableHandles) {
    Object.entries(detail.tableHandles).forEach(([fieldName, value], index) => {
      nextFields.push(buildField(`field-table-handle-${index}`, fieldName, value))
    })
  }
  return nextFields
}

async function fetchSampleAsFile(file: SampleFile) {
  if (file.localFile) return file.localFile
  const response = await fetch(file.fileDownUrl)
  if (!response.ok) throw new Error('Failed to fetch sample file')
  const blob = await response.blob()
  return new File([blob], file.fileName, { type: blob.type || 'application/pdf' })
}

function buildExtractFieldValue(prompt: string, mapping: string): ExtractFieldValue {
  return {
    prompt: prompt || null,
    mapping: mapping || null
  }
}

function buildExtractTemplateDTO(options: { includeId?: boolean } = { includeId: true }): TestExtractTemplateDTO {
  const keys: Record<string, ExtractFieldValue> = {}
  const tableHeaders: Record<string, Record<string, ExtractFieldValue>> = {}

  fields.value.forEach((field, index) => {
    const fieldName = field.name.trim()
    if (field.type === 'text') {
      if (!fieldName) return
      keys[fieldName] = buildExtractFieldValue(field.prompt, field.exportedName)
      return
    }

    const tableKey = fieldName || `Table_${index + 1}`
    const tableFields: Record<string, ExtractFieldValue> = {}
    field.children.forEach(child => {
      const childName = child.name.trim()
      if (!childName) return
      tableFields[childName] = buildExtractFieldValue(child.prompt, child.exportedName)
    })
    if (Object.keys(tableFields).length) {
      tableHeaders[tableKey] = tableFields
    }
  })

  const selectedTemplate = templates.value.find(item => item.id === selectedTemplateId.value)
  const dto: TestExtractTemplateDTO = {
    keys,
    tableHeaders,
    name: currentTemplateName.value
  }
  if (selectedTemplate && options.includeId) {
    dto.id = getTemplateId(selectedTemplate)
    dto.status = selectedTemplate.status
  }
  if (sampleFile.value?.fileId) {
    dto.fileId = sampleFile.value.fileId
  }
  return dto
}

function buildTestFormData(file: File) {
  const data = new FormData()
  data.append('file', file)
  data.append('extractTemplateDTO', JSON.stringify(buildExtractTemplateDTO()))
  return data
}

function hasTemplateFields() {
  return fields.value.some(field => field.name.trim() || field.children.some(child => child.name.trim()))
}

async function uploadSampleFileForTemplate(templateId: string) {
  if (!sampleFile.value?.localFile || !templateId) return
  const formData = new FormData()
  formData.append('file', sampleFile.value.localFile)
  formData.append('templateId', templateId)
  const { data } = await post('/api/idp/add-template-file', formData)
  if (!isSuccessCode(data.code)) {
    throw new Error(getResponseMessage(data) || t('template.uploadSampleFailed'))
  }
}

async function saveTemplate() {
  if (saveLoading.value) return
  const selectedTemplate = templates.value.find(item => item.id === selectedTemplateId.value)
  const templateName = currentTemplateName.value.trim()
  if (!templateName) {
    ElMessage.warning(t('template.enterTemplateName'))
    return
  }
  if (!hasTemplateFields()) {
    ElMessage.warning(t('template.addFieldBeforeSaving'))
    return
  }

  saveLoading.value = true
  try {
    const isUpdate = selectedTemplate?.source === 'custom'
    const payload = buildExtractTemplateDTO({ includeId: isUpdate })
    payload.name = templateName
    if (!isUpdate) {
      delete payload.id
    }

    const { data } = await post(isUpdate ? '/api/idp/update-template' : '/api/idp/add-template', payload)
    if (!isSuccessCode(data.code)) {
      throw new Error(getResponseMessage(data) || t('template.saveFailed'))
    }

    const savedTemplateId = isUpdate && selectedTemplate ? getTemplateId(selectedTemplate) : String(data.data || '')
    await uploadSampleFileForTemplate(savedTemplateId)
    ElMessage.success(t('template.saved'))
    await getTemplateList()
    const nextTemplate = templates.value.find(item => item.id === savedTemplateId)
      || templates.value.find(item => item.name === templateName && item.source === 'custom')
    if (nextTemplate) {
      selectTemplate(nextTemplate.id)
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : t('template.saveFailed')
    ElMessage.error(message)
  } finally {
    saveLoading.value = false
  }
}

async function fetchJsonContent(url: string) {
  const response = await fetch(url, {
    headers: {
      Accept: 'application/json'
    }
  })
  if (!response.ok) throw new Error(t('template.testResultLoadFailed'))
  const text = await response.text()
  if (!text) return {}
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

async function loadTestResultContent(result: unknown) {
  if (typeof result !== 'string') return result
  const value = result.trim()
  if (!value) return {}
  if (/^https?:\/\//i.test(value)) {
    return fetchJsonContent(value)
  }
  try {
    return JSON.parse(value)
  } catch {
    return value
  }
}

async function testTemplate() {
  if (testLoading.value) return
  if (!sampleFile.value) {
    ElMessage.warning(t('template.uploadSampleBeforeTesting'))
    return
  }
  if (!hasTemplateFields()) {
    ElMessage.warning(t('template.addFieldBeforeTesting'))
    return
  }

  testLoading.value = true
  testResultError.value = ''
  testResult.value = null

  try {
    const sourceFile = await fetchSampleAsFile(sampleFile.value)
    const formData = buildTestFormData(sourceFile)
    const { data } = await post('/api/idp/test-extract', formData, {}, {
      timeout: 3600000
    } as any)
    if (!isSuccessCode(data.code)) {
      throw new Error(getResponseMessage(data) || 'Template test failed')
    }
    testResult.value = await loadTestResultContent(data.data)
    activeSettingsTab.value = 'result'
    ElMessage.success('Template test completed')
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Template test failed'
    testResultError.value = message
    activeSettingsTab.value = 'result'
    ElMessage.error(message)
  } finally {
    testLoading.value = false
  }
}

async function loadSampleFile(fileId?: string | number) {
  clearLocalSampleUrl()
  sampleFile.value = null
  pdfLoadToken++
  if (!fileId) return
  const { data: { data } } = await get<FileDetail>(`/api/idp/get-file-by-id?fileId=${fileId}`)
  const fileName = String(data.fileName ?? data.name ?? '')
  const fileDownUrl = String(data.fileDownUrl ?? '')
  if (!fileName || !fileDownUrl) return
  sampleFile.value = {
    fileId: String(data.fileId ?? data.id ?? fileId),
    fileName,
    fileDownUrl,
    previewType: getPreviewType(fileName)
  }
  if (sampleFile.value.previewType === 'pdf') {
    await loadPdfPreview(sampleFile.value)
  }
}

function clearLocalSampleUrl() {
  if (sampleFile.value?.objectUrl) {
    URL.revokeObjectURL(sampleFile.value.objectUrl)
  }
}

function openSampleFilePicker() {
  if (isSelectedDefaultTemplate.value) return
  sampleFileInput.value?.click()
}

async function useLocalSampleFile(file: File) {
  clearLocalSampleUrl()
  pdfLoadToken++
  const objectUrl = URL.createObjectURL(file)
  sampleFile.value = {
    fileId: '',
    fileName: file.name,
    fileDownUrl: objectUrl,
    previewType: getPreviewType(file.name),
    localFile: file,
    objectUrl
  }
  testResult.value = null
  testResultError.value = ''
  if (sampleFile.value.previewType === 'pdf') {
    await loadPdfPreview(sampleFile.value)
  }
}

function handleSampleFileChange(event: Event) {
  const target = event.target
  if (!(target instanceof HTMLInputElement)) return
  const file = target.files?.[0]
  target.value = ''
  if (file) {
    if (!isSupportedSampleFile(file)) {
      ElMessage.warning('Only PDF and image files are supported for sample upload')
      return
    }
    useLocalSampleFile(file)
  }
}

function handleSampleDrop(event: DragEvent) {
  dragover.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) {
    if (!isSupportedSampleFile(file)) {
      ElMessage.warning('Only PDF and image files are supported for sample upload')
      return
    }
    useLocalSampleFile(file)
  }
}

async function initPdfViewer() {
  if (!pdfViewer.value) return
  if (pdfUI && pdfViewerHost === pdfViewer.value) return
  pdfUI = null
  pdfViewerHost = pdfViewer.value
  const core = await ComPDFKitViewer.init({
    license: getEnv('LICENSE_KEY') || '',
    pdfUrl: '',
    path: '/',
    showToolbarControl: false,
    isRenderAnnotations: false,
    enableDefaultFont: true
  }, pdfViewer.value)
  pdfUI = core.UI
  core.UI.disableElements?.(['pageNavOverlay'])
  core.UI.textPopup?.update([])
}

async function loadPdfPreview(file: SampleFile) {
  const token = ++pdfLoadToken
  await nextTick()
  if (token !== pdfLoadToken || !props.visible || !pdfViewer.value) return

  try {
    await initPdfViewer()
    if (token !== pdfLoadToken) return
    const loadSource = file.localFile || file.fileDownUrl
    const loadTask = pdfUI?.loadDocument(loadSource)
    if (loadTask?.then) await loadTask
  } catch {
    if (token === pdfLoadToken) {
      ElMessage.error('Failed to preview PDF file')
    }
  }
}

async function loadTemplateDetail(template: TemplateItem) {
  templateDetailLoading.value = true
  clearLocalSampleUrl()
  sampleFile.value = null
  testResult.value = null
  testResultError.value = ''
  pdfLoadToken++
  fields.value = []
  try {
    const templateId = getTemplateId(template)
    const { data: { data } } = await get<TemplateDetail>(`/api/idp/get-template-by-id?templateId=${templateId}`)
    currentTemplateName.value = data.name || template.name
    if (data.status !== undefined) {
      template.status = normalizeTemplateStatus(data as TemplateSource)
      template.enabled = template.status === 1
      template.raw.status = template.status
    }
    fields.value = mapTemplateFields(data)
    adjustAllPromptHeights()
    await loadSampleFile(data.fileId)
  } catch {
    ElMessage.error('Failed to load template detail')
  } finally {
    templateDetailLoading.value = false
  }
}

watch(() => props.visible, async (visible) => {
  if (visible) {
    pendingCreateOnOpen.value = !!props.createOnOpen
    const hasInitialTemplate = Boolean(props.initialTemplateIds?.length)
    await getTemplateList(!pendingCreateOnOpen.value && !hasInitialTemplate)
    if (pendingCreateOnOpen.value) {
      addTemplate()
      pendingCreateOnOpen.value = false
    } else if (hasInitialTemplate && !selectTemplateByInitialIds(props.initialTemplateIds)) {
      const firstTemplate = templates.value[0]
      if (firstTemplate) selectTemplate(firstTemplate.id)
    }
  } else {
    pdfLoadToken++
    pendingCreateOnOpen.value = false
    clearLocalSampleUrl()
  }
}, { immediate: true })

watch(() => props.initialTemplateIds, (ids) => {
  if (!props.visible || !ids?.length) return
  selectTemplateByInitialIds(ids)
})

watch(searchQuery, () => {
  if (!props.visible) return
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    getTemplateList()
  }, 300)
})

function selectTemplate(id: string) {
  selectedTemplateId.value = id
  const template = templates.value.find(item => item.id === id)
  if (template) {
    currentTemplateName.value = template.name
    if (template.source === 'new') {
      activeSettingsTab.value = 'settings'
      clearLocalSampleUrl()
      sampleFile.value = null
      testResult.value = null
      testResultError.value = ''
      pdfLoadToken++
      fields.value = [{
        id: `field-${Date.now()}`,
        type: 'text',
        name: '',
        aliases: [],
        aliasInput: '',
        prompt: '',
        exportedName: '',
        children: []
      }]
      nextTick(adjustAllPromptHeights)
      return
    }
    loadTemplateDetail(template)
  }
}

async function toggleTemplate(id: string) {
  const template = templates.value.find(item => item.id === id)
  if (!template || statusUpdatingId.value) return

  const templateId = getTemplateId(template)
  const nextEnabled = !template.enabled
  const url = nextEnabled ? '/api/idp/enable-template' : '/api/idp/disable-template'
  statusUpdatingId.value = id
  try {
    const { data } = await post(`${url}?templateId=${encodeURIComponent(templateId)}`)
    if (!isSuccessCode(data.code)) {
      throw new Error(getResponseMessage(data) || 'Failed to update template status')
    }
    template.enabled = nextEnabled
    template.status = nextEnabled ? 1 : 0
    template.raw.status = template.status
    ElMessage.success(nextEnabled ? t('template.enabled') : t('template.disabled'))
    await getTemplateList()
  } catch (error) {
    const message = error instanceof Error ? error.message : t('template.updateStatusFailed')
    ElMessage.error(message)
  } finally {
    statusUpdatingId.value = ''
  }
}

function addTemplate() {
  const template: TemplateItem = {
    id: `template-${Date.now()}`,
    name: t('template.untitledTemplate'),
    enabled: true,
    status: 1,
    source: 'new',
    raw: {}
  }
  templates.value.push(template)
  selectTemplate(template.id)
}

function deleteTemplate(id: string) {
  const template = templates.value.find(item => item.id === id)
  if (template?.source === 'default') return
  const nextTemplates = templates.value.filter(item => item.id !== id)
  templates.value = nextTemplates
  if (selectedTemplateId.value === id) {
    const fallback = templates.value[0]
    if (fallback) selectTemplate(fallback.id)
  }
}

function addField() {
  fields.value.push({
    id: `field-${Date.now()}`,
    type: 'text',
    name: '',
    aliases: [],
    aliasInput: '',
    prompt: '',
    exportedName: '',
    children: []
  })
}

function removeField(id: string) {
  fields.value = fields.value.filter(field => field.id !== id)
}

function addChildField(fieldId: string) {
  const field = fields.value.find(item => item.id === fieldId)
  if (!field) return
  field.children.push({
    id: `child-${Date.now()}`,
    type: 'text',
    name: '',
    aliasInput: '',
    prompt: '',
    exportedName: ''
  })
}

function removeChildField(fieldId: string, childId: string) {
  const field = fields.value.find(item => item.id === fieldId)
  if (!field) return
  field.children = field.children.filter(child => child.id !== childId)
}

function removeAlias(fieldId: string, alias: string) {
  const field = fields.value.find(item => item.id === fieldId)
  if (!field) return
  const index = field.aliases.indexOf(alias)
  if (index >= 0) field.aliases.splice(index, 1)
}

function resizeTextarea(element: HTMLTextAreaElement) {
  element.style.height = '32px'
  element.style.height = `${Math.max(32, element.scrollHeight)}px`
}

function adjustPromptHeight(event: Event) {
  const target = event.target
  if (target instanceof HTMLTextAreaElement) {
    resizeTextarea(target)
  }
}

function adjustAllPromptHeights() {
  nextTick(() => {
    document.querySelectorAll<HTMLTextAreaElement>('.template-settings-page .prompt-control').forEach(resizeTextarea)
  })
}

onBeforeUnmount(() => {
  pdfLoadToken++
  pdfUI = null
  pdfViewerHost = null
  clearLocalSampleUrl()
  if (searchTimer) clearTimeout(searchTimer)
})
</script>

<style lang="scss" scoped>
.template-settings-page {
  --brand: #396ffa;
  --brand-hover: #244ff0;
  --ink: #0c131f;
  --muted: rgba(12, 19, 31, 0.4);
  --line: #e7e8e8;
  --control-line: #dcdde1;
  --page-bg: #f3f6ff;
  --soft: #f3f3f4;

  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  min-width: 1280px;
  min-height: 720px;
  overflow: hidden;
  background: var(--page-bg);
  color: var(--ink);
  font-family: 'Encode Sans', sans-serif;

  * {
    box-sizing: border-box;
  }

  button,
  input,
  select,
  textarea {
    font-family: inherit;
  }
}

.sample-file-input {
  display: none;
}

.app-menu {
  width: 68px;
  flex: 0 0 68px;
  height: 100%;
  background: #fff;
  border-right: 1px solid var(--line);
}

.app-menu__logo {
  position: relative;
  width: 68px;
  height: 56px;
  border-bottom: 1px solid var(--line);
}

.app-menu__logo-blue,
.app-menu__logo-green {
  position: absolute;
  border-radius: 3px;
}

.app-menu__logo-blue {
  left: 19px;
  top: 15px;
  width: 18px;
  height: 21px;
  background: #0071f5;
}

.app-menu__logo-green {
  left: 27px;
  top: 22px;
  width: 19px;
  height: 19px;
  border: 6px solid #00c4a1;
  background: #fff;
}

.app-menu__nav {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  padding-top: 24px;
}

.app-menu__item {
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: #747983;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    background: #f5f7ff;
    color: var(--brand);
  }
}

.settings-shell {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.settings-topbar {
  height: 56px;
  flex: 0 0 56px;
  padding: 0 28px;
  background: #fff;
  border-bottom: 1px solid #eef0f4;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.settings-topbar__left,
.settings-topbar__right,
.topbar-language {
  display: flex;
  align-items: center;
}

.settings-topbar__left {
  gap: 16px;
  font-size: 16px;
  line-height: 24px;
}

.settings-topbar__right {
  gap: 16px;
  font-size: 14px;
  line-height: 22px;
}

.topbar-language {
  gap: 8px;

  svg {
    width: 20px;
    height: 20px;
  }
}

.settings-topbar__divider {
  width: 1px;
  height: 26px;
  background: #e7e8ec;
}

.icon-button,
.user-avatar,
.pager-button,
.delete-field,
.trash-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-button {
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--ink);
  cursor: pointer;

  svg {
    width: 20px;
    height: 20px;
  }
}

.user-avatar {
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 50%;
  background: #ebf1fe;
  color: var(--brand);
  cursor: pointer;

  svg {
    width: 20px;
    height: 20px;
  }
}

.settings-workspace {
  flex: 1 1 auto;
  min-height: 0;
  padding: 16px;
  display: grid;
  grid-template-columns: 230px minmax(460px, 1fr) minmax(560px, 1fr);
  gap: 8px;
}

.template-sidebar,
.preview-panel,
.settings-panel {
  min-height: 0;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}

.template-sidebar {
  display: flex;
  flex-direction: column;
  padding: 0 12px 16px;
}

.template-sidebar__title {
  height: 64px;
  flex: 0 0 64px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid transparent;
}

.template-sidebar__back {
  height: 32px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--ink);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.segmented-tabs {
  height: 32px;
  padding: 2px;
  border: 1px solid #cddbff;
  border-radius: 3px;
  background: #f5f7ff;
  display: grid;
  grid-template-columns: 1fr;

  button {
    width: 100%;
    border: 0;
    border-radius: 2px;
    background: transparent;
    color: var(--ink);
    font-size: 12px;
    line-height: 20px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &.is-active {
      background: #fff;
      color: var(--brand);
    }
  }
}

.template-search {
  height: 32px;
  margin-top: 8px;
  padding: 0 8px;
  border-radius: 3px;
  background: var(--soft);
  color: var(--muted);
  display: flex;
  align-items: center;
  gap: 8px;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
  }

  input {
    min-width: 0;
    width: 100%;
    border: 0;
    outline: 0;
    background: transparent;
    color: var(--ink);
    font-size: 14px;
    line-height: 22px;

    &::placeholder {
      color: var(--muted);
    }
  }
}

.template-sidebar__list {
  flex: 1 1 auto;
  min-height: 0;
  margin-top: 16px;
  overflow-y: auto;
}

.template-sidebar__state {
  height: 38px;
  padding: 8px;
  color: var(--muted);
  font-size: 14px;
  line-height: 22px;
}

.template-item {
  position: relative;
  min-height: 38px;
  margin-bottom: 8px;
  padding: 8px;
  border: 1px solid transparent;
  border-radius: 4px;
  color: rgba(12, 19, 31, 0.56);
  cursor: pointer;

  &.is-selected {
    min-height: 70px;
    border-color: #cddbff;
    background: #f5f7ff;
    color: var(--brand);
  }
}

.template-item__main {
  height: 22px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 40px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
  }

  span {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.switch {
  position: absolute;
  right: 8px;
  top: 9px;
  width: 32px;
  height: 20px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: #d8dbe0;
  cursor: pointer;
  transition: background 0.18s ease;

  span {
    position: absolute;
    top: 2px;
    left: 2px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #fff;
    transition: transform 0.18s ease;
  }

  &.is-on {
    background: var(--brand);

    span {
      transform: translateX(12px);
    }
  }
}

.template-item__actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-sample {
  width: 158px;
  height: 24px;
  border: 1px solid var(--brand);
  border-radius: 3px;
  background: #fff;
  color: var(--brand);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  line-height: 20px;

  svg {
    width: 14px;
    height: 14px;
  }
}

.trash-button {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 1px solid var(--control-line);
  border-radius: 3px;
  background: #fff;
  color: #94969d;
  cursor: pointer;

  svg {
    width: 14px;
    height: 14px;
  }
}

.add-template-button {
  height: 32px;
  flex: 0 0 32px;
  width: 100%;
  border: 1px solid var(--brand);
  border-radius: 3px;
  background: var(--brand);
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.preview-panel {
  display: flex;
  flex-direction: column;
  background: #f3f3f4;
}

.preview-panel__filebar,
.preview-panel__label {
  background: #fff;
  border-bottom: 1px solid var(--line);
}

.preview-panel__filebar {
  height: 40px;
  flex: 0 0 40px;
  padding: 0 12px;
  display: flex;
  align-items: center;
}

.preview-panel__file {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
    color: rgba(12, 19, 31, 0.4);
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 4px;
  border-radius: 3px;
  font-size: 12px;
  line-height: 20px;
}

.status-tag--confirmed {
  color: #67d1a0;
  background: #ecf9f3;
}

.status-tag--unconfirmed {
  color: #f5a13a;
  background: #fef3e6;
}

.preview-panel__label {
  height: 28px;
  flex: 0 0 28px;
  padding: 4px 12px;
  color: var(--muted);
  font-size: 12px;
  line-height: 20px;
}

.preview-panel__canvas {
  flex: 1 1 auto;
  min-height: 0;
  padding: 20px;
  display: flex;
}

.sample-preview,
.sample-preview__pdf {
  width: 100%;
  height: 100%;
  min-height: 0;
}

.sample-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
}

.sample-preview__pdf {
  background: #f3f3f4;
}

.sample-preview__unsupported {
  width: 100%;
  height: 100%;
  border: 1px dashed var(--control-line);
  border-radius: 10px;
  color: #52555f;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  a {
    color: var(--brand);
    text-decoration: none;
  }
}

.upload-dropzone {
  width: 100%;
  height: 100%;
  border: 1px dashed var(--brand);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #52555f;

  &.is-dragging {
    background: #f5f7ff;
  }

  span,
  p {
    margin: 0;
    font-size: 16px;
    line-height: 24px;
  }

  span {
    margin-top: 12px;
  }

  p {
    margin-top: 8px;
  }
}

.upload-files-button {
  height: 32px;
  padding: 5px 16px;
  border: 0;
  border-radius: 3px;
  background: var(--brand);
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.settings-panel {
  display: flex;
  flex-direction: column;
}

.settings-tabs {
  height: 60px;
  flex: 0 0 60px;
  padding: 16px 16px 0;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: flex-end;
  gap: 8px;

  button {
    height: 44px;
    padding: 0 16px;
    border: 0;
    border-bottom: 2px solid transparent;
    background: transparent;
    color: rgba(12, 19, 31, 0.56);
    cursor: pointer;
    font-size: 14px;
    line-height: 22px;

    &.is-active {
      color: var(--brand);
      border-bottom-color: var(--brand);
    }
  }
}

.settings-panel__content {
  flex: 1 1 auto;
  min-height: 0;
  padding: 16px;
  overflow-y: auto;
}

.settings-panel__content--result {
  background: #fff;
}

.test-result-state {
  min-height: 160px;
  border: 1px dashed var(--control-line);
  border-radius: 6px;
  color: var(--muted);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  text-align: center;
  font-size: 14px;
  line-height: 22px;
}

.test-result-state--error {
  border-color: #f3b5b5;
  background: #fff7f7;
  color: #d44040;
}

.test-result-box {
  min-height: 100%;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #f8f9fb;
  color: var(--ink);

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
    font-family: 'Encode Sans', sans-serif;
    font-size: 14px;
    line-height: 22px;
  }
}

.current-template {
  h2 {
    margin: 0 0 8px;
    color: var(--ink);
    font-size: 14px;
    font-weight: 700;
    line-height: 22px;
  }
}

.current-template__row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);

  input {
    flex: 1 1 auto;
    min-width: 0;
    height: 32px;
    padding: 5px 8px;
    border: 1px solid var(--control-line);
    border-radius: 3px;
    outline: 0;
    color: var(--ink);
    font-size: 14px;
    line-height: 22px;

    &:focus {
      border-color: var(--brand);
    }
  }
}

.ai-setup-button {
  width: 169px;
  height: 32px;
  flex: 0 0 169px;
  border: 0;
  border-radius: 3px;
  background: linear-gradient(232.02deg, #396ffa -65.62%, #56f9c8 2.78%, #396ffa 98.62%);
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.field-table {
  margin-top: 28px;
}

.field-table__head,
.field-row {
  display: grid;
  grid-template-columns: 128px 130px 130px minmax(130px, 1fr) 130px 24px;
  gap: 12px;
  align-items: start;
}

.field-table__head {
  height: 28px;
  padding: 0 16px;
  color: var(--muted);
  font-size: 14px;
  line-height: 22px;

  span {
    min-width: 0;
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }
}

:deep(.info-dot) {
  width: 16px;
  height: 16px;
  color: rgba(12, 19, 31, 0.4);
}

.field-table__body {
  margin-top: 12px;
}

.field-row {
  min-height: 48px;
  padding: 8px 12px;
  background: var(--soft);
}

.field-row--child {
  padding: 6px 12px;

  .select-cell {
    width: calc(100% - 20px);
    margin-left: 20px;
  }
}

.form-control {
  width: 100%;
  height: 32px;
  min-width: 0;
  border: 1px solid var(--control-line);
  border-radius: 3px;
  background: #fff;
  color: var(--ink);
  outline: 0;
  padding: 5px 8px;
  font-size: 14px;
  line-height: 22px;

  &::placeholder {
    color: var(--muted);
  }

  &:focus {
    border-color: var(--brand);
  }
}

.select-cell {
  position: relative;
  width: 100%;
  height: 32px;
  min-width: 0;
  display: block;

  select {
    width: 100%;
    height: 32px;
    min-width: 0;
    padding: 5px 32px 5px 8px;
    border: 1px solid #dcdde1;
    border-radius: 3px;
    outline: 0;
    appearance: none;
    background: #fff;
    color: #0c131f;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    cursor: pointer;

    &:focus {
      border-color: var(--brand);
    }
  }
}

.select-cell__chevron {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 16px;
  height: 16px;
  color: rgba(12, 19, 31, 0.4);
  pointer-events: none;
}

.form-control--prompt {
  height: 32px;
  min-height: 32px;
  overflow: hidden;
  resize: none;
}

.alias-cell {
  min-width: 0;
  min-height: 32px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.alias-tag {
  height: 24px;
  max-width: 100%;
  padding: 2px 6px;
  border-radius: 3px;
  background: #dcdee0;
  color: var(--ink);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  line-height: 20px;

  button {
    padding: 0;
    border: 0;
    background: transparent;
    color: rgba(12, 19, 31, 0.45);
    cursor: pointer;
    font-size: 14px;
    line-height: 14px;
  }
}

.delete-field {
  width: 24px;
  height: 32px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #c0c4cc;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    color: #d44040;
  }
}

.table-children {
  background: var(--soft);
}

.add-table-field {
  height: 32px;
  margin: 4px 0 10px 46px;
  padding: 0 8px;
  border: 0;
  background: transparent;
  color: var(--brand);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.add-field-button {
  width: 100%;
  height: 32px;
  margin-top: 16px;
  border: 1px solid var(--brand);
  border-radius: 3px;
  background: #fff;
  color: var(--brand);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.settings-panel__footer {
  height: 64px;
  flex: 0 0 64px;
  padding: 0 16px;
  border-top: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager-buttons,
.footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager-button {
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid var(--brand);
  border-radius: 3px;
  background: #fff;
  color: var(--brand);
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }
}

.test-button,
.save-button {
  height: 32px;
  border-radius: 3px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  line-height: 22px;
}

.test-button {
  width: 82px;
  border: 1px solid var(--brand);
  background: #fff;
  color: var(--brand);
  gap: 8px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.save-button {
  width: 122px;
  border: 1px solid var(--brand);
  background: var(--brand);
  color: #fff;
}

@media (max-width: 1500px) {
  .template-settings-page {
    min-width: 1180px;
  }

  .settings-workspace {
    grid-template-columns: 230px minmax(420px, 1fr) minmax(540px, 1fr);
  }

  .field-table__head,
  .field-row {
    grid-template-columns: 112px 118px 118px minmax(118px, 1fr) 118px 24px;
    gap: 8px;
  }
}
</style>
