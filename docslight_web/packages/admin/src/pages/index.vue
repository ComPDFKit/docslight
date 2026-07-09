<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import logo from "@@/assets/images/layouts/logo.svg?url"

const { t } = useI18n()
</script>
<template>
  <div class="flex flex-col justify-center items-center">
    <img :src="logo" alt="Logo" class="w-63px h-64px mx-auto">
    <div class="my-20px text-brand-0 text-44px leading-56px font-600 text-center">{{ t('home.title') }}</div>
    <div class="text-brand-1 text-tiny">{{ t('home.desc') }}</div>
    <div class="mt-40px h-1px w-964px bg-[#E1E3E8]"></div>
  </div>
</template>