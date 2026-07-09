<template>
  <div class="document-parsing flex flex-col">
    <DocumentParsingList v-show="active === 'list'" mode="page" />
    <DocumentParsingResult ref="resultRef" v-show="active === 'result'" />
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent, provide, ref } from 'vue'

const DocumentParsingList = defineAsyncComponent(() => import('../components/document-parsing/List.vue'))
const DocumentParsingResult = defineAsyncComponent(() => import('../components/document-parsing/Result.vue'))

const resultRef = ref()
const active = ref<'list' | 'result'>('list')

interface FileData {
  fileId: string
  fileName: string
  pageCount: number
  uploadTime: string
  fileDownUrl: string
}

const changeActive = (tab: 'list' | 'result', row?: FileData) => {
  active.value = tab
  if (row) {
    resultRef.value?.openFile(row)
  }
}
provide('changeActive', changeActive)
</script>

<style lang="scss" scoped>
.document-parsing * {
  font-family: 'Encode Sans';
}
</style>
