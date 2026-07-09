import { i18n } from '../i18n'
import type { RouteRecordRaw } from "vue-router"
import { routerConfig } from "@/router/config"
import { registerNavigationGuard } from "@/router/guard"
import { createRouter } from "vue-router"
import { flatMultiLevelRoutes } from "./helper"
const { t } = i18n.global
const Layouts = () => import("@/layouts/index.vue")

/**
 * @name 常驻路由
 * @description 除了 redirect/403/404/login 等隐藏页面，其他页面建议设置唯一的 Name 属性
 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: "/redirect",
    component: Layouts,
    meta: {
      hidden: true
    },
    children: [
      {
        path: ":path(.*)",
        component: () => import("@/pages/redirect/index.vue")
      }
    ]
  },
  {
    path: "/403",
    component: () => import("@/pages/error/403.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/404",
    component: () => import("@/pages/error/404.vue"),
    meta: {
      hidden: true
    },
    alias: "/:pathMatch(.*)*"
  },
  {
    path: "/login",
    component: () => import("@/pages/login/index.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/",
    component: Layouts,
    children: [
      {
        path: "/",
        component: () => import("@/pages/index.vue"),
        name: "Home",
        meta: {
          title: t("common.home"),
          svgIcon: "home",
          affix: false,
          keepAlive: true
        }
      }
    ]
  },
  {
    path: "/user-management",
    component: Layouts,
    children: [
      {
        path: "/user-management",
        component: () => import("@/pages/user-management/index.vue"),
        name: "UserManagement",
        meta: {
          title: t("common.userManagement"),
          svgIcon: "user-management",
          affix: true,
          seo: {
            en: '',
            'zh-cn': '',
            'zh-tw': ''
          }
        }
      }
    ]
  },
  {
    path: "/team",
    component: Layouts,
    redirect: "/team/index",
    children: [
      {
        path: "/admin-team-management",
        component: () => import("@/pages/team-management/index.vue"),
        name: "Team",
        meta: {
          title: t("common.teamManagement"),
          svgIcon: "team-management",
          affix: false,
          keepAlive: true,
          seo: {
            en: '',
            'zh-cn': '',
            'zh-tw': ''
          }
        }
      }
    ]
  },
  {
    path: "/kb",
    component: Layouts,
    redirect: "/kb/index",
    children: [
      {
        path: "/kb-management",
        component: () => import("@/pages/knowledgebase/index.vue"),
        name: "KB",
        meta: {
          title: t("common.knowledgeManagement"),
          svgIcon: "kb",
          affix: false,
          keepAlive: true,
          seo: {
            en: '',
            'zh-cn': '',
            'zh-tw': ''
          }
        }
      }
    ]
  },
  {
    path: "/file",
    component: Layouts,
    redirect: "/log-management",
    children: [
      {
        path: "/log-management",
        component: () => import("@/pages/log/index.vue"),
        name: "File",
        meta: {
          title: t("common.logManagement"),
          svgIcon: "log",
          affix: false,
          keepAlive: true,
          seo: {
            en: '',
            'zh-cn': '',
            'zh-tw': ''
          }
        }
      }
    ]
  }
]

/**
 * @name 动态路由
 * @description 用来放置有权限 (Roles 属性) 的路由
 * @description 必须带有唯一的 Name 属性
 */
export const dynamicRoutes: RouteRecordRaw[] = [
  // {
  //   path: "/permission",
  //   component: Layouts,
  //   redirect: "/permission/page-level",
  //   name: "Permission",
  //   meta: {
  //     title: "权限演示",
  //     elIcon: "Lock",
  //     // 可以在根路由中设置角色
  //     roles: ["admin", "editor"],
  //     alwaysShow: true
  //   },
  //   children: [
  //     {
  //       path: "page-level",
  //       component: () => import("@/pages/demo/permission/page-level.vue"),
  //       name: "PermissionPageLevel",
  //       meta: {
  //         title: "页面级",
  //         // 或者在子路由中设置角色
  //         roles: ["admin"]
  //       }
  //     },
  //     {
  //       path: "button-level",
  //       component: () => import("@/pages/demo/permission/button-level.vue"),
  //       name: "PermissionButtonLevel",
  //       meta: {
  //         title: "按钮级",
  //         // 如果未设置角色，则表示：该页面不需要权限，但会继承根路由的角色
  //         roles: undefined
  //       }
  //     }
  //   ]
  // }
]

/** 路由实例 */
export const router = createRouter({
  history: routerConfig.history,
  routes: routerConfig.thirdLevelRouteCache ? flatMultiLevelRoutes(constantRoutes) : constantRoutes
})

/** 重置路由 */
export function resetRouter() {
  try {
    // 注意：所有动态路由路由必须带有 Name 属性，否则可能会不能完全重置干净
    router.getRoutes().forEach((route) => {
      const { name, meta } = route
      if (name && meta.roles?.length) {
        router.hasRoute(name) && router.removeRoute(name)
      }
    })
  } catch {
    // 强制刷新浏览器也行，只是交互体验不是很好
    location.reload()
  }
}

// 注册路由导航守卫
registerNavigationGuard(router)
