<template>
  <div class="flex flex-col px-20px pt-32px min-w-283px">
    <!-- <a href="/device-management" :class="route.path === '/device-management' ? 'text-brand-2 bg-[#1460F31A]' : 'text-[#0C131F]'" class="py-8px px-12px text-sm font-500 rounded-8px flex items-center cursor-pointer mt-8px">
      <Device class="min-w-20px mr-12px" />
      Device Management
    </a> -->
    <a href="/logs" :class="route.path === '/logs' ? 'text-brand-2 bg-[#1460F31A]' : 'text-[#0C131F]'" class="py-8px px-12px text-sm font-500 rounded-8px flex items-center cursor-pointer mt-8px">
      <Log class="min-w-20px mr-12px" />
      {{ t('logs.title') }}
    </a>
    <a href="/third-party/google-drive-authorization" :class="route.path.includes('/third-party/') ? 'text-brand-2 bg-[#1460F31A]' : 'text-[#0C131F]'" class="py-8px px-12px text-sm font-500 rounded-8px flex items-center cursor-pointer mt-8px">
      <Third class="min-w-20px mr-12px" />
      {{ t('third_party_auth.title') }}
    </a>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const route = useRoute()
const { t } = useI18n()
</script>
