<template>
  <div class="document-extraction-page flex">
    <!-- 左侧模版列表 -->
    <div class="template-sidebar-panel px-12px py-16px w-[230px] flex flex-col h-[calc(100vh-81px)]">
      <div class="template-sidebar-main flex flex-col">
        <!-- New Templates Button + Settings -->
        <div v-if="canEditTemplate" class="template-sidebar-actions flex items-center mb-12px">
          <div @click="startCreateTemplate" class="new-template-btn flex-1 rounded-3px font-400 cursor-pointer bg-[#396FFA] text-white text-14px px-16px flex items-center justify-center hover:bg-[#244FF0]">
            <svg class="w-16px h-16px mr-8px" viewBox="0 0 16 16" fill="none">
              <path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            {{ t('extraction.createTemplate') }}
          </div>
          <div @click="openTemplateSettings" class="settings-square ml-8px rounded-3px border border-[#DCDDE1] cursor-pointer hover:bg-[#F5F7FA]">
            <TemplateSetting class="w-16px h-16px text-[#0C131F]" />
          </div>
        </div>

        <div v-if="canEditTemplate" class="template-sidebar-divider mb-12px"></div>

        <!-- Search -->
        <div class="relative mb-8px flex items-center">
          <Search class="absolute left-8px w-16px h-16px text-[#0C131F]" />
          <input
            v-model="searchQueryTemp"
            @input="getTemplateList"
            @clear="getTemplateList"
            type="text"
            :placeholder="t('extraction.search')"
            class="template-search-input w-full h-32px pl-36px pr-8px text-14px rounded-3px outline-none"
          />
        </div>

        <!-- All Documents Button -->
        <div class="mb-8px">
          <button
            @click="changeActiveTemp()"
            :class="activeTemp === 'all' ? 'bg-[#F5F7FF] text-[#396FFA]' : 'bg-white text-[#396FFA]'"
            class="all-documents-btn w-full h-32px text-14px font-400 px-16px rounded-3px flex items-center justify-start hover:bg-[#F5F7FA] cursor-pointer"
          >
            <Folder class="w-16px h-16px mr-8px text-[#396FFA]" />
            {{ t('extraction.all') }}
          </button>
        </div>

        <!-- Template List -->
        <div class="template-list-scroll flex-1 overflow-y-auto pb-16px">
          <!-- Pinned Section -->
          <div v-if="pinnedTemplates.length > 0">
            <div
              class="template-section-title flex items-center justify-between text-12px text-[#888C94] mb-8px cursor-pointer"
              @click="pinnedTemplateExpanded = !pinnedTemplateExpanded"
            >
              <span class="flex items-center">
                <Pin class="w-16px h-16px mr-4px" />
                {{ t('extraction.pinned') }}
              </span>
              <Down v-if="!pinnedTemplateExpanded" class="w-7px h-5px text-[#B7BABF]" />
              <Up v-else class="w-7px h-5px text-[#B7BABF]" />
            </div>
            <div v-show="pinnedTemplateExpanded">
              <div
                v-for="item in pinnedTemplates"
                :key="item.groupTemplateId"
                @click="changeActiveTemp(item)"
                :class="activeTemp === item.groupTemplateId && 'is-active'"
                class="template-list-row relative flex items-center justify-between rounded-4px cursor-pointer text-14px text-[#0C131F] hover:bg-[#F6F6FB] mb-8px"
              >
                <div class="flex items-center flex-1 min-w-0">
                  <FileTemplate class="w-16px h-16px mr-8px flex-shrink-0" />
                  <span class="truncate">{{ item.templateName }}</span>
                </div>
                <div class="flex items-center flex-shrink-0">
                  <Pin class="w-16px h-16px text-[#396FFA] mr-4px" />
                  <el-dropdown trigger="click" @command="(cmd) => handleTemplateAction(cmd, item)">
                    <MoreVert class="w-16px h-16px text-[#888C94] cursor-pointer hover:text-[#0C131F]" />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="unpin">{{ t('extraction.unpin') }}</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </div>

          <!-- Custom Templates Section -->
          <div v-if="customTemplates.length > 0">
            <div
              class="template-section-title flex items-center justify-between text-12px text-[#888C94] mb-8px cursor-pointer"
              @click="customTemplateExpanded = !customTemplateExpanded"
            >
              <span>{{ t('extraction.customTemplates') }}</span>
              <Down v-if="!customTemplateExpanded" class="w-7px h-5px text-[#B7BABF]" />
              <Up v-else class="w-7px h-5px text-[#B7BABF]" />
            </div>
            <div v-show="customTemplateExpanded">
              <div
                v-for="item in customTemplates"
                :key="item.groupTemplateId"
                @click="changeActiveTemp(item)"
                :class="activeTemp === item.groupTemplateId && 'is-active'"
                class="template-list-row flex items-center justify-between rounded-4px cursor-pointer text-14px text-[#0C131F] hover:bg-[#F6F6FB] mb-8px"
              >
                <div class="flex items-center flex-1 min-w-0">
                  <FileTemplate class="w-16px h-16px mr-8px flex-shrink-0" />
                  <span class="truncate">{{ item.templateName }}</span>
                </div>
                <div class="flex items-center flex-shrink-0">
                  <el-dropdown trigger="click" @command="(cmd) => handleTemplateAction(cmd, item)">
                    <MoreVert class="w-16px h-16px text-[#888C94] cursor-pointer hover:text-[#0C131F]" />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="pin">{{ t('extraction.pin') }}</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </div>

          <!-- Default Template Section -->
          <div v-if="defaultTemplates.length > 0">
            <div
              class="template-section-title flex items-center justify-between text-12px text-[#888C94] mb-8px cursor-pointer"
              @click="defaultTemplateExpanded = !defaultTemplateExpanded"
            >
              <span>{{ t('extraction.defaultTemplate') }}</span>
              <Down v-if="!defaultTemplateExpanded" class="w-7px h-5px text-[#B7BABF]" />
              <Up v-else class="w-7px h-5px text-[#B7BABF]" />
            </div>
            <div v-show="defaultTemplateExpanded">
              <div
                v-for="item in defaultTemplates"
                :key="item.groupTemplateId"
                @click="changeActiveTemp(item)"
                :class="activeTemp === item.groupTemplateId && 'is-active'"
                class="template-list-row flex items-center justify-between rounded-4px cursor-pointer text-14px text-[#0C131F] hover:bg-[#F6F6FB] mb-8px"
              >
                <div class="flex items-center flex-1 min-w-0">
                  <FileTemplate class="w-16px h-16px mr-8px flex-shrink-0" />
                  <span class="truncate">{{ item.templateName }}</span>
                </div>
                <div class="flex items-center flex-shrink-0">
                  <el-dropdown trigger="click" @command="(cmd) => handleTemplateAction(cmd, item)">
                    <MoreVert class="w-16px h-16px text-[#888C94] cursor-pointer hover:text-[#0C131F]" />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="pin">{{ t('extraction.pin') }}</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
    <!-- 右侧文件列表区域 - Empty State -->
    <div v-show="templateGroupLoaded && templateList.length === 0" class="document-list-shell">
      <section class="document-list-card">
        <div class="table-empty">
          <img src="/images/docsList.png" width="120" height="120" alt="Empty">
          <div>{{ t('extraction.empty') }}</div>
          <button type="button" @click="selectDialogVisible = true">{{ t('extraction.select') }}</button>
        </div>
      </section>
    </div>

    <!-- 右侧文件列表区域 - Main -->
    <div v-show="templateList.length > 0" class="document-list-shell">
      <section class="document-list-card">
        <div class="document-list-card__title">{{ t('extraction.list') }}</div>

        <div class="document-list-toolbar">
          <div class="toolbar-left">
            <label class="compact-search">
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
              <input v-model="searchQueryFile" type="text" :placeholder="t('extraction.searchFile')" @keyup.enter="getTemplateFileList()">
            </label>
            <button class="icon-filter" type="button" @click="filtersVisible = !filtersVisible" :class="filtersVisible && 'is-active'">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M2.5 3.5h11L9.5 8v4l-3 1V8L2.5 3.5Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/></svg>
            </button>
          </div>
          <div class="toolbar-right">
            <button v-if="activeTemp !== 'all'" v-permission="'extract:upload'" class="primary-btn" type="button" @click="uploadDialogVisible = true">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 10.5V2.75M4.75 6 8 2.75 11.25 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 10.25v2.5h10v-2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
              {{ t('extraction.upload') }}
            </button>
            <el-dropdown v-if="selectFiles.length > 0" trigger="click" @command="handleBatchAction">
              <button class="outline-btn" type="button">
                {{ t('extraction.batchActions') }}
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="m4 6 4 4 4-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="restart">{{ t('extraction.batchRestart') }}</el-dropdown-item>
                  <el-dropdown-item command="export">{{ t('extraction.batchExport') }}</el-dropdown-item>
                  <el-dropdown-item command="delete">{{ t('extraction.batchDelete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div v-show="filtersVisible" class="filter-panel">
          <div class="filter-grid">
            <label class="filter-input">
              <input v-model="searchQueryFile" type="text" :placeholder="t('extraction.searchFile')" @keyup.enter="getTemplateFileList()">
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
            </label>
            <el-select v-model="extractionStatusFilterValue" multiple collapse-tags clearable :placeholder="t('extraction.extractionStatus')" class="figma-select" @change="getTemplateFileList()">
              <el-option :label="translateStatus('pendingExtraction', 'Pending Extraction')" value="0" />
              <el-option :label="translateStatus('extracting', 'Extracting')" value="1" />
              <el-option :label="translateStatus('extractionSuccess', 'Extraction Success')" value="2" />
              <el-option :label="translateStatus('extractionFailed', 'Extraction Failed')" value="3" />
              <el-option :label="translateStatus('paused', 'Paused')" value="5" />
              <el-option :label="translateStatus('pendingClassification', 'Pending Classification')" value="6" />
              <el-option :label="translateStatus('classifying', 'Classifying')" value="7" />
              <el-option :label="translateStatus('classificationFailed', 'Classification Failed')" value="8" />
              <el-option :label="translateStatus('pendingExtraction', 'Pending Extraction')" value="9" />
              <el-option :label="translateStatus('extracting', 'Extracting')" value="10" />
              <el-option :label="translateStatus('extractionSuccess', 'Extraction Success')" value="11" />
              <el-option :label="translateStatus('extractionFailed', 'Extraction Failed')" value="12" />
            </el-select>
            <el-select v-model="reviewStatusFilterValue" clearable :placeholder="t('extraction.reviewStatus')" class="figma-select" @change="getTemplateFileList()">
              <el-option :label="t('extraction.confirmed')" value="1" />
              <el-option :label="t('extraction.unconfirmed')" value="0" />
            </el-select>
            <el-popover v-model:visible="timeFilter" placement="bottom" popper-class="dateTip" trigger="click" append-to-body>
              <template #reference>
                <button class="filter-input filter-date" type="button">
                  <span>{{ (singleDate || doubleDate.length) ? (singleDate || `${doubleDate[0]} ~ ${doubleDate[1]}`.replace(/-/g, '/')) : t('extraction.selectDate') }}</span>
                  <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M3 3.5h10v10H3v-10Z" stroke="currentColor" stroke-width="1.3"/><path d="M5 2.5v2M11 2.5v2M3 6h10" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
                </button>
              </template>
              <div class="date">
                <div class="date-title">{{ $t('extraction.filter') }}</div>
                <div class="tag-content">
                  <div @click="change('less')" :class="dateType === 'less' && 'active'" class="date-tag">{{ $t('extraction.earlierThan') }}</div>
                  <div @click="change('more')" :class="dateType === 'more' && 'active'" class="date-tag">{{ $t('extraction.laterThan') }}</div>
                  <div @click="change('equal')" :class="dateType === 'equal' && 'active'" class="date-tag">{{ $t('extraction.equalTo') }}</div>
                  <div @click="change('between')" :class="dateType === 'between' && 'active'" class="date-tag">{{ $t('extraction.between') }}</div>
                </div>
                <div class="select">
                  {{ $t('extraction.date') }}
                  <div class="input" @click="handleClick">
                    {{ (singleDate || doubleDate.length) ? (singleDate || `${doubleDate[0]} ~ ${doubleDate[1]}`.replace(/-/g, '/')) : $t('extraction.selectDate') }}
                    <Arrow class="transform -rotate-90" />
                  </div>
                </div>
                <Calender @checkedDate="checkedDate" :userFirstLogin="userFirstLogin" v-show="double" />
                <SingleCalendar @singleCheckedDate="singleCheckedDate" :userFirstLogin="userFirstLogin" v-show="single" />
                <div class="bottom">
                  <div @click="checkDate" class="ok">{{ $t('extraction.ok') }}</div>
                  <div @click="singleDate = '', doubleDate = [], timeFilter = false, getTemplateFileList()" class="clear">{{ t('template.reset') }}</div>
                </div>
              </div>
            </el-popover>
            <el-select v-model="typeFilterValue" clearable :placeholder="t('extraction.type')" class="figma-select" @change="getTemplateFileList()">
              <el-option v-for="temp in templateList" :key="temp.groupTemplateId" :label="temp.templateName" :value="temp.groupTemplateId" />
            </el-select>
          </div>
          <div class="filter-actions">
            <button class="search-btn" type="button" @click="getTemplateFileList()">{{ t('extraction.query') }}</button>
            <button class="reset-btn" type="button" @click="resetFilters">{{ t('template.reset') }}</button>
          </div>
        </div>

        <div class="figma-table-wrap">
          <el-table ref="tableRef" :data="dataList" class="figma-table" @selection-change="handleSelectionChange" :row-key="rowKey" :row-class-name="rowClassName">
            <el-table-column type="selection" width="46" align="center" />
            <el-table-column :label="t('extraction.fileName')" min-width="180">
              <template #default="scope">
                <div class="file-cell" @click="previewFile(scope.row)">
                  <svg viewBox="0 0 12 14.6667" fill="none" aria-hidden="true"><path d="M0 0H8.27614L12 3.72386V14.6667H0V0ZM10.3905 4L8 1.60948V4H10.3905ZM6.66667 1.33333H1.33333V13.3333H10.6667V5.33333H6.66667V1.33333ZM2.66667 7.33333H9.33333V8.66667H2.66667V7.33333ZM2.66667 10H9.33333V11.3333H2.66667V10Z" fill="currentColor" fill-opacity="0.4"/></svg>
                  <span>{{ scope.row.fileName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('extraction.type')" min-width="150">
              <template #default="scope">
                <el-select @change="changeTemp(scope.row.fileId, scope.row.groupTemplateId)" v-model="scope.row.groupTemplateId" class="figma-select table-type-select" :disabled="[1, 7, 10].includes(scope.row.status)" :placeholder="t('extraction.unclassified')">
                  <el-option v-for="temp in templateList" :key="temp.groupTemplateId" :label="['Order', 'Invoice'].includes(temp.templateName) ? t(`extraction.${temp.templateName.toLowerCase()}`) : temp.templateName" :value="temp.groupTemplateId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="t('extraction.extractionStatus')" min-width="220">
              <template #default="scope">
                <span class="status-tag" :class="getExtractionStatusClass(scope.row.status)">{{ getExtractionStatusTxt(scope.row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('extraction.reviewStatus')" min-width="140">
              <template #default="scope">
                <span class="status-tag" :class="getReviewStatusClass(scope.row.reviewStatus, scope.row.status)">{{ getReviewStatusTxt(scope.row.reviewStatus, scope.row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('extraction.time')" min-width="180">
              <template #default="scope">
                <span class="upload-time">{{ dayjs.utc(scope.row.uploadTime).local().format('DD/MM/YYYY HH:mm:ss') }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('extraction.action')" width="160" align="left">
              <template #default="scope">
                <div class="row-actions">
                  <button type="button" title="Preview" @click="previewFile(scope.row)">
                    <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path fill-rule="evenodd" clip-rule="evenodd" d="M1.42716 8C1.50002 8.12591 1.59662 8.28637 1.71628 8.47165C2.02362 8.94754 2.47878 9.5804 3.06969 10.2107C4.26368 11.4843 5.933 12.6667 8 12.6667C10.067 12.6667 11.7363 11.4843 12.9303 10.2107C13.5212 9.5804 13.9764 8.94754 14.2837 8.47165C14.4034 8.28637 14.5 8.12591 14.5728 8C14.5 7.87409 14.4034 7.71363 14.2837 7.52835C13.9764 7.05246 13.5212 6.4196 12.9303 5.78929C11.7363 4.51571 10.067 3.33333 8 3.33333C5.933 3.33333 4.26368 4.51571 3.06969 5.78929C2.47878 6.4196 2.02362 7.05246 1.71628 7.52835C1.59662 7.71363 1.50002 7.87409 1.42716 8ZM15.3333 8C15.9296 7.70186 15.9294 7.70139 15.9294 7.70139L15.9283 7.69925L15.926 7.69469L15.9184 7.67988C15.9121 7.66752 15.9031 7.65021 15.8915 7.62829C15.8683 7.58447 15.8346 7.52215 15.7907 7.44399C15.7028 7.28776 15.5734 7.06768 15.4038 6.80498C15.0653 6.28088 14.5621 5.5804 13.903 4.87737C12.597 3.48429 10.5997 2 8 2C5.40033 2 3.40299 3.48429 2.09698 4.87737C1.43789 5.5804 0.93471 6.28088 0.596224 6.80498C0.426567 7.06768 0.297195 7.28776 0.209314 7.44399C0.165349 7.52215 0.131693 7.58447 0.108502 7.62829C0.0969045 7.65021 0.0879168 7.66752 0.0815586 7.67988L0.0739933 7.69469L0.0716926 7.69925L0.0709134 7.7008C0.0709134 7.7008 0.0703819 7.70186 0.666667 8L0.0703819 7.70186C-0.0234606 7.88954 -0.0234606 8.11046 0.0703819 8.29814L0.666667 8C0.0703819 8.29814 0.0703819 8.29814 0.0703819 8.29814L0.0716926 8.30075L0.0739933 8.30531L0.0815586 8.32012C0.0879168 8.33248 0.0969045 8.34979 0.108502 8.37171C0.131693 8.41553 0.165349 8.47785 0.209314 8.55601C0.297195 8.71224 0.426567 8.93232 0.596224 9.19502C0.93471 9.71913 1.43789 10.4196 2.09698 11.1226C3.40299 12.5157 5.40033 14 8 14C10.5997 14 12.597 12.5157 13.903 11.1226C14.5621 10.4196 15.0653 9.71913 15.4038 9.19502C15.5734 8.93232 15.7028 8.71224 15.7907 8.55601C0.8346 8.47785 15.8683 8.41553 15.8915 8.37171C15.9031 8.34979 15.9121 8.33248 15.9184 8.32012L15.926 8.30531L15.9283 8.30075L15.9294 8.29814C15.9294 8.29814 15.9296 8.29814 15.3333 8ZM8 10.6667C9.47276 10.6667 10.6667 9.47276 10.6667 8C10.6667 6.52724 9.47276 5.33333 8 5.33333C6.52724 5.33333 5.33333 6.52724 5.33333 8C5.33333 9.47276 6.52724 10.6667 8 10.6667Z" fill="currentColor"/></svg>
                  </button>
                  <button v-if="[2, 3, 11, 12].includes(scope.row.status)" type="button" title="Restart" @click="selectFile = scope.row, startExtraction()">
                    <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M13.7143 8C13.7143 11.1559 11.1559 13.7143 8 13.7143C6.11269 13.7143 4.43918 12.7996 3.39916 11.388" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/><path d="M2.28571 8C2.28571 4.84409 4.84409 2.28571 8 2.28571C9.88731 2.28571 11.5608 3.20043 12.6008 4.612" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/><path d="M12 1.71429L13.7143 4.57143H10.2857" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M4 14.2857L2.28571 11.4286H5.71429" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                  <button v-permission="'extract:delete'" type="button" title="Delete" @click="deleteFile([scope.row.fileId])">
                    <svg viewBox="0 0 13.3333 14.6667" fill="none" aria-hidden="true"><path d="M3.66667 0H9.66667V2H13.3333V3.33333H11.9807L11.6473 14.6667H1.68599L1.35265 3.33333H0V2H3.66667V0ZM5 2H8.33333V1.33333H5V2ZM2.68656 3.33333L2.98068 13.3333H10.3527L10.6468 3.33333H2.68656ZM7.33333 4.66667V12H6V4.66667H7.33333Z" fill="currentColor"/></svg>
                  </button>
                </div>
              </template>
            </el-table-column>
            <template #empty>
              <div class="table-empty">
                <img v-if="searchQueryFile || extractionStatusFilterValue.length || reviewStatusFilterValue.length || singleDate || doubleDate.length" src="/images/search-empty.png" width="120" height="120" alt="Empty">
                <img v-else src="/images/docsList.png" width="120" height="120" alt="Empty">
                <div>{{ searchQueryFile || extractionStatusFilterValue.length || reviewStatusFilterValue.length || singleDate || doubleDate.length ? t('extraction.searchEmpty') : t('extraction.noDocument') }}</div>
              </div>
            </template>
          </el-table>
        </div>

        <div class="pagination-row">
          <div class="total-text">{{ t('common.totalItems', { total }) }}</div>
          <el-pagination
            class="figma-pagination"
            background
            :total="total"
            :page-size="pageSize"
            :page-sizes="pageSizes"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            layout="sizes, prev, pager, next, jumper, slot"
          >
            <button class="editPagesize" @click="pageSizesSettingDialogVisible = !pageSizesSettingDialogVisible"><Edit /></button>
          </el-pagination>
        </div>
      </section>
    </div>

    <!-- 设置自定义页码弹窗 -->
    <el-dialog v-model="pageSizesSettingDialogVisible" :title="t('extraction.pageSizesTitle')" align-center width="520px" :show-close="true" modal-class="custom-pagesize-modal" header-class="custom-pagesize">
      <el-input-number
        v-model="customPageSize"
        :controls="false"
        :disabled-scientific="true"
        align="left"
        :precision="0"
        :style="{
          width: '100%'
        }"
        :placeholder="t('logs.pagination.items_per_page')"
      />
      <template #footer>
        <div class="dialog-footer" :style="{ marginTop: '8px', textAlign: 'center' }">
          <el-button size="large" @click="pageSizesSettingDialogVisible = false">{{ t('extraction.cancel') }}</el-button>
          <el-button size="large" type="primary" @click="updatePageSizes">{{ t('extraction.ok') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 上传文件 -->
    <el-dialog v-model="uploadDialogVisible" align-center width="520px" :show-close="false" @closed="uploadActiveTab = 'local'">
      <!-- Title -->
      <h3 class="text-sm font-600 text-[#43474D] py-4px mb-16px">
        {{ t('extraction.upload') }}
      </h3>

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
            {{ t('extraction.selectFile[0]') }}
          </div>
          <div class="mt-12px text-xs text-[#8C8C8C]">{{ t('extraction.selectFile[1]') }}</div>
          <div class="text-xs text-[#8C8C8C]">{{ t('extraction.selectFile[2]') }}</div>
          <!-- Supported formats bar -->
          <div class="mt-12px rounded-6px bg-[#F6F6FB] px-12px py-8px text-xs text-[#8C8C8C] absolute bottom-0px left-0 rounded-10px w-full text-center">
            {{ t('extraction.support') }}
          </div>
        </div>
        <input ref="input" class="hidden" type="file" name="file" accept=".png,.jpg,.jpeg,.tiff,.bmp,.pdf,.doc,.docx,.xls,.xlsx,.csv,.ppt,.pptx,.txt" multiple @change="handleChange">

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
          <!-- Folder tree -->
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
          <!-- Selected files panel -->
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
            <!-- Unauthorized state -->
            <template v-if="!thirdPartyAuthorized">
              <div class="border border-[#E2E3E5] rounded-6px flex-1 flex flex-col items-center justify-center p-12px">
                <img src="/images/unAuthorization.png" alt="UnAuthorization" width="64" height="64">
                <div class="text-sm font-600 text-[#43474D] mt-12px mb-8px">{{ t('dms.team_space.upload.third_party.authorization_required.title') }}</div>
                <div class="text-xs text-[#8C8C8C] text-center mb-16px">
                  {{ t('dms.team_space.upload.third_party.authorization_required.description') }}
                </div>
                <div
                  v-if="['manager', 'admin'].includes(store.role)"
                  @click="authorizeThirdParty"
                  class="rounded-6px bg-[#396FFA] text-white text-sm font-500 py-8px px-16px cursor-pointer hover:bg-[#244FF0]"
                >
                  {{ t('dms.team_space.upload.third_party.authorization_required.authorize_now') }}
                </div>
              </div>
            </template>
            <!-- Authorized: file tree -->
            <template v-else>
              <div class="flex-1 flex flex-col border border-[#E2E3E5] rounded-6px overflow-auto max-h-329px">
                <!-- Loading -->
                <div v-if="thirdPartyDialogLoading" v-loading="true" class="flex-1 min-h-100px" />
                <!-- File tree -->
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
              <!-- Selected files panel -->
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
          {{ t('extraction.cancel') }}
        </div>
        <div
          v-loading="loading"
          @click="handleUpload"
          :class="isUploadEnabled ? 'hover:bg-[#244FF0] cursor-pointer bg-[#396FFA]' : 'bg-[#88a9fc] cursor-not-allowed'"
          class="w-140px rounded-6px font-500 text-white text-sm py-8px px-10px flex items-center justify-center ml-12px"
        >
          {{ t('extraction.ok') }}
        </div>
      </div>
    </el-dialog>

    <!-- 选择模版 -->
    <el-dialog v-model="selectDialogVisible" align-center width="80%" class="selectTemp">
      <div class="sticky top-0 bg-white z-2">
        <h3 class="text-20px leading-28px font-600 text-[#0C131F]">{{ t('extraction.select') }}</h3>
        <div class="flex justify-between py-24px">
          <el-input class="max-w-300px" v-model="searchQueryTemp" clearable @clear="getTemplateList" @input="getTemplateList" :placeholder="t('extraction.search')">
            <template #prefix>
              <Search />
            </template>
          </el-input>
          <div v-permission="'extract:template:create'" @click="startCreateTemplate" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-16px w-fit flex items-center justify-center font-500 hover:bg-[#244FF0]">
            <AddTemplateBtn class="mr-4px" />{{ t('extraction.add') }}
          </div>
        </div>
      </div>
      <div class="max-h-70vh overflow-auto">
        <!-- 默认模版 -->
        <div class="flex flex-wrap">
          <div @click.stop="toggleSelect(item.id)" v-for="(item, index) in defaultTemplateList" :key="index" class="card cursor-pointer relative bg-white p-20px border border-transparent rounded-8px"
            :class="[index > 3 && 'mt-24px', (index + 1) % 4 !== 0 && 'mr-24px']">
            <div class="flex items-center justify-between">
              <div class="text-brand-0 text-sm font-600 flex items-center">
                <Check
                  v-show="!selectTemp.includes(item.id)"
                  class="mr-4px cursor-pointer"
                />
                <Checked
                  v-show="selectTemp.includes(item.id)"
                  class="mr-4px cursor-pointer"
                  :class="sourceTemp?.includes(item.id) ? 'opacity-50' : ''"
                />
                {{ ['invoice', 'order'].includes(item.name.toLowerCase()) ? t(`extraction.${item.name.toLowerCase()}`) : item.name }}
              </div>
              <Action @click.stop.prevent="defaultStatusArr[index].status = !defaultStatusArr[index].status" class="action" :class="defaultStatusArr[index].status && 'active'" />
            </div>
            <img v-if="item.name.toLowerCase() === 'order'" src="/images/order.png" alt="Empty" class="my-12px">
            <img v-else-if="item.name.toLowerCase() === 'invoice'" src="/images/invoice.png" alt="Empty" class="my-12px">
            <img v-else src="/images/custom.png" alt="Empty" class="my-12px">
            <div class="flex items-center justify-between">
              <div class="border border-[#244FF04D] w-fit bg-[#D7E2FE] rounded-64px py-4px px-8px text-12px leading-16px text-[#618CFB]">
                {{ t('extraction.default') }}
              </div>
            </div>
            <div v-show="defaultStatusArr[index].status" class="shadows absolute top-54px right-20px p-4px bg-white rounded-4px min-w-84px">
              <div v-permission="'extract:template:modify'" @click="changeActive('result', undefined, item)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                {{ t('extraction.edit') }}
              </div>
            </div>
          </div>
        </div>
        <div v-show="customTemplateList.length" class="dash-box my-24px"></div>
        <!-- 自定义模版 -->
        <div class="flex flex-wrap">
          <div @click.stop="toggleSelect(item.id)" v-for="(item, index) in customTemplateList" :key="item.id" class="card cursor-pointer relative bg-white p-20px border border-transparent rounded-8px"
            :class="[index > 3 && 'mt-24px', (index + 1) % 4 !== 0 && 'mr-24px']">
            <div class="flex items-center justify-between">
              <div class="text-brand-0 text-sm font-600 truncate flex items-center">
                <Check v-show="!selectTemp.includes(item.id)" class="mr-4px min-w-24px" />
                <Checked v-show="selectTemp.includes(item.id)" class="mr-4px min-w-24px" :class="sourceTemp?.includes(item.id) ? 'opacity-50' : ''" />
                <span class="truncate max-w-150px">
                  <el-tooltip popper-class="tip-item" effect="dark" :content="item.name" placement="top">
                    {{ item.name }}
                  </el-tooltip>
                </span>
              </div>
              <Action @click.stop.prevent="customStatusArr[index].status = !customStatusArr[index].status" class="action" :class="customStatusArr[index].status && 'active'" />
            </div>
            <img src="/images/custom.png" alt="Empty" class="my-12px">
            <div class="flex items-center justify-between">
              <div class="border border-[#00CF854D] w-fit bg-[#E2F7EF] rounded-64px p-4px text-12px leading-16px text-[#00A66A]">
                {{ t('extraction.custom') }}
              </div>
            </div>
            <div v-show="customStatusArr[index].status" class="shadows absolute top-54px right-20px p-4px bg-white rounded-4px min-w-84px">
              <div v-permission="'extract:template:modify'" @click.stop="changeActive('result', undefined, item)" class="py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                {{ t('extraction.edit') }}
              </div>
              <div v-permission="'extract:template:delete'" @click.stop="deleteTemplate(item.id)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                {{ t('extraction.delete') }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex justify-center pt-24px sticky -bottom-0 pb-20px bg-white">
        <div class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])" @click="selectDialogVisible = false">
          {{ t('extraction.cancel') }}
        </div>
        <div v-loading="loading" @click="saveTemplateGroup" :class="selectTemp?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
          class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
          {{ t('extraction.save') }}
        </div>
      </div>
    </el-dialog>

    <!-- 文件导出 -->
    <el-dialog v-model="exportDialogVisible" align-center width="320px">
      <h2 class="text-[#0C131F] text-sm font-600 mb-24px">{{ t('extraction.batchExport') }}</h2>
      <h3 class="text-sm font-500 text-[#404653] pb-8px">
        {{ t('extraction.methods') }}
      </h3>
      <el-radio-group v-model="exportMethod" class="flex !flex-col">
        <el-radio value="each" :label="t('extraction.each')" />
        <el-radio value="single" :label="t('extraction.single')" />
      </el-radio-group>
      <h3 class="text-sm font-500 text-[#404653] pb-8px mt-24px">
        {{ t('extraction.selectFormat') }}
      </h3>
      <el-radio-group v-model="format">
        <el-radio value="json" label="JSON" />
        <el-radio value="excel" label="Excel" />
        <el-radio value="csv" label="CSV" />
      </el-radio-group>
      <div class="flex justify-end mt-24px">
        <div class="w-fit rounded-6px cursor-pointer border-1 border-[#E2E3E5] text-xs text-[#0C131F] py-6px px-12px flex items-center justify-center hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])" @click="exportDialogVisible = false">
          {{ t('extraction.cancel') }}
        </div>
        <div v-loading="loading" @click="exportFile()" class="w-fit rounded-6px text-white bg-[#396FFA] text-xs py-6px px-12px flex items-center justify-center ml-8px hover:bg-[#244FF0] cursor-pointer">
          {{ t('extraction.ok') }}
        </div>
      </div>
    </el-dialog>
    <TemplateSettingsDialog
      :visible="templateSettingVisible"
      :create-on-open="templateCreateOnOpen"
      :initial-template-ids="templateSettingsTargetIds"
      @close="closeTemplateSettings"
    />
    <ApiKeyRequiredDialog v-model="apiKeyDialogVisible" />
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { saveAs } from 'file-saver'
import Cookies from "js-cookie"
import request, { post, get } from '../../utils/request'
import Upload from '../images/SplittingUpload.vue'
import { getSystemBaseUnit } from '../../utils/tools'
import { ElMessageBox, ElMessage } from 'element-plus'
import { inject, onMounted, onUnmounted, ref, watch, nextTick, defineAsyncComponent, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUploadTaskStore } from '../../stores/uploadTask'
import { useStore } from '../../stores'
import { usePermissionStore } from '../../stores/permission'
import ApiKeyRequiredDialog from '../ApiKeyRequiredDialog.vue'
const Calender = defineAsyncComponent(() => import('../calendar/calendar.vue'))
const SingleCalendar = defineAsyncComponent(() => import('../calendar/singleCalendar.vue'))
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import Edit from '../images/Edit.vue'

// 切换选中状态
const toggleSelect = (id: string) => {
  const idx = selectTemp.value.indexOf(id)
  if (idx === -1) {
    selectTemp.value.push(id)
  } else {
    if (sourceTemp.value?.includes(id)) return
    selectTemp.value.splice(idx, 1)
  }
}

interface TemplateList {
  groupId: string
  groupTemplateId: string
  templateId: string
  templateName: string
  leaderId?: string
  pinned?: boolean
  pinnedTime?: string | null
  order?: number
}
const changeActive = inject('changeActive', (_val: string, _row?: FileData, _template?: TemplateList, _add?: boolean, _isConfigResult?: boolean) => {})
const input = ref()
const apiKeyDialogVisible = ref(false)
const tableRef = ref()
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const store = useStore()
const permissionStore = usePermissionStore()
const canEditTemplate = computed(() => !permissionStore.initialized || permissionStore.hasPermission('extract:template:modify'))
const dataList = ref<FileData[]>([])
const groupTemplateId = ref<string>('')
const templateList = ref<TemplateList[]>([])
const pinnedTemplatesList = ref<TemplateList[]>([])
const customTemplatesList = ref<TemplateList[]>([])
const defaultTemplatesList = ref<TemplateList[]>([])
const templateGroupLoaded = ref(false)
const loading = ref(false)
const highlightFileId = ref<string>('')

const rowClassName = ({ row }: { row: FileData }) => {
  return row.fileId === highlightFileId.value ? 'row-highlight-flash' : ''
}
const dragover = ref(false)
const searchQueryFile = ref('')
const searchQueryTemp = ref('')
const activeTemp = ref('all')
const format = ref<string>('json')
const exportMethod = ref<string>('each')
const selectFiles = ref<FileData[]>([])
const fileList = ref<File[]>([])
const exportDialogVisible = ref(false)
const selectDialogVisible = ref(false)
const groupId = ref<string>('')
const customPageSize = ref(0)

interface TemplateData {
  keys: string[]
  tableHeaders: string[]
  id: string
  name: string
  fileId: string
}
interface status {
 status: boolean
}
const customTemplateList = ref<TemplateData[]>([])
const defaultTemplateList = ref<TemplateData[]>([])
const uploadDialogVisible = ref(false)
const uploadTaskStore = useUploadTaskStore()
const defaultTemplateExpanded = ref(false)
const pinnedTemplateExpanded = ref(true)
const customTemplateExpanded = ref(true)
const templateSettingVisible = ref(false)
const templateCreateOnOpen = ref(false)
const templateSettingsTargetIds = ref<string[]>([])
const filtersVisible = ref(false)

// ── Upload dialog tab state ──
type UploadTab = 'local' | 'team' | 'thirdParty' | 'scanner'
const uploadActiveTab = ref<UploadTab>('local')

import Pin from '../images/Pin.vue'
import MoreVert from '../images/MoreVert.vue'
import TemplateSetting from '../images/TemplateSetting.vue'
import TemplateSettingsDialog from './TemplateSettingsDialog.vue'

import FileTemplate from '../images/FileTemplate.vue'
import Down from '../images/Down.vue'
import Up from '../images/Up.vue'

const startCreateTemplate = () => {
  selectDialogVisible.value = false
  templateCreateOnOpen.value = true
  templateSettingVisible.value = true
}

const openTemplateSettings = () => {
  templateCreateOnOpen.value = false
  templateSettingVisible.value = true
}

const closeTemplateSettings = async () => {
  templateSettingVisible.value = false
  templateCreateOnOpen.value = false
  templateSettingsTargetIds.value = []
  await refreshTemplateGroup()
}

const openTemplateSettingsFromRoute = async () => {
  if (route.query.templateSettings !== '1') return
  const targetIds = [
    route.query.groupTemplateId,
    route.query.templateId,
    route.query.templateName
  ].map(item => String(item || '').trim()).filter(Boolean)
  templateCreateOnOpen.value = false
  templateSettingsTargetIds.value = targetIds
  templateSettingVisible.value = true
  const nextQuery = { ...route.query }
  delete nextQuery.templateSettings
  delete nextQuery.groupTemplateId
  delete nextQuery.templateId
  delete nextQuery.templateName
  await router.replace({ path: route.path, query: nextQuery })
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
// teamSelectedFiles 存储 { id, name } 以支持反向同步左侧状态
const teamSelectedFiles = ref<{ id: string; name: string }[]>([])

const updatePageSizes = () => {
  if (!customPageSize.value) return
  Cookies.set('pageSize', customPageSize.value.toString())
  // 在索引 3 处追加为第 4 档，保留原有 [5, 10, 20] 不变
  pageSizes.value[3] = customPageSize.value
  pageSizesSettingDialogVisible.value = false
  handleSizeChange(customPageSize.value)
}
const pageSizesSettingDialogVisible = ref(false)

// 更新父节点的 checked / indeterminate 状态
const updateFolderCheckState = (node: FolderNode) => {
  if (!node.children || node.children.length === 0) return
  const allChecked = node.children.every(c => c.checked)
  const noneChecked = node.children.every(c => !c.checked)
  node.checked = allChecked
  node.indeterminate = !allChecked && !noneChecked
}

// 将子文件同步进 teamSelectedFiles（去重）
const addFilesToSelected = (files: FolderNode[]) => {
  files.forEach(f => {
    if (!teamSelectedFiles.value.some(s => s.id === f.id)) {
      teamSelectedFiles.value.push({ id: f.id, name: f.name })
    }
  })
}

// 从 teamSelectedFiles 中移除一批文件
const removeFilesFromSelected = (ids: string[]) => {
  teamSelectedFiles.value = teamSelectedFiles.value.filter(s => !ids.includes(s.id))
}

// 展开/折叠文件夹，展开时懒加载子文件并恢复选中状态
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

// 勾选/取消文件夹：全选/取消全选其下所有子文件
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

// 勾选/取消单个子文件，并同步更新父节点半选状态与右侧列表
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

// 右侧列表删除单个文件，反向同步左侧 checkbox 状态
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

// 清空所有已选文件，同时重置左侧所有节点状态
const clearTeamSelectedFiles = () => {
  teamSelectedFiles.value = []
  folderList.value.forEach(folder => {
    folder.checked = false
    folder.indeterminate = false
    if (folder.children) folder.children.forEach(c => { c.checked = false })
  })
}

// 上传团队空间文件到 extraction
const uploadTeamSpaceFile = async () => {
  if (loading.value || !teamSelectedFiles.value.length) return
  loading.value = true
  try {
    const response = await post('/v1/team_space/file/download', {
      file_ids: teamSelectedFiles.value.map(f => f.id)
    }, {}, {
      responseType: 'blob'
    } as any)

    const contentType = (response.headers?.['content-type'] as string | undefined) ?? ''

    // 后端可能以 JSON 返回业务错误（即使 HTTP 200），需先检测
    if (contentType.includes('json')) {
      const text = response.data instanceof Blob ? await response.data.text() : JSON.stringify(response.data)
      try {
        const json = JSON.parse(text)
        ElMessage.error(json?.message || t('parsing.fail'))
      } catch {
        ElMessage.error(t('parsing.fail'))
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
        ElMessage.error(t('parsing.fail'))
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

    // zip 下载完毕 → 注册文件，立即关闭弹窗
    const realEntries: { tempId: string; file: File }[] = restoredFiles.map((file, i) => {
      const tempId = `extraction-team-${file.name}-${Date.now()}-${i}`
      uploadTaskStore.addFile({ id: tempId, name: file.name, status: 'uploading', type: 'extraction' })
      return { tempId, file }
    })
    teamSelectedFiles.value = []
    uploadDialogVisible.value = false
    loading.value = false

    // 并发上传每个文件（fire-and-forget）
    realEntries.forEach(({ tempId, file }) => {
      uploadSingleExtractionFile(file).then(result => {
        if (result.success) {
          uploadTaskStore.updateFile(tempId, { status: 'success' })
          getTemplateFileList()
        } else {
          uploadTaskStore.updateFile(tempId, { status: 'uploadFail' })
          if (result.code === 8021) {
            apiKeyDialogVisible.value = true
          } else {
            ElMessage.error(`${t('parsing.fail')}: ${file.name}`)
          }
        }
      })
    })
  } catch {
    ElMessage.error(t('parsing.fail'))
    loading.value = false
  }
}

import Google from '../images/Google.vue'
import AWS from '../images/Aws.vue'
import NAS from '../images/Nas.vue'
import Notion from '../images/Notion.vue'
import Trello from '../images/Trello.vue'
import Gmail from '../images/Gmail.vue'
import Gcs from '../images/Gcs.vue'
import JSZip from 'jszip'
import FileArrow from '../images/FileArrow.vue'
import DocFolder from '../images/DocFolder.vue'
import Docs from '../images/Docs.vue'
import Indeterminate from '../images/Indeterminate.vue'

// ── Third-party tab state ──

// /v1/dms/auth/credentials 返回的单条凭证结构
interface Credential {
  alias: string
  created_at: string
  id: number
  is_active: boolean
  last_verified_at: string
  source: string
  updated_at: string
}

// 平台列表中的 key 与 API source 字段的映射关系
const PLATFORM_SOURCE_MAP: Record<string, string> = {
  'google-drive': 'google_drive',
  'aws':          'aws_oss',
  'nas':          'nas_smb',
  'notion':       'notion',
  'trello':       'trello',
  'gmail':        'gmail',
  'gcs':          'gcs',
}

// ── DmsFileItem: 统一的第三方文件节点类型 ──
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

// 第三方平台列表
const thirdPartyPlatforms = [
  { key: 'google-drive', name: 'Google Drive', icon: Google },
  { key: 'aws',          name: 'AWS S3',        icon: AWS },
  { key: 'nas',          name: 'NAS',            icon: NAS },
  { key: 'notion',       name: 'Notion',         icon: Notion },
  { key: 'trello',       name: 'Trello',         icon: Trello },
  { key: 'gmail',        name: 'Gmail',          icon: Gmail },
  { key: 'gcs',          name: 'Google Cloud',   icon: Gcs },
]

// 以 platform.key 为键存储各平台激活状态的凭证（无凭证则为 null）
const thirdPartyCredentials = ref<Record<string, Credential | null>>(
  Object.fromEntries(thirdPartyPlatforms.map(p => [p.key, null]))
)

// 当前选中平台是否已授权（is_active 为 true 才算）
const thirdPartyAuthorized = computed<boolean>(() => {
  const cred = thirdPartyCredentials.value[selectedPlatform.value]
  return cred?.is_active === true
})

// 第三方文件树状态
const thirdPartyDialogFileList = ref<DmsFileItem[]>([])
const thirdPartyDialogCache = new Map<string, DmsFileItem[]>()
const thirdPartyDialogLoading = ref(false)

// 已选中的叶节点列表
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

// 跳转至对应平台的授权页
const PLATFORM_ROUTE_MAP: Record<string, string> = {
  'google-drive': '/third-party/google-drive-authorization',
  'aws':          '/third-party/aws-authorization',
  'nas':          '/third-party/nas-authorization',
  'notion':       '/third-party/notion-authorization',
  'trello':       '/third-party/trello-authorization',
  'gmail':        '/third-party/gmail-authorization',
  'gcs':          '/third-party/google-cloud-storage-authorization',
}

const authorizeThirdParty = () => {
  const routePath = PLATFORM_ROUTE_MAP[selectedPlatform.value]
  if (routePath) location.href = routePath
}

// 辅助：获取节点缓存 key
const getDmsItemKey = (item: DmsFileItem): string => item.path ?? item.id

// 加载根节点
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

// 将各平台原始 API 数据映射为 DmsFileItem
const mapToDmsItems = (files: any[], level: number, parentId: string | null): DmsFileItem[] => {
  const platform = selectedPlatform.value
  return files.map((f: any): DmsFileItem => {
    if (platform === 'google-drive') {
      return {
        id: String(f.id),
        name: String(f.name),
        is_dir: Boolean(f.is_dir),
        is_expandable: Boolean(f.is_dir),
        level,
        parent_id: parentId,
        expanded: false,
        selected: false,
      }
    }
    if (platform === 'aws') {
      const awsType: string = f.type ?? ''
      const isDir = ['bucket', 'folder'].includes(awsType)
      return {
        id: String(f.id || f.key || f.prefix || f.name),
        name: String(f.name),
        is_dir: isDir,
        is_expandable: isDir,
        level,
        parent_id: parentId,
        awsType,
        prefix: f.prefix || '',
        bucket_name: awsType === 'bucket' ? String(f.name) : (parentId ? undefined : ''),
        expanded: false,
        selected: false,
      }
    }
    if (platform === 'nas') {
      return {
        id: String(f.path),
        name: String(f.name),
        is_dir: Boolean(f.is_dir),
        is_expandable: Boolean(f.is_dir),
        level,
        parent_id: parentId,
        path: String(f.path),
        expanded: false,
        selected: false,
      }
    }
    if (platform === 'gcs') {
      const fileType: string = f.type ?? ''
      const isDir = ['bucket', 'folder'].includes(fileType)
      return {
        id: String(f.id || f.key || f.prefix || f.name),
        name: String(f.name),
        is_dir: isDir,
        is_expandable: isDir,
        level,
        parent_id: parentId,
        prefix: f.prefix || '',
        bucket_name: fileType === 'bucket' ? String(f.name) : undefined,
        expanded: false,
        selected: false,
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
        id: String(f?.id ?? f?.uid ?? f?.attachment_id ?? f?.name ?? f),
        name,
        is_dir: isExpandable,
        is_expandable: isExpandable,
        level,
        parent_id: parentId,
        node_type: nodeType,
        mailbox,
        uid,
        expanded: false,
        selected: false,
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
          is_dir: isDatabase,
          is_expandable: isDatabase,
          level,
          parent_id: null,
          kind,
          node_type: isDatabase ? 'database' : 'file',
          file_property_name: isDatabase ? getFilePropertyName(f?.properties) : undefined,
          expanded: false,
          selected: false,
        }
      }
      const parentItem = thirdPartyDialogFileList.value.find(x => x.id === parentId)
      const parentNodeType = parentItem?.node_type
      if (parentNodeType === 'database') {
        return {
          id: String(f?.id || f?.page_id || f?.name),
          name: String(f?.title || f?.name || f?.id || 'Untitled'),
          is_dir: true,
          is_expandable: true,
          level,
          parent_id: parentId,
          kind: String(f?.kind || 'page'),
          node_type: 'page',
          file_property_name: parentItem?.file_property_name,
          expanded: false,
          selected: false,
        }
      }
      if (parentNodeType === 'page') {
        const parentNode = thirdPartyDialogFileList.value.find(x => x.id === parentId)
        return {
          id: String(f?.id || f?.name || f?.url),
          name: String(f?.name || f?.file_name || f?.title || f?.id || 'Untitled'),
          is_dir: false,
          is_expandable: false,
          level,
          parent_id: parentId,
          kind: 'file',
          node_type: 'property_file',
          file_property_name: parentNode?.file_property_name,
          expanded: false,
          selected: false,
        }
      }
      return {
        id: String(f?.id || f?.page_id || f?.name),
        name: String(f?.title || f?.name || f?.id || 'Untitled'),
        is_dir: Boolean(f?.is_dir),
        is_expandable: Boolean(f?.is_dir),
        level,
        parent_id: parentId,
        kind: String(f?.kind || ''),
        node_type: Boolean(f?.is_dir) ? 'page_root' : 'file',
        expanded: false,
        selected: false,
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
        is_dir: isExpandable,
        is_expandable: isExpandable,
        level,
        parent_id: parentId,
        node_type: nodeType,
        expanded: false,
        selected: false,
      }
    }
    return {
      id: String(f.id || f.name),
      name: String(f.name || f.id),
      is_dir: Boolean(f.is_dir),
      is_expandable: Boolean(f.is_dir),
      level,
      parent_id: parentId,
      expanded: false,
      selected: false,
    }
  })
}

// 展开子节点：根据平台构造对应请求
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
    rawItems.forEach(child => {
      if (!child.bucket_name) child.bucket_name = item.bucket_name
    })
  }
  if (selectedPlatform.value === 'gmail' && item.node_type === 'email') {
    rawItems.forEach((child, idx) => {
      if (child.node_type === 'attachment') child.attachment_index = idx
    })
  }
  return rawItems
}

// 构造 download args
const buildDownloadArgs = (item: DmsFileItem): object => {
  switch (selectedPlatform.value) {
    case 'google-drive':
      return { file_id: item.id }
    case 'aws':
      return { mode: 'url', url: `s3://${item.bucket_name}/${item.prefix || item.id}` }
    case 'nas':
      return { path: item.path ?? item.id }
    case 'gcs':
      return { mode: 'url', url: `gs://${item.bucket_name}/${item.prefix || item.id}` }
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
    case 'trello':
      return { card_id: item.parent_id, att_id: item.id }
    default:
      return {}
  }
}

// 计算可见后代索引
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

// 折叠节点
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

// 展开节点
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

// 递归统计叶节点选中状态
const collectThirdPartyLeafStates = (item: DmsFileItem): { total: number; selected: number } => {
  if (!item.is_expandable) {
    return { total: 1, selected: item.selected ? 1 : 0 }
  }
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
  if (children.length === 0) {
    return { total: 1, selected: item.selected ? 1 : 0 }
  }
  return children.reduce((acc, child) => {
    const s = collectThirdPartyLeafStates(child)
    return { total: acc.total + s.total, selected: acc.selected + s.selected }
  }, { total: 0, selected: 0 })
}

// checkbox 状态
const getThirdPartyCheckState = (item: DmsFileItem): 'checked' | 'indeterminate' | 'unchecked' => {
  if (!item.is_expandable) {
    return item.selected ? 'checked' : 'unchecked'
  }
  const cacheKey = getDmsItemKey(item)
  const hasCache = thirdPartyDialogCache.has(cacheKey)
  if (!item.expanded && !hasCache) {
    return item.selected ? 'checked' : 'unchecked'
  }
  const { total, selected } = collectThirdPartyLeafStates(item)
  if (total === 0) return item.selected ? 'checked' : 'unchecked'
  if (selected === total) return 'checked'
  if (selected > 0) return 'indeterminate'
  return 'unchecked'
}

// 同步祖先节点选中状态
const syncThirdPartyAncestors = (fromIndex: number) => {
  const list = thirdPartyDialogFileList.value
  for (let i = fromIndex - 1; i >= 0; i--) {
    if (!list[i].expanded) continue
    const childIndices = getThirdPartyVisibleDescendantIndices(i)
    if (!childIndices.includes(fromIndex)) continue
    list[i].selected = childIndices.every(j => list[j].selected)
  }
}

// 切换选中
const toggleThirdPartySelect = (item: DmsFileItem) => {
  const idx = thirdPartyDialogFileList.value.indexOf(item)
  if (idx === -1) return
  const currentState = getThirdPartyCheckState(item)
  const newSelected = currentState !== 'checked'
  thirdPartyDialogFileList.value[idx].selected = newSelected
  const descendantIndices = getThirdPartyVisibleDescendantIndices(idx)
  descendantIndices.forEach(i => {
    thirdPartyDialogFileList.value[i].selected = newSelected
  })
  syncThirdPartyAncestors(idx)
}

// 全不选
const deSelectAllThirdParty = () => {
  thirdPartyDialogFileList.value.forEach(item => { item.selected = false })
  for (const cached of thirdPartyDialogCache.values()) {
    cached.forEach(item => { item.selected = false })
  }
}

// 从已选列表移除某项
const removeThirdPartySelectedItem = (index: number) => {
  const item = thirdPartySelectedItems.value[index]
  if (!item) return
  item.selected = false
}

// 上传第三方文件到 extraction
const uploadThirdPartyFiles = async () => {
  if (loading.value || !thirdPartySelectedItems.value.length) return
  loading.value = true
  try {
    const source = PLATFORM_SOURCE_MAP[selectedPlatform.value]
    const uploadFiles: File[] = []

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
      ElMessage.error(t('parsing.fail'))
      loading.value = false
      return
    }

    // 所有文件从第三方下载完成 → 注册 store，立即关闭弹窗
    const realEntries: { tempId: string; file: File }[] = uploadFiles.map((file, i) => {
      const tempId = `extraction-third-${file.name}-${Date.now()}-${i}`
      uploadTaskStore.addFile({ id: tempId, name: file.name, status: 'uploading', type: 'extraction' })
      return { tempId, file }
    })
    uploadDialogVisible.value = false
    loading.value = false

    // 并发上传每个文件（fire-and-forget）
    realEntries.forEach(({ tempId, file }) => {
      uploadSingleExtractionFile(file).then(result => {
        if (result.success) {
          uploadTaskStore.updateFile(tempId, { status: 'success' })
          getTemplateFileList()
        } else {
          uploadTaskStore.updateFile(tempId, { status: 'uploadFail' })
          if (result.code === 8021) {
            apiKeyDialogVisible.value = true
          } else {
            ElMessage.error(`${t('parsing.fail')}: ${file.name}`)
          }
        }
      })
    })
  } catch {
    ElMessage.error(t('parsing.fail'))
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

// ── 上传对话框：打开时加载数据，关闭时重置所有状态 ──
watch(uploadDialogVisible, async (newVal) => {
  if (newVal) {
    // 并行加载：团队空间文件夹列表 + 第三方凭证
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
        // 反查 source → platform key
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

    // 若当前平台已授权，自动加载第三方文件树
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
    // 关闭时重置所有状态
    uploadActiveTab.value = 'local'
    fileList.value = []
    folderList.value = []
    teamSelectedFiles.value = []
    thirdPartyDialogFileList.value = []
    thirdPartyDialogCache.clear()
    selectedPlatform.value = 'google-drive'
    // 重置凭证
    thirdPartyCredentials.value = Object.fromEntries(thirdPartyPlatforms.map(p => [p.key, null]))
  }
})

// ── isUploadEnabled：当前 tab 下是否有可上传的文件 ──
const isUploadEnabled = computed<boolean>(() => {
  if (uploadActiveTab.value === 'local') return fileList.value.length > 0
  if (uploadActiveTab.value === 'team') return teamSelectedFiles.value.length > 0
  if (uploadActiveTab.value === 'thirdParty') return thirdPartySelectedItems.value.length > 0
  return false
})

// ── handleUpload：根据当前 tab 调度对应的上传方法 ──
const handleUpload = () => {
  if (!isUploadEnabled.value) return
  if (uploadActiveTab.value === 'local') upload()
  else if (uploadActiveTab.value === 'team') uploadTeamSpaceFile()
  else if (uploadActiveTab.value === 'thirdParty') uploadThirdPartyFiles()
}

const defaultStatusArr = ref<status[]>([])
const customStatusArr = ref<status[]>([])
const templateStatusArr = ref<status[]>([])
const selectTemp = ref<string[]>([])

const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const pageSizes = ref([5, 10, 20])

watch(selectDialogVisible, (newVal) => {
  if (newVal) {
    getTemplateList()
  }
})

watch(() => route.query.templateSettings, async (value) => {
  if (value === '1') {
    await openTemplateSettingsFromRoute()
  }
})

onMounted(async () => {
  const custom = Number(Cookies.get('pageSize'))
  if (custom) {
    customPageSize.value = custom
    updatePageSizes()
    pageSize.value = custom
  }
  pollFileStatus()
  await getTemplateGroup()
  await openTemplateSettingsFromRoute()
  addEventListener('click', () => {
    templateStatusArr.value.forEach(item => item.status = false)
  })
})

const rowKey = (row: FileData) => row.fileId

const single = ref(false)
const double = ref(false)
const singleDate = ref('')
const dateType = ref('less')
const timeFilter = ref(false)
const doubleDate = ref<string[]>([])
const userFirstLogin = ref('2000-01-01T00:00:00')

const change = (type: string) => {
  dateType.value = type
  single.value = false
  double.value = false
  doubleDate.value = []
  if (type === 'between') {
    singleDate.value = ''
  } else {
    doubleDate.value = []
  }
}

const handleClick = () => {
  if (dateType.value === 'between') {
    double.value = true
  } else {
    single.value = true
  }
}

// 日期范围选择回调（介于两者之间）
const checkedDate = (dateArr: string[]) => {
  // 开始日期 00:00:00，结束日期 23:59:59
  doubleDate.value = [
    `${dateArr[0]}T00:00:00`,
    `${dateArr[1]}T23:59:59`
  ]
  double.value = false
}

// 单日期选择回调
const singleCheckedDate = (date: string) => {
  if (dateType.value === 'less') {
    // 早于：选择日期的 23:59:59
    singleDate.value = `${date}T23:59:59`
  } else if (dateType.value === 'more') {
    // 晚于：选择日期的 00:00:00
    singleDate.value = `${date}T00:00:00`
  } else if (dateType.value === 'equal') {
    // 等于：开始时间 00:00:00，结束时间 23:59:59
    singleDate.value = `${date}T00:00:00 ~ ${date}T23:59:59`
  } else {
    singleDate.value = date
  }
  single.value = false
}

// 获取时间
const checkDate = () => {
  timeFilter.value = false
  // TODO: 根据 dateType 和日期值进行筛选
  getTemplateFileList()
}

// 表格多选事件处理
const handleSelectionChange = (selection: FileData[]) => {
  selectFile.value = null
  selectFiles.value = []
  selection.forEach((item: FileData) => {
    selectFiles.value.push(item)
  })
}

const changeActiveTemp = (item?: TemplateList) => {
  activeTemp.value = item?.groupTemplateId || 'all'
  groupTemplateId.value = item?.groupTemplateId || ''
  getTemplateFileList()
}

// 通过自定义模板切换
const changeActiveTempByCustom = (item: TemplateData) => {
  activeTemp.value = item.id
  groupTemplateId.value = item.id
  getTemplateFileList()
}

// 通过默认模板切换
const changeActiveTempByDefault = (item: TemplateData) => {
  activeTemp.value = item.id
  groupTemplateId.value = item.id
  getTemplateFileList()
}

const getFileExtension = (fileName?: string): string => {
  if (!fileName) return ''
  const lastDotIndex = fileName.lastIndexOf('.')
  // 没有后缀 / 以 . 开头的隐藏文件 / 以 . 结尾
  if (lastDotIndex <= 0 || lastDotIndex === fileName.length - 1) return ''
  return fileName.slice(lastDotIndex + 1).toUpperCase()
}

// 轮询定时器
let pollTimer: ReturnType<typeof setTimeout> | null = null
const POLL_INTERVAL = 3000 // 轮询间隔 3 秒
const failedStatusNotified = new Set<string>()

// 轮询查询文件处理状态
// skipFetch: 为 true 时跳过数据请求（由 getTemplateFileList 触发时使用，避免循环调用）
const pollFileStatus = async (fileIds?: string[], skipFetch = false) => {
  // 清除之前的轮询
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }

  try {
    // 仅在非跳过模式下刷新文件列表
    if (!skipFetch) {
      await getTemplateFileList(false)
    }
    
    // 确定要检查的文件列表
    const filesToCheck = fileIds?.length 
      ? dataList.value.filter((file: FileData) => fileIds.includes(file.fileId))
      : dataList.value

    // status=3 时提示失败原因（同一文件仅提示一次，避免轮询重复弹窗）
    filesToCheck.forEach((file: FileData) => {
      if (file.status === 3 && !failedStatusNotified.has(file.fileId)) {
        failedStatusNotified.add(file.fileId)
        if (file.failureReason) ElMessage.error(file.failureReason)
      }
      // 同步到全局上传面板 store
      if (uploadTaskStore.fileList.find(f => f.id === file.fileId)) {
        if (file.status === 3) {
          uploadTaskStore.updateFile(file.fileId, { status: 'fail' })
        }
      }
    })
    
    // groupTemplateId 为空且 status=0 的文件无需轮询状态，视为已完成轮询条件
    const allCompleted = filesToCheck.length === 0 ||
      filesToCheck.every((file: FileData) => {
        const noTemplatePending = !file.groupTemplateId && file.status === 0
        return noTemplatePending || file.status === 2 || file.status === 3
      })

    if (!allCompleted) {
      // 继续轮询
      pollTimer = setTimeout(() => pollFileStatus(fileIds), POLL_INTERVAL)
    }
  } catch {
    // 请求失败时继续轮询
    pollTimer = setTimeout(() => pollFileStatus(fileIds), POLL_INTERVAL)
  }
}

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
  failureReason?: string
}

const selectFile = ref<FileData | null>(null)

const getSelectedFileIds = (): string[] => selectFiles.value.map(item => item.fileId)

// Batch action handler for dropdown
const handleBatchAction = (command: string) => {
  switch (command) {
    case 'restart':
      startExtraction()
      break
    case 'export':
      exportDialogVisible.value = true
      break
    case 'delete':
      deleteFile([])
      break
  }
}

const startExtraction = async () => {
  const fileIds = selectFile.value?.fileId ? [selectFile.value.fileId] : getSelectedFileIds()
  if (!fileIds.length) return
  loading.value = true

  try {
    const formData = new FormData()
    fileIds.forEach((id: string) => {
      formData.append('idpFileIds', id)
    })
    formData.append('type', 'EXTRACTION')

    const { data } = await post('/api/idp/files-start', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
    if (data.code === 200 && data.message === 'success') {
      getTemplateFileList()
      // 开始轮询查询文件处理状态
      pollFileStatus(fileIds)
    }
  } catch {
    loading.value = false
    ElMessage.error(t('parsing.fail'))
  }
  loading.value = false
}

const previewFile = async (row: FileData, isConfigResult = false) => {
  // 获取当前文件对应的模板
  const template = templateList.value.find(item => item.groupTemplateId === row.groupTemplateId)
  changeActive('result', row, template, false, isConfigResult)
}

onUnmounted(() => {
  stopPolling()
  removeEventListener('click', () => {
  })
})

const changeStatus = async (index: number) => {
  templateStatusArr.value.forEach((status: status) => {
    status.status = false
  })
  templateStatusArr.value[index].status = !templateStatusArr.value[index].status
}

// 更改模板逻辑
const changeTemp = async (fileId: string, newGroupTemplateId: string) => {
  const { data } = await post('/api/idp/file-manual-group', {
    fileId: fileId,
    groupTemplateId: newGroupTemplateId
  })
  if (data.code === 200 && data.message === 'success') {
    ElMessage.success(t('extraction.success'))

    // 全部视图：仅刷新当前列表，不跳转
    if (activeTemp.value === 'all') {
      getTemplateFileList()
      return
    }

    // 具体模板视图：跳转到目标模板并高亮该文件
    const targetTemplate = templateList.value.find(item => item.groupTemplateId === newGroupTemplateId)
    if (targetTemplate) {
      activeTemp.value = targetTemplate.templateName
      groupTemplateId.value = targetTemplate.groupTemplateId
    }
    await getTemplateFileList()

    // 滚动定位 + 高亮闪烁
    await nextTick()
    highlightFileId.value = fileId
    const rowEl = tableRef.value?.$el?.querySelector(`.row-highlight-flash`)
    if (rowEl) {
      rowEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
    // 闪烁 2 次后清除高亮（动画 0.6s × 2 = 1.2s）
    setTimeout(() => {
      highlightFileId.value = ''
    }, 1200)
  } else {
    ElMessage.error(t('extraction.fail'))
  }
}

// 删除组模版逻辑
const deleteGroupTemplate = async (id: string, index: number) => {
  const formData = new FormData()
  formData.append('groupTemplateId', id)
  formData.append('groupId', groupId.value || '')
  const { data } = await post('/api/idp/delete-group-template', formData)
  if (data.code === 200 && data.message === 'success') {
    ElMessage.success(t('extraction.success'))
    getTemplateGroup()
  } else if (data.code === 8019 && data.message === 'There are files under the template') {
    ElMessage.error(t('extraction.under'))
  } else {
    ElMessage.error(t('extraction.fail'))
  }
  selectTemp.value = []
  templateStatusArr.value = []
  templateList.value.forEach((item: TemplateList)=> {
    templateStatusArr.value.push({ status: false })
    selectTemp.value.push(item.templateId)
  })
  sourceTemp.value = [...selectTemp.value]
}

// 保存模板组逻辑
const saveTemplateGroup = async () => {
  const formData = new FormData()
  formData.append('groupId', groupId.value || '')
  selectTemp.value.forEach((id: string) => {
    formData.append('templateIds', id)
  })
  const { data } = await post('/api/idp/create-group-template', formData)
  if (data.code === 200 && data.message === 'success') {
    ElMessage.success(t('extraction.success'))
    selectDialogVisible.value = false
    await getTemplateGroup()
    getTemplateFileList()
  } else {
    ElMessage.error(t('extraction.fail'))
  }
}

const startTime = ref('')
const endTime = ref('')
const sourceTemp = ref()
const reviewStatusFilter = ref(false)
const reviewStatusFilterValue = ref([])
const extractionStatusFilter = ref(false)
const extractionStatusFilterValue = ref([])
const typeFilterValue = ref('')

// 重置筛选条件
const resetFilters = () => {
  searchQueryFile.value = ''
  extractionStatusFilterValue.value = []
  reviewStatusFilterValue.value = []
  singleDate.value = ''
  doubleDate.value = []
  typeFilterValue.value = ''
  getTemplateFileList()
}

// 获取模板文件列表逻辑
// autoPoll: 获取数据后是否自动检查并启动轮询（默认 true）
const getTemplateFileList = async (autoPoll = true) => {
  if (dateType.value === 'less') {
    endTime.value = singleDate.value
    startTime.value = ''
  } else if (dateType.value === 'more') {
    startTime.value = singleDate.value
    endTime.value = ''
  } else if (dateType.value === 'equal') {
    const [start, end] = singleDate.value.split(' ~ ')
    startTime.value = start || ''
    endTime.value = end || ''
  } else {
    startTime.value = doubleDate.value[0] || ''
    endTime.value = doubleDate.value[1] || ''
  }
  let searchQuery = `taskType=EXTRACTION`
    + `&page=${currentPage.value}`
    + `&pageSize=${pageSize.value}`
    + `&status=${extractionStatusFilterValue.value}`
    + `&reviewStatus=${reviewStatusFilterValue.value}`
    + `&startTime=${startTime.value}`
    + `&endTime=${endTime.value}`
    + `&fileName=${searchQueryFile.value}`

  if (activeTemp.value === 'all') {
    searchQuery += `&groupId=${groupId.value}`
  } else {
    searchQuery += `&groupId=${groupId.value}` + `&groupTemplateId=${groupTemplateId.value}`
  }
  const { data }: any = await get(`/api/idp/getFileList?` + searchQuery)
  // 记录当前已选中的文件 ID
  const selectedIds = new Set(selectFiles.value.map(f => f.fileId))
  dataList.value = data?.data.records || []
  total.value = data?.data.total || 0

  // 用列表返回的真实 fileId 替换 store 里同名的 tempId 条目（tempId 以 "extraction-" 开头）
  ;(dataList.value as FileData[]).forEach((row: FileData) => {
    if (!row?.fileId || !row?.fileName) return
    const tempEntry = uploadTaskStore.fileList.find(
      f => f.id.startsWith('extraction-') && f.name === row.fileName && (f.status === 'uploading' || f.status === 'pending')
    )
    if (tempEntry) {
      uploadTaskStore.removeFile(tempEntry.id)
      uploadTaskStore.addFile({ id: row.fileId, name: row.fileName, status: 'pending', type: 'extraction' })
    }
  })

  // 恢复选中状态
  if (selectedIds.size > 0) {
    await nextTick()
    dataList.value.forEach((row: FileData) => {
      if (selectedIds.has(row.fileId)) {
        tableRef.value?.toggleRowSelection(row, true)
      }
    })
  }

  // 获取数据后，检查是否有正在处理的文件，有则自动启动轮询
  if (autoPoll) {
    const hasProcessing = dataList.value.some((file: FileData) => file.status === 0 || file.status === 1)
    if (hasProcessing) {
      pollFileStatus(undefined, true) // skipFetch=true，避免循环调用
    }
  }
}

const getTemplateGroup = async () => {
  // 获取模板列表逻辑
  const { data }: any = await get('/api/idp/get-group-template')
  if (data?.data.length) {
    templateStatusArr.value = []
    groupId.value = data?.data[0].groupId
    // 从 API 响应中提取三个分类列表
    pinnedTemplatesList.value = data?.data[0].pinnedTemplates || []
    customTemplatesList.value = data?.data[0].customTemplates || []
    defaultTemplatesList.value = data?.data[0].defaultTemplates || []
    // 合并三个列表用于兼容旧逻辑
    templateList.value = data?.data[0].groupTemplates || [
      ...pinnedTemplatesList.value,
      ...customTemplatesList.value,
      ...defaultTemplatesList.value
    ]
    selectTemp.value = []
    templateStatusArr.value = []
    templateList.value.forEach((item: TemplateList)=> {
      templateStatusArr.value.push({ status: false })
      selectTemp.value.push(item.templateId)
    })
    sourceTemp.value = [...selectTemp.value]
    await getTemplateFileList(false)
    // 首次加载时将已失败的文件预先记录，避免进入页面时弹出错误提示
    if (!templateGroupLoaded.value) {
      dataList.value.forEach((file: FileData) => {
        if (file.status === 3) failedStatusNotified.add(file.fileId)
      })
    }
    // 检查是否有正在处理的文件，有则自动启动轮询
    const hasProcessing = dataList.value.some((file: FileData) => file.status === 0 || file.status === 1)
    if (hasProcessing) {
      pollFileStatus(undefined, true)
    }
  } else {
    selectDialogVisible.value = true
    getTemplateList()
  }
  templateGroupLoaded.value = true
}

const refreshTemplateGroup = async () => {
  await getTemplateGroup()
}

const isIdpSuccess = (data: any) => data?.code === 200 || data?.code === '200' || data?.code === 0 || data?.code === '0'

const refreshTemplateGroupAfterPinChange = async (expandTarget: 'pinned' | 'custom') => {
  if (expandTarget === 'pinned') {
    pinnedTemplateExpanded.value = true
  } else {
    customTemplateExpanded.value = true
  }
  await getTemplateGroup()
}

// ── Pin/Unpin Template Functions ──
// 置顶模板
const pinTemplate = async (groupTemplateId: string) => {
  try {
    const { data } = await post(`/api/idp/pin-template?groupTemplateId=${groupTemplateId}`)
    if (isIdpSuccess(data)) {
      ElMessage.success('Template pinned')
      await refreshTemplateGroupAfterPinChange('pinned')
    } else {
      ElMessage.error('Failed to pin template')
    }
  } catch {
    ElMessage.error('Failed to pin template')
  }
}

// 取消置顶模板
const unpinTemplate = async (groupTemplateId: string) => {
  try {
    const { data } = await post(`/api/idp/unpin-template?groupTemplateId=${groupTemplateId}`)
    if (isIdpSuccess(data)) {
      ElMessage.success('Template unpinned')
      await refreshTemplateGroupAfterPinChange('custom')
    } else {
      ElMessage.error('Failed to unpin template')
    }
  } catch {
    ElMessage.error('Failed to unpin template')
  }
}

// 处理模板操作菜单
const handleTemplateAction = async (command: string, item: TemplateList) => {
  switch (command) {
    case 'pin':
      await pinTemplate(item.groupTemplateId)
      break
    case 'unpin':
      await unpinTemplate(item.groupTemplateId)
      break
  }
}

// 置顶模板 - 直接使用 API 返回的列表（已按 pinnedTime 倒序排列）
const pinnedTemplates = computed(() => pinnedTemplatesList.value)

// 自定义模板 - 直接使用 API 返回的列表（已按 order 升序排列，不含已置顶的）
const customTemplates = computed(() => customTemplatesList.value)

// 默认模板 - 直接使用 API 返回的列表（已按 order 升序排列，不含已置顶的）
const defaultTemplates = computed(() => defaultTemplatesList.value)

const getTemplateList = async () => {
  // 并行请求，提高性能
  const [defaultTemplateResponse, templateListResponse] = await Promise.all([
    get('/api/idp/get-default-template'),
    get('/api/idp/get-template-list?name=' + searchQueryTemp.value)
  ])
  
  const { data: { data: defaultTemplates = [] } } = defaultTemplateResponse
  const { data: { data: customTemplates = [] } } = templateListResponse
  defaultTemplateList.value = defaultTemplates
  customTemplateList.value = customTemplates
  defaultStatusArr.value = []
  defaultTemplateList.value.forEach(()=> {
    defaultStatusArr.value.push({ status: false })
  })
  customStatusArr.value = []
  customTemplateList.value.forEach(()=> {
    customStatusArr.value.push({ status: false })
  })
}

const deleteTemplate = async (id: string) => {
  // 删除模板逻辑
  ElMessageBox.confirm(t('extraction.deleteTip'), t('extraction.deleteTitle'), {
    confirmButtonText: t('extraction.ok'),
    cancelButtonText: t('extraction.cancel'),
    type: 'warning',
    customClass: 'delete-file',
  }).then(async () => {
    try {
      const { data } = await post('/api/idp/delete-template', {
        id: id
      })
      if (data.code === 200 && data.message === 'success') {
        ElMessage.success(t('splitting.deleteSuccess'))
        getTemplateList()
        getTemplateGroup()
      } else if (data.code === 8019 && data.message === 'There are files under the template') {
        ElMessage.error(t('extraction.under'))
      }  else {
        ElMessage.error(t('splitting.deleteFail'))
      }
    } catch {
      ElMessage.error(t('splitting.deleteFail'))
    }
  })
}

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getTemplateFileList()
}

// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getTemplateFileList()
}

// 获取抽取状态对应的标签文本
// 0=CREATED, 1=PROCESSING, 2=SUCCESS, 3=FAIL, 4=DELETE, 5=PAUSE
// 6=PENDING_CLASSIFICATION, 7=CLASSIFYING, 8=CLASSIFICATION_FAILED
// 9=PENDING_EXTRACTION, 10=EXTRACTING, 11=EXTRACTION_SUCCESS, 12=EXTRACTION_FAILED
const translateStatus = (key: string, fallback: string) => {
  const localeKey = `extraction.${key}`
  const text = t(localeKey)
  return text === localeKey ? fallback : text
}

const getExtractionStatusTxt = (progress: number) => {
  switch (progress) {
    case 0: return translateStatus('pendingExtraction', 'Pending Extraction')
    case 1: return translateStatus('extracting', 'Extracting')
    case 2: return translateStatus('extractionSuccess', 'Extraction Success')
    case 3: return translateStatus('extractionFailed', 'Extraction Failed')
    case 5: return translateStatus('paused', 'Paused')
    case 6: return translateStatus('pendingClassification', 'Pending Classification')
    case 7: return translateStatus('classifying', 'Classifying')
    case 8: return translateStatus('classificationFailed', 'Classification Failed')
    case 9: return translateStatus('pendingExtraction', 'Pending Extraction')
    case 10: return translateStatus('extracting', 'Extracting')
    case 11: return translateStatus('extractionSuccess', 'Extraction Success')
    case 12: return translateStatus('extractionFailed', 'Extraction Failed')
    default: return translateStatus('pendingExtraction', 'Pending Extraction')
  }
}

// 格式化抽取状态样式 - using parsing page semantic class names
const getExtractionStatusClass = (progress: number) => {
  if ([0, 6].includes(progress)) return 'is-neutral'
  if (progress === 9) return 'is-brand'
  if ([1, 7, 10].includes(progress)) return 'is-warning'
  if ([2, 11].includes(progress)) return 'is-success'
  if ([3, 8, 12].includes(progress)) return 'is-error'
  if (progress === 5) return 'is-neutral'
  return 'is-neutral'
}

// 获取核对状态对应的标签类型
const isSuccessfulExtractionStatus = (status: number) => {
  return status === 2 || status === 11
}

const getReviewStatusTxt = (progress: number, status: number) => {
  if (!isSuccessfulExtractionStatus(status)) return '--'
  if (progress === 0) return t('extraction.unconfirmed')
  if (progress === 1) return t('extraction.confirmed')
  return '--'
}

// 格式化核对状态 - using parsing page semantic class names
const getReviewStatusClass = (progress: number, status: number) => {
  if (!isSuccessfulExtractionStatus(status)) return 'is-neutral'
  if (progress === 0) return 'is-warning'
  if (progress === 1) return 'is-success'
  return 'is-neutral'
}

// 删除文件
const deleteFile = async (fileId: string[]) => {
  ElMessageBox.confirm(t('extraction.deleteTip'), t('extraction.deleteTitle'), {
    confirmButtonText: t('extraction.ok'),
    cancelButtonText: t('extraction.cancel'),
    type: 'warning',
    customClass: 'delete-file',
  }).then(async () => {
    const fileIds = fileId.length ? [fileId] : getSelectedFileIds()
    if (!fileIds.length) return
    loading.value = true
    
    try {
      const { data } = await get(`/api/idp/file-delete?fileIds=${fileIds}`)
      if (data.code === 200 && data.message === 'success') {
        getTemplateFileList()
        ElMessage.success(t('splitting.deleteSuccess'))
      }
    } catch {
      ElMessage.error(t('splitting.deleteFail'))
    }
    loading.value = false
  })
}

// 导出文件
const exportFile = async (id?: string[]) => {
  try {
    const fileIds = id?.length ? [id] : getSelectedFileIds()
    if (!fileIds.length) return
    try {
      loading.value = true
      const exportMap = {
        json: 'JSON',
        excel: 'EXCEL',
        csv: 'CSV'
      }

      const res = await request({
        method: 'post',
        url: '/api/idp/extract-export',
        data: {
          fileIds,
          exportFormat: exportMap[format.value as keyof typeof exportMap],
          isCompress: exportMethod.value === 'each'
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
      exportDialogVisible.value = false
      ElMessage.success(t('extraction.success'))
    } catch {
      loading.value = false
      exportDialogVisible.value = false
      ElMessage.error(t('extraction.fail'))
    }
  } catch {
    exportDialogVisible.value = false
    ElMessage.error(t('extraction.fail'))
  }
  loading.value = false
}

const base = getSystemBaseUnit()
const MAX_SIZE = base * base * 1000 // 10MB
const MAX_COUNT = 999
GlobalWorkerOptions.workerSrc = '/lib/pdf.worker.min.mjs'
const CMAP_URL = '/lib/cmaps/'

// 校验上传文件
const SUPPORTED_EXTENSIONS = ['.png', '.jpg', '.jpeg', '.tiff', '.bmp', '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.csv', '.ppt', '.pptx', '.txt']

const validateFiles = async (files: FileList): Promise<globalThis.File[] | null> => {
  const fileArray = Array.from(files)

  const unsupported = fileArray.filter(file => {
    const ext = file.name.slice(file.name.lastIndexOf('.')).toLowerCase()
    return !SUPPORTED_EXTENSIONS.includes(ext)
  })
  if (unsupported.length > 0) {
    ElMessage.error(t('bulkExtract.notSupport'))
    return null
  }

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

// 判断文件是否重复：根据 name 和 size 判断
const isDuplicate = (file: any, list?: any[]): boolean => {
  if (!Array.isArray(list)) return false
  return list.some(item => item.name === file.name && item.size === file.size)
}

const deleteUploadFile = (index: number) => {
  fileList.value.splice(index, 1)
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

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
}

type UploadFileResult = {
  success: boolean
  code?: number
  message?: string
}

const getUploadErrorResult = (error: any): UploadFileResult => ({
  success: false,
  code: error?.response?.data?.code,
  message: error?.response?.data?.message
})

// 上传单个文件到 extraction，返回上传结果
const uploadSingleExtractionFile = (file: File): Promise<UploadFileResult> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('taskType', 'EXTRACTION')
  formData.append('groupId', groupId.value)
  if (groupTemplateId.value) {
    formData.append('groupTemplateId', groupTemplateId.value)
  }
  return post('/api/idp/file-upload', formData, {}, {
    headers: { 'Content-Type': 'multipart/form-data' } as any
  }).then((res: any) => ({
    success: res?.data?.code === 200,
    code: res?.data?.code,
    message: res?.data?.message
  })).catch(getUploadErrorResult)
}

// 解析上传文件
const upload = () => {
  if (!fileList.value.length) return

  // 立即在全局上传面板中注册所有文件，状态为 uploading
  const uploadingEntries: { tempId: string; file: File }[] = fileList.value.map((file: File, i: number) => {
    const tempId = `extraction-${file.name}-${Date.now()}-${i}`
    uploadTaskStore.addFile({ id: tempId, name: file.name, status: 'uploading', type: 'extraction' })
    return { tempId, file }
  })

  // 立即关闭弹窗、清空文件列表
  fileList.value = []
  uploadDialogVisible.value = false

  // 并发上传每个文件，每个文件单独处理回调（fire-and-forget）
  uploadingEntries.forEach(({ tempId, file }) => {
    uploadSingleExtractionFile(file).then(result => {
      if (result.success) {
        uploadTaskStore.updateFile(tempId, { status: 'success' })
        getTemplateFileList()
      } else {
        uploadTaskStore.updateFile(tempId, { status: 'uploadFail' })
        if (result.code === 8021) {
          apiKeyDialogVisible.value = true
        } else {
          ElMessage.error(`${t('parsing.fail')}: ${file.name}`)
        }
      }
    })
  })
}

const leave = () => {
  dragover.value = false
}

defineExpose({
  getTemplateFileList,
  getTemplateList,
  refreshTemplateGroup,
  getTemplateGroup,
  toggleSelect
})
</script>

<style lang="scss" scoped>
/* ── Parsing page CSS classes (Figma-matched) - adapted for sidebar layout ── */
.document-extraction-page {
  height: calc(100vh - 81px);
  background: #f5f7ff;
  padding: 16px;
  gap: 8px;
  overflow: hidden;
}

.template-sidebar-panel {
  height: 100%;
  flex: 0 0 230px;
  border-right: 0;
  border-radius: 6px;
  background: #fff;
  justify-content: space-between;
}

.template-sidebar-main {
  width: 206px;
  flex: 1 1 auto;
  min-height: 0;
  gap: 0;
}

.template-list-scroll {
  min-height: 0;
}

.template-sidebar-actions {
  height: 32px;
  gap: 8px;
}

.new-template-btn,
.settings-square {
  height: 32px;
  line-height: 22px;
}

.new-template-btn {
  max-width: 166px;
}

.settings-square {
  width: 32px;
  padding: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.template-sidebar-divider {
  width: 212px;
  height: 1px;
  margin-left: 0;
  background: #e7e8e8;
}

.template-search-input {
  border: 0;
  background: #f3f3f4;
  background-image: none;
  color: #0c131f;
  line-height: 22px;

  &:focus {
    border: 0;
    box-shadow: none;
  }

  &::placeholder {
    color: #0c131f;
  }
}

.all-documents-btn {
  line-height: 22px;
  border: 1px solid #396ffa;
  background-image: none;
}

.template-section-title {
  height: 24px;
  padding: 4px 4px 4px 0;
  line-height: 16px;
}

.template-list-row {
  height: 32px;
  padding: 4px 4px 4px 8px;
  line-height: 20px;

  &.is-active {
    background: #f5f7ff;
    color: #396ffa;
  }
}

.template-sidebar-footer {
  width: 206px;
  border-top: 1px solid #e7e8e8;
}

.document-list-shell {
  flex: 1 1 auto;
  min-width: 0;
  height: 100%;
  padding: 0;
  background: transparent;
  overflow: hidden;
}

.document-list-card {
  width: 100%;
  padding: 12px 32px 8px;
  border-radius: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%;
  min-height: 0;

  &__title {
    font-family: 'Encode Sans Expanded', 'Encode Sans', sans-serif;
    font-size: 20px;
    font-weight: 600;
    line-height: 28px;
    color: #0c131f;
    padding-bottom: 0;
    border-bottom: 0;
    margin-bottom: 0;
  }
}

.document-list-toolbar {
  min-height: 32px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
}

.toolbar-left {
  gap: 4px;
}

.toolbar-right {
  gap: 8px;
}

.compact-search,
.filter-input {
  height: 32px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  background: #fff;
  display: flex;
  align-items: center;
  color: rgba(12, 19, 31, 0.4);

  input {
    width: 100%;
    border: 0;
    outline: none;
    background: transparent;
    color: #0c131f;
    font-size: 14px;
    line-height: 22px;

    &::placeholder {
      color: rgba(12, 19, 31, 0.4);
    }
  }

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
  }
}

.compact-search {
  width: 200px;
  padding: 4px 8px 4px 8px;
  gap: 8px;
}

.icon-filter,
.primary-btn,
.outline-btn,
.search-btn,
.reset-btn {
  height: 32px;
  border-radius: 3px;
  font-size: 14px;
  line-height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
  }
}

.icon-filter {
  width: 32px;
  padding: 0;
  border: 1px solid #396ffa;
  background: #fff;
  color: #396ffa;

  &.is-active {
    background: #f5f7ff;
  }
}

.primary-btn,
.search-btn {
  gap: 8px;
  padding: 5px 16px;
  border: 1px solid #396ffa;
  background: #396ffa;
  color: #fff;
}

.outline-btn,
.reset-btn {
  gap: 8px;
  padding: 5px 16px;
  border: 1px solid #396ffa;
  background: #fff;
  color: #396ffa;
}

.filter-panel {
  width: 100%;
  min-height: 128px;
  padding: 16px 16px 0;
  border-radius: 6px;
  background: #f3f3f4;
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.filter-grid {
  flex: 1;
  min-width: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 1fr));
  gap: 0 12px;
}

.filter-input {
  width: 100%;
  margin-bottom: 24px;
  padding: 4px 8px;
  justify-content: space-between;
  text-align: left;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: rgba(12, 19, 31, 0.4);
  }
}

.filter-date {
  border-color: #dcdde1;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex: 0 0 auto;
}

.figma-table-wrap {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto hidden;
  display: flex;
  flex-direction: column;
}

:deep(.figma-select) {
  width: 100%;

  .el-select__wrapper {
    min-height: 32px;
    border-radius: 3px;
    box-shadow: 0 0 0 1px #dcdde1 inset;
  }
}

.figma-table-wrap :deep(.table-type-select) {
  width: 94px;

  .el-select__wrapper {
    width: 94px;
    min-height: 24px;
    height: 24px;
    padding: 2px 16px;
    border-radius: 3px;
    background: #fff;
    box-shadow: 0 0 0 1px #dcdde1 inset;
  }

  .el-select__placeholder,
  .el-select__selected-item {
    color: #0c131f;
    font-family: 'Encode Sans', sans-serif;
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .el-select__suffix {
    color: #0c131f;
  }

  .el-select__caret {
    width: 14px;
    height: 14px;
    color: #0c131f;
  }
}

.filter-grid :deep(.figma-select) {
  margin-bottom: 24px;

  .el-select__wrapper {
    width: 100%;
  }
}

:deep(.figma-table) {
  width: 100% !important;
  flex: 1 1 auto;
  color: #0c131f;
  --el-table-border-color: #e7e8e8;
  --el-table-header-bg-color: #f5f7ff;

  .el-table__header,
  .el-table__body {
    width: 100% !important;
  }

  .el-table__inner-wrapper,
  .el-table__body-wrapper,
  .el-scrollbar,
  .el-scrollbar__wrap,
  .el-scrollbar__view {
    width: 100% !important;
  }

  .el-table__inner-wrapper::before {
    display: none;
  }

  .el-table__header th {
    position: relative;
    height: 46px;
    padding: 0;
    background: #f5f7ff;
    border-bottom: 1px solid #e7e8e8;
    border-right: 0 !important;
    color: #9a9ea6;
    font-weight: 400;
  }

  .el-table__header th::before,
  .el-table__header th::after,
  .el-table__header .el-table__cell::before,
  .el-table__header .el-table__cell::after {
    display: none !important;
  }

  .el-table__cell {
    height: 46px;
    padding: 0;
    border-bottom: 1px solid #e7e8e8;
    border-right: 0 !important;
    font-size: 14px;
    line-height: 22px;
  }

  .el-table__cell .cell {
    padding: 0 16px;
    line-height: 22px;
    display: flex;
    align-items: center;
    min-width: 0;
  }

  .el-checkbox__inner {
    width: 16px;
    height: 16px;
    border: 1px solid #dcdde1 !important;
    border-radius: 3px;
    background: #fff;
  }

  .el-checkbox__inner::after {
    border-width: 1px;
  }

  .el-checkbox__input.is-checked .el-checkbox__inner,
  .el-checkbox__input.is-indeterminate .el-checkbox__inner {
    background-color: #396ffa;
    border-color: #396ffa;
  }

  .el-table-column--selection .el-table__cell,
  .el-table-column--selection.el-table__cell,
  .el-table-column--selection .cell {
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
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

.file-cell {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
  color: #396ffa;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
    color: #0C131F;
  }

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-decoration: underline;
    text-underline-offset: 2px;
  }
}

.format-cell {
  text-align: center;
}

.upload-time {
  color: #0c131f;
  white-space: nowrap;
}

.row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;

  button {
    width: 32px;
    height: 32px;
    padding: 0;
    border: 0;
    border-radius: 16px;
    background: transparent;
    color: #396ffa;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;

    svg {
      width: 16px;
      height: 16px;
    }
  }
}

.pagination-row {
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 24px;
  flex: 0 0 auto;
}

.total-text {
  color: #0c131f;
  font-size: 14px;
  line-height: 22px;
}

.table-empty {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(12, 19, 31, 0.6);

  button {
    margin-top: 16px;
    height: 32px;
    padding: 5px 16px;
    border: 1px solid #396ffa;
    border-radius: 3px;
    background: #396ffa;
    color: #fff;
    cursor: pointer;
  }
}

:deep(.el-pagination.is-background) {
  --el-pagination-button-width: 32px;
  --el-pagination-button-height: 32px;
  --el-pagination-button-bg-color: #fff;
  --el-pagination-hover-color: #396ffa;
  gap: 16px;
  justify-content: flex-end;

  .el-pagination__sizes {
    margin: 0;

    .el-select {
      width: 112px;
    }

    .el-select__wrapper {
      min-height: 32px;
      padding: 5px 8px;
      border-radius: 3px;
      box-shadow: 0 0 0 1px #dcdde1 inset;
      font-size: 14px;
      line-height: 22px;
    }
  }

  .el-pager {
    gap: 8px;
  }

  .btn-prev,
  .btn-next {
    min-width: 32px;
    height: 32px;
    margin: 0;
    border-radius: 3px;
    background: #fff;
    border: 0;
    color: #0c131f;
  }

  .btn-prev:disabled,
  .btn-next:disabled,
  .btn-prev.is-disabled,
  .btn-next.is-disabled {
    color: rgba(12, 19, 31, 0.26);
  }

  .el-pager li {
    min-width: 32px;
    height: 32px;
    margin: 0;
    border-radius: 3px;
    background: #fff;
    border: 1px solid #dcdde1;
    color: #0c131f;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
  }

  .el-pager li.is-active {
    background: #396ffa;
    border-color: #396ffa;
    color: #fff;
  }

  .el-pagination__jump {
    height: 32px;
    width: 160px;
    box-sizing: border-box;
    margin: 0;
    padding: 2px 8px;
    border-radius: 3px;
    background: #f3f3f4;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    color: rgba(12, 19, 31, 0.6);
    font-size: 14px;
    line-height: 22px;

    .el-input {
      width: 36px;
      height: 28px;
      margin: 0 0 0 8px;
    }

    .el-input__wrapper {
      height: 28px;
      padding: 3px 8px;
      border-radius: 3px;
      box-shadow: 0 0 0 1px #dcdde1 inset;
    }

    .el-input__inner {
      height: 22px;
      color: #0c131f;
      font-size: 14px;
      line-height: 22px;
      text-align: center;
    }
  }
}

@media (max-width: 900px) {
  .document-list-card {
    padding: 12px 16px 8px;
  }

  .document-list-toolbar,
  .filter-panel,
  .pagination-row {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-right,
  .filter-actions {
    justify-content: flex-start;
  }

  .filter-grid {
    grid-template-columns: 1fr;
  }
}

/* ── Original extraction styles ── */
.assistant-shadow {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
.shadows {
  margin-top: 20px;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0px 4px 35px 0px #8195C82E;
}
:deep(.el-select) {
  &.table-select {
    .el-select__wrapper {
      height: 28px;
      max-height: 28px;
      min-height: auto;
      .el-select__input {
        font-size: 14px;
        line-height: 20px;
      }
    }
  }
}
:deep(.el-checkbox-group) {
  display: flex;
  margin: 4px 0;
  flex-direction: column;
  .el-checkbox {
    height: auto;
    margin-right: 0;
    padding: 8px 16px;
    color: #404653;
    &:hover {
      background-color: #F6F6FB;
    }
    &.is-checked .el-checkbox__label {
      color: #404653;
    }
  }
}
:deep(.el-radio-group) {
  width: 100%;
  flex-direction: row;
  justify-content: space-between;
  .el-radio {
    padding: 0;
    margin-right: 0;
    color: #404653;
    &:hover {
      color: #396FFA;
      background: transparent;
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
:deep(.el-checkbox-group) {
  display: flex;
  margin: 4px 0;
  flex-direction: column;
  .el-checkbox {
    height: auto;
    margin-right: 0;
    padding: 8px 16px;
    color: #404653;
    &:hover {
      background-color: #F6F6FB;
    }
    &.is-checked .el-checkbox__label {
      color: #404653;
    }
  }
}
.border-gradient {
  border: 1.5px solid transparent;
  &.active {
    background-image: linear-gradient(180deg, white, white),
    linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
    background-clip: padding-box, border-box;
    background-origin: padding-box, border-box;
  }
  &:hover {
    background-image: linear-gradient(180deg, white, white),
    linear-gradient(232.02deg, #396FFA -65.62%, #56F9C8 2.78%, #396FFA 98.62%);
    background-clip: padding-box, border-box;
    background-origin: padding-box, border-box;
  }
}
:deep() {
  svg.filter {
    min-width: 20px;
    rect {
      display: none;
    }
    path {
      fill: #94969D;
    }
    &:hover {
      path {
        fill: #396FFA;
      }
    }
    &.active {
      rect {
        display: unset;
      }
      path {
        fill: #396FFA;
      }
    }
  }
}
.template {
  :deep() {
    svg.option {
      rect {
        fill: white;
      }
      &:hover {
        rect {
          fill: #F3F6FF;
        }
      }
    }
  }
  &.active {
    :deep() {
      svg.option {
        rect {
          fill: #D7E2FE;
        }
        &:hover {
          rect {
            fill: #F3F6FF;
          }
        }
      }
    }
  }
}
.document-extraction * {
  font-family: 'Encode Sans';
}
.dash-box{
  height: 1.2px;                 /* 线粗 */
  width: 100%;                /* 线长（也可以 100%） */
  background: repeating-linear-gradient(
    90deg,
    #E2E3E5 0 8px,             /* 实线 8px */
    transparent 8px 16px       /* 间隔 8px */
  );
}
:deep(.el-overlay) {
  .el-dialog.is-align-center {
    padding: 20px;
  }
  .el-dialog.selectTemp {
    padding-bottom: 0;
  }
}
.card {
  border-color: #CDDBFF;
  width: calc(25% - 18px);
  :deep() {
    svg.action {
      cursor: pointer;
      rect {
        fill: white;
      }
      &:hover {
        rect {
          fill: #F3F6FF;
        }
        circle {
          fill: #396FFA;
        }
      }
      &.active {
        rect {
          fill: #F3F6FF;
        }
        circle {
          fill: #396FFA;
        }
      }
      rect {
        fill: transparent;
      }
    }
  }
  &:hover {
    box-shadow: 0px 4px 35px 0px #0029921A;
  }
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
.date {
  padding: 12px;
  text-align: left;
  position: relative;
  border-radius: 8px;
  .date-title {
    font-size: 14px;
    font-weight: 600;
    line-height: 20px;
    color: #0C131F;
    margin-bottom: 12px;
  }
  .tag-content {
    display: flex;
    .date-tag {
      font-size: 14px;
      cursor: pointer;
      font-weight: 500;
      padding: 4px 12px;
      line-height: 20px;
      color: #404653;
      white-space: nowrap;
      border-radius: 100px;
      background-color: #EBEDF0;
      & + .date-tag {
        margin-left: 12px;
      }
      &.active {
        color: white;
        background-color: #396FFA;
      }
    }
  }
  .select {
    display: flex;
    font-size: 14px;
    margin-top: 12px;
    line-height: 20px;
    color: #404653;
    font-weight: normal;
    align-items: center;
    white-space: nowrap;
    .input {
      width: 100%;
      cursor: pointer;
      font-size: 14px;
      margin-left: 8px;
      line-height: 20px;
      padding: 6px 12px;
      color: #888C94;
      position: relative;
      border-radius: 4px;
      padding-right: 28px;
      border: 1px solid #E2E3E5;
      svg {
        top: 8px;
        right: 12px;
        cursor: pointer;
        position: absolute;
      }
    }
  }
  .bottom {
    display: flex;
    font-size: 14px;
    margin-top: 20px;
    line-height: 20px;
    justify-content: flex-end;
    .ok {
      cursor: pointer;
      padding: 2px 8px;
      color: white;
      border-radius: 6px;
      background-color: #396FFA;
    }
    .clear {
      cursor: pointer;
      padding: 2px 8px;
      margin-left: 8px;
      color: #1F2633;
      border-radius: 6px;
      border: 1px solid #1F2633;
    }
  }
}
</style>

<style lang="scss">
.el-overlay.is-message-box .el-overlay-message-box .el-message-box.delete-file {
  max-width: 520px;
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
.el-popover.dateTip {
  padding: 0;
  margin-top: 16px !important;
}

@keyframes row-flash {
  0%, 100% { background-color: transparent; }
  50% { background-color: #FFF3D0; }
}
.el-table .row-highlight-flash td.el-table__cell {
  animation: row-flash 0.6s ease-in-out 2;
}
</style>
