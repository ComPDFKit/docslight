<template>
  <div class="extraction-result flex-1 min-h-0 flex">
    <!-- Panel 1: Sidebar (Figma 230px outer / 206px content) -->
    <ExtractionSidebar
      :documentList="filteredSidebarFiles"
      :activeDocId="fileId"
      :activeTab="sidebarTab"
      :isLoading="sidebarLoading"
      :totalCount="sidebarFiles.length"
      :confirmedCount="confirmedCount"
      :unconfirmedCount="unconfirmedCount"
      :templateOptions="templateGroupOptions"
      :currentTemplateId="currentTemplateId"
      @select-doc="handleSidebarSelectDoc"
      @change-tab="(tab) => sidebarTab = tab"
      @filter="handleFilter"
      @apply-filter="handleSidebarApplyFilter"
      @back="changeActive('list')"
      @search="handleSidebarSearch"
      class="extraction-result__sidebar"
    />

    <!-- Panel 2: File Preview -->
    <div class="extraction-result__preview <lg:hidden">
      <!-- Figma: File name bar (top) -->
      <div v-if="file" class="extraction-result__file-bar flex items-center gap-8px">
        <svg class="w-16px h-16px shrink-0 text-[#396FFA]" viewBox="0 0 16 16" fill="none"><path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2"/><path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
        <span class="text-14px leading-22px text-[#0C131F] truncate">{{ fileName }}</span>
        <div class="flex items-center gap-8px ml-auto shrink-0">
          <span class="px-4px py-0 text-12px leading-20px rounded-3px bg-[#ECF9F3] text-[#67D1A0]" v-if="fileStatus === 2 || fileStatus === 11">{{ t('extraction.extractionSuccess') }}</span>
          <span class="px-4px py-0 text-12px leading-20px rounded-3px bg-[#FEF3E6] text-[#F5A13A]" v-else-if="fileStatus === 1 || fileStatus === 7 || fileStatus === 10">{{ fileStatus === 7 ? t('extraction.classifying') : t('extraction.extracting') }}</span>
          <span class="px-4px py-0 text-12px leading-20px rounded-3px bg-[#FBECEC] text-[#D44040]" v-else-if="isExtractionFailed">{{ t('extraction.extractionFailed') }}</span>
          <span class="px-4px py-0 text-12px leading-20px rounded-3px" :class="currentReviewStatusClass">{{ currentReviewStatusText }}</span>
        </div>
      </div>
      <!-- Figma: Preview label -->
      <div v-if="file" class="extraction-result__preview-bar">
        <span>{{ t('extraction.preview') }}</span>
      </div>
      <div v-loading="loading" v-show="!file" class="<lg:hidden flex-1 flex items-center w-full p-20px">
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
          <input ref="input" class="hidden" type="file" name="file" accept=".pdf, .jpg, .png, .jpeg" @change="handleChange">
        </div>
      </div>
      <div v-loading="loading" v-show="fileType !== 'pdf' && file" class="flex-1 overflow-hidden flex items-center">
        <img :src="picSrc" ref="img" alt="invoice" @load="onImageLoad" class="mx-auto" :class="isWidthBigger ? 'w-full h-auto' : 'w-auto h-auto max-h-full'">
      </div>
      <!-- 自定义PDF nav -->
      <div v-loading="loading" v-show="fileType === 'pdf' && file" id="webviewer" ref="viewer" class="extraction-result__viewer w-full flex-1">
        <div v-show="navShow" class="absolute bottom-14px left-0 right-0 mx-auto z-100 flex justify-center w-fit">
          <div class="bg-[#000000CC] rounded-2px p-8px flex items-center text-white">
            <Previous @click="changePage('reduce')" />
            <div class="flex items-center">
              <input @keyup.enter="setPage" v-model="pdfCurrentPage" :style="{ width: `${Math.abs(pdfCurrentPage).toString().length * 8}px` }" class="border-0 text-14px leading-20px text-center h-18px mr-2px outline-none border-b-1 border-white border-solid bg-transparent" onkeypress="return (/[\d]/.test(String.fromCharCode(event.keyCode)))" type="number" />
              <div class="text-14px leading-16px">{{ `/${pdfPage}` }}</div>
            </div>
            <Previous @click="changePage('add')" class="transform rotate-[180deg]" />
            <div class="w-1px h-20px bg-[#FFFFFF1F] mx-8px"></div>
            <Hand v-show="!hand" @click="switchTool" class="hand cursor-pointer" />
            <HandHover v-show="hand" @click="switchTool" class="cursor-pointer" />
            <div class="w-1px h-20px bg-[#FFFFFF1F] mx-8px"></div>
            <div class="relative rounded-2px border-1 border-[#FFFFFF99] px-8px py-2px flex items-center hover:(border-[#E1E3E8] bg-[#FFFFFF1F])">
              <input @keyup.enter="setScale(scale)" v-model="scale" :style="{ width: `${Math.abs(scale).toString().length * 8 + 8}px` }"  class="border-0 leading-20px text-center h-18px mr-2px outline-none bg-transparent" onkeypress="return (/[\d]/.test(String.fromCharCode(event.keyCode)))" type="number" />
              %
              <ComArrow @click="scaleShow = !scaleShow" class="cursor-pointer" />
              <div v-show="scaleShow" class="absolute shadows bg-white text-[#43474D] bottom-32px left-0 p-8px">
                <div @click="setScale(10)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">10%</div>
                <div @click="setScale(25)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">25%</div>
                <div @click="setScale(50)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">50%</div>
                <div @click="setScale(100)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">100%</div>
                <div @click="setScale(150)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">150%</div>
                <div @click="setScale(200)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">200%</div>
                <div @click="setScale(300)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">300%</div>
                <div @click="setScale(400)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">400%</div>
                <div @click="setScale(500)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">500%</div>
                <div @click="setScale(1000)" class="text-16px leading-18px cursor-pointer whitespace-nowrap px-8px py-6px rounded-2px hover:(text-white bg-brand-2)">1000%</div>
              </div>
            </div>
            <ReduceZoom @click="changeZoom(0)" class="zoom cursor-pointer ml-8px" />
            <AddZoom @click="changeZoom(1)" class="zoom cursor-pointer ml-8px" />
          </div>
        </div>
      </div>
    </div>

    <!-- Panel 3: Result Form -->
    <div class="extraction-result__form <lg:hidden" :class="outputType === 'json' ? 'is-json-dark' : ''">
      <div v-if="isAddTemp" class="flex flex-col flex-1 min-h-0">
        <!-- Template name bar -->
        <div class="flex items-center bg-white py-12px px-24px border-b border-[#E1E3E8] shrink-0">
          <span class="text-14px text-[#94969D] mr-8px whitespace-nowrap">{{ t('extraction.tempName') }}</span>
          <input ref="templateNameRef" v-model="tempName" maxlength="50" class="flex-1 text-14px font-500 text-[#0C131F] outline-none bg-transparent" />
        </div>
        <div class="flex-1 px-24px pb-20px overflow-auto text-[#0C131F]">
        <!-- Template page selector: only when isAddTemp and multi-page PDF -->
        <div v-if="isAddTemp && fileType === 'pdf' && pdfPage > 1" class="flex items-center gap-4px pt-20px pb-8px">
          <span class="text-14px leading-20px text-[#404653]">{{ t('extraction.usePageAsTemplate[0]') }}</span>
          <button
            :disabled="templatePage <= 1"
            @click="templatePage > 1 && (templatePage--)"
            class="w-24px h-24px flex items-center justify-center border border-[#E2E3E5] rounded-4px bg-white cursor-pointer hover:border-[#396FFA] disabled:(opacity-40 cursor-not-allowed hover:border-[#E2E3E5])"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 8H13" stroke="#404653" stroke-width="1.5" stroke-linecap="round"/></svg>
          </button>
          <input
            v-model.number="templatePageInput"
            type="number"
            class="w-64px h-24px text-center text-12px leading-16px text-[#404653] border border-[#E2E3E5] rounded-4px outline-none px-12px"
            @blur="onTemplatePageBlur"
            @keyup.enter="onTemplatePageBlur"
            onkeypress="return (/[\d]/.test(String.fromCharCode(event.keyCode)))"
          />
          <button
            :disabled="templatePage >= pdfPage"
            @click="templatePage < pdfPage && (templatePage++)"
            class="w-24px h-24px flex items-center justify-center border border-[#E2E3E5] rounded-4px bg-white cursor-pointer hover:border-[#396FFA] disabled:(opacity-40 cursor-not-allowed hover:border-[#E2E3E5])"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M8 3V13M3 8H13" stroke="#404653" stroke-width="1.5" stroke-linecap="round"/></svg>
          </button>
          <span class="text-14px leading-20px text-[#404653]">{{ t('extraction.usePageAsTemplate[1]') }}</span>
        </div>

        <div class="py-6px px-12px flex pb-8px text-black text-xs pt-20px sticky top-0 bg-white z-10">
          <div class="flex-1 mr-12px">
            {{ t('extraction.fieldType') }}
          </div>
          <div class="flex-1 mr-12px">
            {{ t('extraction.fieldName') }}
          </div>
          <div class="flex-1 flex items-center">
            {{ t('extraction.exportedFieldName') }}
            <el-tooltip popper-class="temp-item" effect="dark" :content="t('extraction.exportedFieldNameTip')" placement="top">
              <HelpTemp class="ml-10px cursor-pointer" />
            </el-tooltip>
          </div>
        </div>
        <template v-for="(item, index) in templateField" :key="index">
          <div v-if="item.fieldType === 'text'" :class="index && 'mt-8px'" class="py-8px px-12px flex mb-8px text-black text-xs bg-[#F6F6FB] rounded-4px items-center">
            <el-select :model-value="item.fieldType" :disabled="!isAddTemp" class="flex-1 mr-12px tempFieldSelect" @update:model-value="(v) => isAddTemp && onRootFieldTypeChange(index, v)">
              <el-option v-for="opt in fieldTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-popover v-model:visible="promptPopoverShow[index]" :disabled="!isAddTemp" trigger="" placement="top" popper-class="schema-popover" width="320px">
              <template #reference>
                <el-input v-model="item.fieldName" :maxlength="50" :placeholder="t('extraction.pleaseEnter')" :disabled="!isAddTemp" class="flex-1 mr-12px tempFieldInput relative" @keydown.space.stop @keyup.space.stop>
                  <template #suffix>
                    <UnTips @click="() => isAddTemp && (promptPopoverShow[index] = true)" v-show="!item.prompt" class="cursor-pointer !ml-0px" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                    <Tips @click="() => isAddTemp && (promptPopoverShow[index] = true)" v-show="item.prompt" class="cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                  </template>
                </el-input>
              </template>
              <template #default>
                <div class="bg-white text-brand-3 text-sm py-20px px-12px">
                  <CloseSchema @click="() => isAddTemp && (promptPopoverShow[index] = false)" class="absolute top-12px right-10px cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                  <div class="flex items-center mb-8px">
                    <Tips class="mr-12px" />
                    {{ t('template.prompt')  }}
                  </div>
                  <el-input v-model="item.prompt" :maxlength="200" :disabled="!isAddTemp" type="textarea" :placeholder="t('template.prompt')"></el-input>
                  <div @click="() => isAddTemp && (promptPopoverShow[index] = false)" class="mt-8px rounded-6px w-144px py-10px font-500 text-sm text-white bg-brand-2 mx-auto text-center -mb-8px cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'">
                    {{ t('template.ok')  }}
                  </div>
                </div>
              </template>
            </el-popover>
            <el-input v-model="item.mapping" :maxlength="50" :placeholder="t('extraction.pleaseEnter')" :disabled="!isAddTemp" class="flex-1 tempFieldInput" />
            <DeleteField @click="() => isAddTemp && removeField(index)" class="ml-8px cursor-pointer min-w-16px" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
          </div>
          <div v-if="item.fieldType === 'table'" :class="index && 'mt-8px'" class="mt-8px py-8px px-12px mb-8px text-black text-xs bg-[#F6F6FB] rounded-4px">
            <div class="flex items-center">
              <el-select :model-value="item.fieldType" :disabled="!isAddTemp" class="flex-1 mr-12px tempFieldSelect" @update:model-value="(v) => isAddTemp && onRootFieldTypeChange(index, v)">
                <el-option v-for="opt in fieldTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
              <el-popover v-model:visible="promptPopoverShow[index]" :disabled="!isAddTemp" trigger="" placement="top" popper-class="schema-popover" width="320px">
                <template #reference>
                  <div class="flex-[2_0_0] flex items-center mr-12px">
                    <el-input v-model="item.fieldName" :maxlength="50" :placeholder="t('extraction.pleaseEnter')" :disabled="!isAddTemp" class="flex-1 tempFieldInput" @keydown.space.stop @keyup.space.stop>
                      <template #suffix>
                        <UnTips @click="() => isAddTemp && (promptPopoverShow[index] = true)" v-show="!item.prompt" class="cursor-pointer !ml-0px" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                        <Tips @click="() => isAddTemp && (promptPopoverShow[index] = true)" v-show="item.prompt" class="cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                      </template>
                    </el-input>
                    <ArrowBlack @click="() => isAddTemp && (item.collapse = !item.collapse)" class="transform cursor-pointer arrowBlack ml-8px" :class="[item.collapse ? 'rotate-90' : '-rotate-90', !isAddTemp && 'pointer-events-none opacity-50']" />
                  </div>
                </template>
                <template #default>
                  <div class="bg-white text-brand-3 text-sm py-20px px-12px">
                    <CloseSchema @click="() => isAddTemp && (promptPopoverShow[index] = false)" class="absolute top-12px right-10px cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                    <div class="flex items-center mb-8px">
                      <Tips class="mr-12px" />
                      {{ t('template.prompt')  }}
                    </div>
                    <el-input v-model="item.prompt" :maxlength="200" :disabled="!isAddTemp" type="textarea" :placeholder="t('template.prompt')"></el-input>
                    <div @click="() => isAddTemp && (promptPopoverShow[index] = false)" class="mt-8px rounded-6px w-144px py-10px font-500 text-sm text-white bg-brand-2 mx-auto text-center -mb-8px cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'">
                      {{ t('template.ok')  }}
                    </div>
                  </div>
                </template>
              </el-popover>
              <DeleteField @click="() => isAddTemp && removeField(index)" class="ml-8px cursor-pointer min-w-16px" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
            </div>
            <template v-if="item.collapse">
              <div class="h-1px w-full bg-[#E2E3E5] my-8px"></div>
              <div :class="indexSun && 'mt-8px'" v-for="(items, indexSun) in item.children" :key="indexSun" class="flex items-center">
                <div class="flex justify-end flex-1 mr-12px">
                  <div class="!w-[calc(100%-20px)] rounded-4px px-12px py-6px bg-white text-[#606266] text-xs border border-[#e5e7eb]" @mousedown.prevent>
                    {{ t('singleExtract.text') }}
                  </div>
                </div>
                <el-popover :visible="getChildPopoverShow(index, indexSun)" :disabled="!isAddTemp" @update:visible="(v) => isAddTemp && setChildPopoverShow(index, indexSun, v)" trigger="" placement="top" popper-class="schema-popover" width="320px">
                  <template #reference>
                    <el-input v-model="items.fieldName" :maxlength="50" :placeholder="t('extraction.pleaseEnter')" :disabled="!isAddTemp" class="flex-1 mr-12px tempFieldInput" @keydown.space.stop @keyup.space.stop>
                      <template #suffix>
                        <UnTips @click="() => isAddTemp && setChildPopoverShow(index, indexSun, true)" v-show="!items.prompt" class="cursor-pointer !ml-0px" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                        <Tips @click="() => isAddTemp && setChildPopoverShow(index, indexSun, true)" v-show="items.prompt" class="cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                      </template>
                    </el-input>
                  </template>
                  <template #default>
                    <div class="bg-white text-brand-3 text-sm py-20px px-12px">
                      <CloseSchema @click="() => isAddTemp && setChildPopoverShow(index, indexSun, false)" class="absolute top-12px right-10px cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
                      <div class="flex items-center mb-8px">
                        <Tips class="mr-12px" />
                        {{ t('template.prompt')  }}
                      </div>
                      <el-input v-model="items.prompt" :maxlength="200" :disabled="!isAddTemp" type="textarea" :placeholder="t('template.prompt')"></el-input>
                      <div @click="() => isAddTemp && setChildPopoverShow(index, indexSun, false)" class="mt-8px rounded-6px w-144px py-10px font-500 text-sm text-white bg-brand-2 mx-auto text-center -mb-8px cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'">
                        {{ t('template.ok')  }}
                      </div>
                    </div>
                  </template>
                </el-popover>
                <el-input v-model="items.mapping" :maxlength="50" :placeholder="t('extraction.pleaseEnter')" :disabled="!isAddTemp" class="flex-1 tempFieldInput" />
                <DeleteField @click="() => isAddTemp && removeChildField(index, indexSun)" class="ml-8px cursor-pointer min-w-16px" :class="!isAddTemp && 'pointer-events-none opacity-50'" />
              </div>
              <div class="text-[#2E59CA] cursor-pointer flex items-center mt-8px pl-20px py-6px w-fit" :class="!isAddTemp && 'pointer-events-none opacity-50'" @click="() => isAddTemp && addTableChild(index)">
                <AddField class="mr-4px" />
                {{ t('extraction.addNewField') }} 
              </div>
            </template>
          </div>
        </template>
        <div class="py-8px px-12px flex justify-center mb-8px text-[#2E59CA] text-xs bg-[#F6F6FB] rounded-4px">
          <div class="border border-[#618CFB] rounded-6px w-full flex items-center py-6px justify-center cursor-pointer" :class="!isAddTemp && 'pointer-events-none opacity-50'" @click="() => isAddTemp && addRootTextField()">
            <AddField class="mr-4px" />
            {{ t('extraction.addNewField') }}
          </div>
        </div>
      </div>
      </div>
      <div v-else class="flex flex-col h-full min-h-0 text-[#0C131F]">
        <!-- Header: Current Template + Template Details button (Figma) -->
        <div class="extraction-result__template-header shrink-0">
          <div class="text-14px leading-22px font-600 text-[#0C131F] mb-8px">{{ t('extraction.currentTemplate') }}</div>
          <div class="flex items-center gap-4px">
            <el-select
              v-model="currentTemplateId"
              class="extraction-result__template-select"
              :placeholder="tempName || '--'"
              :disabled="templateSwitchLoading"
              @change="handleTemplateChange"
            >
              <el-option
                v-for="item in templateGroupOptions"
                :key="item.groupTemplateId"
                :label="item.templateName"
                :value="item.groupTemplateId"
              />
            </el-select>
            <button
              class="extraction-result__template-detail-btn flex items-center justify-center gap-8px cursor-pointer hover:bg-[#244FF0] whitespace-nowrap shrink-0 border-none"
              type="button"
              @click="openCurrentTemplateSettings"
            >
              <svg class="w-16px h-16px" viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M4 3h8M2 8h12M4 13h8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
              {{ t('extraction.templateDetails') }}
            </button>
          </div>
        </div>
        <div class="extraction-result__divider shrink-0" />
        <!-- Content: JSON/Text toggle + Export (Figma Top buttons) -->
        <div class="extraction-result__result-toolbar flex items-center justify-between shrink-0">
          <div class="extraction-result__result-tabs flex items-center gap-4px">
            <button
              type="button"
              class="extraction-result__result-tab cursor-pointer border-none transition-colors"
              :class="outputType === 'txt' ? 'is-active' : ''"
              @click="changeConvert('txt')"
            >{{ t('extraction.text') }}</button>
            <button
              type="button"
              class="extraction-result__result-tab cursor-pointer border-none transition-colors"
              :class="outputType === 'json' ? 'is-active' : ''"
              @click="changeConvert('json')"
            >{{ t('extraction.json') }}</button>
          </div>
          <button
            type="button"
            class="extraction-result__export-btn flex items-center gap-4px cursor-pointer hover:(bg-[#F3F6FF]) shrink-0"
            @click="openDialog('download')"
          >
            <svg class="w-14px h-14px" viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 10.5V2.75M4.75 6 8 2.75 11.25 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 10.25v2.5h10v-2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
            {{ t('extraction.export') }}
          </button>
        </div>
        <!-- 自定义提取结果展示 -->
        <div v-if="isExtractionFailed && !loading && !dialogVisibleLoading" class="extraction-result__failure-state">
          <div class="extraction-result__failure-content">
            <div class="extraction-result__failure-icon" aria-hidden="true">
              <svg viewBox="0 0 32 32" fill="none">
                <path fill-rule="evenodd" clip-rule="evenodd" d="M16 29.333C23.364 29.333 29.333 23.364 29.333 16S23.364 2.667 16 2.667 2.667 8.636 2.667 16 8.636 29.333 16 29.333Zm0-24C10.109 5.333 5.333 10.109 5.333 16S10.109 26.667 16 26.667 26.667 21.891 26.667 16 21.891 5.333 16 5.333Zm0 13.333a1.333 1.333 0 0 1-1.333-1.333v-6.666a1.333 1.333 0 1 1 2.666 0v6.666A1.333 1.333 0 0 1 16 18.667Zm0 5.333a1.667 1.667 0 1 0 0-3.333 1.667 1.667 0 0 0 0 3.333Z" fill="currentColor"/>
              </svg>
            </div>
            <div class="extraction-result__failure-text">{{ t('extraction.extractionFailedTip') }}</div>
          </div>
          <button class="extraction-result__failure-retry" type="button" @click="startExtraction">
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <path d="M13.333 7.333A5.333 5.333 0 1 0 8 13.333c1.52 0 2.89-.637 3.862-1.66" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              <path d="M13.333 3.333v4h-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ t('extraction.retry') }}
          </button>
        </div>
        <div v-show="outputType === 'txt' && !noResult && !isExtractionFailed" class="extraction-result__result-body whitespace-pre-line flex flex-col relative overflow-hidden">
          <div v-show="!dialogVisibleLoading" class="flex flex-col overflow-auto min-h-0 flex-1">
            <!-- 结果数据展示 -->
            <!-- <div v-show="customTextNum + customTableNum" class="text-[#232748] max-w-392px rounded-8px flex bg-[#EBF1FE] p-4px">
              <div @click="resultType = 'all'" :class="resultType === 'all' && 'bg-white rounded-4px'" class="px-12px py-4px flex justify-evenly items-center cursor-pointer w-120px">
                <All />{{ t('singleExtract.all') }}
                <span class="text-14px leading-20px py-2px px-8px rounded-8px text-[#94969D]" :class="resultType === 'all' && 'bg-[#F3F6FF]'">{{ customTextNum + customTableNum }}</span>
              </div>
              <div @click="resultType = 'text'" :class="resultType === 'text' && 'bg-white rounded-4px'" class="px-12px py-4px flex justify-evenly items-center cursor-pointer w-120px">
                <Text />{{ t('singleExtract.text') }}
                <span class="text-14px leading-20px py-2px px-8px rounded-8px text-[#94969D]" :class="resultType === 'text' && 'bg-[#F3F6FF]'">{{ customTextNum }}</span>
              </div>
              <div @click="resultType = 'table'" :class="resultType === 'table' && 'bg-white rounded-4px'" class="px-12px py-4px flex justify-evenly items-center cursor-pointer w-120px">
                <Table />{{ t('singleExtract.table') }}
                <span class="text-14px leading-20px py-2px px-8px rounded-8px text-[#94969D]" :class="resultType === 'table' && 'bg-[#F3F6FF]'">{{ customTableNum }}</span>
              </div>
            </div> -->
            <!-- 字段结果展示 -->
            <div ref="resultContentRef" v-loading="loading" class="extraction-result__content-scroll flex flex-col" :class="loading ? 'overflow-hidden' : 'overflow-auto'">
              <template v-for="(page, pageNumStr, pageIndex) in editableTabs.customDetails" :key="pageIndex">
                <div v-show="!customInit" @click="customPageShow[pageIndex] = !customPageShow[pageIndex]" class="extraction-result__page-row flex justify-between items-center w-full hover:bg-[#F3F6FF] cursor-pointer">
                  <p>{{ pageNumStr }}</p>
                  <div class="flex items-center">
                    <ExtractPull :class="!customPageShow[pageIndex] && 'transforms'" class="transitions" />
                  </div>
                </div>
                <div v-show="customPageShow[pageIndex]" class="pb-16px">
                  <div v-show="resultType === 'all' || resultType === 'text'" class="bg-white rounded-4px">
                    <template v-for="(item, key, index) in page" :key="key">
                      <div v-if="key !== 'tables'"
                        @mouseenter="handleMouseenter(pageIndex, index)"
                        @mouseleave="handleMouseleave(pageIndex, index)"
                        class="extraction-result__field-card relative text-sm"
                        :class="[key && 'mt-12px', customContentEdit[pageIndex]?.[index].hover && 'handle']">
                        <div :id="'customKey' + pageNumStr + index"
                          :contenteditable="!isResultReadOnly"
                          @focus="!isResultReadOnly && captureEditSnapshot()"
                          @blur="onCustomKeyBlur(pageNumStr, pageIndex, index, key, $event)"
                          @keydown.enter.prevent
                          class="text-[#94969D] break-words outline-none">{{ key }}</div>
                        <div :id="'customText' + pageNumStr + index" 
                          @click.stop="startCustomTextEdit(pageNumStr, pageIndex, index)"
                          @blur="onCustomTextBlur"
                          class="editContent text-[#232748] mt-4px outline-none whitespace-pre truncate w-full"
                          :class="customContentEdit[pageIndex]?.[index]?.status && '!text-[#232748]'"
                          :contenteditable="!isResultReadOnly && customContentEdit[pageIndex]?.[index]?.status">
                          {{ item }}
                        </div>
                        <div class="field-actions hidden absolute top-8px right-16px">
                          <div class="border-1 border-[#E1E3E8] p-3px rounded-4px inline-block cursor-pointer hover:border-[#396FFA] copy">
                            <Copy @click="copy(key + ': ' + item, pageIndex, index)" />
                            <div v-show="customContentEdit[pageIndex]?.[index].copy === t('singleExtract.copy')" class="absolute z-10 top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                              {{ t('singleExtract.copy') }}
                            </div>
                            <div v-show="customContentEdit[pageIndex]?.[index].copy === t('singleExtract.copied')" class="absolute z-10 top-[-30px] whitespace-nowrap text-black left-[-20px] hidden rounded-4px text-12px leading-16px py-5px px-8px bg-[#CCCCCC] tip !shadow-none">
                              {{ t('singleExtract.copied') }}
                            </div>
                          </div>
                          <div
                            v-if="!isResultReadOnly"
                            class="border-1 border-[#E1E3E8] p-3px rounded-4px inline-flex cursor-pointer hover:border-[#396FFA] delete-action"
                            @click.stop="deleteCustomField(pageNumStr, pageIndex, index, key)"
                          >
                            <DeleteField />
                          </div>
                        </div>
                        <!-- <div class="edit hidden border-1 border-[#E1E3E8] p-3px rounded-4px inline-block absolute cursor-pointer top-8px right-16px hover:border-[#396FFA]">
                          <Edit @click.stop="updateDate(), customContentEdit[pageIndex][index].status = true, customPageTableKey = pageNumStr, customTextIndex = index, customTextPageIndex = pageIndex, focusContentEditable('customText' + pageNumStr + index)" class="editIcon" />
                          <div class="absolute top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                            {{ t('singleExtract.edit') }}
                          </div>
                        </div> -->
                      </div>
                    </template>
                  </div>
                  <div class="extraction-result__add-field-wrap flex justify-center text-[#2E59CA] text-xs">
                    <div
                      class="extraction-result__add-field-btn border border-[#618CFB] w-full flex items-center justify-center cursor-pointer"
                      :class="isResultReadOnly && 'pointer-events-none opacity-50'"
                      @click.stop="addKeyValue(pageNumStr)"
                    >
                      <AddField class="mr-4px" />
                      {{ t('extraction.addNewField') }}
                    </div>
                  </div>
                  <!-- Table结果展示 -->
                  <template v-if="page?.tables">
                    <div v-show="resultType === 'all' || resultType === 'table'" v-for="(item, indexTable) in page?.tables" :key="indexTable" class="mt-12px pt-28px">
                      <div v-show="Object.keys(page?.tables).length" class="flex justify-between mb-4px sticky -mt-28px z-10">
                        <div class="text-[#94969D] text-sm">{{ indexTable }}</div>
                        <div class="flex">
                          <div @click="openDialog('table'), customPageTableKey = pageNumStr, customTableListIndex = indexTable" class="border-1 border-[#E1E3E8] p-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA]">
                            <Download class="download" />
                          </div>
                        </div>
                      </div>
                      <div class="overflow-auto">
                        <table border="1" class="text-[#232748] rounded-4px border-collapse overflow-hidden">
                          <thead>
                            <tr>
                              <th v-for="(_value, key, colIndex) in item[0]" :key="key" :contenteditable="!isResultReadOnly" @focus="!isResultReadOnly && captureEditSnapshot()" @blur="onCustomTableHeaderBlur(pageNumStr, indexTable, key, colIndex, $event)" @keydown.enter.prevent class="bg-[#F3F6FF] text-14px leading-20px text-[#94969D] px-12px py-10px font-normal border-1 border-[#E1E3E8] outline-none">{{ key }}</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(indexChi, index) in item" :key="index">
                              <td
                                v-for="(_value, key, indexSun) in indexChi"
                                :key="indexSun"
                                class="extraction-result__table-cell"
                                @click.stop="onCustomTableCellClick(pageNumStr, indexTable, index, indexSun)"
                              >
                                <span
                                  :id="'customTable' + pageNumStr + indexTable + index + indexSun"
                                  :contenteditable="!isResultReadOnly"
                                  class="extraction-result__table-cell-text"
                                  @focus="!isResultReadOnly && captureEditSnapshot()"
                                  @blur="onCustomTableCellBlur(pageNumStr, indexTable, index, key, indexSun, $event)"
                                  @keydown.enter.prevent
                                >{{ indexChi[key] }}</span>
                                <button
                                  v-if="!isResultReadOnly"
                                  class="extraction-result__table-cell-more"
                                  type="button"
                                  aria-label="Table cell actions"
                                  @click.stop="openCustomTableMenu(pageNumStr, indexTable, index, indexSun, $event)"
                                >
                                  <svg viewBox="0 0 14 14" fill="none" aria-hidden="true">
                                    <path d="M7 2.625V11.375" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-dasharray="0.01 4.37" />
                                  </svg>
                                </button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </template>
                </div>
              </template>
            </div>
          </div>
          <!-- Loading -->
          <div v-show="dialogVisibleLoading" class="flex-1 flex justify-center items-center">
            <DemoLoading class="transform scale-70" />
          </div>
        </div>
        <div v-show="outputType === 'json' && !noResult && !isExtractionFailed" v-loading="loading" class="extraction-result__json-scroll">
          <JsonViewer boxed expanded :expandDepth="7" sort theme="dark" :value="editableTabs.customDetails" />
        </div>
      </div>

      <template v-if="isAddTemp">
        <div class="extraction-result__bottom-bar shrink-0 h-44px border-t border-[#E7E8EC] bg-white flex items-center justify-between px-16px">
          <button
            class="px-16px py-5px border border-[#DCDFE6] rounded-3px text-14px text-[#52555F] cursor-pointer bg-transparent hover:border-[#396FFA] hover:text-[#396FFA]"
            type="button"
            @click="changeActive('list'), resetData()"
          >
            Cancel
          </button>
          <div class="flex items-center gap-8px">
            <button
              class="px-16px py-5px border border-[#618CFB] rounded-3px text-14px font-500 text-[#2E59CA] cursor-pointer bg-transparent hover:bg-[#D7E2FE]"
              :class="file && hasValidField ? '' : 'opacity-50 pointer-events-none'"
              type="button"
              @click="file && hasValidField && testTemplate()"
            >
              {{ isFirst ? t('extraction.test') : t('extraction.retest') }}
            </button>
            <button
              class="px-16px py-5px rounded-3px text-14px font-500 cursor-pointer"
              :class="hasValidField ? 'bg-[#396FFA] text-white hover:bg-[#244FF0]' : 'bg-[#C0C4CC] text-white cursor-not-allowed'"
              type="button"
              @click="hasValidField && handleSaveTemplate()"
            >
              {{ saveButtonLabel }}
            </button>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="extraction-result__bottom-bar shrink-0 h-44px border-t border-[#E7E8EC] bg-white flex items-center justify-between px-16px">
          <div aria-hidden="true"></div>
          <div class="flex items-center gap-8px">
            <button class="flex items-center gap-10px px-16px py-5px border border-[#396FFA] rounded-3px text-14px font-400 text-[#396FFA] cursor-pointer bg-transparent hover:bg-[#F3F6FF]" type="button" @click="startExtraction">{{ t('extraction.reExtract') }}</button>
            <button
              v-permission="'extract:check'"
              class="flex items-center gap-10px px-16px py-5px rounded-3px text-14px font-400 text-white cursor-pointer bg-[#396FFA] hover:bg-[#244FF0] border-none disabled:(bg-[#C0C4CC] cursor-not-allowed)"
              type="button"
              @click="configResult()"
              :disabled="isConfirmButtonDisabled"
            >
              {{ confirmActionText }}
            </button>
          </div>
        </div>
      </template>
    </div>

    <!-- download -->
    <el-dialog v-loading="loading" v-model="dialogVisible" width="372px" top="20vh">
      <div class="flex justify-end">
        <IdpClose @click="dialogVisible = false" class="cursor-pointer w-17px h-17px" />
      </div>
      <p class="text-[#43474D] text-sm font-bold mb-12px">{{ t('singleExtract.selectFormat') }}</p>
      <el-radio-group v-model="format">
        <template v-if="!tableDownload">
          <el-radio v-if="['all', 'text'].includes(resultType)" label="json">JSON</el-radio>
          <el-radio v-if="['all', 'table'].includes(resultType)" label="xlsx">Excel</el-radio>
          <el-radio v-if="['all', 'table'].includes(resultType)" label="csv">CSV</el-radio>
        </template>
        <template v-else>
          <el-radio label="xlsx">Excel</el-radio>
          <el-radio label="csv">CSV</el-radio>
        </template>
      </el-radio-group>
      <div v-if="tableDownload" @click="downloadFile" class="cursor-pointer font-600 w-full h-40px flex justify-center items-center bg-[#396FFA] mt-21px text-white rounded-4px text-sm hover:bg-[#244FF0]">
        {{ t('singleExtract.download') }}
      </div>
      <div v-else @click="exportFile([fileId])" class="cursor-pointer font-600 w-full h-40px flex justify-center items-center bg-[#396FFA] mt-21px text-white rounded-4px text-sm hover:bg-[#244FF0]">
        {{ t('singleExtract.download') }}
      </div>
    </el-dialog>

    <!-- Json View Dialog -->
    <el-dialog v-model="dialogVisibleJsonPreview" width="628px" top="5vh" class="jsonViewer" center>
      <div class="flex items-center justify-between py-30px px-32px">
        <div class="flex items-center">
          <Docs class="mr-4px" />
          {{ `${downloadName}.pdf` }}
        </div>
        <el-tooltip popper-class="box-item" effect="dark" :content="t('template.close')" placement="top">
          <CloseDialog @click="dialogVisibleJsonPreview = false" class="ml-2px cursor-pointer" />
        </el-tooltip>
      </div>
      <JsonViewer boxed expanded :expandDepth="7" sort theme="dark" :value="editableTabs.customDetails" />
      <div v-show="(customTextNum + customTableNum)" class="copyTip w-24px h-24px border-1 border-[#E1E3E8] inline-block absolute p-3px rounded-4px cursor-pointer bg-white right-24px top-88px hover:border-[#396FFA]">
        <Copy @click="copy(editableTabs.customDetails, -1, 0)" />
        <div class="absolute top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
          {{ t('singleExtract.copy') }}
        </div>
      </div>
      <div @click="openDialog('txt')"
        class="font cursor-pointer font-700 w-162px py-12px text-center text-white bg-[#396FFA] rounded-8px text-14px leading-16px mx-auto my-20px hover:bg-[#244FF0]">
        {{ t('singleExtract.download') }}
      </div>
    </el-dialog>
    <LocalModeApiBanner :key="localBannerKey" class="extraction-result__local-banner" />

    <Teleport to="body">
      <div
        v-if="activeCustomTableMenu"
        class="extraction-result__table-menu"
        :style="customTableMenuStyle"
        @click.stop
      >
        <button type="button" @click="handleCustomTableMenuAction('insert-above')">{{ t('extraction.tableActions.insertRowAbove') }}</button>
        <button type="button" @click="handleCustomTableMenuAction('insert-below')">{{ t('extraction.tableActions.insertRowBelow') }}</button>
        <button type="button" @click="handleCustomTableMenuAction('insert-column-above')">{{ t('extraction.tableActions.insertColumnAbove') }}</button>
        <button type="button" @click="handleCustomTableMenuAction('insert-column-below')">{{ t('extraction.tableActions.insertColumnBelow') }}</button>
        <button type="button" @click="handleCustomTableMenuAction('delete-row')">{{ t('extraction.tableActions.deleteRow') }}</button>
        <button type="button" @click="handleCustomTableMenuAction('delete-column')">{{ t('extraction.tableActions.deleteColumn') }}</button>
      </div>
    </Teleport>
  </div>
</template>

<script lang="ts" setup>
import JSZip from 'jszip'
import * as XLSX from "xlsx"
import { saveAs } from 'file-saver'
import { useI18n } from 'vue-i18n'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import clipboard from 'copy-to-clipboard'
import { JsonViewer } from 'vue3-json-viewer'
import request, { post, get } from '../../utils/request'
import Upload from '../images/SplittingUpload.vue'
import Hand from '../images/Hand.vue'
import HandHover from '../images/HandHover.vue'
import AddZoom from '../images/AddZoom.vue'
import Docs from '../images/Docs.vue'
import CloseDialog from '../images/CloseDialog.vue'
import Previous from '../images/Previous.vue'
import ComArrow from '../images/ComArrow.vue'
import ReduceZoom from '../images/ReduceZoom.vue'
import JsonIcon from '../images/JsonIcon.vue'
import Tips from '../images/Tips.vue'
import 'vue3-json-viewer/dist/vue3-json-viewer.css'
import ComPDFKitViewer from "../../assets/@compdfkit/webviewer"
import { ref, watch, computed, onMounted, nextTick, onUnmounted, type Ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import ExtractPull from "../images/ExtractPull.vue"
import DemoLoading from "../DemoLoading.vue"
import { getEnv } from '../../utils/env'
import HelpTemp from '../images/HelpTemp.vue'
import DeleteField from '../images/DeleteNoOutLine.vue'
import ExtractionSidebar from '../../view/ExtractionSidebar.vue'
import LocalModeApiBanner from '../LocalModeApiBanner.vue'

const { t, locale } = useI18n()
const router = useRouter()
const timer = ref()
const loading = ref(false)
const localBannerKey = ref(0)
const resultContentRef = ref<HTMLElement | null>(null)
const dragover = ref(false)
const init = ref(true)
const editName = ref(true)
const templateNameRef = ref()
const tempName = ref('')
const showBtn = ref(false)
const customInit = ref(false)
const pdfPage = ref()
const edit = ref(false)
const pdfCurrentPage = ref(1)
const scale = ref(100)
const collapse = ref(true)
const scaleShow = ref(false)
const navShow = ref(false)
const hand = ref(0)
const pullShow = ref(false)
const isFirst = ref(true)
const saveShow = ref(true)
const dialogVisibleSetName = ref(false)
const dialogVisibleJsonPreview = ref(false)
const templateField = ref<Template>([])

type FieldType = 'text' | 'table'

type BaseField = {
  prompt: string
  fieldName: string
  fieldType: FieldType
}

type TextField = BaseField & {
  fieldType: 'text'
  mapping: string
}

type TableField = BaseField & {
  fieldType: 'table'
  collapse?: boolean
  children: TextField[]
}

type FieldItem = TextField | TableField
type Template = FieldItem[]

type FieldTypeOption = { label: string; value: FieldType }

const fieldTypeOptions = computed<FieldTypeOption[]>(() => {
  // 读取 locale 保证切换语言时 label 也更新
  locale.value
  return [
    { label: t('singleExtract.text'), value: 'text' },
    { label: t('singleExtract.table'), value: 'table' }
  ]
})

// 控制根字段 prompt popover 显示状态
const promptPopoverShow = ref<boolean[]>([])
// 控制子字段 prompt popover 显示状态 [parentIndex][childIndex]
const childPromptPopoverShow = ref<boolean[][]>([])

const createTextField = (overrides: Partial<TextField> = {}): TextField => ({
  prompt: '',
  fieldType: 'text',
  fieldName: '',
  mapping: '',
  ...overrides
})

const createTableField = (overrides: Partial<TableField> = {}): TableField => ({
  prompt: '',
  fieldType: 'table',
  fieldName: '',
  collapse: true,
  children: [createTextField()],
  ...overrides
})

const addRootTextField = () => {
  templateField.value.push(createTextField())
  promptPopoverShow.value.push(false)
  childPromptPopoverShow.value.push([])
}

const addTableChild = (tableIndex: number) => {
  const item = templateField.value[tableIndex]
  if (!item || item.fieldType !== 'table') return
  item.children.push(createTextField())
  childPromptPopoverShow.value[tableIndex].push(false)
}

// 删除字段
const removeField = (index: number) => {
  templateField.value.splice(index, 1)
  promptPopoverShow.value.splice(index, 1)
  childPromptPopoverShow.value.splice(index, 1)
}

// 删除子字段
const removeChildField = (parentIndex: number, childIndex: number) => {
  const item = templateField.value[parentIndex]
  if (!item || item.fieldType !== 'table') return
  item.children.splice(childIndex, 1)
  childPromptPopoverShow.value[parentIndex].splice(childIndex, 1)
}

// 初始化 popover 控制数组
const initPromptPopoverShow = () => {
  promptPopoverShow.value = templateField.value.map(() => false)
  childPromptPopoverShow.value = templateField.value.map(item => {
    if (item.fieldType === 'table' && item.children) {
      return item.children.map(() => false)
    }
    return []
  })
}

// 安全获取子字段 popover 状态
const getChildPopoverShow = (parentIndex: number, childIndex: number): boolean => {
  return childPromptPopoverShow.value[parentIndex]?.[childIndex] ?? false
}

// 安全设置子字段 popover 状态
const setChildPopoverShow = (parentIndex: number, childIndex: number, value: boolean) => {
  if (!childPromptPopoverShow.value[parentIndex]) {
    childPromptPopoverShow.value[parentIndex] = []
  }
  childPromptPopoverShow.value[parentIndex][childIndex] = value
}

// 立即初始化
initPromptPopoverShow()

const normalizeFieldType = (v: unknown): FieldType | null => {
  if (v === 'text' || v === 'table') return v
  return null
}

const onRootFieldTypeChange = (index: number, nextTypeRaw: unknown) => {
  const nextType = normalizeFieldType(nextTypeRaw)
  if (!nextType) return
  const current = templateField.value[index]
  if (!current || current.fieldType === nextType) return

  if (current.fieldType === 'text' && nextType === 'table') {
    const backupChildren = (current as any).__tableBackupChildren as TextField[] | undefined
    templateField.value[index] = createTableField({
      prompt: current.prompt,
      fieldName: current.fieldName,
      children: backupChildren?.length
        ? backupChildren
        : [createTextField({ fieldName: current.fieldName, mapping: current.mapping })]
    })
    // 更新 childPromptPopoverShow
    const childrenLength = templateField.value[index].fieldType === 'table' 
      ? (templateField.value[index] as TableField).children.length 
      : 0
    childPromptPopoverShow.value[index] = new Array(childrenLength).fill(false)
    return
  }

  if (current.fieldType === 'table' && nextType === 'text') {
    const first = current.children?.[0]
    const newText = createTextField({
      prompt: current.prompt,
      fieldName: current.fieldName,
      mapping: first?.mapping ?? ''
    })
    ;(newText as any).__tableBackupChildren = current.children
    templateField.value[index] = newText
    // 清空 childPromptPopoverShow
    childPromptPopoverShow.value[index] = []
  }
}

// 以第X页作为模板
const templatePage = ref(1)
const templatePageInput = ref<number | string>(1)

// templatePage 变化时同步 input 显示
watch(templatePage, (val) => {
  templatePageInput.value = val
})

// input 失焦或回车时校验
const onTemplatePageBlur = () => {
  let val = Number(templatePageInput.value)
  if (!Number.isInteger(val) || isNaN(val) || val < 1) {
    val = 1
  } else if (pdfPage.value && val > pdfPage.value) {
    val = pdfPage.value
  }
  templatePage.value = val
  templatePageInput.value = val
}

const customTextIndex = ref()
const customTextPageIndex = ref()
const customTableIndex = ref()
const customTableListIndex = ref()
const customTableSunIndex = ref()
const activeCustomTableMenu = ref<{
  pageKey: string
  listIndex: number
  rowIndex: number
  colIndex: number
} | null>(null)
const customTableMenuPosition = ref({ left: 0, top: 0 })
const customTableMenuStyle = computed(() => ({
  left: `${customTableMenuPosition.value.left}px`,
  top: `${customTableMenuPosition.value.top}px`
}))
const customPageTableKey = ref()
const customTextNum = ref(0)
const customTableNum = ref(0)
const resultType = ref('all')
const firstExtract = ref(true)
const customPageShow = ref<boolean[]>([])
interface contentEditType {
  status: boolean,
  hover: boolean,
  copy: string
}
const customContentTable = ref<boolean[]>([])
const customContentEdit = ref<Array<contentEditType[]>>([])

const editTemplateName = () => {
  editName.value = false
  templateNameRef.value.focus()
}

const handleMouseenter = (pageIndex: number, index: number) => {
  customContentEdit.value[pageIndex][index].hover = true
}

const handleMouseleave = (pageIndex: number, index: number) => {
  customContentEdit.value[pageIndex][index].hover = false
}

// 拖拽上传文件
const onDrop = async (e: DragEvent) => {
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  const postFile = files[0]
  dialogVisibleLoading.value = false
  const nameArray = postFile.name.split('.')
  downloadName.value = nameArray[0]
  fileType.value = nameArray[nameArray.length - 1].toLowerCase()
  if (fileType.value === 'pdf') {
    await UI.value.loadDocument(files[0])
    picSrc.value = ''
  } else if (['jpg', 'png', 'jpeg'].includes(fileType.value)) {
    let reader = new FileReader()
    reader.readAsDataURL(postFile)
    reader.onload = () => {
      picSrc.value = reader.result as string
    }
  } else {
    dragover.value = false
    ElMessage.error(t('bulkExtract.notSupport'))
    return
  }
  file.value = postFile
}

const downloadCSV = () => {
  const zip = new JSZip()
  const data = editableTabs.value.customDetails

  Object.keys(data).forEach(key => {
    const page = data[key]
    const { tables, ...invoiceDetails } = page || {}

    let hasContent = false
    let csvContent = ''

    // 发票信息部分
    const detailsArray = Object.entries(invoiceDetails || {}).map(([k, v]) => ({
      Key: k,
      Value: JSON.stringify(v)
    }))
    if (detailsArray.length) {
      const detailsCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(detailsArray))
      csvContent += detailsCSV
      hasContent = true
    }

    // 表格部分
    if (tables && typeof tables === 'object') {
      Object.entries(tables).forEach(([tableKey, table]: [string, any]) => {
        if (Array.isArray(table) && table.length > 0) {
          const tableCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(table))
          csvContent += `\n\n${key} ${tableKey}\n` + tableCSV
          hasContent = true
        }
      })
    }

    // 如果有内容才写入 zip 文件
    if (hasContent) {
      zip.file(`${key}_compdf_ai_signle_extract.csv`, csvContent)
    }
  })

  // 生成并下载 zip（仅当有有效内容）
  zip.generateAsync({ type: 'blob' }).then((content) => {
    if (Object.keys(zip.files).length > 0) {
      saveAs(content, `${fileName.value}_compdf_ai_signle_extract.zip`)
    }
  })
}

const downloadSingleCsv = () => {
  const data = editableTabs.value.customDetails[customPageTableKey.value]
  const index = customTableListIndex.value
  if (index != null) {
    const tableCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(data?.tables?.[index] ?? []))
    const csvData = tableCSV // 在每个表格前加入表格标识
    const blob = new Blob([csvData], { type: "text/csv;charset=utf-8;" })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${fileName.value}_compdf_ai_signle_extract.csv` // 设置文件名
    link.click()
  } else {
    const zip = new JSZip()
    const data = editableTabs.value.customDetails

    Object.keys(data).forEach(pageKey => {
      const page = data[pageKey]
      if (page && page.tables && typeof page.tables === 'object' && Object.keys(page.tables).length > 0) {
        let pageCSV = '' // 用于拼接该页面的所有表格数据

        Object.entries(page.tables).forEach(([tableKey, table]: [string, any]) => {
          if (Array.isArray(table) && table.length > 0) {
            const tableCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(table))
            pageCSV += `\n\n${pageKey} ${tableKey}\n` + tableCSV
          }
        })

        // 如果当前页面有任何表格内容，则加入 zip
        if (pageCSV.trim()) {
          zip.file(`${pageKey}_compdf_ai_signle_extract.csv`, pageCSV.trim())
        }
      }
    })

    // 如果有有效文件才生成 ZIP
    zip.generateAsync({ type: 'blob' }).then(content => {
      if (Object.keys(zip.files).length > 0) {
        saveAs(content, `${downloadName.value}_compdf_ai_signle_extract.zip`)
      }
    })
  }
}

const downloadXlsx = async () => {
  const data = editableTabs.value.customDetails
  const zip = new JSZip()

  for (const key of Object.keys(data)) {
    const item = data[key]
    if (!item || Object.keys(item).length === 0) continue

    const { tables, ...invoiceDetails } = item
    const workbook = XLSX.utils.book_new()

    // 添加发票信息
    const detailsArray = Object.entries(invoiceDetails).map(([k, v]) => ({ Key: k, Value: JSON.stringify(v) }))
    if (detailsArray.length > 0) {
      const sheet = XLSX.utils.json_to_sheet(detailsArray)
      XLSX.utils.book_append_sheet(workbook, sheet, 'Text Field')
    }

    // 添加表格信息，每页的每张表
    if (tables && typeof tables === 'object' && !Array.isArray(tables)) {
      Object.entries(tables).forEach(([tableKey, table]: [string, any]) => {
        if (Array.isArray(table) && table.length > 0) {
          const tableSheet = XLSX.utils.json_to_sheet(table)
          XLSX.utils.book_append_sheet(workbook, tableSheet, `Page-${key}_${tableKey}`)
        }
      })
    }

    // 如果有有效的 Sheet，则导出成 xlsx 并放入 zip
    if (workbook.SheetNames.length > 0) {
      const wbout = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
      zip.file(`${fileName.value}_${key}_compdf_ai_signle_extract.xlsx`, wbout)
    }
  }

  // 生成并下载 zip 文件
  const content = await zip.generateAsync({ type: 'blob' })
  saveAs(content, `${fileName.value}_compdf_ai_signle_extract.zip`)
}

const downloadSingleXlsx = async () => {
  const workbook = XLSX.utils.book_new()
  const data = editableTabs.value.customDetails[customPageTableKey.value]
  const index = customTableListIndex.value
  if (index != null) {
    const tableSheet = XLSX.utils.json_to_sheet(data?.tables?.[index] ?? [])
    // 添加每个表格为单独的工作表
    XLSX.utils.book_append_sheet(workbook, tableSheet, 'Table')
    XLSX.writeFile(workbook, `${fileName.value}_compdf_ai_signle_extract.xlsx`)
  } else {
    const data = editableTabs.value.customDetails
    const zip = new JSZip()

    Object.keys(data).forEach(pageKey => {
      const page = data[pageKey]
      if (!page || !page.tables || Object.keys(page.tables).length === 0) return

      const workbook = XLSX.utils.book_new()

      Object.entries(page.tables).forEach(([tableKey, table]: [string, any]) => {
        if (!Array.isArray(table) || table.length === 0) return
        const sheet = XLSX.utils.json_to_sheet(table)
        const sheetName = `${pageKey}_${tableKey}`
        XLSX.utils.book_append_sheet(workbook, sheet, sheetName)
      })

      if (workbook.SheetNames.length > 0) {
        const out = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
        zip.file(`${fileName.value}_${pageKey}_compdf_ai_signle_extract.xlsx`, out)
      }
    })

    const blob = await zip.generateAsync({ type: 'blob' })
    saveAs(blob, `${fileName.value}_compdf_ai_signle_extract.zip`)
  }
}

const startCustomTableEdit = (pageKey: string, listIndex: number) => {
  if (isResultReadOnly.value) return
  updateDate()
  customContentTable.value[listIndex] = true
  customPageTableKey.value = pageKey
  customTableListIndex.value = listIndex
  customTableIndex.value = 0
  customTableSunIndex.value = 0
  focusTable(listIndex, pageKey, 0, 0)
}

const onCustomTableCellClick = (pageKey: string, listIndex: number, rowIndex: number, colIndex: number, syncCurrent = true) => {
  if (isResultReadOnly.value) return
  if (syncCurrent && !activeEditSnapshot.value) {
    updateDate()
    captureEditSnapshot()
  }
  customPageTableKey.value = pageKey
  customTableListIndex.value = listIndex
  customTableIndex.value = rowIndex
  customTableSunIndex.value = colIndex
}

const openCustomTableMenu = (pageKey: string, listIndex: number, rowIndex: number, colIndex: number, event?: MouseEvent) => {
  if (isResultReadOnly.value) {
    closeCustomTableMenu()
    return
  }
  if (isCustomTableMenuOpen(pageKey, listIndex, rowIndex, colIndex)) {
    closeCustomTableMenu()
    return
  }
  onCustomTableCellClick(pageKey, listIndex, rowIndex, colIndex)
  const target = event?.currentTarget as HTMLElement | null
  if (target) {
    const rect = target.getBoundingClientRect()
    const menuWidth = 204
    const menuHeight = 190
    const gap = 4
    const preferredLeft = rect.right - menuWidth
    const preferredTop = rect.bottom + gap
    customTableMenuPosition.value = {
      left: Math.max(8, Math.min(preferredLeft, window.innerWidth - menuWidth - 8)),
      top: Math.max(8, Math.min(preferredTop, window.innerHeight - menuHeight - 8))
    }
  }
  activeCustomTableMenu.value = { pageKey, listIndex, rowIndex, colIndex }
}

const isCustomTableMenuOpen = (pageKey: string, listIndex: number, rowIndex: number, colIndex: number) => {
  const active = activeCustomTableMenu.value
  return !!active
    && active.pageKey === pageKey
    && active.listIndex === listIndex
    && active.rowIndex === rowIndex
    && active.colIndex === colIndex
}

const closeCustomTableMenu = () => {
  activeCustomTableMenu.value = null
}

const onCustomTableCellBlur = (
  pageKey: string,
  listIndex: number,
  rowIndex: number,
  key: string,
  colIndex: number,
  event: FocusEvent
) => {
  if (isResultReadOnly.value) {
    const row = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]?.[rowIndex]
    const target = event.target as HTMLElement | null
    if (row && target) target.innerText = row[key] ?? ''
    activeEditSnapshot.value = ''
    return
  }
  const text = (event.target as HTMLElement | null)?.innerText ?? ''
  const row = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]?.[rowIndex]
  if (!row) return
  const beforeSnapshot = activeEditSnapshot.value || stringifyCustomDetails()
  if (row[key] !== text) {
    isDirty.value = true
    row[key] = text
    pushEditHistory(beforeSnapshot)
  }
  activeEditSnapshot.value = ''
  customPageTableKey.value = pageKey
  customTableListIndex.value = listIndex
  customTableIndex.value = rowIndex
  customTableSunIndex.value = colIndex
}

// 表头编辑完成后重命名列
const onCustomTableHeaderBlur = (
  pageKey: string,
  listIndex: number,
  oldKey: string,
  _colIndex: number,
  event: FocusEvent
) => {
  if (isResultReadOnly.value) {
    const target = event.target as HTMLElement | null
    if (target) target.innerText = oldKey
    activeEditSnapshot.value = ''
    return
  }
  const newKey = (event.target as HTMLElement | null)?.innerText?.trim() ?? ''
  if (!newKey || newKey === oldKey) return

  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table || !Array.isArray(table)) return

  // 检查新列名是否已存在
  const existingKeys = Object.keys(table[0] ?? {})
  if (existingKeys.includes(newKey)) {
    // 恢复原列名
    ;(event.target as HTMLElement).innerText = oldKey
    activeEditSnapshot.value = ''
    ElMessage.warning(t('extraction.already'))
    return
  }

  const beforeSnapshot = activeEditSnapshot.value || stringifyCustomDetails()
  isDirty.value = true

  // 重命名所有行中的该列 key，保持列顺序
  table.forEach((row: Record<string, any>) => {
    if (row && typeof row === 'object' && oldKey in row) {
      const entries = Object.entries(row)
      const newEntries = entries.map(([k, v]) => (k === oldKey ? [newKey, v] : [k, v]))
      // 清空原对象并按新顺序赋值
      Object.keys(row).forEach(k => delete row[k])
      newEntries.forEach(([k, v]) => { row[k] = v })
    }
  })
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
}

const getDefaultNewColumnName = (existing: string[]) => {
  let i = 1
  while (existing.includes(`Column${i}`)) i++
  return `Column${i}`
}

const promptColumnName = async (existing: string[]) => {
  const defaultName = getDefaultNewColumnName(existing)
  try {
    const { value } = await ElMessageBox.prompt('Column name', '', {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      inputValue: defaultName,
      inputValidator: (val: string) => {
        if (!val?.trim()) return 'Required'
        if (existing.includes(val.trim())) return 'Already exists'
        return true
      }
    })
    return (value as string).trim()
  } catch {
    return null
  }
}

const promptFieldKeyName = async (existing: string[]) => {
  try {
    const { value } = await ElMessageBox.prompt(t('extraction.addAKey'), '', {
      modalClass: 'addKey',
      confirmButtonText: t('extraction.ok'),
      cancelButtonText: t('extraction.cancel'),
      inputPlaceholder: t('extraction.enterKey'),
      inputValidator: (val: string) => {
        const key = val?.trim()
        if (!key) return t('extraction.enterKey')
        if (key === 'tables') return t('extraction.enterKey')
        if (existing.includes(key)) return t('extraction.already')
        return true
      }
    })
    return (value as string).trim()
  } catch {
    return null
  }
}

const ensureEditStateRow = (pageKey: string) => {
  const details = editableTabs.value.customDetails
  const states = customContentEdit
  const pageIndex = Object.keys(details).indexOf(pageKey)
  if (pageIndex < 0) return
  if (!states.value[pageIndex]) states.value[pageIndex] = []
}

const deleteCustomField = (pageKey: string, pageIndex: number, fieldIndex: number, fieldKey: string) => {
  if (isResultReadOnly.value) return
  updateDate()

  const details = editableTabs.value.customDetails
  const page = details?.[pageKey]
  if (!page || typeof page !== 'object' || fieldKey === 'tables') return

  const beforeSnapshot = stringifyCustomDetails()
  const { tables, ...rest } = page as Record<string, any>

  if (!(fieldKey in rest)) return

  const nextEntries = Object.entries(rest).filter(([key]) => key !== fieldKey)
  details[pageKey] = tables !== undefined
    ? Object.fromEntries([...nextEntries, ['tables', tables]])
    : Object.fromEntries(nextEntries)

  if (customContentEdit.value[pageIndex]) {
    customContentEdit.value[pageIndex].splice(fieldIndex, 1)
  }

  customTextNum.value = Math.max(customTextNum.value - 1, 0)
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
}

/**
 * 新增一个 key-value 字段（非 tables）。
 * 写入 editableTabs.customDetails
 *
 * pageKey 为空时会自动使用当前选中的 customPageTableKey。
 */
const addKeyValue = async (pageKey?: string) => {
  if (isResultReadOnly.value) return
  updateDate()

  const resolvedPageKey = pageKey ?? customPageTableKey.value
  if (!resolvedPageKey) {
    ElMessage.warning(t('extraction.selectPageBeforeAddField'))
    return
  }

  const details = editableTabs.value.customDetails
  const page = details?.[resolvedPageKey]
  if (!page || typeof page !== 'object') return

  const existingKeys = Object.keys(page).filter((k) => k !== 'tables')
  const newKey = await promptFieldKeyName(existingKeys)
  if (!newKey) return

  const beforeSnapshot = stringifyCustomDetails()
  // value 直接为空字符串
  const newValue = t('extraction.enterValue')

  isDirty.value = true

  // 保证 tables 始终在最后
  // @ts-ignore
  const { tables, ...rest } = page
  // @ts-ignore
  details[resolvedPageKey] = tables !== undefined ? { ...rest, [newKey]: newValue, tables } : { ...rest, [newKey]: newValue }

  // 同步 hover/edit 状态数组，否则模板里 customContentEdit[pageIndex][index] 会错位
  ensureEditStateRow(resolvedPageKey)
  const states = customContentEdit
  const pageIndex = Object.keys(details).indexOf(resolvedPageKey)
  if (pageIndex >= 0) {
    states.value[pageIndex].push({
      hover: true,
      status: false,
      copy: t('singleExtract.copy')
    })
  }

  customTextNum.value += 1
  pushEditHistory(beforeSnapshot)
}

const ensureNonEmptyTable = (table: any[]) => {
  if (Array.isArray(table) && table.length > 0) return
  table.push({})
}

const getRowKeysByIndex = (row: Record<string, any> | undefined, colIndex: number) => {
  if (!row) return null
  const keys = Object.keys(row)
  return keys[colIndex] ?? null
}

const addRowToTable = (table: any[], insertAfterIndex: number | null) => {
  ensureNonEmptyTable(table)
  const keys = Object.keys(table[0] ?? {})
  const newRow = keys.reduce((acc: Record<string, any>, k) => {
    acc[k] = ''
    return acc
  }, {})
  const at = typeof insertAfterIndex === 'number' ? insertAfterIndex + 1 : table.length
  table.splice(Math.min(Math.max(at, 0), table.length), 0, newRow)
}

const insertRowToTable = (table: any[], rowIndex: number | null, placement: 'above' | 'below') => {
  ensureNonEmptyTable(table)
  const keys = Object.keys(table[0] ?? {})
  const newRow = keys.reduce((acc: Record<string, any>, k) => {
    acc[k] = ''
    return acc
  }, {})
  const baseIndex = typeof rowIndex === 'number' ? rowIndex : table.length - 1
  const at = placement === 'above' ? baseIndex : baseIndex + 1
  table.splice(Math.min(Math.max(at, 0), table.length), 0, newRow)
  return Math.min(Math.max(at, 0), table.length - 1)
}

const addColumnToTable = async (table: any[]) => {
  ensureNonEmptyTable(table)
  const existingKeys = Object.keys(table[0] ?? {})
  const newKey = await promptColumnName(existingKeys)
  if (!newKey) return
  table.forEach((row: any) => {
    if (row && typeof row === 'object') row[newKey] = ''
  })
}

const createUniqueColumnKey = (existingKeys: string[]) => {
  let index = existingKeys.length + 1
  let key = `Column ${index}`
  while (existingKeys.includes(key)) {
    index++
    key = `Column ${index}`
  }
  return key
}

const insertColumnToTable = (table: any[], colIndex: number | null, placement: 'above' | 'below') => {
  ensureNonEmptyTable(table)
  const existingKeys = Object.keys(table[0] ?? {})
  const newKey = createUniqueColumnKey(existingKeys)
  const baseIndex = typeof colIndex === 'number' ? colIndex : existingKeys.length - 1
  const targetIndex = placement === 'above' ? baseIndex : baseIndex + 1
  const insertIndex = Math.min(Math.max(targetIndex, 0), existingKeys.length)
  table.forEach((row: any) => {
    if (!row || typeof row !== 'object') return
    const entries = Object.entries(row)
    entries.splice(insertIndex, 0, [newKey, ''])
    Object.keys(row).forEach(key => delete row[key])
    entries.forEach(([key, value]) => {
      row[key] = value
    })
  })
  return insertIndex
}

const deleteRowFromTable = (table: any[], rowIndex: number | null) => {
  ensureNonEmptyTable(table)
  const idx = typeof rowIndex === 'number' ? rowIndex : 0
  if (table.length <= 1) {
    const row = table[0] ?? {}
    Object.keys(row).forEach((k) => (row[k] = ''))
    return
  }
  table.splice(Math.min(Math.max(idx, 0), table.length - 1), 1)
}

const deleteColumnFromTable = (table: any[], colIndex: number | null) => {
  ensureNonEmptyTable(table)
  const row = table[0] ?? {}
  const idx = typeof colIndex === 'number' ? colIndex : 0
  const key = getRowKeysByIndex(row, idx)
  if (!key) return
  table.forEach((r: any) => {
    if (r && typeof r === 'object') delete r[key]
  })
}

const addCustomTableRow = async (pageKey: string, listIndex: number) => {
  if (isResultReadOnly.value) return
  updateDate()
  customContentTable.value[listIndex] = true
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const beforeSnapshot = stringifyCustomDetails()
  const insertAfter = customTableListIndex.value === listIndex ? (customTableIndex.value ?? null) : null
  addRowToTable(table, insertAfter)
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
  await nextTick()
  const newRowIndex = typeof insertAfter === 'number' ? insertAfter + 1 : table.length - 1
  onCustomTableCellClick(pageKey, listIndex, newRowIndex, 0, false)
  focusTable(listIndex, pageKey, newRowIndex, 0)
}

const insertCustomTableRow = async (pageKey: string, listIndex: number, placement: 'above' | 'below') => {
  if (isResultReadOnly.value) return
  updateDate()
  customContentTable.value[listIndex] = true
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const beforeSnapshot = stringifyCustomDetails()
  const rowIndex = customTableListIndex.value === listIndex ? (customTableIndex.value ?? null) : null
  const newRowIndex = insertRowToTable(table, rowIndex, placement)
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
  await nextTick()
  onCustomTableCellClick(pageKey, listIndex, newRowIndex, customTableSunIndex.value ?? 0, false)
  focusTable(listIndex, pageKey, newRowIndex, customTableSunIndex.value ?? 0)
}

const addCustomTableColumn = async (pageKey: string, listIndex: number) => {
  if (isResultReadOnly.value) return
  updateDate()
  customContentTable.value[listIndex] = true
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const beforeSnapshot = stringifyCustomDetails()
  await addColumnToTable(table)
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
  await nextTick()
  onCustomTableCellClick(pageKey, listIndex, customTableIndex.value ?? 0, 0, false)
}

const deleteCustomTableRow = async (pageKey: string, listIndex: number) => {
  if (isResultReadOnly.value) return
  updateDate()
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const beforeSnapshot = stringifyCustomDetails()
  const idx = customTableListIndex.value === listIndex ? (customTableIndex.value ?? 0) : 0
  deleteRowFromTable(table, idx)
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
  await nextTick()
  const nextRow = Math.min(idx, table.length - 1)
  onCustomTableCellClick(pageKey, listIndex, nextRow, 0, false)
}

const deleteCustomTableColumn = async (pageKey: string, listIndex: number) => {
  if (isResultReadOnly.value) return
  updateDate()
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const beforeSnapshot = stringifyCustomDetails()
  const idx = customTableListIndex.value === listIndex ? (customTableSunIndex.value ?? 0) : 0
  deleteColumnFromTable(table, idx)
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
  await nextTick()
  onCustomTableCellClick(pageKey, listIndex, customTableIndex.value ?? 0, 0, false)
}

const insertCustomTableColumn = async (pageKey: string, listIndex: number, placement: 'above' | 'below') => {
  if (isResultReadOnly.value) return
  updateDate()
  customContentTable.value[listIndex] = true
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const beforeSnapshot = stringifyCustomDetails()
  const colIndex = customTableListIndex.value === listIndex ? (customTableSunIndex.value ?? null) : null
  const newColIndex = insertColumnToTable(table, colIndex, placement)
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
  await nextTick()
  onCustomTableCellClick(pageKey, listIndex, customTableIndex.value ?? 0, newColIndex, false)
  focusTable(listIndex, pageKey, customTableIndex.value ?? 0, newColIndex)
}

const handleCustomTableMenuAction = async (action: 'insert-above' | 'insert-below' | 'insert-column-above' | 'insert-column-below' | 'delete-row' | 'delete-column') => {
  if (isResultReadOnly.value) {
    closeCustomTableMenu()
    return
  }
  const active = activeCustomTableMenu.value
  if (!active) return
  const { pageKey, listIndex, rowIndex, colIndex } = active
  onCustomTableCellClick(pageKey, listIndex, rowIndex, colIndex, false)
  closeCustomTableMenu()
  if (action === 'insert-above') {
    await insertCustomTableRow(pageKey, listIndex, 'above')
  } else if (action === 'insert-below') {
    await insertCustomTableRow(pageKey, listIndex, 'below')
  } else if (action === 'insert-column-above') {
    await insertCustomTableColumn(pageKey, listIndex, 'above')
  } else if (action === 'insert-column-below') {
    await insertCustomTableColumn(pageKey, listIndex, 'below')
  } else if (action === 'delete-row') {
    await deleteCustomTableRow(pageKey, listIndex)
  } else {
    await deleteCustomTableColumn(pageKey, listIndex)
  }
}
const isNumber = (val: number) => typeof val === 'number'
watch(dialogVisibleSetName, (val: boolean, _value: boolean) => {
  if (!val) {
    edit.value = false
  }
})
const handleWatch = (customPageTableKey: Ref<string>, listIndex: Ref<number>, tableIndex: Ref<number>, sunIndex: Ref<number>): void => {
  if (isRestoringEditHistory.value || !activeEditSnapshot.value) return
  if (isNumber(listIndex.value) && isNumber(tableIndex.value) && isNumber(sunIndex.value)) {
    changeCustomTableDate(customPageTableKey.value, listIndex.value, tableIndex.value, sunIndex.value)
  }
}
watch(customPageTableKey, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
watch(customTableListIndex, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
watch(customTableIndex, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
watch(customTableSunIndex, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
const changeCustomTableDate = (customPageTableKey: string, value: number, valueChi: number, valueSun: number) => {
  if (isRestoringEditHistory.value) return
  const dec = document.getElementById(`customTable${customPageTableKey}${value}${valueChi}${valueSun}`)
  const row = editableTabs.value.customDetails?.[customPageTableKey]?.tables?.[value]?.[valueChi]
  if (!row || !dec) return
  const keys = Object.keys(row)
  const key = keys[valueSun]
  if (key !== undefined) row[key] = dec.innerText
}
const copy = (content: any, pageIndex: number, index: number) => {
  if (pageIndex !== -1) {
    clipboard(content, {
      format: 'text/plain'
    })
    const target = customContentEdit.value
    target[pageIndex][index].copy = t('singleExtract.copied')
    if (timer.value) {
      clearTimeout(timer.value)
      timer.value = null
    }
    timer.value = setTimeout(() => {
      const target = customContentEdit.value
      target[pageIndex][index].copy = t('singleExtract.copy')
    }, 1500)
  } else {
    clipboard(JSON.stringify(content, null, 2), {
      format: 'text/plain'
    })
    ElMessage({
      message: t('singleExtract.copied'),
      type: 'success',
      duration: 3000
    })
  }
}
const input = ref()
const tableDownload = ref(false)
const guide = ref(false)
const setting = ref(false)
const dialogVisible = ref(false)
const toType = ref('json')
const file = ref<File>()
const login = ref(false)
const stepOne = ref(false)
const noResult = ref(false)
const outputType = ref('txt')
const downloadName = ref('invoice-example')
const dialogVisibleLoading = ref(false)
const viewer = ref()
const UI = ref()
const customDownload = ref(false)
let docViewer: any = null
const img = ref()
const picSrc = ref('')

// 当 picSrc 变化时自动释放上一个 blob URL，防止内存泄漏
watch(picSrc, (_newVal, oldVal) => {
  if (oldVal && oldVal.startsWith('blob:')) {
    URL.revokeObjectURL(oldVal)
  }
})

const initFile = ref('one')
const fileType = ref('pdf')
const isWidthBigger = ref()
type TableItem = Record<string, any>

// 切换右侧结果展示格式：Text 使用可编辑字段视图，JSON 使用同一份抽取结果的结构化预览。
const changeConvert = (val: 'txt' | 'json') => {
  if (dialogVisibleLoading.value) return
  if (outputType.value === val) return
  flushActiveEdit()
  outputType.value = val
}

type PageData = {
  [key: string]: unknown
  tables?: TableItem[][]
}

type DetailsType = Record<string, PageData>
interface dataList {
  customDetails: DetailsType,
  tableList: Array<string>,
  fieldsList: Array<string>
}
const lang = ref('0')
const editableTabs = ref<dataList>({
  customDetails: {},
  tableList: [],
  fieldsList: []
})

const EDIT_HISTORY_LIMIT = 50
const undoHistory = ref<string[]>([])
const redoHistory = ref<string[]>([])
const activeEditSnapshot = ref('')
const isRestoringEditHistory = ref(false)

const stringifyCustomDetails = () => JSON.stringify(editableTabs.value.customDetails || {})

const resetEditHistory = () => {
  undoHistory.value = []
  redoHistory.value = []
  activeEditSnapshot.value = ''
}

const captureEditSnapshot = () => {
  if (isResultReadOnly.value) return
  if (isRestoringEditHistory.value) return
  activeEditSnapshot.value = stringifyCustomDetails()
}

const pushEditHistory = (beforeSnapshot: string) => {
  if (isRestoringEditHistory.value || !beforeSnapshot) return
  const afterSnapshot = stringifyCustomDetails()
  if (beforeSnapshot === afterSnapshot) return
  undoHistory.value.push(beforeSnapshot)
  if (undoHistory.value.length > EDIT_HISTORY_LIMIT) {
    undoHistory.value.shift()
  }
  redoHistory.value = []
}

const flushActiveEdit = () => {
  if (isResultReadOnly.value) {
    activeEditSnapshot.value = ''
    return
  }
  if (!activeEditSnapshot.value) return
  const beforeSnapshot = activeEditSnapshot.value
  updateDate()
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
}

const restoreEditSnapshot = (snapshot: string) => {
  try {
    isRestoringEditHistory.value = true
    const parsed = snapshot ? JSON.parse(snapshot) : {}
    handleData(parsed)
    noResult.value = Object.keys(editableTabs.value.customDetails || {}).length === 0
    isDirty.value = true
    closeCustomTableMenu()
  } finally {
    nextTick(() => {
      isRestoringEditHistory.value = false
    })
  }
}

const undoEdit = () => {
  if (isResultReadOnly.value) return
  flushActiveEdit()
  const previousSnapshot = undoHistory.value.pop()
  if (!previousSnapshot) return
  const currentSnapshot = stringifyCustomDetails()
  redoHistory.value.push(currentSnapshot)
  restoreEditSnapshot(previousSnapshot)
}

const redoEdit = () => {
  if (isResultReadOnly.value) return
  const nextSnapshot = redoHistory.value.pop()
  if (!nextSnapshot) return
  const currentSnapshot = stringifyCustomDetails()
  undoHistory.value.push(currentSnapshot)
  if (undoHistory.value.length > EDIT_HISTORY_LIMIT) {
    undoHistory.value.shift()
  }
  restoreEditSnapshot(nextSnapshot)
}

const handleGlobalClick = () => {
  setting.value = false
  pullShow.value = false
  closeCustomTableMenu()
  customContentEdit.value.forEach((page: Array<contentEditType>) => {
    page.forEach((item: contentEditType) => {
      item.status = false
    })
  })
  if (!isResultReadOnly.value) {
    flushActiveEdit()
    updateDate()
  }
}

const focusContentEditable = (id: string) => {
  if (isResultReadOnly.value) return
  nextTick(() => {
    const el = document.getElementById(id)
    if (!el) return
    el.focus()
    // 将光标移到末尾
    const range = document.createRange()
    range.selectNodeContents(el)
    range.collapse(false)
    const sel = window.getSelection()
    sel?.removeAllRanges()
    sel?.addRange(range)
  })
}

const focusTable = (listIndex: number, pageKey: string, index: number, sunIndex: number) => {
  if (isResultReadOnly.value) return
  const dom = document.getElementById(`customTable${pageKey}${listIndex}${index}${sunIndex}`)
  nextTick(() => {
    if (dom) {
      dom.focus()
      // 设置光标到末尾
      const range = document.createRange()
      const selection = window.getSelection()
      range.selectNodeContents(dom)
      range.collapse(false)
      if (selection) {
        selection.removeAllRanges()
        selection.addRange(range)
      }
    }
  })
}

const startCustomTextEdit = (pageKey: string, pageIndex: number, index: number) => {
  if (isResultReadOnly.value) return
  updateDate()
  captureEditSnapshot()
  customContentEdit.value[pageIndex][index].status = true
  customPageTableKey.value = pageKey
  customTextIndex.value = index
  customTextPageIndex.value = pageIndex
  focusContentEditable(`customText${pageKey}${index}`)
}

const onCustomTextBlur = () => {
  if (isResultReadOnly.value) return
  flushActiveEdit()
  isDirty.value = true
}

/**
 * Key 编辑完成时：重命名 customDetails 里对应的 key，保持原有顺序
 */
const onCustomKeyBlur = (
  pageKey: string,
  _pageIndex: number,
  _index: number,
  oldKey: string | number,
  event: FocusEvent
) => {
  if (isResultReadOnly.value) {
    const target = event.target as HTMLElement | null
    if (target) target.innerText = String(oldKey)
    activeEditSnapshot.value = ''
    return
  }
  const newKey = (event.target as HTMLElement).innerText.trim()
  if (!newKey || newKey === String(oldKey)) return

  const details = editableTabs.value.customDetails
  const page = details?.[pageKey]
  if (!page || typeof page !== 'object') return

  // 检查是否与已有 key 重复
  if (Object.prototype.hasOwnProperty.call(page, newKey)) {
    // 还原显示为旧 key
    ;(event.target as HTMLElement).innerText = String(oldKey)
    activeEditSnapshot.value = ''
    ElMessage.warning(t('extraction.keyExists'))
    return
  }

  const beforeSnapshot = activeEditSnapshot.value || stringifyCustomDetails()

  // 重建对象，保持 key 顺序
  const newPage: Record<string, any> = {}
  for (const k of Object.keys(page)) {
    if (k === String(oldKey)) {
      newPage[newKey] = page[k]
    } else {
      newPage[k] = page[k]
    }
  }
  details[pageKey] = newPage
  isDirty.value = true
  pushEditHistory(beforeSnapshot)
  activeEditSnapshot.value = ''
}

const updateDate = () => {
  if (isResultReadOnly.value) return
  const updateDetails = (
    details: any,
    tablePrefix: string,
    textPrefix: string,
    tableListIndex: Ref<number>,
    tableIndex: Ref<number | null>,
    tableSunIndex: Ref<number | null>,
    textIndex: Ref<number | null>,
    pageTableKey: Ref<string | null>
  ): void => {
    // 更新表格内容
    if (tableIndex.value !== null && tableSunIndex.value !== null) {
      const tableId = `${tablePrefix}${pageTableKey.value}${tableListIndex.value}${tableIndex.value}${tableSunIndex.value}`
      const dec = document.getElementById(tableId)
      if (dec) {
        Object.keys(details[pageTableKey.value as keyof typeof details].tables[tableListIndex.value][tableIndex.value as number]).forEach((key, indexSun) => {
          if (indexSun === tableSunIndex.value) {
            details[pageTableKey.value as keyof typeof details].tables[tableListIndex.value][tableIndex.value as number][key] = dec.innerText
          }
        })
      }
    }
    // 更新文本内容
    if (textIndex.value != null && pageTableKey.value != null) {
      const textId = `${textPrefix}${pageTableKey.value}${textIndex.value}`
      const dec = document.getElementById(textId)
      const pageData = details[pageTableKey.value as keyof typeof details]
      if (dec && pageData) {
        Object.keys(pageData).forEach((key, index) => {
          if (index === textIndex.value) {
            pageData[key] = dec.innerText
          }
        })
      }
    }
  }
  // 只更新自定义类型的内容
  updateDetails(editableTabs.value.customDetails, 'customTable', 'customText', customTableListIndex, customTableIndex, customTableSunIndex, customTextIndex, customPageTableKey)
}
const handleKeyDown = (event: any) => {
  const target = event.target as HTMLElement | null
  const isInsideResult = Boolean(target?.closest?.('.extraction-result'))
  if (event.keyCode === 9 && isInsideResult) {
    event.preventDefault()
    return
  }
  const key = String(event.key || '').toLowerCase()
  const isModifierPressed = event.ctrlKey || event.metaKey
  const isUndo = isModifierPressed && key === 'z' && !event.shiftKey
  const isRedo = isModifierPressed && (key === 'y' || (key === 'z' && event.shiftKey))
  if (isUndo || isRedo) {
    if (isResultReadOnly.value) return
    if (isUndo && !undoHistory.value.length && !activeEditSnapshot.value) return
    if (isRedo && !redoHistory.value.length) return
    event.preventDefault()
    if (isUndo) {
      undoEdit()
    } else {
      redoEdit()
    }
  }
}
const changePage = (val: string) => {
  if (val === 'reduce') {
    docViewer.previousPage()
  } else {
    docViewer.nextPage()
  }
  pdfCurrentPage.value = docViewer.currentPageNumber
}
const setPage = () => {
  docViewer.pageNumberChanged(pdfCurrentPage.value)
  pdfCurrentPage.value = docViewer.currentPageNumber
}
const changeZoom = (zoom: number) => {
  if (zoom) {
    docViewer.zoomIn()
  } else {
    docViewer.zoomOut()
  }
  scale.value = Number((docViewer.scale * 100).toFixed(0))
}
const switchTool = () => {
  hand.value = hand.value ? 0 : 1
  docViewer.switchTool(hand.value)
}
const setScale = (val: number) => {
  scaleShow.value = false
  docViewer.webViewerScaleChanged(val / 100)
  scale.value = Number((docViewer.scale * 100).toFixed(0))
}
watch(() => lang, () => {
  setting.value = false
})

onMounted(async () => {
  showBtn.value = true
  addEventListener('click', handleGlobalClick)
  addEventListener('keydown', handleKeyDown)

  // 获取默认模板名称列表
  try {
    const { data: { data: defaultTemplates = [] } } = await get('/api/idp/get-default-template')
    defaultTemplateNames.value = new Set(defaultTemplates.map((item: any) => item.name))
  } catch (e) {
    console.error('Failed to fetch default templates:', e)
  }

  if (window.innerWidth > 930) {
    guide.value = true
    dialogVisibleLoading.value = false
    stepOne.value = localStorage.getItem('first') === '0' ? false : true
    login.value = stepOne.value
    if (login.value) {
      setTimeout(() => {
        login.value = false
      }, 5000)
    }
    const license = getEnv('LICENSE_KEY') || 'Ki6UpWkucL6aKcocIWVc/f6fUYgKpAYSp1jNWm6aAaDr7ADonPnxyKmJSP86hxQgdB6bwzmTgbXe/NRg5JjmxeQKrjYOA6aQH/NUE0p/YfVny07PfmMU7SX6+AQxlTbk+of2WJbt6wf69JxpfjO9Aj2iTq3eR1Vu8+Ue99Z3b/GKSoMjgmjBaSN21lScTJ230yeyVZc0rjdt+QVuDpwBJZfSzpQbBL+/tbYRUhex05kFAtBRUT0d0mNKb4NCTLwr/oPY3u+fZQNI1OwCN8MaeD0ozqfq+itk+tx8s0a3MS3QCBX39TsNqcDi/a5Vt5H04GbID51WuEKkb799UN7SB68kD+Q9C95FZo3W7DLPF5Id3tVLjwj02FGNgeewpeIdNgRNpzdDAHO+UDvFjQ41jdGQ4tgb2bpMiMt/INJeLobLnkbPIwad7n6f7KhGyOTDrhxz9BO+lj2kqK576aB5pF+vmAl2+odMJncYhWcfj8JC5BpjcgCtCkhzbU9v11R07ByAbYqOaoeXnOVdXCbwJZG/RtoaHdnu7QRtPR1L8IZQuqtYbmLAOPf/MKcZJNqiQ8d9Wf3kFPKfscpfcawvNc3nKDL98eIvaPVl9IniKvGs7pTFLtnXIbTW88FCzyKw/aXqrQ6Uhea+RDLGmQJTIojMr4vkPz6c/9gm/RtO/NOyxDGwhy7sHiAcwhIkwl7Zg9s6QB8YBY20hAMGEzV0IZjg27eaqBaClfh1dpIXutHIupoN7O0iH2Jm0duAoYGnMkmhDaatl0gIsUdFyLyd7MnnfM1/PN7JeZhPr1ZPbK6tx9N6XiFMi2eRPwL5TAyN2MIz0ggkq1jjnGXYXa6rdSrdVSe/zA9bYRrB2comG+xb98yVV9hO9gRfyBAAGHFKlkUdj1g1SrbTNwHG164RIhBoP12s3knqc8f8GjpGGk7G5BqgnSydp+Hzc38kd13p'
    ComPDFKitViewer.init({
      license,
      pdfUrl: '',
      path: '/',
      showToolbarControl: false,
      isRenderAnnotations: false,
      enableDefaultFont: true
    }, viewer.value).then((core: any) => {
      UI.value = core.UI
      docViewer = core.docViewer
      core.UI.disableElements(['pageNavOverlay'])
      core.UI.setLanguage(locale.value)
      core.UI.textPopup.update([])
      interface pageType {
        pageLabel: string
        pageNumber: number
        previous: number
      }
      core.docViewer.addEvent('onPageNumberUpdated', (data: pageType) => {
        pdfCurrentPage.value = data.pageNumber
      })
      docViewer.addEvent('documentloaded', async () => {
        navShow.value = true
        pdfPage.value = docViewer.pagesCount
        scale.value = Number((docViewer.scale * 100).toFixed(0))
      })
      type scaleType = {
        previous: number
        scale: number
      }
      docViewer.addEvent('scalechanging', (data: scaleType) => {
        scale.value = Number((data.scale * 100).toFixed(0))
      })
    })
  }
})

onUnmounted(() => {
  removeEventListener('click', handleGlobalClick)
  removeEventListener('keydown', handleKeyDown)
  if (picSrc.value && picSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(picSrc.value)
  }
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
})

const shouldSkipProcessing = computed(() => {
  return false
})

interface FileData {
  fileId: string
  fileName: string
  pageCount: number
  uploadTime: string
  fileDownUrl: string
  status: number
  resultDownUrl: string
  reviewStatus: number
  groupTemplateId: string
}

const changeActive = inject('changeActive', (_val: string, _row?: FileData) => {})

const isAddTemp = ref(false)
const isEditTemp = ref(false)
const fileId = ref<string>('')
const sourceTempName = ref<string>('')
const defaultTemplateNames = ref<Set<string>>(new Set())
const fileName = ref<string>('')
const fileStatus = ref<number | undefined>(undefined)
const reviewStatus = ref<number | undefined>(undefined)
const templateId = ref<string>('')
const groupTemplateId = ref<string>('')
const isConfigResult = ref<boolean>(false)
const isDirty = ref<boolean>(false)
const isExtractionFailed = computed(() => fileStatus.value === 3 || fileStatus.value === 8 || fileStatus.value === 12)
const originalTemplateSnapshot = ref<string>('')
const originalTempName = ref<string>('')

const shouldUpdateTemplate = computed(() => {
  return isEditTemp.value && !defaultTemplateNames.value.has(sourceTempName.value)
})

// 按钮文案：编辑默认模板时显示"另存为"，其他情况（新建、编辑自定义）显示"保存"
const saveButtonLabel = computed(() => {
  if (isEditTemp.value && defaultTemplateNames.value.has(sourceTempName.value)) {
    return t('extraction.saveAs')
  }
  return t('extraction.save')
})

// 模板字段是否有效：至少有一行 fieldName 非空
const hasValidField = computed(() => {
  return templateField.value.some(item => item.fieldName?.trim())
})

const isCurrentFileConfirmed = computed(() => reviewStatus.value === 1)
const isResultReadOnly = computed(() => isCurrentFileConfirmed.value)
const isConfirmButtonDisabled = computed(() => !isCurrentFileConfirmed.value && isConfigResult.value && !isDirty.value)
const confirmActionText = computed(() => isCurrentFileConfirmed.value ? t('extraction.revokeConfirmed') : t('extraction.confirm'))
const isSuccessfulExtractionStatus = (status?: number) => status === 2 || status === 11
const currentReviewStatusText = computed(() => reviewStatusText(reviewStatus.value, fileStatus.value))
const currentReviewStatusClass = computed(() => reviewStatusClass(reviewStatus.value, fileStatus.value))

const isTemplateDirty = computed(() => {
  if (!originalTemplateSnapshot.value) return true
  const currentSnapshot = JSON.stringify(transformTemplateField(templateField.value, tempName.value))
  return currentSnapshot !== originalTemplateSnapshot.value || tempName.value !== originalTempName.value
})

const handleSaveTemplate = () => {
  // 编辑默认模板 → 另存为：必须修改模板名称
  const isDefaultTemplate = isEditTemp.value && defaultTemplateNames.value.has(sourceTempName.value)
  if (isDefaultTemplate) {
    if (defaultTemplateNames.value.has(tempName.value)) {
      ElMessage.warning(t('extraction.saveAsRenameRequired'))
      return
    }
  } else if (isEditTemp.value && !isTemplateDirty.value && !isDirty.value) {
    ElMessage.warning(t('extraction.templateNoChange'))
    return
  }
  if (shouldUpdateTemplate.value) {
    return updateTemplate()
  }
  return addTemplate()
}

// 根据文件头(magic bytes)嗅探真实图片类型，避免后端返回 octet-stream 导致 <img> 无法渲染
const sniffImageType = (bytes: Uint8Array): string => {
  // PNG: 89 50 4E 47
  if (bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47) return 'image/png'
  // JPEG: FF D8 FF
  if (bytes[0] === 0xff && bytes[1] === 0xd8 && bytes[2] === 0xff) return 'image/jpeg'
  // GIF: 47 49 46 38
  if (bytes[0] === 0x47 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x38) return 'image/gif'
  // WebP: "RIFF"...."WEBP"
  if (bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[8] === 0x57) return 'image/webp'
  // BMP: 42 4D
  if (bytes[0] === 0x42 && bytes[1] === 0x4d) return 'image/bmp'
  return ''
}

// 判断字节真实类型：PDF(%PDF-) 或图片(magic bytes)，否则按文件名兜底
const sniffKind = (bytes: Uint8Array): 'pdf' | 'image' | '' => {
  // PDF: 25 50 44 46 2D = "%PDF-"
  if (bytes[0] === 0x25 && bytes[1] === 0x50 && bytes[2] === 0x44 && bytes[3] === 0x46 && bytes[4] === 0x2d) return 'pdf'
  // PNG
  if (bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47) return 'image'
  // JPEG
  if (bytes[0] === 0xff && bytes[1] === 0xd8 && bytes[2] === 0xff) return 'image'
  // GIF
  if (bytes[0] === 0x47 && bytes[1] === 0x49 && bytes[2] === 0x46) return 'image'
  // WebP
  if (bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[8] === 0x57) return 'image'
  // BMP
  if (bytes[0] === 0x42 && bytes[1] === 0x4d) return 'image'
  return ''
}

const urlToFile = async (url: string, filename: string): Promise<{ file: File; objectUrl: string; kind: 'pdf' | 'image' | 'unknown' }> => {
  // 用带 Authorization 的 axios 实例请求；plain fetch 不带 token，后端会返回非图片内容(如 401 JSON)，<img> 解析失败
  const response = await request.get(url, { responseType: 'blob' })
  const blob = response.data as Blob
  const buffer = await blob.arrayBuffer()
  const bytes = new Uint8Array(buffer)

  // 字节头判定真实类型优先；后端可能把图片转成 PDF 存储，文件名后缀并不可靠
  const kind = sniffKind(bytes) || (() => {
    const ext = filename.toLowerCase().split('.').pop() ?? ''
    if (ext === 'pdf') return 'pdf' as const
    if (['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp'].includes(ext)) return 'image' as const
    return '' as const
  })()

  // 确定 MIME type
  let type: string
  if (kind === 'pdf') {
    type = 'application/pdf'
  } else if (kind === 'image') {
    const rawType = blob.type || ''
    type = rawType && rawType !== 'application/octet-stream' ? rawType : sniffImageType(bytes)
    if (!type) {
      const ext = filename.toLowerCase().split('.').pop() ?? ''
      type = ({
        png: 'image/png', jpg: 'image/jpeg', jpeg: 'image/jpeg',
        gif: 'image/gif', webp: 'image/webp', bmp: 'image/bmp',
        svg: 'image/svg+xml',
      } as Record<string, string>)[ext] || 'application/octet-stream'
    }
  } else {
    type = 'application/octet-stream'
  }

  // blob.type 只读，用原始数据重建带正确 type 的 blob，objectUrl 才会是真正的图片类型
  const typedBlob = new Blob([buffer], { type })
  return {
    file: new File([buffer], filename, { type }),
    objectUrl: URL.createObjectURL(typedBlob),
    kind: kind || 'unknown',
  }
}

interface TemplateList {
  groupId: string
  id: string
  groupTemplateId: string
  templateId: string
  templateName: string
}

const normalizeTemplate = (item: any): TemplateList => ({
  groupId: item?.groupId || '',
  id: item?.id || '',
  groupTemplateId: item?.groupTemplateId || item?.id || '',
  templateId: item?.templateId || item?.id || '',
  templateName: item?.templateName || item?.name || '--'
})

const upsertTemplateOption = (template: TemplateList) => {
  if (!template.groupTemplateId) return
  const index = templateGroupOptions.value.findIndex(item =>
    item.groupTemplateId === template.groupTemplateId
    || (!!template.templateId && item.templateId === template.templateId)
    || (!!template.templateName && item.templateName === template.templateName)
  )
  if (index >= 0) {
    templateGroupOptions.value[index] = {
      ...templateGroupOptions.value[index],
      ...template,
      templateName: template.templateName || templateGroupOptions.value[index].templateName
    }
    return
  }
  templateGroupOptions.value.push(template)
}

const getFileExtension = (fileName?: string): string => {
  if (!fileName) return ''
  const lastDotIndex = fileName.lastIndexOf('.')
  // 没有后缀 / 以 . 开头的隐藏文件 / 以 . 结尾
  if (lastDotIndex <= 0 || lastDotIndex === fileName.length - 1) return ''
  return fileName.slice(lastDotIndex + 1).toLowerCase()
}
  
const defaultCollapse = (add: boolean) => {
  resetData()
  if (add) {
    collapse.value = false
    isAddTemp.value = true
    templateField.value = [
      {
        prompt: '',
        fieldType: 'text',
        fieldName: '',
        mapping: ''
      }
    ]
  }
}

const resetExtractionResultState = () => {
  closeCustomTableMenu()
  resetEditHistory()
  customTextNum.value = 0
  customTableNum.value = 0
  customContentEdit.value = []
  customContentTable.value = []
  customPageShow.value = []
  editableTabs.value.customDetails = {}
}

const resolveTemplateOption = (template?: TemplateList | string, groupTemplateId?: string): TemplateList | undefined => {
  if (template && typeof template === 'object') return template
  return findTemplateByGroupId(groupTemplateId)
}

const openFile = async (val: FileData, template?: TemplateList | string, templateDetail?: TransformedResult, configResult?: boolean, refreshSidebar = true) => {
  localBannerKey.value += 1
  isDirty.value = false
  if (!templateGroupOptions.value.length) {
    await fetchTemplateGroupOptions()
  }
  if (val) {
    loading.value = true
    noResult.value = false
    outputType.value = 'txt'
    resetExtractionResultState()
    try {
      fileStatus.value = val.status
      reviewStatus.value = val.reviewStatus
      await loadDocument(val)
      // 按字节真实类型判定：后端可能把图片转存为 PDF，文件名后缀不可靠
      fileType.value = getFileExtension(val.fileName)
      const { file: realFile, objectUrl, kind } = await urlToFile(val.fileDownUrl, val.fileName)
      if (kind === 'pdf') {
        fileType.value = 'pdf'
        picSrc.value = ''
        UI.value.loadDocument(realFile)
      } else if (kind === 'image') {
        fileType.value = getFileExtension(val.fileName) || 'png'
        file.value = realFile
        picSrc.value = objectUrl
      } else {
        ElMessage.error(t('bulkExtract.notSupport'))
      }
      isConfigResult.value = configResult as boolean
      fileName.value = val.fileName
      fileId.value = val.fileId
      groupTemplateId.value = val.groupTemplateId
      currentTemplateId.value = val.groupTemplateId
      const resolvedTemplate = resolveTemplateOption(template, val.groupTemplateId)
      if (refreshSidebar) {
        await fetchSidebarFiles()
      }
      file.value = '1' as unknown as File // 占位，表示已加载文件
      try {
        file.value = realFile
      } catch (error) {
        console.error('Failed to load preview file:', error)
      }
      const templateDetailId = resolvedTemplate?.templateId || val.groupTemplateId
      if (templateDetailId) {
        const { data: { data } } = await get(`/api/idp/get-template-by-id?templateId=${templateDetailId}`)
        templateField.value = reverseTransformTemplateField(data)
        tempName.value = data.name
        upsertTemplateOption(normalizeTemplate({
          ...resolvedTemplate,
          groupTemplateId: val.groupTemplateId,
          templateId: data.id || templateDetailId,
          templateName: data.name
        }))
      }
      collapse.value = true
    } catch (error) {
      console.error('Failed to open extraction file:', error)
      noResult.value = true
      ElMessage.error(t('singleExtract.error'))
    } finally {
      loading.value = false
    }
  } else {
    if (templateDetail) {
      tempName.value = templateDetail.name
      sourceTempName.value = templateDetail.name
      fileId.value = templateDetail?.fileId || ''
      fileStatus.value = undefined
      reviewStatus.value = undefined
      templateId.value = templateDetail?.id || ''
      templateField.value = reverseTransformTemplateField(templateDetail)
      const { data: { data } } = await get(`/api/idp/get-template-by-id?templateId=${templateDetail?.id}`)
      templateField.value = reverseTransformTemplateField(data)
      tempName.value = data.name
      // 保存原始模板快照，用于检测变动
      originalTemplateSnapshot.value = JSON.stringify(transformTemplateField(templateField.value, tempName.value))
      originalTempName.value = tempName.value
      // 优先使用 API 返回的 fileId，兼容 templateDetail 中的 fileId
      const resolvedFileId = data.fileId || templateDetail?.fileId
      fileId.value = resolvedFileId || ''
      if (resolvedFileId) {
        loading.value = true
        const { data: { data: fileData } } = await get(`/api/idp/get-file-by-id?fileId=${resolvedFileId}`)
        fileName.value = fileData.fileName
        fileType.value = getFileExtension(fileData.fileName)
        file.value = '1' as unknown as File // 占位，让左侧立即显示预览区而非上传区
        const { file: realFile, objectUrl, kind } = await urlToFile(fileData.fileDownUrl, fileData.fileName)
        if (kind === 'pdf') {
          fileType.value = 'pdf'
          UI.value.loadDocument(realFile)
          picSrc.value = ''
        } else if (kind === 'image') {
          fileType.value = getFileExtension(fileData.fileName) || 'png'
          file.value = realFile
          picSrc.value = objectUrl
        } else {
          ElMessage.error(t('bulkExtract.notSupport'))
        }
        loading.value = false
      }
    }
    collapse.value = false
    isAddTemp.value = true
    isEditTemp.value = true
  }
  // 重新初始化 popover 控制数组
  initPromptPopoverShow()
}

const loadDocument = async (val: FileData) => {
  if (!val.resultDownUrl) {
    resetExtractionResultState()
    noResult.value = true
    return
  }
  const data = await fetchResultJson(val.resultDownUrl)
  editableTabs.value.customDetails = typeof data === 'string' ? JSON.parse(data) : data || {}
  handleData(editableTabs.value.customDetails)
  resetEditHistory()
  noResult.value = Object.keys(editableTabs.value.customDetails || {}).length === 0
  customDownload.value = false
}

const fetchResultJson = async (url: string): Promise<any> => {
  try {
    const res = await fetch(url, {
      headers: {
        'Accept': 'application/json'
      }
    })
    if (!res.ok) throw new Error('请求失败')
    return await res.json()
  } catch (error) {
    throw error
  }
}

// 将 templateField 数据转换为目标格式
interface TransformedKeyValue {
  prompt: string | null
  mapping: string | null
}

interface TransformedResult {
  keys: Record<string, TransformedKeyValue>
  tableHeaders: Record<string, TransformedKeyValue>[] | Record<string, Record<string, TransformedKeyValue>>
  name: string
  id?: string
  fileId?: string
}

const transformTemplateField = (fields: Template, name: string): TransformedResult => {
  const keys: Record<string, TransformedKeyValue> = {}
  const tableHeaders: Record<string, Record<string, TransformedKeyValue>> = {}

  let tableIndex = 1
  fields.forEach(item => {
    if (item.fieldType === 'text') {
      // text 类型加入 keys
      keys[item.fieldName] = {
        prompt: item.prompt || null,
        mapping: item.mapping || null
      }
    } else if (item.fieldType === 'table' && item.children) {
      // table 类型，以 fieldName 为 key（兼容对象格式）
      const tableKey = item.fieldName || `Table_${tableIndex}`
      const tableObj: Record<string, TransformedKeyValue> = {}
      item.children.forEach(child => {
        tableObj[child.fieldName] = {
          prompt: child.prompt || null,
          mapping: child.mapping || null
        }
      })
      tableHeaders[tableKey] = tableObj
      tableIndex++
    }
  })

  return {
    keys,
    tableHeaders,
    name
  }
}

// 将转换后的数据反向还原为 templateField 格式
const reverseTransformTemplateField = (data: TransformedResult): Template => {
  const result: Template = []
  if (!data) return result

  // 还原 keys -> text 类型字段
  if (data.keys && Object.keys(data.keys).length) {
    Object.entries(data.keys).forEach(([fieldName, value]) => {
      result.push({
        prompt: value.prompt || '',
        fieldType: 'text',
        fieldName,
        mapping: value.mapping || ''
      })
    })
  }

  // 还原 tableHeaders -> table 类型字段（兼容数组和对象两种格式）
  if (data.tableHeaders) {
    if (Array.isArray(data.tableHeaders)) {
      // 旧格式：数组
      data.tableHeaders.forEach((tableObj, index) => {
        const children: TextField[] = Object.entries(tableObj).map(([fieldName, value]) => ({
          prompt: (value as TransformedKeyValue).prompt || '',
          fieldType: 'text',
          fieldName,
          mapping: (value as TransformedKeyValue).mapping || ''
        }))
        result.push({
          prompt: '',
          collapse: true,
          fieldType: 'table',
          fieldName: `Table ${index + 1}`,
          children
        })
      })
    } else {
      // 新格式：对象 { Table_1: { col: { prompt, mapping } }, ... }
      Object.entries(data.tableHeaders).forEach(([tableName, tableObj]) => {
        const children: TextField[] = Object.entries(tableObj as Record<string, TransformedKeyValue>).map(([fieldName, value]) => ({
          prompt: value.prompt || '',
          fieldType: 'text',
          fieldName,
          mapping: value.mapping || ''
        }))
        result.push({
          prompt: '',
          collapse: true,
          fieldType: 'table',
          fieldName: tableName,
          children
        })
      })
    }
  }

  return result
}

defineExpose({
  openFile,
  defaultCollapse
})

// 确认抽取结果
const configResult = async () => {
  const isCancelConfirm = isCurrentFileConfirmed.value
  const formData = new FormData()
  formData.append('fileId', fileId.value)
  if (!isCancelConfirm) {
    formData.append('newResult', JSON.stringify(editableTabs.value.customDetails))
  }
  const { data } = await post(isCancelConfirm ? '/api/idp/cancel-confirm-file-result' : '/api/idp/confirm-file-result', formData)
  if (data.code === 200) {
    reviewStatus.value = isCancelConfirm ? 0 : 1
    isConfigResult.value = !isCancelConfirm
    isDirty.value = false
    updateSidebarFile({ fileId: fileId.value, reviewStatus: reviewStatus.value })
    getTemplateFileList()
    ElMessage.success(isCancelConfirm ? t('extraction.revokeConfirmedSuccess') : t('extraction.confirmSuccess'))
  } else {
    ElMessage.error(data.message || (isCancelConfirm ? t('extraction.revokeConfirmedFailed') : t('extraction.confirmFailed')))
  }
}

const resetData = () => {
  loading.value = false
  dragover.value = false
  init.value = true
  editName.value = true
  tempName.value = ''
  sourceTempName.value = ''
  originalTemplateSnapshot.value = ''
  originalTempName.value = ''
  templateId.value = ''
  groupTemplateId.value = ''
  fileId.value = ''
  fileName.value = ''
  fileStatus.value = undefined
  reviewStatus.value = undefined
  isConfigResult.value = false
  isDirty.value = false
  isEditTemp.value = false
  isAddTemp.value = false
  dialogVisible.value = false
  dialogVisibleJsonPreview.value = false
  dialogVisibleLoading.value = false
  resetEditHistory()

  resultType.value = 'all'
  toType.value = 'json'
  format.value = 'json'
  outputType.value = 'txt'
  tableDownload.value = false
  customDownload.value = true

  fileType.value = 'pdf'
  picSrc.value = ''
  initFile.value = 'one'
  downloadName.value = 'invoice-example'
  isWidthBigger.value = undefined

  pdfCurrentPage.value = 1
  pdfPage.value = undefined
  scale.value = 100
  navShow.value = false
  scaleShow.value = false
  hand.value = 0
  saveShow.value = true
  templatePage.value = 1
  templatePageInput.value = 1

  customInit.value = false
  noResult.value = false
  firstExtract.value = true
  isFirst.value = true

  customTextIndex.value = null
  customTextPageIndex.value = null
  customTableIndex.value = null
  customTableListIndex.value = null
  customTableSunIndex.value = null
  closeCustomTableMenu()
  customPageTableKey.value = null
  customTextNum.value = 0
  customTableNum.value = 0

  customPageShow.value = []
  customContentEdit.value = []
  customContentTable.value = []

  editableTabs.value.tableList = []
  editableTabs.value.fieldsList = []
  file.value = undefined
  templateField.value = []
  collapse.value = false
  editableTabs.value.customDetails = {}

  promptPopoverShow.value = []
  childPromptPopoverShow.value = []
  initPromptPopoverShow()

  if (input.value) {
    input.value.value = ''
  }
}

const testTemplate = async () => {
  loading.value = true
  collapse.value = true
  if (resultContentRef.value) {
    resultContentRef.value.scrollTop = 0
  }
  const req = transformTemplateField(templateField.value, tempName.value)
  const formData = new FormData()
  formData.append('file', file.value as Blob)
  formData.append('extractTemplateDTO', JSON.stringify(req))
  // 多页PDF时传入模板页码
  if (fileType.value === 'pdf' && pdfPage.value > 1) {
    formData.append('pageNum', String(templatePage.value))
  }
  try {
    const { data } = await post('/api/idp/test-extract', formData)
    const res = await fetchResultJson(data.data)
    editableTabs.value.customDetails = typeof res === 'string' ? JSON.parse(res) : res || {}
    handleData(editableTabs.value.customDetails)
    resetEditHistory()
    customInit.value = false
    isFirst.value = false
    customDownload.value = false
  } catch {
    ElMessage.error(t('singleExtract.error'))
  } finally {
    loading.value = false
  }
}

// 重新抽取（与 List 中的 startExtraction 一致，调用 /api/idp/files-start）
const POLL_INTERVAL = 3000
let pollTimer: ReturnType<typeof setTimeout> | null = null

const startExtraction = async () => {
  if (!fileId.value) return
  loading.value = true
  fileStatus.value = 10

  try {
    const formData = new FormData()
    formData.append('idpFileIds', fileId.value)
    formData.append('type', 'EXTRACTION')

    const { data } = await post('/api/idp/files-start', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
    if (data.code === 200 && data.message === 'success') {
      pollFileStatus([fileId.value])
    }
  } catch {
    loading.value = false
    ElMessage.error(t('parsing.fail'))
  }
}

const pollFileStatus = async (fileIds: string[]) => {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
  if (!Array.isArray(fileIds) || fileIds.length === 0) return

  const queryString = fileIds.map(id => `fileIds=${id}`).join('&')

  try {
    const { data } = await get(`/api/idp/get-file-by-ids?${queryString}`)
    if (data.code === 200 && data.data) {
      const files = Array.isArray(data.data) ? data.data : [data.data]
      const latestCurrentFile = files.find((file: any) => file.fileId === fileId.value || file.id === fileId.value) || files[0]
      if (latestCurrentFile?.status !== undefined) {
        fileStatus.value = latestCurrentFile.status
        reviewStatus.value = latestCurrentFile.reviewStatus
      }
      const allCompleted = files.every((file: any) => file.status === 2 || file.status === 3 || file.status === 8 || file.status === 11 || file.status === 12)

      if (allCompleted) {
        loading.value = false
        const successFile = files.find((file: any) => file.status === 2 || file.status === 11)
        if (successFile) {
          fileStatus.value = successFile.status
          reviewStatus.value = successFile.reviewStatus
          // 重新加载抽取结果
          const resultData = await fetchResultJson(successFile.resultDownUrl)
          editableTabs.value.customDetails = typeof resultData === 'string' ? JSON.parse(resultData) : resultData || {}
          handleData(editableTabs.value.customDetails)
          resetEditHistory()
          customDownload.value = false
          ElMessage.success(t('splitting.success'))
        } else {
          const failedFile = files.find((file: any) => file.status === 3 || file.status === 8 || file.status === 12)
          if (failedFile?.status !== undefined) {
            fileStatus.value = failedFile.status
            reviewStatus.value = failedFile.reviewStatus
          }
          ElMessage.error(t('splitting.fail'))
        }
      } else {
        pollTimer = setTimeout(() => pollFileStatus(fileIds), POLL_INTERVAL)
      }
    }
  } catch {
    pollTimer = setTimeout(() => pollFileStatus(fileIds), POLL_INTERVAL)
  }
}

const toggleSelect = inject('toggleSelect', (_val: string) => {})
const getTemplateList = inject('getTemplateList', () => {})
const getTemplateFileList = inject('getTemplateFileList', () => {})

const updateSidebarFile = (fileInfo: Partial<FileData> & { fileId?: string }) => {
  if (!fileInfo.fileId) return
  const index = sidebarFiles.value.findIndex(item => item.fileId === fileInfo.fileId)
  if (index >= 0) {
    sidebarFiles.value.splice(index, 1, { ...sidebarFiles.value[index], ...fileInfo })
  }
}

// Sidebar state
const sidebarSearch = ref('')
const sidebarTab = ref<'all' | 'confirmed' | 'unconfirmed'>('all')
const sidebarFiles = ref<FileData[]>([])
const sidebarLoading = ref(false)
const currentTemplateId = ref<string>('')
const templateSwitchLoading = ref(false)
const templateGroupOptions = ref<TemplateList[]>([])
interface SidebarFilterPayload {
  statuses: string[]
  startTime: string
  endTime: string
  groupTemplateIds: string[]
  skipCurrentTemplate?: boolean
}

const sidebarFilters = ref<SidebarFilterPayload>({
  statuses: [],
  startTime: '',
  endTime: '',
  groupTemplateIds: [],
  skipCurrentTemplate: false
})

const findTemplateByGroupId = (groupTemplateId?: string): TemplateList | undefined => {
  if (!groupTemplateId) return undefined
  return templateGroupOptions.value.find(item => item.groupTemplateId === groupTemplateId)
}

const resolveGroupId = (groupTemplateIds: string[] = []): string => {
  const explicitTemplate = groupTemplateIds
    .map(id => findTemplateByGroupId(id))
    .find(item => item?.groupId)
  if (explicitTemplate?.groupId) return explicitTemplate.groupId

  const currentTemplate = findTemplateByGroupId(currentTemplateId.value || groupTemplateId.value)
  if (currentTemplate?.groupId) return currentTemplate.groupId

  return templateGroupOptions.value.find(item => item.groupId)?.groupId || ''
}

const openCurrentTemplateSettings = () => {
  const selectedGroupTemplateId = currentTemplateId.value || groupTemplateId.value
  const selectedTemplate = findTemplateByGroupId(selectedGroupTemplateId)
  router.push({
    path: '/document-extraction',
    query: {
      templateSettings: '1',
      groupTemplateId: selectedGroupTemplateId || undefined,
      templateId: selectedTemplate?.templateId || templateId.value || undefined,
      templateName: selectedTemplate?.templateName || tempName.value || undefined
    }
  })
}

async function fetchTemplateGroupOptions() {
  try {
    const groupResponse = await get('/api/idp/get-group-template').catch(() => ({ data: { data: [] } }))
    const groupPayload = groupResponse.data?.data
    const groups = Array.isArray(groupPayload) ? groupPayload : (groupPayload ? [groupPayload] : [])
    const groupSource = groups.flatMap((group: any) => [
      ...(group?.pinnedTemplates || []),
      ...(group?.customTemplates || []),
      ...(group?.defaultTemplates || [])
    ])
    let source = groupSource
    if (!source.length) {
      const [defaultResponse, customResponse] = await Promise.all([
        get('/api/idp/get-default-template').catch(() => ({ data: { data: [] } })),
        get('/api/idp/get-template-list?name=').catch(() => ({ data: { data: [] } }))
      ])
      source = [
        ...(customResponse.data?.data || []),
        ...(defaultResponse.data?.data || [])
      ]
    }
    const seen = new Set<string>()
    templateGroupOptions.value = source
      .map(normalizeTemplate)
      .filter(item => item.templateName !== '--')
      .filter(item => {
        const key = item.groupTemplateId || item.templateId || item.templateName
        if (!key || seen.has(key)) return false
        seen.add(key)
        return true
      })
  } catch (e) {
    console.error('Failed to fetch template groups:', e)
  }
}

const isSuccessfulFile = (doc: FileData): boolean => {
  return (doc.status === 2 || doc.status === 11) && Boolean(doc.resultDownUrl)
}

// Fetch sidebar file list from API, filtered by template
async function fetchSidebarFiles() {
  if (sidebarLoading.value) return
  sidebarLoading.value = true
  try {
    let query = `taskType=EXTRACTION&page=1&pageSize=100`
    if (sidebarFilters.value.statuses.length) {
      query += `&status=${sidebarFilters.value.statuses.join(',')}`
    }
    if (sidebarFilters.value.startTime) {
      query += `&startTime=${encodeURIComponent(sidebarFilters.value.startTime)}`
    }
    if (sidebarFilters.value.endTime) {
      query += `&endTime=${encodeURIComponent(sidebarFilters.value.endTime)}`
    }
    const filteredTemplateIds = sidebarFilters.value.groupTemplateIds.filter(Boolean)
    const groupIdQuery = resolveGroupId(filteredTemplateIds)
    if (groupIdQuery) {
      query += `&groupId=${encodeURIComponent(groupIdQuery)}`
    }
    const templateQuery = filteredTemplateIds.length
      ? filteredTemplateIds.join(',')
      : (sidebarFilters.value.skipCurrentTemplate ? '' : currentTemplateId.value)
    if (templateQuery) {
      query += `&groupTemplateId=${encodeURIComponent(templateQuery)}`
    }
    const { data } = await get(`/api/idp/getFileList?${query}`)
    sidebarFiles.value = data?.data?.records || []
  } catch (e) {
    console.error('Failed to fetch sidebar files:', e)
  } finally {
    sidebarLoading.value = false
  }
}

// Compute confirmed count for sidebar badge
const confirmedCount = computed(() => {
  return sidebarFiles.value.filter(doc => isSuccessfulExtractionStatus(doc.status) && doc.reviewStatus === 1).length
})

// Compute unconfirmed count for sidebar badge
const unconfirmedCount = computed(() => {
  return sidebarFiles.value.filter(doc => isSuccessfulExtractionStatus(doc.status) && doc.reviewStatus === 0).length
})

// Sidebar computed: filter by tab and search
const filteredSidebarFiles = computed(() => {
  let list = sidebarFiles.value
  if (sidebarTab.value === 'confirmed') {
    list = list.filter(doc => isSuccessfulExtractionStatus(doc.status) && doc.reviewStatus === 1)
  } else if (sidebarTab.value === 'unconfirmed') {
    list = list.filter(doc => isSuccessfulExtractionStatus(doc.status) && doc.reviewStatus === 0)
  }
  if (sidebarSearch.value) {
    const q = sidebarSearch.value.toLowerCase()
    list = list.filter(doc =>
      doc.fileName.toLowerCase().includes(q)
    )
  }
  return list
})

const handleSidebarSelectDoc = (docId: string) => {
  const doc = sidebarFiles.value.find(d => d.fileId === docId)
  if (!doc) return
  openFile(doc, findTemplateByGroupId(doc.groupTemplateId))
}

const handleTemplateChange = async (nextGroupTemplateId: string) => {
  if (!nextGroupTemplateId || templateSwitchLoading.value) return
  templateSwitchLoading.value = true
  try {
    currentTemplateId.value = nextGroupTemplateId
    groupTemplateId.value = nextGroupTemplateId
    sidebarFilters.value.groupTemplateIds = []
    sidebarFilters.value.skipCurrentTemplate = false
    sidebarSearch.value = ''
    sidebarTab.value = 'all'
    const selectedTemplate = findTemplateByGroupId(nextGroupTemplateId)
    tempName.value = selectedTemplate?.templateName || tempName.value
    await fetchSidebarFiles()
    const firstSuccessFile = sidebarFiles.value.find(isSuccessfulFile)
    if (firstSuccessFile) {
      await openFile(firstSuccessFile, selectedTemplate, undefined, undefined, false)
    } else {
      fileId.value = ''
      fileName.value = ''
      fileStatus.value = undefined
      reviewStatus.value = undefined
      file.value = undefined
      editableTabs.value.customDetails = {}
      customContentEdit.value = []
      customContentTable.value = []
      customPageShow.value = []
      customTextNum.value = 0
      customTableNum.value = 0
      resetEditHistory()
      noResult.value = true
      ElMessage.warning(t('extraction.noData'))
    }
  } finally {
    templateSwitchLoading.value = false
  }
}

// Sidebar event handlers
const handleFilter = () => {
  // Filter button in ExtractionSidebar clicked
}

const handleSidebarApplyFilter = async (payload: SidebarFilterPayload) => {
  sidebarFilters.value = {
    statuses: payload.statuses || [],
    startTime: payload.startTime || '',
    endTime: payload.endTime || '',
    groupTemplateIds: payload.groupTemplateIds || [],
    skipCurrentTemplate: payload.skipCurrentTemplate ?? false
  }
  await fetchSidebarFiles()
}

const handleSidebarSearch = (query: string) => {
  sidebarSearch.value = query
}

// Sidebar helper methods
const reviewStatusClass = (status?: number, fileStatus?: number): string => {
  if (!isSuccessfulExtractionStatus(fileStatus)) return 'bg-[#F3F3F4] text-[#94969D]'
  const map: Record<number, string> = { 1: 'bg-[#ECF9F3] text-[#67D1A0]', 0: 'bg-[#FEF3E6] text-[#F5A13A]' }
  return map[Number(status)] || 'bg-[#F3F3F4] text-[#94969D]'
}

const reviewStatusText = (status?: number, fileStatus?: number): string => {
  if (!isSuccessfulExtractionStatus(fileStatus)) return '--'
  const map: Record<number, string> = { 1: t('extraction.confirmed'), 0: t('extraction.unconfirmed') }
  return map[Number(status)] ?? '--'
}

const statusClass = (status: number): string => {
  const map: Record<number, string> = {
    0: 'is-neutral',
    1: 'is-warning',
    2: 'is-success',
    3: 'is-error',
    5: 'is-brand',
    6: 'is-neutral',
    7: 'is-warning',
    8: 'is-error',
    9: 'is-brand',
    10: 'is-warning',
    11: 'is-success',
    12: 'is-error',
  }
  return map[status] || 'is-neutral'
}

const statusText = (status: number): string => {
  const map: Record<number, string> = {
    0: t('extraction.pendingExtraction'),
    1: t('extraction.extracting'),
    2: t('extraction.extractionSuccess'),
    3: t('extraction.extractionFailed'),
    5: t('extraction.paused'),
    6: t('extraction.pendingClassification'),
    7: t('extraction.classifying'),
    8: t('extraction.classificationFailed'),
    9: t('extraction.pendingExtraction'),
    10: t('extraction.extracting'),
    11: t('extraction.extractionSuccess'),
    12: t('extraction.extractionFailed'),
  }
  return map[status] || t('extraction.unknown')
}

const addTemplate = async () => {
  if (!tempName.value) {
    ElMessage.warning(t('extraction.enterTemplateName'))
    return
  }
  if (tempName.value.length > 50) {
    ElMessage.warning(t('template.nameMaxLength'))
    return
  }
  const req = transformTemplateField(templateField.value, tempName.value)
  // 多页PDF时附加 pageNum
  if (fileType.value === 'pdf' && pdfPage.value > 1) {
    (req as any).page = templatePage.value
  }
  const { data } = await post('/api/idp/add-template', req)
  if (data.code === 200 && data.message === 'success') {
    ElMessage.success(t('extraction.success'))
    changeActive('list')
    if (file.value) {
      const formData = new FormData()
      formData.append('file', file.value as Blob)
      formData.append('templateId', data.data)
      await post('/api/idp/add-template-file', formData)
    }
    resetData()
    getTemplateList()
    toggleSelect(data.data)
  } else if (data.code === 8012 && data.message === 'Template name already exists') {
    ElMessage.warning(t('template.exists'))
  } else {
    ElMessage.error(t('extraction.fail'))
  }
}

const updateTemplate = async () => {
  if (!tempName.value) {
    ElMessage.warning(t('extraction.enterTemplateName'))
    return
  }
  if (tempName.value.length > 50) {
    ElMessage.warning(t('template.nameMaxLength'))
    return
  }
  const req = transformTemplateField(templateField.value, tempName.value)
  req.id = templateId.value
  // 多页PDF时附加 pageNum
  if (fileType.value === 'pdf' && pdfPage.value > 1) {
    (req as any).page = templatePage.value
  }
  const { data } = await post('/api/idp/update-template', req)
  if (data.code === 200 && data.message === 'success') {
    if (file.value) {
      const formData = new FormData()
      formData.append('file', file.value as Blob)
      formData.append('templateId', templateId.value)
      await post('/api/idp/add-template-file', formData)
    }
    changeActive('list')
    resetData()
    getTemplateList()
    ElMessage.success(t('extraction.success'))
  } else {
    ElMessage.error(t('extraction.fail'))
  }
}

const openDialog = (val: string) => {
  if (shouldSkipProcessing.value) return
  if (customDownload.value) return
  if (val === 'txt') {
    const blogContent = JSON.stringify(editableTabs.value.customDetails, null, 2)
    const blob = new Blob([blogContent], { type: 'application/json' })
    const blobUrl = URL.createObjectURL(blob)
    downloadClick(blobUrl, downloadName.value + '.' + format.value)
  } else {
    dialogVisible.value = true
    if (val === 'table' || resultType.value === 'table') {
      format.value = 'xlsx'
      tableDownload.value = true
    } else {
      customTableListIndex.value = null
      tableDownload.value = false
      resultType.value === 'table' ? format.value = 'xlsx' : format.value = 'json'
    }
  }
}

const onImageLoad = () => {
  isWidthBigger.value = img.value.naturalWidth > img.value.naturalHeight
}

// 选择其他文件
const handleChange =  async (e: Event) => {
  const target = e.target as HTMLInputElement | null
  const files = target?.files
  if (!files) return
  init.value = true
  pdfCurrentPage.value = 1
  saveShow.value = true
  navShow.value = false
  customInit.value = true
  initFile.value = ''
  customDownload.value = true
  firstExtract.value = true
  customTextIndex.value = null
  customTableIndex.value = null
  customTableListIndex.value = null
  customTableSunIndex.value = null
  customTextNum.value = 0
  customTableNum.value = 0
  customContentEdit.value = []
  customContentTable.value = []
  customPageShow.value = []
  editableTabs.value.customDetails = {}
  resetEditHistory()
  fileStatus.value = undefined
  reviewStatus.value = undefined
  const postFile = files[0]
  dialogVisibleLoading.value = false
  const nameArray = postFile.name.split('.')
  downloadName.value = nameArray[0]
  fileType.value = nameArray[nameArray.length - 1].toLowerCase()
  if (fileType.value === 'pdf') {
    UI.value.loadDocument(files[0])
    picSrc.value = ''
  } else if (['jpg', 'png', 'jpeg'].includes(fileType.value)) {
    let reader = new FileReader()
    reader.readAsDataURL(postFile)
    reader.onload = () => {
      picSrc.value = reader.result as string
    }
  } else {
    ElMessage.error(t('bulkExtract.notSupport'))
    return
  }
  file.value = postFile
}

const handleData = (data: any) => {
  customTextNum.value = 0
  customTableNum.value = 0
  customContentEdit.value = []
  customContentTable.value = []
  customPageShow.value = []
  const reorderedDetails: Record<string, any> = {}
  Object.entries(data).forEach(([pageKey, pageValue]) => {
    if (pageValue && typeof pageValue === 'object' && !Array.isArray(pageValue)) {
      // @ts-ignore
      const { tables, ...rest } = pageValue
      reorderedDetails[pageKey] = tables !== undefined
        ? { ...rest, tables } // tables放最后
        : { ...rest }
    } else {
      reorderedDetails[pageKey] = pageValue
    }
  })
  if (reorderedDetails) {
    editableTabs.value.customDetails = reorderedDetails
    let i = 0
    let j = 0
    Object.keys(reorderedDetails).forEach((key: string, pageIndex: number) => {
      const pageData = reorderedDetails[key]
      customContentEdit.value.push([])
      customPageShow.value.push(true)
      Object.keys(pageData).forEach((item: string) => {
        if (item !== 'tables') {
          customContentEdit.value[pageIndex].push({
            hover: true,
            status: false,
            copy: t('singleExtract.copy')
          })
          i++
        } else if (Array.isArray(pageData[item])) {
          if (pageData.tables) {
            pageData.tables?.forEach((items: []) => {
              if (items.length) {
                j++
                customContentTable.value.push(false)
              }
            })
          }
        }
      })
    })
    customTextNum.value = i
    customTableNum.value = j
  } else {
    ElMessage({
      message: t('extraction.fail'),
      type: 'error',
      duration: 2000,
      customClass: 'upload'
    })
  }
}

const format = ref<string>('json')

// 导出文件
const exportFile = async (id: string[]) => {
  try {
    loading.value = true
    const exportMap = {
      json: 'JSON',
      xlsx: 'EXCEL',
      csv: 'CSV'
    }

    const res = await request({
      method: 'post',
      url: '/api/idp/extract-export',
      data: {
        fileIds: id,
        exportFormat: exportMap[format.value as keyof typeof exportMap]
      },
      responseType: 'blob'
    })

    const disposition = (res.headers?.['content-disposition'] as string | undefined) ?? ''
    const filenameMatch = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
    const filenameRaw = filenameMatch?.[1] ?? filenameMatch?.[2]

    // 格式对应的 MIME 类型和默认扩展名
    const formatMimeMap: Record<string, { mime: string; ext: string }> = {
      json: { mime: 'application/json', ext: '.json' },
      excel: { mime: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', ext: '.xlsx' },
      csv: { mime: 'text/csv', ext: '.csv' },
    }

    // 单个文件：按所选格式下载，不下载 zip
    const formatInfo = formatMimeMap[format.value] ?? { mime: 'application/octet-stream', ext: '' }
    const fallbackName = `export_compdf_ai_extract${formatInfo.ext}`
    const filename = filenameRaw ? decodeURIComponent(filenameRaw) : fallbackName

    const contentType = (res.headers?.['content-type'] as string | undefined) ?? formatInfo.mime
    let blob: Blob
    if (format.value === 'json') {
      // JSON 格式：保留原格式（格式化输出）
      const rawBlob = res.data instanceof Blob ? res.data : new Blob([res.data])
      const text = await rawBlob.text()
      try {
        const json = JSON.parse(text)
        const formatted = JSON.stringify(json, null, 2)
        blob = new Blob([formatted], { type: 'application/json' })
      } catch {
        blob = rawBlob
      }
    } else {
      blob = res.data instanceof Blob ? res.data : new Blob([res.data], { type: contentType })
    }
    saveAs(blob, filename)

    loading.value = false
    dialogVisible.value = false
    ElMessage.success(t('extraction.success'))
  } catch {
    loading.value = false
    dialogVisible.value = false
    ElMessage.error(t('extraction.fail'))
  }
}

// 生成对应 Blob 对象
const downloadFile = () => {
  dialogVisible.value = false
  let blogContent = null
  const data = editableTabs.value.customDetails
  if (resultType.value === 'text') {
    const obj = JSON.parse(JSON.stringify(data))
    Object.keys(obj).forEach((item: any) => {
      if (obj[item]?.tables?.length) {
        delete obj[item].tables
      }
    })
    blogContent = obj
  } else if (resultType.value === 'table') {
    const obj: Record<string, any> = {}
    Object.keys(data).forEach((key: string) => {
      if (data[key]?.tables?.length) {
        obj[key] = { tables: data[key].tables }
      }
    })
    blogContent = obj
  } else {
    blogContent = data
  }
  const blobContentMap = {
    json: 'application/json',
    text: 'text/plain;charset=utf-8;'
  }
  if (['txt', 'json'].includes(format.value)) {
    blogContent = JSON.stringify(blogContent, null, 2)
  }
  if (['json', 'txt'].includes(format.value)) {
    const blob = new Blob([blogContent], { type: blobContentMap[format.value as keyof typeof blobContentMap] })
    const blobUrl = URL.createObjectURL(blob)
    downloadClick(blobUrl, downloadName.value + '.' + format.value)
  } else if (format.value === 'csv') {
    tableDownload.value ? downloadSingleCsv() : downloadCSV()
  } else if (format.value === 'xlsx') {
    tableDownload.value ? downloadSingleXlsx() : downloadXlsx()
  }
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
    // 定义文件类型映射接口
    interface FileTypeMap {
      [key: string]: string
    }

    // 使用常量定义文件类型映射
    const FILE_TYPE_MAP: FileTypeMap = {
      json: '.json',
      xlsx: '.xlsx',
      csv: '.csv',
      txt: '.txt'
    } as const

    // 使用映射
    if (FILE_TYPE_MAP[toType.value]) {
      const extension = FILE_TYPE_MAP[toType.value]
      a.download = filename.replace(extension, `_compdf_ai_signle_extract${extension}`)
    }
  }
  (document.body || document.documentElement).appendChild(a)
  a.click()
  a.remove()
  tableDownload.value = false
}
</script>

<style lang="scss" scoped>
/* ── 3-Panel Layout ── */
.extraction-result {
  height: 100%;
  display: grid;
  grid-template-columns: 230px minmax(0, 1fr) minmax(0, 1fr);
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 8px;
  padding: 16px;
  background: #F5F7FF;
  font-family: 'Encode Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #0c131f;
  box-sizing: border-box;
  min-height: 0;
}

.extraction-result__sidebar {
  width: 230px;
  min-width: 230px;
  flex: 0 0 230px;
  grid-column: 1;
  grid-row: 1 / span 2;
}

/* Figma: preview panel and result panel share the remaining width equally. */
.extraction-result__preview {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  grid-column: 2;
  grid-row: 1;
}

.extraction-result__file-bar {
  height: 40px;
  padding: 4px 12px;
  border-bottom: 1px solid #E7E8EC;
  box-sizing: border-box;
  flex: 0 0 40px;
}

.extraction-result__preview-bar {
  height: 28px;
  padding: 4px 0 4px 12px;
  border-bottom: 1px solid #E7E8EC;
  box-sizing: border-box;
  flex: 0 0 28px;

  span {
    color: rgba(12, 19, 31, 0.4);
    font-size: 12px;
    line-height: 20px;
  }
}

.extraction-result__viewer {
  background: #F3F3F4;
}

.extraction-result__form {
  flex: 1 1 0;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  padding: 12px 16px 16px 16px;
  box-sizing: border-box;
  grid-column: 3;
  grid-row: 1;
}

.extraction-result__local-banner {
  grid-column: 2 / span 2;
  grid-row: 2;
}

.extraction-result__template-header {
  padding: 0 0 8px;
}

.extraction-result__template-select {
  flex: 1 1 0;
  min-width: 0;
  height: 32px;

  :deep(.el-select__wrapper) {
    min-height: 32px;
    height: 32px;
    padding: 5px 8px;
    border-radius: 3px;
    box-shadow: 0 0 0 1px #DCDDE1 inset;
    box-sizing: border-box;
    background: #fff;
  }

  :deep(.el-select__selected-item) {
    color: #0C131F;
    font-size: 14px;
    line-height: 22px;

    span {
      color: #0C131F;
    }
  }

  :deep(.el-select__selected-item:not(.is-transparent)) {
    color: #0C131F;
  }

  :deep(.el-select__placeholder) {
    color: #0C131F;
    font-size: 14px;
    line-height: 22px;
  }
}

.extraction-result__template-detail-btn {
  width: 159px;
  height: 32px;
  padding: 5px 16px;
  border-radius: 3px;
  color: #fff;
  background: #396FFA;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
}

.extraction-result__divider {
  width: 100%;
  height: 1px;
  margin: 0;
  background: #E7E8E8;
}

.extraction-result__result-toolbar {
  height: 32px;
  padding: 0;
  margin-top: 16px;
  margin-bottom: 12px;
}

.extraction-result__result-tabs {
  height: 32px;
  padding: 4px;
  border: 1px solid #D7E2FE;
  border-radius: 3px;
  background: #F5F7FF;
  box-sizing: border-box;
}

.extraction-result__result-tab {
  height: 24px;
  padding: 2px 8px;
  border-radius: 3px;
  color: #0C131F;
  background: transparent;
  font-size: 12px;
  line-height: 20px;

  &.is-active {
    color: #396FFA;
    background: #fff;
  }
}

.extraction-result__export-btn {
  height: 24px;
  padding: 2px 8px;
  border: 1px solid #396FFA;
  border-radius: 3px;
  color: #396FFA;
  background: #fff;
  font-size: 12px;
  line-height: 20px;
}

.extraction-result__result-body {
  flex: 1 1 auto;
  min-height: 0;
}

.extraction-result__content-scroll {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
  height: auto;
  padding: 0;
  box-sizing: border-box;
}

.extraction-result__json-scroll {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
  height: auto;
  overflow: auto;
  border-radius: 3px;
  background: #030d26;
  box-sizing: border-box;

  :deep(.jv-container) {
    min-height: 100%;
    padding: 8px;
    box-sizing: border-box;
  }
}

.extraction-result__failure-state {
  position: relative;
  width: 100%;
  flex: 1 1 auto;
  height: auto;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
  box-sizing: border-box;
}

.extraction-result__bottom-bar {
  flex: 0 0 44px;
  box-sizing: border-box;
  position: relative;
  z-index: 5;
}

.extraction-result__failure-content {
  position: absolute;
  top: calc(50% - 75px);
  left: 50%;
  display: flex;
  width: 188px;
  height: 94px;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  transform: translate(-50%, -50%);
}

.extraction-result__failure-icon {
  display: flex;
  width: 64px;
  height: 64px;
  align-items: center;
  justify-content: center;
  border: 1px solid #EEB3B3;
  border-radius: 8px;
  color: #D44040;
  background: #FBECEC;
  box-sizing: border-box;

  svg {
    width: 32px;
    height: 32px;
  }
}

.extraction-result__failure-text {
  width: 188px;
  height: 22px;
  color: #0C131F;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  text-align: center;
  white-space: nowrap;
}

.extraction-result__failure-retry {
  position: absolute;
  top: calc(50% + 43px);
  left: 50%;
  display: flex;
  width: 90px;
  height: 32px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 5px 16px;
  border: 0;
  border-radius: 3px;
  color: #FFFFFF;
  background: #396FFA;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  cursor: pointer;
  transform: translateX(-50%);
  box-sizing: border-box;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 16px;
  }

  &:hover {
    background: #244FF0;
  }
}

.extraction-result__page-row {
  height: 22px;
  margin: 0 0 12px;
  padding: 0 8px;
  box-sizing: border-box;

  p {
    margin: 0;
    color: rgba(12, 19, 31, 0.6);
    font-size: 14px;
    line-height: 22px;
    font-weight: 400;
  }
}

.extraction-result__field-card {
  width: 100%;
  min-height: 56px;
  padding: 0;
  margin-bottom: 12px;
  background: transparent;

  > div:first-child {
    min-height: 20px;
    color: rgba(12, 19, 31, 0.4);
    font-size: 12px;
    line-height: 20px;
    margin-bottom: 4px;
  }

  .editContent {
    min-height: 32px;
    padding: 4px 64px 4px 8px;
    margin-top: 0 !important;
    border-radius: 3px;
    box-sizing: border-box;
    color: #0C131F;
    background: #F5F7FF;
    font-size: 12px;
    line-height: 20px;
  }

  .field-actions {
    top: 40px !important;
    right: 8px !important;
    gap: 4px;
    transform: translateY(-50%);
  }

  .copy,
  .delete-action {
    width: 24px;
    height: 24px;
    padding: 3px !important;
    align-items: center;
    justify-content: center;
    background: #fff;
    box-sizing: border-box;
  }
}

.extraction-result__add-field-wrap {
  width: 100%;
  padding: 0;
  margin: 12px 0;
  background: transparent;
  border-radius: 3px;
}

.extraction-result__add-field-btn {
  height: 32px;
  border-radius: 3px;
  color: #396FFA;
  background: #fff;
  font-size: 14px;
  line-height: 22px;
}

.extraction-result__table-cell {
  position: relative;
  min-width: 144px;
  height: 48px;
  padding: 12px 44px 12px 16px;
  border: 1px solid #E7E8E8;
  box-sizing: border-box;
  color: #0C131F;
  background: #F3F3F4;
  font-size: 14px;
  line-height: 22px;
  vertical-align: middle;
}

.extraction-result__table-cell-text {
  display: block;
  min-width: 0;
  min-height: 22px;
  outline: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.extraction-result__table-cell-more {
  position: absolute;
  top: 50%;
  right: 16px;
  z-index: 3;
  display: none;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 5px;
  border: 1px solid #DCDDE1;
  border-radius: 3px;
  color: #0C131F;
  background: #FFFFFF;
  box-sizing: border-box;
  cursor: pointer;
  transform: translateY(-50%);

  svg {
    width: 14px;
    height: 14px;
  }

  &:hover {
    border-color: #396FFA;
    color: #396FFA;
  }
}

.extraction-result__table-cell:hover .extraction-result__table-cell-more,
.extraction-result__table-cell:focus-within .extraction-result__table-cell-more {
  display: inline-flex;
}

.extraction-result__table-menu {
  position: fixed;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 204px;
  padding: 6px;
  border: 1px solid #DCDDE1;
  border-radius: 6px;
  background: #FFFFFF;
  box-shadow: 0 8px 20px rgba(12, 19, 31, 0.08);
  box-sizing: border-box;

  button {
    width: 192px;
    height: 28px;
    padding: 3px 8px;
    border: 0;
    border-radius: 3px;
    color: #0C131F;
    background: #FFFFFF;
    font-size: 14px;
    line-height: 22px;
    text-align: left;
    cursor: pointer;
    box-sizing: border-box;

    &:hover,
    &.is-active {
      color: #396FFA;
      background: #F5F7FF;
    }
  }
}

.extraction-result__file-item-name {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;

  svg {
    width: 16px;
    height: 16px;
    color: #396ffa;
    flex: 0 0 auto;
  }

  span {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.extraction-result__file-item-desc,
.extraction-result__file-item-tags {
  margin-left: 20px;
}

.extraction-result__file-item-desc {
  width: 122px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  line-height: 20px;
}

.extraction-result__file-item-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-tag {
  display: inline-flex;
  align-items: center;
  padding: 0 16px;
  border-radius: 3px;
  font-size: 12px;
  line-height: 20px;

  &.is-success {
    color: #67d1a0;
    background: #ecf9f3;
  }

  &.is-brand {
    color: #396ffa;
    background: #f5f7ff;
  }

  &.is-error {
    color: #d44040;
    background: #fbecec;
  }

  &.is-warning {
    color: #f5a13a;
    background: #fef3e6;
  }

  &.is-neutral {
    color: #0c131f;
    background: #f3f3f4;
  }
}

.font {
  font-family: Helvetica;
  font-weight: 600;
}
:deep(.el-select) {
  &.tempFieldSelect {
    .el-select__wrapper {
      min-height: 32px;
    }
  }
}
:deep(.el-input) {
  &.tempFieldInput {
    .el-input__inner {
      min-width: unset;
      min-height: 30px;
    }
  }
}
.tempName {
  outline: none;
  font-size: 16px;
  line-height: 24px;
  color: #0C131F;
}
:deep() {
  svg.arrowBlack {
    path {
      fill: #888C94;
    }
  }
}
:deep() {
  svg.download {
    path {
      stroke: #52555F;
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

:deep(.hand) {
  & path:nth-child(1) {
    display: none;
  }
  & path:nth-child(2) {
    display: none;
  }
  & path:nth-child(3) {
    fill: white;
  }
  &:hover {
    & path:nth-child(1) {
      display: unset;
    }
    & path:nth-child(2) {
      display: unset;
    }
  }
  &.active {
    rect {
      display: unset;
    }
  }
}

:deep(.zoom) {
  rect {
    display: none;
  }
  &:hover {
    rect {
      display: unset;
    }
  }
}

/* 隐藏Webkit浏览器中的上下箭头 */
input[type="number"]::-webkit-outer-spin-button,
input[type="number"]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 隐藏Firefox浏览器中的上下箭头 */
input[type="number"] {
  appearance: textfield;
  -moz-appearance: textfield;
}
.transitions {
  transform: rotateZ(90deg);
}
.transforms {
  transform: rotateZ(-90deg);
}
.translate {
  background-color: #FFF4E5;
  transform: translate(-50%, 0);
  border: 1px solid #FFC0614D;
}
.loader {
  width: 24px;
  --duration: 8s;
  overflow: visible;
  --track: #ededed;
  --active: #a6abad;
  transform-origin: center;
  transform: rotate(-90deg);
  animation: spin 2s linear infinite;
}

.disabled {
  opacity: .4;
  cursor: not-allowed;
  background: linear-gradient(0deg, #396FFA, #396FFA), radial-gradient(65.28% 65.28% at 50% 100%, rgba(113, 153, 255, 0.8) 0%, rgba(113, 153, 255, 0) 100%);
  &::after {
    display: none;
  }
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
  line-height: 1.5;
  transition: color 0.2s ease-in-out;
}

.inner svg.icon {
  width: 18px;
  height: 18px;
  transition: fill 0.1s linear;
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
:deep(.el-form) {
  .el-form-item {
    margin-bottom: 0;
    padding-bottom: 16px;
  }
  .el-select__caret::before {
    font-weight: bold;
  }
  .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 1px #396FFA inset;
  }
  .el-select__wrapper {
    min-height: 40px;
    @media screen and (max-width: 929.9px) {
      min-height: 40px;
    }
    &.is-focused {
      box-shadow: 0 0 0 1px #396FFA inset;
    }
  }
  .el-input__inner {
    height: 38px;
    border-color: #E1E3E8;
    @media screen and (max-width: 929.9px) {
      height: 32px;
    }
    &:focus {
      border-color: #396FFA;
    }
  }
  .el-form-item.is-error {
    .el-input__inner{
      border-color: #F56C6C;
    }
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
.copyTip:hover {
  .absolute {
    display: inline-block;
  }
}
.free {
  background: #596177;
  box-shadow: 0px 4px 16px 0px #00299233;
  animation: translateLeftRow 1s ease-in-out infinite;
  &::after {
    content: '';
    left: 45%;
    bottom: -10px;
    position: absolute;
    border-top: 10px solid #596177;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
  }
}
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
.contactBtn {
  position: relative;
  background: linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
}
.image-container img.width-bigger {
  width: 100%;
}

.image-container img.height-bigger {
  height: 100%;
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
:deep(.el-select.extractSelect) {
  &.en .el-select__wrapper {
    padding-left: 160px;
  }
  &.cn .el-select__wrapper {
    padding-left: 120px;
  }
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
.shadows {
  box-shadow: -4px 4px 18px 0px #00359A1F;
}
.shadow::before {
  content: "";
  position: absolute;
  top: 54px;
  left: -6px;
  border-top: 6px solid transparent;
  border-right: 6px solid #ddd;
  border-bottom: 6px solid transparent;
}
:deep(.el-table) {
  z-index: 0;
  padding: 0;
  border-radius: 8px;
  .el-table__header .el-table__cell {
    padding: 10px 0;
    border-color: #E1E3E8;
    background-color: #F3F6FF;
    &:nth-child(1) {
      border-top-left-radius: 8px;
    }
    &:last-child {
      border-top-right-radius: 8px;
    }
    .cell {
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
      color: #94969D;
    }
  }
  .el-table__body .el-table__row {
    &:last-child {
      .el-table__cell {
        &:nth-child(1) {
          border-bottom-left-radius: 8px;
        }
        &:last-child {
          border-bottom-right-radius: 8px;
        }
      }
    }
    .el-table__cell {
      border-color: #E1E3E8;
      .cell {
        color: #232748;
      }
    } 
  }
}
.handle {
  .field-actions {
    .copy,
    .delete-action {
      position: relative;

      .tip {
        box-shadow: 0px 4px 4px 0px #00000033;
      }

      &:hover .tip {
        display: inline-block;
      }
    }
  }

  &:hover {
    .editContent {
      color: #396FFA;
      background-color: #396FFA33;
    }
    .edit, .field-actions {
      display: inline-flex;
    }
  }
}
.edit:hover .tip {
  display: inline-block;
}
.samples {
  backdrop-filter: blur(4px);
  box-shadow: 0px 0px 4px 0px #FFFFFF40;
}
@keyframes loading {
  0% {
    -webkit-transform: rotate(0);
    transform: rotate(0);
  }
  100% {
    -webkit-transform: rotate(360deg);
    transform: rotate(360deg);
  }
}
.loading {
  -webkit-animation: loading .6s linear infinite;
  animation: loading .6s linear infinite;
  min-height: 24px;
  min-width: 24px;
  vertical-align: middle;
  display: inline-block;
  background-size: 100%;
}
.privacy-box {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  border-radius: 2px;
  display: inline-block;
  border: 1px solid #CCCCCC;
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
.twitter:hover {
  & img:nth-child(1) {
    display: none;
  }
  & img:nth-child(2) {
    display: inline-block;
  }
}
:deep(.el-textarea) {
  .el-textarea__inner {
    border-radius: 4px;
    min-height: 92px !important;
    &.is-focus {
      box-shadow: 0 0 0 1px #396FFA inset;
    }
  }
}
:deep(.el-radio-group) {
  flex-direction: row;
  .el-radio {
    height: 28px;
    width: calc(50% - 8px);
    &:nth-child(n+3) {
      margin-top: 4px;
    }
    margin-right: 0;
    color: #232748;
    .el-radio__label {
      padding: 4px 0;
      font-size: 14px;
      line-height: 20px;
      &:hover {
        color: #396FFA;
      }
    }
    .el-radio__inner {
      width: 14px;
      height: 14px;
      background: transparent;
      border: 1.5px solid #666666;
    }
    &.is-checked {
      .el-radio__label {
        color: #396FFA;
      }
    }
    .el-radio__input {
      margin-right: 12px;
      &.is-checked {
        .el-radio__inner {
          border-color: #1460F3;
          background-color: #1460F3;
          &::after {
            width: 6px;
            height: 6px;
            background-color: white !important;
          }
        }
      }
    }
  }
}
:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  .el-checkbox {
    margin-right: 0px;
    width: calc(50% - 4px);
    @media screen and (max-width: 1428.9px) {
      width: 100%;
    }
    &:nth-child(2n+1) {
      margin-right: 4px;
    }
    .el-checkbox__label {
      font-size: 14px;
      line-height: 20px;
      padding-left: 12px;
      color: #232748;
    }
    .el-checkbox__input {
      &.is-checked .el-checkbox__inner {
        border: none;
        border-color: #1460F3;
        background-color: #1460F3;
        &::before {
          width: 6px;
          content: '';
          height: 6px;
          margin: auto;
          margin-top: 3.5px;
          display: flex;
          border-radius: 50%;
          background-color: white;
        }
      }
      .el-checkbox__inner {
        width: 13px;
        height: 13px;
        border-radius: 50%;
        border: 1px solid #CCCCCC;
        &::after {
          display: none;
        }
      }
    }
  }
}
:deep(.whitespace-pre-line) {

  table {
    max-width: 100%;
    max-height: 100%;

    tbody tr th {
      font-weight: 600;
      color: #232748 !important;
      border: 1px solid #999 !important;
    }
  }

  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.32);
  }

  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }
}

.after {
  position: relative;
  color: #396FFA;

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

:deep(.el-overlay-dialog) {

  .el-dialog {

    &.jsonViewer {
      padding: 0;
    }

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

:deep(.jv-container) {
  width: 100%;
  float: right;
  overflow: auto;
  font-size: 16px;
  color: #01fef4;
  line-height: 20px;
  padding-left: 10px;
  white-space: nowrap;
  background: #030D26;
  font-family: 'Encode Sans';
  height: calc(100vh - 270px);

  &:hover {
    box-shadow: none;
  }
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.32);
  }

  &::-webkit-scrollbar-corner {
    background-color: transparent;
  }

  .jv-more {
    display: none;
  }

  &.jv-dark {
    margin: 0 16px;
    width: calc(100% - 32px);
    &.boxed {
      border: none;
    }
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
.uploadBtn {
  .contactTip {
    top: 29px;
    left: 85px;
    width: 220px;
    padding: 4px;
    display: none;
    font-size: 12px;
    line-height: 16px;
    position: absolute;
    color: #43474D;
    border-radius: 4px;
    background: white;
    border: 1px solid #D9D9D9;
    box-shadow: 0px 4px 4px 0px #00000033;
  }
  &:hover .contactTip {
    display: inline-block;
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
.el-overlay.is-message-box.addKey .el-overlay-message-box .el-message-box {
  .el-message-box__message {
    font-size: 16px;
    padding-left: 0;
    padding-right: 0;
    font-weight: 600;
    line-height: 24px;
    color: #404653;
  }
}
.schema-popover {
  padding: 0 !important;
  &.animation {
    word-break: auto-phrase;
    animation: floatUpDown 2s ease-in-out infinite;
  }
  box-shadow: 0px 4px 32px 0px #8195C852 !important;
}
.el-popper.is-pure.is-light.el-select__popper.disable {
  display: none;
}
.el-popper.is-dark {
  background: #596177;
  padding: 8px 8px 12px;
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
  &.upload {
    border-color: #F871714D;
    background-color: #FBEDED;
  }
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

.el-message--error {
  .el-message__content {
    color: #F87171;
  }
}
</style>
