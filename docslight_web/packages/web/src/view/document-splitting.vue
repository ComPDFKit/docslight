<template>
  <div @click="rangeMenuVisible = false" class="document-splitting flex flex-col">
    <h1 class="py-24px px-32px text-20px leading-32px font-500 border-b border-[#E1E3E8]">{{ t('splitting.title') }}</h1>
    <div class="bg-[#F3F6FF] p-32px min-h-[calc(100vh-81px)]">
      <h2 class="text-20px leading-28px font-600 mb-20px">{{ t('splitting.list') }}</h2>
      <div class="flex items-center justify-between mb-20px">
        <el-input class="max-w-300px" v-model="searchQuery" clearable @clear="getTableData" @keyup.enter="getTableData" :placeholder="t('splitting.search')">
          <template #prefix>
            <Search />
          </template>
        </el-input>
        <div class="flex items-center">
          <div v-permission="'split:delete'" v-show="selectFilesList.length > 0" class="w-fit rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-16px mx-12px flex items-center justify-center hover:(bg-[#396FFA] text-white)" @click="deleteFile([])">
            <BatchDelete class="mr-4px" />
            {{ t('splitting.batchDelete') }}
          </div>
          <div v-permission="'split:split'" v-show="selectFilesList.length > 0" class="w-fit rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-16px mx-12px flex items-center justify-center hover:(bg-[#396FFA] text-white)" @click="dialogVisible = true">
            <BatchStart class="mr-4px" />
            {{ t('splitting.batchStart') }}
          </div>
          <div v-permission="'split:upload'"  @click="uploadDialogVisible = true" class="ml-12px whitespace-nowrap rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center justify-center font-500 hover:bg-[#244FF0]">
            <Upload class="mr-4px" />
            {{ t('splitting.upload') }}
          </div>
        </div>
      </div>
      <div class="bg-white shadows">
        <el-table v-loading="loading" ref="tableRef" :data="dataList" @selection-change="handleSelectionChange" :row-key="rowKey">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column :label="t('splitting.fileName')" align="left" min-width="180px" show-overflow-tooltip>
            <template #default="scope">
              <div @click.stop="preview(scope.row)" class="flex items-center cursor-pointer">
                <Document class="min-w-20px mr-4px" />
                <div class="truncate underline text-brand-2">{{ scope.row.fileName }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="format" :label="t('splitting.format')" align="center" width="160px">
            <template #default="scope">
              <div class="flex items-center justify-center">
                <div class="truncate">{{ getFileExtension(scope.row.fileName) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="pageCount" :label="t('splitting.pageCount')" align="center" width="160px" />
          <el-table-column prop="update_time" :label="t('splitting.time')" align="center" width="160px">
            <template #default="scope">
              <div class="flex justify-start whitespace-nowrap">{{ dayjs.utc(scope.row.uploadTime).local().format('DD/MM/YYYY HH:mm:ss') }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="t('splitting.action')" align="center" width="150px">
            <template #default="scope">
              <div class="flex items-center justify-center">
                <div @click="handleSelectFile(scope.row)" class="text-brand-2 text-12px leading-16px mr-12px cursor-pointer">
                  {{ t('splitting.split') }}
                </div>
                <el-tooltip popper-class="box-item" effect="dark" :content="t('splitting.delete')" placement="top">
                  <Delete @click="deleteFile([scope.row.fileId])" class="cursor-pointer svg" />
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <div class="w-full h-[calc(100vh-399px)] flex flex-col justify-center items-center">
              <img src="/images/kbEmpty.png" width="120" height="120" alt="Empty">
              <div class="text-sm text-brand-3 mt-8px mb-32px max-w-600px text-center">
                {{ t('splitting.noDesc') }}
              </div>
              <div @click="uploadDialogVisible = true" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px w-fit flex items-center justify-center font-500 hover:bg-[#244FF0]">
                <Upload class="mr-4px" />
                {{ t('splitting.upload') }}
              </div>
            </div>
          </template>
        </el-table>
        <el-pagination
          background
          :total="total"
          :page-size="pageSize"
          :page-sizes="pageSizes"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          layout="total, prev, pager, next, sizes, jumper"
        />
      </div>
      <!-- 文件拆分设置 -->
      <el-dialog v-model="dialogVisible" align-center width="520px">
        <h3 class="text-sm font-600 text-[#404653] mb-12px">
          {{ t('splitting.split') }}
        </h3>
        <div class="text-14px leading-20px font-500 text-[#404653] mt-8px py-10px">
          {{ t('splitting.method') }}
        </div>
        <el-radio-group v-model="splitMethod">
          <el-radio value="every_n_pages">
            {{ t('splitting.page[0]') }}
            <el-input type="number" :disabled="splitMethod !== 'every_n_pages'" v-model="evert" min="1" @input="changeNumber" @change="changeNumber"></el-input>
            <span class="text-[#B7BABF]">
              {{ t('splitting.page[1]') }}
            </span>
          </el-radio>
          <el-radio value="averagely">
            {{ t('splitting.averagely[0]') }}
            <el-input type="number" :disabled="splitMethod !== 'averagely'" v-model="averagely" min="1" @input="changeNumber" @change="changeNumber"></el-input>
            <span class="text-[#B7BABF]">
              {{ t('splitting.averagely[1]') }}
            </span>
          </el-radio>
          <el-radio value="ranges">
            {{ t('splitting.range') }}
            <div class="relative flex-1 ml-8px" @click.stop>
              <template v-if="rangeMode !== 'ranges'">
                <div 
                  class="min-h-32px px-12px py-6px rounded-4px border border-[#DCDFE6] cursor-pointer flex items-center justify-between hover:border-[#396FFA]"
                  @click="rangeMenuVisible = !rangeMenuVisible"
                >
                  <span class="text-14px text-[#606266]">
                    {{ rangeMode === 'odd' ? t('splitting.odd') : rangeMode === 'even' ? t('splitting.even') : 'e.g. 1,2 - 5,10' }}
                  </span>
                  <svg class="w-12px h-12px transition-transform" :class="rangeMenuVisible && 'rotate-180'" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <path fill="currentColor" d="M831.872 340.864 512 652.672 192.128 340.864a30.592 30.592 0 0 0-42.752 0 29.12 29.12 0 0 0 0 41.6L489.664 714.24a32 32 0 0 0 44.672 0l340.288-331.712a29.12 29.12 0 0 0 0-41.728 30.592 30.592 0 0 0-42.752 0z"></path>
                  </svg>
                </div>
              </template>
              <template v-else>
                <div class="relative flex items-center">
                  <el-input
                    v-model="rangeCustomInput"
                    placeholder="e.g. 1,2 - 5,10"
                    @input="rangeMenuVisible = false"
                    class="flex-1 !mx-0"
                  />
                  <div 
                    class="absolute right-12px cursor-pointer flex items-center justify-center"
                    @click.stop="rangeMenuVisible = !rangeMenuVisible"
                  >
                    <svg class="w-16px h-16px transition-transform text-[#A8ABB2]" :class="rangeMenuVisible && 'rotate-180'" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                      <path fill="currentColor" d="M831.872 340.864 512 652.672 192.128 340.864a30.592 30.592 0 0 0-42.752 0 29.12 29.12 0 0 0 0 41.6L489.664 714.24a32 32 0 0 0 44.672 0l340.288-331.712a29.12 29.12 0 0 0 0-41.728 30.592 30.592 0 0 0-42.752 0z"></path>
                    </svg>
                  </div>
                </div>
              </template>
              <div 
                v-show="rangeMenuVisible"
                class="absolute left-0px top-[calc(100%+4px)] w-full bg-white rounded-4px shadow-lg border border-[#E4E7ED] py-4px z-10"
              >
                <div 
                  class="px-12px py-6px cursor-pointer text-14px text-[#606266] hover:bg-[#F3F6FF] hover:text-[#396FFA]"
                  :class="rangeMode === 'odd' && 'text-[#396FFA] bg-[#F3F6FF]'"
                  @click="onRangeModeChange('odd')"
                >
                  {{ t('splitting.odd') }}
                </div>
                <div 
                  class="px-12px py-6px cursor-pointer text-14px text-[#606266] hover:bg-[#F3F6FF] hover:text-[#396FFA]"
                  :class="rangeMode === 'even' && 'text-[#396FFA] bg-[#F3F6FF]'"
                  @click="onRangeModeChange('even')"
                >
                  {{ t('splitting.even') }}
                </div>
                <div 
                  class="px-12px py-6px cursor-pointer text-14px text-[#606266] hover:bg-[#F3F6FF] hover:text-[#396FFA]"
                  :class="rangeMode === 'ranges' && 'text-[#396FFA] bg-[#F3F6FF]'"
                  @click="onRangeModeChange('ranges')"
                >
                  e.g. 1,2 - 5,10
                </div>
              </div>
            </div>
          </el-radio>
        </el-radio-group>
        <div class="text-14px leading-20px font-500 text-[#404653] mt-16px py-10px">
          {{ t('splitting.fileName') }}
        </div>
        <div v-if="selectFile" class="w-full truncate rounded-4px mb-8px text-14px leading-20px py-6px px-12px border border-[#B7BABF] bg-[#F6F6FB] text-[#0C131F]">
          {{ selectFile?.fileName }}
        </div>
        <template v-else>
          <div v-for="(item, index) in selectFilesList" :key="index" class="w-full truncate rounded-4px mb-8px text-14px leading-20px py-6px px-12px border border-[#B7BABF] bg-[#F6F6FB] text-[#0C131F]">
            {{ item?.fileName }}
          </div>
        </template>
        <el-checkbox-group v-model="splitMethods">
          <el-checkbox :label="t('splitting.keep')" value="keep" />
          <el-checkbox :label="t('splitting.label')" value="label">
            {{ t('splitting.label') }}
            <el-input v-model="label"></el-input>
          </el-checkbox>
          <el-checkbox :label="t('splitting.separator')" value="separator">
            {{ t('splitting.separator') }}
            <el-input v-model="separator"></el-input>
          </el-checkbox>
        </el-checkbox-group>
        <div class="flex justify-end mt-24px">
          <div class="w-fit rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])" @click="dialogVisible = false">
            {{ t('splitting.cancel') }}
          </div>
          <div v-loading="loading" @click="startSplit" class="w-fit rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px ml-12px flex items-center justify-center font-500 hover:bg-[#244FF0]">
            {{ t('splitting.split') }}
          </div>
        </div>
      </el-dialog>
      <!-- 上传文件 -->
      <el-dialog v-model="uploadDialogVisible" align-center width="520px" :show-close="false">
        <!-- Title -->
        <h3 class="text-sm font-600 text-[#43474D] py-4px mb-16px">
          {{ t('splitting.upload') }}
        </h3>

        <!-- Tabs -->
        <div class="flex border-b border-[#E2E3E5] mb-16px">
          <div
            v-for="(tab, index) in uploadTabs"
            :key="tab.key"
            @click="uploadActiveTab = tab.key"
            class="mr-38px pb-8px text-sm cursor-pointer flex items-center"
            :class="[uploadActiveTab === tab.key
              ? 'text-[#396FFA] font-600 border-b-2 border-[#396FFA]'
              : 'text-[#404653] font-400 hover:text-[#43474D]', index === 2 && 'mr-0px']"
          >
            <component :is="tab.icon" class="min-w-20px mr-8px" />
            {{ tab.label }}
          </div>
        </div>

        <!-- ── LOCAL TAB ── -->
        <template v-if="uploadActiveTab === 'local'">
          <div
            @drop.prevent="onDrop"
            @dragover.prevent="dragover = true"
            @dragleave.prevent="leave"
            class="border border-[1.5px] border-dashed border-[#396FFA] rounded-10px min-h-329px flex flex-col items-center justify-center transition-colors relative"
            :class="dragover ? 'bg-[#F3F6FF]' : 'bg-white'"
          >
            <div
              v-loading="loading"
              @click="input?.click()"
              class="w-fit rounded-6px cursor-pointer border-1 border-[#396FFA] text-[#396FFA] text-sm font-500 py-8px px-16px flex items-center justify-center hover:(bg-[#EEF3FF]) active:(bg-[#D6E0FF])"
            >
              <Upload class="mr-4px" />
              {{ t('splitting.selectFile[0]') }}
            </div>
            <div class="mt-12px text-xs text-[#8C8C8C]">{{ t('splitting.selectFile[1]') }}</div>
            <div class="text-xs text-[#8C8C8C]">{{ t('splitting.selectFile[2]') }}</div>
            <!-- Supported formats bar -->
            <div class="mt-12px rounded-6px bg-[#F6F6FB] px-12px py-8px text-xs text-[#8C8C8C] absolute bottom-0px left-0 rounded-10px w-full text-center">
              {{ t('splitting.selectFile[2]') }}
            </div>
          </div>
          <input ref="input" class="hidden" type="file" accept=".pdf" name="file" multiple @change="handleChange">

          <!-- Selected files panel -->
          <template v-if="fileList.length">
            <div class="flex justify-between items-center mt-24px mb-12px">
              <span class="text-xs text-[#52555F] font-500">{{ t('dms.team_space.upload.common.selected_count') }} ({{ fileList.length }})</span>
              <span @click="fileList = []" class="text-xs text-[#2E59CA] cursor-pointer hover:underline">
                {{ t('dms.team_space.upload.common.clear') }}
              </span>
            </div>
            <div class="bg-[#F6F6FB] rounded-6px px-12px py-8px max-h-154px overflow-auto">
              <div v-for="(file, index) in fileList" :key="index" :class="index && 'mt-12px'" class="flex justify-between items-center">
                <div class="flex items-center min-w-0">
                  <div class="truncate text-sm text-[#2E59CA]">{{ file.name }}</div>
                </div>
                <DeleteFile @click="deleteUploadFile(index)" class="cursor-pointer min-w-16px flex-shrink-0 ml-8px" />
              </div>
            </div>
          </template>
        </template>

        <!-- ── TEAM SPACE TAB ── -->
        <template v-else-if="uploadActiveTab === 'team'">
          <div class="min-h-329px flex flex-col">
            <div class="flex-1 flex flex-col border border-[#E2E3E5] rounded-6px p-12px overflow-auto max-h-329px">
              <div v-if="folderList.length === 0" class="flex-1 flex flex-col h-full flex items-center justify-center text-xs text-[#8C8C8C]">
                <img src="/images/kbEmpty.png" width="64" height="64" alt="Empty" class="mb-8px">
                {{ t('dms.scanner_inbox.assign_to_team_space.target_folder.empty') }}
              </div>
              <template v-else>
                <div v-for="node in folderList" :key="node.id" class="mb-4px">
                  <div class="flex-1 flex items-center justify-between cursor-pointer py-4px px-6px rounded hover:bg-[#F6F6FB]" @click="toggleTeamFolder(node)">
                    <div class="flex items-center min-w-0 flex-1">
                      <FileArrow
                        class="shrink-0 transition-transform duration-200 min-w-16px"
                        :style="{ transform: node.expanded ? 'rotate(0deg)' : 'rotate(-90deg)' }"
                      />
                      <DocFolder class="text-[#888C94] mx-4px shrink-0 min-w-20px" />
                      <span class="text-sm text-[#43474D] truncate">{{ node.name }}</span>
                    </div>
                    <Checked @click.stop="toggleTeamCheck(node)" v-if="node.checked" class="min-w-16px max-w-16px h-16px" />
                    <Indeterminate @click.stop="toggleTeamCheck(node)" v-else-if="node.indeterminate" class="min-w-16px max-w-16px h-16px" />
                    <Check @click.stop="toggleTeamCheck(node)" v-else class="min-w-16px max-w-16px h-16px" />
                  </div>
                  <template v-if="node.expanded && node.children">
                    <div @click="toggleTeamChildCheck(node, child)" v-for="child in node.children" :key="child.id" class="ml-24px flex items-center justify-between cursor-pointer py-4px px-6px rounded hover:bg-[#F6F6FB]">
                      <div class="flex items-center min-w-0 flex-1">
                        <Docs class="text-[#888C94] mx-4px shrink-0 min-w-20px" />
                        <span class="text-sm text-[#43474D] truncate">{{ child.name }}</span>
                      </div>
                      <Checked v-if="child.checked" class="min-w-16px max-w-16px h-16px" />
                      <Check v-else class="min-w-16px max-w-16px h-16px" />
                    </div>
                  </template>
                </div>
              </template>
            </div>
            <template v-if="teamSelectedFiles.length">
              <div class="flex justify-between items-center mt-24px mb-12px">
                <span class="text-xs text-[#52555F] font-500">{{ t('dms.team_space.upload.common.selected_count') }} ({{ teamSelectedFiles.length }})</span>
                <span @click="clearTeamSelectedFiles" class="text-xs text-[#2E59CA] cursor-pointer hover:underline">
                  {{ t('dms.team_space.upload.common.clear') }}
                </span>
              </div>
              <div class="bg-[#F6F6FB] rounded-6px px-12px py-8px max-h-154px overflow-auto">
                <div v-for="(file, index) in teamSelectedFiles" :key="file.id" :class="index && 'mt-12px'" class="flex justify-between items-center">
                  <div class="flex items-center min-w-0">
                    <div class="truncate text-sm text-[#2E59CA]">{{ file.name }}</div>
                  </div>
                  <DeleteFile @click="removeTeamSelectedFile(index)" class="cursor-pointer min-w-16px flex-shrink-0 ml-8px" />
                </div>
              </div>
            </template>
          </div>
        </template>

        <!-- ── THIRD-PARTY TAB ── -->
        <template v-else-if="uploadActiveTab === 'thirdParty'">
          <div class="min-h-329px max-h-480px flex overflow-hidden">
            <!-- Left sidebar: platform list -->
            <div class="w-120px flex flex-col py-8px bg-white mr-8px shrink-0">
              <div
                v-for="(platform, index) in thirdPartyPlatforms"
                :key="platform.key"
                @click="selectedPlatform = platform.key"
                class="flex whitespace-nowrap items-center p-4px rounded-8px cursor-pointer text-12px leading-16px"
                :class="[selectedPlatform === platform.key ? 'bg-[#EEF3FF] text-[#396FFA]' : 'text-[#43474D] hover:text-[#396FFA]', index && 'mt-8px']"
              >
                <component :is="platform.icon" class="min-w-20px mr-4px" />
                <span class="text-center leading-tight">{{ platform.name }}</span>
              </div>
            </div>
            <!-- Right content -->
            <div class="flex-1 flex flex-col min-w-0 overflow-y-auto">
              <template v-if="!thirdPartyAuthorized">
                <div class="border border-[#E2E3E5] rounded-6px flex-1 flex flex-col items-center justify-center p-12px">
                  <img src="/images/unAuthorization.png" alt="UnAuthorization" width="64" height="64">
                  <div class="text-sm font-600 text-[#43474D] mt-12px mb-8px">{{ t('dms.team_space.upload.third_party.authorization_required.title') }}</div>
                  <div class="text-xs text-[#8C8C8C] text-center mb-16px">
                    {{ t('dms.team_space.upload.third_party.authorization_required.description') }}
                  </div>
                  <div
                    v-if="store.role === 'manager'"
                    @click="authorizeThirdParty"
                    class="rounded-6px bg-[#396FFA] text-white text-sm font-500 py-8px px-16px cursor-pointer hover:bg-[#244FF0]"
                  >
                    {{ t('dms.team_space.upload.third_party.authorization_required.authorize_now') }}
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="flex-1 flex flex-col border border-[#E2E3E5] rounded-6px overflow-auto max-h-329px">
                  <div v-if="thirdPartyDialogLoading" v-loading="true" class="flex-1 min-h-100px" />
                  <div v-else class="w-full flex flex-col">
                    <div
                      v-for="(file, index) in thirdPartyDialogFileList"
                      :key="`${getDmsItemKey(file)}-${index}`"
                      class="flex items-center justify-between py-4px pr-8px rounded hover:bg-[#F6F6FB]"
                      :class="[getThirdPartyCheckState(file) !== 'unchecked' ? 'bg-[#d7e2fe]' : '']"
                    >
                      <div
                        @click="openThirdPartyDialogFolder(file, index)"
                        class="flex items-center flex-1 min-w-0"
                        :class="[file.is_expandable && 'cursor-pointer']"
                        :style="{ paddingLeft: `${file.level * 20 + 8}px` }"
                      >
                        <FileArrow
                          v-if="file.is_expandable"
                          class="shrink-0 transition-transform duration-200 w-16px h-16px min-w-16px"
                          :style="{ transform: file.expanded ? 'rotate(0deg)' : 'rotate(-90deg)' }"
                        />
                        <span v-else class="w-16px min-w-16px" />
                        <DocFolder v-if="file.is_expandable" class="text-[#888C94] mx-4px shrink-0 min-w-16px h-16px" />
                        <Docs v-else class="text-[#888C94] mx-4px shrink-0 min-w-16px h-16px" />
                        <span class="text-sm text-[#0c131f] truncate">{{ file.name }}</span>
                      </div>
                      <div class="flex items-center shrink-0 ml-8px cursor-pointer" @click.stop="toggleThirdPartySelect(file)">
                        <Checked v-if="getThirdPartyCheckState(file) === 'checked'" class="w-16px h-16px min-w-16px" />
                        <Indeterminate v-else-if="getThirdPartyCheckState(file) === 'indeterminate'" class="w-16px h-16px min-w-16px" />
                        <Check v-else class="w-16px h-16px min-w-16px" />
                      </div>
                    </div>
                  </div>
                </div>
                <template v-if="thirdPartySelectedItems.length">
                  <div class="flex justify-between items-center mt-24px mb-12px">
                    <span class="text-xs text-[#52555F] font-500">{{ t('dms.team_space.upload.common.selected_count') }} ({{ thirdPartySelectedItems.length }})</span>
                    <span @click="deSelectAllThirdParty" class="text-xs text-[#2E59CA] cursor-pointer hover:underline">
                      {{ t('dms.team_space.upload.common.clear') }}
                    </span>
                  </div>
                  <div class="bg-[#F6F6FB] rounded-6px px-12px py-8px max-h-154px overflow-auto">
                    <div v-for="(f, i) in thirdPartySelectedItems" :key="i" :class="i && 'mt-12px'" class="flex justify-between items-center">
                      <div class="flex items-center min-w-0">
                        <div class="truncate text-sm text-[#2E59CA]">{{ f.name }}</div>
                      </div>
                      <DeleteFile @click="removeThirdPartySelectedItem(i)" class="cursor-pointer w-16px h-16px flex-shrink-0 ml-8px" />
                    </div>
                  </div>
                </template>
              </template>
            </div>
          </div>
        </template>

        <!-- Bottom buttons -->
        <div class="flex justify-center mt-24px">
          <div
            class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])"
            @click="uploadDialogVisible = false"
          >
            {{ t('splitting.cancel') }}
          </div>
          <div
            v-loading="loading"
            @click="handleUpload"
            :class="isUploadEnabled ? 'hover:bg-[#244FF0] cursor-pointer bg-[#396FFA]' : 'bg-[#88a9fc] cursor-not-allowed'"
            class="w-140px rounded-6px font-500 text-white text-sm py-8px px-10px flex items-center justify-center ml-12px"
          >
            {{ t('splitting.ok') }}
          </div>
        </div>
      </el-dialog>
      <!-- 自定义PDF nav -->
      <div class="preview-overlay <lg:hidden" :class="previewDialogVisible ? 'show' : 'hide'">
        <div @click.stop class="preview-dialog">
          <div class="header">
            <div class="flex items-center">
              <Document class="mr-8px min-w-16px" />
              <span class="text-14px leading-20px text-brand-0 overflow-hidden overflow-ellipsis">
                {{ selectFile?.fileName }}
              </span>
            </div>
          </div>
          <div class="body flex">
            <div class="w-full relative">
              <div id="webviewer" ref="viewer" class="w-628px h-full absolute top-0 left-0"></div>
            </div>
          </div>
          <div class="py-20px border-t border-[#E1E3E8] flex justify-center">
            <div @click="previewDialogVisible = false" class="whitespace-nowrap rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center justify-center font-500 hover:bg-[#244FF0]">
              {{ t('extraction.close') }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { getEnv } from '../utils/env'
import { post, get } from '../utils/request'
import { getSystemBaseUnit } from '../utils/tools'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import BatchStart from '../components/images/BatchStart.vue'
import ComPDFKitViewer from '../assets/@compdfkit/webviewer'
import Upload from '../components/images/SplittingUpload.vue'
import BatchDelete from '../components/images/BatchDelete.vue'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import JSZip from 'jszip'
import Local from '../components/images/Local.vue'
import TeamSpace from '../components/images/TeamSpace.vue'
import ThirdParty from '../components/images/ThirdParty.vue'
import Google from '../components/images/Google.vue'
import AWS from '../components/images/Aws.vue'
import NAS from '../components/images/Nas.vue'
import Notion from '../components/images/Notion.vue'
import Trello from '../components/images/Trello.vue'
import Gmail from '../components/images/Gmail.vue'
import Gcs from '../components/images/Gcs.vue'
import FileArrow from '../components/images/FileArrow.vue'
import DocFolder from '../components/images/DocFolder.vue'
import Docs from '../components/images/Docs.vue'
import Indeterminate from '../components/images/Indeterminate.vue'
import Checked from '../components/images/Checked.vue'
import Check from '../components/images/Check.vue'
import DeleteFile from '../components/images/DeleteFile.vue'
import { useStore } from '../stores'

const { t, locale } = useI18n()
const store = useStore()
let UI = <any>null
const total = ref(0)
const label = ref('')
const separator = ref('')
const viewer = ref<HTMLDivElement | null>(null)
const dataList = ref<FileData[]>([])
const pageSize = ref(10)
const currentPage = ref(1)
const loading = ref(false)
const dragover = ref(false)
const splitMethods = ref<string[]>([])
const rangeCustomInput = ref('')
const splitMethod = ref('every_n_pages')
const dialogVisible = ref(false)
const fileList = ref<File[]>([])
const pageSizes = ref([5, 10, 20])
const searchQuery = ref<string>('')
const rangeMenuVisible = ref(false)
const evert = ref<number | null>(null)
const uploadDialogVisible = ref(false)

type UploadTab = 'local' | 'team' | 'thirdParty'
const uploadActiveTab = ref<UploadTab>('local')
const uploadTabs = computed(() => [
  { key: 'local' as UploadTab, label: t('dms.team_space.upload.tabs.local'), icon: Local },
  { key: 'team' as UploadTab, label: t('dms.scanner_inbox.tabs.team_space'), icon: TeamSpace },
  { key: 'thirdParty' as UploadTab, label: t('dms.team_space.upload.tabs.third_party'), icon: ThirdParty },
])
const previewDialogVisible = ref(false)
const averagely = ref<number | null>(null)
const rangeMode = ref<'odd' | 'even' | 'ranges'>('odd')

watch(dialogVisible, (newVal) => {
  if (!newVal) {
    selectFile.value = null
  }
})

const preview = (row: FileData) => {
  previewDialogVisible.value = true
  selectFile.value = row
  UI.loadDocument(row.fileDownUrl)
}

const changeNumber = () => {
  const max = selectFile.value?.pageCount as number
  if (evert.value !== null && evert.value > max) {
    evert.value = max
  } else if (evert.value !== null && evert.value < 1) {
    evert.value = 1
  }
  if (averagely.value !== null && averagely.value > max) {
    averagely.value = max
  } else if (averagely.value !== null && averagely.value < 1) {
    averagely.value = 1
  }
}

const onRangeModeChange = (value: 'odd' | 'even' | 'ranges') => {
  rangeMode.value = value
  rangeMenuVisible.value = false
  
  if (value !== 'ranges') {
    rangeCustomInput.value = ''
  }
}

const input = ref<HTMLInputElement | null>(null)

const getFileExtension = (fileName?: string): string => {
  if (!fileName) return ''
  const lastDotIndex = fileName.lastIndexOf('.')
  // 没有后缀 / 以 . 开头的隐藏文件 / 以 . 结尾
  if (lastDotIndex <= 0 || lastDotIndex === fileName.length - 1) return ''
  return fileName.slice(lastDotIndex + 1).toUpperCase()
}

const getTableData = async () => {
  const { data } : any = await get(`/api/idp/getFileList?page=${currentPage.value}&pageSize=${pageSize.value}&fileName=${searchQuery.value}&taskType=SPLIT`)
  dataList.value = data.data.records
  total.value = data.data.total
}

onMounted(() => {
  getTableData()
  addEventListener('click', () => {
    previewDialogVisible.value = false
  })
  const license = getEnv('LICENSE_KEY') || 'Ki6UpWkucL6aKcocIWVc/f6fUYgKpAYSp1jNWm6aAaDr7ADonPnxyKmJSP86hxQgdB6bwzmTgbXe/NRg5JjmxeQKrjYOA6aQH/NUE0p/YfVny07PfmMU7SX6+AQxlTbk+of2WJbt6wf69JxpfjO9Aj2iTq3eR1Vu8+Ue99Z3b/GKSoMjgmjBaSN21lScTJ230yeyVZc0rjdt+QVuDpwBJZfSzpQbBL+/tbYRUhex05kFAtBRUT0d0mNKb4NCTLwr/oPY3u+fZQNI1OwCN8MaeD0ozqfq+itk+tx8s0a3MS3QCBX39TsNqcDi/a5Vt5H04GbID51WuEKkb799UN7SB68kD+Q9C95FZo3W7DLPF5Id3tVLjwj02FGNgeewpeIdNgRNpzdDAHO+UDvFjQ41jdGQ4tgb2bpMiMt/INJeLobLnkbPIwad7n6f7KhGyOTDrhxz9BO+lj2kqK576aB5pF+vmAl2+odMJncYhWcfj8JC5BpjcgCtCkhzbU9v11R07ByAbYqOaoeXnOVdXCbwJZG/RtoaHdnu7QRtPR1L8IZQuqtYbmLAOPf/MKcZJNqiQ8d9Wf3kFPKfscpfcawvNc3nKDL98eIvaPVl9IniKvGs7pTFLtnXIbTW88FCzyKw/aXqrQ6Uhea+RDLGmQJTIojMr4vkPz6c/9gm/RtO/NOyxDGwhy7sHiAcwhIkwl7Zg9s6QB8YBY20hAMGEzV0IZjg27eaqBaClfh1dpIXutHIupoN7O0iH2Jm0duAoYGnMkmhDaatl0gIsUdFyLyd7MnnfM1/PN7JeZhPr1ZPbK6tx9N6XiFMi2eRPwL5TAyN2MIz0ggkq1jjnGXYXa6rdSrdVSe/zA9bYRrB2comG+xb98yVV9hO9gRfyBAAGHFKlkUdj1g1SrbTNwHG164RIhBoP12s3knqc8f8GjpGGk7G5BqgnSydp+Hzc38kd13p'
  ComPDFKitViewer.init({
    license,
    pdfUrl: '',
    path: '/',
    showToolbarControl: false,
    isRenderAnnotations: false,
    enableDefaultFont: true
  }, viewer.value).then((core: any) => {
    UI = core.UI
    core.UI.disableElements(['pageNavOverlay'])
    core.UI.setLanguage(locale.value)
    core.UI.textPopup.update([])
  })
})

// 组件卸载时清除轮询
onUnmounted(() => {
  stopPolling()
  removeEventListener('click', () => {
    previewDialogVisible.value = false
  })
})

interface FileData {
  fileId: string
  fileName: string
  pageCount: number
  uploadTime: string
  fileDownUrl: string
  resultDownUrl: string
}

const selectFilesList = ref<FileData[]>([])
const selectFile = ref<FileData | null>(null)
const handleSelectFile = (row: FileData) => {
  dialogVisible.value = true
  selectFile.value = row
}

const getSelectedFileIds = (): string[] => selectFilesList.value.map(item => item.fileId)

const handleSizeChange = (value: number) => {
  pageSize.value = value
  getTableData()
}

const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getTableData()
}

const rowKey = (row: FileData) => row.fileId

// 表格多选事件处理
const handleSelectionChange = (selection: FileData[]) => {
  selectFilesList.value = []
  selection.forEach((item: FileData) => {
    selectFilesList.value.push(item)
  })
}

// 轮询定时器
let pollTimer: ReturnType<typeof setTimeout> | null = null
const POLL_INTERVAL = 3000 // 轮询间隔 3 秒

// 轮询查询文件处理状态
const pollFileStatus = async (fileIds: string[]) => {
  // 清除之前的轮询
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }

  const queryString = fileIds.map(id => `fileIds=${id}`).join('&')
  
  try {
    const { data } = await get(`/api/idp/get-file-by-ids?${queryString}`)
    
    if (data.code === 200 && data.data) {
      const files = Array.isArray(data.data) ? data.data : [data.data]
      // 检查是否所有文件的 status 都是 2 或 3
      const allCompleted = files.every((file: any) => file.status === 2 || file.status === 3)
      
      if (allCompleted) {
        // 所有文件处理完成，刷新表格数据
        getTableData()
        // 只导出状态为 2 的文件
        const successFileIds = files
          .filter((file: any) => file.status === 2)
          .map((file: any) => file.fileId)
        if (successFileIds.length > 0) {
          await downloadExportZip(successFileIds)
          ElMessage.success(t('splitting.success'))
        } else {
          ElMessage.error(t('splitting.fail'))
        }
        loading.value = false
      } else {
        // 继续轮询
        pollTimer = setTimeout(() => pollFileStatus(fileIds), POLL_INTERVAL)
      }
    }
  } catch {
    // 请求失败时继续轮询
    pollTimer = setTimeout(() => pollFileStatus(fileIds), POLL_INTERVAL)
  }
}

// 下载导出的压缩包
const downloadExportZip = async (fileIds: string[]) => {
  try {
    const response = await post(
      `/api/idp/split-export`,
      { fileIds },
      {},
      { headers: {}, responseType: 'blob' } as any
    )

    // 直接使用服务端返回的原始 Blob，避免前端二次封装
    const contentType = (response.headers?.['content-type'] as string | undefined) ?? 'application/zip'
    const blob = response.data instanceof Blob
      ? response.data
      : new Blob([response.data as any], { type: contentType })
    const url = window.URL.createObjectURL(blob)
    
    // 创建下载链接并触发下载
    const link = document.createElement('a')
    link.href = url
    // 从响应头获取文件名，如果没有则使用默认名称
    const contentDisposition = (response.headers?.['content-disposition'] as string | undefined) ?? ''
    let fileName = 'split_files.zip'
    if (contentDisposition) {
      const match = contentDisposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
      const rawName = match?.[1] ?? match?.[2]
      if (rawName) {
        fileName = decodeURIComponent(rawName)
      }
    }
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error(t('splitting.fail'))
  }
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
}

const startSplit = async () => {
  const fileIds = selectFile.value?.fileId ? [selectFile.value.fileId] : getSelectedFileIds()
  if (!fileIds.length) return

  // 验证输入值
  if (splitMethod.value === 'every_n_pages' && !evert.value) {
    ElMessage.error(t('splitting.inputRequired'))
    return
  }
  if (splitMethod.value === 'averagely' && !averagely.value) {
    ElMessage.error(t('splitting.inputRequired'))
    return
  }
  if (splitMethod.value === 'ranges' && rangeMode.value === 'ranges' && !rangeCustomInput.value) {
    ElMessage.error(t('splitting.inputRequired'))
    return
  }

  loading.value = true
  try {
    const parameter = {
      splitMode: splitMethod.value,
      splitArg: '',
      splitLabel: label.value,
      splitSeparator: separator.value,
      splitOriginalNameFirst: splitMethods.value.includes('keep')
    }
    if (splitMethod.value === 'every_n_pages' && evert.value) {
      parameter.splitArg = evert.value.toString()
    } else if (splitMethod.value === 'averagely' && averagely.value) {
      parameter.splitArg = averagely.value.toString()
    } else if (splitMethod.value === 'ranges') {
      parameter.splitMode = rangeMode.value
      if (rangeCustomInput.value) {
        parameter.splitArg = rangeCustomInput.value
      }
    }
    const formData = new FormData()
    fileIds.forEach(id => {
      formData.append('idpFileIds', id)
    })
    formData.append('parameter', JSON.stringify(parameter))
    formData.append('type', 'SPLIT')

    const { data } = await post('/api/idp/files-start', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
    if (data.code === 200 && data.message === 'success') {
      dialogVisible.value = false
      getTableData()
      // 开始轮询查询文件处理状态
      pollFileStatus(fileIds)
    }
  } catch {
    loading.value = false
    ElMessage.error(t('splitting.fail'))
  }
}

// 删除文件
const deleteFile = async (id: string[]) => {

  const fileIds = id?.length ? id : getSelectedFileIds()
  if (!fileIds.length) return

  ElMessageBox.confirm(t('splitting.deleteTip'), t('splitting.deleteTitle'), {
    confirmButtonText: t('splitting.ok'),
    cancelButtonText: t('splitting.cancel'),
    type: 'warning',
    customClass: 'delete-file',
  }).then(async () => {
    try {
      const { data } = await get(`/api/idp/file-delete?fileIds=${fileIds}`)
      if (data.code === 200 && data.message === 'success') {
        getTableData()
        ElMessage.success(t('splitting.deleteSuccess'))
      }
    } catch {
      ElMessage.error(t('splitting.deleteFail'))
    }
  })
}

// 判断文件是否重复：根据 name 和 size 判断
const isDuplicate = (file: any, list?: any[]): boolean => {
  if (!Array.isArray(list)) return false
  return list.some(item => item.name === file.name && item.size === file.size)
}

const onDrop = async (e: DragEvent) => {
  e.preventDefault()
  const files = e.dataTransfer?.files
  dragover.value = false
  if (!files || files.length === 0) return

  const validFiles = await validateFiles(files)
  if (!validFiles) {
    if (input.value) input.value.value = '' // 重置 file input
    return
  }

  // 去重后 push
  validFiles.forEach((file: any) => {
    if (!isDuplicate(file, fileList.value)) {
      fileList.value.push(file)
    }
  })
}

const base = getSystemBaseUnit()
const MAX_SIZE = base * base * 1000 // 10MB
const MAX_COUNT = 999
GlobalWorkerOptions.workerSrc = '/lib/pdf.worker.min.mjs'
const CMAP_URL = '/lib/cmaps/'
// 校验上传文件
const validateFiles = async (files: FileList): Promise<globalThis.File[] | null> => {
  const fileArray = Array.from(files)
  const oversized = fileArray.filter(file => file.size > MAX_SIZE)
  if (oversized.length > 0) {
    ElMessage.error(t('knowledgeBases.dataset.larger'))
    return null
  }

  if (fileArray.length > MAX_COUNT) {
    ElMessage.error(t('knowledgeBases.dataset.max'))
    return null
  }

  const results = await Promise.all(fileArray.map(async file => {
    if (file.name.endsWith('.pdf')) {
      const isProtected = await checkPassword(file)
      return ({ file, isProtected })
    } else {
      return Promise.resolve({ file, isProtected: false })
    }
  }))
  const unprotectedFiles = results.filter(result => !result.isProtected).map(result => result.file)
  if (results.length !== unprotectedFiles.length) {
    ElMessage.warning(t('bulkExtract.encryptTip'))
  }

  return unprotectedFiles
}

// 检查文档是否受密码保护
const checkPassword = async (file: globalThis.File): Promise<boolean> => {
  const arrayBuffer = await file.arrayBuffer()
  const parameters = {
    cMapUrl: CMAP_URL,
    cMapPacked: true,
    enableXfa: true,
    data: arrayBuffer
  }
  const loadingTask = getDocument(parameters)

  return new Promise((resolve, _reject) => {
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

// 点击上传文件
const handleChange = async (e: Event) => {
  const inputEl = e.target as HTMLInputElement
  const files = inputEl.files
  if (!files || files.length === 0) return

  const validFiles = await validateFiles(files)
  if (!validFiles) {
    inputEl.value = ''
    return
  }

  // 去重后 push
  validFiles.forEach((file: any) => {
    if (!isDuplicate(file, fileList.value)) {
      fileList.value.push(file)
    }
  })
  inputEl.value = ''
}

const deleteUploadFile = (index: number) => {
  fileList.value.splice(index, 1)
}

type UploadSummary = {
  total: number
  success: number
  failed: number
  allSucceeded: boolean
}

const uploadAllFiles = async (files: File[]): Promise<UploadSummary> => {
  const tasks = files.map((file) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('taskType', 'SPLIT')
    return post('/api/idp/file-upload', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
  })

  const results = await Promise.allSettled(tasks)
  const success = results.filter(r => r.status === 'fulfilled').length
  const failed = results.length - success
  return {
    total: results.length,
    success,
    failed,
    allSucceeded: failed === 0
  }
}

// 拆分上传文件
const upload = async () => {
  if (loading.value || !fileList.value.length) return
  loading.value = true

  try {
    const summary = await uploadAllFiles(fileList.value)
    if (summary.allSucceeded) {
      ElMessage.success(t('splitting.success'))
      fileList.value = []
      uploadDialogVisible.value = false
      getTableData()
    } else {
      ElMessage.error(`${t('splitting.fail')} (${summary.failed}/${summary.total})`)
    }
  } catch {
    ElMessage.error(t('splitting.fail'))
  } finally {
    loading.value = false
  }
}

const leave = () => {
  dragover.value = false
}

// ── Team Space tab state ──
interface FolderNode {
  id: string
  name: string
  checked: boolean
  indeterminate?: boolean
  expanded?: boolean
  loaded?: boolean
  children?: FolderNode[]
}

interface Creator {
  email: string
  id: string
  nickname: string
}

interface folderType {
  create_time: number
  creator: Creator
  file_count: number
  id: string
  name: string
  role: string
}

interface DocumentFile {
  create_date: string
  create_time: number
  creator: Creator
  file_type: string
  folder_id: string
  folder_name: string
  id: string
  name: string
  role: string
  size: string
  update_date: string
  update_time: number
}

const folderList = ref<FolderNode[]>([])
const teamSelectedFiles = ref<{ id: string; name: string }[]>([])

const updateFolderCheckState = (node: FolderNode) => {
  if (!node.children || node.children.length === 0) return
  const allChecked = node.children.every(c => c.checked)
  const noneChecked = node.children.every(c => !c.checked)
  node.checked = allChecked
  node.indeterminate = !allChecked && !noneChecked
}

const addFilesToSelected = (files: FolderNode[]) => {
  files.forEach(f => {
    if (!teamSelectedFiles.value.some(s => s.id === f.id)) {
      teamSelectedFiles.value.push({ id: f.id, name: f.name })
    }
  })
}

const removeFilesFromSelected = (ids: string[]) => {
  teamSelectedFiles.value = teamSelectedFiles.value.filter(s => !ids.includes(s.id))
}

const toggleTeamFolder = async (node: FolderNode) => {
  node.expanded = !node.expanded
  if (node.expanded && !node.loaded) {
    const { data } = await get(`/v1/team_space/file/search?folder_id=${node.id}`)
    const files: DocumentFile[] = data.data.files ?? []
    node.children = files.map(f => ({
      id: f.id,
      name: f.name,
      checked: teamSelectedFiles.value.some(s => s.id === f.id)
    }))
    node.loaded = true
    updateFolderCheckState(node)
  }
}

const toggleTeamCheck = async (node: FolderNode) => {
  const newChecked = !node.checked
  node.checked = newChecked
  node.indeterminate = false

  if (!node.loaded) {
    const { data } = await get(`/v1/team_space/file/search?folder_id=${node.id}`)
    const files: DocumentFile[] = data.data.files ?? []
    node.children = files.map(f => ({
      id: f.id,
      name: f.name,
      checked: newChecked
    }))
    node.loaded = true
  } else {
    node.children?.forEach(c => { c.checked = newChecked })
  }

  if (newChecked) {
    addFilesToSelected(node.children ?? [])
  } else {
    removeFilesFromSelected((node.children ?? []).map(c => c.id))
  }
}

const toggleTeamChildCheck = (parent: FolderNode, child: FolderNode) => {
  child.checked = !child.checked
  updateFolderCheckState(parent)

  if (child.checked) {
    if (!teamSelectedFiles.value.some(s => s.id === child.id)) {
      teamSelectedFiles.value.push({ id: child.id, name: child.name })
    }
  } else {
    removeFilesFromSelected([child.id])
  }
}

const removeTeamSelectedFile = (index: number) => {
  const removed = teamSelectedFiles.value[index]
  teamSelectedFiles.value.splice(index, 1)
  for (const folder of folderList.value) {
    if (!folder.children) continue
    const child = folder.children.find(c => c.id === removed.id)
    if (child) {
      child.checked = false
      updateFolderCheckState(folder)
      break
    }
  }
}

const clearTeamSelectedFiles = () => {
  teamSelectedFiles.value = []
  folderList.value.forEach(folder => {
    folder.checked = false
    folder.indeterminate = false
    if (folder.children) folder.children.forEach(c => { c.checked = false })
  })
}

// 上传团队空间文件到 splitting
const uploadTeamSpaceFile = async () => {
  if (loading.value || !teamSelectedFiles.value.length) return
  loading.value = true
  try {
    const response = await post('/v1/team_space/file/download', {
      file_ids: teamSelectedFiles.value.map(f => f.id)
    }, {}, { responseType: 'blob' } as any)

    const contentType = (response.headers?.['content-type'] as string | undefined) ?? ''

    // 后端可能以 JSON 返回业务错误（即使 HTTP 200），需先检测
    if (contentType.includes('json')) {
      const text = response.data instanceof Blob ? await response.data.text() : JSON.stringify(response.data)
      try {
        const json = JSON.parse(text)
        ElMessage.error(json?.message || t('splitting.fail'))
      } catch {
        ElMessage.error(t('splitting.fail'))
      }
      loading.value = false
      return
    }

    let restoredFiles: File[]
    const blob = response.data instanceof Blob ? response.data : new Blob([response.data])

    // 始终先尝试 zip 解压（后端无论单文件/多文件都可能返回 zip）
    try {
      const zip = await JSZip.loadAsync(blob)
      const fileEntries = Object.values(zip.files).filter(f => !f.dir)

      if (fileEntries.length === 0) {
        ElMessage.error(t('splitting.fail'))
        loading.value = false
        return
      }

      restoredFiles = await Promise.all(
        fileEntries.map(async entry => {
          const arrayBuffer = await entry.async('arraybuffer')
          const fileName = entry.name.split('/').pop() || entry.name
          return new File([arrayBuffer], fileName)
        })
      )
    } catch {
      // 非 zip 格式，当作单文件处理
      const disposition = (response.headers?.['content-disposition'] as string | undefined) ?? ''
      const filenameMatch = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
      const filenameRaw = filenameMatch?.[1] ?? filenameMatch?.[2]
      const fallbackName = teamSelectedFiles.value[0]?.name || 'download'
      const fileName = filenameRaw ? decodeURIComponent(filenameRaw) : fallbackName
      restoredFiles = [new File([blob], fileName)]
    }

    const summary = await uploadAllFiles(restoredFiles)
    if (summary.allSucceeded) {
      ElMessage.success(t('splitting.success'))
      teamSelectedFiles.value = []
      uploadDialogVisible.value = false
      getTableData()
    } else {
      ElMessage.error(`${t('splitting.fail')} (${summary.failed}/${summary.total})`)
    }
  } catch {
    ElMessage.error(t('splitting.fail'))
  } finally {
    loading.value = false
  }
}

// ── Third-party tab state ──

interface Credential {
  alias: string
  created_at: string
  id: number
  is_active: boolean
  last_verified_at: string
  source: string
  updated_at: string
}

const PLATFORM_SOURCE_MAP: Record<string, string> = {
  'google-drive': 'google_drive',
  'aws':          'aws_oss',
  'nas':          'nas_smb',
  'notion':       'notion',
  'trello':       'trello',
  'gmail':        'gmail',
  'gcs':          'gcs',
}

const PLATFORM_ROUTE_MAP: Record<string, string> = {
  'google-drive': '/third-party/google-drive-authorization',
  'aws':          '/third-party/aws-authorization',
  'nas':          '/third-party/nas-authorization',
  'notion':       '/third-party/notion-authorization',
  'trello':       '/third-party/trello-authorization',
  'gmail':        '/third-party/gmail-authorization',
  'gcs':          '/third-party/google-cloud-storage-authorization',
}

interface DmsFileItem {
  id: string
  name: string
  is_dir: boolean
  is_expandable: boolean
  level: number
  parent_id: string | null
  expanded?: boolean
  selected?: boolean
  path?: string
  prefix?: string
  bucket_name?: string
  awsType?: string
  node_type?: string
  mailbox?: string
  uid?: string
  attachment_index?: number
  file_property_name?: string
  kind?: string
}

const selectedPlatform = ref('google-drive')

const thirdPartyPlatforms = [
  { key: 'google-drive', name: 'Google Drive', icon: Google },
  { key: 'aws',          name: 'AWS S3',        icon: AWS },
  { key: 'nas',          name: 'NAS',            icon: NAS },
  { key: 'notion',       name: 'Notion',         icon: Notion },
  { key: 'trello',       name: 'Trello',         icon: Trello },
  { key: 'gmail',        name: 'Gmail',          icon: Gmail },
  { key: 'gcs',          name: 'Google Cloud',   icon: Gcs },
]

const thirdPartyCredentials = ref<Record<string, Credential | null>>(
  Object.fromEntries(thirdPartyPlatforms.map(p => [p.key, null]))
)

const thirdPartyAuthorized = computed<boolean>(() => {
  const cred = thirdPartyCredentials.value[selectedPlatform.value]
  return cred?.is_active === true
})

const thirdPartyDialogFileList = ref<DmsFileItem[]>([])
const thirdPartyDialogCache = new Map<string, DmsFileItem[]>()
const thirdPartyDialogLoading = ref(false)

const thirdPartySelectedItems = computed<DmsFileItem[]>(() => {
  const leaves: DmsFileItem[] = []
  const collect = (items: DmsFileItem[]) => {
    for (const item of items) {
      if (!item.is_expandable && item.selected) leaves.push(item)
    }
  }
  collect(thirdPartyDialogFileList.value)
  for (const cached of thirdPartyDialogCache.values()) {
    collect(cached)
  }
  return leaves
})

const authorizeThirdParty = () => {
  const routePath = PLATFORM_ROUTE_MAP[selectedPlatform.value]
  if (routePath) location.href = routePath
}

const getDmsItemKey = (item: DmsFileItem): string => item.path ?? item.id

const loadThirdPartyRoot = async (): Promise<DmsFileItem[]> => {
  const source = PLATFORM_SOURCE_MAP[selectedPlatform.value]
  let req: any
  switch (selectedPlatform.value) {
    case 'google-drive':
      req = { source, args: { folder_id: 'root', shared_with_me: true } }
      break
    case 'aws':
      req = { source, args: { mode: 'buckets', bucket: 'buckets' } }
      break
    case 'nas':
      req = { source, args: { path: '/' } }
      break
    case 'gcs':
      req = { source, args: { mode: 'buckets' } }
      break
    case 'gmail':
      req = { source, args: { mode: 'home' } }
      break
    case 'notion':
      req = { source, args: { mode: 'workspace' } }
      break
    case 'trello':
      req = { source, args: { type: 'home' } }
      break
    default:
      return []
  }
  const { data } = await post('/v1/dms/files/list', req)
  return mapToDmsItems(data.data ?? [], 0, null)
}

const mapToDmsItems = (files: any[], level: number, parentId: string | null): DmsFileItem[] => {
  const platform = selectedPlatform.value
  return files.map((f: any): DmsFileItem => {
    if (platform === 'google-drive') {
      return {
        id: String(f.id), name: String(f.name), is_dir: Boolean(f.is_dir),
        is_expandable: Boolean(f.is_dir), level, parent_id: parentId,
        expanded: false, selected: false,
      }
    }
    if (platform === 'aws') {
      const awsType: string = f.type ?? ''
      const isDir = ['bucket', 'folder'].includes(awsType)
      return {
        id: String(f.id || f.key || f.prefix || f.name), name: String(f.name),
        is_dir: isDir, is_expandable: isDir, level, parent_id: parentId,
        awsType, prefix: f.prefix || '',
        bucket_name: awsType === 'bucket' ? String(f.name) : (parentId ? undefined : ''),
        expanded: false, selected: false,
      }
    }
    if (platform === 'nas') {
      return {
        id: String(f.path), name: String(f.name), is_dir: Boolean(f.is_dir),
        is_expandable: Boolean(f.is_dir), level, parent_id: parentId,
        path: String(f.path), expanded: false, selected: false,
      }
    }
    if (platform === 'gcs') {
      const fileType: string = f.type ?? ''
      const isDir = ['bucket', 'folder'].includes(fileType)
      return {
        id: String(f.id || f.key || f.prefix || f.name), name: String(f.name),
        is_dir: isDir, is_expandable: isDir, level, parent_id: parentId,
        prefix: f.prefix || '',
        bucket_name: fileType === 'bucket' ? String(f.name) : undefined,
        expanded: false, selected: false,
      }
    }
    if (platform === 'gmail') {
      type GmailNodeType = 'mailbox' | 'email' | 'attachment'
      const parentItem = (parentId === null) ? null : thirdPartyDialogFileList.value.find(x => x.id === parentId)
      const parentNodeType = parentItem?.node_type ?? null
      const nodeType: GmailNodeType = parentId === null ? 'mailbox'
        : parentNodeType === 'mailbox' ? 'email'
        : 'attachment'
      const isExpandable = nodeType !== 'attachment'
      const mailbox = nodeType === 'mailbox'
        ? String(f?.id ?? f?.mailbox ?? f?.name ?? f)
        : (parentItem?.mailbox ?? '')
      const uid = nodeType === 'attachment'
        ? (parentItem?.uid ?? String(f?.uid ?? f?.id ?? ''))
        : String(f?.uid ?? f?.id ?? '')
      const name = String(f?.name ?? f?.subject ?? f?.fileName ?? f?.filename ?? f?.mailbox ?? f?.id ?? f)
      return {
        id: String(f?.id ?? f?.uid ?? f?.attachment_id ?? f?.name ?? f), name,
        is_dir: isExpandable, is_expandable: isExpandable, level, parent_id: parentId,
        node_type: nodeType, mailbox, uid, expanded: false, selected: false,
      }
    }
    if (platform === 'notion') {
      if (parentId === null) {
        const kind = String(f?.kind || '')
        const isDatabase = kind === 'database'
        const getFilePropertyName = (properties?: Record<string, any>): string | undefined => {
          if (!properties) return undefined
          for (const prop of Object.values(properties)) {
            if (prop?.type === 'file' || prop?.type === 'files') return prop?.name
          }
          return undefined
        }
        return {
          id: String(f?.id || f?.page_id || f?.database_id || f?.name),
          name: String(f?.title || f?.name || f?.id || 'Untitled'),
          is_dir: isDatabase, is_expandable: isDatabase, level, parent_id: null,
          kind, node_type: isDatabase ? 'database' : 'file',
          file_property_name: isDatabase ? getFilePropertyName(f?.properties) : undefined,
          expanded: false, selected: false,
        }
      }
      const parentItem = thirdPartyDialogFileList.value.find(x => x.id === parentId)
      const parentNodeType = parentItem?.node_type
      if (parentNodeType === 'database') {
        return {
          id: String(f?.id || f?.page_id || f?.name),
          name: String(f?.title || f?.name || f?.id || 'Untitled'),
          is_dir: true, is_expandable: true, level, parent_id: parentId,
          kind: String(f?.kind || 'page'), node_type: 'page',
          file_property_name: parentItem?.file_property_name,
          expanded: false, selected: false,
        }
      }
      if (parentNodeType === 'page') {
        const parentNode = thirdPartyDialogFileList.value.find(x => x.id === parentId)
        return {
          id: String(f?.id || f?.name || f?.url),
          name: String(f?.name || f?.file_name || f?.title || f?.id || 'Untitled'),
          is_dir: false, is_expandable: false, level, parent_id: parentId,
          kind: 'file', node_type: 'property_file',
          file_property_name: parentNode?.file_property_name,
          expanded: false, selected: false,
        }
      }
      return {
        id: String(f?.id || f?.page_id || f?.name),
        name: String(f?.title || f?.name || f?.id || 'Untitled'),
        is_dir: Boolean(f?.is_dir), is_expandable: Boolean(f?.is_dir), level, parent_id: parentId,
        kind: String(f?.kind || ''),
        node_type: Boolean(f?.is_dir) ? 'page_root' : 'file',
        expanded: false, selected: false,
      }
    }
    if (platform === 'trello') {
      const parentItem = thirdPartyDialogFileList.value.find(x => x.id === parentId)
      const parentNodeType = parentItem?.node_type ?? (parentId === null ? null : undefined)
      const nodeType = parentId === null ? 'board'
        : parentNodeType === 'board' ? 'list'
        : parentNodeType === 'list' ? 'card'
        : 'file'
      const isExpandable = nodeType !== 'file'
      return {
        id: String(f.id || f.idCard || f.idBoard || f.idList || f.name),
        name: String(f.fileName || f.name || f.id || 'Untitled'),
        is_dir: isExpandable, is_expandable: isExpandable, level, parent_id: parentId,
        node_type: nodeType, expanded: false, selected: false,
      }
    }
    return {
      id: String(f.id || f.name), name: String(f.name || f.id),
      is_dir: Boolean(f.is_dir), is_expandable: Boolean(f.is_dir),
      level, parent_id: parentId, expanded: false, selected: false,
    }
  })
}

const loadThirdPartyChildren = async (item: DmsFileItem): Promise<DmsFileItem[]> => {
  const source = PLATFORM_SOURCE_MAP[selectedPlatform.value]
  let req: any
  switch (selectedPlatform.value) {
    case 'google-drive':
      req = { source, args: { folder_id: item.id } }
      break
    case 'aws': {
      const prefix = item.awsType === 'folder' ? (item.prefix ?? '') : ''
      req = { source, args: { mode: 'bucket', bucket: item.bucket_name, prefix } }
      break
    }
    case 'nas':
      req = { source, args: { path: item.path ?? item.id } }
      break
    case 'gcs': {
      const gcsBucket = item.bucket_name ?? item.name
      const gcsPrefix = (item.level === 0 && item.bucket_name === item.name) ? '' : (item.prefix ?? '')
      req = { source, args: { mode: 'bucket', bucket: gcsBucket, prefix: gcsPrefix } }
      break
    }
    case 'gmail': {
      const nodeType = item.node_type
      if (nodeType === 'mailbox') {
        const now = new Date()
        const start = new Date(); start.setMonth(now.getMonth() - 1)
        const fmt = (d: Date) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
        req = { source, args: { mode: 'mailbox', mailbox: item.mailbox, start: fmt(start), end: fmt(now) } }
      } else if (nodeType === 'email') {
        req = { source, args: { mode: 'email', mailbox: item.mailbox, uid: item.uid } }
      } else {
        return []
      }
      break
    }
    case 'notion': {
      const nodeType = item.node_type
      if (nodeType === 'database') {
        req = { source, args: { mode: 'database', database_id: item.id } }
      } else if (nodeType === 'page') {
        if (!item.file_property_name) return []
        req = { source, args: { mode: 'property', page_id: item.id, property_name: item.file_property_name } }
      } else if (nodeType === 'page_root') {
        req = { source, args: { mode: 'page_blocks', page_id: item.id } }
      } else {
        return []
      }
      break
    }
    case 'trello': {
      const nodeType = item.node_type
      if (nodeType === 'board') req = { source, args: { type: 'board', id: item.id } }
      else if (nodeType === 'list') req = { source, args: { type: 'list', id: item.id } }
      else if (nodeType === 'card') req = { source, args: { type: 'card', id: item.id } }
      else return []
      break
    }
    default:
      return []
  }
  const { data } = await post('/v1/dms/files/list', req)
  const rawItems = mapToDmsItems(data.data ?? [], item.level + 1, item.id)
  if (selectedPlatform.value === 'aws' || selectedPlatform.value === 'gcs') {
    rawItems.forEach(child => { if (!child.bucket_name) child.bucket_name = item.bucket_name })
  }
  if (selectedPlatform.value === 'gmail' && item.node_type === 'email') {
    rawItems.forEach((child, idx) => {
      if (child.node_type === 'attachment') child.attachment_index = idx
    })
  }
  return rawItems
}

const buildDownloadArgs = (item: DmsFileItem): object => {
  switch (selectedPlatform.value) {
    case 'google-drive': return { file_id: item.id }
    case 'aws': return { mode: 'url', url: `s3://${item.bucket_name}/${item.prefix || item.id}` }
    case 'nas': return { path: item.path ?? item.id }
    case 'gcs': return { mode: 'url', url: `gs://${item.bucket_name}/${item.prefix || item.id}` }
    case 'gmail':
      if (item.node_type === 'attachment') {
        return { mode: 'attachment', mailbox: item.mailbox, uid: item.uid, attachment_index: item.attachment_index ?? 0 }
      }
      return { mode: 'email', mailbox: item.mailbox, uid: item.uid }
    case 'notion':
      if (item.node_type === 'property_file') {
        return { mode: 'property', page_id: item.parent_id, property_name: item.file_property_name }
      }
      return { mode: 'page', page_id: item.id }
    case 'trello': return { card_id: item.parent_id, att_id: item.id }
    default: return {}
  }
}

const getThirdPartyDescendantEndIndex = (list: DmsFileItem[], startIndex: number): number => {
  const currentLevel = list[startIndex].level
  let endIndex = startIndex
  for (let i = startIndex + 1; i < list.length; i++) {
    if (list[i].level <= currentLevel) break
    endIndex = i
  }
  return endIndex
}

const getThirdPartyVisibleDescendantIndices = (startIndex: number): number[] => {
  const list = thirdPartyDialogFileList.value
  const currentLevel = list[startIndex].level
  const indices: number[] = []
  for (let i = startIndex + 1; i < list.length; i++) {
    if (list[i].level <= currentLevel) break
    indices.push(i)
  }
  return indices
}

const collapseThirdPartyDialogFolder = (index: number) => {
  const item = thirdPartyDialogFileList.value[index]
  const cacheKey = getDmsItemKey(item)
  const endIndex = getThirdPartyDescendantEndIndex(thirdPartyDialogFileList.value, index)
  if (endIndex > index) {
    const snapshot = thirdPartyDialogFileList.value.slice(index + 1, endIndex + 1)
    thirdPartyDialogCache.set(cacheKey, snapshot)
    thirdPartyDialogFileList.value.splice(index + 1, endIndex - index)
  }
  thirdPartyDialogFileList.value[index].expanded = false
}

const openThirdPartyDialogFolder = async (item: DmsFileItem, index: number) => {
  if (!item.is_expandable) return
  if (item.expanded) {
    collapseThirdPartyDialogFolder(index)
    return
  }
  const cacheKey = getDmsItemKey(item)
  if (thirdPartyDialogCache.has(cacheKey)) {
    const cached = thirdPartyDialogCache.get(cacheKey)!
    thirdPartyDialogCache.delete(cacheKey)
    thirdPartyDialogFileList.value.splice(index + 1, 0, ...cached)
    thirdPartyDialogFileList.value[index].expanded = true
    return
  }
  thirdPartyDialogLoading.value = true
  try {
    const children = await loadThirdPartyChildren(item)
    thirdPartyDialogFileList.value.splice(index + 1, 0, ...children)
    thirdPartyDialogFileList.value[index].expanded = true
  } catch (err) {
    console.error('Failed to load children', err)
  } finally {
    thirdPartyDialogLoading.value = false
  }
}

const collectThirdPartyLeafStates = (item: DmsFileItem): { total: number; selected: number } => {
  if (!item.is_expandable) return { total: 1, selected: item.selected ? 1 : 0 }
  let children: DmsFileItem[]
  const directLevel = item.level + 1
  const cacheKey = getDmsItemKey(item)
  if (item.expanded) {
    const idx = thirdPartyDialogFileList.value.indexOf(item)
    children = []
    for (let i = idx + 1; i < thirdPartyDialogFileList.value.length; i++) {
      if (thirdPartyDialogFileList.value[i].level < directLevel) break
      if (thirdPartyDialogFileList.value[i].level === directLevel) children.push(thirdPartyDialogFileList.value[i])
    }
  } else {
    children = thirdPartyDialogCache.get(cacheKey)?.filter(c => c.level === directLevel) ?? []
  }
  if (children.length === 0) return { total: 1, selected: item.selected ? 1 : 0 }
  return children.reduce((acc, child) => {
    const s = collectThirdPartyLeafStates(child)
    return { total: acc.total + s.total, selected: acc.selected + s.selected }
  }, { total: 0, selected: 0 })
}

const getThirdPartyCheckState = (item: DmsFileItem): 'checked' | 'indeterminate' | 'unchecked' => {
  if (!item.is_expandable) return item.selected ? 'checked' : 'unchecked'
  const cacheKey = getDmsItemKey(item)
  const hasCache = thirdPartyDialogCache.has(cacheKey)
  if (!item.expanded && !hasCache) return item.selected ? 'checked' : 'unchecked'
  const { total, selected } = collectThirdPartyLeafStates(item)
  if (total === 0) return item.selected ? 'checked' : 'unchecked'
  if (selected === total) return 'checked'
  if (selected > 0) return 'indeterminate'
  return 'unchecked'
}

const syncThirdPartyAncestors = (fromIndex: number) => {
  const list = thirdPartyDialogFileList.value
  for (let i = fromIndex - 1; i >= 0; i--) {
    if (!list[i].expanded) continue
    const childIndices = getThirdPartyVisibleDescendantIndices(i)
    if (!childIndices.includes(fromIndex)) continue
    list[i].selected = childIndices.every(j => list[j].selected)
  }
}

const toggleThirdPartySelect = (item: DmsFileItem) => {
  const idx = thirdPartyDialogFileList.value.indexOf(item)
  if (idx === -1) return
  const currentState = getThirdPartyCheckState(item)
  const newSelected = currentState !== 'checked'
  thirdPartyDialogFileList.value[idx].selected = newSelected
  const descendantIndices = getThirdPartyVisibleDescendantIndices(idx)
  descendantIndices.forEach(i => { thirdPartyDialogFileList.value[i].selected = newSelected })
  syncThirdPartyAncestors(idx)
}

const deSelectAllThirdParty = () => {
  thirdPartyDialogFileList.value.forEach(item => { item.selected = false })
  for (const cached of thirdPartyDialogCache.values()) {
    cached.forEach(item => { item.selected = false })
  }
}

const removeThirdPartySelectedItem = (index: number) => {
  const item = thirdPartySelectedItems.value[index]
  if (!item) return
  item.selected = false
}

// 上传第三方文件到 splitting
const uploadThirdPartyFiles = async () => {
  if (loading.value || !thirdPartySelectedItems.value.length) return
  loading.value = true
  const source = PLATFORM_SOURCE_MAP[selectedPlatform.value]
  const uploadFiles: File[] = []

  try {
    for (const item of thirdPartySelectedItems.value) {
      const args = buildDownloadArgs(item)
      const response = await post('/v1/dms/files/download', { source, args }, {}, {
        responseType: 'blob'
      } as any)
      const blob = response.data as unknown as Blob
      const zip = await JSZip.loadAsync(blob)
      for (const [filename, zipEntry] of Object.entries(zip.files)) {
        if (zipEntry.dir) continue
        const fileBlob = await zipEntry.async('blob')
        const basename = filename.split('/').pop() || filename
        uploadFiles.push(new File([fileBlob], basename))
      }
    }

    if (uploadFiles.length === 0) {
      ElMessage.error(t('splitting.fail'))
      loading.value = false
      return
    }

    const summary = await uploadAllFiles(uploadFiles)
    if (summary.allSucceeded) {
      ElMessage.success(t('splitting.success'))
      uploadDialogVisible.value = false
      getTableData()
    } else {
      ElMessage.error(`${t('splitting.fail')} (${summary.failed}/${summary.total})`)
    }
  } catch {
    ElMessage.error(t('splitting.fail'))
  } finally {
    loading.value = false
  }
}

// 切换平台时重置文件树，若已授权则自动加载
watch(selectedPlatform, async () => {
  thirdPartyDialogFileList.value = []
  thirdPartyDialogCache.clear()
  if (thirdPartyAuthorized.value) {
    thirdPartyDialogLoading.value = true
    try {
      thirdPartyDialogFileList.value = await loadThirdPartyRoot()
    } catch (err) {
      console.error('Failed to load third-party root', err)
    } finally {
      thirdPartyDialogLoading.value = false
    }
  }
})

// 上传对话框：打开时加载数据，关闭时重置所有状态
watch(uploadDialogVisible, async (newVal) => {
  if (newVal) {
    const loadFolders = async () => {
      try {
        const { data } = await get('/v1/team_space/root_folders')
        const folders: folderType[] = data.data.folders ?? []
        folderList.value = folders.map(f => ({
          id: f.id,
          name: f.name,
          checked: false,
          indeterminate: false,
          expanded: false,
          loaded: false,
        }))
      } catch (err) {
        console.error('Failed to load team space folders', err)
      }
    }

    const loadCredentials = async () => {
      try {
        const { data } = await get('/v1/dms/auth/credentials')
        const credentials: Credential[] = data.data.connections ?? []
        const SOURCE_TO_KEY: Record<string, string> = Object.fromEntries(
          Object.entries(PLATFORM_SOURCE_MAP).map(([k, v]) => [v, k])
        )
        const updated = { ...thirdPartyCredentials.value }
        for (const cred of credentials) {
          const key = SOURCE_TO_KEY[cred.source]
          if (key) updated[key] = cred
        }
        thirdPartyCredentials.value = updated
      } catch (err) {
        console.error('Failed to load third-party credentials', err)
      }
    }

    await Promise.all([loadFolders(), loadCredentials()])

    if (thirdPartyAuthorized.value) {
      thirdPartyDialogLoading.value = true
      try {
        thirdPartyDialogFileList.value = await loadThirdPartyRoot()
      } catch (err) {
        console.error('Failed to load third-party root', err)
      } finally {
        thirdPartyDialogLoading.value = false
      }
    }
  } else {
    uploadActiveTab.value = 'local'
    fileList.value = []
    folderList.value = []
    teamSelectedFiles.value = []
    thirdPartyDialogFileList.value = []
    thirdPartyDialogCache.clear()
    selectedPlatform.value = 'google-drive'
    thirdPartyCredentials.value = Object.fromEntries(thirdPartyPlatforms.map(p => [p.key, null]))
  }
})

const isUploadEnabled = computed<boolean>(() => {
  if (uploadActiveTab.value === 'local') return fileList.value.length > 0
  if (uploadActiveTab.value === 'team') return teamSelectedFiles.value.length > 0
  if (uploadActiveTab.value === 'thirdParty') return thirdPartySelectedItems.value.length > 0
  return false
})

const handleUpload = () => {
  if (!isUploadEnabled.value) return
  if (uploadActiveTab.value === 'local') upload()
  else if (uploadActiveTab.value === 'team') uploadTeamSpaceFile()
  else if (uploadActiveTab.value === 'thirdParty') uploadThirdPartyFiles()
}
</script>

<style lang="scss" scoped>
.preview-overlay {
  bottom: 0;
  height: 100%;
  left: 0;
  overflow: auto;
  position: fixed;
  right: 0;
  top: 0;
  z-index: 5;
  background-color: rgba(0, 0, 0, 0.5);
  transition: opacity 0.5s ease, visibility 0.5s ease;
  &.show {
    opacity: 1;
    visibility: visible;
    .preview-dialog {
      opacity: 1;
      transform: translateY(0);
    }
  }
  &.hide {
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    .preview-dialog {
      opacity: 0;
      transform: translateY(-15px);
    }
  }
  .preview-dialog {
    padding: 0;
    width: 628px;
    overflow: hidden;
    margin: 40px auto;
    border-radius: 20px;
    background: white;
    transition: all 0.5s ease;
    height: calc(100vh - 80px);
    .header {
      padding: 0 32px;
      height: 80px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .body {
      height: calc(100% - 160px);
      .show {
        opacity: 1;
        visibility: visible;
      }
      .hide {
        opacity: 0;
        visibility: hidden;
        pointer-events: none;
      }
    }
  }
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

.document-splitting * {
  font-family: 'Encode Sans';
}
.shadows {
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0px 4px 35px 0px #8195C82E;
}
:deep(.el-tooltip__trigger.svg) {
  rect.borders {
    stroke: transparent;
  }
  rect.bg {
    fill: transparent;
  }
  &:hover {
    rect.borders {
      stroke: #396FFA;
    }
    rect.bg {
      fill: #EBF1FE;
    }
  }
  &:focus {
    outline: none;
  }
}
:deep(.el-dialog) {
  padding: 20px 24px;
}
:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  .el-checkbox {
    width: 100%;
    height: auto;
    padding: 2px 0;
    margin-right: 0px;
    & + .el-checkbox {
      margin-top: 8px;
    }
    &:hover .el-checkbox__label {
      color: #396FFA;
    }
    .el-checkbox__label {
      display: flex;
      width: 100%;
      font-size: 14px;
      line-height: 20px;
      padding-left: 8px;
      color: #404653;
      align-items: center;
      .el-input {
        flex: 1 1 0%;
        margin-left: 8px;
        .el-input__inner {
          min-height: 30px;
        }
      }
    }
  }
}
:deep(.el-radio-group) {
  width: 100%;
  .el-radio {
    padding: 0;
    width: 100%;
    margin-right: 0;
    color: #404653;
    &:hover {
      color: #396FFA;
      background: transparent;
    }
    & + .el-radio {
      margin-top: 8px;
    }
    .el-radio__label {
      width: 100%;
      display: flex;
      padding: 4px 0;
      font-size: 14px;
      line-height: 20px;
      align-items: center;
      .el-input {
        flex: 1 1 0%;
        margin: 0 8px;
        .el-input__inner {
          min-height: 30px;
        }
      }
      .el-select {
        margin-left: 8px;
        .el-select__wrapper {
          min-height: 32px;
        }
      }
      &:hover {
        color: #396FFA;
      }
    }
    .el-radio__inner {
      width: 16px;
      height: 16px;
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
</style>

<style lang="scss">
.el-overlay.is-message-box .el-overlay-message-box .el-message-box.delete-file {
  .el-message-box__header {
    height: auto;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    margin-left: 36px;
    color: #404653;
    margin-bottom: 8px;
  }
  .el-message-box__content .el-message-box__container {
    align-items: flex-start;
    .el-message-box-icon--warning {
      margin-top: -32px;
      svg path {
        fill: #F28909;
      }
    }
    .el-message-box__message {
      font-size: 14px;
      padding-right: 0;
      line-height: 20px;
      padding-left: 12px;
      color: #404653;
    }
  }
  .el-message-box__btns {
    justify-content: flex-end;
    .el-button {
      padding: 6px 12px;
      width: fit-content;
      &:first-child {
        background: #F6F6FB;
        border: 1px solid #E2E3E5;
        color: #0C131F;
        &:hover {
          background: #F6F6FB;
          color: #396FFA;
        }
        &:active {
          background: #EBEDF0;
          color: #88A9FC;
        }
      }
    }
  }
}
.el-popper.is-pure.is-light.el-tooltip.el-select__popper.split {
  .el-select-dropdown.split .el-scrollbar .el-select-dropdown__wrap .el-select-dropdown__list {
    padding: 4px 0;
    .el-select-dropdown__item {
      color: #404653;
      font-weight: normal;
    }
  }
}
</style>
