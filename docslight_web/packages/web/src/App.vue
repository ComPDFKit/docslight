<script lang="ts" setup>
import cn from "element-plus/es/locale/lang/zh-cn" // Element Plus 中文包
import en from "element-plus/es/locale/lang/en" // Element Plus 英文包
import tw from "element-plus/es/locale/lang/zh-tw" // Element Plus 繁体中文包
import ja from "element-plus/es/locale/lang/ja" // Element Plus 日语包
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'

const { locale } = useI18n()

// 自定义分页文案覆盖
const paginationOverride = {
  goto: '前往',
  pagesize: '筆/頁',
  total: '共 {total} 筆',
  pageClassifier: '頁',
  page: '頁',
  prev: '上一頁',
  next: '下一頁',
  currentPage: '第 {pager} 筆',
  prevPages: '向前 {pager} 筆',
  nextPages: '向後 {pager} 筆',
}

const overrideLocale = (base: typeof cn) => ({
  ...base,
  el: {
    ...base.el,
    pagination: { ...base.el.pagination, ...paginationOverride }
  }
})

const localeMap = {
  'zh-cn': cn,
  'en': en,
  'zh-tw': overrideLocale(tw),
  'ja': ja
}
const lan = computed(() => localeMap[locale.value as keyof typeof localeMap] ?? en)
</script>
<template>
  <el-config-provider :locale="lan">
    <router-view />
  </el-config-provider>
</template>
