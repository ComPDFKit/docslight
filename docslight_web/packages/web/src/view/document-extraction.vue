<template>
  <div class="document-extraction flex flex-col">
    <!-- List mode: simple title -->
    <template v-if="active === 'list'">
      <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8] sticky top-0 bg-white z-10">{{ t('common.ide') }}</h1>
    </template>
    <DocumentExtractionList ref="listRef" v-show="active === 'list'" />
    <DocumentExtractionResult ref="resultRef" v-show="active === 'result'" />
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent, nextTick, provide, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const DocumentExtractionList = defineAsyncComponent(() => import('../components/document-extraction/List.vue'))
const DocumentExtractionResult = defineAsyncComponent(() => import('../components/document-extraction/Result.vue'))

const { t } = useI18n()
const route = useRoute()
const listRef = ref()
const resultRef = ref()
const active = ref<'list' | 'result'>('list')

interface FileData {
  fileId: string
  fileName: string
  pageCount: number
  uploadTime: string
  fileDownUrl: string
  status: number
  resultDownUrl: string
  reviewStatus: number
  groupTemplateId: string
}

const changeActive = (tab: 'list' | 'result', row?: FileData, template?: string, add?: boolean, isConfigResult?: boolean) => {
  active.value = tab
  if (add) {
    resultRef.value?.defaultCollapse(add)
    return
  }
  if (row) {
    resultRef.value?.openFile(row, template, false, isConfigResult)
  } else {
    if (tab === 'result') {
      resultRef.value?.openFile(row, '', template) 
    } else {
      listRef.value?.refreshTemplateGroup?.()
    }
  }
}

const getTemplateList = async () => {
  return await listRef.value?.getTemplateList()
}

const getTemplateFileList = async () => {
  return await listRef.value?.getTemplateFileList()
}

const toggleSelect = async (id: string) => {
  return await listRef.value?.toggleSelect(id)
}

watch(
  () => route.fullPath,
  async () => {
    if (route.name !== 'document-extraction') return
    active.value = 'list'
    await nextTick()
    await listRef.value?.refreshTemplateGroup?.()
  }
)

provide('changeActive', changeActive)
provide('toggleSelect', toggleSelect)
provide('getTemplateList', getTemplateList)
provide('getTemplateFileList', getTemplateFileList)
</script>

<style lang="scss" scoped>
.document-extraction {
  height: calc(100vh - 56px);
  min-height: 0;
  * {
    font-family: 'Encode Sans';
  }
}
</style>
