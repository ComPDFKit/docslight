import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export interface PermissionNode {
  action_type: number
  code: string
  id: string
  level: number
  module_id: string
  name: string
  parent_id: string | null
  status: string
  children?: PermissionNode[]
}

export type PermissionTree = PermissionNode[]

const collectCodes = (nodes: PermissionNode[] | undefined, out: string[]) => {
  if (!nodes?.length) return
  nodes.forEach((node) => {
    if (node?.code) out.push(node.code)
    if (node.children?.length) collectCodes(node.children, out)
  })
}

const buildCodes = (tree: PermissionTree): string[] => {
  const out: string[] = []
  collectCodes(tree, out)
  return Array.from(new Set(out))
}

export const usePermissionStore = defineStore(
  'permission-store',
  () => {
    const permissionTree = ref<PermissionTree>([])
    const codes = ref<string[]>([])

    const initialized = computed(() => codes.value.length > 0 || permissionTree.value.length > 0)

    const setPermissions = (tree: PermissionTree) => {
      permissionTree.value = Array.isArray(tree) ? tree : []
      codes.value = buildCodes(permissionTree.value)
    }

    // 有值就不重复赋值：返回 true 表示本次成功写入；false 表示已存在，跳过
    const setPermissionsOnce = (tree: PermissionTree): boolean => {
      if (initialized.value) return false
      setPermissions(tree)
      return true
    }

    const clearPermissions = () => {
      permissionTree.value = []
      codes.value = []
    }

    const has = (code: string): boolean => codes.value.includes(code)

    // 递归查找权限节点
    const findPermissionNode = (nodes: PermissionNode[] | undefined, code: string): PermissionNode | null => {
      if (!nodes?.length) return null
      for (const node of nodes) {
        if (node.code === code) return node
        if (node.children?.length) {
          const found = findPermissionNode(node.children, code)
          if (found) return found
        }
      }
      return null
    }

    // 检查指定 code 的权限是否启用 (status === '1')
    const hasPermission = (code: string): boolean => {
      if (!code) return false
      const node = findPermissionNode(permissionTree.value, code)
      return node?.status === '1'
    }

    // 模块级判断：支持 'extract' / 'extract:*' / 'extract:template:modify' 这类结构
    const hasModule = (moduleCode: string): boolean => {
      if (!moduleCode) return false
      return codes.value.some((c) => c === moduleCode || c.startsWith(`${moduleCode}:`))
    }

    const pagePermission = computed(() => ({
      extraction: hasModule('extract'),
      layout: hasModule('parse'),
      qa: hasModule('kb'),
      split: hasModule('split'),
      user: hasModule('user'),
    }))

    return {
      permissionTree,
      codes,
      initialized,
      setPermissions,
      setPermissionsOnce,
      clearPermissions,
      has,
      hasPermission,
      hasModule,
      pagePermission,
    }
  },
  {
    persist: {
      storage: localStorage,
      pick: ['permissionTree', 'codes'],
    },
  },
)
