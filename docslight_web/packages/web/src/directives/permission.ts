import type { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '../stores/permission'

export interface PermissionDirectiveOptions {
  code: string
  mode?: 'disabled' | 'hidden' // 默认 'disabled'：禁用；'hidden'：隐藏元素
}

/**
 * 权限指令
 * 
 * 用法示例：
 * 
 * 1. 简单用法（传入权限 code，无权限则禁用）：
 *    <el-button v-permission="'extract:upload'">上传</el-button>
 * 
 * 2. 指定模式（无权限则隐藏）：
 *    <el-button v-permission="{ code: 'extract:delete', mode: 'hidden' }">删除</el-button>
 * 
 * 3. 禁用模式（默认）：
 *    <el-button v-permission="{ code: 'extract:upload', mode: 'disabled' }">上传</el-button>
 */
export const vPermission: Directive<HTMLElement, string | PermissionDirectiveOptions> = {
  mounted(el, binding: DirectiveBinding<string | PermissionDirectiveOptions>) {
    const permissionStore = usePermissionStore()
    
    // 解析传入的参数
    let code: string
    let mode: 'disabled' | 'hidden' = 'disabled'
    
    if (typeof binding.value === 'string') {
      code = binding.value
    } else if (binding.value && typeof binding.value === 'object') {
      code = binding.value.code
      mode = binding.value.mode || 'disabled'
    } else {
      console.warn('[v-permission] 指令参数格式错误:', binding.value)
      return
    }

    // 检查权限
    // 如果权限树为空（还未初始化），默认允许所有操作
    if (!permissionStore.initialized) {
      return
    }
    
    const hasPermission = permissionStore.hasPermission(code)

    if (!hasPermission) {
      if (mode === 'hidden') {
        // 隐藏模式：直接移除元素
        el.style.display = 'none'
        // 或者从 DOM 中移除（更彻底）
        // el.parentNode?.removeChild(el)
      } else {
        // 禁用模式：添加禁用样式和属性
        el.style.pointerEvents = 'none'
        el.style.opacity = '0.5'
        el.style.cursor = 'not-allowed'

        // 如果是按钮元素，设置 disabled 属性
        if (el.tagName === 'BUTTON') {
          ;(el as HTMLButtonElement).disabled = true
        }

        // 如果是 Element Plus 组件，尝试设置 disabled class
        if (el.classList.contains('el-button')) {
          el.classList.add('is-disabled')
        }

        // 阻止点击事件
        el.addEventListener('click', (e) => {
          e.preventDefault()
          e.stopPropagation()
        }, { capture: true })
      }
    }
  },

  updated(el, binding: DirectiveBinding<string | PermissionDirectiveOptions>) {
    // 如果权限状态可能动态变化，可以在这里重新检查
    const permissionStore = usePermissionStore()
    
    let code: string
    let mode: 'disabled' | 'hidden' = 'disabled'
    
    if (typeof binding.value === 'string') {
      code = binding.value
    } else if (binding.value && typeof binding.value === 'object') {
      code = binding.value.code
      mode = binding.value.mode || 'disabled'
    } else {
      return
    }

    // 如果权限树为空，不进行控制
    if (!permissionStore.initialized) {
      return
    }

    const hasPermission = permissionStore.hasPermission(code)

    if (!hasPermission) {
      if (mode === 'hidden') {
        el.style.display = 'none'
      } else {
        el.style.pointerEvents = 'none'
        el.style.opacity = '0.5'
        el.style.cursor = 'not-allowed'
        
        if (el.tagName === 'BUTTON') {
          ;(el as HTMLButtonElement).disabled = true
        }
        
        if (el.classList.contains('el-button')) {
          el.classList.add('is-disabled')
        }
      }
    } else {
      // 有权限时恢复元素状态
      if (mode === 'hidden') {
        el.style.display = ''
      } else {
        el.style.pointerEvents = ''
        el.style.opacity = ''
        el.style.cursor = ''
        
        if (el.tagName === 'BUTTON') {
          ;(el as HTMLButtonElement).disabled = false
        }
        
        if (el.classList.contains('el-button')) {
          el.classList.remove('is-disabled')
        }
      }
    }
  }
}
