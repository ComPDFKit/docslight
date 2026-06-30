<template>
  <teleport to="body">
    <div v-if="modelValue" class="api-key-dialog-mask">
      <div class="api-key-dialog" role="dialog" aria-modal="true" :aria-label="t('apiKeyDialog.title')">
        <div class="api-key-dialog__header">
          <div class="api-key-dialog__title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 23C18.0751 23 23 18.0751 23 12C23 5.92487 18.0751 1 12 1C5.92487 1 1 5.92487 1 12C1 18.0751 5.92487 23 12 23ZM10.996 8.50002V6.49611H12.9999V8.50002H10.996ZM12.9999 10L12.9999 17.5H10.9999V10L12.9999 10Z" fill="#396FFA"/>
            </svg>
            <span>{{ t('apiKeyDialog.title') }}</span>
          </div>
          <button class="api-key-dialog__close" type="button" :aria-label="t('apiKeyDialog.cancel')" @click="closeDialog">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <path d="M4.70013 3.75781L7.99996 7.05764L11.2998 3.75781L12.2426 4.70062L8.94277 8.00045L12.2426 11.3003L11.2998 12.2431L7.99996 8.94326L4.70013 12.2431L3.75732 11.3003L7.05716 8.00045L3.75732 4.70062L4.70013 3.75781Z" fill="#0C131F" fill-opacity="0.6"/>
            </svg>
          </button>
        </div>

        <div class="api-key-dialog__content">
          {{ t('apiKeyDialog.description') }}
        </div>

        <div class="api-key-dialog__footer">
          <button class="api-key-dialog__button api-key-dialog__button--cancel" type="button" @click="closeDialog">
            {{ t('apiKeyDialog.cancel') }}
          </button>
          <button class="api-key-dialog__button api-key-dialog__button--primary" type="button" @click="openApiKeyPage">
            {{ t('apiKeyDialog.getApiKey') }}
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const apiKeyUrl = 'https://www.compdf.com/compdf-portal/signin'

const closeDialog = () => {
  emit('update:modelValue', false)
}

const openApiKeyPage = () => {
  window.open(apiKeyUrl, '_blank', 'noopener,noreferrer')
  closeDialog()
}
</script>

<style scoped lang="scss">
.api-key-dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(12, 19, 31, 0.45);
}

.api-key-dialog {
  width: 480px;
  height: 222px;
  padding: 32px;
  border-radius: 9px;
  background: #fff;
  box-shadow: 0 16px 48px rgba(12, 19, 31, 0.18);
  font-family: 'Encode Sans', 'Microsoft YaHei', sans-serif;
}

.api-key-dialog__header {
  width: 416px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.api-key-dialog__title {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 396px;
  height: 24px;
  color: #0c131f;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.api-key-dialog__close {
  width: 20px;
  height: 20px;
  padding: 2px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  cursor: pointer;
}

.api-key-dialog__content {
  width: 416px;
  height: 102px;
  padding-top: 16px;
  color: #0c131f;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
}

.api-key-dialog__footer {
  width: 416px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.api-key-dialog__button {
  height: 32px;
  border: 0;
  border-radius: 3px;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  cursor: pointer;
}

.api-key-dialog__button--cancel {
  width: 72px;
  color: #0c131f;
  background: #e7e8e8;
}

.api-key-dialog__button--primary {
  width: 106px;
  margin-left: 12px;
  color: #fff;
  background: #396ffa;
}
</style>
