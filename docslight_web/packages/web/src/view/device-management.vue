<template>
  <div class="relative">
    <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8] bg-white w-[calc(100%-68px)] fixed top-0 z-2">{{ t('common.setting') }}</h1>
    <div class="flex w-full mt-80px">
      <SettingSidebar class="sidebar-fixed" />
      <div class="bg-[#F3F6FF] w-[calc(100%-283px)] ml-283px p-32px min-h-[calc(100vh-80px)]">
        <h2 class="text-20px leading-32px font-600 text-[#0C131F] mb-12px">
          {{ t('third_party_auth.title') }}
        </h2>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { get } from '../utils/request'

const { t } = useI18n()

onMounted(async () => {
  const { data: { data } } = await get('/v1/scanner/devices')
  console.log('Device data:', data)
})
</script>

<style lang="scss" scoped>
.sidebar-fixed {
  position: fixed;
  left: 68px;
  top: 80px;
  width: 283px;
  height: calc(100vh - 80px);
  background: #fff;
}
</style>
