<template>
  <div class="relative flex <lg:(bg-[#F3F6FF] h-100vh)">
    <div class="fixed z-2 top-0 h-80px flex items-center border-b border-[#E1E3E8] bg-white w-full py-22px pl-32px">
      <div class="text-20px leading-28px text-brand-0 mr-12px">
        {{ t('common.idp') }}
      </div>
    </div>
   <!-- <div class="w-168px max-h-[calc(100vh-160px)] pt-12px overflow-auto history bg-white my-80px border-r-1 border-[#dcdfe5] <lg:hidden">
     <div @click="openHistoryFile(item)" class="px-8px text-12px leading-14px cursor-pointer hover:text-brand-2" v-for="(item, index) in historyFileList" :key="index" :class="index && 'mt-12px'">
       {{ item.fileName }}
     </div>
   </div> -->
    <div v-show="!file" class="<lg:hidden w-[calc(50%-84px)] h-[calc(100vh-160px)] flex items-center my-80px p-20px bg-[#F2F2F2]">
      <div @drop.prevent="onDrop" @dragover.prevent="dragover = true" @dragleave.prevent="dragover = false" :class="dragover && 'bg-[#F3F6FF]'" class="w-full h-full rounded-12px border-dashed border-1 border-brand-2 flex justify-center items-center flex-col">
        <div class="cursor-pointer relative text-sm">
          <div @click="input.click" class="w-300px rounded-6px mx-auto mt-106px cursor-pointer bg-[#396FFA] text-white text-sm font-600 py-10px px-12px flex items-center justify-center hover:bg-[#244FF0]">
            <Upload class="mr-12px w-20px h-20px" />
            {{ t('singleExtract.selectFile[0]') }}
          </div>
          <div class="text-center my-8px text-xs text-brand-3">
            {{ t('singleExtract.selectFile[1]') }}
          </div>
          <div class="text-center text-xs text-brand-3">
            {{ t('singleExtract.selectFile[2]') }}
          </div>
        </div>
        <input ref="input" class="hidden" type="file" name="file" accept=".pdf, .jpg, .jpeg, .png" @change="handleChange">
      </div>
    </div>
    <div v-show="file" id="webviewer" ref="viewer" class="h-[calc(100vh-160px)] w-[calc(50%-84px)] my-80px <lg:hidden"></div>
    <div class="relative w-[calc(50%-84px)] h-[calc(100vh-160px)] extract <lg:hidden" :class="outputType === 'txt' && '!bg-white'">
      <div class="text-white flex text-16px leading-18px border-b-1px border-[#E1E3E8]">
        <div @click="changeConvert('json')" class="w-100px flex justify-center py-10px items-center cursor-pointer"
          :class="[outputType === 'json' && 'after', outputType === 'txt' && '!text-[#232748]']">JSON</div>
        <div @click="changeConvert('txt')" class="w-100px flex justify-center py-10px items-center cursor-pointer"
          :class="[outputType === 'txt' && 'after', outputType === 'txt' && '!text-[#232748]']">Markdown</div>
      </div>
      <JsonViewer v-show="outputType === 'json' && jsonResult" boxed expanded :expandDepth="7" sort theme="dark" :value="jsonResult"/>
      <div v-show="outputType === 'txt'" class="text-[#52555F] mt-24px h-[calc(100vh-260px)] overflow-auto pr-20px">
        <!-- Loading -->
        <div v-show="dialogVisibleLoading" class="h-[calc(100vh-220px)] flex justify-center items-center">
          <DemoLoading class="transform scale-70" />
        </div>
        <!-- 初始界面显示 -->
        <div v-show="init" class="h-[calc(100vh-220px)] flex flex-col justify-center items-center">
          <img src="/images/idp/init.png" alt="init" width="240" height="150">
        </div>
        <template v-if="jsonResult && !init">
          <div ref="content" @scroll="handleScroll" v-show="outputType === 'txt'" class="md-content text-[#52555F] mt-24px h-[calc(100vh-260px)] overflow-auto pr-20px">
            <div class="my-4px" v-for="(item, index) in jsonResult.result.detail" :key="index">
              <template v-if="item.type === 'table'">
                <div class="grid pr-12px cursor-pointer"
                  @click="handleClick(item.position, item.page_id)"
                  :id="item.position.join('-')"
                  :class="active.join('-') === [(item.position[0] / dpiScale), (item.position[1] / dpiScale), (item.position[2] / dpiScale), (item.position[5] / dpiScale)].join('-') && 'text-[#396FFA] borderActive'" v-html="renderTableContent(item.text)">
                </div>
              </template>
              <template v-else-if="item.type === 'image'">
                <img :src="getImageSrc(item)" alt="image" :class="active.join('-') === [(item.position[0] / dpiScale), (item.position[1] / dpiScale), (item.position[2] / dpiScale), (item.position[5] / dpiScale)].join('-') && 'border-[#396FFA]'" @click="handleClick(item.position, item.page_id)" class="cursor-pointer border-1">
              </template>
              <template v-else>
                <div
                  class="cursor-pointer inline-flex"
                  @click="handleClick(item.position, item.page_id)"
                  :class="active.join('-') === [(item.position[0] / dpiScale), (item.position[1] / dpiScale), (item.position[2] / dpiScale), (item.position[5] / dpiScale)].join('-') && 'text-[#396FFA]'"
                  :id="item.position.join('-')"
                >
                  <span class="mr-8px flex w-6px h-6px rounded-1/2 bg-brand-0"></span>
                  <template v-html="renderedContent(item.text)"></template>
                </div>
              </template>
            </div>
          </div>
        </template>
      </div>
    </div>
    <input ref="input" class="hidden" type="file" name="file" accept=".pdf, .jpg, .jpeg, .png" @change="handleChange">
    <div class="fixed bottom-0 z-1 h-80px w-full bg-white border-t-1px border-[#E1E3E8] flex items-center <lg:hidden">
      <div class="w-[calc(50%-38px)] flex items-center border-r-1 border-[#E1E3E8] h-80px">
        <div @click="input.click" class="font w-140px text-brand-2 font-600 text-14px leading-16px border-1 rounded-4px border-brand-2 hover:(bg-brand-2 text-white) cursor-pointer py-12px flex items-center justify-center mx-auto">
          <Upload class="mr-2px" />
          {{ t('singleExtract.open') }}
        </div>
      </div>
      <div class="w-[calc(50%-38px)] flex justify-center items-center" :class="{ 'opacity-50 cursor-not-allowed': !jsonResult }">
        <div
          @click="dialogVisible = true"
          class="cursor-pointer font-600 w-162px h-40px flex justify-center items-center bg-[#396FFA] text-white rounded-2px text-sm hover:bg-[#244FF0]"
          :class="{ 'pointer-events-none': !jsonResult }"
        >
          <Download class="mr-4px" />
          {{ t('singleExtract.download') }}
        </div>
      </div>
    </div>
    <div class="w-100vw h-100vh bg-black fixed top-0 left-0 opacity-50" v-show="loading"></div>
    
    <!-- download -->
    <el-dialog v-model="dialogVisible" width="372px" top="20vh">
      <div class="flex justify-end">
        <IdpClose @click="dialogVisible = false" class="cursor-pointer w-17px h-17px" />
      </div>
      <p class="text-[#43474D] text-sm font-bold mb-12px">{{ t('singleExtract.selectFormat') }}</p>
      <el-radio-group v-model="toType">
        <el-radio value="json">JSON</el-radio>
        <el-radio value="md">Markdown</el-radio>
        <el-radio value="txt">TXT</el-radio>
      </el-radio-group>
      <div @click="downloadFile" class="cursor-pointer font-600 w-full h-40px flex justify-center items-center bg-[#396FFA] mt-21px text-white rounded-4px text-sm hover:bg-[#244FF0]">
        {{ t('singleExtract.download') }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import { ElMessage } from 'element-plus'
import { post, get } from '../utils/request'
import 'vue3-json-viewer/dist/vue3-json-viewer.css'
import { JsonViewer } from 'vue3-json-viewer'
import { ref, onMounted, nextTick } from 'vue'
import ComPDFKitViewer from "../assets/@compdfkit/webviewer"
import DemoLoading from "@/components/DemoLoading.vue"
import Download from "@/components/idp/Download.vue"
import JSZip from 'jszip'
import { getEnv } from '@/utils/env'
import MarkdownIt from 'markdown-it'
import mk from 'markdown-it-katex'
import jsPDF from 'jspdf'
import { sanitizeHtml, sanitizeTableHtml } from '@/utils/sanitizeHtml'

const { t } = useI18n()
const file = ref()
const password = ref('')
const percentage = ref()
const init = ref(true)
const initTxt = ref(true)
const firstTxt = ref(true)
const toType = ref('json')
const initFile = ref('one')
const dragover = ref(false)
const outputType = ref('txt')
const loading = ref(false)
const isDownload = ref(false)
const historyFileList = ref<fileRes[]>([])
const dialogVisibleLoading = ref(false)
const downloadName = ref('dataExtractJson.json')
interface fileRes {
  fileName: string
  fileUrl: string
  resultFileUrl: string
}
const enableChangeFile = ref(false)
const jsonResult = ref()
const viewer = ref()
const input = ref()
let UI = <any>null
const active = ref<number[]>([])
let docViewer = <any>null
const content = ref()
let license = ''
interface FileData {
  name: string
  blob: Blob
  fullPath?: string
}
interface ExtractedFiles {
  json: any
  images: FileData[],
  fileBlobs: { [key: string]: FileData }
}
const imageData = ref<FileData[]>([])
const blobUrlCache = new Map<string, string>()
const dpiScale = ref(1)
const md = new MarkdownIt({ html: false })
md.use(mk)
const dialogVisible = ref(false)
let extractFiles = <any>null

const handleScroll = (e: Event) => {
  // 将事件目标转换为HTMLDivElement
  const target = e.target as HTMLDivElement

  // 获取各种滚动距离
  const scrollTop = target.scrollTop      // ✅ 垂直滚动距离
  const scrollHeight = target.scrollHeight // 内容总高度
  const clientHeight = target.clientHeight // 可视区域高度

  // 计算滚动百分比
  const maxScrollTop = scrollHeight - clientHeight
  const scrollPercentage = maxScrollTop > 0 ? (scrollTop / maxScrollTop) * 100 : 0
  console.log(`已滚动 ${scrollPercentage.toFixed(1)}%`)

  // 判断是否滚动到底部（容差5像素）
  const isBottom = Math.abs(scrollHeight - clientHeight - scrollTop) <= 5
  if (isBottom) {
    console.log('已滚动到底部')
  }
  docViewer.scrollTo({
    top: scrollTop
  })
}

onMounted(async () => {
  license = getEnv('LICENSE_KEY') || 'T7U3B5DRU/0SpKhlQUK+atunFpFX0BXtsbFKeQ0yWbRJS49y2XW5WvX8l2I97LJfFVys9CviIuVAkI/l5d1yt3gFNNaOr+j8tk6ux49YoYXUDrQFq++KeQD4GeLVOyIRh2//GdLxPLD/3/o009PKmlO7gBb6r4gmJu5XIP3MjuUMcFCMIv8VMJEOqWXOWUGdgv2uvzdQfCwTeh31JBQwI3mMbc89gIqkim8hkVIQc4d1JhjD4bwoZFMbPoifFC8ttZG9p6xJhcYB3nwvthJDHPeypkXe0Cqsz10f3GjDK3psbXyrFPwOYZWk8/iMYbZJac9Xkk7lUk2L7zWQVnBR5K8kD+Q9C95FZo3W7DLPF5KMwW5kHhd0f5YnHanbCC+p50yu/5vL87G9JysRsdZLVpqjq+Rl8TlXYpcWUmsxkyXHeT1cx3lwemQ+isDbjLVieS9+A77jSbnn8YXaYHMTDs2r8HR8raKBaLfi0hJQbCZ3Hp5IoGPlzemWyWlnbqbri1FyKakDUYCMuHXbCGTPCcr9x9yMwPeqSAPl+wokwU0HWDuZdWf9OfgFrGoOgvuLu3jtEqejW30netihq7dCYwlsfm+pCMb1eMDNco6qIQZtDLYU5UYg/9CkDU+lJfRPQc363EiuE/0u2BCNRWxPfMZPhNHzj9ws+OFWVjwyuyYMJ3jbi5b1yn5hiMOzbSwddlCr1w4ArIMAHPpL3p8HvYgCmJwgS9ma80+YX7S0hPxLtaEifYQJ52/fVeOcDF5eLlAj2Z3ZXDkeC9D6DFNjPss5N/8e+M8x3+fmZTRKco5bR//egB4v2zc1/BtVZjx4loGwCB+XRa+8sAp2sYd/TXX1Lk9Dfi34B1gjXFVuafskORJzlMQmx94GcdD6Sstd'

  const { data: { data } } = await get('https://parse.compdf.com/parsing/inter-list', {
    headers: {
      'API_KEY': 'NjhiMTQ0YmQ5ODk2NA=='
    }
  } as any)
  historyFileList.value = data
  if (window.innerWidth > 930) {
    ComPDFKitViewer.init({
      license,
      pdfUrl: '',
      path: '/',
      enableDefaultFont: true,
      showToolbarControl: false
    }, viewer.value).then((core: any) => {
      docViewer = core.docViewer
      UI = core.UI
      docViewer.addEvent('annotationSelected', (data: any) => {
        active.value = [data.rect.left, data.rect.top, data.rect.right, data.rect.bottom]
        // [left, top, right, top, right, bottom, left, bottom]
        const id = [data.rect.left * 2, data.rect.top * 2, data.rect.right * 2, data.rect.top * 2, data.rect.right * 2, data.rect.bottom * 2, data.rect.left * 2, data.rect.bottom * 2]
        const dom = document.getElementById(id.join(', ').replace(/\./g, '-').replace(/\,/g, '-').replace(/\ /g, ''))
        if (dom) {
          const top = dom.offsetTop - 100
          content.value.scrollTop = top
        }
      })
      const scrollViewer = docViewer.getScrollViewElement()
      scrollViewer.addEventListener('scroll', (e: Event) => {
        const target = e.target as HTMLElement

        // 获取滚动距离
        const scrollTop = target.scrollTop || 0

        console.log('垂直滚动距离:', scrollTop)
        nextTick(() => content.value.scrollTop = scrollTop)
      })
      docViewer.addEvent('onPageNumberUpdated', () => {})
      docViewer.addEvent('documentloaded', () => {
        loading.value = false
        jsonResult.value?.result.detail.forEach((element: any) => {
          docViewer.addAnnotations({
            type: 'custom',
            pageIndex: element.page_id - 1,
            borderWidth: 1,
            borderColor: '#396FFA',
            rect: {
              left: element.position[0] / (dpiScale.value),
              top: element.position[1] / (dpiScale.value),
              right: element.position[2] / (dpiScale.value),
              bottom: element.position[5] / (dpiScale.value)
            },
            selectedStyle: {
              border: '2px solid #396FFA',
              background: '#396FFA14'
            }
          })
        })
      })
    })
  }
})
const openHistoryFile = (val: fileRes) => {
  loading.value = true
  loadDocument(val)
}
const urlToFile = async (url: string, fileName: string, type: string): Promise<File> => {
  const res = await fetch(url)
  const blob = await res.blob()
  return new File([blob], fileName, { type: 'application/' + type })
}
const loadDocument = async (val: fileRes) => {
  deselectClick()
  const postFile = await urlToFile(val.fileUrl, val.fileName, 'pdf')
  const { data } : any = await get(val.resultFileUrl)
  file.value = postFile
  UI.loadDocument(postFile)
  jsonResult.value = data
  init.value = false
}
interface annType {
  pageIndex: number
  name: string
  getRect: () => {
    x1: number,
    y1: number,
    x2: number,
    y2: number,
  }
}
// 点击文本
const handleClick = (val: number[], page: number) => {
  active.value = val
  const annotationManager = docViewer.getAnnotationManager()
  const annotations = annotationManager.getAnnotationsList()
  annotations.forEach((ele: annType) => {
    const rect = ele.getRect()
    if (ele.pageIndex === Number(page - 1) && rect.x1 === (val[0] / (dpiScale.value)) && rect.y1 === (val[1] / (dpiScale.value)) && rect.x2 === (val[2] / (dpiScale.value)) && rect.y2 === (val[5] / (dpiScale.value))) {
      annotationManager.selectAnnotation(ele)
    }
  })
  docViewer.pageNumberChanged(page)
}
// 取消选中注释
const deselectClick = () => {
  if (!docViewer) return
  const annotationManager = docViewer.getAnnotationManager()
  const annotations = annotationManager.getAnnotationsList()
  const selectedName = docViewer.annotationStore.selectedElementName
  if (!selectedName) return
  for (const ele of annotations) {
    if (selectedName && ele.name === selectedName) {
      const annot = annotationManager.getAnnotationById(ele.name)
      annot.handleOutside && annot.handleOutside()
      break
    }
  }
}
// 拖拽上传文件
const onDrop = async (e: DragEvent) => {
  deselectClick()
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  let postFile = files[0]

  if (postFile.type.includes('image')) {
    postFile = await imageToPDF(postFile)
  }
  dialogVisibleLoading.value = false
  const nameArray = postFile.name.split('.')
  if (nameArray[nameArray.length - 1].toLowerCase() !== 'pdf') {
    dragover.value = false
    ElMessage.error(t('bulkExtract.notSupport'))
    return
  }
  deselectClick()
  downloadName.value = nameArray[0]
  await UI.loadDocument(postFile)
  file.value = postFile
  uploadFiles(postFile, true)
}
// 选择其他文件
const handleChange = async (e: any) => {
  deselectClick()
  const files = e.target.files
  if (!files || files.length === 0) return
  let postFile = files[0]

  if (postFile.type.includes('image')) {
    postFile = await imageToPDF(postFile)
  }

  initFile.value = ''
  enableChangeFile.value = false
  UI.loadDocument(postFile)
  const passwords = UI.getPassword()
  file.value = postFile
  password.value = passwords
  toType.value = 'json'
  firstTxt.value = true
  initTxt.value = false
  jsonResult.value = null
  extractFiles = null
  uploadFiles(postFile, true)
}
// 显示切换
const changeConvert = (val: string) => {
  if (dialogVisibleLoading.value) return
  if (outputType.value === val) return
  outputType.value = val
  if (initTxt.value) return
}
// 上传文件
const uploadFiles = (postFile: File, val: boolean) => {
  if (!postFile) return
  const name = postFile.name
  const splitName = name.split('.')
  downloadName.value = splitName[0] + '.' + toType.value
  percentage.value = 0
  file.value = postFile
  upload(postFile, val)
}
// 开始转换
const upload = (rawFile: any, val: boolean = false) => {
  const data = new FormData()
  data.append('file', rawFile)
  fileUpload(data, val)
}
// 数据提取
const fileUpload = (data: any, val: boolean) => {
  dialogVisibleLoading.value = val
  post('/api/idp/api-file-resolve-api', data, {}, {
    timeout: 3600000,
    headers: {
      'API_KEY': license
    }
  } as any).then(async (res: any) => {
    if (res && res.data.code === 200) {
      init.value = false
      
      const downFileUrl = res.data.data.downFileUrl
      const response = await fetch(downFileUrl)
      const resNameArray = downFileUrl.split('.')
      const resType = resNameArray[resNameArray.length - 1].toLowerCase()
      if (!response.ok || !response.body) {
        ElMessage.error('Network error or empty response body')
        return
      }
      if (resType.includes('zip')) {
        try {
          clearBlobUrls()
          const result = await extractFilesFromZip(response)
          extractFiles = result
          jsonResult.value = result.json
          imageData.value = result.images
          dpiScale.value = result.json.metrics[0].dpi / 72
          addAnnotations(result.json)
        } catch (error) {
          ElMessage.error('Failed to convert.')
          console.error(error)
        }
      }
    } else {
      ElMessage.error('Failed to convert.')
    }
    isDownload.value = false
    dialogVisibleLoading.value = false
  }).catch((err: any) => {
    enableChangeFile.value = true
    let data
    dialogVisibleLoading.value = false
    if (err && err.request.responseType === 'blob') {
      const reader = new FileReader()
      reader.readAsText(err.data, 'utf-8')
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          data = JSON.parse(reader.result)
          ElMessage({
            message: data.msg.split(':')[1],
            type: 'error',
            duration: 5000,
            customClass: 'upload'
          })
        }
      }
    } else {
      ElMessage({
        message: 'Failed to convert.',
        type: 'error',
        duration: 2000,
        customClass: 'upload'
      })
    }
  })
}
// 解压缩包获取结果
const extractFilesFromZip = async (response: Response): Promise<ExtractedFiles> => {
  const result: ExtractedFiles = {
    json: null,
    images: [],
    fileBlobs: {}
  }
  
  try {
    const zipBuffer = await response.arrayBuffer()
    const zip = new JSZip()
    await zip.loadAsync(zipBuffer)

    const jsonZip = new JSZip()
    const mdZip = new JSZip()

    let jsonZipName = ''
    let mdZipName = ''

    for (const [fullPath, fileEntry] of Object.entries(zip.files)) {
      if (fileEntry.dir) continue

      const fileName = fullPath.split('/').pop()!
      const extension = fileName.toLowerCase().split('.').pop() || ''

      if (fileName.toLowerCase().endsWith('.json')) {
        result.json = JSON.parse(await fileEntry.async('text'))

        jsonZip.file(fullPath, await fileEntry.async('blob'))
        jsonZipName = fileName.replace('.json', '.zip')
      }
      else if (/\.(png|jpg|jpeg)$/i.test(fileName)) {
        const blob = await fileEntry.async('blob')
        result.images.push({
          fullPath: fullPath,
          name: fileName,
          blob
        })

        const blobBuffer = await blob.arrayBuffer()
        jsonZip.file(fullPath, blobBuffer)
        mdZip.file(fullPath, blobBuffer)
      }
      else if (fileName.toLowerCase().endsWith('.md')) {
        mdZip.file(fullPath, await fileEntry.async('blob'))
        mdZipName = fileName.replace('.md', '.zip')
      }

      if (['txt'].includes(extension)) {
        const blob = await fileEntry.async('blob')
        result.fileBlobs[extension] = { name: fileName, blob }
      }
    }

    // 生成压缩包blob
    const generateZipBlob = async (zipInstance: JSZip, name: string) => {
      if (Object.keys(zipInstance.files).length > 0) {
        const blob = await zipInstance.generateAsync({ type: 'blob' })
        return { name, blob }
      }
      return null
    }

    // 生成各种类型的压缩包
    const jsonZipData = await generateZipBlob(jsonZip, jsonZipName)
    const mdZipData = await generateZipBlob(mdZip, mdZipName)

    if (jsonZipData) result.fileBlobs.json = jsonZipData
    if (mdZipData) result.fileBlobs.md = mdZipData

    return result
  } catch (error) {
    console.error('ZIP处理错误:', error)
    throw error
  }
}
// 渲染解析块
const addAnnotations = (json: any) => {
  json.result.detail.forEach((element: any) => {
    docViewer.addAnnotations({
      type: 'custom',
      pageIndex: element.page_id - 1,
      borderWidth: 1,
      borderColor: '#396FFA',
      rect: {
        left: element.position[0] / dpiScale.value,
        top: element.position[1] / dpiScale.value,
        right: element.position[2] / dpiScale.value,
        bottom: element.position[5] / dpiScale.value
      },
      selectedStyle: {
        border: '2px solid #396FFA',
        background: '#396FFA14'
      }
    })
  })
}
const getImageSrc = (item: any) => {
  const targetImg = imageData.value.find((img: FileData) => img.fullPath === item.image_url)
  let imageUrl = ''
  if (targetImg && targetImg.blob) {
    imageUrl = getBlobUrl(targetImg.fullPath, targetImg.blob)
  }
  return imageUrl
}
const renderTableContent = (rawHtml: string) => {
  return sanitizeTableHtml(rawHtml || '')
}
/** 创建 Blob URL 并做缓存 */
const getBlobUrl = (fullPath: string = '', blob: Blob) => {
  if (!blobUrlCache.has(fullPath)) {
    blobUrlCache.set(fullPath, URL.createObjectURL(blob))
  }
  return blobUrlCache.get(fullPath)!
}
/** 释放旧缓存 */
const clearBlobUrls = () => {
  for (const url of blobUrlCache.values()) {
    URL.revokeObjectURL(url)
  }
  blobUrlCache.clear()
}
// 根据选择的格式下载文件
const downloadFile = () => {
  dialogVisible.value = false
  isDownload.value = true
  const file = extractFiles.fileBlobs[toType.value as keyof typeof extractFiles.fileBlobs]
  const blobUrl = getBlobUrl(file.name, file.blob)
  downloadClick(blobUrl, file.name)
}
// 下载文件
const downloadClick = (blobUrl: string, filename: string) => {
  const a = document.createElement('a')
  if (!a.click) {
    throw new Error('DownloadManager: "a.click()" is not supported.')
  }
  a.href = blobUrl
  a.target = '_parent'
  if ('download' in a) {
    a.download = filename
  }
  (document.body || document.documentElement).appendChild(a)
  a.click()
  a.remove()
}
const renderedContent = (val: string) => {
  return sanitizeHtml(md.render(val || ''))
}
const imageToPDF = (file: File) => {
  return new Promise<File>((resolve, reject) => {
    const img = new Image()
    const imgUrl = URL.createObjectURL(file)
    
    img.onload = function() {
      try {
        // 计算图片像素面积
        const pixelArea = img.width * img.height
        const maxPixelArea = 4194304 // 最大像素面积
        
        let finalWidth = img.width
        let finalHeight = img.height
        
        // 如果像素面积超过限制，计算缩放因子
        if (pixelArea > maxPixelArea) {
          // 计算缩放因子：sqrt(当前面积 / 最大面积)
          const scaleFactor = Math.sqrt(pixelArea / maxPixelArea)
          
          // 缩放长和宽
          const scaledWidth = Math.round(img.width / scaleFactor)
          const scaledHeight = Math.round(img.height / scaleFactor)
          
          // 调整为28的倍数
          finalWidth = Math.round(scaledWidth / 28) * 28
          finalHeight = Math.round(scaledHeight / 28) * 28
          
          // 确保调整后的尺寸至少为28的倍数的最小值
          finalWidth = Math.max(finalWidth, 28)
          finalHeight = Math.max(finalHeight, 28)
        }
        
        // 计算PDF页面尺寸（mm）
        const pdfWidthMM = finalWidth * 0.264583 // px转mm (96dpi)
        const pdfHeightMM = finalHeight * 0.264583

        // 使用自定义设置创建PDF
        const pdf = new jsPDF({
          orientation: img.width > img.height ? 'landscape' : 'portrait',
          unit: 'mm',
          format: [pdfWidthMM, pdfHeightMM] // 使用调整后的尺寸
        })
        
        const pageWidth = pdf.internal.pageSize.getWidth()
        const pageHeight = pdf.internal.pageSize.getHeight()
        
        // 计算图片在PDF中的尺寸（转换为mm）
        const imgWidthMM = finalWidth * 0.264583 // px转mm (96dpi)
        const imgHeightMM = finalHeight * 0.264583
        
        // 如果图片比页面大，按比例缩放（理论上不会发生，但保留安全机制）
        let displayWidth = imgWidthMM
        let displayHeight = imgHeightMM
        
        if (imgWidthMM > pageWidth || imgHeightMM > pageHeight) {
          const widthRatio = pageWidth / imgWidthMM
          const heightRatio = pageHeight / imgHeightMM
          const ratio = Math.min(widthRatio, heightRatio, 1)
          displayWidth = imgWidthMM * ratio
          displayHeight = imgHeightMM * ratio
        }
        
        const x = (pageWidth - displayWidth) / 2
        const y = (pageHeight - displayHeight) / 2
        
        // 创建Canvas用于高质量渲染
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')!
        
        // 设置Canvas尺寸为最终尺寸
        canvas.width = finalWidth
        canvas.height = finalHeight
        
        // 高质量绘制（如果尺寸有变化，会进行缩放）
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, finalWidth, finalHeight)
        
        // 获取高质量图片数据
        const nameArray = file.name.split('.')
        const imageType = nameArray.pop()?.toUpperCase() || 'JPEG'
        const dataUrl = canvas.toDataURL(`image/${imageType.toLowerCase()}`, 1.0)
        
        // 添加图片到PDF
        pdf.addImage({
          imageData: dataUrl,
          x: x,
          y: y,
          width: displayWidth,
          height: displayHeight,
          compression: 'MEDIUM', // 可以尝试 'NONE', 'FAST', 'MEDIUM', 'SLOW'
          rotation: 0,
          alias: 'alias'
        })
        
        URL.revokeObjectURL(imgUrl)
        
        // 生成PDF文件
        const pdfBlob = pdf.output('blob')
        const pdfFile = new File([pdfBlob], nameArray[0] + '.pdf', { type: 'application/pdf' })
        
        resolve(pdfFile)
      } catch (error) {
        URL.revokeObjectURL(imgUrl)
        reject(error)
      }
    }
    
    img.onerror = () => {
      URL.revokeObjectURL(imgUrl)
      reject(new Error('图片加载失败'))
    }
    
    img.src = imgUrl
  })
}
</script>
<style lang="scss" scoped>
@keyframes translateLeftRow {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(6px);
  }
  100% {
    transform: translateY(0);
  }
}
@keyframes rotate360 {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
@keyframes translateTopRow {
  0% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(6px);
  }
  100% {
    transform: translateX(0);
  }
}
.contactBtn {
  position: relative;
  background: linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
}
:deep(.mt-24px) {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-thumb {
    border-radius:10px;
    background: #232748;
  }
  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }
}
.history {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
    display: block;
  }
  &::-webkit-scrollbar-thumb {
    border-radius:10px;
    background: #232748;
  }
  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }
}
:deep() {
  table,
  th,
  td {
    border-color: #999;
    border-width: 1px !important;
  }
}
.borderActive {
  :deep() {
    table,
      th,
      td {
        border-color: #396FFA;
      }
  }
}
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
:deep(.el-switch) {
  &.is-checked .el-switch__core {
    background: #396FFA;
  }
}
:deep(.el-select.extractSelect) {
  .el-select__wrapper {
    max-width: 200px;
    &.is-focused {
      box-shadow: 0 0 0 1px #396FFA inset;
    }
  }
  input {
    height: 30px;
  }
}
.shadow {
  box-shadow: 0px 4px 32px 0px #8195C852;
  &::after {
    content: "";
    position: absolute;
    top: 72px;
    left: -6px;
    border-top: 6px solid transparent;
    border-right: 6px solid white;
    border-bottom: 6px solid transparent;
  }
}
.progress-bar {
  top: 0;
  left: 0;
  z-index: 1;
  width: 100%;
  max-width: 0;
  height: 100%;
  position: absolute;
  border-radius: 6px;
  transition: width .6s ease;
}
.login:hover {
  .decs {
    animation: none;
    display: inline-block !important;
  }
}
.decs {
  animation: translateLeftRow 1s ease-in-out infinite;
  &::after {
    content: '';
    top: -9px;
    right: 24px;
    position: absolute;
    border-bottom: 10px solid #596177;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
  }
}
.free {
  box-shadow: 2px 6px 18px 0px #00000033;
  &::after {
    content: '';
    bottom: -10px;
    left: 50%;
    position: absolute;
    border-top: 10px solid white;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
  }
}
:deep(.el-overlay-dialog) {
  .el-dialog {
    .loading {
      animation: rotate360 2s linear infinite;
    }
    .el-dialog__header {
      display: none;
    }
    .el-dialog__body {
      display: flex;
      word-break: initial;
      flex-direction: column;
    }
  }
}
:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  flex-direction: row;
  .el-radio {
    height: 16px;
    &:hover {
      background: transparent;
    }
    &:nth-child(3), &:nth-child(4) {
      margin-top: 20px;
    }
    width: 50%;
    margin-right: 0;
    color: #232748;
    .el-radio__inner {
      width: 12px;
      height: 12px;
      background: transparent;
      border: 1.5px solid #666666;
    }
    .el-radio__input.is-checked + .el-radio__label {
      color: #232748;
    }
    .el-radio, .el-radio__input {
      white-space: normal !important
    }
    .el-radio__input.is-checked {
      .el-radio__inner {
        border-color: #1460F3;
        background-color: #1460F3;
        &::after {
          width: 5px;
          height: 5px;
          background-color: white !important;
        }
      }
    }
  }
}
:deep(.upload) {
  .el-upload {
    width: 100%;
    .el-button {
      width: 100%;
      color: #94969D;
      padding: 12px 16px;
      border-color: #E1E3E8;
      &:hover {
        background-color: unset;
        border-color: #396FFA;
      }
      &:focus {
        background-color: unset;
      }
      span {
        display: flex;
        font-size: 14px;
        font-weight: 400;
        line-height: 18px;
        align-items: center;
        svg { 
          margin-right: 8px;
        }
      }
    }
    .el-button.is-disabled {
      color: #CCCCCC;
      border-color: #E1E3E8;
      background-color: #F0F1F2;
    }
  }
  &.disabled .el-upload {
    pointer-events: none;
  }
  .el-upload-list {
    .el-upload-list__item {
      font-size: 16px;
      margin-top: 16px;
      line-height: 24px;
      &:hover {
        background-color: unset;
      }
      .el-upload-list__item-name {
        padding-left: 0;
        cursor: default;
        color: #232748;
        margin-bottom: 4px;
        &:hover {
          color: #232748;
        }
        .el-icon-document {
          width: 20px;
          height: 20px;
          vertical-align: middle;
          &::before {
            content: none;
          }
        }
      }
      &.is-success .el-upload-list__item-name .el-icon-document {
        flex-shrink: 0;
        background: url('~/assets/images/support/success.svg');
      }
      &.is-fail .el-upload-list__item-name {
        color: #94969D;
        .el-icon-document {
          background: url('~/assets/images/support/fail.svg');
        }
      }
      .el-progress-bar {
        .el-progress-bar__outer {
          height: 6px !important;
          background-color: #F3F6FF;
          border-radius: 0;
          .el-progress-bar__inner {
            border-radius: 0;
            background-color: #396FFA;
          }
        }
      }
      .el-progress__text {
        top: -20px;
        font-size: 16px !important;
        line-height: 24px;
        color: #396FFA;
      }
      .el-icon-close {
        display: inline-block;
        width: 16px;
        height: 16px;
        background: url('~/assets/images/support/delete.svg');
        &::before {
          content: none;
        }
      }
      &.is-uploading .el-icon-close {
        display: none;
      }
      &.is-uploading:hover .el-icon-close {
        display: inline-block;
      }
      .el-icon-close-tip {
        display: none;
      }
      .el-upload-list__item-status-label {
        display: none;
      }
      &.is-ready .el-icon-document,
      &.is-uploading .el-icon-document {
        display: none;
      }
    }
  }
}
.extract {
  float: right;
  margin-top: 80px;
  padding-top: 16px;
  padding-left: 16px;
padding-right: 16px;
  background: #030D26;
}
:deep(.jv-container) {
  width: 100%;
  float: right;
  border: none;
  overflow: auto;
  font-size: 16px;
  color: #01fef4;
  line-height: 20px;
  padding-left: 10px;
  white-space: nowrap;
  background: #030D26;
  font-family: 'Encode Sans';
  height: calc(100vh - 216px);
  &::-webkit-scrollbar{
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-thumb{
    border-radius:10px;
    background: rgba(255, 255, 255, 0.32);
  }
  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }
  .jv-more {
    display: none;
  }
  &.jv-dark {
    background: transparent;
    .jv-ellipsis {
      display: inline-block;
      line-height: 0.9;
      font-size: 0.9em;
      padding: 0px 4px 2px 4px;
      border-radius: 3px;
      vertical-align: 2px;
      cursor: pointer;
      user-select: none;
    }
    .jv-button {
      color: #49b3ff;
    }
    .jv-ke {
      margin-right: 6px;
      color: #FFD686;
    }
    .jv-push {
      color: #fff;
    }
    .jv-array {
      color: #6BBF69;
    }
    .jv-boolean {
      color: #6BBF69;
    }
    .jv-function {
      color: #067bca;
    }
    .jv-item {
      &.jv-number {
        color: #6BBF69;
      }
      &.jv-array {
        color: #6BBF69;
      }
    }
    .jv-key {
      color: #FFD686;
    }
    .jv-number-float {
      color: #fc1e70;
    }
    .jv-number-integer {
      color: #fc1e70;
    }
    .jv-object {
      color: white;
    }
    .jv-undefine {
      color: #e08331;
    }
    .jv-string {
      color: #FFA15E;
      word-break: break-word;
      white-space: normal;
    }
    .jv-lin {
      color: #52ACF3;
      text-decoration: underline;
    }
    .jv-code {
      padding: 30px 0;
      .jv-toggle {
        color: #067bca;
        :before {
          padding: 0px 2px;
          border-radius: 2px;
        }
        :hover {
          :before {
            background: rgb(242, 5, 5);
          }
        }
      }
    }
  }
}
:deep(.katex-html) {
  position: absolute;
  clip: rect(1px, 1px, 1px, 1px);
  padding: 0;
  border: 0;
  height: 1px;
  width: 1px;
  overflow: hidden;
}
</style>
<style lang="scss">
.md-content {
  h1, h2, h3, h4 {
    font-weight: 600;
    line-height: var(--line-height-headings);
  }

  h1 { font-size: var(--font-size-h1); }
  h2 { font-size: var(--font-size-h2); }
  h3 { font-size: var(--font-size-h3); }
  h4 { font-size: var(--font-size-h4); }
}

.el-popper.is-pure.is-light.el-select__popper {
  .el-select-dropdown__item.is-selected {
    color: #396FFA;
    font-weight: 600;
  }
}
.el-popper.is-dark {
  border: none;
  padding: 8px 8px 12px;
  background: #596177;
  box-shadow: 2px 6px 18px 0px #00000033;
  .popper__arrow {
    border-top-color: #596177;
    &::after {
      border-top-color: #596177;
    }
  }
  .el-popper__arrow {
    display: inherit;
    &::before {
      background-color: #596177;
    }
  }
}
.el-message {
  min-width: unset;
  top: 112px !important;
  padding: 8px 16px;
  border-color: #F871714D;
  background-color: #FBEDED;
  .el-message__content {
    font-family: 'Encode Sans';
    font-size: 16px;
    line-height: 24px;
    color: #000;
  }
  .el-icon-error {
    color: #F87171;
    font-size: 20px;
  }
}
.el-message--error {
  .el-message__content {
    color: #F87171;
  }
}
</style>
