<template>
  <div class="bg-[#F3F6FF] min-h-100vh w-full">
    <KbHeader />
    <div class="p-64px pt-152px">
      <h1 class="text-32px leading-48px font-600 text-brand-0 mb-2px max-w-450px truncate">
        {{ t('knowledgeBases.home.title') }}
        {{ user.username }}
      </h1>
      <div class="flex justify-between items-end pb-24px border-b border-[#E1E3E8]">
        <div class="text-brand-1 text-20px leading-28px min-w-0 mr-24px">
          {{ t('knowledgeBases.home.desc') }}
        </div>
        <div class="flex items-center gap-12px shrink-0">
          <el-input class="w-220px shrink-0" v-model="searchQuery" @clear="getKb" @keyup.enter="getKb" :placeholder="t('knowledgeBases.home.search')">
            <template #prefix>
              <Search />
            </template>
          </el-input>
          <div v-permission="'kb:create'" @click="dialogVisible = true" class="h-40px shrink-0 rounded-6px cursor-pointer bg-[#396FFA] text-white text-16px leading-24px py-8px px-10px flex items-center justify-center font-500 hover:bg-[#244FF0] whitespace-nowrap">
            {{ t('knowledgeBases.home.create') }}
          </div>
          <a href="/user-management" class="h-40px shrink-0 rounded-6px cursor-pointer bg-[#396FFA] text-white text-16px leading-24px py-8px px-10px flex items-center justify-center font-500 hover:bg-[#244FF0] whitespace-nowrap">
            {{ t('common.teamManagement') }}
          </a>
        </div>
      </div>
      <div v-if="kbList.length" class="flex flex-wrap mt-36px">
        <a :href="'/knowledge-base/dataset?id=' + item.id" class="card mr-20px relative bg-white p-20px border border-transparent rounded-8px" v-for="(item, index) in kbList" :key="index" :class="index > 2 && 'mt-20px'">
          <div class="flex items-start justify-between">
            <img v-if="item.avatar" :src="item.avatar" alt="avatar" class="w-72px h-72px rounded-6px">
            <div v-else class="p-12px bg-[#1460F31A] text-brand-2 rounded-6px"><Kb /></div>
            <Action v-if="item.role !== 'viewer'" @click.stop.prevent="statusArr[index].status = true" class="action" :class="statusArr[index].status && 'active'" />
          </div>
          <div class="text-brand-0 text-24px leading-36px font-600 my-20px truncate">{{ item.name }}</div>
          <div class="flex items-center mb-8px"><Document class="mr-8px"/>{{ item.doc_num }} {{ t('knowledgeBases.home.docs') }}</div>
          <div class="flex items-center"><Time class="mr-8px"/>{{ dayjs(item.update_time).format('YYYY/MM/DD HH:mm:ss') }}</div>
          <div v-show="statusArr[index].status" class="shadows absolute top-54px right-8px p-4px bg-white rounded-4px">
            <div v-show="item.role === 'owner'" class="py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])" @click.stop.prevent="deleteKb(item.id)">
              {{ t('knowledgeBases.home.delete') }}
            </div>
            <a :href="'/knowledge-base/configuration?id=' + item.id" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
              {{ t('knowledgeBases.home.management') }}
            </a>
          </div>
        </a>
      </div>
      <div v-else class="w-full h-[calc(100vh-244px)] flex flex-col justify-center items-center">
        <Empty class="mb-8px" />
        <div class="text-sm text-brand-3">
          {{ t('knowledgeBases.home.empty') }}
        </div>
      </div>
      <el-dialog v-model="dialogVisible" align-center width="520px">
        <h3 class="text-sm font-bold text-[#43474D] py-4px mb-24px">{{ t('knowledgeBases.home.create') }}</h3>
        <el-form :model="ruleForm" ref="ruleForms" :rules="rules" label-position="top" label-width="100px">
          <el-form-item prop="kbName">
            <template #label>
              <div class="flex text-xs text-brand-0 font-600 mb-8px">
                <span class="text-[#FF5050] inline-block mr-4px font">*</span>
                {{ t('knowledgeBases.home.name') }}
              </div>
            </template>
            <el-input v-model="ruleForm.kbName" @keyup.enter.stop :placeholder="t('knowledgeBases.home.namePlaceholder')"></el-input>
          </el-form-item>
          <el-form-item class="hidden">
            <input class="hidden" v-model="ruleForm.kbName" @keyup.enter.stop :placeholder="t('knowledgeBases.home.namePlaceholder')" />
          </el-form-item>
        </el-form>
        <div class="flex justify-center mt-24px">
          <div class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)" @click="dialogVisible = false">
            {{ t('knowledgeBases.home.cancel') }}
          </div>
          <div :class="ruleForm.kbName.length ? 'hover:bg-[#244FF0]' : 'opacity-50 cursor-not-allowed'" class="w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]" @click="createKb">
            {{ t('knowledgeBases.home.ok') }}
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useCookies } from 'vue3-cookies'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import Kb from '../components/images/Kb.vue'
import Time from '../components/images/Time.vue'
import Empty from '../components/images/Empty.vue'
import Action from '../components/images/Action.vue'
import Document from '../components/images/Document.vue'
import { onMounted, ref, onBeforeUnmount } from 'vue'
import { get, post, _delete } from '../utils/request'

const { cookies } = useCookies()
interface kb {
 avatar: string
 name: string
 update_time: number
 doc_num: number
 nickname: number
 id: string
 role: string
}
interface status {
 status: boolean
}
const route = useRoute()
const total = ref(0)
const ruleForms = ref()
const { t } = useI18n()
const searchQuery = ref('')
const kbList = ref<kb[]>([])
const dialogVisible = ref(false)
const statusArr = ref<status[]>([])
const ruleForm = ref({
  kbName: ''
})
const rules = ref({
  kbName: [
    {
      max: 30,
      message: t('knowledgeBases.home.must'),
      trigger: ['']
    },
    {
      required: true,
      message: t('knowledgeBases.home.namePlaceholder'),
      trigger: ['']
    }
  ]
})
// @ts-ignore
const user = ref((cookies.get('idp_user') || JSON.parse(sessionStorage.getItem('idp_user') || '{}')))
onMounted(async () => {
  getKb()
  if (route.query.create) {
    dialogVisible.value = true
  }
  addEventListener('click', handleClick)
})
onBeforeUnmount(() => {
  removeEventListener('click', handleClick)
})
const getKb = async () => {
  const { data: res } = await get('/v1/kb/list?keywords=' + searchQuery.value)
  if (res.code === 0 && res.message === 'success') {
    total.value = res.data.total
    kbList.value = res.data.kbs
    statusArr.value = []
    kbList.value.forEach(()=> {
      statusArr.value.push({ status: false })
    })
  }
}
const createKb = async () => {
  if (!ruleForm.value.kbName) return
  ruleForms.value.validate( async (valid: boolean) => {
    if (valid) {
      const { data } = await post('/v1/kb/create', {
        name: ruleForm.value.kbName
      })
      if (data.code === 0 && data.message === 'success') {
        getKb()
        ElMessage.success(t('knowledgeBases.home.created'))
        location.href = '/knowledge-base/configuration?id=' + data.data.kb_id
      }
      dialogVisible.value = false
    }
  })
}
const handleClick = () => {
  statusArr.value.forEach(item => {
    item.status = false
  })
}
const deleteKb = async(id: string) => {
  ElMessageBox.confirm(t('knowledgeBases.home.sure'), '', {
    confirmButtonText: t('knowledgeBases.home.yes'),
    cancelButtonText: t('knowledgeBases.home.no'),
    type: 'warning'
  }).then(async () => {
    try {
      const { data: res } = await post(`/v1/kb/rm`, {
        kb_id: id
      })
      if (res.code === 0 && res.message === 'success') {
        getKb()
        ElMessage.success(t('knowledgeBases.home.deleteSuccess'))
      } else {
        ElMessage.error(t('knowledgeBases.home.deleteFail'))
      }
    } catch {
      ElMessage.error(t('knowledgeBases.home.deleteFail'))
    }
  })
}
</script>

<style lang="scss" scoped>
.shadows {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
.card {
  width: calc((100% - 40px) / 3);
  &:nth-child(3n) {
    margin-right: 0;
  }
  &:hover {
    border-color: #CDDBFF;
    box-shadow: 0px 4px 35px 0px #0029921A;
  }
}
.flex-wrap::after {
  height: 0;
  content: '';
  flex-grow: 1;
  visibility: hidden;
  width: calc((100% - 40px) / 3);
}
:deep() {
  svg.action {
    cursor: pointer;
    &:hover {
      rect {
        fill: #F3F6FF;
      }
      circle {
        fill: #396FFA;
      }
    }
    &.active {
      rect {
        fill: #F3F6FF;
      }
      circle {
        fill: #396FFA;
      }
    }
    rect {
      fill: transparent;
    }
  }
}
:deep(.el-form) {
  .el-form-item {
    .el-form-item__label {
      span {
        font-family: 'Helvetica';
      }
      &::before {
        display: none;
      }
    }
  }
}
</style>
