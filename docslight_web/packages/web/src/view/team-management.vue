<template>
  <div class="bg-[#F3F6FF] p-60px min-h-100vh w-full">
    <h1 class="text-32px leading-48px text-brand-0 font-600 mb-40px">
      {{ t('teamManagement.title') }}
    </h1>
    <div class="flex justify-between items-center pb-16px border-b border-[#E1E3E8]">
      <el-select popper-class="team" v-model="team_id" @change="changeTeam" :placeholder="t('teamManagement.select')" class="max-w-320px">
        <el-option v-for="(item, index) in teamList" :key="index" :value="item.tenant_id">
          <div class="flex justify-between items-center">
            <div class="truncate max-w-210px">
              {{ item.nickname + t('teamManagement.team') }}
            </div>
            <div :class="getTeamRole(item.role)">
              {{ t(`teamManagement.${item.role}`) }}
            </div>
          </div>
        </el-option>
        <template #label>
          <div v-show="item.tenant_id === team_id" v-for="(item, index) in teamList" :key="index" class="flex items-center text-[#232748] justify-between text-sm font-500">
            {{ item.nickname + t('teamManagement.team') }}
            <div :class="getTeamRole(item.role)">
              {{ t(`teamManagement.${item.role}`) }}
            </div>
          </div>
        </template>
      </el-select>
      <div class="text-brand-3 text-xs">{{ t('teamManagement.total[0]') }}{{ total }} {{ total > 1 ? t('teamManagement.total[1]') : t('teamManagement.total[2]') }}</div>
    </div>
    <div v-if="ownerTeam_id === team_id" class="flex justify-end mt-32px mb-24px">
      <div @click="dialogVisible = true" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center justify-center font-500 hover:bg-[#244FF0]">
        <Invite class="mr-10px" />
        {{ t('teamManagement.invite') }}
      </div>
    </div>
    <div class="bg-white shadows">
      <el-table :data="paginatedData">
        <el-table-column :label="t('teamManagement.name')" align="center" show-overflow-tooltip>
          <template #default="scope">
            <div class="flex items-center pl-32px w-150px">
              <img v-if="scope.row.avatar" :src="scope.row.avatar" class="min-w-32px max-w-32px max-h-32px rounded-1/2">
              <div v-else class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
                {{ scope.row.nickname.slice(0, 1).toUpperCase() }}
              </div>
              <div class="truncate w-200px text-left">{{ scope.row.nickname }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="t('teamManagement.email')" align="center" />
        <el-table-column :label="t('teamManagement.role')" align="center">
          <template #default="scope">
            <div class="flex justify-center">
              {{ t(`teamManagement.${scope.row.role}`) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="update_date" :label="t('teamManagement.join')" align="center">
          <template #default="scope">
            <div class="flex justify-center">{{ dayjs.utc(scope.row.update_date).local().format('DD/MM/YYYY HH:mm:ss') }}</div>
          </template>
        </el-table-column>
        <el-table-column v-if="ownerTeam === team_id" fixed="right" :label="t('teamManagement.action')" align="center">
          <template #default="scope">
            <el-tooltip popper-class="box-item" effect="dark" :content="t('teamManagement.delete')" placement="top">
              <Delete v-if="scope.row.role !== 'owner'" @click="deleteUser(scope.row)" class="mx-auto cursor-pointer svg" />
            </el-tooltip>
          </template>
        </el-table-column>
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
    <el-dialog v-model="dialogVisible" align-center width="520px">
      <h3 class="text-sm font-bold text-[#43474D] py-4px mb-24px">
        {{ t('teamManagement.invite') }}
      </h3>
      <div class="flex text-xs text-brand-0 font-600 mb-8px">
        <span class="text-[#FF5050] inline-block mr-4px font">*</span>
        {{ t('teamManagement.nameOrEmail') }}
      </div>
      <el-select 
        v-model="selectedUsers"
        :placeholder="t('teamManagement.nameOrEmailPlaceholder')"
        multiple
        filterable
        remote
        :reserve-keyword="false"
        @change="handleUserChange"
        collapse-tags
        :remote-method="remoteMethod"
        :value-key="'id'">
        <el-option
          v-for="user in userList"
          :key="user.id"
          ::label="user.nickname"
          :value="user"
        >
        <div class="flex items-center">
          <img v-if="user.avatar" :src="user.avatar" alt="avatar" class="w-32px h-32px mr-12px">
          <div v-else class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
            {{ user.nickname.slice(0, 1).toUpperCase() }}
          </div>
          <div class="flex flex-col">
            <div class="text-[#18191B] text-14px leading-20px font-600">
              {{ user.nickname }}
            </div>
            <div class="text-[#94969D] text-12px leading-16px">
              {{ user.email }}
            </div>
          </div>
        </div>
        </el-option>
        <template v-if="selectedUsers.length" #tag>
          <el-tag class="custom" closable @close="removeUser(index)" v-for="(item, index) in selectedUsers" :key="index">
            <div class="flex items-center">
              <div class="bg-[#FFE248] rounded-full w-20px h-20px text-xs font-600 text-brand-0 flex justify-center items-center mr-4px">
                {{ item.nickname.slice(0, 1).toUpperCase() }}
              </div>
              {{ item.nickname }}
            </div>
          </el-tag>
        </template>
      </el-select>
      <div class="flex justify-center mt-24px">
        <div class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)" @click="dialogVisible = false">
          {{ t('teamManagement.cancel') }}
        </div>
        <div :class="selectedUsers.length ? 'cursor-pointer' : 'cursor-not-allowed opacity-60'" class="w-140px rounded-6px font-500 bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]" @click="addUser">
          {{ t('teamManagement.ok') }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { onMounted, ref, computed } from 'vue'
import { get, post, _delete } from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import Delete from '../components/images/Delete.vue'
import Invite from '../components/images/Invite.vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'

const total = ref()
const ownerTeam = ref()
const { t } = useI18n()
const team_id = ref('')
const ownerTeam_id = ref('')
const pageSize = ref(10)
const tableData = ref([])
const currentPage = ref(1)
const dialogVisible = ref(false)
const pageSizes = ref([5, 10, 20])
const selected = ref<userInfo[]>([])
const teamList = ref<teamInfo[]>([])
const userList = ref<userInfo[]>([])
const selectedUsers = ref<userInfo[]>([])

interface userInfo {
  email: string
  nickname: string
  role: string
  id: string
  avatar: string
  tenant_id: string
}
interface teamInfo {
  avatar: string
  email: string
  nickname: string
  role: string
  tenant_id: string
}
const changeTeam = async () => {
  const { data: res } = await get(`v1/tenant/${team_id.value}/user`)
  tableData.value = res.data
  total.value = res.data.length
}
const removeUser = async (index: number) => {
  selectedUsers.value.splice(index, 1)
}
// 当前页数据（通过计算属性截取）
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableData.value.slice(start, end)
})
// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getTableData()
}
// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getTableData()
}
const addUser = async () => {
  if (!selectedUsers.value.length) return
  let emails:{}[] = []
  selectedUsers.value.forEach((email) => {
    emails.push({
      email: email.email
    })
  })
  const { data } = await post(`/v1/tenant/${team_id.value}/user`, {
    emails: emails
  })
  if (data.code === 0 && data.message === 'success') {
    data.data.forEach((item: any) => {
      if (item.success) {
        ElMessage.success(`${item.email} ${t('teamManagement.inviteSuccess')}`)
      } else if (item.message.includes('is already in the team.')) {
        ElMessage.error(`${item.email} ${t('teamManagement.already')}`)
      }
    })
    getTableData()
    selectedUsers.value = []
    dialogVisible.value = false
  } else {
    ElMessage.error(t('teamManagement.inviteFail'))
  }
}
const deleteUser = (user: any) => {
  ElMessageBox.confirm(t('teamManagement.deleteTip'), '', {
    confirmButtonText: t('teamManagement.yes'),
    cancelButtonText: t('teamManagement.no'),
    type: 'warning'
  }).then(async () => {
    try {
      await _delete(`/v1/tenant/${team_id.value}/user/${user.user_id}`)
      ElMessage.success(t('teamManagement.deleteSuccess'))
      getTableData()
    } catch {
      ElMessage.error(t('teamManagement.deleteFail'))
    }
  })
}
const getTeamRole = (role: string) => {
  const common = 'rounded-6px py-2px px-8px text-xs font-600 border text-white'
  if (role === 'normal') return 'border-[#00CF854D] bg-[#396FFA] ' + common
  if (role === 'owner') return 'border-[#00CF854D] bg-[#00CF85] ' + common
}
const getTableData = async () => {
  const { data: res } = await get(`v1/tenant/${team_id.value}/user`)
  tableData.value = res.data
  total.value = tableData.value.length
}
const handleUserChange = () => {
  selected.value = []
  selected.value = userList.value.filter(user =>
    selectedUsers.value.some((id: userInfo) => id.id === user.id)
  )
}
const remoteMethod = (query: string) => {
  if (query) {
    setTimeout(async () => {
      const { data } = await get('/v1/user/search_user/' + query)
      userList.value = data.data
    })
  } else {
    userList.value = []
  }
}
onMounted(async () => {
  const { data } = await get('/v1/tenant/list')
  teamList.value = data.data
  if (!teamList.value.length) return
  teamList.value.forEach((item: teamInfo) => {
    if (item.role === 'owner') {
      team_id.value = item.tenant_id
      ownerTeam.value = item.tenant_id
      ownerTeam_id.value = item.tenant_id
    }
  })
  getTableData()
})
</script>

<style lang="scss" scoped>
.font {
  font-family: 'Helvetica';
}
.shadows {
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0px 4px 35px 0px #8195C82E;
}
</style>
<style lang="scss">
.el-popper.is-pure.is-light.el-tooltip.el-select__popper.team {
  .el-select-dropdown.team {
    overflow: auto;
    max-width: 320px;
    max-height: 212px;
  }
}
</style>
