import { createRouter, createWebHistory } from 'vue-router'

let routes = [
  {
    path: '/',
    name: 'hone',
    redirect: '/document-extraction',
  },
  {
    path: '/login',
    name: 'login',
    redirect: '/',
    meta: {
      en: 'Login - Access Your Account',
      'zh-cn': '登录 - 访问您的帐户',
      'zh-tw': 'Login - 訪問您的帳戶'
    }
  },
  // {
  //   path: '/signup',
  //   name: 'signup',
  //   component: () => import('../view/signup.vue'),
  //   meta: {
  //     en: 'Sign Up - Get Started',
  //     'zh-cn': '注册新帐户 - 开始使用',
  //     'zh-tw': 'Sign Up - 開始使用'
  //   }
  // },
  {
    path: '',
    name: 'container',
    component: () => import('../view/Container.vue'),
    children: [
      {
        path: '',
        redirect: '/document-extraction'
      },
      {
        path: '/asset',
        name: 'asset',
        component: () => import('../view/asset.vue'),
        meta: {
          en: 'Assets | ComPDF AI',
          'zh-cn': '资产 | ComPDF AI',
          'zh-tw': '資產 | ComPDF AI'
        }
      },
      {
        path: '/team-management',
        name: 'team-management',
        component: () => import('../view/team-management.vue'),
        meta: {
          en: 'Team Management | ComPDF AI',
          'zh-cn': '团队管理 | ComPDF AI',
          'zh-tw': '團隊管理 | ComPDF AI'
        }
      },
      {
        path: '/single-document-extraction',
        name: 'single-document-extraction',
        component: () => import('../view/single-document-extraction.vue'),
        meta: {
          hideTopUserMenu: true,
          en: 'Single Intelligent Document Extraction | ComPDF AI',
          'zh-cn': '单文档提取 | ComPDF AI',
          'zh-tw': '單文檔提取 | ComPDF AI'
        }
      },
      {
        path: '/document-extraction',
        name: 'document-extraction',
        component: () => import('../view/document-extraction.vue'),
        meta: {
          en: 'Intelligent Document Extraction | ComPDF AI',
          'zh-cn': '文档提取 | ComPDF AI',
          'zh-tw': '文檔提取 | ComPDF AI'
        }
      },
      {
        path: '/bulk-document-extraction',
        name: 'bulk-document-extraction',
        component: () => import('../view/bulk-document-extraction.vue'),
        meta: {
          en: 'Bulk Intelligent Document Extraction | ComPDF AI',
          'zh-cn': '批量文档提取 | ComPDF AI',
          'zh-tw': '批量文檔提取 | ComPDF AI'
        }
      },
      {
        path: '/single-document-parsing',
        name: 'single-document-parsing',
        component: () => import('../view/single-document-parsing.vue'),
        meta: {
          en: 'Single Intelligent Document Parsing | ComPDF AI',
          'zh-cn': '单文档解析 | ComPDF AI',
          'zh-tw': '單文檔解析 | ComPDF AI'
        }
      },
      {
        path: '/document-parsing',
        name: 'document-parsing',
        component: () => import('../view/document-parsing.vue'),
        meta: {
          en: 'Intelligent Document Parsing | ComPDF AI',
          'zh-cn': '文档解析 | ComPDF AI',
          'zh-tw': '文檔解析 | ComPDF AI'
        }
      },
      {
        path: '/bulk-document-parsing',
        name: 'bulk-document-parsing',
        component: () => import('../view/bulk-document-parsing.vue'),
        meta: {
          en: 'Bulk Intelligent Document Parsing | ComPDF AI',
          'zh-cn': '批量文档解析 | ComPDF AI',
          'zh-tw': '批量文檔解析 | ComPDF AI'
        }
      },
      {
        path: '/team-space',
        name: 'team-space',
        component: () => import('../view/team-space.vue'),
        meta: {
          en: 'ComPDF AI Document Management System | Team Space',
          'zh-cn': 'ComPDF AI文档资产管理｜团队空间',
          'zh-tw': 'ComPDF AI 文件資產管理｜團隊空間'
        }
      },
      {
        path: '/settings',
        name: 'settings',
        component: () => import('../view/settings.vue'),
        meta: {
          en: 'Settings | ComPDF AI',
          'zh-cn': '设置 | ComPDF AI',
          'zh-tw': '設定 | ComPDF AI'
        }
      },
      // {
      //   path: '/scanner-inbox',
      //   name: 'scanner-inbox',
      //   component: () => import('../view/scanner-inbox.vue'),
      //   meta: {
      //     en: 'ComPDF AI Document Management System | Scanner Inbox',
      //     'zh-cn': 'ComPDF AI文档资产管理｜扫描仪收件箱',
      //     'zh-tw': 'ComPDF AI 文件資產管理｜掃描器收件匣'
      //   }
      // }
    ]
  },
  { // 当没有匹配到正确路由的时候，匹配404组件
    path: '/:catchAll(.*)',
    name: '404',
    component: () => import('../view/notFind.vue')
  }
]
// 路由
const router = createRouter({
  history: createWebHistory(),
  routes
})

export function addAdminRoutes() {
  if (router.hasRoute('user-management')) return
  router.addRoute('container', {
    path: '/user-management',
    name: 'user-management',
    component: () => import('../view/user-management.vue'),
    meta: {
      en: 'User Management | ComPDF AI',
      'zh-cn': '用户管理 | ComPDF AI',
      'zh-tw': '用戶管理 | ComPDF AI'
    }
  })
}
// 导出
export default router
