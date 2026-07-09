<template>
  <div class="flex flex-col h-screen bg-[#F2F2F2] <lg:(bg-[#F3F6FF] h-100vh)" @keydown="handleKeyDown">
    <!-- Page Header (56px) -->
    <header class="flex items-center justify-between h-56px bg-white px-24px border-b border-[#E1E3E8] shrink-0">
      <div class="flex items-center gap-12px">
        <button class="flex items-center justify-center w-32px h-32px bg-transparent border-0 cursor-pointer text-[#0C131F] p-0" @click="router.push('/document-extraction')">
          <img :src="indentLeftIcon" alt="Back" class="w-20px h-20px" />
        </button>
        <span class="text-16px leading-24px text-[#0C131F]" style="font-family: 'Encode Sans', sans-serif;">AI Document Extraction</span>
      </div>
      <div class="flex items-center gap-16px text-14px leading-20px">
        <div class="flex items-center gap-8px px-6px py-5px rounded-4px cursor-pointer hover:bg-[#F5F7FF] relative" @click.stop="languageMenuVisible = !languageMenuVisible">
          <Language class="w-20px h-20px text-[#0C131F]" />
          <span class="text-[#0C131F]">{{ currentLanguageShort }}</span>
          <div v-show="languageMenuVisible" class="absolute top-[calc(100%+8px)] right-0 z-12 w-120px p-4px border-1 border-[#E7E8E8] rounded-4px bg-white shadow-[0_8px_24px_rgba(12,19,31,0.12)]">
            <button v-for="item in languageOptions" :key="item.value" type="button"
              :class="locale === item.value ? 'bg-[#F5F7FF] text-[#396FFA]' : 'text-[#232748]'"
              class="w-full px-10px py-6px border-0 rounded-4px bg-transparent text-14px leading-20px text-left cursor-pointer hover:(bg-[#F5F7FF] text-[#396FFA])"
              @click.stop="changeHeaderLanguage(item.value)">
              {{ item.label }}
            </button>
          </div>
        </div>
        <div class="w-1px h-26px bg-[#E7E8EC]"></div>
        <div class="flex items-center justify-center w-32px h-32px rounded-full bg-[#EBF1FE] text-[#396FFA]">
          <User class="w-20px h-20px" />
        </div>
      </div>
    </header>

    <!-- Three-column body -->
    <div class="flex flex-1 overflow-hidden <lg:hidden">
      <!-- ExtractionSidebar (fixed ~206px) -->
      <ExtractionSidebar
        :documentList="documentList"
        :activeDocId="activeDocId"
        :activeTab="activeTab"
        :isLoading="isLoading"
        @select-doc="handleSelectDoc"
        @change-tab="handleChangeTab"
        @filter="handleFilter"
        @back="handleBack"
        class="w-[206px] shrink-0 bg-white border-r border-[#E1E3E8]"
      />

      <!-- 自定义PDF nav -->
      <div v-show="fileType === 'pdf' && file" id="webviewer" ref="viewer" class="flex-1 relative overflow-hidden">
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

      <div class="flex-1 extract overflow-auto px-16px" :class="outputType === 'json' ? 'bg-[#030d26]' : 'bg-white'">
      <div class="text-white h-full">
        <div class="text-[#232748] font-500 flex 2xl:(text-16px leading-18px) @2xl:(text-14px leading-20px) @xl:(text-14px leading-20px) @lg:(text-12px leading-16px) border-b-1px border-[#E1E3E8]">
          <div @click="changeConvert('txt'), resultType = 'all'" class="flex justify-center py-10px items-center cursor-pointer 2xl:mx-16px <2xl:mx-6px"
            :class="[outputType === 'txt' && 'after', outputType === 'json' && 'text-white']">
            {{ t('singleExtract.customExtract') }}
          </div>
          <div @click="changeConvert('ai'), resultType = 'all'" class="flex justify-center py-10px items-center cursor-pointer 2xl:mx-16px <2xl:mx-6px"
            :class="[outputType === 'ai' && 'after', outputType === 'json' && 'text-white']">
            {{ t('singleExtract.allExtract') }}
          </div>
        </div>

        <!-- AI提取结果展示 -->
        <div v-show="outputType === 'ai' && !noResult" class="whitespace-pre-line relative mt-12px pb-24px h-[calc(100vh-220px)]" :class="!init && 'overflow-auto'">
          <!-- JSON结果数据展示 -->
          <div v-show="textNum + tableNum" @click="dialogVisibleJsonPreview = true" class="py-8px px-16px flex items-center justify-between bg-[#F3F6FF] border-b border-[#E1E3E8] mb-15px mt-8px cursor-pointer sticky top-0 z-10">
            <div class="flex items-center text-brand-0 font-500 text-xs">
              <JsonIcon class="mr-10px" />
              {{ 'View JSON Result' }}
            </div>
            <ArrowJson />
          </div>
          <div v-show="(textNum + tableNum) === 0 && !init && !dialogVisibleLoading" class="h-full flex flex-col justify-center items-center">
            <img src="/images/idp/no-data.png" alt="No data" width="240" height="150">
            <div class="text-sm text-[#232748] mt-32px" :class="outputType === 'json' && !init && '!text-white'">{{ t('knowledgeBases.home.empty') }}</div>
          </div>
          <!-- 结果数据展示 -->
          <div v-show="textNum + tableNum" class="text-[#232748] max-w-368px rounded-8px flex bg-[#EBF1FE] p-4px">
            <div @click="resultType = 'all'" :class="resultType === 'all' && 'bg-white rounded-4px'" class="px-12px py-4px flex justify-evenly items-center cursor-pointer w-120px">
              <All />{{ t('singleExtract.all') }}
              <span class="text-14px leading-20px py-2px px-8px rounded-8px text-[#94969D]" :class="resultType === 'all' && 'bg-[#F3F6FF]'">{{ textNum + tableNum }}</span>
            </div>
            <div @click="resultType = 'text'" :class="resultType === 'text' && 'bg-white rounded-4px'" class="px-12px py-4px flex justify-evenly items-center cursor-pointer w-120px">
              <Text />{{ t('singleExtract.text') }}
              <span class="text-14px leading-20px py-2px px-8px rounded-8px text-[#94969D]" :class="resultType === 'text' && 'bg-[#F3F6FF]'">{{ textNum }}</span>
            </div>
            <div @click="resultType = 'table'" :class="resultType === 'table' && 'bg-white rounded-4px'" class="px-12px py-4px flex justify-evenly items-center cursor-pointer w-120px">
              <Table />{{ t('singleExtract.table') }}
              <span class="text-14px leading-20px py-2px px-8px rounded-8px text-[#94969D]" :class="resultType === 'table' && 'bg-[#F3F6FF]'">{{ tableNum }}</span>
            </div>
          </div>
          <!-- Loading -->
          <div v-show="dialogVisibleLoading" class="h-[calc(100vh-220px)] flex justify-center items-center">
            <DemoLoading class="transform scale-70" />
          </div>
          <!-- 初始界面显示 -->
          <div v-show="init" class="h-[calc(100vh-220px)] flex flex-col justify-center items-center">
            <img src="/images/idp/init.png" alt="init" width="240" height="150">
            <div class="text-sm text-[#232748] mt-32px">
              {{ t('singleExtract.click') }}
            </div>
          </div>
           <!-- 字段结果展示 -->
          <template v-for="(page, pageNumStr, pageIndex) in editableTabs.aiDetails" :key="pageIndex">
            <div v-show="!init" @click="pageShow[pageIndex] = !pageShow[pageIndex]" class="my-12px flex justify-between items-center w-full hover:bg-[#F3F6FF] cursor-pointer">
              <p class="border-l-2px border-l-[#1460F3] font-600 text-[#1460F3] pl-8px text-14px leading-20px">{{ pageNumStr }}</p>
              <div class="flex items-center">
                <div
                  @click.stop="addKeyValue('ai', pageNumStr)"
                  class="mr-8px border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer hover:border-[#396FFA] text-12px leading-16px text-[#232748]">
                  +字段
                </div>
                <ExtractPull :class="!pageShow[pageIndex] && 'transforms'" class="transitions" />
              </div>
            </div>
            <div v-show="pageShow[pageIndex]" class="pb-16px">
              <div v-show="resultType === 'all' || resultType === 'text'" class="bg-white rounded-4px">
                <template v-for="(item, key, index) in page" :key="key">
                  <div v-if="key !== 'tables'"
                    @mouseenter="handleMouseenter(pageIndex, index, 'ai')"
                    @mouseleave="handleMouseleave(pageIndex, index, 'ai')"
                    class="relative px-16px py-8px text-sm bg-[#F3F6FF]"
                    :class="[key && 'mt-12px', contentEdit[pageIndex]?.[index]?.hover && 'handle']">
                    <div class="text-[#94969D]">{{ key }}</div>
                    <div :id="'text' + pageNumStr + index" @click.stop="updateDate(), contentEdit[pageIndex][index].status = true, pageTableKey = pageNumStr, textIndex = index, textPageIndex = pageIndex"
                      class="editContent text-[#232748] mt-4px outline-none whitespace-pre truncate w-full"
                      :class="contentEdit[pageIndex]?.[index]?.status && '!text-[#232748]'"
                      :contenteditable="contentEdit[pageIndex]?.[index]?.status">
                      {{ item }}
                    </div>
                    <div class="hidden border-1 border-[#E1E3E8] p-3px rounded-4px inline-block absolute cursor-pointer top-8px right-44px hover:border-[#396FFA] copy">
                      <Copy @click="copy(key + ': ' + item, pageIndex, index)" />
                      <div v-show="contentEdit[pageIndex]?.[index]?.copy === t('singleExtract.copy')" class="absolute z-10 top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                        {{ t('singleExtract.copy') }}
                      </div>
                      <div v-show="contentEdit[pageIndex]?.[index]?.copy === t('singleExtract.copied')" class="absolute z-10 top-[-30px] whitespace-nowrap text-black left-[-20px] hidden rounded-4px text-12px leading-16px py-5px px-8px bg-[#CCCCCC] tip !shadow-none">
                        {{ t('singleExtract.copied') }}
                      </div>
                    </div>
                    <div class="edit hidden border-1 border-[#E1E3E8] p-3px rounded-4px inline-block absolute cursor-pointer top-8px right-16px hover:border-[#396FFA]">
                      <Edit @click.stop="updateDate(), contentEdit[pageIndex][index].status = true, pageTableKey = pageNumStr, textIndex = index, focusInput(index, 'ai')" class="editIcon" />
                      <div class="absolute top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                        {{ t('singleExtract.edit') }}
                      </div>
                    </div>
                  </div>
                </template>
              </div>
              <!-- Table结果展示 -->
              <template v-if="page?.tables?.length">
                <div v-show="resultType === 'all' || resultType === 'table'" v-for="(item, indexTable) in page?.tables" :key="indexTable" class="mt-12px pt-28px">
                  <div v-show="tableNum" class="flex justify-between mb-4px sticky -mt-28px z-10">
                    <div class="text-[#94969D] text-sm">Table - {{ indexTable + 1 }}</div>
                    <div class="flex">
                      <div @click="openDialog('table'), pageTableKey = pageNumStr, tableListIndex = indexTable" class="border-1 border-[#E1E3E8] p-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA]">
                        <Download />
                      </div>
                      <div @click="startAiTableEdit(pageNumStr, indexTable)"
                        class="edit relative border-1 border-[#E1E3E8] p-3px rounded-4px cursor-pointer hover:border-[#396FFA]">
                        <Edit />
                        <div class="absolute whitespace-nowrap top-25px left-[-7px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                          {{ t('singleExtract.edit') }}
                        </div>
                      </div>
                      <div v-show="contentTable[indexTable]" class="flex ml-4px">
                        <div @click="addAiTableRow(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA] text-12px leading-16px text-[#232748]">+Row</div>
                        <div @click="addAiTableColumn(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA] text-12px leading-16px text-[#232748]">+Col</div>
                        <div @click="deleteAiTableRow(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA] text-12px leading-16px text-[#232748]">-Row</div>
                        <div @click="deleteAiTableColumn(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer hover:border-[#396FFA] text-12px leading-16px text-[#232748]">-Col</div>
                      </div>
                    </div>
                  </div>
                  <div class="overflow-auto">
                    <table border="1" class="text-[#232748] rounded-4px border-collapse overflow-hidden">
                      <thead>
                        <tr>
                          <th v-for="(_value, key) in item[0]" :key="key" class="bg-[#F3F6FF] text-14px leading-20px text-[#94969D] px-12px py-10px font-normal border-1 border-[#E1E3E8">{{ key }}</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(indexChi, index) in item" :key="index">
                          <td v-for="(_value, key, indexSun) in indexChi" :key="indexSun" :id="'table' + pageNumStr + indexTable + index + indexSun"
                            :contenteditable="contentTable[indexTable]"
                            @click.stop="onAiTableCellClick(pageNumStr, indexTable, index, indexSun)"
                            @blur="onAiTableCellBlur(pageNumStr, indexTable, index, key, indexSun, $event)"
                            @keydown.enter.prevent
                            class="outline-none px-12px py-10px border-1 border-[#E1E3E8]">
                            {{ indexChi[key] }}
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

        <!-- 自定义提取结果展示 -->
        <div v-show="outputType === 'txt' && !noResult" class="whitespace-pre-line mt-12px h-[calc(100%-50.62px)] flex flex-col justify-between relative overflow-y-visible">
          <div ref="resultContent" @scroll="handleScroll" class="flex flex-col flex-1 min-h-0 overflow-auto">
            <!-- JSON结果数据展示 -->
            <div v-show="customTextNum + customTableNum" @click="dialogVisibleJsonPreview = true" class="py-8px px-16px flex items-center justify-between bg-[#F3F6FF] border-b border-[#E1E3E8] mb-15px mt-8px cursor-pointer sticky top-0 z-10">
              <div class="flex items-center text-brand-0 font-500 text-xs">
                <JsonIcon class="mr-10px" />
                {{ 'View JSON Result' }}
              </div>
              <ArrowJson />
            </div>
            <!-- 结果数据展示 -->
            <div v-show="customTextNum + customTableNum" class="text-[#232748] max-w-392px rounded-8px flex bg-[#EBF1FE] p-4px">
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
            </div>
            <!-- 字段结果展示 -->
            <div class="flex flex-col">
              <template v-for="(page, pageNumStr, pageIndex) in editableTabs.customDetails" :key="pageIndex">
                <div v-show="!customInit" @click="customPageShow[pageIndex] = !customPageShow[pageIndex]" class="my-12px flex justify-between items-center w-full hover:bg-[#F3F6FF] cursor-pointer">
                  <p class="border-l-2px border-l-[#1460F3] font-600 text-[#1460F3] pl-8px text-14px leading-20px">{{ pageNumStr }}</p>
                  <div class="flex items-center">
                    <div
                      @click.stop="addKeyValue('custom', pageNumStr)"
                      class="mr-8px border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer hover:border-[#396FFA] text-12px leading-16px text-[#232748]">
                      +字段
                    </div>
                    <ExtractPull :class="!customPageShow[pageIndex] && 'transforms'" class="transitions" />
                  </div>
                </div>
                <div v-show="customPageShow[pageIndex]" class="pb-16px">
                  <div v-show="resultType === 'all' || resultType === 'text'" class="bg-white rounded-4px">
                    <template v-for="(item, key, index) in page" :key="key">
                      <div v-if="key !== 'tables'"
                        @mouseenter="handleMouseenter(pageIndex, index, 'custom')"
                        @mouseleave="handleMouseleave(pageIndex, index, 'custom')"
                        class="relative px-16px py-8px text-sm bg-[#F3F6FF]"
                        :class="[key && 'mt-12px', customContentEdit[pageIndex]?.[index].hover && 'handle']">
                        <div class="text-[#94969D]">{{ key }}</div>
                        <div :id="'customText' + pageNumStr + index" @click.stop="updateDate(), customContentEdit[pageIndex][index].status = true, customPageTableKey = pageNumStr, customTextIndex = index, customTextPageIndex = pageIndex"
                          class="editContent text-[#232748] mt-4px outline-none whitespace-pre truncate w-full"
                          :class="customContentEdit[pageIndex]?.[index]?.status && '!text-[#232748]'"
                          :contenteditable="customContentEdit[pageIndex]?.[index]?.status">
                          {{ item }}
                        </div>
                        <div class="hidden border-1 border-[#E1E3E8] p-3px rounded-4px inline-block absolute cursor-pointer top-8px right-44px hover:border-[#396FFA] copy">
                          <Copy @click="copy(key + ': ' + item, pageIndex, index)" />
                          <div v-show="customContentEdit[pageIndex]?.[index].copy === t('singleExtract.copy')" class="absolute z-10 top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                            {{ t('singleExtract.copy') }}
                          </div>
                          <div v-show="customContentEdit[pageIndex]?.[index].copy === t('singleExtract.copied')" class="absolute z-10 top-[-30px] whitespace-nowrap text-black left-[-20px] hidden rounded-4px text-12px leading-16px py-5px px-8px bg-[#CCCCCC] tip !shadow-none">
                            {{ t('singleExtract.copied') }}
                          </div>
                        </div>
                        <div class="edit hidden border-1 border-[#E1E3E8] p-3px rounded-4px inline-block absolute cursor-pointer top-8px right-16px hover:border-[#396FFA]">
                          <Edit @click.stop="updateDate(), customContentEdit[pageIndex][index].status = true, customPageTableKey = pageNumStr, customTextIndex = index, focusInput(index, 'custom')" class="editIcon" />
                          <div class="absolute top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                            {{ t('singleExtract.edit') }}
                          </div>
                        </div>
                      </div>
                    </template>
                  </div>
                  <!-- Table结果展示 -->
                  <template v-if="page?.tables">
                    <div v-show="resultType === 'all' || resultType === 'table'" v-for="(item, indexTable) in page?.tables" :key="indexTable" class="mt-12px pt-28px">
                      <div v-show="customTableNum" class="flex justify-between mb-4px sticky -mt-28px z-10">
                        <div class="text-[#94969D] text-sm">Table - {{ indexTable + 1 }}</div>
                        <div class="flex">
                          <div @click="openDialog('table'), customPageTableKey = pageNumStr, customTableListIndex = indexTable" class="border-1 border-[#E1E3E8] p-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA]">
                            <Download />
                          </div>
                          <div @click="startCustomTableEdit(pageNumStr, indexTable)"
                            class="edit relative border-1 border-[#E1E3E8] p-3px rounded-4px cursor-pointer hover:border-[#396FFA]">
                            <Edit />
                            <div class="absolute top-25px whitespace-nowrap left-[-7px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
                              {{ t('singleExtract.edit') }}
                            </div>
                          </div>
                          <div v-show="customContentTable[indexTable]" class="flex ml-4px">
                            <div @click="addCustomTableRow(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA] text-12px leading-16px text-[#232748]">+Row</div>
                            <div @click="addCustomTableColumn(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA] text-12px leading-16px text-[#232748]">+Col</div>
                            <div @click="deleteCustomTableRow(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer mr-4px hover:border-[#396FFA] text-12px leading-16px text-[#232748]">-Row</div>
                            <div @click="deleteCustomTableColumn(pageNumStr, indexTable)" class="border-1 border-[#E1E3E8] px-8px py-3px rounded-4px cursor-pointer hover:border-[#396FFA] text-12px leading-16px text-[#232748]">-Col</div>
                          </div>
                        </div>
                      </div>
                      <div class="overflow-auto">
                        <table border="1" class="text-[#232748] rounded-4px border-collapse overflow-hidden">
                          <thead>
                            <tr>
                              <th v-for="(_value, key) in item[0]" :key="key" class="bg-[#F3F6FF] text-14px leading-20px text-[#94969D] px-12px py-10px font-normal border-1 border-[#E1E3E8">{{ key }}</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(indexChi, index) in item" :key="index">
                              <td v-for="(_value, key, indexSun) in indexChi" :key="indexSun" :id="'customTable' + pageNumStr + indexTable + index + indexSun" :contenteditable="customContentTable[indexTable]" @click.stop="onCustomTableCellClick(pageNumStr, indexTable, index, indexSun)" @blur="onCustomTableCellBlur(pageNumStr, indexTable, index, key, indexSun, $event)" @keydown.enter.prevent class="outline-none px-12px py-10px border-1 border-[#E1E3E8]">{{ indexChi[key] }}</td>
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
          <!-- 空状态 -->
          <div v-show="(customTextNum + customTableNum) === 0 && !customInit && !dialogVisibleLoading" class="flex-1 flex flex-col justify-center items-center">
            <img src="/images/idp/no-data.png" alt="No data" width="240" height="150">
            <div class="text-sm text-[#232748] mt-32px" :class="outputType === 'json' && !init && '!text-white'">{{ t('knowledgeBases.home.empty') }}</div>
          </div>
          <!-- Loading -->
          <div v-show="dialogVisibleLoading" class="flex-1 flex justify-center items-center">
            <DemoLoading class="transform scale-70" />
          </div>
          <!-- 初始界面显示 -->
          <div v-show="customInit" class="flex-1 flex flex-col justify-center items-center">
            <img src="/images/idp/init.png" alt="init" width="240" height="150">
            <div class="text-sm text-[#232748] mt-32px">
              {{ t('singleExtract.click') }}
            </div>
          </div>
          <div v-show="collapse" class="absolute bottom-80px left-1/2 -translate-x-1/2 flex items-center justify-center">
            <div @click="collapse = false" class="cursor-pointer border border-[#CDDBFF] border-b-none rounded-t-6px py-6px px-12px flex items-center text-brand-2">
              {{ t('template.edit') }}
              <Extension class="ml-4px" />
            </div>
          </div>
          <!-- 要抽取的字段输入框 -->
          <div v-show="!collapse" class="w-full bg-white border-t-1 border-[#0000001A] sticky bottom-0 z-11 pb-16px px-1px">
            <div class="text-14px leading-20px text-black font-500 my-16px flex items-center justify-between">
              <div class=" flex items-center <xl:inline-block">
                {{ t('singleExtract.tip[0]') }}
                <span class="text-brand-2">{{ t('singleExtract.tip[1]') }}</span>
                <div class="flex ml-8px relative">
                  <el-tooltip popper-class="tip-item" effect="dark" :content="t('singleExtract.enter')" placement="top">
                    <Question class="cursor-pointer" />
                  </el-tooltip>
                </div>
              </div>
              <el-tooltip popper-class="box-item" effect="dark" :content="t('template.collapse')" placement="top">
                <Collapse class="text-[#94969D] hover:text-brand-2 cursor-pointer" @click="collapse = true" />
              </el-tooltip>
            </div>
            <div class="mb-12px">
              <div class="flex items-center">
                <el-popover v-model:visible="schemaShow" trigger="" placement="top" popper-class="schema-popover animation" width="320px">
                  <template #reference>
                    <div class="text-black text-xs font-500 mr-16px relative w-fit whitespace-nowrap">{{ t('template.title')  }}:</div>
                  </template>
                  <template #default>
                    <div class="bg-white float-up-down text-brand-3 py-20px px-12px">
                      <CloseSchema @click="schemaShow = false" class="absolute top-12px right-10px cursor-pointer" />
                      <div class="text-sm font-600 flex items-center">
                        {{ t('template.title')  }}
                        <div class="rounded-10px bg-[#00CF85] py-2px px-8px text-14px leading-16px ml-12px text-white font">New</div>
                      </div>
                      <div class="text-xs mt-8px">{{ t('template.feature')  }}</div>
                    </div>
                  </template>
                </el-popover>
                <div class="flex justify-between" :class="locale === 'en' ? 'w-[calc(100%-112px)]' : 'w-[calc(100%-72px)]'">
                  <div class="flex overflow-x-auto w-[calc(100%-49px)] tags-container" ref="tagsContainer">
                    <div :class="[
                        templateList[item].order - 1 && 'ml-4px',
                        'order-' + templateList[item].order,
                        activeTemplate === item && 'border-transparent bg-[#F3F6FF] border-b-2 border-b-brand-2 border-solid rounded-b-l-none template-item',
                      ]"
                      @dblclick="['Invoice', 'Order'].includes(item) ? '' : (edit = true, dialogVisibleSetName = true, templateName = item)"
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
                      <PulldownTem v-show="(Object.keys(templateList).length > 4)" @click.stop="pullShow = !pullShow" class="cursor-pointer text-brand-3 pulldownTem" />
                      <div v-show="pullShow" class="rounded-4px p-4px shadowTemp absolute bg-white right-0 top-28px z-10">
                        <div @click="pulldownChange(item)" v-for="(item, index) in pullList" :key="index" class="text-brand-0 rounded-6px py-4px px-12px cursor-pointer whitespace-nowrap hover:bg-[#1460F31A]">
                          {{ item }}
                        </div>
                      </div>
                    </div>
                    <div class="h-16px w-1px bg-[#E1E3E8] mx-4px"></div>
                    <el-tooltip popper-class="box-item" effect="dark" :content="t('template.add')" placement="top">
                      <AddTemplate v-show="!(Object.keys(templateList).length === 7)" @click="addTemplate" class="cursor-pointer addTemplate" />
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex items-center mb-8px relative">
              <div class="whitespace-nowrap mr-8px text-[#232748] h-40px flex items-center absolute z-2 pl-12px">
                {{ t('bulkExtract.textField') }}:
                <div class="flex ml-8px relative">
                  <el-tooltip popper-class="tip-item" effect="dark" :content="t('template.fieldTip')" placement="top">
                    <Question class="cursor-pointer" />
                  </el-tooltip>
                </div>
              </div>
              <div v-show="addField" class="absolute h-40px z-2 w-full flex items-center" :class="locale === 'en' ? 'left-158px' : 'left-120px'">
                <div @click="focus('field')" class="w-full text-[#94969D] text-14px leading-20px flex items-center cursor-pointer"><Add class="mr-8px" />
                  {{ t('bulkExtract.addField') }}
                </div>
              </div>
              <div v-show="fieldFocus" class="h-full flex items-center absolute right-16px z-2 w-14px">
                <Clear v-show="!['Invoice', 'Order'].includes(activeTemplate)" @click="deleteInput('field')" class="w-14px h-14px cursor-pointer" />
              </div>
              <div class="w-[calc(100%-4px)]">
                <el-select class="extractSelect" :class="locale === 'en' ? 'en' : 'cn'" ref="inputField"
                  v-model="editableTabs.fieldsList" multiple filterable
                  @focus="fieldFocus = true" allow-create default-first-option
                  :reserve-keyword="false" popper-class="disable"
                  placeholder=" " style="min-width: 240px"
                  @keydown.capture.backspace="onBackspace($event, 'field')"
                  @blur="blurInput('field')" @change="clearInput('field')">
                  <template v-if="editableTabs.fieldsList.length" #tag>
                    <template v-for="(item, index) in editableTabs.fieldsList" :key="index">
                      <el-popover v-model:visible="templateList[activeTemplate].keysPromptShow[index]" trigger="" placement="top" popper-class="schema-popover" width="320px">
                        <template #reference>
                          <el-tag class="custom" closable @click="templateList[activeTemplate].keysPromptShow[index] = !templateList[activeTemplate].keysPromptShow[index], sourceContent = templateList[activeTemplate].keysTip[index]" @close="removeItem(index, 'field')">
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
            </div>
            <div class="flex items-center relative">
              <div class="whitespace-nowrap mr-8px text-[#232748] h-40px flex items-center absolute z-2 pl-12px">
                {{ t('bulkExtract.tableHeader') }}:
                <div class="flex ml-8px relative">
                  <el-tooltip popper-class="tip-item" effect="dark" :content="t('template.tableTip')" placement="top">
                    <Question class="cursor-pointer" />
                  </el-tooltip>
                </div>
              </div>
              <div v-show="addTable" class="absolute h-40px z-2 w-full flex items-center" :class="locale === 'en' ? 'left-158px' : 'left-120px'">
                <div @click="focus('table')" class="w-full text-[#94969D] text-14px leading-20px flex items-center cursor-pointer"><Add class="mr-8px" />
                  {{ t('bulkExtract.addHeader') }}
                </div>
              </div>
              <div v-show="tableFocus" class="h-full flex items-center absolute right-16px z-2 w-14px">
                <Clear v-show="!['Invoice', 'Order'].includes(activeTemplate)" @click="deleteInput('table')" class="w-14px h-14px cursor-pointer" />
              </div>
              <div class="w-[calc(100%-4px)]">
                <el-select class="extractSelect" :class="locale === 'en' ? 'en' : 'cn'" ref="inputTable"
                  v-model="editableTabs.tableList" multiple filterable
                  @focus="tableFocus = true" allow-create default-first-option
                  :reserve-keyword="false" popper-class="disable"
                  placeholder=" " style="min-width: 240px"
                  @keydown.capture.backspace="onBackspace($event, 'table')"
                  @blur="blurInput('table')" @change="clearInput('table')">
                  <template v-if="editableTabs.tableList.length" #tag>
                    <template v-for="(item, index) in editableTabs.tableList" :key="index">
                      <el-popover v-model:visible="templateList[activeTemplate].tablePromptShow[index]" trigger="" placement="top" popper-class="schema-popover" width="320px">
                        <template #reference>
                          <el-tag class="custom" closable @click="templateList[activeTemplate].tablePromptShow[index] = !templateList[activeTemplate].tablePromptShow[index], sourceContent = templateList[activeTemplate].tableHandlesTip[index]" @close="removeItem(index, 'table')">
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
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>

    <div class="shrink-0 h-80px border-t border-[#E1E3E8] w-full bg-white flex items-center <lg:hidden">
      <!-- Hidden file input for upload button -->
      <input type="file" ref="input" @change="handleChange" accept=".pdf,.jpg,.png,.jpeg" class="hidden" />
      <div class="w-[calc(50%-38px)] flex items-center border-r-1 border-[#E1E3E8] h-80px">
        <div @click="input.click" class="font w-140px text-brand-2 font-600 text-14px leading-16px border-1 rounded-4px border-brand-2 hover:(bg-brand-2 text-white) cursor-pointer py-12px flex items-center justify-center mx-auto">
          <Upload class="mr-2px" />
          {{ t('singleExtract.open') }}
        </div>
      </div>
      <div class="w-[calc(50%-38px)] flex justify-center items-center">
        <div @click="uploadClick"
          class="cursor-pointer relative z-1 font-600 h-41px w-162px flex justify-center items-center text-center text-white rounded-2px text-14px leading-16px"
          :class="dialogVisibleLoading && 'bg-[#396FFA80] !hover:bg-[#396FFA80]'">
          <template v-if="!dialogVisibleLoading">
            <button type="button" class="button" :class="!file && 'disabled'">
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
              <span class="inner font"><Light class="icon" />{{ firstExtract ? t('singleExtract.now') : t('singleExtract.reExtract') }}</span>
            </button>
          </template>
          <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 384" class="loader">
            <circle r="176" cy="192" cx="192" stroke-width="24" fill="transparent" pathLength="360" class="active"></circle>
            <circle r="176" cy="192" cx="192" stroke-width="24" fill="transparent" pathLength="360" class="track"></circle>
          </svg>
        </div>
        <div v-if="!saveShow" @click="openDialog('all')"
          :class="outputType === 'ai' ? (aiDownload || !(textNum + tableNum)) && 'disable' : (customDownload || !(customTextNum + customTableNum)) && 'disable'"
          class="font cursor-pointer font-600 w-162px py-12px text-center border-1 border-[#396FFA] text-[#396FFA] rounded-4px text-14px leading-16px hover:(bg-[#396FFA] text-white) ml-10px">
          {{ t('singleExtract.download') }}
        </div>
        <template v-if="outputType === 'txt' && (saveShow || btnShow)">
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

    <!-- download -->
    <el-dialog v-model="dialogVisible" width="372px" top="20vh">
      <div class="flex justify-end">
        <IdpClose @click="dialogVisible = false" class="cursor-pointer w-17px h-17px" />
      </div>
      <p class="text-[#43474D] text-sm font-bold mb-12px">{{ t('singleExtract.selectFormat') }}</p>
      <el-radio-group v-model="toType">
        <template v-if="!tableDownload">
          <el-radio v-if="['all', 'text'].includes(resultType)" label="json">JSON</el-radio>
          <el-radio v-if="['all', 'table'].includes(resultType)" label="xlsx">Excel</el-radio>
          <el-radio v-if="['all', 'table'].includes(resultType)" label="csv">CSV</el-radio>
          <el-radio v-if="['all', 'text'].includes(resultType)" label="txt">TXT</el-radio>
        </template>
        <template v-else>
          <el-radio label="xlsx">Excel</el-radio>
          <el-radio label="csv">CSV</el-radio>
        </template>
      </el-radio-group>
      <div @click="downloadFile" class="cursor-pointer font-600 w-full h-40px flex justify-center items-center bg-[#396FFA] mt-21px text-white rounded-4px text-sm hover:bg-[#244FF0]">
        {{ t('singleExtract.extract') }}
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
      <JsonViewer boxed expanded :expandDepth="7" sort theme="dark" :value="jsonShow === 'ai' ? editableTabs.aiDetails : editableTabs.customDetails" />
      <div v-show="(textNum + tableNum) || (customTextNum + customTableNum)" class="copyTip w-24px h-24px border-1 border-[#E1E3E8] inline-block absolute p-3px rounded-4px cursor-pointer bg-white right-24px top-88px hover:border-[#396FFA]">
        <Copy @click="copy(jsonShow === 'ai' ? editableTabs.aiDetails : editableTabs.customDetails, -1, 0)" />
        <div class="absolute top-22px whitespace-nowrap left-[-6px] hidden bg-[#F2F3F5] rounded-4px text-12px leading-16px text-[#43474D] p-3px border-1 border-[#D9D9D9] tip">
          {{ t('singleExtract.copy') }}
        </div>
      </div>
      <div @click="openDialog('all')"
        class="font cursor-pointer font-700 w-162px py-12px text-center text-white bg-[#396FFA] rounded-8px text-14px leading-16px mx-auto my-20px hover:bg-[#244FF0]">
        {{ t('singleExtract.download') }}
      </div>
    </el-dialog>

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
import { post, get } from '../utils/request'
import Hand from '../components/images/Hand.vue'
import HandHover from '../components/images/HandHover.vue'
import AddZoom from '../components/images/AddZoom.vue'
import Docs from '../components/images/Docs.vue'
import CloseDialog from '../components/images/CloseDialog.vue'
import Previous from '../components/images/Previous.vue'
import ComArrow from '../components/images/ComArrow.vue'
import ReduceZoom from '../components/images/ReduceZoom.vue'
import JsonIcon from '../components/images/JsonIcon.vue'
import ArrowJson from '../components/images/ArrowJson.vue'
import Tips from '../components/images/Tips.vue'
import 'vue3-json-viewer/dist/vue3-json-viewer.css'
import ComPDFKitViewer from "../assets/@compdfkit/webviewer"
import { ref, watch, computed, onMounted, onBeforeUnmount, nextTick, onUnmounted, type Ref } from 'vue'
import Clear from "../components/images/Clear.vue"
import ExtractPull from "../components/images/ExtractPull.vue"
import DemoLoading from "../components/DemoLoading.vue"
import { getEnv } from '../utils/env'
import CloseSchema from "../components/closeSchema.vue"
import AddTemplate from "../components/images/AddTemplate.vue"
import DeleteTemplate from "../components/images/DeleteTemplate.vue"
import ExtractionSidebar from '../view/ExtractionSidebar.vue'
import { useRouter } from 'vue-router'
import indentLeftIcon from '../assets/images/indent-left.svg'
import Language from '../components/images/Language.vue'
import User from '../components/images/User.vue'

const { t, locale } = useI18n()
const router = useRouter()
const languageMenuVisible = ref(false)

const languageOptions = [
  { value: 'en', label: 'English', short: 'En', apiValue: 'English' },
  { value: 'zh-cn', label: '简中', short: '简中', apiValue: 'Chinese' },
  { value: 'zh-tw', label: '繁中', short: '繁中', apiValue: 'Traditional Chinese' },
  { value: 'ja', label: '日本語', short: '日本語', apiValue: 'Japanese' }
] as const

const currentLanguageShort = computed(() => languageOptions.find(item => item.value === locale.value)?.short || 'En')

const closeLanguageMenu = () => {
  languageMenuVisible.value = false
}

const changeHeaderLanguage = async (value: string) => {
  const option = languageOptions.find(item => item.value === value)
  if (!option) return
  await post('/v1/user/setting', { language: option.apiValue })
  localStorage.setItem('locale', option.value)
  locale.value = option.value
  languageMenuVisible.value = false
  ElMessage.success(t('knowledgeBases.configuration.update'))
}

onMounted(() => {
  addEventListener('click', closeLanguageMenu)
})

onBeforeUnmount(() => {
  removeEventListener('click', closeLanguageMenu)
})

const timer = ref()
const dragover = ref(false)
const init = ref(true)
const type = ref('single')
const showBtn = ref(false)
const customInit = ref(true)
const textPageIndex = ref()
const textIndex = ref()
const tableIndex = ref()
const tableListIndex = ref()
const tableSunIndex = ref()
const pageTableKey = ref()
const pdfPage = ref()
const edit = ref(false)
const templateName = ref()
const pdfCurrentPage = ref(1)
const scale = ref(100)
const collapse = ref(false)
const scaleShow = ref(false)
const navShow = ref(false)
const hand = ref(0)
const resultContent = ref()
const sourceContent = ref('')
const schemaShow = ref(false)
const pullShow = ref(false)
const saveShow = ref(true)
const dialogVisibleSetName = ref(false)
const dialogVisibleJsonPreview = ref(false)
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
const activeTemplate = ref('Invoice')
const customTextIndex = ref()
const customTextPageIndex = ref()
const customTableIndex = ref()
const customTableListIndex = ref()
const customTableSunIndex = ref()
const customPageTableKey = ref()
const textNum = ref(0)
const tableNum = ref(0)
const customTextNum = ref(0)
const customTableNum = ref(0)
const resultType = ref('all')
const firstExtract = ref(true)
const pageShow = ref<boolean[]>([])
const customPageShow = ref<boolean[]>([])
interface contentEditType {
  status: boolean,
  hover: boolean,
  copy: string
}
const contentTable = ref<boolean[]>([])
const contentEdit = ref<Array<contentEditType[]>>([])
const customContentTable = ref<boolean[]>([])
const customContentEdit = ref<Array<contentEditType[]>>([])

// Document list state for selecting previous extraction files
interface ExtractionFileItem {
  fileId: string
  fileName: string
  fileDownUrl: string
  resultDownUrl: string
  status: number
  createTime?: string
}
const documentList = ref<ExtractionFileItem[]>([])
const activeDocId = ref<string>('')
const activeTab = ref<string>('all')
const isLoading = ref(false)
const docListError = ref('')

const handleSelectDoc = (docId: string) => {
  activeDocId.value = docId
  loadDocument(docId)
}

const handleChangeTab = (tabName: string) => {
  activeTab.value = tabName
}

const handleFilter = () => {
  // Filter functionality - can be expanded later
}

const handleBack = () => {
  router.push('/document-extraction')
}

const loadDocument = async (docId: string) => {
  activeDocId.value = docId
  // Document loading logic to be implemented when API is available
}

const pullList = computed(() => {
  let arr: string[] = []
  if ([6,7,8].includes(Object.keys(templateList.value).length)) {
    arr = Object.keys(templateList.value).slice(-3)
  } else if ([5, 4].includes(Object.keys(templateList.value).length)) {
    arr = Object.keys(templateList.value).slice(-2)
  }
  return arr
})
const handleMouseenter = (pageIndex: number, index: number, type: string) => {
  if (type === 'ai') {
    contentEdit.value[pageIndex][index].hover = true
  } else {
    customContentEdit.value[pageIndex][index].hover = true
  }
}
const handleMouseleave = (pageIndex: number, index: number, type: string) => {
  if (type === 'ai') {
    contentEdit.value[pageIndex][index].hover = false
  } else {
    customContentEdit.value[pageIndex][index].hover = false
  }
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
const onBackspace = (e: KeyboardEvent, type: 'table' | 'field') => {
  if (e.isComposing) return
  const target = e.target as HTMLInputElement | null
  if (target?.value) return
  removeItem(type === 'table' ? editableTabs.value.tableList.length - 1 : editableTabs.value.fieldsList.length - 1, type)
}
const btnShow = computed(() => {
  let num = 0
  Object.keys(templateList.value).forEach((item) => {
    if (templateList.value[item].canSave) {
      num++
    }
  })
  return num
})
const downloadCSV = () => {
  const zip = new JSZip()
  const data = outputType.value === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails

  Object.keys(data).forEach(key => {
    const page = data[key]
    const { tables, ...invoiceDetails } = page || {}

    let hasContent = false
    let csvContent = ''

    // 发票信息部分
    const detailsArray = Object.entries(invoiceDetails || {}).map(([k, v]) => ({
      Key: k,
      Value: JSON.stringify(v),
    }))
    if (detailsArray.length) {
      const detailsCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(detailsArray))
      csvContent += detailsCSV
      hasContent = true
    }

    // 表格部分
    if (Array.isArray(tables) && tables.length > 0) {
      tables.forEach((table: any[], index: number) => {
        if (Array.isArray(table) && table.length > 0) {
          const tableCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(table))
          csvContent += `\n\n${key} Table ${index + 1}\n` + tableCSV
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
      saveAs(content, `${downloadName.value}_compdf_ai_signle_extract.zip`)
    }
  })
}

const downloadSingleCsv = () => {
  const data = outputType.value === 'ai' ? editableTabs.value.aiDetails[pageTableKey.value] : editableTabs.value.customDetails[customPageTableKey.value]
  const index = outputType.value === 'ai' ? tableListIndex.value : customTableListIndex.value
  if (typeof index === 'number') {
    const tableCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(data?.tables?.[index] ?? []))
    const csvData = tableCSV // 在每个表格前加入表格标识
    const blob = new Blob([csvData], { type: "text/csv;charset=utf-8;" })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `_compdf_ai_signle_extract.csv` // 设置文件名
    link.click()
  } else {
    const zip = new JSZip()
    const data = outputType.value === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails

    Object.keys(data).forEach(pageKey => {
      const page = data[pageKey]
      if (page && Array.isArray(page.tables) && page.tables.length > 0) {
        let pageCSV = '' // 用于拼接该页面的所有表格数据

        page.tables.forEach((table: any[], index: number) => {
          if (Array.isArray(table) && table.length > 0) {
            const tableCSV = XLSX.utils.sheet_to_csv(XLSX.utils.json_to_sheet(table))
            pageCSV += `\n\n${pageKey} Table ${index + 1}\n` + tableCSV
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
  const data = outputType.value === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails
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
    if (Array.isArray(tables)) {
      tables.forEach((table: any[], index: number) => {
        if (table.length > 0) {
          const tableSheet = XLSX.utils.json_to_sheet(table)
          XLSX.utils.book_append_sheet(workbook, tableSheet, `Page-${key}_Table_${index + 1}`)
        }
      })
    }

    // 如果有有效的 Sheet，则导出成 xlsx 并放入 zip
    if (workbook.SheetNames.length > 0) {
      const wbout = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
      zip.file(`${downloadName.value}_${key}_compdf_ai_signle_extract.xlsx`, wbout)
    }
  }

  // 生成并下载 zip 文件
  const content = await zip.generateAsync({ type: 'blob' })
  saveAs(content, `${downloadName.value}_compdf_ai_signle_extract.zip`)
}

const downloadSingleXlsx = async () => {
  const workbook = XLSX.utils.book_new()
  const data = outputType.value === 'ai' ? editableTabs.value.aiDetails[pageTableKey.value] : editableTabs.value.customDetails[customPageTableKey.value]
  const index = outputType.value === 'ai' ? tableListIndex.value : customTableListIndex.value
  if (typeof index === 'number') {
    const tableSheet = XLSX.utils.json_to_sheet(data?.tables?.[index] ?? [])
    // 添加每个表格为单独的工作表
    XLSX.utils.book_append_sheet(workbook, tableSheet, 'Table')
    XLSX.writeFile(workbook, `${downloadName.value}_compdf_ai_signle_extract.xlsx`)
  } else {
    const data = outputType.value === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails
    const zip = new JSZip()

    Object.keys(data).forEach(pageKey => {
      const page = data[pageKey]
      if (!page || !page.tables || page.tables.length === 0) return

      const workbook = XLSX.utils.book_new()

      page.tables.forEach((table: any[], index: number) => {
        if (!Array.isArray(table) || table.length === 0) return
        const sheet = XLSX.utils.json_to_sheet(table)
        const sheetName = `${pageKey}_table_${index + 1}` // 如 Page-3_table_1
        XLSX.utils.book_append_sheet(workbook, sheet, sheetName)
      })

      if (workbook.SheetNames.length > 0) {
        const out = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
        zip.file(`${downloadName.value}_${pageKey}_compdf_ai_signle_extract.xlsx`, out)
      }
    })

    const blob = await zip.generateAsync({ type: 'blob' })
    saveAs(blob, `${downloadName.value}_compdf_ai_signle_extract.zip`)
  }
}
const focusInput = (index: number, type: string) => {
  const dom = document.getElementById((type === 'ai' ? 'text' + pageTableKey.value : 'customText' + customPageTableKey.value) + index)
  nextTick(() => {
    if (dom) {
      dom.focus()
      // 设置光标到末尾
      const range = document.createRange()
      const selection = window.getSelection()
      range.selectNodeContents(dom)
      range.collapse(false) // false 表示光标在内容末尾
      if (selection) {
        selection.removeAllRanges()
        selection.addRange(range)
      }
    }
  })
}
const focusTable = (listIndex: number, pageKey: string, index: number, sunIndex: number, type: string) => {
  const dom = document.getElementById(`${type === 'ai' ? 'table' : 'customTable'}${pageKey}${listIndex}${index}${sunIndex}`)
  nextTick(() => {
    if (dom) {
      dom.focus()
      // 设置光标到末尾
      const range = document.createRange()
      const selection = window.getSelection()
      range.selectNodeContents(dom)
      range.collapse(false) // false 表示光标在内容末尾
      if (selection) {
        selection.removeAllRanges()
        selection.addRange(range)
      }
    }
  })
}

const startAiTableEdit = (pageKey: string, listIndex: number) => {
  updateDate()
  contentTable.value[listIndex] = true
  pageTableKey.value = pageKey
  tableListIndex.value = listIndex
  tableIndex.value = 0
  tableSunIndex.value = 0
  focusTable(listIndex, pageKey, 0, 0, 'ai')
}

const startCustomTableEdit = (pageKey: string, listIndex: number) => {
  updateDate()
  customContentTable.value[listIndex] = true
  customPageTableKey.value = pageKey
  customTableListIndex.value = listIndex
  customTableIndex.value = 0
  customTableSunIndex.value = 0
  focusTable(listIndex, pageKey, 0, 0, 'text')
}

const onAiTableCellClick = (pageKey: string, listIndex: number, rowIndex: number, colIndex: number) => {
  updateDate()
  pageTableKey.value = pageKey
  tableListIndex.value = listIndex
  tableIndex.value = rowIndex
  tableSunIndex.value = colIndex
}

const onCustomTableCellClick = (pageKey: string, listIndex: number, rowIndex: number, colIndex: number) => {
  updateDate()
  customPageTableKey.value = pageKey
  customTableListIndex.value = listIndex
  customTableIndex.value = rowIndex
  customTableSunIndex.value = colIndex
}

const onAiTableCellBlur = (
  pageKey: string,
  listIndex: number,
  rowIndex: number,
  key: string,
  colIndex: number,
  event: FocusEvent
) => {
  const text = (event.target as HTMLElement | null)?.innerText ?? ''
  const row = editableTabs.value.aiDetails?.[pageKey]?.tables?.[listIndex]?.[rowIndex]
  if (!row) return
  row[key] = text
  pageTableKey.value = pageKey
  tableListIndex.value = listIndex
  tableIndex.value = rowIndex
  tableSunIndex.value = colIndex
}

const onCustomTableCellBlur = (
  pageKey: string,
  listIndex: number,
  rowIndex: number,
  key: string,
  colIndex: number,
  event: FocusEvent
) => {
  const text = (event.target as HTMLElement | null)?.innerText ?? ''
  const row = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]?.[rowIndex]
  if (!row) return
  row[key] = text
  customPageTableKey.value = pageKey
  customTableListIndex.value = listIndex
  customTableIndex.value = rowIndex
  customTableSunIndex.value = colIndex
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
    const { value } = await ElMessageBox.prompt('请输入字段名（Key）', '', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：InvoiceNo',
      inputValidator: (val: string) => {
        const key = val?.trim()
        if (!key) return 'Key 不能为空'
        if (key === 'tables') return 'Key 不能为 tables'
        if (existing.includes(key)) return 'Key 已存在'
        return true
      }
    })
    return (value as string).trim()
  } catch {
    return null
  }
}

const promptFieldValue = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入字段值（Value）', '', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '可为空，默认写入空字符串',
      inputValue: ''
    })
    return (value as string) ?? ''
  } catch {
    return null
  }
}

const ensureEditStateRow = (type: 'ai' | 'custom', pageKey: string) => {
  const details = type === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails
  const states = type === 'ai' ? contentEdit : customContentEdit
  const pageIndex = Object.keys(details).indexOf(pageKey)
  if (pageIndex < 0) return
  if (!states.value[pageIndex]) states.value[pageIndex] = []
}

/**
 * 新增一个 key-value 字段（非 tables）。
 * - type='ai'：写入 editableTabs.aiDetails
 * - type='custom'：写入 editableTabs.customDetails
 *
 * pageKey 为空时会自动使用当前选中的 pageKey（pageTableKey/customPageTableKey）。
 */
const addKeyValue = async (type: 'ai' | 'custom', pageKey?: string) => {
  updateDate()

  const resolvedPageKey = pageKey ?? (type === 'ai' ? pageTableKey.value : customPageTableKey.value)
  if (!resolvedPageKey) {
    ElMessage.warning('请先点击某一页的字段，选中页面后再新增')
    return
  }

  const details = type === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails
  const page = details?.[resolvedPageKey]
  if (!page || typeof page !== 'object') return

  const existingKeys = Object.keys(page).filter((k) => k !== 'tables')
  const newKey = await promptFieldKeyName(existingKeys)
  if (!newKey) return

  const newValue = await promptFieldValue()
  if (newValue === null) return

  // 保证 tables 始终在最后
  // @ts-ignore
  const { tables, ...rest } = page
  // @ts-ignore
  details[resolvedPageKey] = tables !== undefined ? { ...rest, [newKey]: newValue, tables } : { ...rest, [newKey]: newValue }

  // 同步 hover/edit 状态数组，否则模板里 contentEdit[pageIndex][index] 会错位
  ensureEditStateRow(type, resolvedPageKey)
  const states = type === 'ai' ? contentEdit : customContentEdit
  const pageIndex = Object.keys(details).indexOf(resolvedPageKey)
  if (pageIndex >= 0) {
    states.value[pageIndex].push({
      hover: true,
      status: false,
      copy: t('singleExtract.copy')
    })
  }

  if (type === 'ai') {
    textNum.value += 1
  } else {
    customTextNum.value += 1
  }
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

const addColumnToTable = async (table: any[]) => {
  ensureNonEmptyTable(table)
  const existingKeys = Object.keys(table[0] ?? {})
  const newKey = await promptColumnName(existingKeys)
  if (!newKey) return
  table.forEach((row: any) => {
    if (row && typeof row === 'object') row[newKey] = ''
  })
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

const addAiTableRow = async (pageKey: string, listIndex: number) => {
  updateDate()
  if (!contentTable.value[listIndex]) startAiTableEdit(pageKey, listIndex)
  const table = editableTabs.value.aiDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const insertAfter = tableListIndex.value === listIndex ? (tableIndex.value ?? null) : null
  addRowToTable(table, insertAfter)
  await nextTick()
  const newRowIndex = typeof insertAfter === 'number' ? insertAfter + 1 : table.length - 1
  onAiTableCellClick(pageKey, listIndex, newRowIndex, 0)
  focusTable(listIndex, pageKey, newRowIndex, 0, 'ai')
}

const addAiTableColumn = async (pageKey: string, listIndex: number) => {
  updateDate()
  if (!contentTable.value[listIndex]) startAiTableEdit(pageKey, listIndex)
  const table = editableTabs.value.aiDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  await addColumnToTable(table)
  await nextTick()
  onAiTableCellClick(pageKey, listIndex, tableIndex.value ?? 0, 0)
}

const deleteAiTableRow = async (pageKey: string, listIndex: number) => {
  updateDate()
  const table = editableTabs.value.aiDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const idx = tableListIndex.value === listIndex ? (tableIndex.value ?? 0) : 0
  deleteRowFromTable(table, idx)
  await nextTick()
  const nextRow = Math.min(idx, table.length - 1)
  onAiTableCellClick(pageKey, listIndex, nextRow, 0)
  focusTable(listIndex, pageKey, nextRow, 0, 'ai')
}

const deleteAiTableColumn = async (pageKey: string, listIndex: number) => {
  updateDate()
  const table = editableTabs.value.aiDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const idx = tableListIndex.value === listIndex ? (tableSunIndex.value ?? 0) : 0
  deleteColumnFromTable(table, idx)
  await nextTick()
  onAiTableCellClick(pageKey, listIndex, tableIndex.value ?? 0, 0)
}

const addCustomTableRow = async (pageKey: string, listIndex: number) => {
  updateDate()
  if (!customContentTable.value[listIndex]) startCustomTableEdit(pageKey, listIndex)
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const insertAfter = customTableListIndex.value === listIndex ? (customTableIndex.value ?? null) : null
  addRowToTable(table, insertAfter)
  await nextTick()
  const newRowIndex = typeof insertAfter === 'number' ? insertAfter + 1 : table.length - 1
  onCustomTableCellClick(pageKey, listIndex, newRowIndex, 0)
  focusTable(listIndex, pageKey, newRowIndex, 0, 'text')
}

const addCustomTableColumn = async (pageKey: string, listIndex: number) => {
  updateDate()
  if (!customContentTable.value[listIndex]) startCustomTableEdit(pageKey, listIndex)
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  await addColumnToTable(table)
  await nextTick()
  onCustomTableCellClick(pageKey, listIndex, customTableIndex.value ?? 0, 0)
}

const deleteCustomTableRow = async (pageKey: string, listIndex: number) => {
  updateDate()
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const idx = customTableListIndex.value === listIndex ? (customTableIndex.value ?? 0) : 0
  deleteRowFromTable(table, idx)
  await nextTick()
  const nextRow = Math.min(idx, table.length - 1)
  onCustomTableCellClick(pageKey, listIndex, nextRow, 0)
  focusTable(listIndex, pageKey, nextRow, 0, 'text')
}

const deleteCustomTableColumn = async (pageKey: string, listIndex: number) => {
  updateDate()
  const table = editableTabs.value.customDetails?.[pageKey]?.tables?.[listIndex]
  if (!table) return
  const idx = customTableListIndex.value === listIndex ? (customTableSunIndex.value ?? 0) : 0
  deleteColumnFromTable(table, idx)
  await nextTick()
  onCustomTableCellClick(pageKey, listIndex, customTableIndex.value ?? 0, 0)
}
const isNumber = (val: number) => typeof val === 'number'
const handleUpdate = (pageTableKey: Ref<string>, listIndex: Ref<number>, tableIndex: Ref<number>, sunIndex: Ref<number>): void => {
  if (isNumber(listIndex.value) && isNumber(tableIndex.value) && isNumber(sunIndex.value)) {
    changeAiTableDate(pageTableKey.value, listIndex.value, tableIndex.value, sunIndex.value);
  }
}
watch(dialogVisibleSetName, (val: boolean, _value: boolean) => {
  if (!val) {
    edit.value = false
  }
})
watch(pageTableKey, (_val: number, _value: number) => handleUpdate(pageTableKey, tableListIndex, tableIndex, tableSunIndex))
watch(tableListIndex, (_val: number, _value: number) => handleUpdate(pageTableKey, tableListIndex, tableIndex, tableSunIndex))
watch(tableIndex, (_val: number, _value: number) => handleUpdate(pageTableKey, tableListIndex, tableIndex, tableSunIndex))
watch(tableSunIndex, (_val: number, _value: number) => handleUpdate(pageTableKey, tableListIndex, tableIndex, tableSunIndex))
const changeAiTableDate = (pageTableKey: string, value: number, valueChi: number, valueSun: number) => {
  const dec = document.getElementById(`table${pageTableKey}${value}${valueChi}${valueSun}`)
  const row = editableTabs.value.aiDetails?.[pageTableKey]?.tables?.[value]?.[valueChi]
  if (!row || !dec) return
  const keys = Object.keys(row)
  const key = keys[valueSun]
  if (key !== undefined) row[key] = dec.innerText
}
const handleWatch = (customPageTableKey: Ref<string>, listIndex: Ref<number>, tableIndex: Ref<number>, sunIndex: Ref<number>): void => {
  if (isNumber(listIndex.value) && isNumber(tableIndex.value) && isNumber(sunIndex.value)) {
    changeCustomTableDate(customPageTableKey.value, listIndex.value, tableIndex.value, sunIndex.value)
  }
}
watch(customPageTableKey, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
watch(customTableListIndex, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
watch(customTableIndex, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
watch(customTableSunIndex, (_val: number, _value: number) => handleWatch(customPageTableKey, customTableListIndex, customTableIndex, customTableSunIndex))
const changeCustomTableDate = (customPageTableKey: string, value: number, valueChi: number, valueSun: number) => {
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
    const target = outputType.value === 'ai' ? contentEdit.value : customContentEdit.value
    target[pageIndex][index].copy = t('singleExtract.copied')
    if (timer.value) {
      clearTimeout(timer.value)
      timer.value = null
    }
    timer.value = setTimeout(() => {
      const target = outputType.value === 'ai' ? contentEdit.value : customContentEdit.value
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
const addField = ref(true)
const addTable = ref(true)
const tableFocus = ref(false)
const fieldFocus = ref(false)
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
const aiDownload = ref(true)
const customDownload = ref(true)
let docViewer: any = null
const inputField = ref()
const inputTable = ref()
const img = ref()
const picSrc = ref('')
const initFile = ref('one')
const fileType = ref('pdf')
const isWidthBigger = ref()
const jsonShow = ref('txt')
type TableItem = Record<string, any>

type PageData = {
  [key: string]: unknown
  tables?: TableItem[][]
}

type DetailsType = Record<string, PageData>
interface dataList {
  aiDetails: DetailsType,
  customDetails: DetailsType,
  tableList: Array<string>,
  fieldsList: Array<string>
}
const lang = ref('0')
const editableTabs = ref<dataList>({
  aiDetails: {},
  customDetails: {},
  tableList: [],
  fieldsList: []
})
const handleGlobalClick = () => {
  setting.value = false
  pullShow.value = false
  contentEdit.value.forEach((page: Array<contentEditType>) => {
    page.forEach((item: contentEditType) => {
      item.status = false
    })
  })
  updateDate()
}
const updateDate = () => {
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
    if (textIndex.value !== null) {
      const textId = `${textPrefix}${pageTableKey.value}${textIndex.value}`
      const dec = document.getElementById(textId)
      if (dec) {
        Object.keys(details[pageTableKey.value as keyof typeof details]).forEach((key, index) => {
          if (index === textIndex.value) {
            details[pageTableKey.value as keyof typeof details][key] = dec.innerText
          }
        })
      }
    }
  }
  // 判断类型并更新相应的内容
  if (outputType.value === 'ai') {
    updateDetails(editableTabs.value.aiDetails, 'table', 'text', tableListIndex, tableIndex, tableSunIndex, textIndex, pageTableKey)
  } else if (outputType.value === 'txt') {
    updateDetails(editableTabs.value.customDetails, 'customTable', 'customText', customTableListIndex, customTableIndex, customTableSunIndex, customTextIndex, customPageTableKey)
  }
}
const handleKeyDown = (event: any) => {
  if (event.keyCode === 9) {
    event.preventDefault()
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
  scale.value = Number((docViewer.scale * 100).toFixed(2))
}
const switchTool = () => {
  hand.value = hand.value ? 0 : 1
  docViewer.switchTool(hand.value)
}
const setScale = (val: number) => {
  scaleShow.value = false
  docViewer.webViewerScaleChanged(val / 100)
  scale.value = Number((docViewer.scale * 100).toFixed(2))
}
watch(() => lang, () => {
  upload(file.value as File)
  setting.value = false
})
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
  // 重置状态
  addField.value = false
  addTable.value = false

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

// ========== Document list API ==========

/**
 * Fetch the list of previously extracted documents
 */
const fetchDocumentList = async () => {
  isLoading.value = true
  docListError.value = ''
  try {
    const { data }: any = await get('/api/idp/getFileList?taskType=EXTRACTION&pageNum=1&pageSize=100')
    documentList.value = data?.data?.records || []
  } catch (err) {
    console.error('Failed to fetch document list:', err)
    docListError.value = t('common.networkError')
    documentList.value = []
  } finally {
    isLoading.value = false
  }
}

/**
 * Load a previously extracted document's results and preview
 */
const urlToFile = async (url: string, fileName: string, type: string): Promise<File> => {
  const res = await fetch(url)
  const blob = await res.blob()
  return new File([blob], fileName, { type: 'application/' + type })
}

const selectDocument = async (doc: ExtractionFileItem) => {
  activeDocId.value = doc.fileId
  dialogVisibleLoading.value = true
  noResult.value = false

  try {
    // Set file info from the selected document
    const nameArray = doc.fileName.split('.')
    downloadName.value = nameArray[0]
    fileType.value = nameArray[nameArray.length - 1].toLowerCase()

    // Load the preview (PDF or image)
    if (fileType.value === 'pdf') {
      if (doc.fileDownUrl) {
        UI.value?.loadDocument(doc.fileDownUrl)
      }
      picSrc.value = ''
    } else if (['jpg', 'png', 'jpeg'].includes(fileType.value)) {
      picSrc.value = doc.fileDownUrl || ''
    }

    // Fetch extraction result JSON
    const response = await fetch(doc.resultDownUrl, {
      headers: { 'Accept': 'application/json' }
    })
    if (!response.ok) throw new Error('Failed to load result')
    const jsonData = await response.json()

    // Normalize and reorder details (same pattern as processResponse)
    const details = jsonData?.data?.details || jsonData?.details || jsonData || {}
    const reorderedDetails: Record<string, any> = {}
    Object.entries(details).forEach(([pageKey, pageValue]) => {
      if (pageValue && typeof pageValue === 'object' && !Array.isArray(pageValue)) {
        // @ts-ignore
        const { tables, ...rest } = pageValue
        reorderedDetails[pageKey] = tables !== undefined
          ? { ...rest, tables }
          : { ...rest }
      } else {
        reorderedDetails[pageKey] = pageValue
      }
    })

    if (Object.keys(reorderedDetails).length) {
      // Clear existing state
      contentEdit.value = []
      contentTable.value = []
      customContentEdit.value = []
      customContentTable.value = []
      pageShow.value = []
      customPageShow.value = []

      if (['ai', 'json'].includes(outputType.value)) {
        editableTabs.value.aiDetails = reorderedDetails
        let i = 0; let j = 0
        Object.keys(reorderedDetails).forEach((key: string, pageIndex: number) => {
          const pageData = reorderedDetails[key]
          contentEdit.value.push([])
          pageShow.value.push(true)
          Object.keys(pageData).forEach((item: string) => {
            if (item !== 'tables') {
              contentEdit.value[pageIndex].push({ hover: true, status: false, copy: t('singleExtract.copy') })
              i++
            } else if (Array.isArray(pageData[item])) {
              pageData.tables?.forEach((items: []) => {
                if (items.length) { j++; contentTable.value.push(false) }
              })
            }
          })
        })
        textNum.value = i
        tableNum.value = j
        aiDownload.value = false
      } else {
        editableTabs.value.customDetails = reorderedDetails
        let i = 0; let j = 0
        Object.keys(reorderedDetails).forEach((key: string, pageIndex: number) => {
          const pageData = reorderedDetails[key]
          customContentEdit.value.push([])
          customPageShow.value.push(true)
          Object.keys(pageData).forEach((item: string) => {
            if (item !== 'tables') {
              customContentEdit.value[pageIndex].push({ hover: true, status: false, copy: t('singleExtract.copy') })
              i++
            } else if (Array.isArray(pageData[item])) {
              pageData.tables?.forEach((items: []) => {
                if (items.length) { j++; customContentTable.value.push(false) }
              })
            }
          })
        })
        customTextNum.value = i
        customTableNum.value = j
        customDownload.value = false
      }
    }

    saveShow.value = false
    init.value = false
    customInit.value = false
    firstExtract.value = false

  } catch (err) {
    console.error('Failed to load document:', err)
    noResult.value = true
    init.value = false
    customInit.value = false
    ElMessage.error(t('singleExtract.error'))
  } finally {
    dialogVisibleLoading.value = false
  }
}

onMounted(async () => {
  showBtn.value = true
  getTemplate()
  fetchDocumentList()
  addEventListener('click', handleGlobalClick)
  const { data: { data: first } } =  await get('/api/idp/is-first-extract')
  if (first === true) {
    schemaShow.value = true
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
    const license = getEnv('LICENSE_KEY')
    ComPDFKitViewer.init({
      license: 'NjhlY2JlZTliMzAxNQ==',
      pdfUrl: '/pdf/invoice.pdf',
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
      const scrollViewer = docViewer.getScrollViewElement()
      scrollViewer.addEventListener('scroll', (e: Event) => {
        const target = e.target as HTMLElement

        // 获取滚动距离
        const scrollTop = target.scrollTop || 0

        nextTick(() => resultContent.value.scrollTop = scrollTop)
      })
      docViewer.addEvent('documentloaded', async () => {
        navShow.value = true
        pdfPage.value = docViewer.pagesCount
        scale.value = Number((docViewer.scale * 100).toFixed(2))
      })
      type scaleType = {
        previous: number
        scale: number
      }
      docViewer.addEvent('scalechanging', (data: scaleType) => {
        scale.value = Number((data.scale * 100).toFixed(2))
      })
    })
    fetch('/pdf/invoice.pdf')
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok')
      }
      return response.blob()
    })
    .then(blob => {
      const files = new File([blob], 'invoice.pdf', { type: 'application/json' })
      file.value = files
    })
    .catch(error => {
      console.error(error)
    })
  }
})
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
const shouldSkipProcessing = computed(() => {
  if (outputType.value === 'ai') {
    return !(textNum.value + tableNum.value)
  }
  if (outputType.value === 'txt') {
    return !(customTextNum.value + customTableNum.value)
  }
  return false
})
const openDialog = (val: string) => {
  if (shouldSkipProcessing.value) return
  if (outputType.value === 'ai') {
    if (aiDownload.value) return
  } else if (outputType.value === 'txt') {
    if (customDownload.value) return
  }
  if (outputType.value === 'json') {
    const blogContent = JSON.stringify(jsonShow.value === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails, null, 2)
    const blob = new Blob([blogContent], { type: 'application/json' })
    const blobUrl = URL.createObjectURL(blob)
    downloadClick(blobUrl, downloadName.value + '.' + toType.value)
  } else {
    dialogVisible.value = true
    if (val === 'table' || resultType.value === 'table') {
      toType.value = 'xlsx'
      tableDownload.value = true
    } else {
      tableListIndex.value = null
      customTableListIndex.value = null
      tableDownload.value = false
      resultType.value === 'table' ? toType.value = 'xlsx' : toType.value = 'json'
    }
  }
}
const onImageLoad = () => {
  isWidthBigger.value = img.value.naturalWidth > img.value.naturalHeight
}
// 切换显示结果
const changeConvert = (val: string) => {
  if (dialogVisibleLoading.value) return
  if (outputType.value === val) return
  outputType.value = val
  if (val === 'ai') {
    jsonShow.value = 'ai'
  } else if (val === 'txt') {
    jsonShow.value = 'txt'
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
  aiDownload.value = true
  customDownload.value = true
  textIndex.value = null
  tableIndex.value = null
  tableListIndex.value = null
  firstExtract.value = true
  tableSunIndex.value = null
  customTextIndex.value = null
  customTableIndex.value = null
  customTableListIndex.value = null
  customTableSunIndex.value = null
  textNum.value = 0
  tableNum.value = 0
  customTextNum.value = 0
  customTableNum.value = 0
  contentEdit.value = []
  contentTable.value = []
  customContentEdit.value = []
  customContentTable.value = []
  editableTabs.value.aiDetails = {}
  editableTabs.value.customDetails = {}
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
  }
  file.value = postFile
}
// 点击开始提取
const uploadClick = () => {
  if (!file.value) return
  saveShow.value = false
  init.value = false
  customInit.value = false
  guide.value = false
  if (dialogVisibleLoading.value) return
  if ((editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) || ['ai', 'json'].includes(outputType.value)) {
    upload(file.value)
  } else {
    ElMessage({
      message: t('bulkExtract.fieldEmptyTip'),
      type: 'error',
      duration: 2000,
      customClass: 'upload'
    })
  }
}
// 处理请求数据
const upload = (rawFile: File) => {
  const data = new FormData()
  data.append('file', rawFile)
  data.append('lang', lang.value)
  data.append('keys', outputType.value === 'ai' ? '' : editableTabs.value.fieldsList as any)
  data.append('tableHandles', outputType.value === 'ai' ? '' : editableTabs.value.tableList as any)
  if (outputType.value === 'txt') {
    data.append('keysDescribe', templateList.value[activeTemplate.value].keysTip as any)
    data.append('tableHandlesDescribe', templateList.value[activeTemplate.value].tableHandlesTip as any)
  }
  fileUpload(data)
}
// 数据提取
const fileUpload = async (data: FormData) => {
  if (outputType.value === 'ai') {
    editableTabs.value.aiDetails = {}
    textNum.value = 0
    tableNum.value = 0
  } else {
    customTextNum.value = 0
    customTableNum.value = 0
    editableTabs.value.customDetails = {}
  }
  dialogVisibleLoading.value = true
  post('/api/idp/data-extract', data, {}, {
    responseType: 'blob',
    timeout: 3600000
  } as any).then(async (res: any) => {
    if (outputType.value === 'ai') {
      aiDownload.value = false
    } else if (outputType.value === 'txt') {
      customDownload.value = false
    }
    firstExtract.value = false
    processResponse(res)
    // Refresh document list after successful upload
    try {
      await fetchDocumentList()
      if (documentList.value.length > 0) {
        activeDocId.value = documentList.value[0].fileId
      }
    } catch {
      // Non-critical - sidebar will show stale data but upload succeeded
    }
  }).catch((err: any) => {
    handleError(err)
    firstExtract.value = false
  })
}
// 处理相应
const processResponse = (res: any) => {
  dialogVisibleLoading.value = false
  const reader = new FileReader()
  reader.readAsText(res.data, 'utf-8')
  reader.onload = () => {
    if (typeof reader.result === 'string') {
      let data = JSON.parse(reader.result)
      if (data.code === 8005) {
        ElMessage.error(t('singleExtract.error'))
        return
      }
      data = data.data
      const reorderedDetails: Record<string, any> = {}
      Object.entries(data.details).forEach(([pageKey, pageValue]) => {
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
        if (['ai', 'json'].includes(outputType.value)) {
          editableTabs.value.aiDetails = reorderedDetails
          let i = 0
          let j = 0
          Object.keys(reorderedDetails).forEach((key: string, pageIndex: number) => {
            const pageData = reorderedDetails[key]
            contentEdit.value.push([])
            pageShow.value.push(true)
            Object.keys(pageData).forEach((item: string) => {
              if (item !== 'tables') {
                contentEdit.value[pageIndex].push({
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
                      contentTable.value.push(false)
                    }
                  })
                }
              }
            })
          })
          textNum.value = i
          tableNum.value = j
        } else if (outputType.value === 'txt') {
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
        }
      } else {
        ElMessage({
          message: 'Failed',
          type: 'error',
          duration: 2000,
          customClass: 'upload'
        })
      }
    } else {
      console.error('Reader result is not a string.')
    }
  }
}
// 处理错误相应
const handleError = (err: any) => {
  dialogVisibleLoading.value = false
  if (err && err.request.responseType === 'blob') {
    const reader = new FileReader()
    reader.readAsText(err.data, 'utf-8')
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        ElMessage({
          message: 'Failed to convert.',
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
}
// 生成对应 Blob 对象
const downloadFile = () => {
  dialogVisible.value = false
  let blogContent = null
  const data = outputType.value === 'ai' ? editableTabs.value.aiDetails : editableTabs.value.customDetails
  if (resultType.value === 'text') {
    const obj = JSON.parse(JSON.stringify(data))
    Object.keys(obj).forEach((item: any) => {
      if (obj[item]?.tables?.length) {
        delete obj[item].tables
      }
    })
    blogContent = obj
  } else if (resultType.value === 'table') {
    blogContent = data.tables
  } else {
    blogContent = data
  }
  const blobContentMap = {
    json: 'application/json',
    text: 'text/plain;charset=utf-8;'
  }
  if (['txt', 'json'].includes(toType.value)) {
    blogContent = JSON.stringify(blogContent, null, 2)
  }
  if (['json', 'txt'].includes(toType.value)) {
    const blob = new Blob([blogContent], { type: blobContentMap[toType.value as keyof typeof blobContentMap] })
    const blobUrl = URL.createObjectURL(blob)
    downloadClick(blobUrl, downloadName.value + '.' + toType.value)
  } else if (toType.value === 'csv') {
    tableDownload.value ? downloadSingleCsv() : downloadCSV()
  } else if (toType.value === 'xlsx') {
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
  return templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) && Object.keys(templateList).length <= 7 && !dialogVisibleLoading.value ? '' : 'disable'
})
const saveAsClick = () => {
  if (templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) && Object.keys(templateList).length <= 7 && !dialogVisibleLoading.value) {
   dialogVisibleSetName.value = true
  }
}
const saveConfigurationClass = computed(() => {
  return templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) && !dialogVisibleLoading.value ? '' : 'disable'
})
const saveConfigurationClick = () => {
  templateName.value = activeTemplate.value
  if (templateList.value[activeTemplate.value]?.canSave && (editableTabs.value.fieldsList.length || editableTabs.value.tableList.length) && !dialogVisibleLoading.value) {
    templateList.value[activeTemplate.value].addTemplate ? saveTemplate('create') : saveTemplate('edit')
  }
}
</script>

<style lang="scss" scoped>
.font {
  font-family: Helvetica;
  font-weight: 600;
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

@keyframes spin {
  0% {
    rotate: 0deg;
  }

  100% {
    rotate: 360deg;
  }
}

.active {
  stroke: var(--active);
  stroke-linecap: round;
  stroke-dashoffset: 360;
  animation: active-animation var(--duration) ease-in-out infinite;
}

@keyframes active-animation {
  0% {
    stroke-dasharray: 0 0 0 360 0 360;
  }
  12.5% {
    stroke-dasharray: 0 0 270 90 270 90;
  }
  25% {
    stroke-dasharray: 0 270 0 360 0 360;
  }
  37.5% {
    stroke-dasharray: 0 270 270 90 270 90;
  }
  50% {
    stroke-dasharray: 0 540 0 360 0 360;
  }
  50.001% {
    stroke-dasharray: 0 180 0 360 0 360;
  }
  62.5% {
    stroke-dasharray: 0 180 270 90 270 90;
  }
  75% {
    stroke-dasharray: 0 450 0 360 0 360;
  }
  87.5% {
    stroke-dasharray: 0 450 270 90 270 90;
  }
  87.501% {
    stroke-dasharray: 0 90 270 90 270 90;
  }
  100% {
    stroke-dasharray: 0 360 1 360 0 360;
  }
}

.track {
  stroke: var(--track);
  stroke-linecap: round;
  stroke-dashoffset: 360;
  animation: track-animation var(--duration) ease-in-out infinite;
}

@keyframes track-animation {
  0% {
    stroke-dasharray: 0 20 320 40 320 40;
  }
  12.5% {
    stroke-dasharray: 0 290 50 310 50 310;
  }
  25% {
    stroke-dasharray: 0 290 320 40 320 40;
  }
  37.5% {
    stroke-dasharray: 0 560 50 310 50 310;
  }
  37.501% {
    stroke-dasharray: 0 200 50 310 50 310;
  }
  50% {
    stroke-dasharray: 0 200 320 40 320 40;
  }
  62.5% {
    stroke-dasharray: 0 470 50 310 50 310;
  }
  62.501% {
    stroke-dasharray: 0 110 50 310 50 310;
  }
  75% {
    stroke-dasharray: 0 110 320 40 320 40;
  }
  87.5% {
    stroke-dasharray: 0 380 50 310 50 310;
  }
  100% {
    stroke-dasharray: 0 380 320 40 320 40;
  }
}
/* From Uiverse.io by ilkhoeri */ 
.button {
  width: 162px;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.25s ease;
  background: radial-gradient(
      65.28% 65.28% at 50% 100%,
      #7199FF,
      rgba(223, 113, 255, 0) 100%
    ),
    linear-gradient(0deg, #396FFA, #396FFA);
  border-radius: 2px;
  border: none;
  outline: none;
  padding: 8.65px 10px;
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
.w-100vw.absolute {
  background: rgba(0,0,0,0.5);
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
  .inline-block.absolute.copy {
    .tip {
      box-shadow: 0px 4px 4px 0px #00000033;
    }
    &:hover .tip {
      display: inline-block;
    }
  }
  &:hover {
    .editContent {
      color: #396FFA;
      background-color: #396FFA33;
    }
    .edit, .copy {
      display: inline-block;
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

.extract {
  overflow: auto;
  padding-top: 16px;
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