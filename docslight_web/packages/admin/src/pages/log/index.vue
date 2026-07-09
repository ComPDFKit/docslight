<script lang='ts' setup>
import dayjs from "dayjs"
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import { usePagination } from '@@/composables/usePagination'
import { ref } from 'vue'
import { request } from '@/http/axios'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message-box.css'
import 'element-plus/theme-chalk/el-message.css'
import Search from '@@/components/Image/Search.vue'
import Reset from '@@/components/Image/Reset.vue'
import Calendar from './components/Calendar.vue'

const loading = ref<boolean>(false)
const { paginationData } = usePagination()

const { t } = useI18n()
// 定义文件数据类型
interface FileData {
  id: string
  name: string
  size: number
  type: string
  kb_id: string
  location: string
  create_time?: number
}
const actionMap = ref({
  修改: t('log.modify'),
  添加: t('log.add'),
  删除: t('log.delete')
})
const calendarShow = ref(false)
// 查询文件列表
const tableData = ref<FileData[]>([])
const searchFormRef = ref<FormInstance | null>(null)
interface SearchData {
  start_time: number | '';
  end_time: number | '';
  name: string;
  email: string;
}
const time = ref()
const searchData = reactive<SearchData>({
  start_time: '',
  end_time: '',
  name: '',
  email: ''
})

interface ReqData {
  code: number
  data: any
  message: string
}
const handleDate = ([start, end, show]: [Date, Date, string]) => {
  time.value = start + ' ~ ' + end
  if (show === '1') {
    calendarShow.value = false
  }
  searchData.start_time = new Date(start).getTime()
  searchData.end_time = new Date(end).getTime()
}
// 获取文件列表数据
const getTableData = async () => {
  loading.value = true
  // 调用获取文件列表API
  const data: ReqData = await request({
    url: '/api/v1/logs/filter',
    method: 'POST',
    data: {
      time_range: {
          start_time: searchData.start_time,
          end_time: searchData.end_time
      },
      email: searchData.email,
      user: searchData.name
    }})
    if (data.code === 0 && data.message === 'Logs fetched successfully') {
      tableData.value = data.data
      paginationData.total = tableData.value.length
    } else {
      tableData.value = []
    }
    loading.value = false
}

// 搜索处理
function handleSearch() {
  paginationData.currentPage === 1 ? getTableData() : (paginationData.currentPage = 1)
}

// 当前页数据（通过计算属性截取）
const paginatedData = computed(() => {
  const start = (paginationData.currentPage - 1) * paginationData.pageSize
  const end = start + paginationData.pageSize
  return tableData.value.slice(start, end)
})

// 页码或页大小变化事件
const handlePageChange = (page: number) => {
  paginationData.currentPage = page
}
const handleSizeChange = (size: number) => {
  paginationData.pageSize = size
  paginationData.currentPage = 1 // 重置为第一页
}
// 重置搜索
function resetSearch() {
  searchFormRef.value?.resetFields()
  searchData.start_time = ''
  searchData.end_time = ''
  time.value = ''
  handleSearch()
}

// 监听分页参数的变化
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, { immediate: true })

const handleClick = () => {
  calendarShow.value = false
}
// 确保页面挂载和激活时获取数据
onMounted(() => {
  getTableData()
  addEventListener('click', handleClick)
})
onUnmounted(() => {
  removeEventListener('click', handleClick)
})

// 当从其他页面切换回来时刷新数据
onActivated(() => {
  getTableData()
})
// 获取解析状态对应的标签类型
function getParseStatusType(type: string) {
  if (type === '修改') return 'border-[#FFC0614D] bg-[#FFF4E5] text-[#F09004]'
  if (type === '添加') return 'border-[#00CF854D] bg-[#E2F7EF] text-[#00CF85]'
  if (type === '删除') return 'border-[#F871714D] bg-[#FBEDED] text-[#F87171]'
}
</script>

<template>
  <div class="app-container">
    <h1 class="text-32px leading-48px font-600 text-brand-0 mb-40px">{{ $t('log.title') }}</h1>
    <el-card v-loading="loading" shadow="never" class="search-wrapper calendar">
      <el-form ref="searchFormRef" :inline="true" :model="searchData">
        <el-form-item prop="time" :label="$t('log.time')">
          <div @click.stop="calendarShow = !calendarShow" class="relative">
            <Calendar v-show="calendarShow" :userFirstLogin="'2024-01-01'" @checkedDate="handleDate" />
            <div v-show="time" class="text-xs text-brand-3 py-10px px-12px bg-white border border-[#CED6E1] rounded-4px">{{ time }}</div>
            <div v-show="!time" class="text-xs text-brand-3 py-10px px-12px bg-white border border-[#CED6E1] rounded-4px cursor-pointer min-w-201px">{{ t('log.timePlaceholder') }}</div>
          </div>
        </el-form-item>
        <el-form-item prop="name" :label="$t('log.name')">
          <el-input v-model="searchData.name" :placeholder="$t('log.namePlaceholder')" />
        </el-form-item>
        <el-form-item prop="email" :label="$t('log.email')">
          <el-input v-model="searchData.email" :placeholder="$t('log.emailPlaceholder')" />
        </el-form-item>
        <el-form-item class="footer">
          <div class="rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center font-500 hover:bg-[#244FF0]" @click="handleSearch">
            <Search class="w-20px h-20px mr-10px" />{{ $t('log.search') }}
          </div>
          <div class="rounded-6px cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px ml-20px flex items-center font-500 hover:(bg-[#396FFA] text-white)" @click="resetSearch">
            <Reset class="w-20px h-20px mr-10px" />{{ $t('log.reset') }}
          </div>
        </el-form-item>
      </el-form>
    </el-card>
    <div v-loading="loading" class="border border-white bg-white rounded-8px overflow-hidden shadows">
      <el-table :data="paginatedData">
        <el-table-column :label="$t('log.update')" align="center" width="180">
          <template #default="scope">
            {{ dayjs(scope.row.update_time).format('DD/MM/YYYY HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column prop="user_name" :label="$t('log.name')" align="center" show-overflow-tooltip />
        <el-table-column prop="user_email" :label="$t('log.email')" align="center" />
        <el-table-column :label="$t('log.action')" align="center">
          <template #default="scope">
            <div class="border rounded-6px px-8px py-2px w-fit mx-auto" :class="getParseStatusType(scope.row.log_type)">{{ actionMap[scope.row.log_type] }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="log_content" :label="$t('log.detail')" align="left" show-overflow-tooltip />
      </el-table>
      <div class="pager-wrapper">
        <el-pagination
          background
          :layout="paginationData.layout"
          :page-sizes="paginationData.pageSizes"
          :total="paginationData.total"
          :page-size="paginationData.pageSize"
          :current-page="paginationData.currentPage"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.el-alert {
  margin-bottom: 20px;
}

.search-wrapper {
  margin-bottom: 20px;
  padding-bottom: 20px;
  :deep(.el-card__body) {
    .el-form .el-form-item {
      margin-bottom: 0;
      align-items: center;
      &.footer {
        @media screen and (max-width: 1440px) {
          display: block;
          .el-form-item__content {
            margin-top: 20px;
          }
        }
      }
      .el-form-item__label {
        font-size: 14px;
        font-weight: 600;
        color: #232748;
      }
      .el-input__wrapper {
        &.is-focus {
          box-shadow: 0 0 0 1px #396FFA inset;
        }
        .el-input__inner {
          min-width: 210px;
          min-height: 40px;
        }
      }
    }
  }
}
.shadows {
  box-shadow: 0px 4px 35px 0px #8195C82E;
}

.pager-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>
