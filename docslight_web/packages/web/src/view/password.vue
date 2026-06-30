<template>
  <div class="pt-60px pl-28px pr-32px bg-[#F3F6FF] min-h-100vh w-full">
    <h1 class="text-32px leading-48px text-brand-0 font-600">{{ t('password.title') }}</h1>
    <div class="text-20px leading-28px text-brand-1 mt-16px mb-48px">{{ t('password.desc') }}</div>
    <div class="shadows bg-white rounded-10px mt-42px p-40px">
      <el-form :model="ruleForm" ref="ruleFormRef" :rules="rules" label-position="left">
        <el-form-item prop="password">
          <template #label>
            <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
            {{ t('password.currentPassword') }}
          </template>
          <el-input v-model="ruleForm.password" :placeholder="t('password.currentPasswordPlaceholder')" show-password type="password" />
        </el-form-item>
        <el-form-item prop="new_password">
          <template #label>
            <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
            {{ t('password.newPassword') }}
          </template>
          <el-input v-model="ruleForm.new_password" :placeholder="t('password.newPasswordPlaceholder')" show-password type="password" />
        </el-form-item>
        <el-form-item prop="confirm_password">
          <template #label>
            <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
            {{ t('password.confirmPassword') }}
          </template>
          <el-input v-model="ruleForm.confirm_password" :placeholder="t('password.confirmPasswordPlaceholder')" show-password type="password" />
        </el-form-item>
      </el-form>
      <div class="flex mt-40px">
        <div @click="router.go(-1)" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px ml-20px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)">
          {{ t('password.cancel') }}
        </div>
        <div @click="submit" class="w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]">
          {{ t('password.save') }}
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { rsaPsw } from '../utils/rsaPsw'
import { post } from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'

const { t } = useI18n()
const ruleFormRef = ref()
const router = useRouter()
const ruleForm = ref({
  password: '',
  new_password: '',
  confirm_password: ''
})
const only = /^[a-zA-Z0-9]+$/
const rules = ref({
  password: [
    {
      required: true,
      message: t('password.currentPasswordPlaceholder'),
      trigger: 'blur'
    },
    {
      max: 30,
      message: t('password.must'),
      trigger: ''
    },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!only.test(value)) {
          callback(new Error(t('register.only')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  new_password: [
    {
      required: true,
      message: t('password.newPasswordPlaceholder'),
      trigger: ''
    },
    {
      max: 30,
      message: t('password.must'),
      trigger: ''
    },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!only.test(value)) {
          callback(new Error(t('register.only')))
        } else {
          callback()
        }
      },
      trigger: ''
    }
  ],
  confirm_password: [
    {
      max: 30,
      message: t('password.must'),
      trigger: ''
    },
    {
      required: true,
      message: t('password.confirmPasswordPlaceholder'),
      trigger: ''
    },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!only.test(value)) {
          callback(new Error(t('register.only')))
        } else {
          callback()
        }
      },
      trigger: ''
    },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (ruleForm.value.confirm_password !== ruleForm.value.new_password) {
          callback(new Error(t('password.match')))
        } else {
          callback()
        }
      },
      trigger: ''
    }
  ]
})
const submit = () => {
  ruleFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      const { data } = await post('/v1/user/setting', {
        password: rsaPsw(ruleForm.value.password),
        new_password: rsaPsw(ruleForm.value.new_password)
      })
      if (data.code === 0 && data.data) {
        ElMessage({
          duration: 3000,
          type: 'success',
          message: t('password.saved')
        })
        location.href = '/'
      } else if (data.code === 109 && data.message === 'Password error!') {
        ElMessage({
          message: t('password.passwordError'),
          type: 'error',
          duration: 3000
        })
      } else if (data.code === 103 && data.message === 'New password cannot be the same as the old one!') {
        ElMessage({
          message: t('password.same'),
          type: 'error',
          duration: 3000
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
:deep(.el-form) {
  & .el-form-item:first-child {
    margin-top: 0;
  }
  .el-form-item {
    display: flex;
    margin-top: 24px;
    justify-content: space-between;
    .el-form-item__label {
      margin-bottom: 0;
      min-width: 240px;
      font-size: 16px;
    }
    .el-form-item__content {
      .el-input .el-input__wrapper .el-input__inner {
        min-height: 36px;
      }
      .el-form-item__error {
        padding-top: 4px;
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
