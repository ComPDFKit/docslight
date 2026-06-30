<script setup lang="ts">
import { ref, provide, onMounted } from 'vue'
import SideMenu from '../components/sideMenu.vue'
import TopUserMenu from '../components/TopUserMenu.vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useStore } from '../stores'
import Lock from "../components/images/Lock.vue"
import UploadList from '../components/UploadList.vue'
import { useUploadTaskStore } from '../stores/uploadTask'

const route = useRoute()
const { t } = useI18n()
const store = useStore()
const uploadTaskStore = useUploadTaskStore()

// 页面加载时将因刷新中断的 uploading 条目标记为 uploadFail
onMounted(() => {
  uploadTaskStore.fixInterruptedOnLoad()
})

const sideMenu = ref()
const topUserMenu = ref()
const width = ref('68px')
const isCollapse = ref(true)
const menuLocked = ref(false)
const changeMenu = () => {
  sideMenu.value.changeInfo()
  topUserMenu.value?.changeInfo()
}
const changeWidth = () => {
  if (menuLocked.value) return
  width.value = '68px'
  isCollapse.value = true
  sideMenu.value.logoutShow = false
  sideMenu.value.languageShow = false
}
const expandWidth = () => {
  width.value = '285px'
  isCollapse.value = false
}
const setMenuExpanded = (expanded: boolean, locked = false) => {
  menuLocked.value = locked
  if (expanded) {
    expandWidth()
  } else {
    width.value = '68px'
    isCollapse.value = true
    sideMenu.value && (sideMenu.value.logoutShow = false)
    sideMenu.value && (sideMenu.value.languageShow = false)
  }
}
provide('changeMenu', changeMenu)
provide('setMenuExpanded', setMenuExpanded)
</script>

<template>
  <el-container>
    <el-aside class="aside border-r border-[#E1E3E8]"
      @mouseenter="expandWidth"
      @mouseleave="changeWidth" :width="width">
      <SideMenu ref="sideMenu" :isCollapse="isCollapse" />
    </el-aside>
    <el-main class="collapse" :style="{ marginLeft: width, width: `calc(100vw - ${width})` }">
      <TopUserMenu v-if="!route.meta?.hideTopUserMenu" ref="topUserMenu" />
      <router-view ref="routerRef" />
    </el-main>
    
    <el-dialog v-model="store.showPermissionDialog" width="454px" align-center :show-close="false">
      <div class="flex flex-col justify-center items-center">
        <Lock />
        <div class="text-brand-0 text-base font-600 my-16px">{{ t('home.lock.title') }}</div>
        <div class="text-brand-3 mb-32px font-500">{{ t('home.lock.desc') }}</div>
        <div @click="store.showPermissionDialog = false" class="min-w-200px justify-center rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center font-500 hover:bg-[#244FF0]">
          {{ t('home.lock.ok') }}
        </div>
      </div>
    </el-dialog>

    <!-- 全局上传任务浮窗，可拖拽，所有页面共享 -->
    <UploadList />
  </el-container>
</template>
<style lang="scss" scoped>
.aside {
  z-index: 2001;
  position: fixed;
  background-color: #fff;
  transition: width 0.2s ease-in-out;
}
.collapse {
  min-height: 100vh;
  margin-left: 68px;
  width: calc(100vw - 68px);
  padding: 0;
  transition: margin-left 0.2s ease-in-out, width 0.2s ease-in-out;
}
.collapse :deep(> div > h1:first-child) {
  display: none !important;
}
.collapse :deep(> div > template + h1),
.collapse :deep(> div > div:first-child > h1:first-child) {
  display: none !important;
}
</style>
