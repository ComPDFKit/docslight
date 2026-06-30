<script lang="ts" setup>
import dayjs from "dayjs"
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import {
  deleteDocumentApi,
  getDocumentListApi,
  runDocumentParseApi
} from '@@/apis/kbs/document'
import {
  batchDeleteKnowledgeBaseApi,
  deleteKnowledgeBaseApi,
  getKnowledgeBaseListApi
} from '@@/apis/kbs/knowledgebase'
import { usePagination } from '@@/composables/usePagination'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onActivated, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message-box.css'
import 'element-plus/theme-chalk/el-message.css'
import Search from '@@/components/Image/Search.vue'
import Reset from '@@/components/Image/Reset.vue'
import Delete from '@@/components/Image/Delete.vue'
import DeleteBtn from '@@/components/Image/DeleteBtn.vue'
import View from '@@/components/Image/View.vue'
import Refresh from '@@/components/Image/Refresh.vue'
import Pause from '@@/components/Image/Pause.vue'
import Start from '@@/components/Image/Start.vue'
import Document from '@@/components/Image/Document.vue'

defineOptions({
  // 命名当前组件
  name: 'KnowledgeBase'
})

const { t } = useI18n()
const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()
const createDialogVisible = ref(false)
const uploadLoading = ref(false)

// 添加清理函数
function cleanupResources() {
  // 重置所有状态
  if (multipleSelection.value) {
    multipleSelection.value = []
  }

  loading.value = false
  documentLoading.value = false
  fileLoading.value = false
  uploadLoading.value = false

  // 关闭所有对话框
  viewDialogVisible.value = false
  createDialogVisible.value = false
  addDocumentDialogVisible.value = false
}

// 在组件停用时清理资源
onDeactivated(() => {
  cleanupResources()
})

// 在组件卸载前清理资源
onBeforeUnmount(() => {
  cleanupResources()
})

// 定义知识库数据类型
interface KnowledgeBaseData {
  id: string
  name: string
  description: string
  doc_num: number
  create_time: number
  create_date: string
  avatar?: string
  language: string
  permission: string
  chunk_num: number
  token_num: number
}

interface ApiResponse<T> {
  data: T
  code: number
  message: string
}

interface ListResponse {
  list: any[]
  total: number
}

// 查询知识库列表
const tableData = ref<KnowledgeBaseData[]>([])
const searchFormRef = ref<FormInstance | null>(null)
const searchData = reactive({
  name: ''
})

// 存储多选的表格数据
const multipleSelection = ref<KnowledgeBaseData[]>([])

// 获取知识库列表数据
function getTableData() {
  loading.value = true
  // 调用获取知识库列表API
  getKnowledgeBaseListApi({
    currentPage: paginationData.currentPage,
    size: paginationData.pageSize,
    name: searchData.name
  }).then((response) => {
    const result = response as ApiResponse<ListResponse>
    paginationData.total = result.data.total
    tableData.value = result.data.list
    // 清空选中数据
    multipleSelection.value = []
  }).catch(() => {
    tableData.value = []
  }).finally(() => {
    loading.value = false
  })
}

// 搜索处理
function handleSearch() {
  paginationData.currentPage === 1 ? getTableData() : (paginationData.currentPage = 1)
}

// 重置搜索
function resetSearch() {
  searchFormRef.value?.resetFields()
  handleSearch()
}

// 查看知识库详情
const viewDialogVisible = ref(false)
const currentKnowledgeBase = ref<KnowledgeBaseData | null>(null)
const documentLoading = ref(false)
const documentList = ref<any[]>([])

// 文档列表分页
const docPaginationData = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
  pageSizes: [5, 10, 20],
  layout: 'total, sizes, prev, pager, next, jumper'
})

// 处理文档分页变化
function handleDocCurrentChange(page: number) {
  docPaginationData.currentPage = page
  getDocumentList()
}

function handleDocSizeChange(size: number) {
  docPaginationData.pageSize = size
  docPaginationData.currentPage = 1
  getDocumentList()
}
let intervalId: any = 0
// 获取知识库下的文档列表
function getDocumentList() {
  if (!currentKnowledgeBase.value) return

  documentLoading.value = true
  getDocumentListApi({
    kb_id: currentKnowledgeBase.value.id,
    currentPage: docPaginationData.currentPage,
    size: docPaginationData.pageSize,
    name: ''
  }).then((response) => {
    const result = response as ApiResponse<ListResponse>
    documentList.value = result.data.list
    let i = 0
    documentList.value.forEach((item: any) => {
      if (![-1, 0, 1].includes(item.progress)) {
        i++
      }
    })
    if (!i) {
      clearInterval(intervalId)
    }
    docPaginationData.total = result.data.total
  }).catch((error) => {
    ElMessage.error(error?.message)
    documentList.value = []
  }).finally(() => {
    documentLoading.value = false
  })
}

// 修改handleView方法
function handleView(row: KnowledgeBaseData) {
  currentKnowledgeBase.value = row
  viewDialogVisible.value = true
  // 重置文档分页
  docPaginationData.currentPage = 1
  // 获取文档列表
  getDocumentList()
}

// 格式化解析状态
function formatParseStatus(progress: number) {
  if (progress === 0) return t('knowledge.pending')
  if (progress === 1) return t('knowledge.success')
  if (progress === -1) return t('knowledge.fail')
  return `${t('knowledge.parsing')} ${Math.floor(progress * 100)}%`
}

// 获取解析状态对应的标签类型
function getParseStatusType(progress: number) {
  if (progress === 0) return 'border-[#FFC0614D] bg-[#FFF4E5] text-[#F09004]'
  if (progress === 1) return 'border-[#00CF854D] bg-[#E2F7EF] text-[#00CF85]'
  if (progress === -1) return 'border-[#F871714D] bg-[#FBEDED] text-[#F87171]'
  return 'border-[#244FF04D] bg-[#EBF1FE] text-[#396FFA]'
}

// 修改 handleParseDocument 方法
function handleParseDocument(row: any) {
  runDocumentParseApi(row.id)
    .then(() => {
      // 刷新文档列表
      getDocumentList()
      intervalId = setInterval(() => {
        getDocumentList()
      }, 3000)
    })
    .catch((error) => {
      ElMessage.error(error?.message)
    })
}

// 处理移除文档
function handleRemoveDocument(row: any) {
  ElMessageBox.confirm(
    t('knowledge.removeTip'),
    '',
    {
      confirmButtonText: t('knowledge.yes'),
      cancelButtonText: t('knowledge.no'),
      type: 'warning',
      center: true,
      dangerouslyUseHTMLString: true,
      customClass: 'delete-confirm-dialog',
      distinguishCancelAndClose: true,
      showClose: false,
      closeOnClickModal: false,
      closeOnPressEscape: true,
      roundButton: true,
    }
  ).then(() => {
    deleteDocumentApi(row.id)
      .then(() => {
        ElMessage.success(t('knowledge.removeSuccess'))
        // 刷新文档列表
        getDocumentList()
        // 刷新知识库列表（因为文档数量会变化）
        getTableData()
      })
      .catch((error) => {
        ElMessage.error(t('knowledge.removeFail'))
      })
  }).catch(() => {
    // 用户取消操作
  })
}

// 添加文档对话框
const addDocumentDialogVisible = ref(false)
const fileLoading = ref(false)

// 删除知识库
function handleDelete(row: KnowledgeBaseData) {
  ElMessageBox.confirm(
    t('knowledge.deleteTip'),
    '',
    {
      confirmButtonText: t('knowledge.yes'),
      cancelButtonText: t('knowledge.cancel'),
      type: 'warning',
      dangerouslyUseHTMLString: true,
      center: true,
      customClass: 'delete-confirm-dialog',
      distinguishCancelAndClose: true,
      showClose: false,
      closeOnClickModal: false,
      closeOnPressEscape: true,
      roundButton: true,
      beforeClose: (action: any, instance: any, done: Function) => {
        if (action === 'confirm') {
          instance.confirmButtonLoading = true
          loading.value = true
          deleteKnowledgeBaseApi(row.id)
            .then(() => {
              ElMessage.success(t('knowledge.deleteSuccess'))
              getTableData() // 刷新表格数据
              done()
            })
            .catch(() => {
              ElMessage.error(t('knowledge.deleteFail'))
              done()
            })
            .finally(() => {
              instance.confirmButtonLoading = false
              loading.value = false
            })
        } else {
          done()
        }
      }
    }
  ).catch(() => {
    // 用户取消删除操作
  })
}

// 批量删除知识库
function handleBatchDelete() {
  if (multipleSelection.value.length === 0) return

  ElMessageBox.confirm(
    t('knowledge.deleteTip'),
    '',
    {
      confirmButtonText: t('knowledge.yes'),
      cancelButtonText: t('knowledge.cancel'),
      type: 'warning',
      dangerouslyUseHTMLString: true,
      center: true,
      customClass: 'delete-confirm-dialog',
      distinguishCancelAndClose: true,
      showClose: false,
      closeOnClickModal: false,
      closeOnPressEscape: true,
      roundButton: true,
      beforeClose: (action: any, instance: any, done: Function) => {
        if (action === 'confirm') {
          loading.value = true
          instance.confirmButtonLoading = true
          const ids = multipleSelection.value.map((item :any) => item.id)
          batchDeleteKnowledgeBaseApi(ids)
            .then(() => {
              ElMessage.success(t('knowledge.deleteSuccess'))
              getTableData() // 刷新表格数据
              done()
            })
            .catch(() => {
              ElMessage.error(t('knowledge.deleteFail'))
              done()
            })
            .finally(() => {
              instance.confirmButtonLoading = false
              loading.value = false
            })
        } else {
          done()
        }
      }
    }
  ).catch(() => {
    // 用户取消删除操作
  })
}

// 表格多选事件处理
function handleSelectionChange(selection: KnowledgeBaseData[]) {
  multipleSelection.value = selection
}

// 监听分页参数的变化
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, { immediate: true })

// 确保页面挂载和激活时获取数据
onMounted(() => {
  getTableData()
})

// 当从其他页面切换回来时刷新数据
onActivated(() => {
  getTableData()
})
</script>

<template>
  <div class="app-container">
    <h1 class="text-32px leading-48px font-600 text-brand-0 mb-40px">
      {{ t('knowledge.title') }}
    </h1>
    <el-card v-loading="loading" shadow="never" class="search-wrapper">
      <el-form ref="searchFormRef" :inline="true" :model="searchData">
        <el-form-item prop="name" :label="t('knowledge.knowledgeName')">
          <el-input v-model="searchData.name" :placeholder="t('knowledge.searchPlaceholder')" />
        </el-form-item>
        <el-form-item class="hidden">
          <input class="hidden" v-model="searchData.name" :placeholder="t('knowledgeBases.searchPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <div class="rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center font-500 hover:bg-[#244FF0] ml-[-40px]" @click="handleSearch">
            <Search class="w-20px h-20px mr-10px" />
            {{ t('knowledge.search') }}
          </div>
          <div class="rounded-6px cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px ml-20px flex items-center font-500 hover:(bg-[#396FFA] text-white)" @click="resetSearch">
            <Reset class="w-20px h-20px mr-10px" />
            {{ t('knowledge.reset') }}
          </div>
        </el-form-item>
      </el-form>
    </el-card>
    <div class="toolbar-wrapper bg-[#F3F6FF]">
      <div :class="!multipleSelection.length && '!bg-[#F8717180] cursor-not-allowed'" class="bg-[#F87171] cursor-pointer rounded-6px py-8px px-10px text-sm font-500 text-white flex items-center" @click="handleBatchDelete">
        <Delete class="mr-10px" />
        {{ t('knowledge.deleteBtn') }}
      </div>
    </div>
    <div v-loading="loading" class="border border-white bg-white rounded-8px overflow-hidden shadows">
      <div class="table-wrapper">
        <el-table :data="tableData" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column :label="t('knowledge.order')" align="center" width="80">
            <template #default="scope">
              {{ (paginationData.currentPage - 1) * paginationData.pageSize + scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="name" :label="t('knowledge.knowledgeName')" align="center" min-width="120" show-overflow-tooltip />
          <el-table-column prop="description" :label="t('knowledge.desc')" align="center" min-width="180" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.description === '暂无描述' ? '-' : scope.row.description }}
            </template>
          </el-table-column>
          <el-table-column prop="doc_num" :label="t('knowledge.docs')" align="center" width="100" />
          <!-- 添加权限列 -->
          <el-table-column :label="t('knowledge.permission')" align="center">
            <template #default="scope">
              <div class="border-1 rounded-6px border-solid py-2px px-8px text-12px leading-16px w-fit mx-auto"
                :class="scope.row.permission === 'me' ? 'border-[#00CF854D] text-[#00CF85] bg-[#E2F7EF]' : 'border-[#F871714D] text-[#F87171] bg-[#FBEDED]'"
              >
                {{ scope.row.permission === 'me' ? t('knowledge.individual') : t('knowledge.team') }}
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="t('knowledge.created')" align="center" width="180">
            <template #default="scope">
              {{ scope.row.create_date }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="t('knowledge.action')" width="180" align="center">
            <template #default="scope">
              <div class="flex justify-center">
                <el-tooltip effect="dark" :content="t('knowledge.show')" placement="top">
                  <View @click="handleView(scope.row)" class="mr-12px cursor-pointer" />
                </el-tooltip>
                <el-tooltip effect="dark" :content="t('knowledge.delete')" placement="top">
                  <DeleteBtn @click="handleDelete(scope.row)" class="cursor-pointer" />
                </el-tooltip>
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

    <!-- 知识库详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="`${t('knowledge.knowledgeName')} - ${currentKnowledgeBase?.name || ''}`"
      width="800px"
      align-center
    >
      <div v-if="currentKnowledgeBase">
        <div class="kb-info-header mt-8px">
          <div class="flex">
            <span class="kb-info-label">{{ t('knowledge.id') }}</span> {{ currentKnowledgeBase.id }}
          </div>
          <div class="flex">
            <span class="kb-info-label">{{ t('knowledge.docs') }}</span> {{ currentKnowledgeBase.doc_num }}
          </div>
          <div class="flex">
            <span class="kb-info-label">{{ t('knowledge.permission') }}</span>
            <div class="border-1 rounded-6px border-solid py-2px px-8px text-12px leading-16px w-fit mx-auto h-fit"
              :class="currentKnowledgeBase.permission === 'me' ? 'border-[#00CF854D] text-[#00CF85] bg-[#E2F7EF]' : 'border-[#F871714D] text-[#F87171] bg-[#FBEDED]'"
            >
              {{ currentKnowledgeBase.permission === 'me' ? 'Individual' : 'Team' }}
            </div>
          </div>
        </div>

        <div class="document-table-wrapper" v-loading="documentLoading">
          <el-table :data="documentList" style="width: 100%" max-height="60vh">
            <el-table-column prop="name" :label="t('knowledge.knowledgeName')" width="180" show-overflow-tooltip>
              <template #default="scope">
                <div class="flex items-center text-brand-2">
                  <Document class="min-w-20px mr-4px" />
                  {{ scope.row.name }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="chunk_num" :label="t('knowledge.chunks')" width="100" align="center" />
            <el-table-column prop="create_date" :label="t('knowledge.upload')" width="180" align="center">
              <template #default="scope">
                {{ dayjs(scope.row.create_date).format('DD/MM/YYYY HH:mm:ss') }}
              </template>
            </el-table-column>
            <el-table-column :label="t('knowledge.status')" min-width="163px" align="center">
              <template #default="scope">
                <div class="flex pl-20px items-center text-12px leading-16px">
                  <div :class="getParseStatusType(scope.row.progress)" class="border-1 rounded-6px border-solid py-2px px-8px text-12px leading-16px min-w-115px py-6px mr-4px">
                    {{ formatParseStatus(scope.row.progress) }}
                  </div>
                  <el-tooltip v-if="scope.row.progress === 0" effect="dark" :content="t('knowledge.start')" placement="top">
                    <Start @click="handleParseDocument(scope.row)" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip>
                  <!-- <el-tooltip v-else-if="scope.row.progress === 1" effect="dark" :content="t('knowledge.reParsing')" placement="top">
                    <Refresh @click="handleParseDocument(scope.row)" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip> -->
                  <el-tooltip v-else-if="scope.row.progress === -1" effect="dark" :content="t('knowledge.reParsing')" placement="top">
                    <Refresh @click="handleParseDocument(scope.row)" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip>
                  <!-- <el-tooltip v-else effect="dark" :content="t('knowledge.cancelParsing')" placement="top">
                    <Pause @click="handleParseDocument(scope.row)" class="cursor-pointer min-w-20px h-20px" />
                  </el-tooltip> -->
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('knowledge.action')" align="center">
              <template #default="scope">
                <el-tooltip effect="dark" :content="t('knowledge.delete')" placement="top">
                  <DeleteBtn @click="handleRemoveDocument(scope.row)" class="mx-auto cursor-pointer" />
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页控件 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="docPaginationData.currentPage"
              v-model:page-size="docPaginationData.pageSize"
              :page-sizes="docPaginationData.pageSizes"
              :layout="docPaginationData.layout"
              :total="docPaginationData.total"
              @size-change="handleDocSizeChange"
              @current-change="handleDocCurrentChange"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 94%;
}
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
          min-width: 250px;
          min-height: 40px;
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
  padding-bottom: 20px;
  justify-content: space-between;
}

.pager-wrapper {
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
}

.document-table-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
  margin-top: 16px;
}

.kb-info-header {
  gap: 16px;
  display: flex;
  padding: 16px;
  flex-wrap: wrap;
  font-size: 16px;
  line-height: 24px;
  color: #232748;
  border-radius: 8px;
  margin-bottom: 24px;
  align-items: center;
  background-color: #F3F6FF;

  .flex {
    align-items: center;
  }

  .kb-info-label {
    font-weight: 600;
    margin-right: 12px;
  }
}

.document-table-wrapper {
  margin-top: 20px;
}

.document-table-header {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 16px;
  margin-top: 16px;
}

.pagination-container {
  margin-top: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.delete-confirm-dialog {
  :deep(.el-message-box__message) {
    text-align: center;
  }
}
</style>
