<template>
  <div class="settings-figma-page">
    <section class="settings-figma-card">
      <h2 class="settings-figma-title">{{ t('settingsPage.title') }}</h2>

      <div class="settings-figma-form">
        <label class="settings-figma-field settings-figma-field--processing">
          <span class="settings-figma-label">
            {{ t('settingsPage.processingMode') }}
            <em>*</em>
          </span>
          <el-select
            ref="processingModeSelectRef"
            v-model="processingMode"
            class="settings-figma-select"
            :class="{ 'is-error': validationErrors.processingMode }"
            @change="handleProcessingModeChange"
          >
            <el-option :label="t('settingsPage.cloud')" value="Cloud" />
            <el-option :label="t('settingsPage.local')" value="Local" />
          </el-select>
        </label>

        <label v-if="processingMode === 'Cloud'" class="settings-figma-field settings-figma-field--api">
          <span class="settings-figma-label">
            {{ t('settingsPage.apiKey') }}
            <em>*</em>
          </span>
          <input
            v-model="apiKey"
            class="settings-figma-input"
            :class="{ 'is-error': validationErrors.apiKey }"
            type="text"
            :placeholder="t('settingsPage.apiKeyPlaceholder')"
            @input="clearValidationError('apiKey')"
          >
        </label>

        <template v-if="processingMode === 'Local'">
          <label class="settings-figma-field settings-figma-field--local">
            <span class="settings-figma-label">
              {{ t('settingsPage.modelProvider') }}
              <em>*</em>
            </span>
            <el-select
              ref="modelProviderSelectRef"
              v-model="modelProvider"
              class="settings-figma-select settings-figma-select--full"
              :class="{ 'is-error': validationErrors.modelProvider }"
              :teleported="false"
              @change="handleModelProviderChange"
            >
              <el-option label="ollama" value="ollama" />
              <el-option label="openai-compatible" value="openai-compatible" />
              <el-option label="openai" value="openai" />
            </el-select>
          </label>

          <label class="settings-figma-field settings-figma-field--local">
            <span class="settings-figma-label">
              {{ t('settingsPage.modelName') }}
              <em>*</em>
            </span>
            <input
              v-model="modelName"
              class="settings-figma-input"
              :class="{ 'is-error': validationErrors.modelName }"
              type="text"
              :placeholder="t('settingsPage.modelNamePlaceholder')"
              @input="clearValidationError('modelName')"
            >
          </label>

          <label class="settings-figma-field settings-figma-field--local">
            <span class="settings-figma-label">
              {{ t('settingsPage.modelBaseUrl') }}
              <em>*</em>
            </span>
            <input
              v-model="modelBaseUrl"
              class="settings-figma-input"
              :class="{ 'is-error': validationErrors.modelBaseUrl }"
              type="text"
              :placeholder="t('settingsPage.modelBaseUrlPlaceholder')"
              @input="clearValidationError('modelBaseUrl')"
            >
          </label>

          <label class="settings-figma-field settings-figma-field--local">
            <span class="settings-figma-label">
              {{ t('settingsPage.modelApiKey') }}
              <em>*</em>
            </span>
            <input
              v-model="modelApiKey"
              class="settings-figma-input"
              :class="{ 'is-error': validationErrors.modelApiKey }"
              type="text"
              :placeholder="t('settingsPage.modelApiKeyPlaceholder')"
              @input="clearValidationError('modelApiKey')"
            >
          </label>
        </template>

        <button v-if="processingMode === 'Cloud'" class="settings-figma-tip" type="button" @click="openExternalLink(apiKeyUrl)">
          <svg class="settings-figma-tip__icon" width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M7.64297 9.02409C5.80202 9.02409 4.30963 10.5165 4.30963 12.3574C4.30963 14.1984 5.80202 15.6908 7.64297 15.6908C9.48392 15.6908 10.9763 14.1984 10.9763 12.3574C10.9763 10.5165 9.48392 9.02409 7.64297 9.02409ZM2.64297 12.3574C2.64297 9.596 4.88154 7.35742 7.64297 7.35742C8.72283 7.35742 9.72273 7.69975 10.5401 8.2818L15.3033 3.51859L16.4818 4.6971L15.5979 5.58098L16.7764 6.75949L15.5979 7.938L14.4194 6.75949L13.8302 7.34875L15.597 9.11561L14.4185 10.2941L12.6516 8.52726L11.7186 9.46031C12.3006 10.2777 12.643 11.2776 12.643 12.3574C12.643 15.1188 10.4044 17.3574 7.64297 17.3574C4.88154 17.3574 2.64297 15.1188 2.64297 12.3574Z" fill="#396FFA"/>
          </svg>
          <span class="settings-figma-tip__text">{{ t('settingsPage.getFreeApiKey') }}</span>
          <svg class="settings-figma-tip__icon" width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M2.5 17.5L2.5 2.5L8.40909 2.5V4.16667H4.16667L4.16667 15.8333H15.8333V11.5909H17.5V17.5H2.5ZM8.82149 10L14.6548 4.16667L10.8333 4.16667V2.5L17.5 2.5L17.5 9.16667H15.8333V5.34518L10 11.1785L8.82149 10Z" fill="#396FFA"/>
          </svg>
        </button>
      </div>

      <div class="settings-figma-actions">
        <button class="settings-figma-button settings-figma-button--cancel" type="button" @click="resetForm">{{ t('settingsPage.cancel') }}</button>
        <button class="settings-figma-button settings-figma-button--save" type="button" @click="saveForm">{{ t('settingsPage.save') }}</button>
      </div>
    </section>

    <section v-if="showSettingsBanner" class="settings-figma-banner" aria-label="DocSlight banner">
      <div class="settings-figma-banner__ellipse settings-figma-banner__ellipse--left" aria-hidden="true">
        <svg width="217" height="96" viewBox="0 0 217 96" fill="none" xmlns="http://www.w3.org/2000/svg">
          <g opacity="0.7" filter="url(#settings-banner-left-blur)">
            <path d="M-56.5981 82.3787C-64.3042 69.0314 -42.0747 3.47998 13.5056 -28.6093C69.0859 -60.6986 136.97 -47.1742 144.676 -33.8269C152.382 -20.4796 99.6197 -7.81256 44.0394 24.2767C-11.5409 56.366 -48.892 95.726 -56.5981 82.3787Z" fill="#00D3F3"/>
          </g>
          <defs>
            <filter id="settings-banner-left-blur" x="-128.679" y="-118.996" width="344.707" height="274.646" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
              <feFlood flood-opacity="0" result="BackgroundImageFix"/>
              <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
              <feGaussianBlur stdDeviation="35.3" result="effect1_foregroundBlur"/>
            </filter>
          </defs>
        </svg>
      </div>
      <div class="settings-figma-banner__ellipse settings-figma-banner__ellipse--left-center" aria-hidden="true">
        <svg width="274" height="96" viewBox="0 0 274 96" fill="none" xmlns="http://www.w3.org/2000/svg">
          <g opacity="0.7" filter="url(#settings-banner-left-center-blur)">
            <path d="M18.553 40.9237C21.5644 53.1612 71.795 79.2461 122.754 66.7063C173.713 54.1666 206.102 7.75081 203.091 -4.48669C200.08 -16.7242 161.781 5.67802 110.822 18.2178C59.8631 30.7575 15.5417 28.6862 18.553 40.9237Z" fill="#001CF3"/>
          </g>
          <defs>
            <filter id="settings-banner-left-center-blur" x="-52.192" y="-78.6293" width="326.077" height="219.262" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
              <feFlood flood-opacity="0" result="BackgroundImageFix"/>
              <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
              <feGaussianBlur stdDeviation="35.3" result="effect1_foregroundBlur"/>
            </filter>
          </defs>
        </svg>
      </div>
      <div class="settings-figma-banner__ellipse settings-figma-banner__ellipse--right" aria-hidden="true">
        <svg width="17" height="96" viewBox="0 0 17 96" fill="none" xmlns="http://www.w3.org/2000/svg">
          <g opacity="0.7" filter="url(#settings-banner-right-blur)">
            <path d="M70.745 23.3915C73.7564 35.629 123.987 61.7139 174.946 49.1741C225.905 36.6344 258.294 -9.78142 255.283 -22.0189C252.272 -34.2564 213.973 -11.8542 163.014 0.685534C112.055 13.2253 67.7337 11.154 70.745 23.3915Z" fill="#001CF3"/>
          </g>
          <defs>
            <filter id="settings-banner-right-blur" x="-2.28882e-05" y="-96.1615" width="326.077" height="219.262" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
              <feFlood flood-opacity="0" result="BackgroundImageFix"/>
              <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
              <feGaussianBlur stdDeviation="35.3" result="effect1_foregroundBlur"/>
            </filter>
          </defs>
        </svg>
      </div>
      <div class="settings-figma-banner__header">
        <div class="settings-figma-banner__content">
          <div class="settings-figma-banner__icon-surface" aria-hidden="true">
            <svg class="settings-figma-banner__icon" width="49" height="49" viewBox="0 0 49 49" fill="none" xmlns="http://www.w3.org/2000/svg">
              <g filter="url(#settings-banner-icon-shadow)">
                <path d="M19.8691 7.54688C22.2852 7.54707 24.2441 9.50575 24.2441 11.9219V16.5879C24.2439 19.0038 22.2851 20.9627 19.8691 20.9629H18.9941V24.0254C18.9944 24.8306 19.6478 25.4844 20.4531 25.4844H23.9531V25.0469C23.9531 22.6306 25.9119 20.6719 28.3281 20.6719H32.9941C35.4102 20.6721 37.3691 22.6307 37.3691 25.0469V29.7129C37.3689 32.1288 35.4101 34.0877 32.9941 34.0879H28.3281C25.912 34.0879 23.9533 32.129 23.9531 29.7129V28.4004H20.4531C18.037 28.4004 16.0783 26.4415 16.0781 24.0254V20.9629H15.2031C12.787 20.9629 10.8283 19.004 10.8281 16.5879V11.9219C10.8281 9.50563 12.7869 7.54688 15.2031 7.54688H19.8691ZM28.3281 23.5879C27.5227 23.5879 26.8691 24.2415 26.8691 25.0469V29.7129C26.8694 30.5181 27.5228 31.1719 28.3281 31.1719H32.9941C33.7993 31.1717 34.4529 30.518 34.4531 29.7129V25.0469C34.4531 24.2416 33.7994 23.5881 32.9941 23.5879H28.3281ZM15.2031 10.4629C14.3977 10.4629 13.7441 11.1165 13.7441 11.9219V16.5879C13.7444 17.3931 14.3978 18.0469 15.2031 18.0469H19.8691C20.6743 18.0467 21.3279 17.393 21.3281 16.5879V11.9219C21.3281 11.1166 20.6744 10.4631 19.8691 10.4629H15.2031Z" fill="white"/>
              </g>
              <defs>
                <filter id="settings-banner-icon-shadow" x="0" y="0" width="48.1973" height="48.1973" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                  <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                  <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                  <feOffset dy="3.28125"/>
                  <feGaussianBlur stdDeviation="5.41406"/>
                  <feComposite in2="hardAlpha" operator="out"/>
                  <feColorMatrix type="matrix" values="0 0 0 0 0.588905 0 0 0 0 0.890375 0 0 0 0 1 0 0 0 0.38 0"/>
                  <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow"/>
                  <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow" result="shape"/>
                </filter>
              </defs>
            </svg>
          </div>
          <div class="settings-figma-banner__text">
            <h3 class="settings-figma-banner__title">{{ t('settingsPage.docSlightTitle') }}</h3>
            <p class="settings-figma-banner__desc">{{ t('settingsPage.docSlightDesc') }}</p>
          </div>
        </div>
        <div class="settings-figma-banner__actions">
          <button class="settings-figma-banner__button settings-figma-banner__button--primary" type="button" @click="openExternalLink(docSlightLearnMoreUrl)">{{ t('settingsPage.learnMore') }}</button>
          <button class="settings-figma-banner__button settings-figma-banner__button--ghost" type="button" @click="openExternalLink(contactSalesUrl)">{{ t('settingsPage.bookDemo') }}</button>
        </div>
      </div>
      <button class="settings-figma-banner__close" type="button" aria-label="close" @click="showSettingsBanner = false">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd" clip-rule="evenodd" d="M0.292893 0.292893C0.683417 -0.0976311 1.31658 -0.0976311 1.70711 0.292893L7 5.58579L12.2929 0.292893C12.6834 -0.0976311 13.3166 -0.0976311 13.7071 0.292893C14.0976 0.683417 14.0976 1.31658 13.7071 1.70711L8.41421 7L13.7071 12.2929C14.0976 12.6834 14.0976 13.3166 13.7071 13.7071C13.3166 14.0976 12.6834 14.0976 12.2929 13.7071L7 8.41421L1.70711 13.7071C1.31658 14.0976 0.683417 14.0976 0.292893 13.7071C-0.0976311 13.3166 -0.0976311 12.6834 0.292893 12.2929L5.58579 7L0.292893 1.70711C-0.0976311 1.31658 -0.0976311 0.683417 0.292893 0.292893Z" fill="white" fill-opacity="0.8"/>
        </svg>
      </button>
    </section>

    <div v-if="showLeaveDialog" class="settings-leave-dialog__mask">
      <div class="settings-leave-dialog" role="dialog" aria-modal="true" :aria-label="t('settingsPage.leaveDialogTitle')">
        <div class="settings-leave-dialog__header">
          <div class="settings-leave-dialog__title-wrap">
            <span class="settings-leave-dialog__icon" aria-hidden="true">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12.0001 23C18.0753 23 23.0001 18.0751 23.0001 12C23.0001 5.92487 18.0753 1 12.0001 1C5.92499 1 1.00012 5.92487 1.00012 12C1.00012 18.0751 5.92499 23 12.0001 23ZM10.9961 8.50002V6.49611H13V8.50002H10.9961ZM13 10L13 17.5H11V10L13 10Z" fill="#396FFA"/>
              </svg>
            </span>
            <h3 class="settings-leave-dialog__title">{{ t('settingsPage.leaveDialogTitle') }}</h3>
          </div>
          <button class="settings-leave-dialog__close" type="button" :aria-label="t('common.cancel')" @click="handleStayOnPage">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4.70013 3.75781L7.99996 7.05764L11.2998 3.75781L12.2426 4.70062L8.94277 8.00045L12.2426 11.3003L11.2998 12.2431L7.99996 8.94326L4.70013 12.2431L3.75732 11.3003L7.05716 8.00045L3.75732 4.70062L4.70013 3.75781Z" fill="#0C131F" fill-opacity="0.6"/>
            </svg>
          </button>
        </div>
        <p class="settings-leave-dialog__desc">{{ t('settingsPage.leaveDialogDesc') }}</p>
        <div class="settings-leave-dialog__actions">
          <button class="settings-leave-dialog__button settings-leave-dialog__button--cancel" type="button" @click="handleStayOnPage">
            {{ t('settingsPage.leaveDialogCancel') }}
          </button>
          <button class="settings-leave-dialog__button settings-leave-dialog__button--confirm" type="button" @click="handleConfirmLeave">
            {{ t('settingsPage.leaveDialogConfirm') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { SelectInstance } from 'element-plus'
import { onBeforeRouteLeave } from 'vue-router'
import { get, post } from '@/utils/request'

const { t } = useI18n()

const compdfWebsite = 'https://www.compdf.com'
const apiKeyUrl = `${compdfWebsite}/compdf-portal/signin`
const docSlightLearnMoreUrl = `${compdfWebsite}/ai/docslight`
const contactSalesUrl = `${compdfWebsite}/contact-sales`

type SettingsForm = {
  processingMode: 'Cloud' | 'Local'
  apiKey: string
  modelProvider: 'ollama' | 'openai-compatible' | 'openai'
  modelName: string
  modelBaseUrl: string
  modelApiKey: string
}

type DocSlightSettingsDTO = {
  apikey?: string
  model?: string
  localLlmProvider?: string
  localLlmModel?: string
  localLlmBaseUrl?: string
  localLlmApiKey?: string
}

const defaultSettings = (): SettingsForm => ({
  processingMode: 'Cloud',
  apiKey: '',
  modelProvider: 'ollama',
  modelName: '',
  modelBaseUrl: '',
  modelApiKey: ''
})

const backendToForm = (settings?: DocSlightSettingsDTO | null): SettingsForm => ({
  processingMode: settings?.model?.toLowerCase() === 'local' ? 'Local' : 'Cloud',
  apiKey: settings?.apikey || '',
  modelProvider: (settings?.localLlmProvider as SettingsForm['modelProvider']) || 'ollama',
  modelName: settings?.localLlmModel || '',
  modelBaseUrl: settings?.localLlmBaseUrl || '',
  modelApiKey: settings?.localLlmApiKey || ''
})

const formToBackend = (): DocSlightSettingsDTO => ({
  apikey: apiKey.value,
  model: processingMode.value,
  localLlmProvider: modelProvider.value,
  localLlmModel: modelName.value,
  localLlmBaseUrl: modelBaseUrl.value,
  localLlmApiKey: modelApiKey.value
})

const latestSavedSettings = ref<SettingsForm>(defaultSettings())

const processingMode = ref<SettingsForm['processingMode']>('Cloud')
const apiKey = ref('')
const modelProvider = ref<SettingsForm['modelProvider']>('ollama')
const modelName = ref('')
const modelBaseUrl = ref('')
const modelApiKey = ref('')
const processingModeSelectRef = ref<SelectInstance | null>(null)
const modelProviderSelectRef = ref<SelectInstance | null>(null)
const showLeaveDialog = ref(false)
const showSettingsBanner = ref(true)
const pendingLeaveAction = ref<null | { confirm: () => void, cancel: () => void }>(null)
const ignoreLeaveGuard = ref(false)
const validationErrors = ref<Record<keyof SettingsForm, boolean>>({
  processingMode: false,
  apiKey: false,
  modelProvider: false,
  modelName: false,
  modelBaseUrl: false,
  modelApiKey: false
})

const currentFormSettings = computed<SettingsForm>(() => ({
  processingMode: processingMode.value,
  apiKey: apiKey.value,
  modelProvider: modelProvider.value,
  modelName: modelName.value,
  modelBaseUrl: modelBaseUrl.value,
  modelApiKey: modelApiKey.value
}))

const hasUnsavedChanges = computed(() => JSON.stringify(currentFormSettings.value) !== JSON.stringify(latestSavedSettings.value))

const applyFormSettings = (settings: SettingsForm) => {
  processingMode.value = settings.processingMode
  apiKey.value = settings.apiKey
  modelProvider.value = settings.modelProvider
  modelName.value = settings.modelName
  modelBaseUrl.value = settings.modelBaseUrl
  modelApiKey.value = settings.modelApiKey
  clearValidationErrors()
}

const handleProcessingModeChange = () => {
  clearValidationError('processingMode')
  clearValidationErrors()
  nextTick(() => {
    if (processingModeSelectRef.value?.expanded) {
      processingModeSelectRef.value.toggleMenu()
    }
    processingModeSelectRef.value?.blur?.()
  })
}

const handleModelProviderChange = () => {
  clearValidationError('modelProvider')
  nextTick(() => {
    if (modelProviderSelectRef.value?.expanded) {
      modelProviderSelectRef.value.toggleMenu()
    }
    modelProviderSelectRef.value?.blur?.()
  })
}

const loadSettings = async () => {
  const { data: { data } } = await get<DocSlightSettingsDTO>('/api/idp/get-settings')
  const mapped = backendToForm(data)
  latestSavedSettings.value = { ...mapped }
  applyFormSettings(mapped)
}

const resetForm = () => {
  applyFormSettings(latestSavedSettings.value)
}

const openExternalLink = (url: string) => {
  window.open(url, '_blank', 'noopener,noreferrer')
}

const clearValidationError = (field: keyof SettingsForm) => {
  validationErrors.value[field] = false
}

const clearValidationErrors = () => {
  ;(Object.keys(validationErrors.value) as Array<keyof SettingsForm>).forEach((field) => {
    validationErrors.value[field] = false
  })
}

const validateRequiredFields = () => {
  clearValidationErrors()
  const requiredFields: Array<{ key: keyof SettingsForm, label: string, value: string }> = processingMode.value === 'Cloud'
    ? [
        { key: 'processingMode', label: t('settingsPage.processingMode'), value: processingMode.value },
        { key: 'apiKey', label: t('settingsPage.apiKey'), value: apiKey.value }
      ]
    : [
        { key: 'processingMode', label: t('settingsPage.processingMode'), value: processingMode.value },
        { key: 'modelProvider', label: t('settingsPage.modelProvider'), value: modelProvider.value },
        { key: 'modelName', label: t('settingsPage.modelName'), value: modelName.value },
        { key: 'modelBaseUrl', label: t('settingsPage.modelBaseUrl'), value: modelBaseUrl.value },
        { key: 'modelApiKey', label: t('settingsPage.modelApiKey'), value: modelApiKey.value }
      ]
  const missingFields = requiredFields.filter(item => !String(item.value || '').trim())
  if (!missingFields.length) {
    return true
  }
  missingFields.forEach((item) => {
    validationErrors.value[item.key] = true
  })
  const missingField = missingFields[0]
  ElMessage.error(t('settingsPage.requiredField', { field: missingField.label }))
  return false
}

const saveForm = async () => {
  if (!validateRequiredFields()) {
    return
  }
  const { data: { data } } = await post<DocSlightSettingsDTO>('/api/idp/update-settings', formToBackend())
  const mapped = backendToForm(data)
  latestSavedSettings.value = { ...mapped }
  applyFormSettings(mapped)
  ElMessage.success(t('settingsPage.saved'))
}

const handleStayOnPage = () => {
  pendingLeaveAction.value?.cancel()
  showLeaveDialog.value = false
  pendingLeaveAction.value = null
}

const handleConfirmLeave = () => {
  const leaveAction = pendingLeaveAction.value
  showLeaveDialog.value = false
  pendingLeaveAction.value = null
  ignoreLeaveGuard.value = true
  showSettingsBanner.value = true
  leaveAction?.confirm()
}

const handleBeforeUnload = (event: BeforeUnloadEvent) => {
  if (!hasUnsavedChanges.value) {
    return
  }
  event.preventDefault()
  event.returnValue = ''
}

onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  try {
    await loadSettings()
  } catch (error) {
    latestSavedSettings.value = defaultSettings()
    applyFormSettings(latestSavedSettings.value)
    ElMessage.error(t('common.networkError'))
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave((to, from, next) => {
  if (ignoreLeaveGuard.value) {
    ignoreLeaveGuard.value = false
    showSettingsBanner.value = true
    next()
    return
  }
  if (!hasUnsavedChanges.value) {
    showSettingsBanner.value = true
    next()
    return
  }
  pendingLeaveAction.value = {
    confirm: () => next(),
    cancel: () => next(false)
  }
  showLeaveDialog.value = true
})
</script>

<style lang="scss" scoped>
.settings-figma-page {
  position: relative;
  min-height: calc(100vh - 56px);
  padding: 32px 32px 128px;
  box-sizing: border-box;
  background: #f3f6ff;
  font-family: 'Encode Sans', sans-serif;
}

.settings-figma-card {
  width: 593px;
  min-height: 384px;
  max-width: none;
  padding: 24px 32px 32px;
  overflow: hidden;
  border-radius: 8px;
  background: #fff;
  box-sizing: border-box;
  flex: 0 0 auto;
}

.settings-figma-title {
  width: 88px;
  height: 28px;
  margin: 0 0 24px;
  color: #0c131f;
  font-family: 'Encode Sans Expanded', 'Encode Sans', sans-serif;
  font-size: 20px;
  line-height: 28px;
  font-weight: 600;
  letter-spacing: 0;
  white-space: nowrap;
}

.settings-figma-form {
  width: 529px;
  height: auto;
}

.settings-figma-field {
  display: block;
  margin: 0;
  padding: 0;
}

.settings-figma-field--processing {
  width: 514px;
  height: 86px;
}

.settings-figma-field--api {
  width: 529px;
  height: 86px;
}

.settings-figma-field--local {
  width: 529px;
  height: 86px;
}

.settings-figma-label {
  display: flex;
  align-items: flex-start;
  height: 30px;
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  white-space: nowrap;

  em {
    margin-left: 2px;
    color: #d44040;
    font-style: normal;
  }
}

.settings-figma-select {
  display: block;
  width: 514px;
  height: 32px;
}

.settings-figma-select--full {
  width: 529px;
}

:deep(.settings-figma-select .el-select__wrapper) {
  width: 514px;
  min-height: 32px;
  height: 32px;
  border-radius: 3px;
  box-shadow: 0 0 0 1px #dcdde1 inset;
  padding: 0 8px;
  background: #fff;
  box-sizing: border-box;
}

:deep(.settings-figma-select--full .el-select__wrapper) {
  width: 529px;
}

:deep(.settings-figma-select .el-select__selected-item) {
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
}

:deep(.settings-figma-select .el-select__caret) {
  color: #a7abb2;
}

:deep(.settings-figma-select.is-error .el-select__wrapper) {
  box-shadow: 0 0 0 1px #d44040 inset;
}

.settings-figma-input {
  display: block;
  width: 529px;
  height: 32px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  padding: 0 8px;
  box-sizing: border-box;
  color: #0c131f;
  background: #fff;
  font-size: 14px;
  line-height: 22px;
  outline: none;

  &::placeholder {
    color: #9ca3af;
  }

  &:focus {
    border-color: #396ffa;
  }

  &.is-error {
    border-color: #d44040;
  }
}

.settings-figma-tip {
  width: 529px;
  height: 48px;
  margin: 0;
  border: 1px solid #618cfb;
  border-radius: 6px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #396ffa;
  background: #f5f7ff;
  box-sizing: border-box;
  cursor: pointer;
  appearance: none;
}

.settings-figma-tip__icon {
  width: 20px;
  height: 20px;
  min-width: 20px;
  max-width: 20px;
  flex: 0 0 20px;
  color: #396ffa;
}

.settings-figma-tip__text {
  flex: 1;
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  text-align: left;
  white-space: nowrap;
}

.settings-figma-actions {
  width: 529px;
  height: 32px;
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.settings-figma-button {
  height: 32px;
  border-radius: 3px;
  padding: 0 16px;
  box-sizing: border-box;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  white-space: nowrap;
  cursor: pointer;
  appearance: none;
}

.settings-figma-button--cancel {
  width: 72px;
  border: 1px solid #396ffa;
  color: #396ffa;
  background: #fff;
}

.settings-figma-button--save {
  width: 62px;
  border: 1px solid #396ffa;
  color: #fff;
  background: #396ffa;
}

.settings-figma-banner {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 96px;
  overflow: hidden;
  background: linear-gradient(128deg, #0e063e 0%, #090b16 100%);
  box-sizing: border-box;
  box-shadow: inset 0 0 15.7px #618cfb;
}

.settings-figma-banner::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 2px;
  background: linear-gradient(90deg, rgba(97, 140, 251, 0.65) 0%, rgba(97, 140, 251, 1) 50%, rgba(97, 140, 251, 0.65) 100%);
  box-shadow: 0 0 12px rgba(97, 140, 251, 0.95);
  pointer-events: none;
}

.settings-figma-banner__ellipse {
  position: absolute;
  pointer-events: none;
}

.settings-figma-banner__ellipse svg {
  display: block;
}

.settings-figma-banner__ellipse--left {
  left: 0;
  top: 0;
}

.settings-figma-banner__ellipse--left-center {
  left: 0;
  top: 0;
}

.settings-figma-banner__ellipse--right {
  right: 0;
  top: 0;
}

.settings-figma-banner__header {
  position: relative;
  z-index: 1;
  width: 916px;
  height: 58px;
  margin: 19px auto;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.settings-figma-banner__content {
  width: 654px;
  height: 58px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.settings-figma-banner__icon-surface {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(91, 132, 255, 0.96) 0%, rgba(93, 167, 255, 0.78) 100%);
  border: 1px solid rgba(132, 240, 255, 0.92);
  box-shadow: inset 0 6px 18px rgba(255, 255, 255, 0.22);
}

.settings-figma-banner__icon {
  display: block;
  width: 49px;
  height: 49px;
}

.settings-figma-banner__text {
  width: 578px;
}

.settings-figma-banner__title {
  margin: 0;
  font-family: 'Encode Sans', sans-serif;
  font-size: 20px;
  line-height: 32px;
  font-weight: 600;
  background: linear-gradient(96deg, #396ffa 0%, #56f9ca 41.6461%, #396ffa 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.settings-figma-banner__desc {
  margin: 2px 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 16px;
  line-height: 24px;
  font-weight: 400;
}

.settings-figma-banner__actions {
  width: 214px;
  height: 32px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.settings-figma-banner__button {
  height: 32px;
  border-radius: 16px;
  padding: 0 12px;
  font-size: 14px;
  line-height: 20px;
  font-weight: 400;
  cursor: pointer;
  appearance: none;
  white-space: nowrap;
}

.settings-figma-banner__button--primary {
  width: 95px;
  border: 0;
  color: #fff;
  background: linear-gradient(90deg, #396ffa 0%, #45dfbd 100%);
}

.settings-figma-banner__button--ghost {
  width: 107px;
  border: 1px solid rgba(57, 111, 250, 0.4);
  color: rgba(255, 255, 255, 0.8);
  background: transparent;
}

.settings-figma-banner__close {
  position: absolute;
  z-index: 1;
  right: 40px;
  top: 36px;
  width: 24px;
  height: 24px;
  border: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  cursor: pointer;
  appearance: none;
}

.settings-leave-dialog__mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(12, 19, 31, 0.18);
  backdrop-filter: blur(2px);
}

.settings-leave-dialog {
  position: relative;
  width: 480px;
  height: 222px;
  border-radius: 9px;
  padding: 32px;
  box-sizing: border-box;
  background: #fff;
  box-shadow: 0 20px 60px rgba(12, 19, 31, 0.16);
}

.settings-leave-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.settings-leave-dialog__title-wrap {
  display: flex;
  align-items: center;
  gap: 18px;
}

.settings-leave-dialog__icon {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 32px;
}

.settings-leave-dialog__icon svg {
  width: 24px;
  height: 24px;
  display: block;
}

.settings-leave-dialog__title {
  margin: 0;
  color: #0c131f;
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
}

.settings-leave-dialog__close {
  width: 16px;
  height: 16px;
  border: 0;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  cursor: pointer;
  appearance: none;
}

.settings-leave-dialog__close svg {
  width: 16px;
  height: 16px;
  display: block;
}

.settings-leave-dialog__desc {
  width: 385px;
  margin: 28px 0 0;
  color: rgba(12, 19, 31, 0.6);
  font-size: 16px;
  line-height: 24px;
  font-weight: 400;
}

.settings-leave-dialog__actions {
  position: absolute;
  right: 32px;
  bottom: 32px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.settings-leave-dialog__button {
  min-width: 100px;
  height: 48px;
  border-radius: 6px;
  padding: 0 24px;
  box-sizing: border-box;
  font-size: 16px;
  line-height: 24px;
  font-weight: 400;
  cursor: pointer;
  appearance: none;
}

.settings-leave-dialog__button--cancel {
  border: 1px solid #dcdde1;
  color: #0c131f;
  background: #f3f3f4;
}

.settings-leave-dialog__button--confirm {
  border: 1px solid #396ffa;
  color: #fff;
  background: #396ffa;
}

@media (max-width: 720px) {
  .settings-figma-page {
    padding: 16px 16px 128px;
  }

  .settings-figma-card {
    width: 100%;
    height: auto;
  }

  .settings-figma-form,
  .settings-figma-field--processing,
  .settings-figma-field--api,
  .settings-figma-field--local,
  .settings-figma-select,
  .settings-figma-select--full,
  .settings-figma-input,
  .settings-figma-tip,
  .settings-figma-actions {
    width: 100%;
  }

  :deep(.settings-figma-select .el-select__wrapper) {
    width: 100%;
  }

  .settings-figma-banner {
    padding: 16px;
  }

  .settings-figma-banner__header {
    width: calc(100% - 48px);
    height: auto;
    margin: 12px 0 0;
    align-items: flex-start;
  }

  .settings-figma-banner__content {
    width: calc(100% - 36px);
    gap: 12px;
  }

  .settings-figma-banner__text {
    width: auto;
  }

  .settings-figma-banner__title {
    font-size: 16px;
    line-height: 24px;
  }

  .settings-figma-banner__desc,
  .settings-figma-banner__actions {
    display: none;
  }

  .settings-figma-banner__close {
    right: 16px;
    top: 16px;
  }

  .settings-leave-dialog {
    width: calc(100vw - 32px);
    height: auto;
    padding: 24px;
  }

  .settings-leave-dialog__desc {
    width: auto;
  }

  .settings-leave-dialog__actions {
    margin-top: 32px;
    gap: 12px;
  }

  .settings-leave-dialog__button {
    min-width: 92px;
    height: 44px;
    padding: 0 20px;
  }
}
</style>
