<template>
  <div class="login w-100vw min-h-100vh  flex flex-col justify-between">
    <div class="flex flex-col">
      <div class="flex justify-between items-center font-700 text-brand-0 text-24px leading-24px pt-18px pl-48px pr-30px title">
        <div class="flex items-center pt-6px">
          <img src="/logo.svg" alt="Logo" class="mr-[10.8px]">
          ComPDF AI
        </div>
        <LanguageCom />
      </div>
      <div class="mt-96px rounded-22px py-48px px-40px shadows mx-auto bg-white max-w-480px w-full">
        <h1 class="text-brand-0 text-32px leading-48px font-600 mb-20px text-center">
          {{ t('register.title') }}
        </h1>
        <el-form :model="ruleForm" ref="ruleForms" :rules="rules" label-position="top" label-width="100px">
          <el-form-item :label="t('register.name')" prop="username">
            <el-input @focus="ruleForms.clearValidate('username')" v-model="ruleForm.username" :placeholder="t('register.namePlaceholder')"></el-input>
          </el-form-item>
          <el-form-item :label="t('register.email')" prop="email">
            <el-input @focus="ruleForms.clearValidate('email')" v-model="ruleForm.email" :placeholder="t('register.emailPlaceholder')"></el-input>
          </el-form-item>
          <el-form-item :label="t('register.password')" prop="password">
            <el-input @focus="ruleForms.clearValidate('password')" v-model="ruleForm.password" :placeholder="t('register.passwordPlaceholder')" show-password type="password"></el-input>
          </el-form-item>
          <el-form-item :label="t('register.confirm')" prop="confirmPassword">
            <el-input @focus="ruleForms.clearValidate('confirmPassword')" v-model="ruleForm.confirmPassword" :placeholder="t('register.confirmPlaceholder')" show-password type="password"></el-input>
          </el-form-item>
        </el-form>
        <div class="flex mt-32px text-brand-1 text-12px leading-16px mb-48px min-h-32px">
          <div v-if="checkbox" class="privacy-box-active mr-8px">
            <img src="../assets/images/login/check_box.svg" class="cursor-pointer" @click="checkbox = false" />
          </div>
          <el-tooltip v-else :visible="showAllowError" class="box-item" effect="dark" :content="t('register.read')"
            placement="bottom-start" popper-class="el-popper-showArrow">
            <span class="privacy-box cursor-pointer mr-8px" @click="checkbox = true, showAllowError = false"></span>
          </el-tooltip>
          <p class="text-12px leading-16px text-brand-1">
            {{ t('register.have[0]') }}
            <a :href="'https://www.compdf.com' + (locale === 'en' ? '' : '/zh-cn' )+ '/terms-of-service'" target="_blank" class="text-brand-2 underline font-400">
              {{ t('register.have[1]') }}
            </a>
            {{ t('register.have[2]') }}
            <a :href="'https://www.compdf.com' + (locale === 'en' ? '' : '/zh-cn' )+ '/privacy-policy'" target="_blank" class="text-brand-2 underline font-400">
              {{ t('register.have[3]') }}
            </a>
          </p>
        </div>
        <div v-loading="loading" @click="submit" class="w-200px h-44px cursor-pointer flex justify-center items-center text-sm font-500 text-white rounded-6px bg-brand-2 mx-auto">
          {{ t('register.btn') }}
        </div>
        <div class="text-brand-3 mt-8px text-center">
          {{ t('register.alreadyHave') }}
          <a href="/login" class="text-brand-2 underline font-400 hover:text-[#244FF0]">
            {{ t('register.login') }}
          </a>
        </div>
      </div>
    </div>
    <div class="text-brand-1 text-sm font-500 flex justify-center w-full py-20px">
      {{ t('register.reserved') }}
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { rsaPsw } from '../utils/rsaPsw'
import { post } from '../utils/request'
import { ElMessage } from "element-plus"

const ruleForms = ref()
const loading = ref(false)
const checkbox = ref(false)
const { t, locale } = useI18n()
const showAllowError = ref(false)
const ruleForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})
const specialCharRegex = /^[\u4e00-\u9fa5_a-zA-Z0-9]+$/
const only = /^[a-zA-Z0-9]+$/
const rules = ref({
  username: [
    {
      max: 30,
      message: t('register.must'),
      trigger: ['']
    },
    {
      required: true,
      message: t('register.namePlaceholder'),
      trigger: ['']
    },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!specialCharRegex.test(value)) {
          callback(new Error(t('register.special')))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  email: [
    {
      type: 'email',
      message: t('register.valid'),
      trigger: ['']
    },
    {
      required: true,
      message: t('register.emailPlaceholder'),
      trigger: ['']
    }
  ],
  password: [
    {
      max: 30,
      message: t('register.passwordMust'),
      trigger: ['']
    },
    {
      required: true,
      message: t('register.passwordPlaceholder'),
      trigger: ['']
    },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!only.test(value)) {
          callback(new Error(t('register.only')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      max: 30,
      message: t('register.passwordMust'),
      trigger: ['']
    },
    {
      required: true,
      message: t('register.confirmPlaceholder'),
      trigger: ['']
    },
    {
      validator: (_rule: any, _value: string, callback: Function) => {
        if (ruleForm.value.confirmPassword !== ruleForm.value.password) {
          callback(new Error(t('register.match')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!only.test(value)) {
          callback(new Error(t('register.only')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})
const submit = () => {
  if (!checkbox.value) {
    showAllowError.value = true
    return
  }
  if (loading.value) return
  loading.value = true
  ruleForms.value.validate( async (valid: boolean) => {
    if (valid) {
      const { data } = await post('/v1/user/register', {
        nickname: ruleForm.value.username,
        email: ruleForm.value.email,
        password: rsaPsw(ruleForm.value.password)
      })
      loading.value = false
      if (data.code === 0 && data.message.includes('welcome aboard!')) {
        sessionStorage.setItem('user', JSON.stringify({ email: ruleForm.value.email, password: ruleForm.value.password }))
        location.href = '/login'
      } else if (data.code === 103 && data.data === false ) {
        ElMessage({
          type: 'error',
          duration: 3000,
          grouping: true,
          customClass: 'upload',
          message: t('register.already')
        })
      }
    } else {
      loading.value = false
    }
  })
}
</script>
<style lang="scss" scoped>
.login {
  background: #F3F6FF url('/images/login-bg.png') center center/100% 100% no-repeat;
  .shadows {
    box-shadow: 0px 4px 35px 0px #0029921A;
    .after {
      position: relative;
      &::after {
        content: " ";
        display: block;
        position: absolute;
        bottom: -1px;
        left: 0;
        width: 100%;
        height: 3px;
        background: #396FFA;
      }
    }
    :deep(.el-form) {
      .el-form-item {
        display: flex;
        margin-bottom: 20px;
        align-items: center;
        justify-content: space-between;
        .el-form-item__content {
          max-width: 278px
        }
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
          margin-bottom: 0;
          line-height: 24px;
          color: #232748;
          &::before {
            content: "*";
            font-size: 18px;
            line-height: 24px;
            font-weight: bold;
            color: #FF5050;
            font-family: 'Helvetica';
          }
        }
      }
    }
    .privacy-box-active {
      min-width: 16px;
      max-width: 16px;
      min-height: 16px;
      max-height: 16px;
      color: #1460F3;
      svg {
        width: 100%;
        height: 100%;
      }
    }
    .privacy-box {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
      border-radius: 2px;
      display: inline-block;
      border: 1px solid #AAAEB2;
    }
  }
}
</style>