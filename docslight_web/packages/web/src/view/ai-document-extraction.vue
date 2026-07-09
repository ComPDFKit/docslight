<template>
  <div class="extraction-detail">
    <!-- Header bar -->
    <header class="extraction-detail__header">
      <div class="extraction-detail__header-left">
        <span class="extraction-detail__header-title">AI Document Extraction</span>
      </div>
    </header>

    <!-- Main content: 3-panel layout -->
    <div class="extraction-detail__body">
      <!-- Panel 1: Template Sidebar (230px) -->
      <aside class="detail-sidebar">
        <!-- New Template button + Settings -->
        <div class="detail-sidebar__new-btn-row">
          <button class="detail-sidebar__new-btn" type="button" @click="selectDialogVisible = true">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
            New Templates
          </button>
          <button class="detail-sidebar__settings-btn" type="button" @click="templateSettingVisible = !templateSettingVisible">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 10a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z" stroke="currentColor" stroke-width="1.2"/><path d="M13.3 10a1.1 1.1 0 0 0 .2 1.2l.04.04a1.33 1.33 0 1 1-1.88 1.88l-.04-.04a1.1 1.1 0 0 0-1.2-.2 1.1 1.1 0 0 0-.67 1.01v.11a1.33 1.33 0 1 1-2.66 0v-.06a1.1 1.1 0 0 0-.72-1.01 1.1 1.1 0 0 0-1.2.2l-.04.04a1.33 1.33 0 1 1-1.88-1.88l.04-.04a1.1 1.1 0 0 0 .2-1.2 1.1 1.1 0 0 0-1.01-.67h-.11a1.33 1.33 0 1 1 0-2.66h.06a1.1 1.1 0 0 0 1.01-.72 1.1 1.1 0 0 0-.2-1.2l-.04-.04a1.33 1.33 0 1 1 1.88-1.88l.04.04a1.1 1.1 0 0 0 1.2.2h.01a1.1 1.1 0 0 0 .67-1.01v-.11a1.33 1.33 0 1 1 2.66 0v.06a1.1 1.1 0 0 0 .72 1.01 1.1 1.1 0 0 0 1.2-.2l.04-.04a1.33 1.33 0 1 1 1.88 1.88l-.04.04a1.1 1.1 0 0 0-.2 1.2v.01a1.1 1.1 0 0 0 1.01.67h.11a1.33 1.33 0 1 1 0 2.66h-.06a1.1 1.1 0 0 0-1.01.72Z" stroke="currentColor" stroke-width="1.2"/></svg>
          </button>
        </div>

        <!-- All Documents button + Search -->
        <button
          class="detail-sidebar__all-btn"
          :class="activeTemplate === 'all' && 'is-active'"
          type="button"
          @click="activeTemplate = 'all'"
        >
          <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M2 3.5h5.5L9.5 5H14v8.5H2v-10Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
          All Documents
        </button>

        <label class="detail-sidebar__search">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
          <input v-model="searchQuery" type="text" placeholder="Search by file name">
        </label>

        <div class="detail-sidebar__divider"></div>

        <!-- File items list -->
        <div class="detail-sidebar__list">
          <button
            v-for="doc in filteredDocuments"
            :key="doc.id"
            class="detail-file-item"
            :class="selectedFileId === doc.id && 'is-selected'"
            type="button"
            @click="selectFile(doc)"
          >
            <div class="detail-file-item__name">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2"/><path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
              <span>{{ doc.fileName }}</span>
            </div>
            <div class="detail-file-item__desc">{{ doc.type || 'Purchase Order' }}</div>
            <div class="detail-file-item__tags">
              <span v-if="doc.reviewStatus" class="mini-tag" :class="reviewStatusTone(doc.reviewStatus)">{{ doc.reviewStatus }}</span>
              <span class="mini-tag" :class="statusTone(doc.processingStatus)">{{ doc.processingStatus }}</span>
            </div>
          </button>
        </div>
      </aside>

      <!-- Panel 2: File Preview (547px) -->
      <div class="extraction-detail__preview">
        <!-- Preview top bar -->
        <div class="extraction-detail__preview-header">
          <button class="extraction-detail__back-btn" type="button">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
          <el-select v-model="currentTemplateId" class="extraction-detail__template-select" placeholder="Select template">
            <el-option
              v-for="tmpl in allTemplates"
              :key="tmpl.id"
              :label="tmpl.name"
              :value="tmpl.id"
            />
          </el-select>
          <button class="extraction-detail__export-btn" type="button">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 10.5V2.75M4.75 6 8 2.75 11.25 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 10.25v2.5h10v-2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
            Export
          </button>
        </div>
        <!-- Preview content area (PDF/image viewer) -->
        <div class="extraction-detail__preview-content">
          <div v-if="!selectedFileId" class="extraction-detail__preview-empty">
            <svg viewBox="0 0 48 48" fill="none" aria-hidden="true" class="extraction-detail__preview-empty-icon"><path d="M24 4C12.95 4 4 12.95 4 24s8.95 20 20 20 20-8.95 20-20S35.05 4 24 4Z" stroke="#DCDEE0" stroke-width="1.5"/><path d="M24 16v12M18 22h12" stroke="#DCDEE0" stroke-width="1.5" stroke-linecap="round"/></svg>
            <span>Select a file to preview</span>
          </div>
          <!-- PDF viewer placeholder - will integrate with Result.vue viewer later -->
          <div v-else class="extraction-detail__preview-placeholder">
            <span>File Preview Area</span>
          </div>
        </div>
        <!-- Page navigation -->
        <div v-if="selectedFileId" class="extraction-detail__page-nav">
          <button type="button" class="extraction-detail__page-btn" @click="prevPage">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
          <span class="extraction-detail__page-text">Page {{ String(currentPage).padStart(2, '0') }}</span>
          <button type="button" class="extraction-detail__page-btn" @click="nextPage">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M6 3.5 10.5 8 6 12.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
        </div>
      </div>

      <!-- Panel 3: Result Form (547px) -->
      <div class="extraction-detail__result">
        <!-- Add new field button row -->
        <div class="extraction-detail__result-top-bar">
          <button class="extraction-detail__add-field-btn" type="button">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
            Add New Field
          </button>
        </div>

        <!-- Result header: template label + select + export -->
        <div class="extraction-detail__result-header">
          <div class="extraction-detail__result-header-label">Current Template</div>
          <div class="extraction-detail__result-header-row">
            <el-select v-model="currentTemplateId" class="extraction-detail__result-select" placeholder="Select template">
              <el-option
                v-for="tmpl in allTemplates"
                :key="tmpl.id"
                :label="tmpl.name"
                :value="tmpl.id"
              />
            </el-select>
            <button class="extraction-detail__header-action-btn" type="button">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 10.5V2.75M4.75 6 8 2.75 11.25 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 10.25v2.5h10v-2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
              Export
            </button>
          </div>
        </div>

        <div class="extraction-detail__result-divider"></div>

        <!-- Scrollable content area -->
        <div class="extraction-detail__result-content">
          <!-- Focus type buttons -->
          <div class="extraction-detail__focus-row">
            <button
              v-for="focus in focusTypes"
              :key="focus.id"
              type="button"
              class="extraction-detail__focus-btn"
              :class="activeFocus === focus.id && 'is-active'"
              @click="activeFocus = focus.id"
            >{{ focus.label }}</button>
          </div>

          <!-- Page label row -->
          <div class="extraction-detail__page-label-row">
            <span class="extraction-detail__page-label">Page {{ String(currentPage).padStart(2, '0') }}</span>
            <div class="extraction-detail__zoom-controls">
              <button type="button" class="extraction-detail__icon-btn" title="Zoom out">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M3 8h10" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
              </button>
              <button type="button" class="extraction-detail__icon-btn" title="Zoom in">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
              </button>
              <button type="button" class="extraction-detail__icon-btn" title="Fit width">
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M2 2h12v12H2z" stroke="currentColor" stroke-width="1.2"/><path d="M5 6l-2 2 2 2M11 6l2 2-2 2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
            </div>
          </div>

          <!-- Title input group -->
          <div class="extraction-detail__field-group">
            <label class="extraction-detail__field-label">Title</label>
            <input
              v-model="titleValue"
              type="text"
              class="extraction-detail__field-input"
              placeholder="Enter title"
            />
          </div>

          <!-- Subtitle input group -->
          <div class="extraction-detail__field-group">
            <label class="extraction-detail__field-label">Subtitle</label>
            <input
              v-model="subtitleValue"
              type="text"
              class="extraction-detail__field-input"
              placeholder="Enter subtitle"
            />
          </div>

          <!-- Add New Field button (inline) -->
          <button class="extraction-detail__add-field-inline" type="button">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
            Add New Field
          </button>

          <!-- Table group -->
          <div class="extraction-detail__table-group">
            <div class="extraction-detail__table-header-row">
              <span class="extraction-detail__table-title">Extraction Results</span>
            </div>
            <table class="extraction-detail__table">
              <thead>
                <tr>
                  <th class="extraction-detail__th">Field Name</th>
                  <th class="extraction-detail__th">Value</th>
                  <th class="extraction-detail__th extraction-detail__th--actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="field in extractionFields" :key="field.id" class="extraction-detail__tr">
                  <td class="extraction-detail__td">{{ field.name }}</td>
                  <td class="extraction-detail__td">{{ field.value }}</td>
                  <td class="extraction-detail__td extraction-detail__td--actions">
                    <button type="button" class="extraction-detail__table-icon-btn" title="Edit">
                      <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M11.5 2.5l2 2-8 8H3.5v-2l8-8Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
                    </button>
                    <button type="button" class="extraction-detail__table-icon-btn" title="Delete">
                      <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M4 4h8M6.67 4V2.67h2.66V4M10 4v8.67a1.33 1.33 0 0 1-1.33 1.33H7.33A1.33 1.33 0 0 1 6 12.67V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Bottom buttons -->
        <div class="extraction-detail__bottom-bar">
          <div class="extraction-detail__bottom-left">
            <button type="button" class="extraction-detail__icon-btn" title="Previous page" @click="prevPage">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
            <button type="button" class="extraction-detail__icon-btn" title="Next page" @click="nextPage">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M6 3.5 10.5 8 6 12.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
          </div>
          <div class="extraction-detail__bottom-right">
            <button class="extraction-detail__cancel-btn" type="button">Cancel</button>
            <button class="extraction-detail__confirm-btn" type="button">Confirm</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// Template state
const activeTemplate = ref('all')
const currentTemplateId = ref('vat-invoice')
const selectDialogVisible = ref(false)
const templateSettingVisible = ref(false)
const searchQuery = ref('')

const allTemplates = ref([
  { id: 'vat-invoice', name: 'VAT Invoice' },
  { id: 'purchase-order', name: 'Purchase Order' },
  { id: 'business-contract', name: 'Business Contract' },
  { id: 'employment-contract', name: 'Employment Contract' },
  { id: 'onboarding-form', name: 'Onboarding Form' },
  { id: 'default-invoice', name: 'Default Invoice Template' },
])

// Document list (sidebar)
const selectedFileId = ref<number | null>(6)
const currentPage = ref(1)
const totalPages = ref(3)

const documentList = ref([
  { id: 1, fileName: 'Compdf1234567890.pdf', type: 'Invoice', processingStatus: 'Pending Classification', reviewStatus: '' },
  { id: 2, fileName: 'Compdf1234567891.pdf', type: 'Invoice', processingStatus: 'Classifying', reviewStatus: '' },
  { id: 3, fileName: 'Compdf1234567892.pdf', type: '', processingStatus: 'Classification Failed', reviewStatus: '' },
  { id: 4, fileName: 'Compdf1234567893.pdf', type: 'Invoice', processingStatus: 'Pending Extraction', reviewStatus: '' },
  { id: 5, fileName: 'Compdf1234567894.pdf', type: 'Invoice', processingStatus: 'Extracting', reviewStatus: '' },
  { id: 6, fileName: 'Compdf1234567895.pdf', type: 'Invoice', processingStatus: 'Confirmed', reviewStatus: 'Confirmed' },
  { id: 7, fileName: 'Compdf1234567896.pdf', type: 'Invoice', processingStatus: 'Confirmed', reviewStatus: 'Unconfirmed' },
  { id: 8, fileName: 'Compdf1234567897.pdf', type: 'Invoice', processingStatus: 'Extraction Failed', reviewStatus: '' },
])

const filteredDocuments = computed(() => {
  if (!searchQuery.value) return documentList.value
  const q = searchQuery.value.toLowerCase()
  return documentList.value.filter(doc =>
    doc.fileName.toLowerCase().includes(q) ||
    doc.type.toLowerCase().includes(q)
  )
})

// Result form state
const titleValue = ref('')
const subtitleValue = ref('')
const activeFocus = ref('all')

const focusTypes = ref([
  { id: 'all', label: 'All' },
  { id: 'tables', label: 'Tables' },
  { id: 'key-value', label: 'Key-Value' },
  { id: 'text', label: 'Text' },
])

const extractionFields = ref([
  { id: 1, name: 'Invoice Number', value: 'INV-2024-001' },
  { id: 2, name: 'Date', value: '2024-01-15' },
  { id: 3, name: 'Total Amount', value: '$1,234.56' },
  { id: 4, name: 'Vendor Name', value: 'Acme Corp' },
  { id: 5, name: 'Tax ID', value: '12-3456789' },
])

// Methods
function selectFile(doc: { id: number }) {
  selectedFileId.value = doc.id
  currentPage.value = 1
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++
}

function statusTone(status: string): string {
  const map: Record<string, string> = {
    'Pending Classification': 'is-neutral',
    'Classifying': 'is-warning',
    'Classification Failed': 'is-error',
    'Pending Extraction': 'is-brand',
    'Extracting': 'is-warning',
    'Confirmed': 'is-success',
    'Extraction Failed': 'is-error',
  }
  return map[status] || 'is-neutral'
}

function reviewStatusTone(status: string): string {
  const map: Record<string, string> = {
    'Confirmed': 'is-success',
    'Unconfirmed': 'is-warning',
  }
  return map[status] || 'is-neutral'
}
</script>

<style lang="scss" scoped>
.extraction-detail {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  font-family: 'Encode Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #0c131f;
}

/* ── Header ── */
.extraction-detail__header {
  height: 56px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e8eaed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 0 0 auto;
}

.extraction-detail__header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.extraction-detail__header-title {
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

/* ── 3-Panel Body ── */
.extraction-detail__body {
  flex: 1 1 auto;
  min-height: 0;
  padding: 16px;
  display: flex;
  gap: 8px;
}

/* ── Panel 1: Detail Sidebar (reuses parsing sidebar pattern) ── */
.detail-sidebar {
  width: 230px;
  height: 100%;
  padding: 12px;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 230px;
  color: #0c131f;
}

.detail-sidebar__new-btn-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-sidebar__new-btn {
  flex: 1 1 auto;
  min-width: 0;
  height: 32px;
  border: 0;
  border-radius: 3px;
  background: #396ffa;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #244ff0;
  }
}

.detail-sidebar__settings-btn {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
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

  &:hover {
    border-color: #396ffa;
    color: #396ffa;
  }
}

.detail-sidebar__all-btn {
  width: 100%;
  height: 32px;
  border: 1px solid #396ffa;
  border-radius: 3px;
  background: #fff;
  color: #396ffa;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;

  svg {
    width: 16px;
    height: 16px;
  }

  &.is-active {
    background: #ebf1fe;
  }

  &:hover {
    background: #f5f7ff;
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

.detail-sidebar__divider {
  height: 1px;
  background: #e7e8e8;
  flex: 0 0 auto;
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

.detail-file-item__name {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
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

.detail-file-item__desc {
  margin-left: 20px;
  width: 122px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  line-height: 20px;
}

.detail-file-item__tags {
  margin-left: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-tag {
  display: inline-flex;
  align-items: center;
  padding: 0 16px;
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

/* ── Panel 2: File Preview ── */
.extraction-detail__preview {
  width: 547px;
  flex: 0 0 547px;
  display: flex;
  flex-direction: column;
  border-radius: 6px;
  background: #f3f3f4;
  overflow: hidden;
}

.extraction-detail__preview-header {
  height: 40px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #e7e8e8;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.extraction-detail__back-btn {
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: #0c131f;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #f5f7fa;
  }
}

.extraction-detail__template-select {
  flex: 1 1 auto;
  min-width: 0;

  :deep(.el-select__wrapper) {
    height: 32px;
    border-radius: 3px;
    box-shadow: 0 0 0 1px #dcdde1 inset;
  }
}

.extraction-detail__export-btn {
  height: 32px;
  padding: 0 16px;
  border: 1px solid #396ffa;
  border-radius: 3px;
  background: #fff;
  color: #396ffa;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 0 0 auto;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #ebf1fe;
  }
}

.extraction-detail__preview-content {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f3f4;
}

.extraction-detail__preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(12, 19, 31, 0.4);
  font-size: 14px;
}

.extraction-detail__preview-empty-icon {
  width: 48px;
  height: 48px;
}

.extraction-detail__preview-placeholder {
  color: rgba(12, 19, 31, 0.4);
  font-size: 14px;
}

.extraction-detail__page-nav {
  height: 32px;
  padding: 0 12px;
  background: #fff;
  border-top: 1px solid #e7e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 0 0 auto;
}

.extraction-detail__page-btn {
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: #0c131f;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #f5f7fa;
  }
}

.extraction-detail__page-text {
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  color: #0c131f;
}

/* ── Panel 3: Result Form ── */
.extraction-detail__result {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}

.extraction-detail__result-top-bar {
  padding: 12px 16px 0 16px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex: 0 0 auto;
}

.extraction-detail__add-field-btn {
  height: 32px;
  padding: 0 12px;
  border: 1px solid #396ffa;
  border-radius: 3px;
  background: #fff;
  color: #396ffa;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #ebf1fe;
  }
}

.extraction-detail__result-header {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 auto;
}

.extraction-detail__result-header-label {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  color: #0c131f;
}

.extraction-detail__result-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.extraction-detail__result-select {
  flex: 1 1 auto;
  min-width: 0;

  :deep(.el-select__wrapper) {
    height: 32px;
    border-radius: 3px;
    box-shadow: 0 0 0 1px #dcdde1 inset;
  }
}

.extraction-detail__header-action-btn {
  height: 32px;
  padding: 0 16px;
  border: 1px solid #396ffa;
  border-radius: 3px;
  background: #fff;
  color: #396ffa;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 0 0 auto;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #ebf1fe;
  }
}

.extraction-detail__result-divider {
  height: 1px;
  background: #e7e8e8;
  margin: 0 16px;
  flex: 0 0 auto;
}

.extraction-detail__result-content {
  flex: 1 1 auto;
  min-height: 0;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Focus type buttons */
.extraction-detail__focus-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.extraction-detail__focus-btn {
  height: 32px;
  padding: 0 16px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  background: #f5f7ff;
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;

  &.is-active {
    background: #396ffa;
    border-color: #396ffa;
    color: #fff;
  }

  &:hover:not(.is-active) {
    background: #ebf1fe;
  }
}

/* Page label row */
.extraction-detail__page-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 0 0 auto;
}

.extraction-detail__page-label {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  color: #0c131f;
}

.extraction-detail__zoom-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.extraction-detail__icon-btn {
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

  &:hover {
    background: #f5f7fa;
    border-color: #396ffa;
    color: #396ffa;
  }
}

/* Field groups (Title/Subtitle inputs) */
.extraction-detail__field-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 0 0 auto;
}

.extraction-detail__field-label {
  font-size: 12px;
  line-height: 20px;
  color: rgba(12, 19, 31, 0.4);
}

.extraction-detail__field-input {
  height: 32px;
  padding: 0 12px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  background: #f5f7ff;
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
  outline: none;

  &::placeholder {
    color: rgba(12, 19, 31, 0.4);
  }

  &:focus {
    border-color: #396ffa;
  }
}

/* Add New Field inline button */
.extraction-detail__add-field-inline {
  width: 100%;
  height: 32px;
  border: 1px dashed #396ffa;
  border-radius: 3px;
  background: transparent;
  color: #396ffa;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex: 0 0 auto;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #f5f7ff;
  }
}

/* Table */
.extraction-detail__table-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 auto;
}

.extraction-detail__table-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.extraction-detail__table-title {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  color: #0c131f;
}

.extraction-detail__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  line-height: 22px;
}

.extraction-detail__th {
  padding: 8px 12px;
  background: #f5f7fa;
  color: #5f6b7a;
  font-weight: 500;
  font-size: 14px;
  text-align: left;
  border-bottom: 1px solid #e8eaed;
}

.extraction-detail__th--actions {
  text-align: center;
  width: 80px;
}

.extraction-detail__td {
  padding: 8px 12px;
  color: #0c131f;
  border-bottom: 1px solid #e8eaed;
}

.extraction-detail__td--actions {
  text-align: center;
  width: 80px;
}

.extraction-detail__tr:hover .extraction-detail__td {
  background: #fafbfc;
}

.extraction-detail__table-icon-btn {
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: rgba(12, 19, 31, 0.6);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 14px;
    height: 14px;
  }

  &:hover {
    background: #f5f7fa;
    color: #396ffa;
  }
}

/* Bottom bar */
.extraction-detail__bottom-bar {
  padding: 12px 16px;
  border-top: 1px solid #e7e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 0 0 auto;
}

.extraction-detail__bottom-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.extraction-detail__bottom-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.extraction-detail__cancel-btn {
  height: 32px;
  padding: 0 16px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  background: #fff;
  color: #0c131f;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  cursor: pointer;

  &:hover {
    background: #f5f7fa;
  }
}

.extraction-detail__confirm-btn {
  height: 32px;
  padding: 0 16px;
  border: 0;
  border-radius: 3px;
  background: #396ffa;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  cursor: pointer;

  &:hover {
    background: #244ff0;
  }
}

/* ── Scrollbar ── */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #d0d5dd;
  border-radius: 3px;

  &:hover {
    background: #888c94;
  }
}
</style>