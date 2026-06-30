/// <reference types="vitest/config" />
/// <reference types="vite/client" />

import path from 'path'
import { defineConfig, loadEnv } from "vite"
import vue from '@vitejs/plugin-vue'
import WindiCSS from 'vite-plugin-windicss'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "") as ImportMetaEnv
  return {
    base: '/',
    plugins: [
      vue(),
      WindiCSS(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        globsExclude: [
          'src/components/images/{Copy,Download,Setting,Upload}.vue',
        ],
      }),
      {
        name: 'html-prod-config-injector',
        transformIndexHtml(html: string) {
          const isProd = process.env.NODE_ENV === 'production'
          return html.replace(
            '%PROD_CONFIG_JS%',
            isProd ? '<script src="/config.js"></script>' : ''
          )
        }
      }
    ] as any,
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    server: {
      // 端口号
      port: 3022,
      // 监听所有地址
      host: "0.0.0.0",
      // 反向代理
      proxy: {
        "^/v1": {
          target: env.VITE_BASE_URL,
          // 是否允许跨域
          changeOrigin: true
        },
        "^/api/v1": {
          // target: "https://apifoxmock.com/m1/2930465-2145633-default",
          target: env.VITE_ADMIN_BASE_URL,
          // 是否允许跨域
          changeOrigin: true
        },
        "^/api/idp": {
          target: env.VITE_IDP_URL,
          // 是否允许跨域
          changeOrigin: true
        }
      }
    }
  }
})
