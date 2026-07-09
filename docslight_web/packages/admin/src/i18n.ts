import { createI18n } from 'vue-i18n'
import zh from './locales/zh-cn.json'
import tw from './locales/zh-tw.json'
import en from './locales/en.json'
import Cookies from 'js-cookie'

const locale = localStorage.getItem('locale') || Cookies.get('locale')
const supportedLocales = ['zh-cn', 'zh-tw', 'en']
const lang = navigator.language.toLocaleLowerCase()
const defaultLocale = supportedLocales.includes(lang) ? lang : 'en'
const language = locale || defaultLocale
export const i18n = createI18n({
  legacy: true, // 推荐组合式 API
  locale: language,
  fallbackLocale: 'en',
  messages: {
    'zh-cn': zh,
    'zh-tw': tw,
    'en': en
  }
})
