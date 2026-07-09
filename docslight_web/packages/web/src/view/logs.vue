<template>
  <div class="relative">
    <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8] bg-white w-[calc(100%-68px)] fixed top-0 z-2">{{ t('common.setting') }}</h1>
    <div class="flex w-full mt-80px min-h-[calc(100vh-80px)]">
      <SettingSidebar class="sidebar-fixed" />
      <div class="bg-[#F3F6FF] w-[calc(100%-283px)] ml-283px p-32px min-h-[calc(100vh-80px)]">
        <h2 class="text-20px leading-32px font-600 text-[#0C131F] mb-32px">
          {{ t('logs.title') }}
        </h2>
        <el-input class="max-w-300px mb-32px" v-model="searchQuery" clearable @input="searchLogList" :placeholder="t('logs.search_placeholder')">
          <template #prefix>
            <Search />
          </template>
        </el-input>
        <div class="border border-white bg-white rounded-8px overflow-hidden shadows">
          <el-table ref="tableRef" :data="logList" :row-key="rowKey">
            <el-table-column min-width="170" align="left">
              <template #header>
                <el-popover v-model:visible="timeFilter" placement="bottom-start" popper-class="dateTip" trigger="" append-to-body>
                  <template #reference>
                    <div @click.stop="timeFilter = true" class="flex items-center justify-start cursor-pointer">
                      {{ t('logs.time') }}
                      <FilterFile class="ml-4px filter" :class="(singleDate || doubleDate.length) && 'active'" />
                    </div>
                  </template>
                  <div class="date">
                    <div class="date-title">{{ t('extraction.filter') }}</div>
                    <div class="tag-content">
                      <div @click="change('less')" :class="dateType === 'less' && 'active'" class="date-tag">{{ t('extraction.earlierThan') }}</div>
                      <div @click="change('more')" :class="dateType === 'more' && 'active'" class="date-tag">{{ t('extraction.laterThan') }}</div>
                      <div @click="change('equal')" :class="dateType === 'equal' && 'active'" class="date-tag">{{ t('extraction.equalTo') }}</div>
                      <div @click="change('between')" :class="dateType === 'between' && 'active'" class="date-tag">{{ t('extraction.between') }}</div>
                    </div>
                    <div class="select">
                      {{ t('extraction.date') }}
                      <div class="input" @click="handleClick">
                        {{ (singleDate || doubleDate.length) ? (singleDate || `${doubleDate[0]} ~ ${doubleDate[1]}`.replace(/-/g, '/')) : t('extraction.selectDate') }}
                        <Arrow class="transform -rotate-90" />
                      </div>
                    </div>
                    <Calender @checkedDate="checkedDate" :userFirstLogin="userFirstLogin" v-show="double" />
                    <SingleCalendar @singleCheckedDate="singleCheckedDate" :userFirstLogin="userFirstLogin" v-show="single" />
                    <div class="bottom">
                      <div @click="checkDate" class="ok">{{ t('extraction.ok') }}</div>
                      <div @click="singleDate = '', doubleDate = []" class="clear">{{ t('extraction.clear') }}</div>
                    </div>
                  </div>
                </el-popover>
              </template>
              <template #default="scope">
                <div class="flex justify-start whitespace-nowrap">{{ dayjs.utc(scope.row.update_time).local().format('YYYY/MM/DD HH:mm:ss') }}</div>
              </template>
            </el-table-column>
            <!-- Operator -->
            <el-table-column :label="t('logs.operator')" prop="user_name" min-width="170" align="left">
              <template #header>
                <el-popover v-model:visible="operator" placement="bottom" popper-class="dateTip" trigger="" append-to-body>
                  <template #reference>
                    <div @click.stop="operator = true" class="flex items-center justify-start cursor-pointer">
                      {{ t('logs.operator') }}
                      <FilterFile class="ml-4px filter" :class="operatorList.length && 'active'" />
                    </div>
                  </template>
                  <div class="rounded-8px bg-white flex flex-col whitespace-nowrap min-w-180px">
                    <el-checkbox-group v-model="operatorList">
                      <el-checkbox v-for="user in userList" :label="user.user_name" :value="user.user_id" :key="user.user_id" />
                    </el-checkbox-group>
                    <div class="h-1px w-full bg-[#0000000F]"></div>
                    <div class="flex justify-center py-16px">
                      <div @click="operatorList = [], getLogList(), operator = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                        {{ t('template.reset') }}
                      </div>
                      <div @click="getLogList(), operator = false"
                        class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px hover:bg-[#244FF0] cursor-pointer">
                        {{ t('extraction.ok') }}
                      </div>
                    </div>
                  </div>
                </el-popover>
              </template>
            </el-table-column>
            <!-- Type -->
            <el-table-column :label="t('logs.type')" prop="action_type" min-width="170" align="left">
              <template #header>
                <el-popover v-model:visible="actionType" placement="bottom" popper-class="dateTip" trigger="" append-to-body>
                  <template #reference>
                    <div @click.stop="actionType = true" class="flex items-center justify-start cursor-pointer">
                      {{ t('logs.type') }}
                      <FilterFile class="ml-4px filter" :class="actionTypeList.length && 'active'" />
                    </div>
                  </template>
                  <div class="rounded-8px bg-white flex flex-col whitespace-nowrap min-w-180px">
                    <el-checkbox-group v-model="actionTypeList">
                      <el-checkbox :label="t('logs.types.download_files')" value="下载文件" />
                      <el-checkbox :label="t('logs.types.upload_files')" value="上传文件" />
                      <el-checkbox :label="t('logs.types.delete_files')" value="删除文件" />
                      <el-checkbox :label="t('logs.types.move_files')" value="移动文件" />
                      <el-checkbox :label="t('logs.types.rename_files')" value="重命名文件" />
                      <el-checkbox :label="t('logs.types.copy_files')" value="复制文件" />
                      <!-- <el-checkbox :label="t('logs.types.assign_files_from_scanner_inbox')" value="分配文件" /> -->
                      <el-checkbox :label="t('logs.types.create_folders')" value="创建文件夹" />
                      <el-checkbox :label="t('logs.types.delete_folders')" value="删除文件夹" />
                      <el-checkbox :label="t('logs.types.rename_folders')" value="重命名文件夹" />
                      <el-checkbox :label="t('logs.types.add_permissions')" value="添加权限" />
                      <el-checkbox :label="t('logs.types.delete_permissions')" value="删除权限" />
                      <el-checkbox :label="t('logs.types.modify_permissions')" value="修改权限" />
                      <el-checkbox :label="t('logs.types.add_members')" value="添加成员" />
                      <el-checkbox :label="t('logs.types.delete_members')" value="移除成员" />
                      <el-checkbox :label="t('logs.types.start_parsing')" value="启动解析任务" />
                      <el-checkbox :label="t('logs.types.start_extraction')" value="启动抽取任务" />
                      <!-- <el-checkbox :label="t('logs.types.export_data')" value="导出数据" /> -->
                      <el-checkbox :label="t('logs.types.confirm_extraction_results')" value="确认抽取结果" />
                      <el-checkbox :label="t('logs.types.login')" value="用户登录" />
                      <el-checkbox :label="t('logs.types.logout')" value="用户登出" />
                      <el-checkbox :label="t('logs.types.third_party_authorization')" value="第三方平台授权" />
                      <el-checkbox :label="t('logs.types.remove_third_party_authorization')" value="解除第三方授权" />
                    </el-checkbox-group>
                    <div class="h-1px w-full bg-[#0000000F]"></div>
                    <div class="flex justify-center py-16px">
                      <div @click="actionTypeList = [], getLogList(), actionType = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                        {{ t('template.reset') }}
                      </div>
                      <div @click="getLogList(), actionType = false"
                        class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px hover:bg-[#244FF0] cursor-pointer">
                        {{ t('extraction.ok') }}
                      </div>
                    </div>
                  </div>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column :label="t('logs.related_content')" prop="related_content" min-width="170" align="left">
              <template #default="scope">
                <div class="flex items-center justify-start whitespace-nowrap">
                  <DocFolder v-if="scope.row.action_type.includes('文件夹')" class="mr-8px min-w-20px" />
                  <Docs v-else-if="scope.row.action_type.includes('文件')" class="mr-8px min-w-20px" />
                  <span class="truncate max-w-200px">
                    {{ scope.row.related_content }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('logs.details')" prop="action_detail" min-width="320" align="left" />
          </el-table>
          <el-pagination
            background
            :total="total"
            :page-size="pageSize"
            :page-sizes="pageSizes"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            layout="total, prev, pager, next, sizes, jumper"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { get } from '../utils/request'
import { onMounted, ref, defineAsyncComponent } from 'vue'
const Calender = defineAsyncComponent(() => import('../components/calendar/calendar.vue'))
const SingleCalendar = defineAsyncComponent(() => import('../components/calendar/singleCalendar.vue'))

const { t } = useI18n()
const total = ref(0)
const logList = ref([])
const pageSize = ref(10)
const currentPage = ref(1)
const actionType = ref(false)
const searchQuery = ref('')
const timeFilter = ref(false)
const pageSizes = ref([5, 10, 20])

const endTime = ref('')
const startTime = ref('')
const single = ref(false)
const double = ref(false)
const operator = ref(false)
const operatorList = ref([])
const actionTypeList = ref([])
const userList = ref<{ user_id: string; user_name: string }[]>([])
const singleDate = ref('')
const dateType = ref('less')
const doubleDate = ref<string[]>([])
const userFirstLogin = ref('2000-01-01T00:00:00')

const change = (type: string) => {
  dateType.value = type
  single.value = false
  double.value = false
  doubleDate.value = []
  if (type === 'between') {
    singleDate.value = ''
  } else {
    doubleDate.value = []
  }
}

const handleClick = () => {
  if (dateType.value === 'between') {
    double.value = true
  } else {
    single.value = true
  }
}

// 日期范围选择回调（介于两者之间）
const checkedDate = (dateArr: string[]) => {
  // 开始日期 00:00:00，结束日期 23:59:59
  doubleDate.value = [
    `${dateArr[0]}T00:00:00`,
    `${dateArr[1]}T23:59:59`
  ]
  double.value = false
}

// 单日期选择回调
const singleCheckedDate = (date: string) => {
  if (dateType.value === 'less') {
    // 早于：选择日期的 23:59:59
    singleDate.value = `${date}T23:59:59`
  } else if (dateType.value === 'more') {
    // 晚于：选择日期的 00:00:00
    singleDate.value = `${date}T00:00:00`
  } else if (dateType.value === 'equal') {
    // 等于：开始时间 00:00:00，结束时间 23:59:59
    singleDate.value = `${date}T00:00:00 ~ ${date}T23:59:59`
  } else {
    singleDate.value = date
  }
  single.value = false
}

// 获取时间
const checkDate = () => {
  timeFilter.value = false
  // TODO: 根据 dateType 和日期值进行筛选
  getLogList()
}

const rowKey = (row: any) => row.id

const getLogList = async () => {
  if (dateType.value === 'less') {
    endTime.value = singleDate.value
    startTime.value = ''
  } else if (dateType.value === 'more') {
    startTime.value = singleDate.value
    endTime.value = ''
  } else if (dateType.value === 'equal') {
    const [start, end] = singleDate.value.split(' ~ ')
    startTime.value = start || ''
    endTime.value = end || ''
  } else {
    startTime.value = doubleDate.value[0] || ''
    endTime.value = doubleDate.value[1] || ''
  }
  const params = new URLSearchParams({
    page_size: String(pageSize.value),
    page: String(currentPage.value),
    action_type: actionTypeList.value.join(','),
    user_id: operatorList.value.join(','),
    create_date_start: startTime.value,
    create_date_end: endTime.value
  })
  const { data } = await get(`/v1/log/list?${params.toString()}`)
  logList.value = data.data.logs
  total.value = data.data.total
}

const searchLogList = async () => {
  if (!searchQuery.value) {
    getLogList()
    return
  }
  const params = new URLSearchParams({
    keyword: searchQuery.value,
    page_size: String(pageSize.value),
    page: String(currentPage.value),
  })
  const { data } = await get(`/v1/log/search?${params.toString()}`)
  logList.value = data.data.logs
  total.value = data.data.total
}

const getUserList = async () => {
  const { data: { data } } = await get('/v1/log/userlist')
  userList.value = data.userlist
}

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  searchQuery.value ? searchLogList() : getLogList()
}

// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  searchQuery.value ? searchLogList() : getLogList()
}

onMounted(() => {
  getLogList()
  getUserList()
})
</script>

<style lang="scss" scoped>
.sidebar-fixed {
  position: fixed;
  left: 68px;
  top: 80px;
  width: 283px;
  height: calc(100vh - 80px);
  background: #fff;
}
:deep(.el-checkbox-group) {
  display: flex;
  margin: 4px 0;
  flex-direction: column;
  max-height: 170px;
  overflow: auto;
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
    display: unset;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background: #C2C2C2;
  }

  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }

  &::-webkit-scrollbar-thumb {
    display: unset;
  }

  &::-webkit-scrollbar-track {
    display: unset;
  }
  .el-checkbox {
    height: auto;
    margin-right: 0;
    padding: 8px 16px;
    color: #404653;
    &:hover {
      background-color: #F6F6FB;
    }
    &.is-checked .el-checkbox__label {
      color: #404653;
    }
  }
}
:deep() {
  svg.filter {
    rect {
      display: none;
    }
    path {
      fill: #94969D;
    }
    &:hover {
      path {
        fill: #396FFA;
      }
    }
    &.active {
      rect {
        display: unset;
      }
      path {
        fill: #396FFA;
      }
    }
  }
}
.date {
  padding: 12px;
  text-align: left;
  position: relative;
  border-radius: 8px;
  .date-title {
    font-size: 14px;
    font-weight: 600;
    line-height: 20px;
    color: #0C131F;
    margin-bottom: 12px;
  }
  .tag-content {
    display: flex;
    .date-tag {
      font-size: 14px;
      cursor: pointer;
      font-weight: 500;
      padding: 4px 12px;
      line-height: 20px;
      color: #404653;
      white-space: nowrap;
      border-radius: 100px;
      background-color: #EBEDF0;
      & + .date-tag {
        margin-left: 12px;
      }
      &.active {
        color: white;
        background-color: #396FFA;
      }
    }
  }
  .select {
    display: flex;
    font-size: 14px;
    margin-top: 12px;
    line-height: 20px;
    color: #404653;
    font-weight: normal;
    align-items: center;
    white-space: nowrap;
    .input {
      width: 100%;
      cursor: pointer;
      font-size: 14px;
      margin-left: 8px;
      line-height: 20px;
      padding: 6px 12px;
      color: #888C94;
      position: relative;
      border-radius: 4px;
      padding-right: 28px;
      border: 1px solid #E2E3E5;
      svg {
        top: 8px;
        right: 12px;
        cursor: pointer;
        position: absolute;
      }
    }
  }
  .bottom {
    display: flex;
    font-size: 14px;
    margin-top: 20px;
    line-height: 20px;
    justify-content: flex-end;
    .ok {
      cursor: pointer;
      padding: 2px 8px;
      color: white;
      border-radius: 6px;
      background-color: #396FFA;
    }
    .clear {
      cursor: pointer;
      padding: 2px 8px;
      margin-left: 8px;
      color: #1F2633;
      border-radius: 6px;
      border: 1px solid #1F2633;
    }
  }
}
</style>
