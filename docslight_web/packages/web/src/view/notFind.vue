<template>
  <div class="bg-[#F4F8FF] w-100vw">
    <h2 class="container">
      <div class="2xl:px-100px">
        <div class="lg:flex md:justify-between md:items-center md:min-h-100vh md:py-80px <md:py-50px">
          <div class="lg:w-[45.9183%] flex justify-center">
            <img src="../assets/images/notFound/sad.svg" />
          </div>
          <div class="lg:w-[33.6735%] <lg:mt-50px">
            <img src="../assets/images/notFound/404.svg" class="w-full mx-auto max-w-327px" />
            <h1 class="mt-30px mb-37px text-center text-sm text-brand-3 text-center">
              {{ t('notFind.desc') }}
            </h1>
            <div class="flex justify-center">
              <div class="max-w-140px text-center cursor-pointer flex-1 rounded-8px py-9px border mr-16px border-[#1460F3] text-[#1460F3]" @click="refresh">{{ t('notFind.reload') }}</div>
              <a class="max-w-140px text-center cursor-pointer flex-1 rounded-8px py-9px bg-[#1460F3] text-white text-sm" href="/">{{ t('notFind.goHome') }}</a>
            </div>
          </div>
        </div>
      </div>
    </h2>
  </div>
</template>

<script lang='ts' setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const refresh = () => {
  location.reload()
}
</script>
