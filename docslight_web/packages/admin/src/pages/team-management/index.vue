<script lang="ts" setup>
import type { FormInstance } from 'element-plus'
import { addTeamMemberApi, getTableDataApi, getTeamMembersApi, getUsersApi, removeTeamMemberApi } from '@@/apis/teams'
import { usePagination } from '@@/composables/usePagination'
import { request } from '@/http/axios'
import { useI18n } from 'vue-i18n'
import Search from './Search.vue'
import Reset from './Reset.vue'
import Invite from './Invite.vue'
import Manage from './Manage.vue'
import DeleteBtn from './DeleteBtn.vue'
import Add from '@@/components/Image/Add.vue'
import Xlsx from '@@/components/Image/Xlsx.vue'

defineOptions({
  name: 'TeamManagement'
})

const { t, locale } = useI18n()
const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

// 团队数据结构
interface TeamData {
  id: number
  name: string
  ownerName: string
  memberCount: number
  createTime: string
  updateTime: string
}

// 团队成员数据结构
interface TeamMember {
  userId: number
  username: string
  role: string
  joinTime: string
}

const tableData = ref<TeamData[]>([])

const searchData = reactive({
  name: ''
})

// 存储多选的表格数据
const multipleSelection = ref<TeamData[]>([])

function getTableData() {
  loading.value = true
  getTableDataApi({
    currentPage: paginationData.currentPage,
    size: paginationData.pageSize,
    name: searchData.name
  }).then(({ data }: any) => {
    paginationData.total = data.total
    tableData.value = data.list.map((item: any) => ({
      id: item.id,
      name: item.name,
      ownerName: item.ownerName,
      memberCount: item.memberCount,
      createTime: item.createTime,
      updateTime: item.updateTime
    }))
    // 清空选中数据
    multipleSelection.value = []
  }).catch(() => {
    tableData.value = []
  }).finally(() => {
    loading.value = false
  })
}
async function remoteMethod(query: string) {
  // @ts-ignore
  const { data } = await request({
    url: `api/v1/users?keyword=${query}`,
    method: 'get'
  })
  userList.value = data.list.filter((item: any) => item.username !== currentTeam.value?.ownerName)
}
function handleSearch() {
  paginationData.currentPage === 1 ? getTableData() : (paginationData.currentPage = 1)
}

const searchFormRef = ref<FormInstance | null>(null)
function resetSearch() {
  searchFormRef.value?.resetFields()
  handleSearch()
}

// 表格多选事件处理
function handleSelectionChange(selection: TeamData[]) {
  multipleSelection.value = selection
}
// 团队成员管理相关
const memberDialogVisible = ref<boolean>(false)
const currentTeam = ref<TeamData | null>(null)
const teamMembers = ref<TeamMember[]>([])
const memberLoading = ref<boolean>(false)
// 添加成员相关状态
const dialogVisibleBulk = ref<boolean>(false)
const addMemberDialogVisible = ref<boolean>(false)
const userList = ref<{ id: number, username: string, email: string }[]>([])
const userLoading = ref<boolean>(false)
const selectedUser = ref<number | undefined>(undefined)
const selectedRole = ref<string>('normal')
watch(() => dialogVisibleBulk.value, (val: boolean) => {
  if (!val) {
    file.value = null
    fileName.value = ''
    input.value.value = ''
  }
})
watch(() => memberDialogVisible.value, (val: boolean) => {
  if (!val) {
    getTableData()
  }
})
function handleManageMembers(row: TeamData) {
  currentTeam.value = row
  memberDialogVisible.value = true
  getTeamMembers(row.id)
}

// 获取团队成员列表
function getTeamMembers(teamId: number) {
  memberLoading.value = true
  getTeamMembersApi(teamId)
    .then((response: any) => {
      if (response.data && Array.isArray(response.data.list)) {
        teamMembers.value = response.data.list
      } else if (Array.isArray(response.data)) {
        teamMembers.value = response.data
      } else {
        teamMembers.value = []
      }
    })
    .catch(() => {
      teamMembers.value = []
    })
    .finally(() => {
      memberLoading.value = false
    })
}

// 添加成员
function handleAddMember() {
  // 打开添加成员对话框
  addMemberDialogVisible.value = true
}
watch(() => addMemberDialogVisible.value, (val: boolean) => {
  if (!val) {
    selected.value = ''
    selectedUser.value = undefined
  }
})

// 确认添加成员
function confirmAddMember() {
  // 请选择要添加的用户
  if (!selectedUser.value) return

  // 当前团队信息不存在
  if (!currentTeam.value) return

  // 调用添加成员API
  addTeamMemberApi({
    teamId: currentTeam.value.id,
    userId: selectedUser.value,
    role: selectedRole.value
  }).then(() => {
    ElMessage.success(t('teamManagement.inviteSuccess'))
    // 关闭对话框
    addMemberDialogVisible.value = false
    // 重新获取成员列表
    getTeamMembers(currentTeam.value!.id)
    // 刷新团队列表（更新成员数量）
    getTableData()
    // 重置选择
    selectedUser.value = undefined
    selectedRole.value = 'normal'
  }).catch((error: any) => {
    if (error.response.data.code === 400 && error.response.data.message === '用户已是团队成员') {
      ElMessage.error(t('teamManagement.already'))
    } else {
      ElMessage.error(t('teamManagement.inviteFail'))
    }
  })
}

// 移除成员
function handleRemoveMember(member: TeamMember) {
  ElMessageBox.confirm(t('teamManagement.deleteTip'), '', {
    confirmButtonText: t('teamManagement.yes'),
    cancelButtonText: t('teamManagement.no'),
    type: 'warning'
  }).then(() => {
    if (!currentTeam.value || !currentTeam.value.id) {
      ElMessage.error(t('teamManagement.deleteFail'))
      return
    }
    removeTeamMemberApi({
      teamId: currentTeam.value.id,
      memberId: member.userId
    }).then(() => {
      ElMessage.success(t('teamManagement.deleteSuccess'))
      // 重新获取成员列表
      if (currentTeam.value) {
        getTeamMembers(currentTeam.value.id)
      }
    }).catch(() => {
      ElMessage.error(t('teamManagement.deleteFail'))
    }).finally(() => {
      rotate.value = false
      getTableData()
    })
  })
}

// 监听分页参数的变化
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, { immediate: true })
const downloadTemplate = () => {
  const link = document.createElement('a')
  link.href = '/Users.xlsx'
  link.download = 'Users.xlsx'
  link.click()
}
const selected = ref()
const handleUserChange = () => {
  userList.value.forEach((item: { id: number, username: string, email: string }) => {
    if (item.id === selectedUser.value) {
      selected.value = item
    }
  })
}
const rotate = ref<boolean>(false)
const refresh = () => {
  rotate.value = true
  getTableData()
}
const input = ref()
// 选择其他文件
const file = ref<FileList | null>(null)
const fileName = ref('')
const handleChange = async (e: any) => {
  const files = e.target.files
  if (!files) return
  fileName.value = files[0].name
  file.value = files
}
const handleInviteBulk = async () => {
  if (!file.value) return
  const reqData = new FormData()
  const files = file.value
  reqData.append('file', files[0])
  type UploadResponse = {
    code: number
    data: boolean
    failures: Record<string, string>
    'successes count': number
    message: string
  }
  const data: UploadResponse = await request({
    url: `/api/v1/teams/${currentTeam.value?.id}/members/bulk`,
    method: 'POST',
    data: reqData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  dialogVisibleBulk.value = false
  if (data.code !== 0 && typeof data.message === 'object') {
    ElMessage.error(data.message[locale.value as keyof typeof data.message])
  } else {
    // 已注册错误处理
    const failures = data.failures
    const successCount = data['successes count']
  
    if (Object.keys(failures).length > 0) {
      Object.keys(failures).forEach((email: string) => {
        const reason = failures[email]
        if (reason === 'This User is already exist.') {
          ElMessage({
            message: `${t('userManagement.email')} ${email} ${t('userManagement.already')}`,
            type: 'error',
            duration: 5000
          })
        } else if (reason.includes('Invalid email address: ')) {
          ElMessage({
            message: `${email} ${t('teamManagement.emailError')}`,
            type: 'error',
            duration: 5000
          })
        }
      })
    } else {
      ElMessage.success(t('userManagement.success') + ' - ' + successCount)
    }
    if (successCount > 0) {
      ElMessage.success(t('userManagement.success') + ' - ' + successCount)
    }
  }
  getTeamMembers(currentTeam.value!.id)
}
</script>

<template>
  <div class="app-container">
    <h1 class="text-32px leading-48px font-600 text-brand-0 mb-40px">
      {{ t('teamManagement.title') }}
    </h1>
    <el-card v-loading="loading" shadow="never" class="search-wrapper">
      <el-form ref="searchFormRef" :inline="true" :model="searchData">
        <el-form-item prop="name" ::label="t('teamManagement.owner')">
          <el-input v-model="searchData.name" :placeholder="t('teamManagement.ownerPlaceholder')" />
        </el-form-item>
        <el-form-item class="hidden">
          <input class="hidden" v-model="searchData.name" :placeholder="t('teamManagement.searchPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <div class="rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-20px flex items-center font-500 hover:bg-[#244FF0] ml-[-12px]" @click="handleSearch">
            <Search class="w-20px h-20px mr-10px" />
            {{ t('teamManagement.search') }}
          </div>
          <div class="rounded-6px cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px ml-20px flex items-center font-500 hover:(bg-[#396FFA] text-white)" @click="resetSearch">
            <Reset class="w-20px h-20px mr-10px" />
            {{ t('teamManagement.reset') }}
          </div>
        </el-form-item>
      </el-form>
      <div class="text-[#52555F] text-xs">
        {{ t('teamManagement.total[0]') }}
        {{ paginationData.total }}
        {{ paginationData.total > 1 ? t('teamManagement.total[2]') : t('teamManagement.total[1]') }}
      </div>
    </el-card>
    <el-tooltip effect="dark" :content="t('teamManagement.reload')" placement="top">
      <div @click="refresh" class="w-40px h-40px ml-auto mb-24px mt-32px rounded-6px p-10px bg-[#396FFA] text-white cursor-pointer hover:bg-[#244FF0]">
        <Reset class="w-20px h-20px mr-10px" :class="rotate ? 'rotate360' : ''" />
      </div>
    </el-tooltip>

    <div v-loading="loading" class="border border-white bg-white rounded-8px overflow-hidden shadows">
      <div class="table-wrapper">
        <el-table :data="tableData" @selection-change="handleSelectionChange">
          <el-table-column prop="name" :label="t('teamManagement.teamName')" align="center" show-overflow-tooltip />
          <el-table-column prop="ownerName" :label="t('teamManagement.teamOwner')" align="center" show-overflow-tooltip />
          <el-table-column prop="memberCount" :label="t('teamManagement.member')" align="center" />
          <el-table-column prop="createTime" :label="t('teamManagement.joining')" align="center" />
          <el-table-column prop="updateTime" :label="t('teamManagement.modified')" align="center" />
          <el-table-column fixed="right" :label="t('teamManagement.action')" width="220" align="center">
            <template #default="scope">
              <div class="flex justify-center items-center">
                <div @click="handleManageMembers(scope.row)" class="cursor-pointer border-[#00CF854D] border-1 p-4px flex items-center text-[#00CF85] bg-[#E2F7EF] rounded-6px">
                  <Manage class="mr-2px" />
                  {{ t('teamManagement.manage') }}
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pager-wrapper">
        <el-pagination
          background
          :layout="paginationData.layout"
          :page-sizes="paginationData.pageSizes"
          :total="paginationData.total"
          :page-size="paginationData.pageSize"
          :current-page="paginationData.currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 团队成员管理 -->
    <el-dialog
      v-model="memberDialogVisible"
      :title="`${t('teamManagement.manageTeam')} - ${currentTeam?.name || ''}`"
      width="50%"
    >
      <div v-if="currentTeam">
        <div class="team-info">
          <p class="text-brand-0 text-sm max-w-300px truncate">
            <strong class="font-600">
              {{ t('teamManagement.teamManager') }}:
            </strong>
            {{ currentTeam.name }}
          </p>
          <p class="text-brand-0 text-sm max-w-300px truncate">
            <strong class="font-600">
              {{ t('teamManagement.teamOwner') }}:
            </strong>
            {{ currentTeam.ownerName }}
          </p>
        </div>

        <div class="member-toolbar flex items-center justify-between">
          <div class="flex items-center">
            <div @click="dialogVisibleBulk = true" class="border-1 border-[#CED6E1] text-[#52555F] py-8px px-12px text-sm cursor-pointer rounded-4px mr-12px">
              {{ t('teamManagement.inviteBulk') }}
            </div>
            <div @click="downloadTemplate" class="cursor-pointer underline text-[#396FFA] text-xs">
              {{ t('teamManagement.download') }}
            </div>
          </div>
          <div @click="handleAddMember" class="rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center font-500 hover:bg-[#244FF0]">
            <Invite class="mr-10px" />
            {{ t('teamManagement.singleInvite') }}
          </div>
        </div>

        <el-table :data="teamMembers" style="width: 100%" v-loading="memberLoading">
          <el-table-column prop="username" :label="t('teamManagement.username')" align="center" show-overflow-tooltip />
          <el-table-column prop="role" :label="t('teamManagement.role')" align="center">
            <template #default="scope">
              {{ scope.row.role === 'owner' ? t('teamManagement.ownerRole') : t('teamManagement.normal') }}
            </template>
          </el-table-column>
          <el-table-column prop="joinTime" :label="t('teamManagement.joining')" align="center" />
          <el-table-column fixed="right" :label="t('teamManagement.action')" width="150" align="center">
            <template #default="scope">
              <div v-if="scope.row.role !== 'owner'" class="flex justify-center">
                 <el-tooltip effect="dark" :content="t('teamManagement.delete')" placement="top">
                   <DeleteBtn @click="handleRemoveMember(scope.row)" class="cursor-pointer ml-12px" />
                 </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="teamMembers.length === 0" class="empty-data">
          <el-empty description="empty" />
        </div>
      </div>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisible"
      :title="t('teamManagement.invite')"
      width="520px"
    >
      <div v-loading="userLoading">
        <el-form>
          <el-form-item :label="t('teamManagement.username')">
            <el-select @change="handleUserChange" v-model="selectedUser" :placeholder="t('teamManagement.namePlaceholder')" class="w-full" filterable :remote-method="remoteMethod" remote>
              <el-option
                v-for="user in userList" :key="user.id"
                ::label="user.username" :value="user.id"
              >
              <div class="flex items-center">
                <div class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
                  {{ user.username.slice(0, 1).toUpperCase() }}
                </div>
                <div class="flex flex-col">
                  <div class="text-[#18191B] text-14px leading-20px font-600">{{ user.username }}</div>
                  <div class="text-[#94969D] text-12px leading-16px">{{ user.email }}</div>
                </div>
              </div>
              </el-option>
              <template #label>
                <div class="flex items-center">
                  <div class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
                    {{ selected.username.slice(0, 1).toUpperCase() }}
                  </div>
                  <div class="flex flex-col">
                    <div class="text-[#18191B] text-14px leading-20px font-600">{{ selected.username }}</div>
                    <div class="text-[#94969D] text-12px leading-16px">{{ selected.email }}</div>
                  </div>
                </div>
              </template>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex justify-center pt-8px">
          <div @click="addMemberDialogVisible = false" class="rounded-6px w-140px cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px ml-20px flex justify-center items-center font-500 hover:(bg-[#396FFA] text-white)">
            {{ t('teamManagement.cancel') }}
          </div>
          <div @click="confirmAddMember" :class="selectedUser ? 'cursor-pointer' : 'cursor-not-allowed opacity-60'" class="rounded-6px w-140px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-20px flex justify-center items-center font-500 hover:bg-[#244FF0]">
            {{ t('teamManagement.ok') }}
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导入 -->
    <el-dialog v-model="dialogVisibleBulk" :title="t('teamManagement.inviteBulk')" width="392px" align-center>
      <input ref="input" class="hidden" type="file" name="file" accept=".xlsx" @change="handleChange">
      <div @click="input.click" class="border-1 border-[#CED6E1] py-10px cursor-pointer w-full flex items-center justify-center text-[#52555F] rounded-4px mb-8px mt-8px text-14px leading-20px">
        <Add class="mr-16px" />
        {{ t('teamManagement.select') }}
      </div>
      <div v-show="file" class="text-xs text-brand-0 my-8px font-600 flex items-center">
        <Xlsx class="mr-4px" /> {{ fileName }}
      </div>
      <div class="text-xs text-brand-0 mb-24px flex">
        <span class="text-[#FF5050] text-xs font-600 flex mr-4px">*</span>
        {{ t('teamManagement.selectTip') }}
      </div>
      <div v-loading="loading" :class="file ? 'cursor-pointer' :  'cursor-not-allowed opacity-60'" @click="handleInviteBulk" class="rounded-6px w-140px bg-[#396FFA] text-white text-sm py-8px px-10px mx-auto flex justify-center items-center font-500 hover:bg-[#244FF0]">
        {{ t('teamManagement.ok') }}
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.el-alert {
  margin-bottom: 20px;
}
.rotate360 {
  animation: rotate360 1s ease-out 0s;
}
@keyframes rotate360 {
  100% {
    transform: rotate(360deg);
  }
}

.search-wrapper {
  margin-bottom: 20px;
  padding-bottom: 20px;
  :deep(.el-card__body) {
    display: flex;
    justify-content: space-between;
    .el-form .el-form-item {
      margin-bottom: 0;
      align-items: center;
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

.table-wrapper {
  overflow: hidden;
  border-radius: 8px;
}

.pager-wrapper {
  display: flex;
  justify-content: flex-end;
}

.team-info {
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 24px;
  background-color: #F3F6FF;
}

.member-toolbar {
  margin-bottom: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.empty-data {
  margin-top: 20px;
  text-align: center;
}
:deep() .el-dialog { 
  .el-dialog__body {
    .el-table {
      box-shadow: 0px 4px 35px 0px #8195C82E;
    }
    .el-form {
      .el-form-item {
        flex-direction: column;
        .el-form-item__label {
          font-size: 14px;
          font-weight: 600;
          color: #232748;
          justify-content: flex-start;
        }
        .el-input .el-input__wrapper {
          min-height: 44px;
        }
        .el-select .el-select__wrapper {
          min-height: 44px;
          &.is-focused {
            box-shadow: 0 0 0 1px #396FFA inset;
          }
          .el-select__input {
            caret-color: transparent;
          }
        }
      }
    }
  }
}
</style>

<style lang="scss">
.el-popper.is-pure.is-light.el-select__popper {
  .el-select-dropdown {
    .el-scrollbar {
      .el-select-dropdown__wrap {
        .el-select-dropdown__list {
          .el-select-dropdown__item {
            height: auto;
            padding: 14px 20px;
            &:hover {
              background-color: #F3F6FF;
            }
          }
        }
      }
    }
  }
}
</style>