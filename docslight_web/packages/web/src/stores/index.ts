import { ref, reactive } from 'vue'
import { defineStore } from 'pinia'

export const useStore = defineStore('store', () => {
  const permission = reactive({ // 页面权限
    qa: false,
    extraction: false,
    layout: false,
    split: false,
    user: false
  })
  const role = ref('')
  const showPermissionDialog = ref(false) // 无权限弹窗

  return { permission, showPermissionDialog, role }
}, {
  persist: {
    storage: localStorage,
    pick: ['permission', 'role']
  }
})

export * from './permission'
export * from './uploadTask'
