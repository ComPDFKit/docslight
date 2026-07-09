<template>
  <div class="px-32px py-26px flex items-center bg-white border-b border-[#E1E3E8] fixed w-[calc(100vw-68px)] z-3">
    <div class="text-tiny text-brand-0 mr-12px">
      {{ t('common.kb') }}
    </div>
    <div class="flex rounded-8px text-xs p-4px bg-[#EBF1FE]">
      <a href="/knowledge-base" class="flex px-12px py-4px items-center rounded-4px cursor-pointer"
        :class="route.path.includes('/knowledge-base') && 'bg-white'">
        <KbKnowledge class="mr-8px" />
        {{ t('knowledgeBases.chat.kbManage') }}
      </a>
      <a v-permission="'kb:qa'" href="/chat" class="flex px-12px py-4px items-center rounded-4px cursor-pointer"
        :class="route.path.includes('/chat') && 'bg-white'">
        <KbChat class="mr-8px" />
        {{ t('knowledgeBases.chat.title') }}
      </a>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import KbChat from "./images/KbChat.vue"
import KbKnowledge from "./images/KbKnowledge.vue"

const { t } = useI18n()
const route = useRoute()
</script>