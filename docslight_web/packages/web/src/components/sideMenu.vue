<template>
  <div :class="isCollapse && 'active'" class="flex flex-col justify-start h-100vh overflow-auto">
    <div class="logo flex items-center font-700 text-brand-0 text-24px leading-24px ml-20px mb-28px mt-32px title">
      <Logo v-show="!isCollapse" />
      <img src="/logo.svg" alt="Logo" width="36" height="36" v-show="isCollapse">
    </div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical-demo"
        :collapse="isCollapse"
        mode="vertical" router
        :collapse-transition="false"
      >
        <el-menu-item v-if="permission.extraction" :index="permission.extraction ? '/document-extraction' : ''" @click="showPermissionDialog(!permission.extraction)">
          <Extraction class="min-w-20px" />
          <template #title>{{ t('common.de') }}</template>
        </el-menu-item>
        <el-menu-item v-if="permission.layout" :index="permission.layout ? '/document-parsing' : ''" @click="showPermissionDialog(!permission.layout)">
          <Parsing class="min-w-20px" />
          <template #title>{{ t('common.dp') }}</template>
        </el-menu-item>
        <el-menu-item index="/settings">
          <Setting class="min-w-20px" />
          <template #title>{{ t('common.setting') }}</template>
        </el-menu-item>
      </el-menu>
    </el-scrollbar>
  </div>
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { onBeforeUnmount, onMounted, ref, computed } from 'vue'
import Logo from './images/Logo.vue'
import Extraction from './images/Extraction.vue'
import Parsing from './images/Parsing.vue'
import Setting from './images/Setting.vue'
import { useStore, usePermissionStore } from '../stores'
import { addAdminRoutes } from '../router'
import { applyFixedAuthState, getFixedUserSettings } from '../utils/mockAuth'

const { t, locale } = useI18n()
const route = useRoute()
const store = useStore()
const permissionStore = usePermissionStore()

const language = ref('')
const logoutShow = ref(false)
const languageShow = ref(false)
const permission = computed(() => permissionStore.pagePermission)

defineProps({
  isCollapse: Boolean
})
const changeInfo = () => {
  getUserInfo()
}
defineExpose({
  changeInfo,
  logoutShow,
  languageShow
})
const userInfo = ref<Record<string, any> | null>(null)
const getUserInfo = async () => {
  userInfo.value = applyFixedAuthState(store, permissionStore)
  if (['admin', 'manager'].includes(store.role)) {
    addAdminRoutes()
  }
  const supportedLocales = ['zh-cn', 'zh-tw', 'en']
  const lang = navigator.language.toLocaleLowerCase()
  const defaultLocale = supportedLocales.includes(lang) ? lang : 'en'
  const langMap = {
    'Chinese': 'zh-cn',
    'English': 'en',
    'Traditional Chinese': 'zh-tw',
    'Japanese': 'ja'
  }
  const settings = getFixedUserSettings()
  localStorage.setItem('locale', localStorage.getItem('locale') || langMap[settings.language as keyof typeof langMap] || defaultLocale)
  locale.value = localStorage.getItem('locale') || langMap[settings.language as keyof typeof langMap] || defaultLocale
  language.value = localStorage.getItem('locale') as string
}
onMounted(async () => {
  await getUserInfo()
  addEventListener('click', handleClick)
})
onBeforeUnmount(() => {
  removeEventListener('click', handleClick)
})
const handleClick = () => {
  logoutShow.value = false
  languageShow.value = false
}
/** 判断权限弹窗 */
const showPermissionDialog = (val: boolean) => {
  val && (store.showPermissionDialog = true)
}
</script>

<style lang="scss" scoped>
.shadows {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
.transitions {
  transform: rotateZ(-90deg);
  &.transition {
    transform: rotateZ(90deg);
  }
}

.el-scrollbar {
  height: 100%;
  min-height: 500px;
  :deep(.scrollbar-wrapper) {
    // 限制水平宽度
    overflow-x: hidden;
    .el-scrollbar__view {
      height: 100%;
      display: flex;
      margin: 0 16px;
      flex-direction: column;
      justify-content: space-between;
      .el-menu {
        border: none;
        .el-menu-item {
          height: auto;
          font-size: 16px;
          font-weight: 600;
          padding: 8px 12px;
          line-height: 24px;
          color: #232748;
          border-radius: 6px;
          font-family: 'Encode Sans';
          svg {
            margin-right: 18px;
          }
          &.el-menu-item {
            margin-top: 8px;
          }
          &:hover {
            color: #396FFA;
            background-color: #1460F31A;
          }
          &.is-active {
            color: #396FFA;
            background-color: #1460F31A;
            &::before {
              display: none;
            }
          }
          span {
            font-family: "Encode Sans";
          }
        }
      }
    }
  }
  // 滚动条
  :deep(.el-scrollbar__bar) {
    &.is-horizontal {
      // 隐藏水平滚动条
      display: none;
    }
  }
}
.active {
  width: 68px;
  .logo {
    svg {
      margin-right: 0;
    }
    span {
      display: none;
    }
  }
  .avatar {
    padding: 0;
    display: flex;
    padding: 8px 4px;
    justify-content: center;
    .name {
      display: none;
    }
    .img {
      width: 32px;
      margin: 2px 0;
      margin-left: 3px;
      margin-right: 0;
    }
  }
}
</style>
