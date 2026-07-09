<template>
  <div class="bg-[#F3F6FF] min-h-100vh w-full">
    <KbHeader />
    <div class="flex min-h-[calc(100vh-88px)] pt-88px">
      <div v-if="kbList.length" class="bg-white py-32px px-20px border-r border-[#E1E3E8] h-[calc(100vh-88px)] fixed z-10">
        <div v-if="!assistantList.length" class="border-gradient mb-20px rounded-10px">
          <div :class="kbList.length ? 'cursor-pointer' : 'opacity-50 cursor-not-allowed'" @click="handleCreate" class="gradient py-6px px-8px text-sm font-500 text-white flex items-center rounded-8px whitespace-nowrap">
            <img src="/images/add-robot.png" width="32" height="32" class="mr-8px" />
            {{ t('knowledgeBases.chat.create') }}
          </div>
        </div>
        <!-- 助手列表 -->
        <div v-else class="w-full relative">
          <div @click.stop="assistantListShow = !assistantListShow, handleClose()" class="border-gradient relative cursor-pointer py-6px px-8px text-sm font-500 text-white flex items-center justify-between rounded-8px mb-20px whitespace-nowrap">
            <div class="flex items-center text-brand-0 text-sm font-500">
              <img v-if="selectAssistant?.icon" :src="selectAssistant.icon" alt="avatar" class="w-32px h-32px rounded-1/2 mr-8px">
              <div v-else class="w-32px h-32px rounded-1/2 mr-12px py-4px px-8px text-sm text-white font-600 flex justify-center items-center gradients">
                {{ selectAssistant?.name.slice(0, 1).toUpperCase() }}
              </div>
              <div class="truncate max-w-140px">{{ selectAssistant?.name }}</div>
            </div>
            <PullDown class="cursor-pointer" />
          </div>
          <div v-show="assistantListShow" class="absolute z-2 list-shadow top-48px left-[-18px] w-252px px-14px py-10px rounded-4px bg-white">
            <div class="border-gradient mb-20px rounded-10px">
              <div @click.stop.prevent="handleCreate" class="gradient cursor-pointer py-6px px-8px text-sm font-500 text-white flex items-center rounded-8px whitespace-nowrap">
                <img src="/images/add-robot.png" width="32" height="32" class="mr-8px" />
                {{ t('knowledgeBases.chat.create') }}
              </div>
            </div>
            <div class="h-1px w-full bg-[#E1E3E8] mt-12px"></div>
            <a :href="`/chat?dialog_id=${assistant.id}`" v-for="(assistant, index) in assistantList" :key="assistant.id" :class="statusArr[index].status && 'bg-[#1460F31A]'" class="relative rounded-6px cursor-pointer flex items-center px-8px py-2px justify-between mt-12px">
              <div class="flex items-center">
                <img v-if="assistant.icon" :src="assistant.icon" alt="avatar" class="w-32px h-32px mr-12px rounded-1/2">
                <div v-else class="gradients text-white min-w-32px h-32px rounded-1/2 mr-12px py-4px px-8px text-sm font-600 flex justify-center items-center">
                  {{ assistant.name.slice(0, 1).toUpperCase() }}
                </div>
                <div class="flex flex-col truncate max-w-140px w-full">
                  <div :class="statusArr[index].status && '!text-brand-2'" class="text-xs text-[#18191B] truncate">{{ assistant.name }}</div>
                  <div class="text-brand-1 text-12px leading-16px truncate">{{ assistant.description }}</div>
                </div>
              </div>
              <div @click.stop.prevent="changeStatus(index)" class="flex flex-col cursor-pointer px-12px h-36px items-center justify-center">
                <div class="w-[3.2px] h-[3.2px] rounded-1/2 bg-brand-1 mb-[4.8px]"></div>
                <div class="w-[3.2px] h-[3.2px] rounded-1/2 bg-brand-1 mb-[4.8px]"></div>
                <div class="w-[3.2px] h-[3.2px] rounded-1/2 bg-brand-1"></div>
              </div>
              <div v-show="statusArr[index].status" class="assistant-shadow absolute top-32px right-[-62px] bg-white z-3 p-4px rounded-4px">
                <div @click.stop.prevent="getDialogInfo(assistant.id)" class="py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                  {{ t('knowledgeBases.chat.edit') }}
                </div>
                <div @click.stop.prevent="deleteAssistant(assistant.id)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                  {{ t('knowledgeBases.chat.delete') }}
                </div>
              </div>
            </a>
          </div>
        </div>
        <!-- Chat列表 -->
        <div class="relative min-w-218px">
          <div class="flex justify-between items-center border-b border-[#E1E3E8] pb-12px">
            <div class="flex items-center py-4px">
              <KbChat />
              <div class="mx-4px text-brand-0 text-xs">
                {{ t('knowledgeBases.chat.chat') }}
              </div>
              <div class="bg-[#EBF1FE] rounded-8px py-2px px-8px text-xs text-brand-3">
                {{ chatList.length }}
              </div>
            </div>
            <AddChat @click="addChat" :class="assistantList.length && chatList[0]?.name !== t('knowledgeBases.chat.new') && !disable ? 'cursor-pointer text-brand-2' : 'text-[#CDDBFF] cursor-not-allowed'" />
          </div>
          <div @click="changeChat(chat.id, chat.name)" v-for="(chat, index) in chatList" :key="index" :class="conversionId === chat.id && 'bg-[#1460F31A] text-brand-2'" class="flex items-center justify-between relative text-brand-0 text-xs py-8px px-12px mt-12px hover:(bg-[#1460F31A] text-brand-2) cursor-pointer rounded-6px"> 
            <div class="w-150px truncate">{{ chat.name }}</div>
            <div v-show="chatList[index]?.name !== t('knowledgeBases.chat.new')" @click.stop.prevent="changeChatStatus(index)" class="flex flex-col w-12px h-20px items-center justify-center">
              <div class="w-2px h-2px rounded-1/2 bg-brand-1 mb-3px"></div>
              <div class="w-2px h-2px rounded-1/2 bg-brand-1 mb-3px"></div>
              <div class="w-2px h-2px rounded-1/2 bg-brand-1"></div>
            </div>
            <div v-show="chatStatusArr[index].status" class="assistant-shadow absolute top-32px right-[-62px] bg-white z-3 p-4px rounded-4px">
              <div @click.stop.prevent="deleteChat(chat.id)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                {{ t('knowledgeBases.chat.delete') }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!kbList.length" class="w-full h-[calc(100vh-88px)] flex flex-col justify-center items-center pl-220px">
        <img src="/images/kbEmpty.png" width="120" height="120" alt="Empty">
        <div class="text-sm text-brand-3 mt-8px mb-32px max-w-600px text-center">
          {{ t('knowledgeBases.chat.desc') }}
        </div>
        <a href="/knowledge-base?create=true" @click="dialogVisible = true" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px w-fit flex items-center justify-center font-500 hover:bg-[#244FF0]">
          <KbKnowledge class="text-white mr-10px" />
          {{ t('knowledgeBases.chat.btn') }}
        </a>
      </div>
      <div v-else class="w-full relative px-32px mb-190px pb-24px overflow-auto pl-292px h-[calc(100vh-276px)]" ref="scrollContainer">
        <div v-if="!assistantList.length" class="m-auto min-h-full pb-190px flex flex-col justify-center items-center">
          <img src="/images/add-robot.png" class="w-120px h-120px mx-auto" />
          <div class="mt-8px mb-32px text-sm text-brand-3 text-center">
            {{ t('knowledgeBases.chat.createDesc') }}
          </div>
          <div @click="dialogVisible = true" class="gradient w-200px cursor-pointer py-6px px-8px text-sm font-500 text-white flex items-center justify-center rounded-8px mx-auto">
            {{ t('knowledgeBases.chat.create') }}
          </div>
        </div>
        <!-- 对话列表 -->
        <template v-if="messageList?.length">
          <div v-for="(message, index) in messageList" :key="index" class="flex flex-col mt-32px">
            <template v-if="message.role === 'user'">
              <div class="flex items-start flex-row-reverse">
                <img v-if="userAvatar" :src="userAvatar" alt="avatar" class="w-40px h-40px rounded-1/2 overflow-hidden">
                <div v-else class="bg-[#FFE248] rounded-1/2 w-40px h-40px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
                  {{ user?.username.slice(0, 1).toUpperCase() }}
                </div>
                <div class="bg-white w-[calc(100%-112px)] rounded-8px mr-16px py-20px px-16px text-sm text-brand-0 flex justify-end">
                  {{ message.content }}
                </div>
              </div>
              <div class="pr-56px mt-8px flex justify-end">
                <div class="p-4px"></div>
                <el-tooltip popper-class="box-item" :content="t('knowledgeBases.chat.copy')" placement="top">
                  <CopyChat @click="clipboard(message.content, { format: 'text/plain' }), ElMessage.success(t('knowledgeBases.chat.copied'))" class="mr-8px cursor-pointer" />
                </el-tooltip>
                <el-tooltip popper-class="box-item" :content="t('knowledgeBases.chat.retry')" placement="top">
                  <RefreshChat @click="startSSE(true)" class="cursor-pointer" />
                </el-tooltip>
              </div>
            </template>
            <template v-else>
              <div class="flex items-start">
                <img v-if="selectAssistant?.icon" :src="selectAssistant?.icon" alt="avatar" class="w-40px h-40px rounded-1/2 overflow-hidden">
                <div v-else class="gradient rounded-1/2 w-40px h-40px py-4px px-8px text-sm text-white font-600 flex justify-center items-center">
                  {{ selectAssistant?.name.slice(0, 1).toUpperCase() }}
                </div>
                <div class="bg-white w-[calc(100%-112px)] rounded-8px ml-16px py-20px px-16px text-sm text-brand-0" v-html="renderedContent(message.content)"></div>
              </div>
              <template v-if="referenceIndexMap[index] !== undefined && reference?.[referenceIndexMap[index]]?.doc_aggs?.length">
                <div
                  class="bg-white ml-56px w-[calc(100%-112px)] rounded-8px py-4px px-16px mt-8px"
                  v-for="(references, chiIndex) in reference[referenceIndexMap[index]]?.doc_aggs"
                  :key="chiIndex"
                >
                  {{ references.doc_name }}
                </div>
              </template>
              <div v-if="index && showCopy" class="pl-56px mt-8px flex">
                <el-tooltip popper-class="box-item" :content="t('knowledgeBases.chat.copy')" placement="top">
                  <CopyChat @click="clipboard(message.content, { format: 'text/plain' })" class="mr-8px cursor-pointer" />
                </el-tooltip>
                <el-tooltip popper-class="box-item" :content="t('knowledgeBases.chat.retry')" placement="top">
                  <RefreshChat @click="startSSE(true)" class="cursor-pointer" />
                </el-tooltip>
              </div>
            </template>
          </div>
          <div v-if="loading" class="flex items-start mt-32px">
            <img v-if="selectAssistant?.icon" :src="selectAssistant?.icon" alt="avatar" class="w-40px h-40px rounded-1/2 overflow-hidden">
            <div v-else class="gradient rounded-1/2 w-40px h-40px py-4px px-8px text-sm text-white font-600 flex justify-center items-center">
              {{ selectAssistant?.name.slice(0, 1).toUpperCase() }}
            </div>
            <div class="bg-white w-[calc(100%-112px)] rounded-8px ml-16px py-20px px-16px text-sm text-brand-2 flex">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 384" class="loader mr-20px">
                <circle r="176" cy="192" cx="192" stroke-width="24" fill="transparent" pathLength="360" class="active"></circle>
                <circle r="176" cy="192" cx="192" stroke-width="24" fill="transparent" pathLength="360" class="track"></circle>
              </svg>
              {{ t('knowledgeBases.chat.search') }}...
            </div>
          </div>
        </template>
        <div class="fixed bottom-0 right-0 w-[calc(100%-330px)] bg-white px-32px py-20px">
          <el-input :disabled="loading || !assistantList.length || disable" :placeholder="t('knowledgeBases.chat.start')" type="textarea" @keyup.enter="sendQuestion" :rows="4" v-model="question"></el-input>
          <div @click="sendQuestion" v-show="!loading" :class="question ? 'bg-[#396FFA] hover:bg-[#244FF0] cursor-pointer' : 'bg-[#396FFA] opacity-50 cursor-not-allowed'" class="ml-auto mt-12px w-140px rounded-6px text-white text-sm p-10px flex items-center justify-center font-500">
            {{ t('knowledgeBases.chat.send') }}
          </div>
          <div @click="pauseSSE" v-show="loading" class="ml-auto mt-12px w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px mr-20px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)">
            <Stop class="mr-10px" />
            {{ t('knowledgeBases.chat.stop') }}
          </div>
        </div>
      </div>
    </div>
    <!-- 创建/编辑助理弹窗 -->
    <el-dialog v-model="dialogVisible" align-center width="520px">
      <h3 class="fixed text-sm font-bold text-[#43474D] py-4px pb-28px bg-white z-3 w-492px pt-16px mt-[-12px]">
        {{ t('knowledgeBases.chat.setting') }}
      </h3>
      <div ref="dialogRef" class="h-full overflow-y-auto overflow-x-hidden relative pb-66px">
        <div class="flex text-xs text-brand-0 font-600 mb-8px pt-64px">
          <span class="text-[#FF5050] inline-block mr-4px font">*</span>
          {{ t('knowledgeBases.chat.name') }}
        </div>
        <el-input v-model="assistantName" @input="errorAssistantName = ''" :placeholder="t('knowledgeBases.chat.namePlaceholder')"></el-input>
        <div v-show="errorAssistantName" class="text-[#f56c6c] text-12px leading-12px mt-2px">
          {{ errorAssistantName }}
        </div>
        <div class="flex text-xs text-brand-0 font-600 mb-8px mt-20px">
          {{ t('knowledgeBases.chat.description') }}
        </div>
        <el-input v-model="description" :placeholder="t('knowledgeBases.chat.descriptionPlaceholder')"></el-input>
        <div class="flex text-xs text-brand-0 font-600 mb-8px mt-20px">
          {{ t('knowledgeBases.chat.avatar') }}
        </div>
        <div class="flex">
          <img v-show="avatar" :src="avatar" alt="avatar" width="64" height="64" class="mr-12px rounded-4px">
          <input ref="input" class="hidden" type="file" name="file" accept=".png, .jpg, .jpeg" @change="handleChange">
          <div @click="input.value = '', input.click()" class="relative bg-[#F3F6FF] w-64px h-64px border-[#CED6E1] border-dashed border flex justify-center items-center flex-col rounded-4px cursor-pointer text-[#52555F] rounded-4px text-12px leading-16px">
            <Add class="mb-2px" />
            {{ t('knowledgeBases.chat.upload') }}
          </div>
        </div>
        <div class="flex text-xs text-brand-0 font-600 mb-8px mt-20px">
          {{ t('knowledgeBases.chat.emptyResponse') }}
          <el-tooltip popper-class="box-item" class="ml-4px" :content="t('knowledgeBases.chat.emptyResponseDesc')" placement="top">
            <Tip class="ml-4px" />
          </el-tooltip>
        </div>
        <el-input v-model="response" @input="errorResponse = ''" :placeholder="t('knowledgeBases.chat.emptyResponsePlaceholder')"></el-input>
        <div v-show="errorResponse" class="text-[#f56c6c] text-12px leading-12px mt-2px">
          {{ errorResponse }}
        </div>
        <div class="flex text-xs text-brand-0 font-600 mb-8px mt-20px">
          {{ t('knowledgeBases.chat.openingGreeting') }}
          <el-tooltip popper-class="box-item" :content="t('knowledgeBases.chat.openingGreetingDesc')" placement="top">
            <Tip class="ml-4px" />
          </el-tooltip>
        </div>
        <div v-show="errorGreeting" class="text-[#f56c6c] text-12px leading-12px mt-2px">
          {{ errorGreeting }}
        </div>
        <el-input v-model="greeting" @input="errorGreeting = ''" :placeholder="t('knowledgeBases.chat.openingGreetingPlaceholder')"></el-input>
        <div class="flex justify-between items-center mb-8px mt-20px">
          <div class="flex text-xs text-brand-0 font-600">
            {{ t('knowledgeBases.chat.quote') }}
            <el-tooltip popper-class="box-item" :content="t('knowledgeBases.chat.quoteDesc')" placement="top">
              <Tip class="ml-4px" />
            </el-tooltip>
          </div>
          <el-switch v-model="quote" style="--el-switch-on-color: #396FFA; --el-switch-off-color: #CED6E1"
            :active-value="true" :inactive-value="false">
          </el-switch>
        </div>
        <div class="flex text-xs text-brand-0 font-600 mb-8px mt-20px">
          <span class="text-[#FF5050] inline-block mr-4px font">*</span>
          {{ t('knowledgeBases.chat.knowledgeBases') }}
          <el-tooltip popper-class="box-item" :content="t('knowledgeBases.chat.knowledgeBasesDesc')" placement="top">
            <Tip class="ml-4px" />
          </el-tooltip>
        </div>
        <el-select
          v-model="selectedKb"
          :placeholder="t('knowledgeBases.chat.knowledgeBasesPlaceholder')"
          filterable
          remote
          multiple
          @change="errorKb = ''"
          class="kbList"
          popper-class="kbListPopper"
          @focus="handleFilterKb"
          collapse-tags
          clearable
          value-key="id">
          <el-option
            v-for="kb in kbList"
            :key="kb.id"
            :label="kb.name"
            :value="kb"
          >
            <div class="flex items-center">
              <img v-if="kb.avatar" :src="kb.avatar" alt="avatar" class="w-32px h-32px mr-12px">
              <div v-else class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">{{ kb?.name?.slice(0, 1).toUpperCase() }}</div>
              <div class="flex flex-col truncate">
                <div class="text-[#18191B] text-14px leading-20px font-600 truncate">{{ kb.name }}</div>
                <div class="text-[#94969D] text-12px leading-16px truncate">{{ kb.description }}</div>
              </div>
            </div>
          </el-option>
          <template v-if="selectedKb.length" #tag>
            <el-tag class="custom" closable @close="removeKb(kb.id)" v-for="(kb, index) in selectedKb" :key="index">
              <div class="flex items-center">
                <div class="text-brand-0 text-12px leading-16px">{{ kb.name }}</div>
              </div>
            </el-tag>
          </template>
        </el-select>
        <div v-show="errorKb" class="text-[#f56c6c] text-12px leading-12px mt-2px">
          {{ errorKb }}
        </div>
      </div>
      <div class="flex shadows absolute bottom-0 w-full ml-[-14px] rounded-b-l-4px flex justify-center bg-white py-12px">
        <div @click="dialogVisible = false" class="w-140px rounded-6px cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)">
          {{ t('knowledgeBases.chat.cancel') }}
        </div>
        <div @click="createOrSetAssistant()" class="w-140px rounded-6px cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center ml-12px justify-center font-500 hover:bg-[#244FF0]">
          {{ t('knowledgeBases.chat.save') }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { v4 as uuid } from 'uuid'
import { useI18n } from 'vue-i18n'
import markdownit from 'markdown-it'
import clipboard from 'copy-to-clipboard'
import { onMounted, ref, onBeforeUnmount, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type UploadFile } from 'element-plus'
import { get, post } from '../utils/request'
import { useRoute, useRouter } from 'vue-router'
import { useCookies } from "vue3-cookies"
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import AddChat from '../components/images/AddChat.vue'
import { createParser, type ParsedEvent, type ReconnectInterval } from 'eventsource-parser'
import Stop from '../components/images/Stop.vue'
import CopyChat from '../components/images/CopyChat.vue'
import { sanitizeHtml } from '../utils/sanitizeHtml'

interface KbList {
  name?: string
  id: string
  avatar?: string
  role?: string
  description?: string
  chunk_num: number
}
interface Assistant {
  name: string
  id: string
  icon: string
  description: string
}
interface Status {
  status: boolean
}
interface ChatList {
  id: string
  name: string
}
const md = markdownit('default', {
  html: false
})
const { t } = useI18n()
const { cookies } = useCookies()
const route = useRoute()
const router = useRouter()
const input = ref()
const quote = ref(false)
const showCopy = ref(true)
const disable = ref(false)
const avatar = ref('')
const editDialogID = ref('')
const loading = ref(false)
const dialogRef = ref<HTMLElement>()
const scrollContainer = ref<HTMLElement>()
const userAvatar = ref(localStorage.getItem('avatar'))
const errorKb = ref('')
const question = ref('')
const errorResponse = ref('')
const errorGreeting = ref('')
const greeting = ref(t('knowledgeBases.chat.hi'))
const system = ref('')
const response = ref('')
const reference = ref()
const messageList = ref<any[]>([])
const description = ref('')
const conversionId = ref('')
const selectAssistant = ref()
const assistantName = ref('')
const dialogVisible = ref(false)
const kbList = ref<KbList[]>([])
const errorAssistantName = ref('')
const statusArr = ref<Status[]>([])
const chatStatusArr = ref<Status[]>([])
const chatList = ref<ChatList[]>([])
const type = ref<'create' | 'set'>()
const assistantListShow = ref(false)
const selectedKb = ref<KbList[]>([])
const assistantList = ref<Assistant[]>([])
const user = ref((cookies.get('idp_user') || JSON.parse(sessionStorage.getItem('idp_user') as string)))

const checkDisable = async (dialogId: string) => {
  const { data } = await get(`/v1/dialog/get?dialog_id=${dialogId}`)
  disable.value = data.code === 0 && data.message === 'success' && !data.data.kb_ids.length
}

// Maps each messageList index to its reference index (for assistant replies after the prologue).
// The prologue is the first assistant message (index 0); it has no reference.
// Subsequent assistant messages map to reference[0], reference[1], etc.
const referenceIndexMap = computed(() => {
  const map: Record<number, number> = {}
  let refIdx = 0
  let isPrologue = true
  messageList.value.forEach((msg, i) => {
    if (msg.role === 'assistant') {
      if (isPrologue) {
        isPrologue = false // skip prologue
      } else {
        map[i] = refIdx++
      }
    }
  })
  return map
})

onMounted(async () => {
  const { data: res } = await get('/v1/kb/list')
  if (res.code === 0 && res.message === 'success' && res.data.kbs?.length) {
    kbList.value = res.data.kbs.filter((item: KbList) => item.chunk_num > 0)
    await getAssistantList()
    if (assistantList.value.length) {
      setRoute('dialog_id', selectAssistant.value.id)
    }
  }
  jump()
  addEventListener('click', handleClick)
})
const renderedContent = (val: string) => {
  const htmlWithImg = replaceRefsWithImages(val)
  return sanitizeHtml(md.render(htmlWithImg))
}

function replaceRefsWithImages(text: string): string {
  return text.replace(/##(\d+)\$\$/g, (match, p1) => {
    return `<img src="/images/question.svg" alt="svg" class="inline pb-2px hidden">`
  })
}
onBeforeUnmount(() => {
  removeEventListener('click', handleClick)
})
const getDialogInfo = async (id: string) => {
  dialogVisible.value = true
  type.value = 'set'
  editDialogID.value = id
  const { data } = await get(`/v1/dialog/get?dialog_id=${id}`)
  if (data.code === 0 && data.message === 'success') {
    assistantName.value = data.data.name
    description.value = data.data.description
    avatar.value = data.data.icon
    response.value = data.data.prompt_config.empty_response
    greeting.value = data.data.prompt_config.prologue
    system.value = data.data.prompt_config.system
    quote.value = data.data.prompt_config.quote
    selectedKb.value = data.data.kb_ids.map((id: string, index: number) => ({
      id: id,
      name: data.data.kb_names[index]
    }))
  }
}
const changeChatStatus = async (index: number) => {
  chatStatusArr.value.forEach((status: Status) => {
    status.status = false
  })
  chatStatusArr.value[index].status = !chatStatusArr.value[index].status
}
const changeStatus = async (index: number) => {
  statusArr.value.forEach((status: Status) => {
    status.status = false
  })
  statusArr.value[index].status = !statusArr.value[index].status
}
const setRoute = (key: string, val: string) => {
  const params = new URLSearchParams(route.query as Record<string, string>)
  params.set(key, val)
  router.replace({
    path: route.path,
    // @ts-ignore
    query: Object.fromEntries(params.entries())
  })
}
const handleFilterKb = async () => {
  const { data } = await get('/v1/kb/list')
  if (data.code === 0 && data.message === 'success') {
    kbList.value = data.data.kbs.filter((item: KbList) => item.chunk_num > 0)
  }
}
const deleteAssistant = (id: string) => {
  ElMessageBox.confirm(t('knowledgeBases.chat.deleteTip'), '', {
    confirmButtonText: t('knowledgeBases.chat.yes'),
    cancelButtonText: t('knowledgeBases.chat.no'),
    type: 'warning'
  }).then(async () => {
    const { data } = await post('/v1/dialog/rm', {
      dialog_ids: [id]
    })
    if (data.code === 0 && data.message === 'success') {
      const { dialog_id, ...restQuery } = route.query
      router.replace({ path: route.path, query: restQuery })
      ElMessage.success(t('knowledgeBases.chat.deleteSuccess'))
      getAssistantList(true)
    } else {
      ElMessage.error(t('knowledgeBases.chat.deleteFail'))
    }
  })
}
const deleteChat = async (id: string) => {
  const { data } = await post('/v1/conversation/rm', {
    conversation_ids: [id],
    dialog_id: route.query.dialog_id as string
  })
  if (data.code === 0 && data.message === 'success') {
    ElMessage.success(t('knowledgeBases.chat.deleteSuccess'))
    await getChatList(true)
    if (!chatList.value.length) {
      conversionId.value = ''
      messageList.value = []
    } else if (id === conversionId.value) {
      if (chatList.value.length) {
        setRoute('conversion_id', chatList.value[0].id)
        conversionId.value = chatList.value[0].id
        getConversion()
      } else {
        const { conversion_id, ...restQuery } = route.query
        router.replace({ path: route.path, query: restQuery })
      }
    }
  } else {
    ElMessage.error(t('knowledgeBases.chat.deleteFail'))
  }
}
// 切换对话
const changeChat = (conversion_id: string, name: string) => {
  setRoute('conversion_id', conversion_id)
  conversionId.value = conversion_id
  if (name !== t('knowledgeBases.chat.new')) {
    getConversion()
  } else {
    messageList.value = []
    messageList.value.push({
      role: 'assistant',
      content: selectAssistant.value.prompt_config.prologue,
      id: uuid()
    })
  }
}
const handleCreate = () => {
  if (kbList.value.length) {
    assistantName.value = ''
    description.value = ''
    avatar.value = ''
    response.value = ''
    quote.value = false
    selectedKb.value = []
    dialogVisible.value = true
    type.value = 'create'
  }
}
const addChat = () => {
  if (!kbList.value.length || !assistantList.value.length || chatList.value[0]?.name === t('knowledgeBases.chat.new') || disable.value) return
  const conversion_id = uuid().replace(/-/g, '')
  if (chatList.value[0]?.name !== t('knowledgeBases.chat.new')) {
    chatList.value.unshift({
      name: t('knowledgeBases.chat.new'),
      id: conversion_id
    })
    chatStatusArr.value.unshift({
      status: false
    })
    messageList.value = []
    messageList.value.push({
      role: 'assistant',
      content: selectAssistant.value.prompt_config.prologue,
      id: uuid()
    })
  }
  setRoute('conversion_id', conversion_id)
  conversionId.value = conversion_id
}
const sendQuestion = async () => {
  const cleanedQuestion = question.value.replace(/\n/g, '') // 提前处理

  // 如果没有问题，直接返回
  if (!cleanedQuestion) return
  jump()

  // 处理新聊天：持久化到后端
  if (chatList.value[0]?.name === t('knowledgeBases.chat.new')) {
    const { data } = await post('/v1/conversation/set', {
      conversation_id: conversionId.value,
      dialog_id: selectAssistant.value.id,
      is_new: true,
      message: [{
        role: 'user',
        content: cleanedQuestion
      }],
      name: cleanedQuestion
    })
    if (data.code !== 0 || data.message !== 'success') return
  }

  // 更新消息列表
  messageList.value.push({
    role: 'user',
    content: cleanedQuestion,
    id: uuid()
  })

  // 启动 SSE
  startSSE()
}

interface RequestBody {
  conversation_id?: string
  messages: any
  doc_ids?: any
  role?: string
}
let controller: AbortController | null = null
const pauseSSE = () => {
  if (controller) {
    controller.abort()
    controller = null
    loading.value = false
    // Remove the incomplete assistant message left by startSSE
    const last = messageList.value[messageList.value.length - 1]
    if (last && last.role === 'assistant' && !last.content) {
      messageList.value.pop()
    }
    showCopy.value = true
  }
}
const startSSE = async (re?: boolean) => {
  if (loading.value || disable.value) return
  controller = new AbortController()

  if (re) {
    messageList.value.pop()
  }
  const body: RequestBody = {
    conversation_id: conversionId.value,
    messages: messageList.value
  }

  try {
    question.value = ''
    loading.value = true
    showCopy.value = false
    const response = await fetch('/v1/conversation/completion', {
      method: 'POST',
      headers: {
        Authorization: user.value.token,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
      signal: controller.signal,
    })

    if (!response.ok || !response.body) {
      throw new Error('Network error or empty response body')
    }

    let fullAnswer = ''
    messageList.value.push({
      role: 'assistant',
      content: fullAnswer,
      id: uuid()
    })
    jump()
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    const parser = createParser({
      onEvent(event: ParsedEvent | ReconnectInterval) {
        try {
          const json = JSON.parse(event.data)
          const content = json?.data?.answer || ''
          if (content.length > messageList.value[messageList.value.length - 1].content.length) {
            messageList.value[messageList.value.length - 1].content = content
          }
        } catch (e) {
          console.warn('Failed to parse JSON:', e)
        }
      }
    })

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      parser.feed(chunk)
    }
  } catch (error) {
    console.info('SSE error:', error)
  } finally {
    loading.value = false
    setTimeout(() => {
      getChatList(true)
    }, 1000)
    controller = null
    showCopy.value = true
  }
}
const jump = () => {
  nextTick(() => {
    setTimeout(() => {
      if (scrollContainer.value) {
        scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
      }
    }, 200)
  })
}
const handleChange = async (e: any) => {
  const files = e.target.files
  if (!files) return
  avatar.value = await getBase64FromUploadFileList(files)
}
const getAssistantList = async (preserveConversation = false) => {
  const { data } = await get('/v1/dialog/list')
  assistantList.value = data.data
  statusArr.value = []
  assistantList.value.forEach(()=> {
    statusArr.value.push({ status: false })
  })
  if (route.query.dialog_id) {
    assistantList.value.forEach(item => {
      if (item.id === route.query.dialog_id) {
        selectAssistant.value = item
      }
    })
  }
  if (!selectAssistant.value && assistantList.value.length) {
    setRoute('dialog_id', assistantList.value[0].id)
    selectAssistant.value = assistantList.value[0]
  }
  if (selectAssistant.value) {
    await checkDisable(selectAssistant.value.id)
  }
  if (assistantList.value.length) {
    await getChatList(preserveConversation)
  } else {
    chatList.value = []
  }
}
const getChatList = async (preserveConversation = false) => {
  const { data } = await get('/v1/conversation/list?dialog_id=' + selectAssistant.value.id)
  chatStatusArr.value = []
  chatList.value = data.data
  chatList.value.forEach(() => {
    chatStatusArr.value.push({
      status: false
    })
  })
  if (data.data.length && !preserveConversation) {
    const targetId = (route.query.conversion_id as string) || data.data[0].id
    conversionId.value = targetId
    setRoute('conversion_id', targetId)
    getConversion()
  }
}
const getConversion = async () => {
  const { data } = await get('/v1/conversation/get?conversation_id=' + conversionId.value)
  messageList.value = data.data.message
  reference.value = data.data.reference
}
const handleClose = () => {
  chatStatusArr.value.forEach(item => {
    item.status = false
  })
}
const handleClick = () => {
  assistantListShow.value = false
  statusArr.value.forEach(item => {
    item.status = false
  })
  chatStatusArr.value.forEach(item => {
    item.status = false
  })
}
const getBase64FromUploadFileList = async (fileList: UploadFile[]) => {
  if (fileList.length > 0) {
    const file = fileList[0]
    const base64 = await transformFile2Base64(file)
    return base64
  }
  return ''
}
const transformFile2Base64 = (val: any): Promise<any> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(val)
    reader.onload = (): void => {
      // Create image object
      const img = new Image()
      img.src = reader.result as string
      img.onload = () => {
        // Create canvas
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        // Calculate compressed dimensions, set max width/height to 800px
        let width = img.width
        let height = img.height
        const maxSize = 100
        if (width > height && width > maxSize) {
          height = (height * maxSize) / width
          width = maxSize
        } else if (height > maxSize) {
          width = (width * maxSize) / height
          height = maxSize
        }
        // Set canvas dimensions
        canvas.width = width
        canvas.height = height
        // Draw image
        ctx?.drawImage(img, 0, 0, width, height)
        // Convert to base64, maintain original format and transparency
        const compressedBase64 = canvas.toDataURL('image/png')
        resolve(compressedBase64);
      }
      img.onerror = reject
    }
    reader.onerror = reject
  })
}
// 创建或修改知识库
const createOrSetAssistant = async () => {
  let i = 0
  if (assistantName.value === '') {
    errorAssistantName.value = t('knowledgeBases.chat.namePlaceholder')
    i++
  } else if (assistantName.value.length > 30) {
    errorAssistantName.value = t('knowledgeBases.chat.must')
    i++
  }
  if (selectedKb.value.length === 0) {
    errorKb.value = t('knowledgeBases.chat.knowledgeBasesPlaceholder')
    setTimeout(() => {
      dialogRef.value?.scrollTo({
        top: dialogRef.value.scrollHeight,
        behavior: 'smooth'
      })
    }, 100)
    i++
  }
  if (response.value.length > 200) {
    errorResponse.value = t('knowledgeBases.chat.emptyResponseMust')
    i++
  }
  if (greeting.value.length > 200) {
    errorGreeting.value = t('knowledgeBases.chat.openingGreetingMust')
    i++
  }
  if (i > 0) return
  let kb_ids: string[] = []
  selectedKb.value.forEach(item => {
    kb_ids.push(item.id)
  })
  const reqData: any = {
    icon: avatar.value,
    description: description.value,
    kb_ids: kb_ids,
    language: 'English',
    llm_id: 'qwen2.5:latest@Ollama',
    llm_setting: {
      frequency_penalty: 0.7,
      presence_penalty: 0.4,
      temperature: 0.1,
      top_p: 0.3
    },
    name: assistantName.value,
    prompt_config: {
      empty_response: response.value,
      keyword: false,
      parameters: [{
        key: 'knowledge', optional: 'false'
      }],
      prologue: greeting.value ? greeting.value : t('knowledgeBases.chat.hi'),
      quote: quote.value,
      reasoning: false,
      refine_multiturn: false,
      system: system.value ? system.value : t('knowledgeBases.chat.system'),
      tts: false,
      use_kg: false,
      rerank_id: ''
    },
    similarity_threshold: 0.2,
    top_n: 8,
    vector_similarity_weight: 0.3
  }
  if (type.value === 'set') {
    reqData.dialog_id = editDialogID.value
  }
  if (loading.value) return
  loading.value = true
  const { data } = await post('/v1/dialog/set', reqData)
  loading.value = false
  if (data.code === 0 && data.message === 'success') {
    dialogVisible.value = false
    if (type.value === 'create') {
      setRoute('dialog_id', data.data.id)
      selectAssistant.value = data.data
      setTimeout(() => {
        const { conversion_id, ...restQuery } = route.query
        router.replace({ path: route.path, query: restQuery })
      })
      ElMessage.success(t('knowledgeBases.chat.createSuccess'))
    } else {
      ElMessage.success(t('knowledgeBases.chat.modified'))
    }
    getAssistantList()
  }
}
const removeKb = async (val: string) => {
  selectedKb.value = selectedKb.value.filter(user => user.id !== val)
}
</script>

<style lang="scss" scoped>
.loader {
  width: 24px;
  --duration: 8s;
  overflow: visible;
  --track: white;
  --active: #396FFA;
  transform-origin: center;
  transform: rotate(-90deg);
  animation: spin 2s linear infinite;
}

@keyframes spin {
  0% {
    rotate: 0deg;
  }

  100% {
    rotate: 360deg;
  }
}

.active {
  stroke: var(--active);
  stroke-linecap: round;
  stroke-dashoffset: 360;
  animation: active-animation var(--duration) ease-in-out infinite;
}

@keyframes active-animation {
  0% {
    stroke-dasharray: 0 0 0 360 0 360;
  }
  12.5% {
    stroke-dasharray: 0 0 270 90 270 90;
  }
  25% {
    stroke-dasharray: 0 270 0 360 0 360;
  }
  37.5% {
    stroke-dasharray: 0 270 270 90 270 90;
  }
  50% {
    stroke-dasharray: 0 540 0 360 0 360;
  }
  50.001% {
    stroke-dasharray: 0 180 0 360 0 360;
  }
  62.5% {
    stroke-dasharray: 0 180 270 90 270 90;
  }
  75% {
    stroke-dasharray: 0 450 0 360 0 360;
  }
  87.5% {
    stroke-dasharray: 0 450 270 90 270 90;
  }
  87.501% {
    stroke-dasharray: 0 90 270 90 270 90;
  }
  100% {
    stroke-dasharray: 0 360 1 360 0 360;
  }
}

.track {
  stroke: var(--track);
  stroke-linecap: round;
  stroke-dashoffset: 360;
  animation: track-animation var(--duration) ease-in-out infinite;
}

@keyframes track-animation {
  0% {
    stroke-dasharray: 0 20 320 40 320 40;
  }
  12.5% {
    stroke-dasharray: 0 290 50 310 50 310;
  }
  25% {
    stroke-dasharray: 0 290 320 40 320 40;
  }
  37.5% {
    stroke-dasharray: 0 560 50 310 50 310;
  }
  37.501% {
    stroke-dasharray: 0 200 50 310 50 310;
  }
  50% {
    stroke-dasharray: 0 200 320 40 320 40;
  }
  62.5% {
    stroke-dasharray: 0 470 50 310 50 310;
  }
  62.501% {
    stroke-dasharray: 0 110 50 310 50 310;
  }
  75% {
    stroke-dasharray: 0 110 320 40 320 40;
  }
  87.5% {
    stroke-dasharray: 0 380 50 310 50 310;
  }
  100% {
    stroke-dasharray: 0 380 320 40 320 40;
  }
}

.border-gradient {
  border: 1.5px solid transparent;
  background-image: linear-gradient(180deg, white, white),
  linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
  background-clip: padding-box, border-box;
  background-origin: padding-box, border-box;
  &:hover .gradient {
    background: linear-gradient(232.02deg, rgba(57, 111, 250, 0.4) -65.62%, rgba(86, 249, 200, .4) 2.78%, rgba(57, 111, 250, .4) 98.62%);
  }
}
.gradient {
  background: linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
}
.gradients {
  background: linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
}
:deep(.el-dialog) {
  height: 600px;
  position: relative;
  .el-dialog__body {
    height: 580px;
    .center {
      transform: translate(-50%);
    }
  }
}
.list-shadow {
  box-shadow: 0px 4px 32px 0px #8195C852;
  .relative:hover {
    background: #1460F31A;
    .text-xs {
      color: #396FFA;
    }
  }
}
.h-full {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
    display: unset;
  }
  &::-webkit-scrollbar-track {
    display: unset;
    background-color: #f1f1f1;
  }
  &::-webkit-scrollbar-thumb {
    display: unset;
    border-radius: 4px;
    background-color: #c1c1c1;
  }
  &::-webkit-scrollbar-corner {
    display: unset;
    background-color: transparent;
  }
}
.assistant-shadow {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
::-webkit-scrollbar {
  display: none;
}
::-webkit-scrollbar-track {
  display: none;
}
::-webkit-scrollbar-thumb {
  display: none;
}
::-webkit-scrollbar-corner {
  display: none;
}
</style>
<style lang="scss">
.kbListPopper.el-popper.is-pure.is-light.el-tooltip.el-select__popper {
  .kbListPopper.el-select-dropdown.is-multiple {
    max-width: 492px;
  }
}
</style>
