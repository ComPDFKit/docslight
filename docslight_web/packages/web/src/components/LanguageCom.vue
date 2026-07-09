<template>
  <div class="flex text-sm relative font-500 text-brand-3 border-1 border-[#E1E3E8] rounded-6px py-6px px-12px">
    <div @click.stop="languageShow = !languageShow" class="flex items-center cursor-pointer">
      <Language class="mr-18px min-w-20px" />
      {{ lan }}
      <Arrow class="transitions ml-12px" :class="languageShow && 'transition'" />
    </div>
    <div v-show="languageShow" class="absolute top-48px left-0 cursor-pointer rounded-4px shadows bg-white w-full text-sm font-600 text-[#232748] p-4px">
      <div :class="language === 'en' && 'text-[#396FFA] bg-[#1460F31A]'" @click="changeLanguage('en')" class="flex items-center py-4px px-12px rounded-6px hover:(text-[#396FFA] bg-[#1460F31A])">
        English
      </div>
      <div :class="language === 'zh-cn' && 'text-[#396FFA] bg-[#1460F31A]'" @click="changeLanguage('zh-cn')" class="flex items-center py-4px px-12px rounded-6px hover:(text-[#396FFA] bg-[#1460F31A]) mt-2px">
        简中
      </div>
      <div :class="language === 'zh-tw' && 'text-[#396FFA] bg-[#1460F31A]'" @click="changeLanguage('zh-tw')" class="flex items-center py-4px px-12px rounded-6px hover:(text-[#396FFA] bg-[#1460F31A]) mt-2px">
        繁中
      </div>
      <div :class="language === 'ja' && 'text-[#396FFA] bg-[#1460F31A]'" @click="changeLanguage('ja')" class="flex items-center py-4px px-12px rounded-6px hover:(text-[#396FFA] bg-[#1460F31A]) mt-2px">
        日本語
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()
const languageShow = ref(false)
const languageMap = ref({
  'en': 'English',
  'zh-cn': '简中',
  'zh-tw': '繁中',
  'ja': '日本語',
})
const language = ref(localStorage.getItem('locale') as string)
const lan = ref(languageMap.value[locale.value as keyof typeof languageMap.value])
const changeLanguage = (val: string) => {
  locale.value = val
  localStorage.setItem('locale', val)
  language.value = localStorage.getItem('locale') as string
  lan.value = languageMap.value[locale.value as keyof typeof languageMap.value]
}
onMounted(() => {
  addEventListener('click', handleClick)
})
onBeforeUnmount(() => {
  removeEventListener('click', handleClick)
})
const handleClick = () => {
  languageShow.value = false
}
</script>

<style lang="scss" scoped>
.transitions {
  transform: rotateZ(-90deg);
  &.transition {
    transform: rotateZ(90deg);
  }
}
</style>
