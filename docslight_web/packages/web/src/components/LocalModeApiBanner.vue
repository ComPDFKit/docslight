<template>
  <section v-if="isLocalMode && !dismissed" class="local-mode-api-banner" aria-label="API Key reminder">
    <img class="local-mode-api-banner__shape local-mode-api-banner__shape--left" :src="ellipseLeft" alt="" aria-hidden="true">
    <img class="local-mode-api-banner__shape local-mode-api-banner__shape--left-center" :src="ellipseLeftCenter" alt="" aria-hidden="true">
    <img class="local-mode-api-banner__shape local-mode-api-banner__shape--right" :src="ellipseRight" alt="" aria-hidden="true">
    <img class="local-mode-api-banner__shape local-mode-api-banner__shape--right-center" :src="ellipseRightCenter" alt="" aria-hidden="true">

    <div class="local-mode-api-banner__content">
      <img class="local-mode-api-banner__product-icon" :src="apiKeyImage" alt="" aria-hidden="true">
      <div class="local-mode-api-banner__copy">
        <p class="local-mode-api-banner__title">{{ t('localModeBanner.title') }}</p>
      </div>
    </div>
    <a class="local-mode-api-banner__action" href="https://www.compdf.com/compdf-portal/signin" target="_blank" rel="noopener noreferrer">
      {{ t('localModeBanner.signIn') }}
    </a>
    <button class="local-mode-api-banner__close" type="button" :aria-label="t('common.cancel')" @click="dismissed = true">
      <img :src="closeIcon" alt="" aria-hidden="true">
    </button>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { get } from '@/utils/request'
import apiKeyImage from '@/assets/images/local-mode-banner/apikeyImg.png'
import ellipseLeft from '@/assets/images/local-mode-banner/Ellipse 13570.svg'
import ellipseLeftCenter from '@/assets/images/local-mode-banner/Ellipse 13571.svg'
import ellipseRight from '@/assets/images/local-mode-banner/Ellipse 13567.svg'
import ellipseRightCenter from '@/assets/images/local-mode-banner/Ellipse 13572.svg'
import closeIcon from '@/assets/images/local-mode-banner/ic.svg'

const { t } = useI18n()
const isLocalMode = ref(false)
const dismissed = ref(false)

onMounted(async () => {
  try {
    const { data } = await get<{ model?: string }>('/api/idp/get-settings')
    isLocalMode.value = data.data?.model?.toLowerCase() === 'local'
  } catch {
    // Keep the detail page available if the optional settings lookup fails.
    isLocalMode.value = false
  }
})
</script>

<style scoped lang="scss">
.local-mode-api-banner {
  position: relative;
  isolation: isolate;
  width: 100%;
  min-height: 64px;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 0;
  padding: 8px 92px 8px 90px;
  box-sizing: border-box;
  background: linear-gradient(127.4deg, #6973ff 0%, #4d09ea 100%);
  color: #fff;
  font-family: 'Encode Sans', sans-serif;

  &::after {
    content: '';
    position: absolute;
    z-index: -1;
    inset: 0;
    background: linear-gradient(90deg, rgba(255, 255, 255, .14), transparent 32%, transparent 68%, rgba(46, 0, 207, .35));
  }
}

.local-mode-api-banner__shape {
  position: absolute;
  z-index: -1;
  pointer-events: none;
  opacity: 1;

  &--left {
    left: -87px;
    bottom: -76px;
    width: 217px;
  }

  &--left-center {
    left: 17px;
    bottom: -35px;
    width: 198px;
  }

  &--right {
    right: -88px;
    top: -42px;
    width: 242px;
  }

  &--right-center {
    right: 16px;
    top: -28px;
    width: 198px;
  }
}

.local-mode-api-banner__content {
  min-width: 0;
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  gap: 20px;
}

.local-mode-api-banner__product-icon {
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  object-fit: contain;
}

.local-mode-api-banner__copy {
  min-width: 0;
}

.local-mode-api-banner__title {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.local-mode-api-banner__title {
  font-size: 18px;
  font-weight: 600;
  line-height: 26px;
}

.local-mode-api-banner__action {
  height: 32px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 99px;
  padding: 6px 12px;
  box-sizing: border-box;
  border: 1px solid #e2e3e5;
  border-radius: 4px;
  background: rgba(255, 255, 255, .96);
  color: #396ffa;
  font-size: 12px;
  font-weight: 400;
  line-height: 20px;
  text-decoration: none;
  white-space: nowrap;
}

.local-mode-api-banner__close {
  position: absolute;
  top: 20px;
  right: 40px;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;

  img {
    width: 14px;
    height: 14px;
  }
}

@media (max-width: 900px) {
  .local-mode-api-banner {
    display: none;
  }
}
</style>
