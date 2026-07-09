<template>
  <div class="bg-[#F3F6FF] min-h-100vh w-full">
    <KbHeader />
    <div class="flex min-h-[calc(100vh-88px)] pt-88px">
      <KbSidebar :kbDetail="kbDetail" />
      <div class="py-40px pl-32px w-[calc(100%-240px)] pr-80px">
        <div class="flex text-xs mb-36px">
          <a href="/knowledge-base" class="text-brand-1">{{ t('knowledgeBases.title') }}</a>
          <ArrowRight class="mx-12px" />
          <a :href="'/knowledge-base/dataset?id=' + route.query.id" class="text-brand-2">{{ t('knowledgeBases.dataset.title') }}</a>
          <ArrowRight class="mx-12px" />
          <div class="text-brand-2">{{ t('knowledgeBases.dataset.chunk') }}</div>
        </div>
        <div class="flex items-end justify-between pb-24px border-b border-[#E1E3E8] mb-32px">
          <div class="flex items-center text-brand-2 max-w-250px">
            <a :href="`/knowledge-base/dataset?id=${route.query.id}`"><ArrowBlack /></a>
            <Document class="mx-4px min-w-16px" />
            <div class="truncate">{{ filename }}</div>
          </div>
          <div class="flex">
            <div class="flex bg-[#CDDBFF] p-4px text-brand-0 text-xs rounded-8px mr-8px">
              <div @click="model = 'expand'" class="py-6px px-12px rounded-4px cursor-pointer whitespace-nowrap" :class="model === 'expand' && 'bg-white'">
                {{ t('knowledgeBases.dataset.expand') }}
              </div>
              <div @click="model = 'collapse'" class="py-6px px-12px rounded-4px cursor-pointer whitespace-nowrap" :class="model === 'collapse' && 'bg-white'">
                {{ t('knowledgeBases.dataset.collapse') }}
              </div>
            </div>
            <div @click.stop="role === 'viewer' ? '' : bulk = !bulk, filterShow = false" class="min-w-86px cursor-pointer mr-8px relative border border-[#CED6E1] py-10px px-12px rounded-4px">
              <div class="flex items-center text-xs">
                {{ t('knowledgeBases.dataset.bulk') }}
                <Arrow class="transitions ml-12px" />
              </div>
              <div v-show="bulk" class="absolute shadows bg-white py-4px px-8px whitespace-nowrap top-52px left-0 rounded-4px z-2 text-xs">
                <div @click.stop="selectAll = !selectAll" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                  <div v-if="selectAll" class="privacy-box-active mr-4px">
                    <img src="../assets/images/login/check_box.svg" class="cursor-pointer" />
                  </div>
                  <span v-else class="privacy-box cursor-pointer mr-4px"></span>
                  {{ t('knowledgeBases.dataset.selectAll') }}
                </div>
                <div class="h-1px w-full bg-[#E1E3E8]"></div>
                <div @click="handleSelectionChange('enable')" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                  <Enable class="mr-4px" />
                  {{ t('knowledgeBases.dataset.enableChunk') }}
                </div>
                <div @click="handleSelectionChange('disable')" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                  <Disable class="mr-4px" />
                  {{ t('knowledgeBases.dataset.disableChunk') }}
                </div>
                <div class="h-1px w-full bg-[#E1E3E8]"></div>
                <div @click="deleteChunk" class="flex items-center py-8px px-12px rounded-6px hover:bg-[#1460F31A]">
                  <Delete class="mr-4px svg" />
                  {{ t('knowledgeBases.dataset.delete') }}
                </div>
              </div>
            </div>
            <!-- <el-input v-show="searchShow" @blur="searchShow = false" v-model="searchQuery" clearable @clear="getChunkDetail" @keyup.enter="getChunkDetail" class="mr-8px" :placeholder="t('knowledgeBases.dataset.search')">
              <template #prefix>
                <Search />
              </template>
            </el-input> -->
            <!-- <div v-show="!searchShow" @click="searchClick" class="cursor-pointer p-10px rounded-4px border border-[#CED6E1] mr-8px text-[#94969D] hover:(border-[#396FFA] text-[#396FFA])"><Search /></div> -->
            <el-tooltip popper-class="box-item" class="ml-4px" :content="t('knowledgeBases.dataset.selected')" placement="top">
              <div @click.stop="filterShow = true, bulk = false" class="relative cursor-pointer p-10px rounded-4px border border-[#CED6E1] mr-8px text-[#94969D] hover:(border-[#396FFA] text-[#396FFA])" :class="filterShow && '!border-[#396FFA] !text-[#396FFA]'"><Filter />
                <div v-show="filterShow" class="absolute flex flex-col shadows bg-white py-4px px-8px whitespace-nowrap top-52px right-0 rounded-4px z-2 text-xs">
                  <el-radio-group v-model="filter">
                    <el-radio label="all">{{ t('knowledgeBases.dataset.selectAll') }}</el-radio>
                    <el-radio label="enable">{{ t('knowledgeBases.dataset.enable') }}</el-radio>
                    <el-radio label="disable">{{ t('knowledgeBases.dataset.disable') }}</el-radio>
                  </el-radio-group>
                </div>
              </div>
            </el-tooltip>
            <AddChunk :class="role === 'viewer' && 'cursor-not-allowed opacity-60'" @click="role === 'viewer' ? '' : dialogVisible = true, chunkType = 'create', keyword = [], question = [], content = ''" class="cursor-pointer min-w-40px" />
          </div>
        </div>
        <div v-loading="loading" class="rounded-10px bg-white shadows px-32px py-20px">
          <div v-for="(chunk, index) in chunkDetail" :key="index" :class="index && 'mt-24px'" class="flex items-start justify-between p-12px border border-[#E1E3E8] rounded-8px text-black">
            <div class="flex items-start w-[calc(100%-96px)]">
              <div v-if="statusArr[index].status" class="privacy-box-active">
                <img src="../assets/images/login/check_box.svg" class="cursor-pointer" @click="statusArr[index].status = false" />
              </div>
              <span :class="role === 'viewer' && 'cursor-not-allowed opacity-60'" v-else class="privacy-box cursor-pointer" @click="role === 'viewer' ? '' : statusArr[index].status = true"></span>
              <div @dblclick="handleClick(chunk.chunk_id)" class="cursor-pointer mx-8px w-full break-words" :class="model === 'collapse' && 'collapse'" v-html="sanitizeHtml(chunk.content_with_weight)"></div>
            </div>
            <el-switch :disabled="role === 'viewer'" v-model="chunk.available_int" @change="changeStatus(chunk.available_int, chunk.chunk_id)"
              style="--el-switch-on-color: #396FFA; --el-switch-off-color: #CED6E1"
              :active-value="1" :inactive-value="0">
            </el-switch>
          </div>
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
    <el-dialog v-model="dialogVisible" align-center width="400px">
      <h3 class="text-sm font-bold text-[#43474D] py-4px mb-24px">{{ t('knowledgeBases.dataset.createChunk') }}</h3>
      <div class="flex text-xs text-brand-0 font-600 mb-8px">
        <span class="text-[#FF5050] inline-block mr-4px font">*</span>
        {{ t('knowledgeBases.dataset.chunk') }}
      </div>
      <el-input type="textarea" :rows="7" v-model="content" :placeholder="t('knowledgeBases.dataset.content')"></el-input>
      <div v-show="errorChunk" class="text-[#f56c6c] text-12px leading-12px mt-2px">{{ errorChunk }}</div>
      <div class="flex text-xs text-brand-0 font-600 mb-8px mt-12px">
        {{ t('knowledgeBases.dataset.keyword') }}
      </div>
      <div class="flex justify-between">
        <el-select
          multiple
          ref="inputKeyword"
          popper-class="no-arrow"
          filterable
          @change="clearInput('keyword')"
          allow-create
          default-first-option
          :reserve-keyword="false"
          class="no-arrow"
          v-model="keyword"
          :placeholder="t('knowledgeBases.dataset.content')">
        </el-select>
      </div>
      <div class="flex text-xs text-brand-0 font-600 mb-8px mt-12px">
        {{ t('knowledgeBases.dataset.question') }}
      </div>
      <div class="flex justify-between">
        <el-select
          multiple
          ref="inputQuestion"
          popper-class="no-arrow"
          filterable
          @change="clearInput('question')"
          allow-create
          default-first-option
          :reserve-keyword="false"
          class="no-arrow"
          v-model="question"
          :placeholder="t('knowledgeBases.dataset.content')">
          <template #tag>
            <el-tooltip v-for="(item, index) in question" :key="index" popper-class="box-item" class="ml-4px" :content="item" placement="top">
              <el-tag class="custom" closable @close="removeKb(item)">
                <div class="flex items-center max-w-320px truncate text-brand-0 text-12px leading-16px">
                  <div class="w-full truncate">{{ item }}</div>
                </div>
              </el-tag>
            </el-tooltip>
          </template>
        </el-select>
      </div>
      <div class="flex justify-center mt-24px">
        <div class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)" @click="dialogVisible = false">
          {{ t('knowledgeBases.dataset.cancel') }}
        </div>
        <div :class="!content.length && 'cursor-not-allowed opacity-50'" class="w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]" @click="chunkType === 'edit' ? submitChunk('set') : submitChunk('create')">
          {{ t('knowledgeBases.dataset.ok') }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { get, post } from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import Document from '../components/images/Document.vue'
import { onMounted, ref, watch, onBeforeUnmount } from 'vue'
import ArrowBlack from '../components/images/ArrowBlack.vue'
import Enable from '../components/images/Enable.vue'
import Disable from '../components/images/Disable.vue'
import Search from '../components/images/Search.vue'
import Filter from '../components/images/Filter.vue'
import Arrow from '../components/images/Arrow.vue'
import Delete from '../components/images/Delete.vue'
import AddChunk from '../components/images/AddChunk.vue'
import { sanitizeHtml } from '../utils/sanitizeHtml'

const { t } = useI18n()
const role = ref()
const filter = ref()
const total = ref(0)
const status = ref()
const chunk_id = ref()
const chunkType = ref()
const bulk = ref(false)
const loading = ref(false)
const content = ref('')
const keyword = ref([])
const question = ref([])
const route = useRoute()
const pageSize = ref(10)
const filename = ref('')
const chunkDetail = ref()
const currentPage = ref(1)
const inputKeyword = ref()
const errorChunk = ref('')
const model = ref('expand')
const inputQuestion = ref()
const searchQuery = ref('')
const selectAll = ref(false)
const searchShow = ref(false)
const filterShow = ref(false)
const dialogVisible = ref(false)
const statusArr = ref<status[]>([])
const pageSizes = ref([5, 10, 20])
interface status {
 status: boolean
}
watch(() => selectAll.value, (val) => {
  if (val) {
    statusArr.value.forEach(item => {
      item.status = true
    })
  } else {
    statusArr.value.forEach(item => {
      item.status = false
    })
  }
})
watch(() => filter.value, (val) => {
  if (val === 'all') {
    getChunkDetail()
  } else if (val === 'enable') {
    getChunkDetail(1)
  } else if (val === 'disable') {
    getChunkDetail(0)
  }
})
watch(() => dialogVisible.value, (val) => {
  if (!val) {
    errorChunk.value = ''
  }
})
onMounted(async() => {
  const { data } = await get(`/v1/kb/get-mykb_role?kb_id=${route.query.id}`)
  if (data.code === 0 && data.message === 'success') {
    role.value = data.data.role
  }
  getKbDetail()
  getChunkDetail()
  addEventListener('click', handleGlobalClick)
})
onBeforeUnmount(() => {
  removeEventListener('click', handleGlobalClick)
})
const removeKb = (value: string) => {
  question.value = question.value.filter(user => user !== value)
}
const deleteChunk = async () => {
  ElMessageBox.confirm(t('knowledgeBases.dataset.deleteChunksTip'), '', {
    confirmButtonText: t('knowledgeBases.dataset.yes'),
    cancelButtonText: t('knowledgeBases.dataset.no'),
    type: 'warning'
  }).then(() => handleSelectionChange('delete'))
}
const handleGlobalClick = () => {
  bulk.value = false
  filterShow.value = false
}
interface KbDetail {
  id: string
  name: string
  avatar: string
}
const kbDetail = ref<KbDetail>({
  name: '',
  avatar: '',
  id: ''
})
interface ReqData {
  doc_id: string
  keywords: string
  page: number
  size: number
  available_int?: number
}
const handleClick = async (chunk: string) => {
  if (role.value === 'viewer') return
  dialogVisible.value = true
  chunkType.value = 'edit'
  const { data } = await get(`/v1/chunk/get?chunk_id=${chunk}`)
  if (data.code === 0 && data.message === 'success') {
    content.value = data.data.content_with_weight
    keyword.value = data.data.important_kwd
    question.value = data.data.question_kwd
    chunk_id.value = data.data.id
    status.value = data.data.available_int
  }
}
const searchClick = () => {
  searchShow.value = true
}
const clearInput = (val: string) => {
  setTimeout(() => {
    if (val === 'keyword') {
      inputKeyword.value.blur()
      setTimeout(() => {
        inputKeyword.value.focus()
      })
    } else {
      inputQuestion.value.blur()
      setTimeout(() => {
        inputQuestion.value.focus()
      })
    }
  })
}
const submitChunk = async (type: 'create' | 'set') => {
  if (!content.value) return
  if (content.value.trim() === '') {
    errorChunk.value = t('knowledgeBases.dataset.content')
    return
  }

  const reqData = {
    tag_feas: {},
    available_int: type === 'create' ? 1 : status.value,
    content_with_weight: content.value,
    important_kwd: keyword.value,
    question_kwd: question.value,
    doc_id: route.query.doc_id,
    chunk_id: chunk_id.value
  }

  const url = type === 'create' ? '/v1/chunk/create' : '/v1/chunk/set'
  const successMessage = type === 'create' ? t('knowledgeBases.dataset.created') : t('knowledgeBases.dataset.modifiedSuccess')

  const { data } = await post(url, reqData)
  if (data.code === 0 && data.message === 'success') {
    loading.value = true
    setTimeout(() => {
      getChunkDetail()
      loading.value = false
    }, 2000)
    dialogVisible.value = false
    ElMessage.success(successMessage)
  }
}
const getChunkDetail = async (available?: number) => {
  const reqData: ReqData = {
    doc_id: route.query.doc_id as string,
    keywords: searchQuery.value,
    page: currentPage.value,
    size: pageSize.value
  }
  statusArr.value = []
  chunkDetail.value = []
  if (available === 0 || available === 1) {
    reqData.available_int = available
  }
  const { data } = await post('/v1/chunk/list', reqData)
  if (data.code === 0 && data.message === 'success') {
    total.value = data.data.total
    chunkDetail.value = [...data.data.chunks]
    filename.value = data.data.doc.name
    chunkDetail.value.forEach(()=> {
      statusArr.value.push({ status: false })
    })
  }
}
const changeStatus = async (status: number, id: string) => {
  const { data } = await post('/v1/chunk/switch', {
    chunk_ids: [id],
    doc_id: route.query.doc_id,
    available_int: status
  })
  if (data.code === 0 && data.message === 'success') {
    ElMessage.success(t('knowledgeBases.dataset.modifiedSuccess'))
  }
}
// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getChunkDetail()
}
// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getChunkDetail()
}
const getKbDetail = async () => {
  const { data } = await get(`/v1/kb/detail?kb_id=${route.query.id}`)
  if (data.code === 0 && data.message === 'success') {
    kbDetail.value.name = data.data.name
    kbDetail.value.avatar = data.data.avatar
    kbDetail.value.id = data.data.parser_id
  }
}
const handleSelectionChange = async (type: 'enable' | 'disable' | 'delete') => {
  const chunk_ids = statusArr.value
    .map((item, index) => (item.status ? chunkDetail.value[index].chunk_id : null))
    .filter(Boolean) as string[]

  if (chunk_ids.length === 0) {
    ElMessage.warning(t('knowledgeBases.dataset.selectLeast'))
    return
  }

  let url = ''
  let payload: Record<string, any> = { chunk_ids, doc_id: route.query.doc_id }
  let successMsg = ''

  switch (type) {
    case 'enable':
      url = '/v1/chunk/switch'
      payload.available_int = 1
      successMsg = t('knowledgeBases.dataset.successful')
      break
    case 'disable':
      url = '/v1/chunk/switch'
      payload.available_int = 0
      successMsg = t('knowledgeBases.dataset.successful')
      break
    case 'delete':
      url = '/v1/chunk/rm'
      successMsg = t('knowledgeBases.dataset.deleteSuccess')
      break
  }

  const { data } = await post(url, payload)
  if (data.code === 0 && data.message === 'success') {
    ElMessage.success(successMsg)
    loading.value = true
    setTimeout(() => {
      getChunkDetail()
      loading.value = false
    }, 2000)
  }
  bulk.value = false
  selectAll.value = false
}
</script>

<style lang="scss" scoped>
:deep(.el-select) .el-select__wrapper .el-select__selection {
  overflow: auto;
  max-height: 180px;
}
p, h2 {
  font-family: 'Encode Sans' !important;
  span {
    font-family: 'Encode Sans' !important;
    strong { 
      font-weight: 600 !important;
      span {
        font-family: 'Encode Sans' !important;
      }
    }
  }
  strong {
    font-weight: 600 !important;
  }
}
:deep(.cursor-pointer ){
  table, th, td {
    padding: 2px;
    word-break: break-all;
    border: 1px solid #ccc;
  }
  table {
    caption {
      font-weight: 600;
    }
  }
}
li {
  list-style: inherit;
}
code, pre {
  white-space: pre-wrap;
}
:deep(.svg) {
  rect.borders {
    stroke: transparent;
  }
  rect.bg {
    fill: transparent;
  }
}
.transitions {
  transform: rotateZ(-90deg);
}
.collapse {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  text-overflow: ellipsis;
}
:deep(.el-pagination) {
  margin-top: 24px;
  margin-bottom: 0;
}
.privacy-box-active {
  min-width: 20px;
  max-width: 20px;
  min-height: 20px;
  max-height: 20px;
  color: #1460F3;
  svg {
    width: 100%;
    height: 100%;
  }
}
.privacy-box {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  border-radius: 3.3px;
  display: inline-block;
  border: 2px solid #AAAEB2;
}
</style>
