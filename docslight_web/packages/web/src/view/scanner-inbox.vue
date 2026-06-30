<template>
  <div class="document-extraction flex flex-col">
    <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8]">{{ t('dms.title') }} | {{ t('dms.scanner_inbox.title') }}</h1>
    <div class="flex">
      <!-- 左侧文件夹列表 -->
      <div class="px-20px py-32px w-258px border-r border-[#E1E3E8] flex flex-col justify-between h-[calc(100vh-81px)]">
        <div class="flex flex-col">
          <el-input class="max-w-218px" v-model="searchQueryFolder" clearable @clear="getFolderList" @input="getFolderList" :placeholder="t('dms.scanner_inbox.search.by_folder_name')">
            <template #prefix>
              <Search />
            </template>
          </el-input>
          <div class="h-1px w-full bg-[#B7BABF] my-20px"></div>
          <div class="bg-[#1460F31A] text-brand-2 py-8px px-12px rounded-8px flex items-center justify-between text-sm font-500 cursor-pointer">
            <div class="flex items-center">
              <Scanner class="mr-12px" />
              {{ t('dms.scanner_inbox.title') }}
            </div>
            <div class="w-16px h-16px rounded-1/2 bg-[#D44040] text-8px leading-16px text-white flex items-center justify-center">99+</div>
          </div>
          <a href="/team-space" class="py-8px px-12px rounded-8px flex items-center justify-between text-sm font-500 cursor-pointer mt-8px">
            <div class="flex items-center">
              <TeamFile class="mr-12px" />
              {{ t('dms.team_space.title') }}
            </div>
            <DocArrow class="transform" />
          </a>
        </div>
      </div>
      <!-- 右侧文件列表区域 -->
      <div class="bg-white shadows">
        <el-table :data="scannerFileList" @selection-change="handleSelectionChange" :row-key="rowKey">
          <el-table-column type="selection" width="50" />
          <el-table-column :label="t('dms.scanner_inbox.table.columns.file_id')" prop="" align="left" width="80px">
            <template #default="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column :label="t('dms.scanner_inbox.table.columns.name')" min-width="186px" show-overflow-tooltip>
            <template #default="scope">
              <div class="flex items-center">
                <Document class="min-w-20px mr-4px" />
                <div class="truncate">{{ scope.row.name }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="format" :label="t('dms.team_space.table.columns.types')" min-width="140px">
            <template #default="scope">
              <div class="flex items-center justify-start">
                <div class="truncate">{{ scope.row.file_type }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="size" :label="t('dms.team_space.table.columns.size')" min-width="140px" />
          <el-table-column  width="140px">
            <template #header>
              <el-popover v-model:visible="createFilter" placement="bottom" popper-class="dateTip" trigger="" append-to-body>
                <template #reference>
                  <div @click.stop="createFilter = true" class="flex items-center justify-start cursor-pointer">
                    {{ t('dms.team_space.table.columns.creator') }}
                    <FilterFile class="ml-4px filter" :class="createFilterValue.length && 'active'" />
                  </div>
                </template>
                <div class="rounded-8px bg-white flex flex-col whitespace-nowrap min-w-180px">
                  <el-checkbox-group v-model="createFilterValue">
                    <el-checkbox v-for="user in createUserList" :label="user.nickname" :value="user.id" :key="user.id" />
                  </el-checkbox-group>
                  <div class="h-1px w-full bg-[#0000000F]"></div>
                  <div class="flex justify-center py-16px">
                    <div @click="createFilterValue = [], getFolderFileList(), createFilter = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                      {{ t('template.reset') }}
                    </div>
                    <div v-loading="loading" @click="getFolderFileList(), createFilter = false" :class="createFilterValue?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
                      class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px">
                      {{ t('extraction.ok') }}
                    </div>
                  </div>
                </div>
              </el-popover>
            </template>
            <template #default="scope">
              <div class="flex items-center justify-start">
                <div class="truncate">{{ scope.row.creator.nickname }}</div>
              </div>
            </template>
          </el-table-column>
          <!-- Permissions -->
          <el-table-column prop="pageCount" :label="t('dms.team_space.table.columns.permissions')" min-width="140px">
            <template #header>
              <el-popover v-model:visible="permissionFilter" placement="bottom" popper-class="dateTip" trigger="" append-to-body>
                <template #reference>
                  <div @click.stop="permissionFilter = true" class="flex items-center justify-center cursor-pointer">
                    {{ t('dms.team_space.table.columns.permissions') }}
                    <FilterFile class="ml-4px filter" :class="permissionFilterValue.length && 'active'" />
                  </div>
                </template>
                <div class="rounded-8px bg-white flex flex-col whitespace-nowrap min-w-180px">
                  <el-checkbox-group v-model="permissionFilterValue">
                    <el-checkbox :label="t('extraction.completed')" value="2" />
                    <el-checkbox :label="t('extraction.processing')" value="1" />
                    <el-checkbox :label="t('extraction.pending')" value="0" />
                    <el-checkbox :label="t('extraction.fail')" value="3" />
                  </el-checkbox-group>
                  <div class="h-1px w-full bg-[#0000000F]"></div>
                  <div class="flex justify-center py-16px">
                    <div @click="permissionFilterValue = [], getFolderFileList(), permissionFilter = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                      {{ t('template.reset') }}
                    </div>
                    <div v-loading="loading" @click="getFolderFileList(), permissionFilter = false" :class="permissionFilterValue?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
                      class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px">
                      {{ t('extraction.ok') }}
                    </div>
                  </div>
                </div>
              </el-popover>
            </template>
            <template #default="scope">
              <div class="flex items-center text-12px leading-16px">
                <div :class="getPermissionStatusClass(scope.row.role)" class="border-1 rounded-6px border-solid py-2px px-8px text-12px leading-16px min-w-115px py-6px mr-4px text-center">
                  {{ getPermissionsStatusTxt(scope.row.role) }}
                </div>
              </div>
            </template>
          </el-table-column>
          <!-- Scan Time -->
          <el-table-column prop="create_time" :label="t('dms.scanner_inbox.table.columns.scan_time')" width="140px">
            <template #header>
              <el-popover v-model:visible="timeFilter" placement="bottom-end" popper-class="dateTip" trigger="" append-to-body>
                <template #reference>
                  <div @click.stop="timeFilter = true" class="flex items-center justify-center cursor-pointer">
                    {{ t('dms.scanner_inbox.table.columns.scan_time') }}
                    <FilterFile class="ml-4px filter" :class="(singleDate || doubleDate.length) && 'active'" />
                  </div>
                </template>
                <div class="date">
                  <div class="date-title">{{ $t('extraction.filter') }}</div>
                  <div class="tag-content">
                    <div @click="change('less')" :class="dateType === 'less' && 'active'" class="date-tag">{{ $t('extraction.earlierThan') }}</div>
                    <div @click="change('more')" :class="dateType === 'more' && 'active'" class="date-tag">{{ $t('extraction.laterThan') }}</div>
                    <div @click="change('equal')" :class="dateType === 'equal' && 'active'" class="date-tag">{{ $t('extraction.equalTo') }}</div>
                    <div @click="change('between')" :class="dateType === 'between' && 'active'" class="date-tag">{{ $t('extraction.between') }}</div>
                  </div>
                  <div class="select">
                    {{ $t('extraction.date') }}
                    <div class="input" @click="handleClick">
                      {{ (singleDate || doubleDate.length) ? (singleDate || `${doubleDate[0]} ~ ${doubleDate[1]}`.replace(/-/g, '/')) : $t('extraction.selectDate') }}
                      <Arrow class="transform -rotate-90" />
                    </div>
                  </div>
                  <Calender @checkedDate="checkedDate" :userFirstLogin="userFirstLogin" v-show="double" />
                  <SingleCalendar @singleCheckedDate="singleCheckedDate" :userFirstLogin="userFirstLogin" v-show="single" />
                  <div class="bottom">
                    <div @click="checkDate" class="ok">{{ $t('extraction.ok') }}</div>
                    <div @click="singleDate = '', doubleDate = []" class="clear">{{ $t('extraction.clear') }}</div>
                  </div>
                </div>
              </el-popover>
            </template>
            <template #default="scope">
              <div class="flex justify-start whitespace-nowrap">{{ dayjs.utc(scope.row.create_time).format('DD/MM/YYYY HH:mm:ss') }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="t('extraction.action')" width="140px" align="center">
            <template #default="scope">
              <div class="flex items-center justify-center">
                <div @click="selectFile = scope.row, downloadFile()" class="text-brand-2 text-12px leading-16px mr-12px cursor-pointer">
                  <DownloadFile class="cursor-pointer mr-12px downloadFile" />
                </div>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <div class="w-full h-[calc(100vh-452px)] flex flex-col justify-center items-center">
              <img src="/images/kbEmpty.png" width="120" height="120" alt="Empty">
              <div class="text-sm text-brand-3 mt-8px mb-32px max-w-600px text-center">
                {{ t('extraction.noDocument') }}
              </div>
            </div>
          </template>
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
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { ref, onMounted } from 'vue'
import { get, post } from '../utils/request'

const { t } = useI18n()

const pageSize = ref(10)
const currentPage = ref(1)
const total = ref(0)
const device_id = ref('')
const searchQueryFolder = ref('')
const pageSizes = ref([5, 10, 20])
const selectFile = ref<ScannerFileWithDeviceName | null>(null)
const selectFiles = ref<ScannerFileWithDeviceName[]>([])

type ScannerDevice = {
  id: string
  is_dir?: boolean
  name: string
  type?: string
}

type ScannerFile = {
  created_at: string
  id: string
  is_dir: boolean
  name: string
  size: string
  type: string
}
type ScannerFileWithDeviceName = ScannerFile & {
  deviceName: string
  deviceId: string
}

const deviceList = ref<ScannerDevice[]>([])
const scannerFileList = ref<ScannerFileWithDeviceName[]>([])

const login = async () => {
  const { data } = await post('/v1/scanner/login', 
    {
      email: 'pengjianyong@kdanmobile.com',
      password: '123456'
    }
  )
  console.log(data)
}

const getDeviceList = async () => {
  const { data } = await get('/v1/scanner/devices')
  const devices = (data?.data ?? []) as ScannerDevice[]
  deviceList.value = Array.isArray(devices) ? devices : []
  return deviceList.value
}

const getFileList = async () => {
  const devices = deviceList.value.length ? deviceList.value : await getDeviceList()
  const validDevices = devices.filter((device) => Boolean(device?.id))
  if (!validDevices.length) {
    scannerFileList.value = []
    return []
  }

  const responses = await Promise.allSettled(
    validDevices.map((device) =>
      get(`/v1/scanner/device/files?device_id=${encodeURIComponent(device.id)}`)
    )
  )

  const allFiles: ScannerFileWithDeviceName[] = []
  responses.forEach((result, index) => {
    if (result.status !== 'fulfilled') return
    const device = validDevices[index]
    const payload = result.value?.data?.data
    const files = Array.isArray(payload?.files)
      ? payload.files
      : (Array.isArray(payload) ? payload : [])

    files.forEach((file: ScannerFile) => {
      allFiles.push({
        ...file,
        deviceName: device.name,
        deviceId: device.id
      })
    })
  })

  scannerFileList.value = allFiles
  console.log(scannerFileList.value)
  return allFiles
}

const getFolderList = async () => {
  const { data } = await get(`/v1/team_space/root_folders?keywords=${searchQueryFolder.value}`)
  console.log(data)
}

onMounted(async () => {
  await login()
  await getFileList()
})

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getFileList()
}

// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getFileList()
}

// 表格多选事件处理
const handleSelectionChange = (selection: ScannerFileWithDeviceName[]) => {
  selectFile.value = null
  selectFiles.value = []
  selection.forEach((item: ScannerFileWithDeviceName) => {
    selectFiles.value.push(item)
  })
}
</script>

<style lang="scss" scoped>
:deep() {
   .el-input.max-w-218px .el-input__wrapper .el-input__inner {
    max-width: 170px;
    min-width: 170px;
  }
}
.downloadFile {
  rect:first-child {
    display: none;
  }
  rect:nth-child(2) {
    display: none;
  }
  &:hover {
    rect:first-child {
      display: unset;
    }
    rect:nth-child(2) {
      display: unset;
    }
  }
}
</style>
