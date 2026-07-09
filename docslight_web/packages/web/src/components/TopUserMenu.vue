<template>
  <header v-if="user" class="top-user-menu" @click.stop>
    <div class="top-user-menu__left">
      <button class="top-user-menu__indent" type="button" aria-label="Toggle side menu" @click="toggleSideMenu">
        <img :src="sideMenuExpanded ? indentLeftIcon : indentRightIcon" alt="" />
      </button>
      <span class="top-user-menu__title">{{ pageTitle }}</span>
    </div>

    <div class="top-user-menu__right">
      <div class="language-trigger" role="button" tabindex="0" @click.stop="toggleLanguageMenu">
        <svg class="language-trigger__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M2.54594 9.16671C2.87734 6.16882 4.97568 3.70348 7.77646 2.83519C6.67392 4.7124 5.99116 6.86635 5.85762 9.16671H2.54594ZM9.16036 0.871329C4.49137 1.29539 0.833496 5.22055 0.833496 10C0.833496 14.7795 4.49133 18.7047 9.16029 19.1287L9.16685 19.1369L9.52131 19.1544C9.57779 19.1573 9.63441 19.1597 9.69115 19.1616C9.79374 19.165 9.89675 19.1667 10.0002 19.1667C10.099 19.1667 10.1976 19.1651 10.2957 19.162C10.357 19.1601 10.4181 19.1576 10.479 19.1544L10.8335 19.1369L10.84 19.1287C15.509 18.7047 19.1668 14.7795 19.1668 10C19.1668 5.22055 15.509 1.29539 10.84 0.871329L10.8334 0.863135L10.4782 0.845623C10.3974 0.841474 10.3164 0.838371 10.2351 0.836326C10.157 0.834362 10.0787 0.833374 10.0002 0.833374C9.92106 0.833374 9.84219 0.834376 9.76357 0.836368C9.68282 0.838414 9.60233 0.841504 9.52211 0.845625L9.16691 0.863135L9.16036 0.871329ZM10.0011 2.50004C11.4145 4.38143 12.3089 6.67361 12.4728 9.16671H7.52748C7.69146 6.67361 8.58587 4.38143 9.99922 2.50004C9.99953 2.50004 9.99985 2.50004 10.0002 2.50004C10.0005 2.50004 10.0008 2.50004 10.0011 2.50004ZM5.85762 10.8334C5.99114 13.1337 6.67389 15.2877 7.77641 17.1649C4.97566 16.2966 2.87733 13.8312 2.54594 10.8334H5.85762ZM9.99916 17.5C8.58583 15.6186 7.69144 13.3265 7.52747 10.8334H12.4729C12.3089 13.3265 11.4145 15.6186 10.0012 17.5C10.0008 17.5 10.0005 17.5 10.0002 17.5C9.99983 17.5 9.9995 17.5 9.99916 17.5ZM12.2239 17.1649C13.3264 15.2877 14.0092 13.1337 14.1427 10.8334H17.4544C17.123 13.8312 15.0247 16.2966 12.2239 17.1649ZM14.1427 9.16671C14.0092 6.86635 13.3264 4.7124 12.2239 2.83519C15.0246 3.70348 17.123 6.16882 17.4544 9.16671H14.1427Z" fill="currentColor"/>
        </svg>
        <span>{{ currentLanguageShort }}</span>
        <div v-show="languageMenuVisible" class="language-menu">
          <button
            v-for="item in languageOptions"
            :key="item.value"
            type="button"
            :class="locale === item.value && 'is-active'"
            @click.stop="changeHeaderLanguage(item.value)"
          >{{ item.label }}</button>
        </div>
      </div>
    </div>
  </header>
</template>

<script lang="ts" setup>
import { computed, inject, onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import indentLeftIcon from '../assets/images/indent-left.svg'
import indentRightIcon from '../assets/images/indent-right.svg'
import { usePermissionStore, useStore } from '../stores'
import { addAdminRoutes } from '../router'
import { applyFixedAuthState } from '../utils/mockAuth'

interface UserInfo {
  id?: string
  email: string
  username: string
  avatar: string
  token?: string
}

const { t, locale } = useI18n()
const route = useRoute()
const store = useStore()
const permissionStore = usePermissionStore()
const setMenuExpanded = inject<((expanded: boolean, locked?: boolean) => void) | undefined>('setMenuExpanded')

const user = ref<UserInfo | null>(null)
const languageMenuVisible = ref(false)
const sideMenuExpanded = ref(false)

const languageOptions = [
  { value: 'en', label: 'English', short: 'En', apiValue: 'English' },
  { value: 'zh-cn', label: '简中', short: '简中', apiValue: 'Chinese' },
  { value: 'zh-tw', label: '繁中', short: '繁中', apiValue: 'Traditional Chinese' },
  { value: 'ja', label: '日本語', short: '日本語', apiValue: 'Japanese' }
] as const

const currentLanguageShort = computed(() => languageOptions.find(item => item.value === locale.value)?.short || 'En')

const pageTitle = computed(() => {
  const metaTitle = route.meta?.[locale.value as keyof typeof route.meta] || route.meta?.en || ''
  return String(metaTitle)
    .replace(/\s*[|｜]\s*ComPDF AI.*$/i, '')
    .replace(/\s+-\s+Welcome.*$/i, '')
    .replace(/\s+-\s+Access.*$/i, '')
    .trim() || 'ComPDF AI'
})

const getUserInfo = async () => {
  const fixedUser = applyFixedAuthState(store, permissionStore)
  user.value = fixedUser
  if (['admin', 'manager'].includes(store.role)) {
    addAdminRoutes()
  }
}

const closeMenu = () => {
  languageMenuVisible.value = false
}

const toggleLanguageMenu = () => {
  languageMenuVisible.value = !languageMenuVisible.value
}

const changeHeaderLanguage = async (value: string) => {
  const option = languageOptions.find(item => item.value === value)
  if (!option) return
  localStorage.setItem('locale', option.value)
  locale.value = option.value
  languageMenuVisible.value = false
  ElMessage.success(t('knowledgeBases.configuration.update'))
}

const toggleSideMenu = () => {
  sideMenuExpanded.value = !sideMenuExpanded.value
  setMenuExpanded?.(sideMenuExpanded.value, sideMenuExpanded.value)
}

defineExpose({
  changeInfo: getUserInfo,
  closeMenu
})

onMounted(async () => {
  await getUserInfo()
  addEventListener('click', closeMenu)
})

onBeforeUnmount(() => {
  removeEventListener('click', closeMenu)
})
</script>

<style lang="scss" scoped>
.top-user-menu {
  position: sticky;
  top: 0;
  z-index: 2000;
  width: 100%;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #fff;
  color: #0C131F;
  font-family: 'Encode Sans', sans-serif;
}
.top-user-menu__left,
.top-user-menu__right,
.language-trigger {
  display: flex;
  align-items: center;
}
.top-user-menu__left {
  gap: 12px;
  min-width: 0;
}
.top-user-menu__right {
  gap: 16px;
  flex: 0 0 auto;
}
.top-user-menu__indent {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: #0C131F;
  cursor: pointer;
  outline: none;

  img {
    width: 20px;
    height: 20px;
    display: block;
  }
}
.top-user-menu__title {
  min-width: 0;
  color: #0C131F;
  font-size: 16px;
  line-height: 24px;
  font-weight: 400;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.language-trigger {
  position: relative;
  justify-content: center;
  gap: 8px;
  min-width: 44px;
  height: 32px;
  padding: 5px 6px;
  border-radius: 4px;
  color: #0C131F;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  cursor: pointer;

  &:hover {
    background: #F5F7FF;
  }
}
.language-trigger__icon {
  width: 20px;
  height: 20px;
  min-width: 20px;
  color: #0C131F;
}
.language-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 12;
  width: 132px;
  padding: 4px;
  border: 1px solid #E7E8E8;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(12, 19, 31, 0.12);

  button {
    width: 100%;
    padding: 6px 10px;
    border: 0;
    border-radius: 4px;
    background: transparent;
    color: #232748;
    font-size: 14px;
    line-height: 20px;
    text-align: left;
    cursor: pointer;

    &:hover,
    &.is-active {
      background: #F5F7FF;
      color: #396FFA;
    }
  }
}
</style>
