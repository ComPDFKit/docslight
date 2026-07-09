<script lang='ts' setup>
import { useAppStore } from '@/pinia/stores/app'
import { usePermissionStore } from '@/pinia/stores/permission'
import { useSettingsStore } from '@/pinia/stores/settings'
import { useDevice } from '@@/composables/useDevice'
import { useLayoutMode } from '@@/composables/useLayoutMode'
import { getCssVar } from '@@/utils/css'
import { useI18n } from 'vue-i18n'
import Item from './Item.vue'
import Help from './Help.vue'
import More from './More.vue'
import Cookies from 'js-cookie'
import Arrow from './Arrow.vue'
import Logout from './Logout.vue'
import Language from './Language.vue'
import { useUserStore } from '@/pinia/stores/user'
import logo from '@@/assets/images/layouts/logo.svg?url'

const v3SidebarMenuBgColor = getCssVar('--v3-sidebar-menu-bg-color')
const v3SidebarMenuTextColor = getCssVar('--v3-sidebar-menu-text-color')
const v3SidebarMenuActiveTextColor = getCssVar('--v3-sidebar-menu-active-text-color')

const { t, locale } = useI18n()
const { isLeft, isTop } = useLayoutMode()
const route = useRoute()
const permissionStore = usePermissionStore()

const activeMenu = computed(() => route.meta.activeMenu || route.path)
const noHiddenRoutes = computed(() => permissionStore.routes.filter(item => !item.meta?.hidden))
const backgroundColor = computed(() => (isLeft.value ? v3SidebarMenuBgColor : undefined))
const textColor = computed(() => (isLeft.value ? v3SidebarMenuTextColor : undefined))
const activeTextColor = computed(() => (isLeft.value ? v3SidebarMenuActiveTextColor : undefined))
const sidebarMenuItemHeight = computed(() => !isTop.value ? "var(--v3-sidebar-menu-item-height)" : "var(--v3-navigationbar-height)")
const sidebarMenuHoverBgColor = computed(() => !isTop.value ? "var(--v3-sidebar-menu-hover-bg-color)" : "transparent")
const tipLineWidth = computed(() => !isTop.value ? "2px" : "0px")
const name = ref('')
const language = ref('')
const logoutShow = ref(false)
const languageShow = ref(false)
const handleClick = () => {
  logoutShow.value = false
  languageShow.value = false
}
const changeLanguage = (val: string) => {
  locale.value = val
  localStorage.setItem('locale', val)
  location.reload()
}
onMounted(() => {
  addEventListener('click', handleClick)
  language.value = localStorage.getItem('locale') as string
})
onBeforeUnmount(() => {
  removeEventListener('click', handleClick)
})
onMounted(() => {
  const user = Cookies.get('idp_admin')
  if (!user) {
    router.push('/login')
  }
  name.value = JSON.parse(user as string).username
})

const router = useRouter()
const userStore = useUserStore()
/** 登出 */
const logout = () => {
  userStore.logout()
  router.push('/login')
  Cookies.remove('admin_token')
}
const width = ref('76px')
const isCollapse = ref(true)
</script>

<template>
  <div :class="isCollapse && 'collapse'" class="sidebar" @mouseenter="width = '285px', isCollapse = false"
      @mouseleave="width = '76px', isCollapse = true, logoutShow = false, languageShow = false" :style="{ width: width }">
    <div class="flex items-center font-700 text-brand-0 text-24px leading-24px ml-16px mb-28px mt-32px title">
      <img :src="logo" alt="Logo" class="mr-[10.8px]">
      <span>ComPDF AI</span>
    </div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="backgroundColor"
        :text-color="textColor"
        :active-text-color="activeTextColor"
        :collapse-transition="isCollapse"
        :mode="'vertical'"
        router
      >
        <Item
          v-for="noHiddenRoute in noHiddenRoutes"
          :key="noHiddenRoute.path"
          :item="noHiddenRoute"
          :base-path="noHiddenRoute.path"
        />
      </el-menu>
      <div class="absolute bottom-32px left-16px w-[calc(100%-32px)]">
        <a href="https://www.compdf.com/guides/idp/self-hosted-deployment/overview" target="_blank" class="flex items-center text-sm font-600 text-[#232748] py-8px px-12px w-full rounded-6px hover:(bg-[#1460F31A] text-brand-2)">
          <Help class="mr-18px min-w-20px my-2px" />
          <span class="whitespace-nowrap">{{ t('common.help') }}</span>
        </a>
        <div class="flex items-center text-sm font-600 text-[#232748] py-4px px-12px w-full rounded-6px hover:(bg-[#1460F31A] text-brand-2) relative" :class="languageShow && 'bg-[#1460F31A]'">
          <div @click.stop="languageShow = !languageShow" class="flex items-center justify-between cursor-pointer w-full py-8px">
            <div class="help flex items-center">
              <Language class="mr-18px min-w-20px my-2px" />
              <span class="whitespace-nowrap">{{ t('common.language') }}</span>
            </div>
            <Arrow class="transitions" :class="languageShow && 'transition'" />
          </div>
          <div v-show="languageShow" class="absolute bottom-[64px] left-0 cursor-pointer rounded-4px shadows bg-white w-full text-sm font-600 text-[#232748] p-4px">
            <div :class="language === 'en' && 'text-[#396FFA] bg-[#1460F31A]'" @click="changeLanguage('en')" class="flex items-center py-4px px-12px rounded-6px hover:(text-[#396FFA] bg-[#1460F31A])">
              English
            </div>
            <div :class="language === 'zh-cn' && 'text-[#396FFA] bg-[#1460F31A]'" @click="changeLanguage('zh-cn')" class="flex items-center py-4px px-12px rounded-6px hover:(text-[#396FFA] bg-[#1460F31A]) mt-2px">
              简中
            </div>
            <div :class="language === 'zh-tw' && 'text-[#396FFA] bg-[#1460F31A]'" @click="changeLanguage('zh-tw')" class="flex items-center py-4px px-12px rounded-6px hover:(text-[#396FFA] bg-[#1460F31A]) mt-2px">
              繁中
            </div>
          </div>
        </div>
        <div @click.stop="logoutShow = !logoutShow" class="flex items-center cursor-pointer text-sm font-600 text-[#232748] py-4px px-8px mb-8px w-full rounded-6px hover:(bg-[#1460F31A] text-brand-2) relative" :class="logoutShow && 'bg-[#1460F31A]'">
          <div class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">{{ name.slice(0, 1).toUpperCase() }}</div>
          <span class="name w-[calc(100%-64px)] truncate">{{ name }}</span>
          <More class="absolute right-12px top-10px cursor-pointer more" />
          <div v-show="logoutShow" @click="logout" class="absolute top-[-54px] left-0 cursor-pointer rounded-4px shadows flex items-center bg-white w-full text-sm font-600 text-[#232748] py-12px px-16px"><Logout class="mr-18px" />
            {{ t('common.logout') }}
          </div>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<style lang="scss" scoped>
%tip-line {
  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: v-bind(tipLineWidth);
    height: 100%;
    background-color: var(--v3-sidebar-menu-tip-line-bg-color);
  }
}

.shadows {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
.transitions {
  transform: rotateZ(-90deg);
  &.transition {
    transform: rotateZ(90deg);
  }
}
:deep(.el-scrollbar) {
  height: calc(100% - 90px);
  min-height: 500px;
  .scrollbar-wrapper {
    height: 100%;
    .el-scrollbar__view {
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      .el-menu {
        padding: 0 16px;
        .el-menu-item {
          height: auto;
          font-size: 16px;
          font-weight: 600;
          padding: 8px 12px;
          line-height: 24px;
          color: #232748;
          border-radius: 6px;
          &.el-menu-item {
            margin-top: 8px;
            &:hover {
              color: #396FFA;
              background-color: #1460F31A;
            }
          }
          &.is-active {
            color: #396FFA;
            background-color: #1460F31A;
            &::before {
              display: none;
            }
          }
          span {
            font-family: "Poppins";
          }
        }
        &.el-menu--collapse {
          a {
            display: flex;
            justify-content: center;
            .el-menu-item {
              max-width: 44px;
              min-width: 44px;
              min-height: 40px;
              .el-menu-tooltip__trigger {
                padding: 0;
                display: flex;
                min-height: 40px;
                justify-content: center;
                svg {
                  width: 20px;
                  height: 20px;
                  margin-right: 0;
                }
              }
            }
          }
        }
      }
    }
    // 滚动条
    .el-scrollbar__bar {
      &.is-horizontal {
        // 隐藏水平滚动条
        display: none;
      }
    }
  }
}

.collapse {
  .title {
    img {
      margin-right: 0px;
    }
    span {
      display: none;
    }
  }
  .absolute {
    span {
      display: none;
    }
  }
  .transitions, .more {
    display: none;
  }
}

.el-menu {
  user-select: none;
  border: none;
  width: 100%;
}

.el-menu--horizontal {
  height: v-bind(sidebarMenuItemHeight);
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title),
:deep(.el-sub-menu .el-menu-item),
:deep(.el-menu--horizontal .el-menu-item) {
  height: v-bind(sidebarMenuItemHeight);
  line-height: v-bind(sidebarMenuItemHeight);
  &.is-active,
  &:hover {
    background-color: v-bind(sidebarMenuHoverBgColor);
  }
}

:deep(.el-sub-menu) {
  &.is-active {
    > .el-sub-menu__title {
      color: v-bind(activeTextColor);
    }
  }
}

:deep(.el-menu-item.is-active) {
  @extend %tip-line;
}

.el-menu--collapse {
  :deep(.el-sub-menu.is-active) {
    .el-sub-menu__title {
      @extend %tip-line;
      background-color: v-bind(sidebarMenuHoverBgColor);
    }
  }
}
</style>
