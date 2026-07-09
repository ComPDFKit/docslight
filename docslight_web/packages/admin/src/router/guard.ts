import type { Router } from 'vue-router'
import { usePermissionStore } from '@/pinia/stores/permission'
import { useUserStore } from '@/pinia/stores/user'
import { routerConfig } from '@/router/config'
import { setRouteChange } from '@@/composables/useRouteListener'
import { useTitle } from '@@/composables/useTitle'
import { getToken } from '@@/utils/cache/cookies'
import NProgress from 'nprogress'
import Cookies from 'js-cookie'
import { getEnv } from '@@/utils/env'

NProgress.configure({ showSpinner: false })

const { setTitle } = useTitle()
let domain = getEnv('COMIDP_HOST')
if (domain.indexOf(':') === 0) {
  domain = window.location.protocol + '//' + window.location.hostname + domain
}
const LOGIN_PATH = domain + '/login'

export function registerNavigationGuard(router: Router) {
  // 全局前置守卫
  router.beforeEach(async (to, _from) => {
    NProgress.start()
    const userStore = useUserStore()
    const token = Cookies.get('admin_token') || sessionStorage.getItem('admin_token')
    if (token) {
      userStore.setToken(token)
    }
    const permissionStore = usePermissionStore()
    // 如果没有登录
    if (!getToken() || to.path === '/login') {
      // 如果在免登录的白名单中，则直接进入
      // if (isWhiteList(to)) return true
      // 其他没有访问权限的页面将被重定向到登录页面
      location.href = LOGIN_PATH
      // return LOGIN_PATH
    }
    // 如果已经登录，并准备进入 Login 页面，则重定向到主页
    if (to.path === LOGIN_PATH) return '/'
    // 如果用户已经获得其权限角色
    if (userStore.roles.length !== 0) return true
    // 否则要重新获取权限角色
    try {
      await userStore.getInfo()
      // 注意：角色必须是一个数组！ 例如: ['admin'] 或 ['developer', 'editor']
      const roles = userStore.roles
      // 生成可访问的 Routes
      routerConfig.dynamic ? permissionStore.setRoutes(roles) : permissionStore.setAllRoutes()
      // 将 '有访问权限的动态路由' 添加到 Router 中
      permissionStore.addRoutes.forEach(route => router.addRoute(route))
      // 设置 replace: true, 因此导航将不会留下历史记录
      return { ...to, replace: true }
    } catch (error) {
      // 过程中发生任何错误，都直接重置 Token，并重定向到登录页面
      userStore.resetToken()
      ElMessage.error((error as Error).message || '路由守卫发生错误')
      return LOGIN_PATH
    }
  })

  // 全局后置钩子
  router.afterEach((to) => {
    setRouteChange(to)
    setTitle(to.meta.title)
    NProgress.done()
  })
}
