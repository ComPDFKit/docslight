/* eslint-disable perfectionist/sort-imports */

// core
import { pinia } from "@/pinia"
import { router } from "@/router"
import { installPlugins } from "@/plugins"
import App from "@/App.vue"
// css
import "normalize.css"
import 'virtual:windi.css'
import "nprogress/nprogress.css"
import "vxe-table/lib/style.css"
import "@@/assets/styles/index.scss"
import "element-plus/theme-chalk/dark/css-vars.css"
import { i18n } from './i18n'
// 创建应用实例
const app = createApp(App)

router.beforeEach((to, from, next) => {
  if (to.meta?.seo) {
    // @ts-ignore
    document.title = to.meta.seo[i18n.global.locale.value] as string
  } else {
    document.title = 'ComIDP'
  }
  next()
})
// 安装插件（全局组件、自定义指令等）
installPlugins(app)

// 安装 pinia 和 router
app.use(pinia).use(router).use(i18n)

// router 准备就绪后挂载应用
router.isReady().then(() => {
  app.mount("#app")
})
