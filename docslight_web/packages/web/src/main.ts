import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import 'virtual:windi.css'
import router, { addAdminRoutes } from "./router/index"
import './assets/styles/element-plus.scss'
import { i18n } from './i18n'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { vPermission } from './directives/permission'
import { usePermissionStore, useStore } from './stores'
import { applyFixedAuthState } from './utils/mockAuth'

dayjs.extend(utc)

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
const app = createApp(App)
app.directive('permission', vPermission)
// 先安装 pinia，守卫里才能正常使用 store（含持久化数据）
app.use(pinia)

const store = useStore()
const permissionStore = usePermissionStore()
applyFixedAuthState(store, permissionStore)

router.beforeEach((to) => {
  if (['admin', 'manager'].includes(store.role) && !router.hasRoute('user-management')) {
    addAdminRoutes()
    return to.fullPath
  }
  if (to.path === '/login') {
    return '/'
  }
  if (to.meta) {
    document.title = to.meta[i18n.global.locale.value] as string
  } else {
    document.title = 'ComPDF AI'
  }
})

app.use(router).use(i18n)
app.mount('#app')
