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
          <div @click="active = 'google_drive'" :class="active === 'google_drive' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Google class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.google_drive') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.google_drive?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="active = 'aws_oss'" :class="active === 'aws_oss' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Aws class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.aws') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.aws_oss?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="active = 'nas_smb'" :class="active === 'nas_smb' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Nas class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.nas') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.nas_smb?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="active = 'notion'" :class="active === 'notion' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Notion class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.notion') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.notion?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="active = 'trello'" :class="active === 'trello' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Trello class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.trello') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.trello?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="active = 'gmail'" :class="active === 'gmail' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Gmail class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.gmail') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.gmail?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
          <div @click="active = 'gcs'" :class="active === 'gcs' ? 'border-brand-2 text-[#2E59CA]' : 'border-transparent text-[#404653]'" class="flex items-center text-18px leading-28px font-600 py-8px mr-32px border-b-3 cursor-pointer hover:text-[#2E59CA]">
            <Gcs class="mr-8px min-w-20px" />
            {{ t('third_party_auth.providers.gcs') }}
            <el-tooltip popper-class="box-item" effect="dark" :content="t('third_party_auth.common.authorized')" placement="top">
              <Authorization v-show="authorizationList.gcs?.is_active" class="ml-8px min-w-16px" />
            </el-tooltip>
          </div>
        </div>
        <GoogleParty v-show="active === 'google_drive'" />
        <AwsParty v-show="active === 'aws_oss'" />
        <NasParty v-show="active === 'nas_smb'" />
        <NotionParty v-show="active === 'notion'" />
        <TrelloParty v-show="active === 'trello'" />
        <GmailParty v-show="active === 'gmail'" />
        <GcsParty v-show="active === 'gcs'" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { get } from '../utils/request'
import { ref, onMounted, provide } from 'vue'

type ProviderKey = 'google_drive' | 'aws_oss' | 'nas_smb' | 'notion' | 'trello' | 'gmail' | 'gcs'

interface ConnectionItem {
  source: string
  is_active: boolean
}

interface AuthorizationItem {
  source: ProviderKey
  is_active: boolean
}

const { t } = useI18n()
const active = ref<ProviderKey>('google_drive')
const authorizationList = ref<Partial<Record<ProviderKey, AuthorizationItem>>>({})

const isProviderKey = (source: string): source is ProviderKey => {
  return ['google_drive', 'aws_oss', 'nas_smb', 'notion', 'trello', 'gmail', 'gcs'].includes(source)
}

const getAuthorizationStatus = async () => {
  const { data: { data  } } = await get('/v1/dms/auth/credentials')
  authorizationList.value = {}
  data.connections.forEach((element: ConnectionItem) => {
    if (!isProviderKey(element.source)) return

    authorizationList.value[element.source] = {
      source: element.source,
      is_active: element.is_active
    }
  })
}

onMounted(async () => {
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
