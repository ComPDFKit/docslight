<template>
  <div class="relative">
    <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8] bg-white w-[calc(100%-68px)] fixed top-0 z-2">{{ t('common.setting') }}</h1>
    <div class="flex w-full mt-80px">
      <SettingSidebar class="sidebar-fixed" />
      <div class="bg-[#F3F6FF] w-[calc(100%-283px)] ml-283px p-32px min-h-[calc(100vh-80px)]">
        <h2 class="text-20px leading-32px font-600 text-[#0C131F] mb-12px">
          {{ t('third_party_auth.title') }}
        </h2>
        <div class="text-brand-1 text-xs mb-32px">
          {{ t('third_party_auth.description') }}
        </div>
        <div class="px-20px flex mb-32px w-full overflow-x-auto whitespace-nowrap scroll">
          <div @click="router.push('/third-party/google-drive-authorization')" :class="activeProvider === 'google_drive' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Google class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.google_drive') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.google_drive?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="router.push('/third-party/aws-authorization')" :class="activeProvider === 'aws_oss' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Aws class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.aws') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.aws_oss?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="router.push('/third-party/nas-authorization')" :class="activeProvider === 'nas_smb' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Nas class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.nas') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.nas_smb?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="router.push('/third-party/notion-authorization')" :class="activeProvider === 'notion' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Notion class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.notion') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.notion?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="router.push('/third-party/trello-authorization')" :class="activeProvider === 'trello' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Trello class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.trello') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.trello?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="router.push('/third-party/gmail-authorization')" :class="activeProvider === 'gmail' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Gmail class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.gmail') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.gmail?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="router.push('/third-party/google-cloud-storage-authorization')" :class="activeProvider === 'gcs' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Gcs class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.gcs') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.gcs?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
        </div>
        <NotionParty />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { onMounted, provide, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthorizationStatus } from '../../composables/useAuthorizationStatus'
import type { ProviderKey } from '../../composables/useAuthorizationStatus'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const { authorizationList, getAuthorizationStatus } = useAuthorizationStatus()

const routeToProvider: Record<string, ProviderKey> = {
  'google-drive-authorization': 'google_drive',
  'aws-authorization': 'aws_oss',
  'nas-authorization': 'nas_smb',
  'notion-authorization': 'notion',
  'trello-authorization': 'trello',
  'gmail-authorization': 'gmail',
  'google-cloud-storage-authorization': 'gcs'
}

const activeProvider = computed(() => routeToProvider[route.name as string] ?? 'google_drive')

onMounted(() => {
  getAuthorizationStatus()
})

provide('getAuthorizationStatus', getAuthorizationStatus)
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

.scroll {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
    display: unset;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background: #C2C2C2;
  }

  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }

  &::-webkit-scrollbar-thumb {
    display: unset;
  }

  &::-webkit-scrollbar-track {
    display: unset;
  }
}
</style>
