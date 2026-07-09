<template>
  <div class=" bg-white p-32px min-h-[calc(100vh-88px)] min-w-240px">
    <div class="fixed w-176px">
      <img v-if="kbDetail?.avatar" :src="kbDetail?.avatar" alt="avatar" class="mx-auto w-48px h-48px">
      <div v-else class="p-12px bg-[#1460F31A] rounded-6px mx-auto w-fit text-brand-2"><Kb /></div>
      <div class="truncate border-b border-[#E1E3E8] text-center pt-24px pb-20px text-sm font-600 text-brand-0">
        {{ kbDetail?.name }}
      </div>
      <a :href="'/knowledge-base/configuration?id=' + route.query.id" class="py-8px px-12px rounded-6px text-brand-0 flex items-center font-600 mt-20px"
        :class="route.path.includes('/knowledge-base/configuration') && 'bg-[#1460F31A] text-brand-2'">
        <Configuration class="mr-18px" />
        {{ t('knowledgeBases.configuration.title') }}
      </a>
      <a :href="'/knowledge-base/dataset?id=' + route.query.id" class="py-8px px-12px rounded-6px text-brand-0 flex items-center font-600 mt-8px"
        :class="route.path.includes('/knowledge-base/dataset') && 'bg-[#1460F31A] text-brand-2'">
        <Knowledge class="mr-18px min-w-20px" />
        {{ t('knowledgeBases.dataset.title') }}
      </a>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import Knowledge from './images/Knowledge.vue'
import Configuration from './images/Configuration.vue'

const { t } = useI18n()
const route = useRoute()
defineProps({
  kbDetail: Object
})
</script>