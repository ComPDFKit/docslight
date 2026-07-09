<template>
  <div class="pt-60px pl-28px pr-32px bg-[#F3F6FF] min-h-100vh w-full">
    <h1 class="text-32px leading-48px text-brand-0 font-600">
      {{ t('profile.title') }}
    </h1>
    <div class="text-20px leading-28px text-brand-1 mt-16px mb-48px">
      {{ t('profile.desc') }}
    </div>
    <div class="shadows bg-white rounded-10px mt-42px p-40px">
      <el-form :model="ruleForm" ref="ruleFormRef" :rules="rules" label-position="top">
        <el-form-item prop="username" class="col">
          <template #label>
            <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
            {{ t('profile.name') }}
          </template>
          <el-input @input="noChange = false" v-model="ruleForm.username" :placeholder="t('profile.namePlaceholder')" />
        </el-form-item>
        <el-form-item prop="language">
          <template #label>
            <div class="pb-8px pt-12px">
              <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
              {{ t('profile.avatar') }}
            </div>
          </template>
          <div v-show="ruleForm.avatar" class="w-120px h-120px flex justify-center items-center border border-[#CED6E1] p-8px rounded-4px mr-8px">
            <img :src="ruleForm.avatar" alt="avatar" class="w-auto">
          </div>
          <input ref="input" class="hidden" type="file" name="file" accept=".png, .jpg, .jpeg" @change="handleChange">
          <div @click="input.value = '', input.click()" class="border-1 border-dashed bg-[#F3F6FF] w-120px h-120px border-[#CED6E1] flex justify-center items-center flex-col rounded-4px cursor-pointer w-full text-[#52555F] rounded-4px text-14px leading-20px">
            <Add class="mb-10px" />
            {{ t('profile.upload') }}
          </div>
        </el-form-item>
        <el-form-item class="col">
          <template #label>
            <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
            {{ t('profile.language') }}
          </template>
          <el-select @change="noChange = false" v-model="ruleForm.language">
            <el-option label="English" value="English"></el-option>
            <el-option label="简中" value="Chinese"></el-option>
            <el-option label="繁中" value="Traditional Chinese"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="col">
          <template #label>
            <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
            {{ t('profile.email') }}
          </template>
          <el-input disabled v-model="ruleForm.email" />
        </el-form-item>
        <div class="text-xs text-brand-1 flex justify-end mt-8px">
          <div class="max-w-688px w-full">
            {{ t('profile.noChange') }}
          </div>
        </div>
      </el-form>
      <div class="flex mt-40px">
        <div @click="router.go(-1)" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)">
          {{ t('profile.cancel') }}
        </div>
        <div @click="submit" :class="noChange && 'opacity-50 cursor-not-allowed'" class="w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]">
          {{ t('profile.save') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import { post } from '../utils/request'
import { inject, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { UploadFile } from 'element-plus'
import Add from '../components/images/Add.vue'
import { getFixedUserSettings, syncFixedUserSession } from '../utils/mockAuth'

const input = ref()
const { t } = useI18n()
const ruleFormRef = ref()
const router = useRouter()
const noChange = ref(true)
const ruleForm = ref({
  username: '',
  email: '',
  avatar: '',
  language: ''
})
const specialCharRegex = /^[\u4e00-\u9fa5_a-zA-Z0-9]+$/
const rules = ref({
  username: [
    { required: true, message: t('profile.namePlaceholder'), trigger: 'blur' },
    { max: 30, message: t('profile.must'), trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!specialCharRegex.test(value)) {
          callback(new Error(t('profile.special')))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
})
onMounted(() => {
  getUserInfo()
})
const submit = async () => {
  if (noChange.value) return
  ruleFormRef.value.validate( async (valid: boolean) => {
    if (valid) {
      const { data } = await post('/v1/user/setting', {
        avatar: ruleForm.value.avatar,
        color_schema: 'Bright',
        email: ruleForm.value.email,
        language: ruleForm.value.language,
        nickname: ruleForm.value.username,
        timezone: 'UTC+8\tAsia/Shanghai'
      })
      getUserInfo()
      setTimeout(() => {
        changeMenu()
      })
      const langMap = {
        'Chinese': 'zh-cn',
        'English': 'en',
        'Traditional Chinese': 'zh-tw'
      }
      localStorage.setItem('locale', langMap[ruleForm.value.language as keyof typeof langMap])
      if (data.code === 0 && data.data) {
        syncFixedUserSession({
          username: ruleForm.value.username,
          avatar: ruleForm.value.avatar,
        })
        ElMessage({
          message: t('profile.saved'),
          type: 'success',
          duration: 3000
        })
        setTimeout(() => {
          location.reload()
        }, 200)
      }
    }
  })
}
const changeMenu = inject('changeMenu', () => {})
const getUserInfo = async () => {
  const settings = getFixedUserSettings()
  localStorage.setItem('avatar', settings.avatar || '')
  ruleForm.value.email = settings.email
  ruleForm.value.username = settings.nickname
  ruleForm.value.avatar = settings.avatar
  ruleForm.value.language = settings.language
}

defineExpose({
  getUserInfo
})
const handleChange = async (e: any) => {
  const files = e.target.files
  if (!files) return
  noChange.value = false
  ruleForm.value.avatar = await getBase64FromUploadFileList(files)
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
</script>

<style lang="scss" scoped>
.shadows {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
:deep(.el-form) {
  .el-form-item {
    &.col {
      display: flex;
      margin-top: 22px;
      align-items: center;
      justify-content: space-between;
      .el-form-item__label {
        margin-bottom: 0;
      }
      .el-form-item__content {
        max-width: 688px;
        .el-input .el-input__wrapper .el-input__inner {
          min-height: 36px;
        }
        .el-select .el-select__wrapper {
          min-height: 36px;
          &.is-focused {
            box-shadow: 0 0 0 1px #396FFA inset;
          }
        }
      }
    }
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
