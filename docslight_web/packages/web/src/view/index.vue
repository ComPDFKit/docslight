<template>
  <div class="pt-100px pl-60px bg-[#F3F6FF] min-h-100vh w-full">
    <h1 class="text-44px leading-56px font-600">{{ t('home.title') }}</h1>
    <div class="text-20px leading-28px text-brand-1 mt-16px mb-48px">{{ t('home.desc') }}</div>
    <div class="flex flex-wrap gap-20px pr-20px" :class="[visibleCardCount === 2 && 'max-w-820px', [3, 4].includes(visibleCardCount) && 'max-w-1300px']">
      <a v-if="permission.extraction" class="card" :style="{ width: cardWidth }" :href="permission.extraction ? '/document-extraction' : 'javascript:;'" @click="!permission.extraction && (store.showPermissionDialog = true)">
        <img src="/images/ide.png" alt="ide">
        <div class="text-tiny font-600 mt-20px mb-4px">{{ t('home.cardOne.title') }}</div>
        <div class="text-xs text-brand-1">{{ t('home.cardOne.desc') }}</div>
        <Locked v-if="!permission.extraction" class="absolute right-8px bottom-8px" />
      </a>
      <a v-if="permission.qa" class="card" :style="{ width: cardWidth }" :href="permission.qa ? '/knowledge-base' : 'javascript:;'" @click="!permission.qa && (store.showPermissionDialog = true)">
        <img src="/images/akb.png" alt="akb">
        <div class="text-tiny font-600 mt-20px mb-4px">{{ t('home.cardTwo.title') }}</div>
        <div class="text-xs text-brand-1">{{ t('home.cardTwo.desc') }}</div>
        <Locked v-if="!permission.qa" class="absolute right-8px bottom-8px" />
      </a>
      <a v-if="permission.layout" class="card" :style="{ width: cardWidth }" :href="permission.layout ? '/document-parsing' : 'javascript:;'" @click="!permission.layout && (store.showPermissionDialog = true)">
        <img src="/images/idp.png" alt="idp">
        <div class="text-tiny font-600 mt-20px mb-4px">{{ t('home.cardThree.title') }}</div>
        <div class="text-xs text-brand-1">{{ t('home.cardThree.desc') }}</div>
        <Locked v-if="!permission.layout" class="absolute right-8px bottom-8px" />
      </a>
      <a v-if="permission.split" class="card" :style="{ width: cardWidth }" :href="permission.split ? '/document-splitting' : 'javascript:;'" @click="!permission.split && (store.showPermissionDialog = true)">
        <img src="/images/splitting.png" alt="splitting">
        <div class="text-tiny font-600 mt-20px mb-4px">{{ t('splitting.title') }}</div>
        <div class="text-xs text-brand-1">{{ t('splitting.desc') }}</div>
        <Locked v-if="!permission.split" class="absolute right-8px bottom-8px" />
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import Locked from "../components/images/Locked.vue"
import { useStore, usePermissionStore } from '../stores'
const permissionStore = usePermissionStore()

const { t } = useI18n()
const store = useStore()

const permission = computed(() => permissionStore.pagePermission)

const visibleCardCount = computed(() => {
  let count = 3
  if (permission.value.extraction) count++
  if (permission.value.qa) count++
  if (permission.value.layout) count++
  if (permission.value.split) count++
  return count
})

// 根据可见卡片数量动态计算宽度（gap 为 20px）
const cardWidth = computed(() => {
  const count = visibleCardCount.value
  if (count <= 1) return '33%'
  if (count === 2) return 'calc(50% - 10px)'
  if (count === 3) return 'calc(33.33% - 14px)'
  return 'calc(25% - 15px)'
})
</script>

<style lang="scss" scoped>
.pt-100px {
  font-family: 'Encode Sans';
}
.card {
  padding: 16px;
  min-width: 387px;
  border-radius: 8px;
  position: relative;
  padding-bottom: 32px;
  background-color: #fff;
  border: 1px solid transparent;
  &:hover {
    border-color: #CDDBFF;
    box-shadow: 0px 4px 35px 0px #0029921A;
    .text-tiny {
      color: #396FFA;
    }
  }
}
:deep(.el-dialog) {
  padding: 40px 32px;
  border-radius: 10px;
}
</style>
