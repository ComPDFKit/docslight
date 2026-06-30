<template>
  <div class="asset-page">
    <div class="asset-header">
      <h1>{{ t('asset.title') }}</h1>
    </div>
    <div v-loading="loading" class="asset-content">
      <div v-if="panel" class="asset-panel shadows">
        <div class="account-row">
          <img v-if="panelUser.avatar" :src="panelUser.avatar" class="account-avatar" alt="avatar">
          <span v-else class="account-avatar account-avatar--text">{{ userInitial }}</span>
          <div class="account-info">
            <div class="account-name">{{ panelUser.username || '-' }}</div>
            <div class="account-email">{{ panelUser.email || '-' }}</div>
          </div>
          <div class="account-meta">
            <span class="account-tag">{{ panel.accountTypeName || accountTypeName }}</span>
            <span v-if="panel.showExpireTime && panel.expireTime" class="expire-time">
              {{ t('asset.expireTime') }} {{ formatDate(panel.expireTime) }}
            </span>
          </div>
        </div>

        <div v-if="products.length" class="product-grid">
          <div v-for="item in products" :key="item.productType" class="product-card">
            <div class="product-card__head">
              <div>
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-unit">{{ item.unitName || unitName(item.unit) }}</div>
              </div>
              <div class="product-count">{{ item.used }}/{{ item.total }}{{ item.unitName || unitName(item.unit) }}</div>
            </div>
            <el-progress :percentage="item.progress" :stroke-width="8" :show-text="false" />
            <div class="product-detail">
              <span>{{ t('asset.remaining') }} {{ item.remaining }}</span>
              <span v-if="item.withholding">{{ t('asset.withholding') }} {{ item.withholding }}</span>
            </div>
            <div v-if="item.productType === 'KNOWLEDGE_BASE' && (item.fileLimit || item.singleFileSizeLimitMB)" class="product-limit">
              <span v-if="item.fileLimit">{{ t('asset.fileLimit', { count: item.fileLimit }) }}</span>
              <span v-if="item.singleFileSizeLimitMB">{{ t('asset.singleFileSizeLimit', { size: item.singleFileSizeLimitMB }) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <Asset class="empty-state__icon" />
          <div>{{ t('asset.noAsset') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCookies } from 'vue3-cookies'
import { get } from '../utils/request'
import Asset from '../components/images/Asset.vue'

interface AssetUserDTO {
  avatar?: string
  username?: string
  email?: string
}

interface AssetProductDTO {
  productType: string
  productName: string
  unit?: string
  unitName?: string
  used: number
  withholding: number
  remaining: number
  total: number
  progress: number
  fileLimit?: number
  singleFileSizeLimitMB?: number
}

interface AssetPanelDTO {
  user?: AssetUserDTO
  accountType?: string
  accountTypeName?: string
  expireTime?: string
  overageTime?: string
  showExpireTime?: boolean
  products?: AssetProductDTO[]
  asset?: number
  assetTotal?: number
}

const { t } = useI18n()
const { cookies } = useCookies()
const loading = ref(false)
const panel = ref<AssetPanelDTO | null>(null)

const productLabels: Record<string, string> = {
  EXTRACT: t('asset.products.extract'),
  PARSE: t('asset.products.parse'),
  KNOWLEDGE_BASE: t('asset.products.knowledgeBase')
}

const getCookieUser = (): AssetUserDTO => {
  const rawUser = cookies.get('idp_user') || JSON.parse(sessionStorage.getItem('idp_user') || '{}')
  if (!rawUser) return {}
  if (typeof rawUser === 'string') {
    try {
      const user = JSON.parse(rawUser)
      return { avatar: user.avatar, username: user.username, email: user.email }
    } catch {
      return {}
    }
  }
  return { avatar: rawUser.avatar, username: rawUser.username, email: rawUser.email }
}

const panelUser = computed(() => panel.value?.user || getCookieUser())
const userInitial = computed(() => panelUser.value.username?.slice(0, 1).toUpperCase() || 'U')
const accountTypeName = computed(() => panel.value?.accountType === 'FORMAL' ? t('asset.formal') : t('asset.trial'))

const unitName = (unit?: string) => {
  if (unit === 'FILE') return t('asset.units.file')
  if (unit === 'MB') return 'MB'
  return t('asset.units.page')
}

const normalizeProduct = (item: AssetProductDTO) => {
  const total = Number(item.total || 0)
  const withholding = Number(item.withholding || 0)
  const remaining = Number(item.remaining || 0)
  const used = Number.isFinite(item.used) ? Number(item.used) : Math.max(total - remaining - withholding, 0)
  const progress = total > 0 ? Math.min(Math.max(Math.round((used / total) * 100), 0), 100) : 0
  return {
    ...item,
    productName: item.productName || productLabels[item.productType] || item.productType,
    used,
    withholding,
    remaining,
    total,
    progress: Number.isFinite(item.progress) ? Math.min(Math.max(Number(item.progress), 0), 100) : progress
  }
}

const products = computed(() => {
  if (panel.value?.products?.length) {
    return panel.value.products.map(normalizeProduct)
  }
  if (panel.value && Number.isFinite(panel.value.assetTotal)) {
    const total = Number(panel.value.assetTotal || 0)
    const remaining = Number(panel.value.asset || 0)
    return [normalizeProduct({
      productType: 'EXTRACT',
      productName: productLabels.EXTRACT,
      unit: 'PAGE',
      unitName: t('asset.units.page'),
      used: Math.max(total - remaining, 0),
      withholding: 0,
      remaining,
      total,
      progress: total > 0 ? Math.round(((total - remaining) / total) * 100) : 0
    })]
  }
  return []
})

const formatDate = (date: string) => dayjs(date).format('YYYY.MM.DD')

const getAssetPanel = async () => {
  loading.value = true
  try {
    const { data: { data } } = await get<AssetPanelDTO>('/api/idp/get-asset')
    panel.value = {
      ...data,
      user: data?.user || getCookieUser(),
      expireTime: data?.expireTime || data?.overageTime,
      showExpireTime: Boolean(data?.showExpireTime ?? (data?.expireTime || data?.overageTime))
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getAssetPanel()
})
</script>

<style lang="scss" scoped>
.asset-page {
  min-height: 100vh;
  background: #F3F6FF;
  font-family: 'Encode Sans';
}
.asset-header {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 24px 220px 24px 32px;
  border-bottom: 1px solid #E1E3E8;
  background: #fff;
  h1 {
    color: #0C131F;
    font-size: 20px;
    line-height: 32px;
    font-weight: 500;
    margin: 0;
  }
}
.asset-content {
  padding: 32px;
}
.asset-panel {
  max-width: 1120px;
  min-height: 360px;
  padding: 32px;
  border-radius: 10px;
  background: #fff;
}
.account-row {
  display: flex;
  align-items: center;
  padding-bottom: 28px;
  border-bottom: 1px solid #E1E3E8;
}
.account-avatar {
  width: 64px;
  height: 64px;
  min-width: 64px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 16px;
}
.account-avatar--text {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #232748;
  background: #FFE248;
  font-size: 26px;
  font-weight: 600;
}
.account-info {
  min-width: 0;
}
.account-name {
  color: #232748;
  font-size: 24px;
  line-height: 36px;
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.account-email {
  color: #888C94;
  font-size: 14px;
  line-height: 20px;
}
.account-meta {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #52555F;
  font-size: 14px;
}
.account-tag {
  padding: 4px 12px;
  border-radius: 14px;
  color: #396FFA;
  background: #EBF1FE;
  font-weight: 600;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin-top: 28px;
}
.product-card {
  min-height: 172px;
  padding: 20px;
  border: 1px solid #E1E3E8;
  border-radius: 8px;
  background: #fff;
  &:hover {
    border-color: #CDDBFF;
    box-shadow: 0px 4px 35px 0px #0029921A;
  }
  :deep(.el-progress-bar__outer) {
    background: #EBF1FE;
  }
  :deep(.el-progress-bar__inner) {
    background: #396FFA;
  }
}
.product-card__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}
.product-name {
  color: #232748;
  font-size: 18px;
  line-height: 28px;
  font-weight: 600;
}
.product-unit,
.product-detail,
.product-limit {
  color: #888C94;
  font-size: 12px;
  line-height: 18px;
}
.product-count {
  color: #232748;
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
  white-space: nowrap;
}
.product-detail {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}
.product-limit {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}
.empty-state {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #888C94;
  font-size: 14px;
}
.empty-state__icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  color: #B7BABF;
}
@media screen and (max-width: 1080px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  .account-row {
    align-items: flex-start;
    flex-wrap: wrap;
  }
  .account-meta {
    width: 100%;
    margin-left: 80px;
    margin-top: 12px;
    flex-wrap: wrap;
  }
}
</style>