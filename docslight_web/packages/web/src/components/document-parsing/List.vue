<template>
  <div v-if="mode === 'page'" class="parsing-list-page">
    <main class="document-list-shell">
      <section class="document-list-card">
        <div class="document-list-card__title">{{ t('parsing.list') }}</div>

        <div class="document-list-toolbar">
          <div class="toolbar-left">
            <label class="compact-search">
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
              <input v-model="searchQuery" type="text" :placeholder="t('parsing.searchFile')" @keyup.enter="getTableData">
            </label>
            <button class="icon-filter" type="button" @click="filtersVisible = !filtersVisible" :class="filtersVisible && 'is-active'">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M2.5 3.5h11L9.5 8v4l-3 1V8L2.5 3.5Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/></svg>
            </button>
          </div>
          <div class="toolbar-right">
            <button v-permission="'parse:upload'" class="primary-btn" type="button" @click="uploadDialogVisible = true">
              <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M8 10.5V2.75M4.75 6 8 2.75 11.25 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 10.25v2.5h10v-2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
              {{ t('parsing.upload') }}
            </button>
            <el-dropdown trigger="click" @command="handleBatchAction">
              <button class="outline-btn" type="button">
                {{ t('parsing.batchActions') }}
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="m4 6 4 4 4-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="restart">{{ t('parsing.batchRestart') }}</el-dropdown-item>
                  <el-dropdown-item command="export">{{ t('parsing.batchExport') }}</el-dropdown-item>
                  <el-dropdown-item command="delete">{{ t('parsing.batchDelete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div v-show="filtersVisible" class="filter-panel">
          <div class="filter-grid">
            <label class="filter-input">
              <input v-model="searchQuery" type="text" :placeholder="t('parsing.searchFile')" @keyup.enter="getTableData">
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
            </label>
            <el-select v-model="parsingStatusFilterValue" class="figma-select" multiple collapse-tags clearable :placeholder="t('parsing.selectParsingStatus')">
              <el-option :label="t('parsing.pending')" value="0" />
              <el-option :label="t('parsing.parsing')" value="1" />
              <el-option :label="t('parsing.success')" value="2" />
              <el-option :label="t('parsing.fail')" value="3" />
            </el-select>
            <el-select v-model="reviewStatusFilter" class="figma-select" clearable :placeholder="t('parsing.selectReviewStatus')">
              <el-option :label="t('parsing.confirmed')" value="confirmed" />
              <el-option :label="t('parsing.unconfirmed')" value="unconfirmed" />
            </el-select>
            <el-popover v-model:visible="timeFilter" placement="bottom" popper-class="dateTip" trigger="click" append-to-body>
              <template #reference>
                <button class="filter-input filter-date" type="button">
                  <span>{{ (singleDate || doubleDate.length) ? (singleDate || `${doubleDate[0]} ~ ${doubleDate[1]}`.replace(/-/g, '/')) : t('parsing.selectUploadTime') }}</span>
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
                <Calender @checkedDate="checkedDate" :userFirstLogin="userFirstLogin" v-if="double" />
                <SingleCalendar @singleCheckedDate="singleCheckedDate" :userFirstLogin="userFirstLogin" v-if="single" />
                <div class="bottom">
                  <div @click="checkDate" class="ok">{{ $t('extraction.ok') }}</div>
                  <div @click="singleDate = '', doubleDate = [], timeFilter = false, getTableData()" class="clear">{{ t('template.reset') }}</div>
                </div>
              </div>
            </el-popover>
          </div>
          <div class="filter-actions">
            <button class="search-btn" type="button" @click="getTableData">{{ t('parsing.search') }}</button>
            <button class="reset-btn" type="button" @click="resetFilters">{{ t('parsing.reset') }}</button>
          </div>
        </div>

        <div class="figma-table-wrap">
          <el-table ref="tableRef" :data="dataList" class="figma-table" @selection-change="handleSelectionChange" :row-key="rowKey">
            <el-table-column type="selection" width="46" align="center" />
            <el-table-column :label="t('parsing.fileName')" min-width="240">
              <template #default="scope">
                <div class="file-cell" @click="preview(scope.row)">
                  <svg viewBox="0 0 12 14.6667" fill="none" aria-hidden="true"><path d="M0 0H8.27614L12 3.72386V14.6667H0V0ZM10.3905 4L8 1.60948V4H10.3905ZM6.66667 1.33333H1.33333V13.3333H10.6667V5.33333H6.66667V1.33333ZM2.66667 7.33333H9.33333V8.66667H2.66667V7.33333ZM2.66667 10H9.33333V11.3333H2.66667V10Z" fill="currentColor"/></svg>
                  <span>{{ scope.row.fileName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('parsing.parsingStatus')" min-width="180">
              <template #default="scope">
                <span class="status-tag" :class="statusTone(scope.row.status)">{{ statusText(scope.row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('parsing.reviewStatus')" min-width="180">
              <template #default="scope">
                <span class="status-tag" :class="reviewTone(scope.row)">{{ reviewStatusText(scope.row) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('parsing.uploadTime')" min-width="220">
              <template #default="scope">
                <span class="upload-time">{{ formatUploadTime(scope.row.uploadTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('parsing.action')" width="160" align="left">
              <template #default="scope">
                <div class="row-actions">
                  <button type="button" title="Preview" @click="preview(scope.row)">
                    <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path fill-rule="evenodd" clip-rule="evenodd" d="M1.42716 8C1.50002 8.12591 1.59662 8.28637 1.71628 8.47165C2.02362 8.94754 2.47878 9.5804 3.06969 10.2107C4.26368 11.4843 5.933 12.6667 8 12.6667C10.067 12.6667 11.7363 11.4843 12.9303 10.2107C13.5212 9.5804 13.9764 8.94754 14.2837 8.47165C14.4034 8.28637 14.5 8.12591 14.5728 8C14.5 7.87409 14.4034 7.71363 14.2837 7.52835C13.9764 7.05246 13.5212 6.4196 12.9303 5.78929C11.7363 4.51571 10.067 3.33333 8 3.33333C5.933 3.33333 4.26368 4.51571 3.06969 5.78929C2.47878 6.4196 2.02362 7.05246 1.71628 7.52835C1.59662 7.71363 1.50002 7.87409 1.42716 8ZM15.3333 8C15.9296 7.70186 15.9294 7.70139 15.9294 7.70139L15.9283 7.69925L15.926 7.69469L15.9184 7.67988C15.9121 7.66752 15.9031 7.65021 15.8915 7.62829C15.8683 7.58447 15.8346 7.52215 15.7907 7.44399C15.7028 7.28776 15.5734 7.06768 15.4038 6.80498C15.0653 6.28088 14.5621 5.5804 13.903 4.87737C12.597 3.48429 10.5997 2 8 2C5.40033 2 3.40299 3.48429 2.09698 4.87737C1.43789 5.5804 0.93471 6.28088 0.596224 6.80498C0.426567 7.06768 0.297195 7.28776 0.209314 7.44399C0.165349 7.52215 0.131693 7.58447 0.108502 7.62829C0.0969045 7.65021 0.0879168 7.66752 0.0815586 7.67988L0.0739933 7.69469L0.0716926 7.69925L0.0709134 7.7008C0.0709134 7.7008 0.0703819 7.70186 0.666667 8L0.0703819 7.70186C-0.0234606 7.88954 -0.0234606 8.11046 0.0703819 8.29814L0.666667 8C0.0703819 8.29814 0.0703819 8.29814 0.0703819 8.29814L0.0716926 8.30075L0.0739933 8.30531L0.0815586 8.32012C0.0879168 8.33248 0.0969045 8.34979 0.108502 8.37171C0.131693 8.41553 0.165349 8.47785 0.209314 8.55601C0.297195 8.71224 0.426567 8.93232 0.596224 9.19502C0.93471 9.71913 1.43789 10.4196 2.09698 11.1226C3.40299 12.5157 5.40033 14 8 14C10.5997 14 12.597 12.5157 13.903 11.1226C14.5621 10.4196 15.0653 9.71913 15.4038 9.19502C15.5734 8.93232 15.7028 8.71224 15.7907 8.55601C15.8346 8.47785 15.8683 8.41553 15.8915 8.37171C15.9031 8.34979 15.9121 8.33248 15.9184 8.32012L15.926 8.30531L15.9283 8.30075L15.9291 8.2992C15.9291 8.2992 15.9296 8.29814 15.3333 8ZM15.3333 8L15.9296 8.29814C16.0235 8.11046 16.0232 7.88907 15.9294 7.70139L15.3333 8ZM8 6.66667C7.26362 6.66667 6.66667 7.26362 6.66667 8C6.66667 8.73638 7.26362 9.33333 8 9.33333C8.73638 9.33333 9.33333 8.73638 9.33333 8C9.33333 7.26362 8.73638 6.66667 8 6.66667ZM5.33333 8C5.33333 6.52724 6.52724 5.33333 8 5.33333C9.47276 5.33333 10.6667 6.52724 10.6667 8C10.6667 9.47276 9.47276 10.6667 8 10.6667C6.52724 10.6667 5.33333 9.47276 5.33333 8Z" fill="currentColor"/></svg>
                  </button>
                  <button type="button" title="Restart" @click="openRestartConfirm(scope.row)">
                    <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M13.7143 8C13.7143 11.1559 11.1559 13.7143 8 13.7143C6.11269 13.7143 4.43918 12.7996 3.39916 11.388" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/><path d="M2.28571 8C2.28571 4.84409 4.84409 2.28571 8 2.28571C9.88731 2.28571 11.5608 3.20043 12.6008 4.612" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/><path d="M12 1.71429L13.7143 4.57143H10.2857" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/><path d="M4 14.2857L2.28571 11.4286H5.71429" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                  <button type="button" title="Delete" @click="openDeleteConfirm([scope.row.fileId])">
                    <svg viewBox="0 0 13.3333 14.6667" fill="none" aria-hidden="true"><path d="M3.66667 0H9.66667V2H13.3333V3.33333H11.9807L11.6473 14.6667H1.68599L1.35265 3.33333H0V2H3.66667V0ZM5 2H8.33333V1.33333H5V2ZM2.68656 3.33333L2.98068 13.3333H10.3527L10.6468 3.33333H2.68656ZM7.33333 4.66667V12H6V4.66667H7.33333Z" fill="currentColor"/></svg>
                  </button>
                </div>
              </template>
            </el-table-column>
            <template #empty>
              <div class="table-empty">
                <img src="/images/kbEmpty.png" width="120" height="120" alt="Empty">
                <div>{{ searchQuery || parsingStatusFilterValue.length || singleDate || doubleDate.length ? t('extraction.searchEmpty') : t('extraction.noDocument') }}</div>
                <button v-if="!searchQuery && !parsingStatusFilterValue.length && !singleDate && !doubleDate.length" type="button" @click="uploadDialogVisible = true">{{ t('parsing.upload') }}</button>
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
            :prev-icon="PaginationPrevIcon"
            :next-icon="PaginationNextIcon"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            layout="sizes, prev, pager, next, jumper, slot"
          >
            <button class="editPagesize" @click="pageSizesSettingDialogVisible = !pageSizesSettingDialogVisible"><Edit /></button>
          </el-pagination>
        </div>
      </section>
    </main>
  <aside v-if="mode === 'sidebar'" class="detail-sidebar">
    <div class="detail-sidebar__head">
      <button class="detail-sidebar__back" type="button" @click="returnToList">
        <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M10 3.5 5.5 8l4.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
        {{ t('parsing.parsingDetails') }}
      </button>
      <button class="detail-sidebar__filter" type="button" aria-label="Filter">
        <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M2.5 3.5h11L9.5 8v4l-3 1V8L2.5 3.5Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/></svg>
      </button>
    </div>
    <div class="detail-sidebar__divider"></div>
    <div class="detail-sidebar__tabs">
      <button type="button" class="is-active">{{ t('parsing.all') }}</button>
      <button type="button">{{ t('parsing.confirmed') }}</button>
      <button type="button">{{ t('parsing.unconfirmed') }}</button>
    </div>
    <label class="detail-sidebar__search">
      <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M9.17 15.83a6.67 6.67 0 1 0 0-13.33 6.67 6.67 0 0 0 0 13.33ZM14.17 14.17 17.5 17.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
      <input v-model="searchQuery" type="text" :placeholder="t('parsing.searchFile')" @keyup.enter="getTableData">
    </label>
    <div class="detail-sidebar__divider"></div>
    <div class="detail-sidebar__list">
      <button
        v-for="row in dataList"
        :key="row.fileId"
        class="detail-file-item"
        :class="selectedFileId === row.fileId && 'is-selected'"
        type="button"
        @click="preview(row)"
      >
        <div class="detail-file-item__name">
          <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M4 2.5h5.5L12 5v8.5H4v-11Z" stroke="currentColor" stroke-width="1.2"/><path d="M9.5 2.5V5H12M5.8 8h4.4M5.8 10.5h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
          <span>{{ row.fileName }}</span>
        </div>
        <div class="detail-file-item__desc">{{ getFileExtension(row.fileName) || t('parsing.document') }}</div>
        <div class="detail-file-item__tags">
          <span class="mini-tag" :class="reviewTone(row)">{{ reviewStatusText(row) }}</span>
          <span class="mini-tag" :class="statusTone(row.status)">{{ statusText(row.status) }}</span>
        </div>
      </button>
    </div>
  </aside>

    <!-- 上传文件 -->
    <el-dialog v-model="uploadDialogVisible" align-center width="520px" :show-close="false" @closed="fileList = []">
      <!-- Title -->
      <h3 class="text-sm font-600 text-[#43474D] py-4px mb-16px">
        {{ t('parsing.upload') }}
      </h3>

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
          {{ t('parsing.selectFile[0]') }}
        </div>
        <div class="mt-12px text-xs text-[#8C8C8C]">{{ t('parsing.selectFile[1]') }}</div>
        <div class="text-xs text-[#8C8C8C]">{{ t('parsing.selectFile[2]') }}</div>
        <!-- Supported formats bar -->
        <div class="mt-12px rounded-6px bg-[#F6F6FB] px-12px py-8px text-xs text-[#8C8C8C] absolute bottom-0px left-0 rounded-10px w-full text-center">
          {{ t('parsing.support') }}
        </div>
      </div>
      <input ref="input" class="hidden" type="file" accept=".pdf, .jpg, .png, .jpeg, .tiff, .doc, .docx, .txt, .xls, .xlsx, .ppt" name="file" multiple @change="handleChange">

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

      <!-- Bottom buttons -->
      <div class="flex justify-center mt-24px">
        <div
          class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])"
          @click="uploadDialogVisible = false"
        >
          {{ t('parsing.cancel') }}
        </div>
        <div
          v-loading="loading"
          @click="handleUpload"
          :class="isUploadEnabled ? 'hover:bg-[#244FF0] cursor-pointer bg-[#396FFA]' : 'bg-[#88a9fc] cursor-not-allowed'"
          class="w-140px rounded-6px font-500 text-white text-sm py-8px px-10px flex items-center justify-center ml-12px"
        >
          {{ t('parsing.ok') }}
        </div>
      </div>
    </el-dialog>
    <!-- 文件导出 -->
    <el-dialog v-model="exportDialogVisible" align-center width="320px">
      <h3 class="text-sm font-500 text-[#404653] pb-8px">
        {{ t('parsing.selectFormat') }}
      </h3>
      <el-radio-group v-model="exportMethod">
        <el-radio value="JSON">JSON</el-radio>
        <el-radio value="TXT">TXT</el-radio>
        <el-radio value="MD">MarkDown</el-radio>
      </el-radio-group>
      <div class="flex justify-end mt-24px">
        <div class="w-fit rounded-6px cursor-pointer border-1 border-[#E2E3E5] text-xs text-[#0C131F] py-6px px-12px flex items-center justify-center hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])" @click="exportDialogVisible = false">
          {{ t('parsing.cancel') }}
        </div>
        <div v-loading="loading" @click="exportFile"
          class="w-fit rounded-6px text-white bg-[#396FFA] text-xs py-6px px-12px flex items-center justify-center ml-8px hover:bg-[#244FF0] cursor-pointer">
          {{ t('parsing.ok') }}
        </div>
      </div>
    </el-dialog>
    <el-dialog
      v-model="actionConfirmVisible"
      align-center
      width="480px"
      :show-close="false"
      :close-on-click-modal="false"
      custom-class="action-confirm-dialog"
    >
      <div class="confirm-card">
        <div class="confirm-card__header">
          <div class="confirm-card__title">
            <svg viewBox="0 0 22 22" fill="none" aria-hidden="true"><path d="M11 22C17.0751 22 22 17.0751 22 11C22 4.92487 17.0751 0 11 0C4.92487 0 0 4.92487 0 11C0 17.0751 4.92487 22 11 22ZM9.99597 7.50002V5.49611H11.9999V7.50002H9.99597ZM11.9999 9.00002L11.9999 16.5H9.99988V9.00002L11.9999 9.00002Z" fill="currentColor"/></svg>
            <span>{{ actionConfirmTitle }}</span>
          </div>
          <button class="confirm-card__close" type="button" aria-label="Close" @click="closeActionConfirm">
            <svg viewBox="0 0 8.48528 8.48528" fill="none" aria-hidden="true"><path d="M0.942809 0L4.24264 3.29983L7.54247 2.38419e-07L8.48528 0.942809L5.18545 4.24264L8.48528 7.54247L7.54247 8.48528L4.24264 5.18545L0.942809 8.48528L2.38419e-07 7.54247L3.29983 4.24264L0 0.942809L0.942809 0Z" fill="currentColor"/></svg>
          </button>
        </div>
        <div class="confirm-card__content">{{ actionConfirmMessage }}</div>
        <div class="confirm-card__footer">
          <button class="confirm-card__cancel" type="button" @click="closeActionConfirm">{{ t('parsing.cancel') }}</button>
          <button class="confirm-card__ok" type="button" @click="confirmAction">{{ t('parsing.ok') }}</button>
        </div>
      </div>
    </el-dialog>

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
    <ApiKeyRequiredDialog v-model="apiKeyDialogVisible" />
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { saveAs } from 'file-saver'
import Cookies from "js-cookie"
import request, { post, get } from '../../utils/request'
import { getSystemBaseUnit } from '../../utils/tools'
import { ElMessage } from 'element-plus'
import { ref, nextTick, onMounted, onUnmounted, defineAsyncComponent, inject, computed, h } from 'vue'
import Upload from '../images/SplittingUpload.vue'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
const Calender = defineAsyncComponent(() => import('../calendar/calendar.vue'))
const SingleCalendar = defineAsyncComponent(() => import('../calendar/singleCalendar.vue'))
import { useUploadTaskStore } from '../../stores/uploadTask'
import DeleteFile from '../images/DeleteFile.vue'
import Edit from '../images/Edit.vue'
import ApiKeyRequiredDialog from '../ApiKeyRequiredDialog.vue'

const PaginationPrevIcon = {
  render: () => h('svg', { class: 'pagination-chevron', viewBox: '0 0 5.55228 9.21895', fill: 'none', 'aria-hidden': 'true' }, [
    h('path', { d: 'M5.55228 8.27614L1.88562 4.60947L5.55228 0.942809L4.60948 0L0 4.60947L4.60948 9.21895L5.55228 8.27614Z', fill: 'currentColor' })
  ])
}

const PaginationNextIcon = {
  render: () => h('svg', { class: 'pagination-chevron', viewBox: '0 0 5.55228 9.21895', fill: 'none', 'aria-hidden': 'true' }, [
    h('path', { d: 'M8.74497e-08 8.27614L3.66667 4.60947L0 0.942809L0.942809 0L5.55228 4.60947L0.942809 9.21895L8.74497e-08 8.27614Z', fill: 'currentColor' })
  ])
}

const input = ref()
const apiKeyDialogVisible = ref(false)
const props = withDefaults(defineProps<{
  mode?: 'page' | 'sidebar'
  selectedFileId?: string
}>(), {
  mode: 'page',
  selectedFileId: ''
})

const mode = computed(() => props.mode)
const selectedFileId = computed(() => props.selectedFileId)
const { t } = useI18n()
const uploadTaskStore = useUploadTaskStore()
const tableRef = ref()
const dataList = ref<FileData[]>([])
const timeFilter = ref(false)
const templateList = ref([1])
const loading = ref(false)
const dragover = ref(false)
const searchQuery = ref('')
const exportMethod = ref<string>('json')
const fileList = ref<File[]>([])
const exportDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const actionConfirmVisible = ref(false)
const actionConfirmType = ref<'restart' | 'delete'>('delete')
const actionConfirmTitle = computed(() => actionConfirmType.value === 'restart'
  ? t('parsing.confirmRetryTitle')
  : t('parsing.deleteTitle'))
const actionConfirmMessage = computed(() => actionConfirmType.value === 'restart'
  ? t('parsing.confirmRetryTip')
  : t('parsing.deleteTip'))
const pendingRestartRow = ref<FileData | null>(null)
const pendingDeleteIds = ref<string[]>([])

const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const pageSizes = ref([10, 20, 50, 100])
const filtersVisible = ref(true)
const reviewStatusFilter = ref('')


const single = ref(false)
const double = ref(false)
const singleDate = ref('')
const dateType = ref('less')
const userFirstLogin = ref('2000-01-01T00:00:00')
const doubleDate = ref<string[]>([])
const customPageSize = ref(0)

const selectFile = ref<FileData | null>(null)

const rowKey = (row: any) => row.fileId

const updatePageSizes = () => {
  if (!customPageSize.value) return
  Cookies.set('pageSize', customPageSize.value.toString())
  pageSizes.value[3] = customPageSize.value
  pageSizesSettingDialogVisible.value = false
  handleSizeChange(customPageSize.value)
}
const pageSizesSettingDialogVisible = ref(false)

// 轮询定时器
let pollTimer: ReturnType<typeof setTimeout> | null = null
const POLL_INTERVAL = 3000 // 轮询间隔 3 秒

// 轮询查询文件处理状态
const pollFileStatus = async (fileIds: string[], previousSuccessCount = 0) => {
  // 清除之前的轮询
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }

  // 没有需要轮询的文件则停止
  if (!Array.isArray(fileIds) || fileIds.length === 0) return

  const queryString = fileIds.map(id => `fileIds=${id}`).join('&')

  try {
    const { data } = await get(`/api/idp/get-file-by-ids?${queryString}`)

    if (data.code === 200 && data.data) {
      const files = Array.isArray(data.data) ? data.data : [data.data]
      const currentSuccessCount = files.filter((file: any) => file.status === 2).length
      const failedFiles = files.filter((file: any) => file.status === 3)

      // Sync file statuses into the global upload panel
      files.forEach((file: any) => {
        if (file.status === 2) {
          uploadTaskStore.updateFile(file.fileId, { status: 'success' })
        }
        if (file.status === 3) {
          uploadTaskStore.updateFile(file.fileId, { status: 'fail' })
        }
      })

      if (currentSuccessCount > previousSuccessCount) {
        getTableData()
      }
      // 检查是否所有文件的 status 都是 2 或 3
      const allCompleted = files.every((file: any) => file.status === 2 || file.status === 3)

      if (allCompleted) {
        // 所有文件处理完成，刷新表格数据
        getTableData()
        if (failedFiles.length) {
          ElMessage.error(t('parsing.parseFail'))
        } else {
          ElMessage.success(t('parsing.success'))
        }
      } else {
        // 继续轮询
        pollTimer = setTimeout(() => pollFileStatus(fileIds, currentSuccessCount), POLL_INTERVAL)
      }
    }
  } catch {
    // 请求失败时继续轮询
    pollTimer = setTimeout(() => pollFileStatus(fileIds, previousSuccessCount), POLL_INTERVAL)
  }
}

const startParsing = async () => {
  const fileIds = selectFile.value?.fileId ? [selectFile.value.fileId] : getSelectedFileIds()
  if (!fileIds.length) return
  loading.value = true

  try {
    const formData = new FormData()
    fileIds.forEach(id => {
      formData.append('idpFileIds', id)
    })
    formData.append('type', 'LAYOUT')

    const { data } = await post('/api/idp/files-start', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
    loading.value = false
    if (data.code === 200 && data.message === 'success') {
      getTableData()
      // 开始轮询查询文件处理状态
      pollFileStatus(fileIds)
    }
  } catch {
    loading.value = false
    ElMessage.error(t('parsing.fail'))
  }
}

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
  getTableData()
}

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getTableData()
}

// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getTableData()
}

const getFileExtension = (fileName?: string): string => {
  if (!fileName) return ''
  const lastDotIndex = fileName.lastIndexOf('.')
  // 没有后缀 / 以 . 开头的隐藏文件 / 以 . 结尾
  if (lastDotIndex <= 0 || lastDotIndex === fileName.length - 1) return ''
  return fileName.slice(lastDotIndex + 1).toUpperCase()
}

const endTime = ref('')
const startTime = ref('')
const parsingStatusFilter = ref(false)
const parsingStatusFilterValue = ref([])

const resetFilters = () => {
  searchQuery.value = ''
  parsingStatusFilterValue.value = []
  reviewStatusFilter.value = ''
  singleDate.value = ''
  doubleDate.value = []
  startTime.value = ''
  endTime.value = ''
  currentPage.value = 1
  getTableData()
}

const handleBatchAction = (command: 'restart' | 'export' | 'delete') => {
  if (command === 'restart') startParsing()
  if (command === 'export') exportDialogVisible.value = true
  if (command === 'delete') deleteFile([])
}

const statusText = (status?: number) => {
  if (status === 2) return t('parsing.success')
  if (status === 1) return t('parsing.parsing')
  if (status === 3) return t('parsing.fail')
  return t('parsing.pending')
}

const statusTone = (status?: number) => {
  if (status === 2) return 'is-success'
  if (status === 1) return 'is-brand'
  if (status === 3) return 'is-error'
  return 'is-warning'
}

const reviewStatusText = (row: any) => {
  if (!isSuccessfulParsingStatus(row?.status)) return '--'
  const reviewStatus = row?.reviewStatus ?? row?.confirmStatus ?? row?.auditStatus ?? row?.review_status
  if (reviewStatus === 1 || reviewStatus === true || reviewStatus === 'confirmed' || reviewStatus === 'CONFIRMED') return t('parsing.confirmed')
  if (reviewStatus === 0 || reviewStatus === false || reviewStatus === 'unconfirmed' || reviewStatus === 'UNCONFIRMED') return t('parsing.unconfirmed')
  return '--'
}

const reviewTone = (row: any) => {
  if (!isSuccessfulParsingStatus(row?.status)) return 'is-neutral'
  const reviewStatus = row?.reviewStatus ?? row?.confirmStatus ?? row?.auditStatus ?? row?.review_status
  if (reviewStatus === 1 || reviewStatus === true || reviewStatus === 'confirmed' || reviewStatus === 'CONFIRMED') return 'is-success'
  if (reviewStatus === 0 || reviewStatus === false || reviewStatus === 'unconfirmed' || reviewStatus === 'UNCONFIRMED') return 'is-warning'
  return 'is-neutral'
}

const isSuccessfulParsingStatus = (status?: number) => status === 2

const formatUploadTime = (uploadTime?: string) => {
  if (!uploadTime) return '--'
  return dayjs(uploadTime).format('DD/MM/YYYY HH:mm:ss')
}

const getTableData = async () => {
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
  const { data } : any = await get(`/api/idp/getFileList?page=${currentPage.value}&pageSize=${pageSize.value}&fileName=${searchQuery.value}&taskType=LAYOUT&startTime=${startTime.value}&endTime=${endTime.value}&status=${parsingStatusFilterValue.value}`)
  // 记录当前已选中的文件 ID
  const selectedIds = new Set(selectFilesList.value.map(f => f.fileId))
  total.value = Number(data?.data?.total || 0)
  const records = Array.isArray(data?.data?.records) ? data.data.records : []
  dataList.value = records

  // 用列表返回的真实 fileId 替换 store 里同名的 tempId 条目（tempId 以 "parsing-" 开头）
  records.forEach((row: any) => {
    if (!row?.fileId || !row?.fileName) return
    const tempEntry = uploadTaskStore.fileList.find(
      f => f.id.startsWith('parsing-') && f.name === row.fileName && (f.status === 'uploading' || f.status === 'pending')
    )
    if (tempEntry) {
      uploadTaskStore.removeFile(tempEntry.id)
      uploadTaskStore.addFile({ id: row.fileId, name: row.fileName, status: 'pending', type: 'parsing' })
    }
  })

  // 恢复选中状态
  if (selectedIds.size > 0) {
    await nextTick()
    dataList.value.forEach((row: any) => {
      if (selectedIds.has(row.fileId)) {
        tableRef.value?.toggleRowSelection(row, true)
      }
    })
  }

  // 获取未处理(0)与处理中(1)的文件ID并开始轮询
  const fileIdsToPoll: string[] = Array.from(new Set(
    records
      .filter((row: any) => row && (row.status === 0 || row.status === 1))
      .map((row: any) => row.fileId)
      .filter((id: any): id is string => typeof id === 'string' && id.length > 0)
  ))
  void pollFileStatus(fileIdsToPoll)
}

const changeActive = inject('changeActive') as (active: string, row?: FileData) => void
const returnToList = () => {
  changeActive('list')
}
const preview = (row: FileData) => {
  changeActive('result', row)
}

const closeActionConfirm = () => {
  actionConfirmVisible.value = false
  pendingRestartRow.value = null
  pendingDeleteIds.value = []
}

const openRestartConfirm = (row: FileData) => {
  actionConfirmType.value = 'restart'
  pendingRestartRow.value = row
  pendingDeleteIds.value = []
  actionConfirmVisible.value = true
}

const openDeleteConfirm = (id: string[]) => {
  const fileIds = id?.length ? id : getSelectedFileIds()
  if (!fileIds.length) return
  actionConfirmType.value = 'delete'
  pendingDeleteIds.value = fileIds
  pendingRestartRow.value = null
  actionConfirmVisible.value = true
}

const restartFile = async (row: FileData) => {
  selectFile.value = row
  await startParsing()
  selectFile.value = null
}

const confirmAction = async () => {
  if (actionConfirmType.value === 'restart' && pendingRestartRow.value) {
    const row = pendingRestartRow.value
    closeActionConfirm()
    await restartFile(row)
    return
  }

  if (actionConfirmType.value === 'delete' && pendingDeleteIds.value.length) {
    const fileIds = [...pendingDeleteIds.value]
    closeActionConfirm()
    await executeDeleteFile(fileIds)
  }
}

onMounted(() => {
  const custom = Number(Cookies.get('pageSize'))
  if (custom) {
    customPageSize.value = custom
    updatePageSizes()
    pageSize.value = custom
  }
  getTableData()
})

onUnmounted(() => {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
})

// 获取抽取状态对应的标签类型
const getParsingStatusTxt = (progress: number) => {
  if (progress === 0) return t('extraction.pending')
  if (progress === 2) return t('extraction.completed')
  if (progress === 1) return t('extraction.processing')
  if (progress === 3) return t('extraction.fail')
}

// 格式化抽取状态
const getParsingStatusClass = (progress: number) => {
  if (progress === 0) return 'border-[#FFC0614D] bg-[#FFF4E5] text-[#F09004]'
  if (progress === 2) return 'border-[#00CF854D] bg-[#E2F7EF] text-[#00A66A]'
  if (progress === 1) return 'border-[#244FF04D] bg-[#D7E2FE] text-[#618CFB]'
  if (progress === 3) return 'border-[#F871714D] bg-[#FBEDED] text-[#F87171]'
}

interface FileData {
  fileId: string
  fileName: string
  pageCount: number
  uploadTime: string
  fileDownUrl: string
  status?: number
}
const selectFilesList = ref<FileData[]>([])

// 表格多选事件处理
const handleSelectionChange = (selection: FileData[]) => {
  selectFilesList.value = []
  selection.forEach((item: FileData) => {
    selectFilesList.value.push(item)
  })
}

const getSelectedFileIds = (): string[] => selectFilesList.value.map(item => item.fileId)

// 删除文件
const deleteFile = async (id: string[]) => {
  openDeleteConfirm(id)
}

const executeDeleteFile = async (fileIds: string[]) => {
  if (!fileIds.length) return

  try {
    loading.value = true
    const { data } = await get(`/api/idp/file-delete?fileIds=${fileIds}`)
    loading.value = false
    if (data.code === 200 && data.message === 'success') {
      getTableData()
      ElMessage.success(t('splitting.deleteSuccess'))
    }
  } catch {
    loading.value = false
    ElMessage.error(t('splitting.deleteFail'))
  }
}

// 导出文件
const exportFile = async () => {

  const fileIds = getSelectedFileIds()
  if (!fileIds.length) return

  loading.value = true
  try {
    const res = await request({
      method: 'post',
      url: '/api/idp/layout-export',
      data: {
        fileIds,
        exportFormat: exportMethod.value
      },
      responseType: 'blob'
    })

    const disposition = (res.headers?.['content-disposition'] as string | undefined) ?? ''
    const filenameMatch = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
    const filenameRaw = filenameMatch?.[1] ?? filenameMatch?.[2]

    const fallbackZipName = `'export'_compdf_ai_extract.zip`
    const parsedName = filenameRaw ? decodeURIComponent(filenameRaw) : fallbackZipName
    const filename = parsedName.toLowerCase().endsWith('.zip') ? parsedName : `${parsedName}.zip`

    const contentType = (res.headers?.['content-type'] as string | undefined) ?? 'application/zip'
    const blob = res.data instanceof Blob ? res.data : new Blob([res.data], { type: contentType })
    saveAs(blob, filename)

    loading.value = false
    ElMessage.success(t('extraction.success'))
  } catch {
    loading.value = false
    ElMessage.error(t('extraction.fail'))
  }
}

const base = getSystemBaseUnit()
const MAX_SIZE = base * base * 1000 // 10MB
const MAX_COUNT = 999
GlobalWorkerOptions.workerSrc = '/lib/pdf.worker.min.mjs'
const CMAP_URL = '/lib/cmaps/'

// 校验上传文件
const SUPPORTED_EXTENSIONS = ['.pdf', '.jpg', '.png', '.jpeg', '.tiff', '.doc', '.docx', '.txt', '.xls', '.xlsx', '.ppt']

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

// parsing 的 /api/idp/file-upload 返回 data: null，无法从上传响应中拿到 fileId
// 通过 code === 200 判断单个文件是否上传成功

// 上传单个文件到 parsing，返回上传结果
const uploadSingleParsingFile = (file: File): Promise<UploadFileResult> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('taskType', 'LAYOUT')
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

  // 1. 立即在全局上传面板中注册所有文件，状态为 uploading
  const uploadingEntries: { tempId: string; file: File }[] = fileList.value.map((file: File, i: number) => {
    const tempId = `parsing-${file.name}-${Date.now()}-${i}`
    uploadTaskStore.addFile({ id: tempId, name: file.name, status: 'uploading', type: 'parsing' })
    return { tempId, file }
  })

  // 2. 立即关闭弹窗、清空文件列表
  fileList.value = []
  uploadDialogVisible.value = false

  // 3. 并发上传每个文件，每个文件单独处理回调（fire-and-forget）
  uploadingEntries.forEach(({ tempId, file }) => {
    uploadSingleParsingFile(file).then(result => {
      if (result.success) {
        // 上传成功：标为 pending，等 getTableData 替换为真实 fileId 后开始轮询
        uploadTaskStore.updateFile(tempId, { status: 'pending' })
        getTableData()
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

const isUploadEnabled = computed<boolean>(() => {
  return fileList.value.length > 0
})

const handleUpload = () => {
  if (!isUploadEnabled.value) return
  upload()
}
</script>

<style lang="scss" scoped>
.parsing-list-page {
  min-height: calc(100vh - 56px);
  background: #f5f7ff;
  color: #0c131f;
  font-family: 'Encode Sans', 'Microsoft YaHei', sans-serif;
}

.document-list-shell {
  min-height: calc(100vh - 56px);
  padding: 16px;
  background: #f5f7ff;
  display: flex;
}

.document-list-card {
  width: 100%;
  min-height: calc(100vh - 144px);
  padding: 12px 32px 8px;
  border-radius: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 24px;

  &__title {
    font-family: 'Encode Sans Expanded', 'Encode Sans', sans-serif;
    font-size: 20px;
    font-weight: 600;
    line-height: 28px;
  }
}

.document-list-toolbar {
  min-height: 32px;
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
  grid-template-columns: repeat(2, minmax(220px, 1fr));
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
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.figma-select) {
  width: 100%;
  margin-bottom: 24px;

  .el-select__wrapper {
    min-height: 32px;
    border-radius: 3px;
    box-shadow: 0 0 0 1px #dcdde1 inset;
  }
}

:deep(.figma-table) {
  flex: 1 1 auto;
  color: #0c131f;

  .el-table__header,
  .el-table__body {
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
    color: rgba(12, 19, 31, 0.4);
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
    font-size: 14px;
    line-height: 22px;
  }

  .el-table__cell .cell {
    padding: 0 16px;
    line-height: 22px;
  }

  .el-table__empty-block {
    min-height: 360px;
  }

  .el-table__empty-text {
    width: 100%;
    line-height: normal;
    overflow: visible;
  }

  .el-checkbox__inner {
    width: 16px;
    height: 16px;
    border: 1px solid #aeb4bc !important;
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

.detail-sidebar {
  width: 230px;
  height: 100%;
  padding: 16px 12px;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 230px;
  color: #0c131f;
}

.detail-sidebar__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.detail-sidebar__back {
  min-width: 0;
  height: 32px;
  padding: 5px 0;
  border: 0;
  background: transparent;
  color: #0c131f;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
    padding: 0;
  }
}

.detail-sidebar__filter {
  width: 32px;
  height: 32px;
  border: 1px solid #dcdde1;
  border-radius: 3px;
  background: #fff;
  color: #0c131f;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }
}

.detail-sidebar__divider {
  height: 1px;
  background: #e7e8e8;
  flex: 0 0 auto;
}

.detail-sidebar__tabs {
  padding: 4px;
  border: 1px solid #d7e2fe;
  border-radius: 3px;
  background: #f5f7ff;
  display: flex;
  gap: 4px;

  button {
    min-width: 0;
    flex: 1 1 0;
    height: 24px;
    padding: 2px 8px;
    border: 0;
    border-radius: 3px;
    background: transparent;
    color: #0c131f;
    font-size: 12px;
    line-height: 20px;
    cursor: pointer;

    &.is-active {
      background: #fff;
      color: #396ffa;
    }
  }
}

.detail-sidebar__search {
  height: 32px;
  padding: 4px 8px 4px 4px;
  border-radius: 3px;
  background: #f3f3f4;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(12, 19, 31, 0.4);
  flex: 0 0 auto;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
  }

  input {
    min-width: 0;
    flex: 1 1 auto;
    border: 0;
    outline: 0;
    background: transparent;
    color: #0c131f;
    font-size: 14px;
    line-height: 22px;

    &::placeholder {
      color: rgba(12, 19, 31, 0.4);
    }
  }
}

.detail-sidebar__list {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-file-item {
  width: 100%;
  padding: 4px 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #fff;
  color: rgba(12, 19, 31, 0.6);
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;

  &.is-selected {
    border-color: #d7e2fe;
    background: #f5f7ff;
    color: #396ffa;
  }
}

.detail-file-item__name {
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

.detail-file-item__desc,
.detail-file-item__tags {
  margin-left: 20px;
}

.detail-file-item__desc {
  width: 122px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(12, 19, 31, 0.4);
  font-size: 12px;
  line-height: 20px;
}

.detail-file-item__tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-tag {
  display: inline-flex;
  align-items: center;
    padding: 0 16px;
    line-height: 22px;
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
  color: #618cfb;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
    color: rgba(12, 19, 31, 0.4);
  }

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-decoration: underline;
    text-underline-position: from-font;
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
}

.total-text {
  color: rgba(12, 19, 31, 0.6);
  font-size: 14px;
  line-height: 22px;
}

.table-empty {
  min-height: 280px;
  padding: 40px 0;
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
    font-size: 14px;
    line-height: 20px;
    cursor: pointer;
  }
}

:deep(.action-confirm-dialog) {
  width: 480px;
  border-radius: 9px;
  overflow: hidden;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.confirm-card {
  min-height: 222px;
  padding: 32px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.confirm-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.confirm-card__title {
  min-width: 0;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #0c131f;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;

  svg {
    width: 24px;
    height: 24px;
    flex: 0 0 auto;
    color: #396ffa;
  }

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.confirm-card__close {
  width: 20px;
  height: 20px;
  padding: 2px;
  border: 0;
  border-radius: 3px;
  background: transparent;
  color: rgba(12, 19, 31, 0.6);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  svg {
    width: 8.48528px;
    height: 8.48528px;
  }
}

.confirm-card__content {
  flex: 1 1 auto;
  padding: 16px 0 24px;
  color: rgba(12, 19, 31, 0.6);
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
}

.confirm-card__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.confirm-card__cancel,
.confirm-card__ok {
  min-width: 56px;
  height: 32px;
  padding: 5px 16px;
  border: 0;
  border-radius: 3px;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  cursor: pointer;
}

.confirm-card__cancel {
  background: #e7e8e8;
  color: #0c131f;
}

.confirm-card__ok {
  background: #396ffa;
  color: #fff;
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

  .pagination-chevron {
    width: 5.55228px;
    height: 9.21895px;
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

.header {
  img {
    cursor: pointer;
  }
}
:deep(.calender-wrap) {
  top: 0;
  right: calc(100% + 12px);
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
:deep(.el-radio-group) {
  width: 100%;
  flex-direction: row;
  justify-content: space-between;
  .el-radio {
    width: 50%;
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
}
:deep() {
  svg.filter {
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
}
.card {
  border-color: #CDDBFF;
  width: calc((100% - 18px) / 4);
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
  &:nth-child(4n) {
    margin-right: 0;
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
.editPagesize {
  margin-left: 11px;
}
.custom-pagesize {
  margin-bottom: 24px;
}
</style>

<style lang="scss">
.el-popover.dateTip {
  padding: 0;
  margin-top: 16px !important;
}
</style>
