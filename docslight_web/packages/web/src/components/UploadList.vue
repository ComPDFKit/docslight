<template>
  <div
    ref="wrapRef"
    v-show="posReady && (store.totalCount > 0 || keepVisible)"
    class="upload-list-wrap bg-white rounded-8px flex flex-col overflow-hidden transition-[width] duration-200 fixed z-2002"
    :style="{
      width: collapsed ? '220px' : '360px',
      boxShadow: '0px 4px 32px 0px rgba(129,149,200,0.32)',
      left: posX + 'px',
      top: posY + 'px',
    }"
  >
    <!-- Header (drag handle) -->
    <div
      class="h-40px flex items-center justify-between px-16px shrink-0 select-none"
      :class="[collapsed ? 'rounded-8px' : 'rounded-tl-8px rounded-tr-8px', dragging ? 'cursor-grabbing' : 'cursor-grab']"
      style="background: rgba(12,19,31,0.8);"
      @mousedown="onDragStart"
      @click="onHeaderClick"
    >
      <div class="flex items-center gap-4px">
        <span class="text-white text-16px font-500 leading-24px">{{ t('dms.team_space.upload_task.title') }}</span>
        <!-- Red dot badge -->
        <div class="relative shrink-0 w-16px h-16px">
          <div class="absolute inset-0 rounded-full bg-[#F04438] flex items-center justify-center">
            <span class="text-white leading-none font-400" style="font-size: 8px;">
              {{ store.totalCount > 99 ? '99+' : store.totalCount }}
            </span>
          </div>
        </div>
      </div>
      <!-- Arrow: 展开时朝下，折叠时朝上 -->
      <svg
        class="transition-transform duration-200 shrink-0"
        :class="collapsed ? 'rotate-180' : ''"
        width="20" height="20" viewBox="0 0 20 20" fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fill-rule="evenodd" clip-rule="evenodd"
          d="M4.13128 8.00628C4.4467 7.69086 4.94303 7.66659 5.28629 7.93349L5.36872 8.00628L10 12.6369L14.6313 8.00628C14.9467 7.69086 15.443 7.66659 15.7863 7.93349L15.8687 8.00628C16.1841 8.32171 16.2084 8.81803 15.9415 9.16129L15.8687 9.24372L10.6187 14.4937C10.3033 14.8091 9.80697 14.8334 9.46371 14.5665L9.38128 14.4937L4.13128 9.24372C3.78957 8.90201 3.78957 8.34799 4.13128 8.00628Z"
          fill="white"
        />
      </svg>
    </div>

    <!-- Body (collapsible) -->
    <div v-show="!collapsed" class="flex flex-col pb-12px px-12px">
      <!-- Tabs -->
      <div class="flex items-end gap-20px border-b border-[#E1E3E8] mb-12px">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="py-8px text-16px leading-24px cursor-pointer transition-colors shrink-0"
          :class="activeTab === tab.key
            ? 'font-500 text-[#2E59CA] border-b-[3px] border-[#396FFA] -mb-px'
            : 'font-400 text-[#404653] border-b-[3px] border-transparent'"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </div>
      </div>

      <!-- Uploading count + Clear -->
      <div class="flex items-center justify-between text-14px leading-20px font-400 mb-12px">
        <span class="text-[#52555F]">{{ t('dms.team_space.upload_task.uploading') }}({{ filteredUploadingCount }})</span>
        <span
          class="transition-opacity"
          :class="filteredList.length === 0 ? 'text-[#C0C4CC] cursor-not-allowed' : 'text-[#2E59CA] cursor-pointer hover:opacity-80'"
          @click="filteredList.length > 0 && handleClear()"
        >
          {{ t('dms.team_space.upload_task.clear') }}
        </span>
      </div>

      <!-- File list area -->
      <div class="bg-[#F6F6FB] rounded-4px relative overflow-hidden" style="min-height: 64px;">

        <!-- Empty state -->
        <div
          v-if="filteredList.length === 0"
          class="flex flex-col items-center justify-center py-28px gap-8px"
        >
          <Task />
          <span class="text-14px leading-20px text-[#94969D] font-400">{{ t('dms.team_space.upload_task.no_task') }}</span>
        </div>

        <!-- File list -->
        <div
          v-else
          class="upload-file-list p-12px flex flex-col gap-12px overflow-y-auto"
          style="max-height: 220px;"
        >
          <div
            v-for="file in filteredList"
            :key="file.id"
            class="flex items-center w-full gap-8px"
          >
            <!-- Status icon -->
            <div class="shrink-0 size-16px flex items-center justify-center">
              <!-- Success -->
              <svg v-if="file.status === 'success'" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="8" cy="8" r="8" fill="#00CF85"/>
                <path d="M4.5 8.2L6.8 10.5L11.5 5.5" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <!-- Error / uploadFail -->
              <svg v-else-if="file.status === 'fail' || file.status === 'uploadFail'" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="8" cy="8" r="8" fill="#F87171"/>
                <path d="M5.5 5.5L10.5 10.5M10.5 5.5L5.5 10.5" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <!-- Uploading / Processing: spinning arc -->
              <svg v-else-if="file.status === 'uploading' || file.status === 'processing'" class="upload-spin" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="8" cy="8" r="6.5" stroke="#DDE1E8" stroke-width="1.5"/>
                <path d="M8 1.5A6.5 6.5 0 0 1 14.5 8" stroke="#396FFA" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <!-- Pending: clock -->
              <svg v-else-if="file.status === 'pending'" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="8" cy="8" r="6.5" stroke="#94969D" stroke-width="1.2"/>
                <path d="M8 4.5V8.2L10.2 9.5" stroke="#94969D" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <!-- Paused: pause icon -->
              <svg v-else-if="file.status === 'paused'" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="8" cy="8" r="8" fill="#F59E0B"/>
                <path d="M6 5V11" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M10 5V11" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>

            <!-- File name -->
            <span class="flex-1 text-14px leading-20px font-400 text-[#404653] overflow-hidden text-ellipsis whitespace-nowrap min-w-0">
              {{ file.name }}
            </span>

            <!-- Right action -->
            <div class="shrink-0 size-16px flex items-center justify-center">
              <!-- Pause (hidden: feature not implemented yet) -->
              <!-- Delete -->
              <el-tooltip
                v-if="file.status === 'pending' || file.status === 'paused'"
                :content="t('extraction.delete')"
                placement="top"
                popper-class="box-item"
              >
                <svg
                  class="cursor-pointer hover:opacity-70 transition-opacity"
                  @click.stop="store.removeFile(file.id)"
                  width="16" height="16" viewBox="0 0 16 16" fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M2 4H14" stroke="#94969D" stroke-width="1.2" stroke-linecap="round"/>
                  <path d="M5.5 4V2.5C5.5 2.22 5.72 2 6 2H10C10.28 2 10.5 2.22 10.5 2.5V4" stroke="#94969D" stroke-width="1.2" stroke-linecap="round"/>
                  <path d="M3.5 4L4 13.5C4 13.78 4.22 14 4.5 14H11.5C11.78 14 12 13.78 12 13.5L12.5 4" stroke="#94969D" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M6.5 7V11" stroke="#94969D" stroke-width="1.2" stroke-linecap="round"/>
                  <path d="M9.5 7V11" stroke="#94969D" stroke-width="1.2" stroke-linecap="round"/>
                </svg>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUploadTaskStore } from '../stores/uploadTask'

// 直接从全局 store 读取，无需 props 传入
const store = useUploadTaskStore()
const { t } = useI18n()

const collapsed = ref(true)
const activeTab = ref<'all' | 'extraction' | 'parsing'>('all')
/** 清空后保持弹窗可见，展示空状态 */
const keepVisible = ref(false)
let clearTimer: ReturnType<typeof setTimeout> | null = null

// ─── 拖拽相关 ────────────────────────────────────────────────────────────────
const wrapRef = ref<HTMLElement | null>(null)
const posX = ref(0)
const posY = ref(0)
const dragging = ref(false)
/** 位置初始化完成前隐藏，防止刷新时从 (0,0) 闪到目标位置 */
const posReady = ref(false)
/** 是否发生了实际拖拽移动，用于区分点击和拖拽 */
let hasMoved = false
let startMouseX = 0
let startMouseY = 0
let startPosX = 0
let startPosY = 0

/** 初始化位置：右下角，确保不超出视口 */
const initPosition = () => {
  const w = collapsed.value ? 220 : 360
  posX.value = window.innerWidth - w - 60
  // 先设一个底部留 60px 的保守位置，onMounted 后 nextTick clamp 会精确修正
  posY.value = window.innerHeight - 100
  nextTick(clampPosition)
}

/** 将位置约束在视口内 */
const clampPosition = () => {
  const el = wrapRef.value
  // 直接用目标宽度，避免 CSS transition 导致拿到中间值
  const w = collapsed.value ? 220 : 360
  const h = (el && el.offsetHeight > 0) ? el.offsetHeight : (collapsed.value ? 40 : 400)
  const maxX = window.innerWidth - w
  const maxY = window.innerHeight - h
  posX.value = Math.max(0, Math.min(posX.value, maxX))
  posY.value = Math.max(0, Math.min(posY.value, maxY))
}

const onDragStart = (e: MouseEvent) => {
  // 仅响应鼠标左键
  if (e.button !== 0) return
  dragging.value = true
  hasMoved = false
  startMouseX = e.clientX
  startMouseY = e.clientY
  startPosX = posX.value
  startPosY = posY.value
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
}

const onDragMove = (e: MouseEvent) => {
  const dx = e.clientX - startMouseX
  const dy = e.clientY - startMouseY
  if (!hasMoved && (Math.abs(dx) > 3 || Math.abs(dy) > 3)) {
    hasMoved = true
  }
  if (hasMoved) {
    posX.value = startPosX + dx
    posY.value = startPosY + dy
    clampPosition()
  }
}

const onDragEnd = () => {
  dragging.value = false
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
}

/** Header 点击：仅在没有拖拽移动时才切换折叠 */
const onHeaderClick = () => {
  if (!hasMoved) {
    toggleCollapse()
  }
}

onMounted(() => {
  initPosition()
  posReady.value = true
  window.addEventListener('resize', clampPosition)
})

onUnmounted(() => {
  if (clearTimer) clearTimeout(clearTimer)
  window.removeEventListener('resize', clampPosition)
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
})

const tabs = computed(() => [
  { key: 'all' as const,        label: t('dms.team_space.upload_task.tabs.all') },
  { key: 'extraction' as const, label: t('dms.team_space.upload_task.tabs.extraction') },
  { key: 'parsing' as const,    label: t('dms.team_space.upload_task.tabs.parsing') },
])

const filteredList = computed(() => store.getByType(activeTab.value))

const filteredUploadingCount = computed(() =>
  filteredList.value.filter(f => f.status === 'uploading' || f.status === 'processing').length
)

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
  // 宽度变化后重新约束位置
  nextTick(clampPosition)
}

/** 清空当前 Tab 的文件，展示空状态 2 秒后再隐藏弹窗 */
const handleClear = () => {
  // 按当前 Tab 清除
  store.clearByType(activeTab.value)

  // 如果整个列表已空，启动 2 秒延迟隐藏
  if (store.totalCount === 0) {
    // 确保弹窗展开以展示空状态
    collapsed.value = false
    keepVisible.value = true
    if (clearTimer) clearTimeout(clearTimer)
    clearTimer = setTimeout(() => {
      keepVisible.value = false
      clearTimer = null
    }, 2000)
  }
}

/** 暂停文件的抽取/解析处理 */
const handlePause = async (fileId: string) => {
  const ok = await store.pauseFile(fileId)
  if (ok) {
    ElMessage.success(t('extraction.pauseSuccess'))
  }
}

// 有新文件进来时：展开弹窗、切回"全部"tab、取消延迟隐藏
watch(() => store.totalCount, (count, oldCount) => {
  if (count > 0 && keepVisible.value) {
    keepVisible.value = false
    if (clearTimer) { clearTimeout(clearTimer); clearTimer = null }
  }
  // 新文件加入时，自动展开并高亮"全部"tab
  if (count > oldCount) {
    collapsed.value = false
    activeTab.value = 'all'
    nextTick(clampPosition)
  }
})
</script>

<style lang="scss" scoped>
.upload-list-wrap {
  font-family: 'Encode Sans', sans-serif;
}

@keyframes upload-spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.upload-spin {
  animation: upload-spin 1s linear infinite;
  transform-origin: center;
}

.upload-file-list {
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background: rgba(0, 0, 0, 0.15);
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
}
</style>
