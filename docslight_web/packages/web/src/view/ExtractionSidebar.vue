<template>
  <div class="extraction-sidebar flex flex-col h-full bg-white">
    <!-- Top section: back button "Extraction Details" + Filter -->
    <div class="extraction-sidebar__top flex items-center justify-between">
      <button
        class="flex items-center text-14px leading-20px text-[#0C131F] cursor-pointer bg-transparent border-none p-0 hover:opacity-70"
        type="button"
        @click="$emit('back')"
      >
        <svg class="w-16px h-16px mr-8px shrink-0" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ t('extraction.extractionDetails') }}
      </button>
      <div class="extraction-sidebar__filter-wrap">
        <button
          class="extraction-sidebar__filter-btn w-32px h-32px border border-[#DCDFE6] rounded-3px flex items-center justify-center cursor-pointer bg-transparent hover:border-[#396FFA] text-[#94969D] shrink-0"
          :class="filterPanelVisible && 'is-active'"
          type="button"
          @click.stop="filterPanelVisible = !filterPanelVisible"
        >
          <svg class="w-16px h-16px" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path d="M2.5 3.5h11L9.5 8v4l-3 1V8L2.5 3.5Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
          </svg>
        </button>

        <div v-show="filterPanelVisible" class="sidebar-filter-popover" @click.stop>
          <div class="sidebar-filter-popover__content">
            <div class="sidebar-filter-popover__title">{{ t('extraction.filter') }}</div>

            <label class="sidebar-filter-popover__label">{{ t('extraction.processingStatus') }}</label>
            <el-select
              v-model="draftFilters.statuses"
              class="sidebar-filter-select"
              multiple
              collapse-tags
              clearable
              popper-class="sidebar-filter-select-dropdown"
              :placeholder="t('extraction.allStatus')"
            >
              <el-option v-for="item in processingStatusOptions" :key="item.value" :label="t(item.labelKey)" :value="item.value" />
            </el-select>

            <label class="sidebar-filter-popover__label">{{ t('extraction.time') }}</label>
            <el-popover
              v-model:visible="uploadTimePanelVisible"
              placement="right-start"
              popper-class="dateTip sidebar-date-calendar-popper"
              trigger="click"
              append-to-body
            >
              <template #reference>
                <button class="sidebar-filter-time-trigger" type="button">
                  <svg class="sidebar-filter-time-trigger__icon" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                    <path d="M3 3.5h10v10H3v-10Z" stroke="currentColor" stroke-width="1.3"/>
                    <path d="M5 2.5v2M11 2.5v2M3 6h10" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                  </svg>
                  <span>{{ (singleDate || doubleDate.length) ? (singleDate || `${doubleDate[0]} ~ ${doubleDate[1]}`.replace(/-/g, '/')) : t('extraction.selectDate') }}</span>
                </button>
              </template>
              <div class="date sidebar-date-calendar-wrap">
                <div class="date-title">{{ t('extraction.filter') }}</div>
                <div class="tag-content">
                  <div @click="changeDateType('less')" :class="dateType === 'less' && 'active'" class="date-tag">{{ t('extraction.earlierThan') }}</div>
                  <div @click="changeDateType('more')" :class="dateType === 'more' && 'active'" class="date-tag">{{ t('extraction.laterThan') }}</div>
                  <div @click="changeDateType('equal')" :class="dateType === 'equal' && 'active'" class="date-tag">{{ t('extraction.equalTo') }}</div>
                  <div @click="changeDateType('between')" :class="dateType === 'between' && 'active'" class="date-tag">{{ t('extraction.between') }}</div>
                </div>
                <div class="select">
                  {{ t('extraction.date') }}
                  <div class="input" @click="handleDateSelectorClick">
                    {{ (singleDate || doubleDate.length) ? (singleDate || `${doubleDate[0]} ~ ${doubleDate[1]}`.replace(/-/g, '/')) : t('extraction.selectDate') }}
                    <Arrow class="transform -rotate-90" />
                  </div>
                </div>
                <Calender
                  v-show="double"
                  :userFirstLogin="userFirstLogin"
                  @checkedDate="checkedDate"
                />
                <SingleCalendar
                  v-show="single"
                  :userFirstLogin="userFirstLogin"
                  @singleCheckedDate="singleCheckedDate"
                />
                <div class="bottom">
                  <div @click="confirmFilters" class="ok">{{ t('extraction.ok') }}</div>
                  <div @click="resetDateFiltersOnly" class="clear">{{ t('template.reset') }}</div>
                </div>
              </div>
            </el-popover>

            <label class="sidebar-filter-popover__label">{{ t('extraction.type') }}</label>
            <el-select
              v-model="draftFilters.groupTemplateIds"
              class="sidebar-filter-select"
              multiple
              collapse-tags
              clearable
              popper-class="sidebar-filter-select-dropdown"
              :placeholder="t('extraction.allStatus')"
            >
              <el-option :label="t('extraction.allStatus')" value="__all__" />
              <el-option v-for="item in templateOptions" :key="item.groupTemplateId || item.templateId || item.templateName" :label="item.templateName" :value="item.groupTemplateId" />
            </el-select>
          </div>

          <div class="sidebar-filter-popover__actions">
            <button class="sidebar-filter-popover__confirm" type="button" @click="confirmFilters">{{ t('extraction.confirm') }}</button>
            <button class="sidebar-filter-popover__reset" type="button" @click="resetFilters">{{ t('template.reset') }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Divider -->
    <div class="extraction-sidebar__line" />

    <!-- Status tabs: All / Confirmed / Unconfirmed -->
    <div class="extraction-sidebar__tabs-wrap">
      <div class="extraction-sidebar__tabs flex items-center gap-4px">
      <button
        type="button"
        class="extraction-sidebar__tab flex items-center justify-center cursor-pointer select-none transition-colors border-none overflow-hidden"
        :class="activeTab === 'all'
          ? 'bg-white text-[#396FFA] font-500'
          : 'bg-transparent text-[#52555F] hover:text-[#396FFA]'"
        @click="$emit('change-tab', 'all')"
      >
        <span class="truncate">{{ t('extraction.allStatus') }}</span>
      </button>
      <button
        type="button"
        class="extraction-sidebar__tab flex items-center justify-center cursor-pointer select-none transition-colors border-none overflow-hidden"
        :class="activeTab === 'confirmed'
          ? 'bg-white text-[#396FFA] font-500'
          : 'bg-transparent text-[#52555F] hover:text-[#396FFA]'"
        @click="$emit('change-tab', 'confirmed')"
      >
        <span class="truncate">{{ t('extraction.confirmed') }}</span>
      </button>
      <button
        type="button"
        class="extraction-sidebar__tab flex items-center justify-center cursor-pointer select-none transition-colors border-none overflow-hidden"
        :class="activeTab === 'unconfirmed'
          ? 'bg-white text-[#396FFA] font-500'
          : 'bg-transparent text-[#52555F] hover:text-[#396FFA]'"
        @click="$emit('change-tab', 'unconfirmed')"
      >
        <span class="truncate">{{ t('extraction.unconfirmed') }}</span>
      </button>
      </div>
    </div>

    <!-- Search input -->
    <div class="extraction-sidebar__search-wrap">
      <label class="extraction-sidebar__search flex items-center gap-8px text-[#94969D]">
        <svg class="w-16px h-16px shrink-0" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
        </svg>
        <input
          v-model="searchText"
          type="text"
          :placeholder="t('template.search')"
          class="flex-1 min-w-0 border-0 outline-none bg-transparent text-14px leading-22px text-[#0C131F] placeholder:text-[#94969D]"
          @input="$emit('search', searchText)"
        />
      </label>
    </div>

    <div class="extraction-sidebar__line" />

    <!-- Document list -->
    <div class="flex-1 overflow-auto py-4px">
      <!-- Loading state -->
      <div v-if="isLoading" class="flex justify-center items-center py-40px text-sm text-[#94969D]">
        {{ t('singleParse.loading') }}
      </div>

      <!-- Empty state -->
      <div v-else-if="!documentList.length" class="flex justify-center items-center py-40px text-sm text-[#94969D]">
        {{ t('extraction.noDocument') }}
      </div>

      <!-- Document items -->
      <template v-else>
        <div
          v-for="doc in documentList"
          :key="doc.fileId || doc.id"
          class="extraction-sidebar__item cursor-pointer rounded-6px border border-transparent hover:bg-[#F3F6FF] hover:border-[#D7E2FE] transition-colors"
          :class="activeDocId === (doc.fileId || doc.id) ? 'bg-[#F3F6FF] border-[#D7E2FE]' : ''"
          @click="$emit('select-doc', doc.fileId || doc.id)"
        >
          <!-- File name row -->
          <div class="extraction-sidebar__file-name flex items-center gap-4px truncate" :class="activeDocId === (doc.fileId || doc.id) ? 'is-active' : ''">
            <svg class="w-16px h-16px shrink-0" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2"/>
              <path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
            </svg>
            <span class="truncate">{{ doc.fileName }}</span>
          </div>
          <!-- Status tags row -->
          <div class="extraction-sidebar__tags flex items-center gap-8px">
            <span class="extraction-sidebar__tag inline-flex items-center rounded-3px" :class="processingStatusClass(doc.status ?? doc.processingStatus)">
              {{ statusText(doc.status ?? doc.processingStatus) || doc.processingStatusText || t('extraction.extractionSuccess') }}
            </span>
            <span class="extraction-sidebar__tag inline-flex items-center rounded-3px" :class="reviewStatusClass(doc)">
              {{ reviewStatusLabel(doc) }}
            </span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const Calender = defineAsyncComponent(() => import('../components/calendar/calendar.vue'))
const SingleCalendar = defineAsyncComponent(() => import('../components/calendar/singleCalendar.vue'))
const { t } = useI18n()

interface SidebarFilterPayload {
  statuses: string[]
  startTime: string
  endTime: string
  groupTemplateIds: string[]
  skipCurrentTemplate?: boolean
}

const props = defineProps<{
  documentList: any[]
  activeDocId?: string | number
  activeTab?: string
  isLoading?: boolean
  totalCount?: number
  confirmedCount?: number
  unconfirmedCount?: number
  templateOptions?: any[]
  currentTemplateId?: string
}>()

const emit = defineEmits<{
  (e: 'select-doc', docId: string | number): void
  (e: 'change-tab', tabName: string): void
  (e: 'filter'): void
  (e: 'back'): void
  (e: 'search', query: string): void
  (e: 'apply-filter', payload: SidebarFilterPayload): void
}>()

const searchText = ref('')
const filterPanelVisible = ref(false)
const uploadTimePanelVisible = ref(false)
const userFirstLogin = ref('2000-01-01T00:00:00')
const templateOptions = computed(() => props.templateOptions || [])
const draftFilters = reactive({
  statuses: [] as string[],
  uploadTime: [] as string[],
  groupTemplateIds: [] as string[]
})
const single = ref(false)
const double = ref(false)
const singleDate = ref('')
const dateType = ref('less')
const doubleDate = ref<string[]>([])

const processingStatusOptions = [
  { labelKey: 'extraction.pendingExtraction', value: '0' },
  { labelKey: 'extraction.extracting', value: '1' },
  { labelKey: 'extraction.extractionSuccess', value: '2' },
  { labelKey: 'extraction.extractionFailed', value: '3' },
  { labelKey: 'extraction.pendingClassification', value: '6' },
  { labelKey: 'extraction.classifying', value: '7' },
  { labelKey: 'extraction.classificationFailed', value: '8' },
  { labelKey: 'extraction.pendingExtraction', value: '9' },
  { labelKey: 'extraction.extracting', value: '10' },
  { labelKey: 'extraction.extractionSuccess', value: '11' },
  { labelKey: 'extraction.extractionFailed', value: '12' }
]

watch(() => draftFilters.groupTemplateIds, (value) => {
  if (!value.includes('__all__')) return
  if (value.length > 1) {
    draftFilters.groupTemplateIds = ['__all__']
  }
}, { deep: true })

const syncCurrentTemplateFilter = (nextTemplateId?: string, prevTemplateId?: string) => {
  if (!nextTemplateId) return
  const currentIds = draftFilters.groupTemplateIds.filter(Boolean)
  const shouldSync = currentIds.length === 0
    || (currentIds.length === 1 && (!!prevTemplateId && currentIds[0] === prevTemplateId))
  if (!shouldSync) return
  draftFilters.groupTemplateIds = [nextTemplateId]
}

watch(() => props.currentTemplateId, (nextValue, prevValue) => {
  syncCurrentTemplateFilter(nextValue, prevValue)
}, { immediate: true })

const buildFilterPayload = (): SidebarFilterPayload => {
  const groupTemplateIds = draftFilters.groupTemplateIds.includes('__all__')
    ? []
    : draftFilters.groupTemplateIds.filter(Boolean)
  let startTime = ''
  let endTime = ''

  if (dateType.value === 'less') {
    endTime = singleDate.value
  } else if (dateType.value === 'more') {
    startTime = singleDate.value
  } else if (dateType.value === 'equal') {
    const [start, end] = singleDate.value.split(' ~ ')
    startTime = start || ''
    endTime = end || ''
  } else {
    startTime = doubleDate.value[0] || ''
    endTime = doubleDate.value[1] || ''
  }

  return {
    statuses: [...draftFilters.statuses],
    startTime,
    endTime,
    groupTemplateIds,
    skipCurrentTemplate: draftFilters.groupTemplateIds.includes('__all__') || groupTemplateIds.length === 0
  }
}

const changeDateType = (type: string) => {
  dateType.value = type
  single.value = false
  double.value = false
  doubleDate.value = []
  if (type === 'between') {
    singleDate.value = ''
  } else {
    doubleDate.value = []
  }
}

const handleDateSelectorClick = () => {
  if (dateType.value === 'between') {
    double.value = true
  } else {
    single.value = true
  }
}

const checkedDate = (dateArr: string[]) => {
  doubleDate.value = [
    `${dateArr[0]}T00:00:00`,
    `${dateArr[1]}T23:59:59`
  ]
  draftFilters.uploadTime = [...doubleDate.value]
  double.value = false
}

const singleCheckedDate = (date: string) => {
  if (dateType.value === 'less') {
    singleDate.value = `${date}T23:59:59`
  } else if (dateType.value === 'more') {
    singleDate.value = `${date}T00:00:00`
  } else if (dateType.value === 'equal') {
    singleDate.value = `${date}T00:00:00 ~ ${date}T23:59:59`
  } else {
    singleDate.value = date
  }
  draftFilters.uploadTime = singleDate.value ? [singleDate.value] : []
  single.value = false
}

const confirmFilters = () => {
  emit('apply-filter', buildFilterPayload())
  filterPanelVisible.value = false
}

const resetFilters = () => {
  draftFilters.statuses = []
  draftFilters.uploadTime = []
  singleDate.value = ''
  doubleDate.value = []
  dateType.value = 'less'
  single.value = false
  double.value = false
  draftFilters.groupTemplateIds = props.currentTemplateId ? [props.currentTemplateId] : []
  emit('apply-filter', buildFilterPayload())
  filterPanelVisible.value = false
}

const resetDateFiltersOnly = () => {
  singleDate.value = ''
  doubleDate.value = []
  draftFilters.uploadTime = []
  uploadTimePanelVisible.value = false
  emit('apply-filter', buildFilterPayload())
}

// Helper functions
function reviewStatusClass(doc: any): string {
  if (!isSuccessfulStatus(doc.status ?? doc.processingStatus)) return 'bg-[#F3F3F4] text-[#94969D]'
  if (doc.reviewStatus === 1) return 'bg-[#ECF9F3] text-[#67D1A0]'
  if (doc.reviewStatus === 0) return 'bg-[#FEF3E6] text-[#F5A13A]'
  return 'bg-[#F3F3F4] text-[#94969D]'
}

function reviewStatusLabel(doc: any): string {
  if (!isSuccessfulStatus(doc.status ?? doc.processingStatus)) return '--'
  if (doc.reviewStatusText) return doc.reviewStatusText
  if (doc.reviewStatus === 1) return t('extraction.confirmed')
  if (doc.reviewStatus === 0) return t('extraction.unconfirmed')
  return '--'
}

function isSuccessfulStatus(status: number | string): boolean {
  const s = Number(status)
  return s === 2 || s === 11
}

function processingStatusClass(status: number | string): string {
  const s = Number(status)
  if (s === 2 || s === 11) return 'bg-[#ECF9F3] text-[#67D1A0]'  // completed / extraction successful
  if (s === 1 || s === 7 || s === 10) return 'bg-[#FEF3E6] text-[#F5A13A]'  // processing
  if (s === 3 || s === 8 || s === 12) return 'bg-[#FBECEC] text-[#D44040]'  // failed
  if (s === 0 || s === 6 || s === 9) return 'bg-[#F5F7FF] text-[#396FFA]'  // pending
  return 'bg-[#F3F3F4] text-[#94969D]'
}

function statusText(status: number | string | undefined): string {
  const map: Record<number, string> = {
    0: t('extraction.pendingExtraction'), 1: t('extraction.extracting'), 2: t('extraction.extractionSuccess'), 3: t('extraction.extractionFailed'),
    5: t('extraction.paused'), 6: t('extraction.pendingClassification'), 7: t('extraction.classifying'),
    8: t('extraction.classificationFailed'), 9: t('extraction.pendingExtraction'),
    10: t('extraction.extracting'), 11: t('extraction.extractionSuccess'), 12: t('extraction.extractionFailed')
  }
  return map[Number(status)] || ''
}
</script>

<style lang="scss" scoped>
.extraction-sidebar {
  width: 230px;
  min-width: 230px;
  padding: 16px 12px;
  border-radius: 6px;
  box-sizing: border-box;
}

.extraction-sidebar__top {
  width: 206px;
  height: 40px;
  padding: 0;
  box-sizing: border-box;
}

.extraction-sidebar__filter-wrap {
  position: relative;
  z-index: 12;
}

.extraction-sidebar__filter-btn {
  &.is-active {
    border-color: #396FFA;
    color: #396FFA;
    background: #F5F7FF;
  }
}

.sidebar-filter-popover {
  position: absolute;
  top: 40px;
  right: 0;
  z-index: 20;
  width: 200px;
  height: 230px;
  padding: 8px;
  border: 1px solid #DCDDE1;
  border-radius: 6px;
  background: #FFFFFF;
  box-sizing: border-box;
  box-shadow: 0 8px 20px rgba(12, 19, 31, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-filter-popover__content {
  width: 184px;
  height: 178px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-filter-popover__title {
  width: 184px;
  height: 22px;
  color: #0C131F;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
}

.sidebar-filter-popover__label {
  width: 184px;
  height: 20px;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  font-weight: 400;
  line-height: 20px;
}

.sidebar-filter-popover__actions {
  width: 184px;
  height: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.sidebar-filter-popover__confirm,
.sidebar-filter-popover__reset {
  height: 24px;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 400;
  line-height: 20px;
  cursor: pointer;
}

.sidebar-filter-popover__confirm {
  width: 58px;
  border: 1px solid #396FFA;
  background: #396FFA;
  color: #FFFFFF;
}

.sidebar-filter-popover__reset {
  width: 46px;
  border: 1px solid #396FFA;
  background: #FFFFFF;
  color: #396FFA;
}

:deep(.sidebar-filter-select) {
  width: 184px;
  height: 24px;

  .el-select__wrapper {
    min-height: 24px;
    height: 24px;
    padding: 2px 8px 2px 4px !important;
    border-radius: 3px;
    box-shadow: 0 0 0 1px #DCDDE1 inset;
    box-sizing: border-box;
    overflow: hidden;
  }

  .el-select__selection {
    width: 150px;
    height: 20px;
    min-height: 20px;
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    margin: 0 !important;
    padding: 0 !important;
    overflow: hidden;
  }

  .el-select__selected-item {
    height: 20px;
    margin: 0 !important;
    padding: 0 !important;
    flex: 0 1 auto;
    min-width: 0;
    max-width: 132px;
    display: inline-flex;
    align-items: center;
  }

  .el-tag {
    max-width: 132px;
    height: 20px;
    margin: 0 !important;
    padding: 0 4px !important;
    border: 0;
    border-radius: 3px;
    background: #E7E8E8;
    color: #0C131F;
    font-size: 12px;
    line-height: 20px;
    display: inline-flex;
    align-items: center;
    box-sizing: border-box;
  }

  .el-tag__content {
    max-width: 106px;
    color: #0C131F;
    font-size: 12px;
    line-height: 20px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .el-tag__close {
    width: 14px;
    height: 14px;
    margin-left: 4px;
    flex: 0 0 auto;
  }

  .el-select__placeholder,
  .el-select__input {
    min-width: 0;
    height: 20px;
    margin: 0 !important;
    padding: 0 !important;
    color: rgba(12, 19, 31, 0.4);
    font-size: 12px;
    line-height: 20px;
  }

  .el-select__input-wrapper {
    min-width: 0;
    height: 20px;
    margin-left: 0 !important;
    padding: 0 !important;
  }

  .el-select__caret {
    width: 14px;
    height: 14px;
    color: rgba(12, 19, 31, 0.4);
  }
}

.sidebar-filter-time-trigger {
  width: 184px;
  height: 24px;
  padding: 2px 8px 2px 4px;
  border: 1px solid #DCDDE1;
  border-radius: 3px;
  background: #FFFFFF;
  color: rgba(12, 19, 31, 0.4);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  span {
    flex: 1;
    min-width: 0;
    color: rgba(12, 19, 31, 0.4);
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    text-align: center;
    white-space: nowrap;
  }
}

.sidebar-filter-time-trigger__icon {
  width: 14px;
  height: 14px;
  flex: 0 0 auto;
  color: rgba(12, 19, 31, 0.4);
}

.sidebar-date-calendar-wrap {
  position: relative;
  min-width: 360px;
}

.sidebar-date-calendar-wrap :deep(.calender-wrap) {
  position: static;
  top: auto;
  right: auto;
}

:global(.el-popover.el-popper.sidebar-date-calendar-popper) {
  width: auto !important;
  min-width: 0 !important;
  padding: 0 !important;
  border: 1px solid #DCDDE1 !important;
  border-radius: 8px !important;
  background: #FFFFFF !important;
  box-shadow: 0 8px 20px rgba(12, 19, 31, 0.08) !important;
}

:global(.el-popover.el-popper.sidebar-date-calendar-popper .el-popper__arrow) {
  display: none !important;
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
        color: #FFFFFF;
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
      color: #FFFFFF;
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

:global(.el-popover.dateTip) {
  padding: 0 !important;
  margin-top: 16px !important;
}

.extraction-sidebar__line {
  width: 206px;
  height: 1px;
  background: #E7E8E8;
  flex: 0 0 auto;
}

.extraction-sidebar__tabs-wrap {
  width: 206px;
  padding-top: 8px;
}

.extraction-sidebar__tabs {
  width: 206px;
  height: 32px;
  padding: 4px;
  border: 1px solid #D7E2FE;
  border-radius: 3px;
  background: #F5F7FF;
  box-sizing: border-box;
}

.extraction-sidebar__tab {
  height: 24px;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  line-height: 20px;
  flex: 0 0 auto;
}

.extraction-sidebar__search-wrap {
  width: 206px;
  padding-top: 8px;
  padding-bottom: 8px;
}

.extraction-sidebar__search {
  width: 206px;
  height: 32px;
  padding: 4px 8px 4px 4px;
  border-radius: 3px;
  background: #F3F3F4;
  box-sizing: border-box;
}

.extraction-sidebar__item {
  width: 206px;
  min-height: 74px;
  padding: 4px 8px;
  margin-bottom: 2px;
  box-sizing: border-box;
}

.extraction-sidebar__file-name {
  width: 190px;
  height: 22px;
  color: rgba(12, 19, 31, 0.6);
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;

  svg {
    color: rgba(12, 19, 31, 0.4);
  }

  &.is-active {
    color: #396FFA;

    svg {
      color: #396FFA;
    }
  }
}

.extraction-sidebar__desc,
.extraction-sidebar__tags {
  margin-left: 20px;
}

.extraction-sidebar__desc {
  width: 122px;
  height: 20px;
  margin-top: 2px;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  line-height: 20px;
}

.extraction-sidebar__tags {
  height: 20px;
  margin-top: 4px;
}

.extraction-sidebar__tag {
  height: 20px;
  padding: 0 4px;
  font-size: 12px;
  line-height: 20px;
  box-sizing: border-box;
}
</style>
