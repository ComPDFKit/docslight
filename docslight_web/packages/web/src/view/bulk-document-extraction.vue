<template>
  <div class="relative flex bg-[#F2F2F2] <lg:(bg-[#F3F6FF] h-100vh)" @keydown="handleKeyDown">
    <div class="fixed z-2 flex items-center border-b border-[#E1E3E8] bg-white w-full py-22px pl-32px">
      <div class="text-20px leading-28px text-brand-0 mr-12px">{{ t('common.ide') }}</div>
      <div class="flex rounded-8px text-xs p-4px bg-[#EBF1FE]">
        <a href="/single-document-extraction" class="flex px-12px py-4px items-center rounded-4px cursor-pointer">
          <Document class="mr-8px" />{{ t('bulkExtract.single') }}
        </a>
        <a class="flex px-12px py-4px items-center rounded-4px cursor-pointer bg-white">
          <Bulk class="mr-8px" />{{ t('bulkExtract.bulk') }}
        </a>
      </div>
    </div>

    <div class="<lg:hidden w-full h-[calc(100vh-80px)] mt-80px">
      <el-splitter>
        <el-splitter-panel size="50%">
          <div v-if="!fileList.length" class="w-full h-full p-20px">
            <div @drop.prevent="onDrop" @dragover.prevent="dragover = true" @dragleave.prevent="dragover = false" :class="dragover && 'bg-[#F3F6FF]'" class="w-full h-full rounded-12px border-dashed border-1 border-brand-2 flex justify-center items-center flex-col">
              <div class="cursor-pointer relative text-sm">
                <div @click="input.click" class="w-300px rounded-6px mx-auto mt-106px cursor-pointer bg-[#396FFA] text-white text-sm font-600 py-10px px-12px flex items-center justify-center hover:bg-[#244FF0]">
                  <Upload class="mr-12px w-20px h-20px" />
                  {{ t('knowledgeBases.dataset.selectFile[0]') }}
                </div>
                <div class="text-center my-8px text-xs text-brand-3">
                  {{ t('knowledgeBases.dataset.selectFile[1]') }}
                </div>
                <div class="text-center text-xs text-brand-3">
                  {{ t('knowledgeBases.dataset.selectFile[2]') }}
                </div>
              </div>
              <input ref="input" class="hidden" type="file" name="file" accept=".pdf, .png, .jpg, .jpeg" @change="handleChange" multiple>
            </div>
          </div>

          <div v-else class="w-full h-full">
            <BulkFileList :fileList="fileList" :processStatus="processStatus" :taskId="taskId" @changeExtractStatus="changeExtractStatus" />
          </div>
        </el-splitter-panel>

        <el-splitter-panel size="50%">
          <div class="w-full h-full flex flex-col justify-between bg-white">
            <div class="w-full h-full border-b border-[#E1E3E8] p-40px overflow-y-auto">
              <div class="flex items-center mb-20px">
                <span @click="menuActive = 0" class="text-14px leading-20px cursor-pointer hover:text-[#2E59C8]" :class="menuActive === 0 ? 'text-[#396FFA]' : 'text-[#94969D]'">{{ t('bulkExtract.customExtract') }}</span>
                <ArrowRight class="mx-12px" />
                <span @click="menuActive = 1" class="text-14px leading-20px cursor-pointer hover:text-[#2E59C8]" :class="{ 'text-[#396FFA]': menuActive === 1, 'text-[#94969D]': menuActive !== 1, 'text-[#CCC] pointer-events-none': !processStatus }">{{ t('bulkExtract.processingProgress') }}</span>
              </div>

              <div v-show="menuActive === 0" class="py-20px">
                <p class="mb-12px text-20px leading-28px font-600 text-[#43474D]">{{ t('bulkExtract.customExtract') }}</p>
                <div class="mb-24px text-16px leading-24px text-[#94969D] flex items-center">
                  {{ t('bulkExtract.customExtractDesc') }}
                  <div class="flex ml-8px relative">
                    <el-tooltip popper-class="tip-item" effect="dark" :content="t('singleExtract.enter')" placement="top">
                      <Question class="cursor-pointer" />
                    </el-tooltip>
                  </div>
                </div>
                <div class="mb-20px">
                  <el-popover v-model:visible="schemaShow" trigger="" placement="top" popper-class="schema-popover animation" width="320px">
                    <template #reference>
                      <div class="text-black text-16px leading-24px mb-20px font-500 mr-16px relative w-fit whitespace-nowrap">{{ t('template.title')  }}:</div>
                    </template>
                    <template #default>
                      <div class="bg-white float-up-down text-brand-3 py-20px px-12px">
                        <CloseSchema @click="schemaShow = false" class="absolute top-12px right-10px cursor-pointer" />
                        <div class="text-sm font-600 flex items-center">
                          {{ t('template.title')  }}
                          <div class="rounded-10px bg-[#00CF85] py-2px px-8px text-14px leading-16px ml-12px text-white font">{{ t('common.newBadge') }}</div>
                        </div>
                        <div class="text-xs mt-8px">{{ t('template.feature')  }}</div>
                      </div>
                    </template>
                  </el-popover>
                  <div class="flex items-center">
                    <div class="flex justify-between w-full">
                      <div class="flex overflow-auto w-[calc(100%-49px)] tags-container" ref="tagsContainer">
                        <div :class="[
                            templateList[item].order - 1 && 'ml-4px',
                            'order-' + templateList[item].order,
                            activeTemplate === item && 'border-transparent bg-[#F3F6FF] border-b-2 border-b-brand-2 border-solid rounded-b-l-none template-item',
                          ]"
                          :contenteditable="!['Invoice', 'Order'].includes(item) && edit"
                          @dblclick="['Invoice', 'Order'].includes(item) || isDisableParams ? '' : (edit = true, dialogVisibleSetName = true, templateName = item)"
                          @click="changeActiveTemplate(item)"
                          class="text-brand-0 max-w-160px whitespace-nowrap w-fit py-4px px-8px rounded-6px border border-[#E1E3E8] outline-none cursor-pointer flex items-center"
                          v-for="(item, index) in Object.keys(templateList)"
                          :key="index">
                          <div v-show="templateList[item].canSave" class="min-w-4px h-4px rounded-1/2 bg-[#F87171] mr-4px"></div>
                          <el-tooltip popper-class="box-item" effect="dark" :content="item" placement="top">
                            <span class="overflow-hidden overflow-ellipsis">
                              {{ item }}
                            </span>
                          </el-tooltip>
                          <el-tooltip popper-class="box-item" effect="dark" :content="t('template.reset')" placement="top">
                            <Reset @click="resetTemplate" v-show="templateList[item].canSave" class="reset ml-4px cursor-pointer min-w-20px" />
                          </el-tooltip>
                          <DeleteTemplate @click="deleteTemplate" v-show="activeTemplate === item && !['Invoice', 'Order'].includes(item)" class="ml-4px cursor-pointer hidden delete min-w-16px" />
                        </div>
                      </div>
                      <div class="flex items-center shadowsTag">
                        <div class="relative">
                          <PulldownTem v-show="(Object.keys(templateList).length > 4)" @click.stop="isDisableParams ? '' : pullShow = !pullShow" class="cursor-pointer text-brand-3 pulldownTem" />
                          <div v-show="pullShow" class="rounded-4px p-4px shadowTemp absolute bg-white right-0 top-28px z-10">
                            <div @click="pulldownChange(item)" v-for="(item, index) in pullList" :key="index" class="text-brand-0 rounded-6px py-4px px-12px cursor-pointer whitespace-nowrap hover:bg-[#1460F31A]">
                              {{ item }}
                            </div>
                          </div>
                        </div>
                        <div class="h-16px w-1px bg-[#E1E3E8] mx-4px"></div>
                        <el-tooltip popper-class="box-item" effect="dark" :content="t('template.add')" placement="top">
                          <AddTemplate v-show="!(Object.keys(templateList).length === 7)" @click="isDisableParams ? '' : addTemplate()" class="cursor-pointer addTemplate" />
                        </el-tooltip>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="mb-12px py-8px text-16px leading-24px font-600 text-[#232748] flex items-center">
                  {{ t('bulkExtract.textField') }}
                  <div class="flex ml-8px relative">
                    <el-tooltip popper-class="tip-item" effect="dark" :content="t('template.fieldTip')" placement="top">
                      <Question class="cursor-pointer" />
                    </el-tooltip>
                  </div>
                </div>
                <div class="flex items-center mb-8px relative" :class="isDisableParams && 'cursor-not-allowed'">
                  <div v-show="addField" class="absolute h-40px z-2 w-full flex items-center" :class="isDisableParams && 'pointer-events-none'">
                    <div @click="focus('field')" class="w-full h-full pl-12px text-[#94969D] text-14px leading-20px flex items-center cursor-pointer"><IdpAdd class="mr-8px" />{{ t('bulkExtract.addField') }}</div>
                  </div>
                  <div v-show="fieldFocus" class="h-full flex items-center absolute right-16px z-2 w-14px">
                    <IdpDelete v-show="!['Invoice', 'Order'].includes(activeTemplate)" @click="deleteInput('field')" class="w-14px h-14px cursor-pointer" />
                  </div>
                  <el-select class="extractSelect" ref="inputField"
                    v-model="editableTabs.fieldsList" multiple filterable
                    @focus="fieldFocus = true" allow-create default-first-option
                    :reserve-keyword="false" popper-class="disable"
                    placeholder=" " style="min-width: 240px"
                    @keydown.capture.backspace="onBackspace($event, 'field')"
                    :disabled="isDisableParams"
                    @blur="blurInput('field')" @change="clearInput('field')">
                    <template v-if="editableTabs.fieldsList.length" #tag>
                      <template v-for="(item, index) in editableTabs.fieldsList" :key="index">
                        <el-popover v-model:visible="templateList[activeTemplate].keysPromptShow[index]" trigger="" placement="top" popper-class="schema-popover" width="320px">
                          <template #reference>
                            <el-tag class="custom" closable @click="isDisableParams ? '' : templateList[activeTemplate].keysPromptShow[index] = !templateList[activeTemplate].keysPromptShow[index], sourceContent = templateList[activeTemplate].keysTip[index]" @close="removeItem(index, 'field')">
                              <div class="flex items-center">
                                {{ item }}
                                <Tips v-show="templateList[activeTemplate].keysTip[index]" class="ml-4px" />
                              </div>
                            </el-tag>
                          </template>
                          <template #default>
                            <div class="bg-white text-brand-3 text-sm py-20px px-12px">
                              <CloseSchema @click="clearInputContent(index, 'field')" class="absolute top-12px right-10px cursor-pointer" />
                              <div class="flex items-center mb-8px">
                                <Tips class="mr-12px" />
                                {{ t('template.prompt')  }}
                              </div>
                              <el-input v-model="templateList[activeTemplate].keysTip[index]" type="textarea" :placeholder="t('template.prompt')"></el-input>
                              <div @click="templateList[activeTemplate].keysPromptShow[index] = false, ['Invoice', 'Order'].includes(activeTemplate) && Object.keys(templateList).length === 7 ? '' : templateList[activeTemplate].canSave = true" class="mt-8px rounded-6px w-144px py-10px font-500 text-sm text-white bg-brand-2 mx-auto text-center -mb-8px cursor-pointer">
                                {{ t('template.ok')  }}
                              </div>
                            </div>
                          </template>
                        </el-popover>
                      </template>
                    </template>
                  </el-select>
                </div>
                <div class="mt-20px mb-12px py-8px text-16px leading-24px font-600 text-[#232748] flex items-center">
                  {{ t('bulkExtract.tableHeader') }}
                  <div class="flex ml-8px relative">
                    <el-tooltip popper-class="tip-item" effect="dark" :content="t('template.tableTip')" placement="top">
                      <Question class="cursor-pointer" />
                    </el-tooltip>
                  </div>
                </div>
                <div class="flex items-center mb-16px relative" :class="isDisableParams && 'cursor-not-allowed'">
                  <div v-show="addTable" class="absolute h-40px z-2 w-full flex items-center" :class="isDisableParams && 'pointer-events-none'">
                    <div @click="focus('table')" class="w-full h-full pl-12px text-[#94969D] text-14px leading-20px flex items-center cursor-pointer"><IdpAdd class="mr-8px" />{{ t('bulkExtract.addHeader') }}</div>
                  </div>
                  <div v-show="tableFocus" class="h-full flex items-center absolute right-16px z-2 w-14px">
                    <IdpDelete v-show="!['Invoice', 'Order'].includes(activeTemplate)" @click="deleteInput('table')" class="w-14px h-14px cursor-pointer" />
                  </div>
                  <el-select class="extractSelect" ref="inputTable"
                    v-model="editableTabs.tableList" multiple filterable
                    @focus="tableFocus = true" allow-create default-first-option
                    :reserve-keyword="false" popper-class="disable"
                    placeholder=" " style="min-width: 240px"
                    @keydown.capture.backspace="onBackspace($event, 'table')"
                    :disabled="isDisableParams"
                    @blur="blurInput('table')" @change="clearInput('table')">
                    <template v-if="editableTabs.tableList.length" #tag>
                      <template v-for="(item, index) in editableTabs.tableList" :key="index">
                        <el-popover v-model:visible="templateList[activeTemplate].tablePromptShow[index]" trigger="" placement="top" popper-class="schema-popover" width="320px">
                          <template #reference>
                            <el-tag class="custom" closable @click="isDisableParams ? '' : templateList[activeTemplate].tablePromptShow[index] = !templateList[activeTemplate].tablePromptShow[index], sourceContent = templateList[activeTemplate].tableHandlesTip[index]" @close="removeItem(index, 'table')">
                              <div class="flex items-center">
                                {{ item }}
                                <Tips v-show="templateList[activeTemplate].tableHandlesTip[index]" class="ml-4px" />
                              </div>
                            </el-tag>
                          </template>
                          <template #default>
                            <div class="bg-white text-brand-3 text-sm py-20px px-12px">
                              <CloseSchema @click="clearInputContent(index, 'table')" class="absolute top-12px right-10px cursor-pointer" />
                              <div class="flex items-center mb-8px">
                                <Tips class="mr-12px" />
                                {{ t('template.prompt')  }}
                              </div>
                              <el-input v-model="templateList[activeTemplate].tableHandlesTip[index]" type="textarea"></el-input>
                              <div @click="templateList[activeTemplate].tablePromptShow[index] = false, ['Invoice', 'Order'].includes(activeTemplate) && Object.keys(templateList).length === 7 ? '' : templateList[activeTemplate].canSave = true" class="mt-8px rounded-6px w-144px py-10px font-500 text-sm text-white bg-brand-2 mx-auto text-center -mb-8px cursor-pointer">
                                {{ t('template.ok')  }}
                              </div>
                            </div>
                          </template>
                        </el-popover>
                      </template>
                    </template>
                  </el-select>
                </div>
                <p class="mt-28px mb-20px text-16px leading-24px font-600 text-[#232748]">{{ t('bulkExtract.saveIn') }}</p>
                <el-radio-group v-model="editableTabs.outType" :disabled="!fileList.length || !!processStatus || process.uploadFailAmount === fileList.length">
                  <el-radio value="json">JSON</el-radio>
                  <el-radio value="txt">TXT</el-radio>
                  <el-radio value="excel">Excel</el-radio>
                  <el-radio value="csv">CSV</el-radio>
                </el-radio-group>
              </div>

              <div v-show="menuActive === 1" class="py-20px">
                <p class="mb-12px text-20px leading-28px font-600 text-[#43474D]">{{ t('bulkExtract.processingProgress') }}</p>
                <div class="mb-24px flex items-center">
                  <span class="text-16px leading-24px text-[#232748]">{{ t('bulkExtract.filesProcessed') }}<span :class="process.percent === 100 && 'text-[#00CF85]'">{{ process.percent }}%</span></span>
                  <div class="ml-16px relative">
                    <img src="../assets/images/icons/info.svg" @mouseenter="showInfo = true" @mouseleave="showInfo = false" />
                    <div class="info absolute bottom-26px left-0 p-4px w-177px bg-[#F2F3F5] border border-[#D9D9D9] rounded-4px text-11px leading-16px text-[#43474D]" :class="showInfo && '!opacity-100'">{{ t('bulkExtract.filesProcessedDesc') }}</div>
                  </div>
                </div>
                <div class="border border-[#E1E3E8]">
                  <div class="py-10px pr-20px bg-[#F5F7F9] flex justify-between">
                    <div class="flex-1 h-32px text-center border-r border-r-[#E1E3E8] text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.pending') }}</div>
                    <div class="flex-1 h-32px text-center border-r border-r-[#E1E3E8] text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.success') }}</div>
                    <div class="flex-1 h-32px text-center border-r border-r-[#E1E3E8] text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.fail') }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F] font-600">{{ t('bulkExtract.stopped') }}</div>
                  </div>
                  <div class="py-10px pr-20px flex justify-between">
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F]">{{ process.pendingAmount }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F]">{{ process.successAmount }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px" :class="process.failAmount ? 'text-[#F87171]' : 'text-[#52555F]'">{{ process.failAmount }}</div>
                    <div class="flex-1 h-32px text-center text-14px leading-32px text-[#52555F]">{{ process.stoppedAmount }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="processStatus !== 1" class="min-h-80px py-10px w-full flex justify-center items-center <lg:hidden">
              <div v-if="processStatus === 2" class="flex justify-center">
                <div @click="download" class="cursor-pointer w-187px py-12px text-center rounded-6px text-16px leading-24px font-500 bg-[#396FFA] text-white border border-[#396FFA] hover:opacity-80 mr-16px">{{ isDownloaded ? t('bulkExtract.reDownload') : t('knowledgeBases.dataset.download') }}</div>
                <div v-show="isDownloaded" @click="fileList.length = 0" class="cursor-pointer w-187px py-12px text-center rounded-6px text-16px leading-24px font-500 bg-white text-[#396FFA] border border-[#396FFA] hover:(bg-[#396FFA] text-white)">{{ t('bulkExtract.reUpload') }}</div>
              </div>
              <div @click="uploadClick" v-if="!processStatus" class="relative z-1 w-162px flex justify-center items-center rounded-2px" :class="(!taskId || process.noUploadAmount || process.uploadFailAmount === fileList.length) && 'opacity-40 pointer-events-none'">
                <button type="button" class="button">
                  <div class="points_wrapper">
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                    <i class="point"></i>
                  </div>
                  <span class="inner"><Light class="icon" />{{ t('bulkExtract.extractNow') }}</span>
                </button>
              </div>
              <template v-if="!isDisableParams && !processStatus">
                <div v-show="['Invoice', 'Order'].includes(activeTemplate)" @click="saveAsClick"
                  :class="saveAsClass"
                  class="font cursor-pointer font-600 w-162px py-12px text-center border-1 border-[#396FFA] text-[#396FFA] rounded-4px text-14px leading-16px hover:(bg-[#396FFA] text-white) ml-10px">
                  {{ t('template.saveAs')  }}
                </div>
                <div v-show="!['Invoice', 'Order'].includes(activeTemplate)" @click="saveConfigurationClick"
                  :class="saveConfigurationClass"
                  class="font cursor-pointer font-600 w-162px py-12px text-center border-1 border-[#396FFA] text-[#396FFA] rounded-4px text-14px leading-16px hover:(bg-[#396FFA] text-white) ml-10px">
                  {{ t('template.saveConfiguration')  }}
                </div>
              </template>
            </div>
          </div>
        </el-splitter-panel>
      </el-splitter>
      <!-- Save Template Dialog -->
      <el-dialog v-model="dialogVisibleSetName" align-center width="520px">
        <h3 class="text-sm font-bold text-[#43474D] py-4px mb-24px">
          {{ t('template.saveFieldName')  }}
        </h3>
        <div class="flex text-xs text-brand-0 font-600 mb-8px">
          <span class="text-[#FF5050] inline-block mr-4px font">*</span>
          {{ t('template.templateName')  }}
        </div>
        <el-input v-model="templateName" maxlength="50" :placeholder="t('template.customTemplateName')"></el-input>
        <div class="flex justify-center mt-24px">
          <div @click="dialogVisibleSetName = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)">
            {{ t('teamManagement.cancel') }}
          </div>
          <div @click="templateList[activeTemplate].addTemplate ? editTemplate() : saveTemplate(edit ? 'edit' : 'create')" :class="templateName ? 'cursor-pointer' : 'cursor-not-allowed opacity-60'" class="w-140px rounded-6px font-500 bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]">
            {{ t('teamManagement.ok') }}
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, provide, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Event } from 'vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import { get, post } from '../utils/request'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import { getSystemBaseUnit } from '../utils/tools'
import CloseSchema from "../components/closeSchema.vue"
import AddTemplate from "../components/images/AddTemplate.vue"
import DeleteTemplate from "../components/images/DeleteTemplate.vue"

interface FileEntry {
  id: string
  index: number
  name: string
  status: string
  file?: File
  failureReason?: string
  totalPageCount?: number
  currentPageCount?: number
}

GlobalWorkerOptions.workerSrc = '/lib/pdf.worker.min.mjs'
const CMAP_URL = '/lib/cmaps/'

const { t, locale } = useI18n()
const fileList = ref<FileEntry[]>([])
const dragover = ref(false)
const input = ref()
const menuActive = ref(0)
const addField = ref(true)
const addTable = ref(true)
const tableFocus = ref(false)
const fieldFocus = ref(false)
const saveShow = ref(true)

interface dataList {
  outType: string,
  tableList: Array<string>,
  fieldsList: Array<string>
}
const editableTabs = ref<dataList>({
  tableList: [],
  fieldsList: [],
  outType: 'json'
})
const inputField = ref()
const inputTable = ref()
const showInfo = ref(false)
const processStatus = ref(0) // 0:未开始处理 1:处理中 2:处理完成
const taskId = ref('')
const isDownloaded = ref(false)
const statusMap = {
  0: 'pending',
  1: 'processing',
  2: 'success',
  3: 'fail',
  4: 'delete',
  5: 'stopped'
}
const edit = ref(false)
const templateName = ref()
const sourceContent = ref('')
const schemaShow = ref(false)
const pullShow = ref(false)
const dialogVisibleSetName = ref(false)
const tagsContainer = ref<HTMLElement | null>(null)
interface TemplateData {
  keys: string[]
  tableHandles: string[]
  keysTip: string[]
  tableHandlesTip: string[]
  keysPromptShow: boolean[]
  tablePromptShow: boolean[]
  id: number,
  canSave: boolean
  addTemplate?: boolean
  order: number
  delete: boolean
}
interface DynamicTemplateList {
  [templateName: string]: TemplateData
}
const templateList = ref(<DynamicTemplateList>{})
const activeTemplate = ref('')
const process = computed(() => {
  let pending = 0
  let success = 0
  let fail = 0
  let uploadFail = 0
  let stopped = 0
  let percent = 0
  let noUpload = 0

  fileList.value.forEach(item => {
    if (item.status === 'pending' || item.status === 'processing') pending++
    if (item.status === 'success') success++
    if (item.status === 'fail') fail++
    if (item.status === 'uploadFail') uploadFail++
    if (item.status === 'stopped') stopped++
    if (!item.status || item.status === 'uploading') noUpload++
  })
  percent = Math.round((fileList.value.length - pending - uploadFail) / (fileList.value.length - uploadFail) * 100)

  if (processStatus.value === 1 && pending === 0) processStatus.value = 2

  return {
    percent,
    pendingAmount: pending,
    successAmount: success,
    failAmount: fail,
    uploadFailAmount: uploadFail,
    stoppedAmount: stopped,
    noUploadAmount: noUpload
  }
})

const isDisableParams = computed(() => !fileList.value.length || processStatus.value || process.value.uploadFailAmount === fileList.value.length)

const pullList = computed(() => {
  let arr: string[] = []
  if ([6,7,8].includes(Object.keys(templateList.value).length)) {
    arr = Object.keys(templateList.value).slice(-3)
  } else if ([5, 4].includes(Object.keys(templateList.value).length)) {
    arr = Object.keys(templateList.value).slice(-2)
  }
  return arr
})

watch(dialogVisibleSetName, (val: boolean, value: boolean) => {
  if (!val) {
    edit.value = false
  }
})

watch(() => fileList, (newList) => {
  if (!newList.value.length) {
    processStatus.value = 0
    menuActive.value = 0
    taskId.value = ''
    addField.value = true
    addTable.value = true
    editableTabs.value.fieldsList = []
    editableTabs.value.tableList = []
  }
}, { deep: true })

const onBackspace = (e: Event, type: 'table' | 'field') => {
  if (e.isComposing) return
  if (e.target.value) return
  removeItem(type === 'table' ? editableTabs.value.tableList.length - 1 : editableTabs.value.fieldsList.length - 1, type)
}

// 定义数据类型
interface TemplateKeys {
  [key: string]: string
}

interface TemplateSource {
  keys: TemplateKeys
  tableHandles: TemplateKeys
  name: string
  id: number
}

type TemplateList = Record<string, TemplateData>

const getTemplate = async (): Promise<void> => {

  try {
    // 并行请求，提高性能
    const [defaultTemplateResponse, templateListResponse] = await Promise.all([
      get('/api/idp/get-default-template'),
      get('/api/idp/get-template-list')
    ])

    const { data: { data: defaultTemplates = [] } } = defaultTemplateResponse
    const { data: { data: customTemplates = [] } } = templateListResponse

    // 创建模板的工厂函数
    const createTemplate = (item: TemplateSource, order: number): TemplateData => ({
      keys: Object.keys(item.keys || {}),
      tableHandles: Object.keys(item.tableHandles || {}),
      keysTip: Object.values(item.keys || {}),
      tableHandlesTip: Object.values(item.tableHandles || {}),
      keysPromptShow: Object.values(item.keys || {}).map(() => false),
      tablePromptShow: Object.values(item.tableHandles || {}).map(() => false),
      id: item.id,
      canSave: false,
      order: order,
      delete: false
    })

    // 合并模板数据
    const allTemplates: TemplateSource[] = [
      ...(defaultTemplates || []),
      ...(customTemplates || [])
    ]

    const templateMap = allTemplates.reduce((acc, item, index) => {
      if (item?.name) {
        acc.set(item.name, createTemplate(item, index + 1))
      }
      return acc
    }, new Map<string, TemplateData>())

    // 如果需要转换为对象但保持顺序
    templateList.value = Object.fromEntries(templateMap)

    // 设置当前激活的模板数据
    const currentTemplate = templateList.value[activeTemplate.value]
    if (currentTemplate) {
      editableTabs.value.fieldsList = currentTemplate.keys
      editableTabs.value.tableList = currentTemplate.tableHandles
    } else {
      console.warn(`Template "${activeTemplate.value}" not found`)
      // 设置默认值
      editableTabs.value.fieldsList = []
      editableTabs.value.tableList = []
    }

  } catch (error) {
    console.error('Failed to fetch templates:', error)
  }
}
const handleGlobalClick = () => {
  pullShow.value = false
}
onUnmounted(() => {
  removeEventListener('click', handleGlobalClick)
})
const scrollTo = async () => {
  await nextTick()
  if (tagsContainer.value) {
    const container = tagsContainer.value
    container.scrollLeft = container.scrollWidth
  }
}
onMounted(async () => {
  addEventListener('click', handleGlobalClick)
  const res = await get('/api/idp/get-task-list?pageNum=1&pageSize=1')
  if (!res.data || res.data.code !== 200) return
  getTemplate()
  const id = res.data.data.records[0].id
  const params = JSON.parse(res.data.data.records[0].params)

  const taskRes = await get(`/api/idp/get-task-file-list?taskId=${id}`)
  if (!taskRes.data || taskRes.data.code !== 200) return

  const files = taskRes.data.data
  const extracting = files.some((file: any) => file.status === 0 || file.status === 1)
  if (!extracting) return

  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    fileList.value.push({
      id: file.fileId,
      index: i,
      name: file.fileName,
      status: statusMap[file.status as keyof typeof statusMap],
      totalPageCount: file.fileSchedule.totalPageCount,
      currentPageCount: file.fileSchedule.currentPageCount
    })
  }

  if (params.keys.length) {
    editableTabs.value.fieldsList = params.keys
    addField.value = false
  }
  if (params.tableHandles.length) {
    editableTabs.value.tableList = params.tableHandles
    addTable.value = false
  }
  editableTabs.value.outType = params.outType
  setTimeout(() => {
    processStatus.value = 1
    menuActive.value = 1
    taskId.value = id
  })
})

const handleKeyDown = (event: any) => {
  if (event.keyCode === 9) {
    event.preventDefault()
  }
}

// 拖拽上传文件
const onDrop = async (e: DragEvent) => {
  activeTemplate.value = 'Invoice'
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  pushToFileList(files)
}

// 选择上传文件
const handleChange = async (e: any) => {
  activeTemplate.value = 'Invoice'
  const files = e.target.files
  if (!files || files.length === 0) return
  pushToFileList(files)
}

// 将上传的文件传入列表
const pushToFileList = async (files: FileList) => {
  addField.value = false
  addTable.value = false
  editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
  editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
  if (files.length > 32) {
    ElMessage.error(t('bulkExtract.fileLenTip'))
    return
  }
  const fileArr = Array.from(files)
  const totalSize = fileArr.reduce((sum, file) => sum + file.size, 0)
  const base = getSystemBaseUnit()

  if (totalSize > 100 * base * base) {
    ElMessage.error(t('bulkExtract.fileSizeTip'))
    return
  }

  const results = await Promise.all(fileArr.map(async file => {
    const nameArray = file.name.split('.')
    const fileType = nameArray[nameArray.length - 1].toLowerCase()

    if (fileType === 'pdf') {
      const isProtected = await checkPassword(file)
      return ({ file, isProtected })
    } else if (['jpg', 'png', 'jpeg'].includes(fileType)) {
      return Promise.resolve({ file, isProtected: false })
    } else {
      ElMessage.error(t('bulkExtract.notSupport'))
      return null
    }
  }))
  const filteredResults = results.filter(result => result !== null)
  const unprotectedFiles = filteredResults.filter(result => !result.isProtected)
  if (filteredResults.length !== unprotectedFiles.length) {
    ElMessage.warning(t('bulkExtract.encryptTip'))
  }

  for (let i = 0; i < unprotectedFiles.length; i++) {
    const { file } = unprotectedFiles[i]
    fileList.value.push({
      id: '',
      index: i,
      name: file.name,
      status: '',
      file
    })
  }
  upload()
}

// 检查文档是否受密码保护
const checkPassword = async (file: File): Promise<boolean> => {
  const arrayBuffer = await file.arrayBuffer()
  const parameters = {
    cMapUrl: CMAP_URL,
    cMapPacked: true,
    enableXfa: true,
    data: arrayBuffer
  }
  const loadingTask = getDocument(parameters)

  return new Promise((resolve, reject) => {
    loadingTask.promise.then(() => {
      resolve(false)
    }).catch(error => {
      if (error.name === 'PasswordException') {
        resolve(true)
      } else {
        console.warn('File ' + file.name + ': ' + error.message)
        resolve(false)
      }
    })
  })
}
provide('checkPassword', checkPassword)

// 接口创建任务并上传文件
const upload = async () => {
  const res = await get('/api/idp/create-task?taskType=EXTRACTION')
  if (!res.data.data) return
  taskId.value = res.data.data

  fileList.value.forEach((item, index: number) => {
    item.status = 'uploading'
    const formdata = new FormData()
    formdata.append('taskId', taskId.value)
    formdata.append('order', index as any)
    item.file && formdata.append('file', item.file, item.name)
    post('/api/idp/file-upload', formdata).then((res: any) => {
      if (res.data.code === 200) {
        item.id = res.data.data
        item.status = 'pending'
      } else {
        item.status = 'uploadFail'
        item.failureReason = t('bulkExtract.uploadFail')
      }
    }).catch((err: any) => {
      console.log(err)
    })
  })
}

// 点击开始提取
const uploadClick = async () => {
  if (!editableTabs.value.tableList.length && !editableTabs.value.fieldsList.length) {
    ElMessage.error(t('bulkExtract.fieldEmptyTip'))
    return
  }
  const res = await post('/api/idp/task-start', {
    'taskId': taskId.value,
    'keys': editableTabs.value.fieldsList,
    'tableHandles': editableTabs.value.tableList,
    // 'pages': [],
    'resolveType': 'all',
    'outType': editableTabs.value.outType,
    'enableOCR': false,
    'ocrLanguage': 'auto',
    'pdfPwd': '',
    keysDescribe: templateList.value[activeTemplate.value].keysTip,
    tableHandlesDescribe: templateList.value[activeTemplate.value].tableHandlesTip
  })
  if (res.data.code === 200 && res.data.message === 'success') {
    processStatus.value = 1
    menuActive.value = 1
  }
}

// 清除Tag
const deleteInput = async (type: 'table' | 'field') => {
  // 标记可保存
  templateList.value[activeTemplate.value].canSave = true
  
  const targetList = type === 'table' 
    ? editableTabs.value.fieldsList 
    : editableTabs.value.tableList

  // 情况1：如果对侧列表为空，需要删除整个模板
  if (targetList.length === 0) {
    await handleDeleteTemplate()
    return
  }
  
  // 情况2：否则只是清空当前类型的列表
  clearCurrentType(type)
}

// ========== 提取的核心函数 ==========

/**
 * 处理删除整个模板的逻辑
 */
const handleDeleteTemplate = async () => {
  try {
    // 确认删除
    await ElMessageBox.confirm(
      t('template.deleteTemplateTipOne'),
      '',
      {
        confirmButtonText: t('template.delete'),
        cancelButtonText: t('template.cancel'),
        type: 'warning'
      }
    )

    const currentTemplate = templateList.value[activeTemplate.value]
    let success = false

    if (currentTemplate.addTemplate) {
      // 本地添加的模板：直接删除
      delete templateList.value[activeTemplate.value]
      success = true
    } else {
      // 服务器模板：调用API删除
      const { data }: any = await post('/api/idp/delete-template', {
        id: currentTemplate.id
      })
      success = data.code === 200
      
      if (success) {
        delete templateList.value[activeTemplate.value]
      }
    }

    if (success) {
      // 切换到默认模板 'Invoice'
      changeActiveTemplate('Invoice')
      ElMessage.success(t('knowledgeBases.dataset.deleteSuccess'))
    } else {
      ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
    }

  } catch (error) {
    // 用户取消或API调用失败
    if (error !== 'cancel') {
      ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
    }
  }
}

/**
 * 清空指定类型（field或table）的数据
 */
const clearCurrentType = (type: 'table' | 'field') => {
  const template = templateList.value[activeTemplate.value]
  
  if (type === 'table') {
    // 清空表格相关数据
    template.tableHandles = []
    template.tableHandlesTip = []
    template.tablePromptShow = []
    editableTabs.value.tableList = []
    addTable.value = true
  } else {
    // 清空字段相关数据
    template.keys = []
    template.keysTip = []
    template.keysPromptShow = []
    editableTabs.value.fieldsList = []
    addField.value = true
  }
}
// 多选框失焦
const blurInput = (val: string) => {
  if (val === 'table') {
    inputTable.value.blur()
    editableTabs.value.tableList?.length ? '' : addTable.value = true
    setTimeout(() => {
      tableFocus.value = false
    }, 200)
  } else {
    inputField.value.blur()
    editableTabs.value.fieldsList?.length ? '' : addField.value = true
    setTimeout(() => {
      fieldFocus.value = false
    }, 200)
  }
}
// 多选框聚焦
const focus = (val: string) => {
  if (val === 'table') {
    inputTable.value.focus()
    addTable.value = false
  } else {
    inputField.value.focus()
    addField.value = false
  }
}
const clearInput = (val: string) => {
  saveShow.value = true
  templateList.value[activeTemplate.value].delete = true
  if (Object.keys(templateList.value).length >= 7 && ['Invoice', 'Order'].includes(activeTemplate.value)) {
    ElMessage.warning(t('template.max'))
    return
  } else {
    templateList.value[activeTemplate.value].canSave = true
  }
  if(val === 'table') {
    const newItems = editableTabs.value.tableList.filter(item =>
      !templateList.value[activeTemplate.value].tableHandles.includes(item)
    )
    templateList.value[activeTemplate.value].tableHandles.push(...newItems)
    templateList.value[activeTemplate.value].tableHandlesTip.push('')
    templateList.value[activeTemplate.value].tablePromptShow.push(false)
  } else {
    const newItems = editableTabs.value.fieldsList.filter(item =>
      !templateList.value[activeTemplate.value].keys.includes(item)
    )
    templateList.value[activeTemplate.value].keys.push(...newItems)
    templateList.value[activeTemplate.value].keysTip.push('')
    templateList.value[activeTemplate.value].keysPromptShow.push(false)
  }
  const handleInputFocus = async (type: 'table' | 'field') => {
    const isTable = type === 'table'

    // 获取对应的 DOM 元素和响应式变量
    const inputElement = isTable ? inputTable.value : inputField.value
    const focusFlag = isTable ? tableFocus : fieldFocus
    const list = isTable ? editableTabs.value.tableList : editableTabs.value.fieldsList
    const addFlag = isTable ? addTable : addField

    // 先失去焦点
    inputElement?.blur()

    // 使用 nextTick 确保 DOM 更新
    await nextTick()

    // 设置焦点状态并重新获取焦点
    focusFlag.value = true
    inputElement?.focus()

    // 检查列表长度，如果为空则关闭添加状态
    if (!list?.length) {
      addFlag.value = false
    }
  }

  // 使用
  setTimeout(() => {
    handleInputFocus(val as 'table' | 'field')
  })
}

// 下载所有文件结果
const download = () => {
  const loadingInstance = ElLoading.service()

  get(`/api/idp/down-all-files?taskId=${taskId.value}`, {
    responseType: 'blob'
  }).then(async (res: any) => {
    const disposition = res.headers['content-disposition']
    const regExp = /filename=([^;]*)/i
    const match = disposition.match(regExp)
    const filename = match ? match[1].replace(/"/g, '').trim() : 'compdf_ai-batch-extract-' + getCurrentDate()

    const blob = new Blob([res.data], { type: res.headers['content-type'] })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    isDownloaded.value = true
    loadingInstance.close()
  }).catch((err: any) => {
    console.log(err)
    loadingInstance.close()
    ElMessage.error(t('singleParse.downloadFailed'))
  })
}

// 获取当前日期 YYYYMMDD
const getCurrentDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  
  return `${year}${month}${day}`
}

// 子组件修改提取状态
const changeExtractStatus = (val: number) => {
  processStatus.value = val
}
// 添加模版
const addTemplate = async () => {
  const entries = Object.entries(templateList.value)
  const lastEntry = entries[entries.length - 1]
  if (lastEntry[1].addTemplate) {
    delete templateList.value[lastEntry[0]]
  }
  const baseName = t('template.customTemplate')
  const templateNames = Object.keys(templateList.value)

  // 提取所有已有模板的编号
  const numbers = templateNames
    .filter(name => name.startsWith(baseName))
    .map(name => {
      const match = name.match(new RegExp(`${baseName}(\\d+)`))
      return match ? parseInt(match[1], 10) : 0
    })
    .filter(num => !isNaN(num))

  // 计算下一个可用的编号
  const nextNum = numbers.length > 0 ? Math.max(...numbers) + 1 : 1
  activeTemplate.value = `${baseName}${nextNum}`
  templateList.value[`${baseName}${nextNum}`] = {
    keys: [],
    tableHandles: [],
    keysTip: [],
    tableHandlesTip: [],
    keysPromptShow: [],
    tablePromptShow: [],
    id: 0,
    canSave: false,
    addTemplate: true,
    order: Object.keys(templateList.value).length + 1,
    delete: false
  }
  addTable.value = true
  focus('field')
  await nextTick()
  if (tagsContainer.value) {
    const container = tagsContainer.value
    container.scrollLeft = container.scrollWidth
  }
  editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
  editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
}
const pulldownChange = (item: string) => {
  scrollTo()
  pullShow.value = false
  activeTemplate.value = item
  editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
  editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
  addTable.value = !!!editableTabs.value.tableList.length
  addField.value = !!!editableTabs.value.fieldsList.length
}
// 切换模板
const changeActiveTemplate = async (val: string) => {
  templateName.value = ''
  activeTemplate.value = val
  editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
  editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
  addField.value = !!!editableTabs.value.fieldsList.length
  addTable.value = !!!editableTabs.value.tableList.length
  await nextTick()
  if (tagsContainer.value) {
    const container = tagsContainer.value
    if (['Invoice', 'Order'].includes(val)) {
      container.scrollLeft = 0
    } else {
      container.scrollLeft = container.scrollWidth
    }
  }
}
// 删除字段或表格项
const removeItem = async (index: number, type: 'field' | 'table') => {
  // 1. 提取配置创建函数 - 消除重复
  if (isDisableParams.value) return
  const getConfig = (type: 'field' | 'table') => {
    const template = templateList.value[activeTemplate.value]
    const common = {
      templateKeys: type === 'field' ? template.keys : template.tableHandles,
      templateTips: type === 'field' ? template.keysTip : template.tableHandlesTip,
      templatePromptShow: type === 'field' ? template.keysPromptShow : template.tablePromptShow
    }
    
    return type === 'field' 
      ? {
          ...common,
          keys: editableTabs.value.fieldsList,
          length: editableTabs.value.fieldsList.length
        }
      : {
          ...common,
          keys: editableTabs.value.tableList,
          length: editableTabs.value.tableList.length
        }
  }

  // 2. 核心删除操作 - 单一职责
  const performDelete = (config: ReturnType<typeof getConfig>, index: number) => {
    // 删除模板相关数组项
    config.templateKeys.splice(index, 1)
    config.templateTips.splice(index, 1)
    config.templatePromptShow.splice(index, 1)

    // 条件性删除可编辑列表项
    const template = templateList.value[activeTemplate.value]
    if (template.addTemplate || template.delete) {
      config.keys.splice(index, 1)
    }

    // 设置保存标志
    template.canSave = true
  }

  // 3. 检查模板数量限制
  const checkTemplateLimit = () => {
    if (Object.keys(templateList.value).length >= 7 && ['Invoice', 'Order'].includes(activeTemplate.value)) {
      ElMessage.warning(t('template.max'))
      return false
    }
    return true
  }

  // 4. 主逻辑流程
  const totalItems = editableTabs.value.fieldsList.length + editableTabs.value.tableList.length

  // 情况A: 只剩一个项目时需要确认
  if (totalItems === 1) {
    try {
      if (['Invoice', 'Order'].includes(activeTemplate.value)) {
        await ElMessageBox.alert(
          t('template.deleteDefaultTemp'),
          '', 
          {
            confirmButtonText: t('template.ok'),
            type: 'warning'
          }
        )
        return
      }
      await ElMessageBox.confirm(
        t('template.deleteTemplateTipOne'),
        '', 
        {
          confirmButtonText: t('template.delete'),
          cancelButtonText: t('template.cancel'),
          type: 'warning'
        }
      )
      try {
        if (templateList.value[activeTemplate.value].addTemplate) {
          delete templateList.value[activeTemplate.value]
          changeActiveTemplate('Invoice')
        } else {
          const { data }: any = await post('/api/idp/delete-template', {
            id: templateList.value[activeTemplate.value].id
          })
          if (data.code === 200) {
            delete templateList.value[activeTemplate.value]
            changeActiveTemplate('Invoice')
            editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
            editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
            ElMessage.success(t('knowledgeBases.dataset.deleteSuccess'))
          } else {
            ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
          }
        }
      } catch {
        ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
      }

    } catch (cancel) {
      return
    }
  }
  // 情况B: 有多个项目，直接删除
  else {
    saveShow.value = true
    const config = getConfig(type)
    
    // 执行删除前检查限制
    if (!checkTemplateLimit()) return
    
    performDelete(config, index)
  }
}
// 清除输入内容
const clearInputContent = (index: number, type: 'table' | 'field') => {
  const template = templateList.value[activeTemplate.value]
  const isTable = type === 'table'

  const promptShowArray = isTable ? template.tablePromptShow : template.keysPromptShow
  const tipArray = isTable ? template.tableHandlesTip : template.keysTip

  // 更新状态
  promptShowArray[index] = false
  tipArray[index] = sourceContent.value
}

// 删除模板
const deleteTemplate = async () => {
  ElMessageBox.confirm(t('template.recovered'), '', {
    confirmButtonText: t('template.yes'),
    cancelButtonText: t('template.cancel'),
    type: 'warning'
  }).then(async () => {
    if (templateList.value[activeTemplate.value].addTemplate) {
      delete templateList.value[activeTemplate.value]
      changeActiveTemplate('Invoice')
    } else {
      try {
        const { data }: any = await post('/api/idp/delete-template', {
          id: templateList.value[activeTemplate.value].id
        })
        if (data.code === 200) {
          delete templateList.value[activeTemplate.value]
          changeActiveTemplate('Invoice')
          editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
          editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
          ElMessage.success(t('knowledgeBases.dataset.deleteSuccess'))
        } else {
          ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
        }
      } catch {
        ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
      }
    }
  }).catch(() => {
    addField.value = false
    inputField.value.focus()
  })
}
// 转换数据函数
const convertToObjectFormat = (data: TemplateData, value: 'create' | 'edit') => {
  const result = {
    keys: Object.fromEntries(
      data.keys.map((key: string, index: number) => [key, data.keysTip[index] || ''])
    ),
    tableHandles: Object.fromEntries(
      data.tableHandles.map((key: string, index: number) => [key, data.tableHandlesTip[index] || ''])
    ),
    id: data.id,
    name: templateName.value ? templateName.value : activeTemplate.value
  }
  if (value === 'edit') {
    result.id = data.id  
  }
  return result
}
// 修改 templateList 中的 key
const renameTemplate = (oldName: string, newName: string): void => {
  if (templateList.value[oldName]) {
    // 创建新的 templateList
    const newTemplateList: TemplateList = {}
    Object.entries(templateList.value).forEach(([key, value]) => {
      if (key === oldName) {
        newTemplateList[newName] = value
      } else {
        newTemplateList[key] = value
      }
    })
    templateList.value = newTemplateList
  }
}

const editTemplate = () => {
  const arr = Object.keys(templateList.value).filter(item => item !== activeTemplate.value)
  if (arr.includes(templateName.value)) {
    ElMessage.warning(t('template.exists'))
    return
  }
  renameTemplate(activeTemplate.value, templateName.value)
  dialogVisibleSetName.value = false
  activeTemplate.value = templateName.value
}
// 保存模板
const saveTemplate = async (value: 'create' | 'edit') => {
  if (!editableTabs.value.fieldsList.length && !editableTabs.value.tableList.length) return
  if (!templateName.value) return
  const arr = Object.keys(templateList.value).filter(item => item !== activeTemplate.value)
  if (arr.includes(templateName.value)) {
    ElMessage.warning(t('template.exists'))
    return
  }
  const date = templateList.value[activeTemplate.value]
  const result = convertToObjectFormat(date, value)
  const reqUrl = value === 'create' ? '/api/idp/add-template' : '/api/idp/update-template'
  try {
    const { data }: any = await post(reqUrl, result)
    if (data.code === 200) {
      dialogVisibleSetName.value = false
      ElMessage.success(t('template.saved'))
      if (value === 'edit') {
        renameTemplate(activeTemplate.value, templateName.value)
        activeTemplate.value = templateName.value
        templateList.value[activeTemplate.value].canSave = false
      } else {
        if (Object.keys(templateList.value).length >= 7 && activeTemplate.value === Object.keys(templateList.value).pop()) {
          ElMessage.warning(t('template.max'))
        }
        // 并行请求，提高性能
        const [defaultTemplateResponse, templateListResponse] = await Promise.all([
          get('/api/idp/get-default-template'),
          get('/api/idp/get-template-list')
        ])
    
        const { data: { data = [] } } = defaultTemplateResponse
        const { data: { data: custom = [] } } = templateListResponse
        const createTemplate = (item: TemplateSource, order: number): TemplateData => ({
          keys: Object.keys(item.keys),
          tableHandles: Object.keys(item.tableHandles),
          keysTip: Object.values(item.keys),
          tableHandlesTip: Object.values(item.tableHandles),
          keysPromptShow: Object.values(item.keys).map(() => false),
          tablePromptShow: Object.values(item.tableHandles).map(() => false),
          id: item.id,
          canSave: false,
          order: order,
          delete: false
        })
        const allTemplates: TemplateSource[] = [...(data || []), ...(custom || [])]
        const resultList = allTemplates.reduce((acc, item, index) => {
          if (item?.name) {
            acc[item.name] = createTemplate(item, index + 1)
          }
          return acc
        }, {} as TemplateList)
        templateList.value[templateName.value] = resultList[templateName.value]
        templateList.value[activeTemplate.value] = resultList[activeTemplate.value]
        activeTemplate.value = templateName.value
        editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
        editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
        await nextTick()
        if (tagsContainer.value) {
          const container = tagsContainer.value
          container.scrollLeft = container.scrollWidth
        }
      }
    } else {
      if (data.message === 'Template name already exists') {
        ElMessage.warning(t('template.exists'))
      } else if (data.message === 'The number of templates has reached the limit') {
        ElMessage.warning(t('template.limit'))
      } else {
        ElMessage.warning(data.message)
      }
    }
  } catch (error) {
    console.log(error)
  }
}
// 重置模板
const resetTemplate = async () => {
  if (templateList.value[activeTemplate.value].addTemplate) {
    addTemplate()
    return
  }
  ElMessageBox.confirm(t('template.resetTip'), '', {
    confirmButtonText: t('template.yes'),
    cancelButtonText: t('template.cancel'),
    type: 'warning'
  }).then(async () => {
    if (templateList.value[activeTemplate.value].addTemplate) {
      delete templateList.value[activeTemplate.value]
      changeActiveTemplate('Invoice')
    } else {
      try {
        // 并行请求，提高性能
        const [defaultTemplateResponse, templateListResponse] = await Promise.all([
          get('/api/idp/get-default-template'),
          get('/api/idp/get-template-list')
        ])

        const { data: { data = [] } } = defaultTemplateResponse
        const { data: { data: result = [] } } = templateListResponse
        const createTemplate = (item: TemplateSource, order: number): TemplateData => ({
          keys: Object.keys(item.keys),
          tableHandles: Object.keys(item.tableHandles),
          keysTip: Object.values(item.keys),
          tableHandlesTip: Object.values(item.tableHandles),
          keysPromptShow: Object.values(item.keys).map(() => false),
          tablePromptShow: Object.values(item.tableHandles).map(() => false),
          id: item.id,
          canSave: false,
          order: order,
          delete: false
        })
        const allTemplates: TemplateSource[] = [...(data || []), ...(result || [])]
        
        const sourceList = allTemplates.reduce((acc, item, index) => {
          if (item?.name) {
            acc[item.name] = createTemplate(item, index + 1)
          }
          return acc
        }, {} as TemplateList)
        templateList.value[activeTemplate.value] = sourceList[activeTemplate.value]
        editableTabs.value.fieldsList = templateList.value[activeTemplate.value].keys
        editableTabs.value.tableList = templateList.value[activeTemplate.value].tableHandles
        addField.value = !!!editableTabs.value.fieldsList.length
        addTable.value = !!!editableTabs.value.tableList.length
      } catch {
        ElMessage.error(t('knowledgeBases.dataset.deleteFail'))
      }
    }
  })
}
const saveAsClass = computed(() => {
  return templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) && Object.keys(templateList).length <= 7 ? '' : 'disable'
})
const saveAsClick = () => {
  if (templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) && Object.keys(templateList).length <= 7) {
   dialogVisibleSetName.value = true
  }
}
const saveConfigurationClass = computed(() => {
  return templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) ? '' : 'disable'
})
const saveConfigurationClick = () => {
  templateName.value = activeTemplate.value
  if (templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length)) {
    templateList.value[activeTemplate.value].addTemplate ? saveTemplate('create') : saveTemplate('edit')
  }
}
</script>

<style lang="scss" scoped>
.relative.flex.ml-8px:hover {
  .tip {
    display: block;
    background: #000000B2;
    box-shadow: 0px 4px 16px 0px #00299233;
    &::after {
      content: '';
      left: 45%;
      bottom: -10px;
      position: absolute;
      border-top: 10px solid #000000B2;
      border-left: 10px solid transparent;
      border-right: 10px solid transparent;
    }
    @media (min-width:930px) and (max-width: 1279.9px) {
      right: -10px;
      left: inherit;
      &::after {
        left: 87%;
      }
    }
  }
}

.tags-container {
  /* 核心：通过遮罩实现右侧阴影效果 */
  mask-image: linear-gradient(to right, 
    transparent 0%, 
    black 0%, 
    black calc(100% - 49px), 
    transparent 100%
  );
  -webkit-mask-image: linear-gradient(to right, 
    transparent 0%, 
    black 0%, 
    black calc(100% - 49px), 
    transparent 100%
  );
}

.order-1 {
  order: 1;
}

.order-2 {
  order: 2;
}

.order-3 {
  order: 3;
}

.order-4 {
  order: 4;
}

.order-5 {
  order: 5;
}

.order-6 {
  order: 6;
}

.order-7 {
  order: 7;
}

.order-8 {
  order: 8;
}

:deep(.el-select.extractSelect) {
  .el-select__wrapper {
    min-height: 40px;
    .el-select__selection {
      max-height: 54px;
      overflow: auto;
      .el-select__selected-item {
        .el-tag {
          color: #232748;
          background: #F3F6FF;
          .el-icon {
            color: #52555F;
            &:hover {
              background: #CED6E1;
            }
          }
        }
      }
    }
    &.is-focused {
      box-shadow: 0 0 0 1px #396FFA;
    }
  }
  .el-select__suffix {
    display: none;
  }
  &.setting .el-select__wrapper {
    padding-left: 12px;
    .el-select__suffix {
      display: inline-block;
    }
  }
}
.info {
  box-shadow: 0px 4px 4px 0px #00000033;
  transition: all 0.5s ease-out;
  opacity: 0;
  pointer-events: none;
}

.border-b-none {
  border-bottom: none;
}

.shadowTemp {
  box-shadow: 0px 4px 35px 0px #0029921A;
}

.w-fit {
  width: fit-content;
}

.template-item:hover {
  .delete {
    display: flex;
  }
}

:deep(.reset) {
  rect {
    display: none;
  }
  &:hover {
    rect {
      display: unset;
    }
  }
}

:deep(.addTemplate) {
  rect {
    display: none;
  }
  &:hover {
    rect {
      display: unset;
    }
  }
}

:deep(.pulldownTem) {
  rect {
    display: none;
  }
  &:hover {
    rect {
      display: unset;
    }
  }
}

.shadowField {
  box-shadow: 0px 4px 32px 0px #8195C852;
}
.disable {
  color: #94969D;
  cursor: not-allowed;
  border-color: #94969D;
  &:hover {
    color: #94969D;
    border-color: #94969D;
    background-color: #fff;
  }
}
/* From Uiverse.io by ilkhoeri */ 
.button {
  width: 100%;
  height: 40px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.25s ease;
  background: radial-gradient(65.28% 65.28% at 50% 100%, #7199FF, rgba(223, 113, 255, 0) 100%), linear-gradient(0deg, #396FFA, #396FFA);
  border-radius: 2px;
  border: none;
  outline: none;
}
.button::before,
.button::after {
  content: "";
  position: absolute;
  inset: var(--space);
  transition: all 0.5s ease-in-out;
  border-radius: calc(var(--round) - var(--space));
  z-index: 0;
}
.button::before {
  --space: 1px;
  background: linear-gradient(
    177.95deg,
    rgba(255, 255, 255, 0.19) 0%,
    rgba(255, 255, 255, 0) 100%
  );
}
.button::after {
  --space: 2px;
  background: radial-gradient(
      65.28% 65.28% at 50% 100%,
      #7199FF,
      rgba(223, 113, 255, 0) 100%
    ),
    linear-gradient(0deg, #396FFA, #396FFA);
}
.button:active {
  transform: scale(0.95);
}
.button:focus svg.icon {
  fill: white;
}
.button:hover svg.icon {
  fill: transparent;
  animation:
    dasharray 1s linear forwards,
    filled 0.1s linear forwards 0.95s;
}
@keyframes dasharray {
  from {
    stroke-dasharray: 0 0 0 0;
  }
  to {
    stroke-dasharray: 68 68 0 0;
  }
}
@keyframes filled {
  to {
    fill: white;
  }
}
.inner {
  z-index: 2;
  gap: 6px;
  position: relative;
  width: 100%;
  color: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  transition: color 0.2s ease-in-out;
}
.inner svg.icon {
  width: 18px;
  height: 18px;
  transition: fill 0.1s linear;
}
.points_wrapper {
  overflow: hidden;
  width: 100%;
  height: 100%;
  pointer-events: none;
  position: absolute;
  z-index: 1;
}

.points_wrapper .point {
  bottom: -10px;
  position: absolute;
  animation: floating-points infinite ease-in-out;
  pointer-events: none;
  width: 4px;
  height: 4px;
  background-color: #fff;
  border-radius: 9999px;
}
@keyframes floating-points {
  0% {
    transform: translateY(0);
  }
  85% {
    opacity: 0;
  }
  100% {
    transform: translateY(-55px);
    opacity: 0;
  }
}
.points_wrapper .point:nth-child(1) {
  left: 10%;
  opacity: 1;
  animation-duration: 2.35s;
  animation-delay: 0.2s;
}
.points_wrapper .point:nth-child(2) {
  left: 30%;
  opacity: 0.7;
  animation-duration: 2.5s;
  animation-delay: 0.5s;
}
.points_wrapper .point:nth-child(3) {
  left: 25%;
  opacity: 0.8;
  animation-duration: 2.2s;
  animation-delay: 0.1s;
}
.points_wrapper .point:nth-child(4) {
  left: 44%;
  opacity: 0.6;
  animation-duration: 2.05s;
}
.points_wrapper .point:nth-child(5) {
  left: 50%;
  opacity: 1;
  animation-duration: 1.9s;
}
.points_wrapper .point:nth-child(6) {
  left: 75%;
  opacity: 0.5;
  animation-duration: 1.5s;
  animation-delay: 1.5s;
}
.points_wrapper .point:nth-child(7) {
  left: 88%;
  opacity: 0.9;
  animation-duration: 2.2s;
  animation-delay: 0.2s;
}
.points_wrapper .point:nth-child(8) {
  left: 58%;
  opacity: 0.8;
  animation-duration: 2.25s;
  animation-delay: 0.2s;
}
.points_wrapper .point:nth-child(9) {
  left: 98%;
  opacity: 0.6;
  animation-duration: 2.6s;
  animation-delay: 0.1s;
}
.points_wrapper .point:nth-child(10) {
  left: 65%;
  opacity: 1;
  animation-duration: 2.5s;
  animation-delay: 0.2s;
}

:deep(.el-splitter) {
  .el-splitter-bar {
    width: 12px !important;
    background-color: #D7D7D7;
  }
  .el-splitter-bar__dragger {
    height: 60px !important;
    &:before {
      background-color: #666;
    }
    &:hover:before {
      background-color: #A0CFFF;
    }
  }
  .el-splitter-bar__dragger-active:before {
    background-color: #A0CFFF;
  }
  .el-splitter-bar__dragger-horizontal:before {
    width: 4px;
  }
}

:deep(.el-radio-group) {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  .el-radio {
    .el-radio__inner {
      border: 2px solid #AAAEB2;
    }
    .el-radio__label {
      color: #52555F;
      font-weight: 400;
    }
    &.is-disabled {
      cursor: not-allowed;
    }
    &.is-disabled:not(.is-checked) .el-radio__inner {
      background-color: transparent;
      opacity: 0.5;
    }
    &.is-disabled .el-radio__label {
      color: #97999F;
    }
  }
  .el-radio__input.is-disabled {
    cursor: not-allowed;
    &.is-checked {
      opacity: 0.5;
    }
  }
}
</style>

<style lang="scss">
@keyframes floatUpDown {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}
.schema-popover {
  padding: 0 !important;
  &.animation {
    animation: floatUpDown 2s ease-in-out infinite;
  }
  box-shadow: 0px 4px 32px 0px #8195C852 !important;
}
.el-popper.is-pure.is-light.el-select__popper.disable {
  display: none;
}
.el-message {
  min-width: unset;
  top: 112px !important;
  padding: 8px 16px;

  .el-message__content {
    font-family: 'Encode Sans';
    font-size: 16px;
    line-height: 24px;
    color: #232748;
  }

  .el-icon-error {
    color: #F87171;
    font-size: 20px;
  }
}
</style>