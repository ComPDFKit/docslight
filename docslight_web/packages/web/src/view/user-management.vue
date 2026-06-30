<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import { post, get, _delete } from '../utils/request'
import { cloneDeep } from 'lodash-es'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import type { AuthUser } from '../types/auth'
import { rsaPsw } from '../utils/rsaPsw'
import Add from '../components/user/UserAdd.vue'
import Delete from '../components/user/UserDelete.vue'
import DeleteBtn from '../components/user/UserDeleteBtn.vue'
import Edit from '../components/user/UserEdit.vue'
import Invite from '../components/user/UserInvite.vue'
import Lock from '../components/user/UserLock.vue'
import Reset from '../components/user/UserReset.vue'
import Search from '../components/user/UserSearch.vue'
import { ref, watch, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from '../stores'

const store = useStore()
const { t, locale } = useI18n()
const loading = ref<boolean>(false)

// #region 增
const DEFAULT_FORM_DATA = {
  id: undefined,
  nickname: "",
  email: "",
  password: ""
}
const rotate = ref<boolean>(false)
const dialogVisible = ref<boolean>(false)
const dialogVisibleBulk = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref(cloneDeep(DEFAULT_FORM_DATA))
const specialCharRegex = /^[\u4e00-\u9fa5_a-zA-Z0-9]+$/

const buildSubmitPayload = () => {
  const payload = cloneDeep(formData.value)
  // 只在提交时加密，不修改输入框绑定的原始密码
  if (payload.id === undefined) {
    payload.password = rsaPsw(String(payload.password ?? ''))
  }
  return payload
}

const formRules: FormRules = {
  nickname: [
    {
      trigger: 'blur',
      required: true,
      message: t('userManagement.namePlaceholder')
    },
    {
      trigger: '',
      max: 30,
      message: t('userManagement.must'),
    },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!specialCharRegex.test(value)) {
          callback(new Error(t('userManagement.special')))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  email: [
    {
      trigger: 'blur',
      required: true,
      message: t('userManagement.emailPlaceholder')
    },
    {
      trigger: ['blur', 'change'],
      type: 'email',
      message: t('userManagement.emailError'),
    }
  ],
  password: [
    {
      trigger: '',
      required: true,
      message: t('userManagement.passwordPlaceholder')
    },
    {
      pattern: /^[a-zA-Z0-9]+$/,
      trigger: 'blur',
      message: t('userManagement.passwordOnly')
    },
    {
      max: 30,
      required: true,
      message: t('userManagement.passwordMust')
    },
  ]
}
watch(() => dialogVisibleBulk.value, (val: boolean) => {
  if (!val) {
    file.value = null
    fileName.value = ''
    input.value.value = ''
  }
})
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    loading.value = true
    if (formData.value.id === undefined) {
      const payload = buildSubmitPayload()
      post('/v1/user/register', payload).then((res: any) => {
        if (res.code === 0) {
          ElMessage.success(t('userManagement.inviteSuccess'))
        } else if(res.code === 400 && /^(昵称).*(已存在)$/.test(res.message)) {
          ElMessage.error(`${t('userManagement.name')} ${formData.value.nickname} ${t('userManagement.already')}`)
        } else if(res.code === 400 && /^(邮箱).*(已存在)$/.test(res.message)) {
          ElMessage.error(`${t('userManagement.email')} ${formData.value.email} ${t('userManagement.already')}`)
        }
        dialogVisible.value = false
      }).finally(() => {
        getTableData()
        loading.value = false
      })
    } else {
      post(`/v1/user/update_user/${formData.value.id}`, {
        nickname: formData.value.nickname,
        email: formData.value.email
      }).then(({ data }: any) => {
        if (data.code === 0) {
          ElMessage.success(t('userManagement.modifySuccess'))
        } else if(data.code === 103 && /^(Nickname).*(is already taken)$/.test(data.message)) {
          ElMessage.error(`${t('userManagement.name')} ${formData.value.nickname} ${t('userManagement.already')}`)
        } else if(data.code === 103 && /^(Email).*(is already taken)$/.test(data.message)) {
          ElMessage.error(`${t('userManagement.email')} ${formData.value.email} ${t('userManagement.already')}`)
        }
        dialogVisible.value = false
      }).finally(() => {
        getTableData()
        loading.value = false
      })
    }
  })
}
const resetForm = () => {
  formRef.value?.clearValidate()
  formData.value = cloneDeep(DEFAULT_FORM_DATA)
}
// #endregion

// #region 删
const handleDelete = (row: AuthUser) => {
  ElMessageBox.confirm(t('userManagement.deleteSingle'), '', {
    confirmButtonText: t('userManagement.yes'),
    cancelButtonText: t('userManagement.no'),
    type: 'warning'
  }).then(() => {
    _delete(`/v1/user/delete/${row.id}`).then(() => {
      ElMessage.success(t('userManagement.deleteSuccess'))
      getTableData()
    })
  })
}
// #endregion

// #region 改
const handleUpdate = (row: AuthUser) => {
  dialogVisible.value = true
  formData.value = cloneDeep(row) as any
}
// #endregion


// #region 查
const handleResetPassword = async (id: string) => {
  const { data } : any = await post(`/v1/user/reset/password/${id}`)
  if (data.code === 0) {
    ElMessage.success(t('userManagement.resetSuccess'))
  }
  getTableData()
}
// #endregion

// #region 查
const tableData = ref<any[]>([])
const searchFormRef = ref<FormInstance | null>(null)
const searchData = reactive({
  nickname: '',
  email: ''
})

// 存储多选的表格数据
const multipleSelection = ref<any[]>([])

const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const total = ref<number>(0)
const pageSizes = ref<number[]>([10, 20, 50, 100])

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getTableData()
}

// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getTableData()
}
const getTableData = () => {
  loading.value = true
  get(`/v1/user/subordinate_users?currentPage=${currentPage.value}&size=${pageSize.value}&nickname=${searchData.nickname}&email=${searchData.email}`).then(({ data }: any) => {
    total.value = data.data.total
    tableData.value = data.data.list
    // 清空选中数据
    multipleSelection.value = []
  }).catch(() => {
    tableData.value = []
  }).finally(() => {
    loading.value = false
    rotate.value = false
  })
}
const refresh = () => {
  rotate.value = true
  getTableData()
}
const handleSearch = () => {
  currentPage.value === 1 ? getTableData() : (currentPage.value = 1)
}
const resetSearch = () => {
  searchFormRef.value?.resetFields()
  handleSearch()
}

// 表格多选事件处理
const handleSelectionChange = (selection: any[]) => {
  multipleSelection.value = selection
}

// 批量删除方法
const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) return
  ElMessageBox.confirm(t('userManagement.deleteTip'), '', {
    confirmButtonText: t('userManagement.yes'),
    cancelButtonText: t('userManagement.no'),
    type: 'warning'
  }).then(async () => {
    loading.value = true
    try {
      // 使用 Promise.all 并行处理所有删除请求
      await Promise.all(
        multipleSelection.value.map((row: any) => _delete(`/v1/user/delete/${row.id}`))
      )
      ElMessage.success(t('userManagement.deleteSuccess'))
      getTableData()
    } catch {
      ElMessage.error(t('userManagement.fail'))
    } finally {
      loading.value = false
    }
  })
}
// #endregion

// 监听分页参数的变化
watch([() => currentPage.value, () => pageSize.value], getTableData, { immediate: true })
const downloadTemplate = () => {
  const link = document.createElement('a')
  link.href = '/users.xlsx'
  link.download = 'users.xlsx'
  link.click()
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
  if (loading.value) return
  loading.value = true
  const reqData = new FormData()
  const files = file.value
  reqData.append('file', files[0])
  const { data }: any = await post('/v1/user/batch_register', reqData)
  loading.value = false
  dialogVisibleBulk.value = false
  if (data.code !== 0) {
    ElMessage.error(data.message[locale.value as keyof typeof data.message])
  }
  getTableData()
  // 已注册错误处理
  if (
    typeof data.message === 'object' &&
    data.message !== null &&
    'failures' in data.message &&
    'successes count' in data.message
  ) {
    const failures = data.message.failures
    const successCount = data.message['successes count']

    if (Object.keys(failures).length > 0) {
      Object.keys(failures).forEach((email: string) => {
        const reason = failures[email]
        if (reason === 'User already exists') {
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
}
</script>

<template>
  <div class="min-h-100vh relative">
    <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8] bg-white w-[calc(100%-68px)] fixed top-0 z-2 flex items-center">
      <a href="/knowledge-base" class="team-management-back" aria-label="Back to knowledge base">
        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M12.5 4.17 6.67 10l5.83 5.83" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </a>
      {{ t('common.teamManagement') }}
    </h1>
    <div class="flex w-full mt-80px">
      <div class="bg-[#F3F6FF] w-full p-32px min-h-[calc(100vh-80px)]">
        <h2 class="text-32px leading-48px font-600 text-brand-0 mb-40px">
          {{ t('userManagement.title') }}
        </h2>
        <el-card shadow="never" class="search-wrapper">
          <el-form ref="searchFormRef" :inline="true" :model="searchData">
            <el-form-item prop="nickname" :label="t('userManagement.name')">
              <el-input v-model="searchData.nickname" :placeholder="t('userManagement.namePlaceholder')" />
            </el-form-item>
            <el-form-item prop="email" :label="t('userManagement.email')">
              <el-input v-model="searchData.email" :placeholder="t('userManagement.emailPlaceholder')" />
            </el-form-item>
            <el-form-item>
              <div v-permission="'user:search'" class="rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-20px flex items-center font-500 hover:bg-[#244FF0] ml-[-12px]" @click="handleSearch">
                <Search class="w-20px h-20px mr-10px" />
                {{ t('userManagement.search') }}
              </div>
              <div class="rounded-6px cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px ml-20px flex items-center font-500 hover:(bg-[#396FFA] text-white)" @click="resetSearch">
                <Reset class="w-20px h-20px mr-10px" />
                {{ t('userManagement.reset') }}
              </div>
            </el-form-item>
          </el-form>
        </el-card>
        <div class="toolbar-wrapper">
          <div class="flex items-center">
            <div v-permission="'user:delete'" v-if="store.role !== 'admin'" :class="!multipleSelection.length && '!bg-[#F8717180] cursor-not-allowed'" class="bg-[#F87171] cursor-pointer rounded-6px py-8px px-10px text-sm font-500 text-white flex items-center" @click="handleBatchDelete">
              <Delete class="mr-10px" />
              {{ t('userManagement.deleteBtn') }}
            </div>
            <div v-permission="'user:create'" @click="dialogVisible = true" class="rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center font-500 hover:bg-[#244FF0]">
              <Invite class="mr-10px" />
              {{ t('userManagement.singleInvite') }}
            </div>
            <div v-permission="'user:create'" @click="dialogVisibleBulk = true" class="border-1 border-[#CED6E1] text-[#52555F] py-8px px-12px text-sm cursor-pointer rounded-4px mx-12px">
              {{ t('userManagement.inviteBulk') }}
            </div>
            <div @click="downloadTemplate" class="cursor-pointer underline text-[#396FFA] text-xs">
              {{ t('userManagement.download') }}
            </div>
          </div>
          <el-tooltip effect="dark" :content="t('userManagement.reload')" placement="top">
            <div @click="refresh" class="w-40px h-40px rounded-6px p-10px bg-[#396FFA] text-white cursor-pointer hover:bg-[#244FF0]">
              <Reset class="w-20px h-20px mr-10px" :class="rotate ? 'rotate360' : ''" />
            </div>
          </el-tooltip>
        </div>
        <div v-loading="loading" class="border border-white bg-white rounded-8px overflow-hidden shadows">
          <div class="table-wrapper">
            <el-table :data="tableData" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column prop="nickname" :label="t('userManagement.name')" align="center" width="180">
                <template #default="scope">
                  <div class="flex items-center pl-32px w-170px">
                    <div class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
                      {{ scope.row.nickname.slice(0, 1).toUpperCase() }}
                    </div>
                    <el-tooltip effect="dark" :content="scope.row.nickname" placement="top">
                      <div class="truncate">
                        {{ scope.row.nickname }}
                      </div>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="email" :label="t('userManagement.email')" align="center" />
              <el-table-column prop="createTime" :label="t('userManagement.joining')" align="center">
                <template #default="scope">
                  {{ dayjs.utc(scope.row.create_time).local().format('DD/MM/YYYY HH:mm:ss') }}
                </template>
              </el-table-column>
              <el-table-column prop="updateTime" :label="t('userManagement.modified')" align="center">
                <template #default="scope">
                  {{ dayjs.utc(scope.row.update_time).local().format('DD/MM/YYYY HH:mm:ss') }}
                </template>
              </el-table-column>
              <el-table-column fixed="right" :label="t('userManagement.action')" align="center">
                <template #default="scope">
                  <div class="flex justify-center">
                    <el-tooltip v-permission="'user:reset'" effect="dark" :content="t('userManagement.resetPassword')" placement="top">
                      <Lock @click="handleResetPassword(scope.row.id)" class="svg mr-12px cursor-pointer" />
                    </el-tooltip>
                    <el-tooltip v-permission="'user:modify'" effect="dark" :content="t('userManagement.modifyBtn')" placement="top">
                      <Edit @click="handleUpdate(scope.row)" class="svg mr-12px cursor-pointer" />
                    </el-tooltip>
                    <el-tooltip v-permission="'user:delete'" v-if="store.role !== 'admin'" effect="dark" :content="t('userManagement.delete')" placement="top">
                      <DeleteBtn @click="handleDelete(scope.row)" class="svg cursor-pointer" />
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pager-wrapper">
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
        <!-- 批量导入 -->
        <el-dialog
          v-model="dialogVisibleBulk"
          :title="t('userManagement.invite')"
          width="392px"
          align-center
        >
          <input ref="input" class="hidden" type="file" name="file" accept=".xlsx" @change="handleChange">
          <div @click="input.click" class="border-1 border-[#CED6E1] py-10px cursor-pointer w-full flex items-center justify-center text-[#52555F] rounded-4px mb-8px mt-8px text-14px leading-20px">
            <Add class="mr-16px" />
            {{ t('userManagement.select') }}
          </div>
          <div v-show="file" class="text-xs text-brand-0 my-8px font-600 flex items-center">
            <Xlsx class="mr-4px" /> {{ fileName }}
          </div>
          <div class="text-xs text-brand-0 mb-24px flex">
            <span class="text-[#FF5050] text-xs font-600 flex mr-4px">*</span>
            {{ t('userManagement.selectTip') }}
          </div>
          <div :class="file ? 'hover:bg-[#244FF0]' : 'cursor-not-allowed opacity-50'" v-loading="loading" @click="handleInviteBulk" class="rounded-6px w-140px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px mx-auto flex justify-center items-center font-500">
            {{ t('userManagement.ok') }}
          </div>
        </el-dialog>
        <!-- 新增/修改 -->
        <el-dialog
          v-model="dialogVisible"
          :title="t('userManagement.title')"
          width="520px"
          align-center
          @closed="resetForm"
        >
          <el-form ref="formRef" :model="formData" :rules="formRules" label-position="left">
            <el-form-item prop="nickname" :label="t('userManagement.name')">
              <el-input v-model="formData.nickname" :placeholder="t('userManagement.namePlaceholder')" />
            </el-form-item>
            <el-form-item prop="email" :label="t('userManagement.email')">
              <el-input v-model="formData.email" :placeholder="t('userManagement.emailPlaceholder')" />
            </el-form-item>
            <el-form-item v-if="formData.id === undefined" prop="password" :label="t('userManagement.password')">
              <el-input v-model="formData.password" :placeholder="t('userManagement.passwordPlaceholder')" />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="flex justify-center">
              <div @click="dialogVisible = false" class="rounded-6px w-140px cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px ml-20px flex justify-center items-center font-500 hover:(bg-[#396FFA] text-white)">
                {{ t('userManagement.cancel') }}
              </div>
              <div :loading="loading" @click="handleCreateOrUpdate" class="rounded-6px w-140px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-20px flex justify-center items-center font-500 hover:bg-[#244FF0]">
                {{ t('userManagement.ok') }}
              </div>
            </div>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.team-management-back {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #232748;

  &:hover {
    background: #f5f7ff;
    color: #396ffa;
  }

  svg {
    width: 20px;
    height: 20px;
  }
}

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
svg {
  outline: none;
}
:deep(.el-tooltip__trigger.svg) {
  rect.borders {
    stroke: transparent;
  }
  rect.bg {
    fill: transparent;
  }
  &:hover {
    rect.borders {
      stroke: #396FFA;
    }
    rect.bg {
      fill: #EBF1FE;
    }
  }
}
.search-wrapper {
  margin-bottom: 20px;
  :deep(.el-card__body) {
    padding-top: 0;
    padding-bottom: 16px;
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
:deep(.el-card) {
  border: none;
  &.search-wrapper {
    border-bottom: 1px solid #E1E3E8;
    background-color: #F3F6FF !important;
  }
}
:deep(.el-dialog) {
  .el-dialog__header.show-close {
    margin-bottom: 24px;
  }
  .el-dialog__body {
    .el-form {
      .el-form-item {
        margin-bottom: 22px;
        flex-direction: column;
        align-items: flex-start;
        .el-input__wrapper {
          &.is-focus {
            box-shadow: 0 0 0 1px #396FFA inset;
          }
          .el-input__inner {
            min-height: 40px;
          }
        }
        .el-form-item__label {
          font-size: 16px;
          font-weight: 600;
          line-height: 24px;
          color: #232748;
          font-family: 'Encode Sans';
          &::before {
            content: "*";
            font-size: 18px;
            line-height: 24px;
            font-weight: bold;
            color: #FF5050;
            font-family: 'Helvetica';
          }
        }
        .el-form-item__content {
          width: 100%;
        }
      }
    }
  }
}

.shadows {
  box-shadow: 0px 4px 35px 0px #8195C82E;
}

.toolbar-wrapper {
  display: flex;
  padding-bottom: 24px;
  background-color: #F3F6FF;
  justify-content: space-between;
}

.table-wrapper {
  overflow: hidden;
  border-radius: 8px;
  background-color: #fff;
}

.pager-wrapper {
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
}
.el-card.is-never-shadow {
  overflow: hidden;
  border-radius: 8px;
}
</style>
