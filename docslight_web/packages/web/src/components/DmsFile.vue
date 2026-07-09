<template>
  <div class="bg-[#F3F6FF] p-32px w-full min-h-[calc(100vh-81px)]">
    <h2 class="text-tiny text-[#0C131F] font-600">{{ t('dms.team_space.title') }}</h2>
    <div class="text-brand-1 text-sx mt-12px">{{ t('dms.team_space.description') }}</div>
    <div class="flex justify-between my-20px">
      <el-input class="max-w-300px" v-model="searchQueryFile" clearable @clear="getFolderFileList()" @input="getFolderFileList()" :placeholder="t('extraction.searchFile')">
        <template #prefix>
          <Search />
        </template>
      </el-input>
      <div class="flex">
        <div v-if="folderRole === 'editor' || folderRole === 'manager'" @click="dialogVisible = true" class="w-fit rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-16px flex items-center justify-center hover:(bg-[#396FFA] text-white)">
           <Upload class="mr-4px" />
           {{ t('dms.team_space.toolbar.upload') }}
         </div>
        <div v-show="selectFiles.length" @click.stop="batchAction = true" class="ml-12px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-16px w-fit flex items-center justify-center font-500 relative hover:bg-[#244FF0]">
          {{ t('dms.team_space.toolbar.batch_actions') }}
          <BatchAction class="ml-4px" />
           <div v-show="batchAction" class="assistant-shadow bg-white w-full z-3 p-4px rounded-4px absolute right-0 top-42px">
             <!-- 下载：所有角色可用 -->
             <div @click.stop.prevent="downloadFile(), batchAction = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
               {{ t('dms.team_space.file_actions_single.download') }}
             </div>
             <!-- 删除：仅当所有选中文件 role 为 manager 时可见 -->
             <div v-if="selectFiles.every(f => f.role === 'manager')" @click.stop.prevent="deleteFile(), batchAction = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
               {{ t('dms.team_space.file_actions_single.delete') }}
             </div>
             <!-- 移动到：仅当所有选中文件 role 为 editor 或 manager 时可见 -->
             <div v-if="selectFiles.every(f => f.role === 'editor' || f.role === 'manager')" @click.stop.prevent="moveDialogVisible = true, getFolderList(), batchAction = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
               {{ t('dms.team_space.file_actions_single.move_to') }}
             </div>
           </div>
        </div>
      </div>
    </div>
    <div class="bg-white shadows">
      <el-table :data="folderFileList" @selection-change="handleSelectionChange" :row-key="rowKey">
        <el-table-column type="selection" width="50" />
        <el-table-column :label="t('dms.team_space.table.columns.file_id')" prop="" align="left" width="100px">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column :label="t('dms.team_space.table.columns.name')" min-width="186px" show-overflow-tooltip>
          <template #default="scope">
            <div class="flex items-center">
              <Document class="min-w-20px mr-4px" />
              <div class="truncate">{{ scope.row.name }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="format" min-width="140px">
          <template #header>
            <el-popover v-model:visible="fileTypeFilter" placement="bottom" popper-class="dateTip" trigger="" append-to-body>
              <template #reference>
                <div @click.stop="fileTypeFilter = true" class="flex items-center justify-start cursor-pointer">
                  {{ t('dms.team_space.table.columns.types') }}
                  <FilterFile class="ml-4px filter" :class="fileTypeFilterValue.length && 'active'" />
                </div>
              </template>
              <div class="rounded-8px bg-white flex flex-col whitespace-nowrap min-w-180px">
                <el-checkbox-group v-model="fileTypeFilterValue" class="file-type-filter-group">
                  <el-checkbox label="PDF" value="pdf" />
                  <el-checkbox label="DOC" value="doc" />
                  <el-checkbox label="DOCX" value="docx" />
                  <el-checkbox label="XLS" value="xls" />
                  <el-checkbox label="XLSX" value="xlsx" />
                  <el-checkbox label="CSV" value="csv" />
                  <el-checkbox label="PPT" value="ppt" />
                  <el-checkbox label="PPTX" value="pptx" />
                  <el-checkbox label="TXT" value="txt" />
                  <el-checkbox label="JPG" value="jpg" />
                  <el-checkbox label="JPEG" value="jpeg" />
                  <el-checkbox label="PNG" value="png" />
                  <el-checkbox label="TIFF" value="tiff" />
                  <el-checkbox label="BMP" value="bmp" />
                </el-checkbox-group>
                <div class="h-1px w-full bg-[#0000000F]"></div>
                <div class="flex justify-center py-16px">
                  <div @click="fileTypeFilterValue = [], getFolderFileList(), fileTypeFilter = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                    {{ t('template.reset') }}
                  </div>
                  <div v-loading="loading" @click="getFolderFileList(), fileTypeFilter = false" :class="fileTypeFilterValue?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
                    class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px">
                    {{ t('extraction.ok') }}
                  </div>
                </div>
              </div>
            </el-popover>
          </template>
          <template #default="scope">
            <div class="flex items-center justify-start">
              <div class="truncate">{{ scope.row.file_type }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="size" :label="t('dms.team_space.table.columns.size')" min-width="140px" />
        <el-table-column  width="140px">
          <template #header>
            <el-popover v-model:visible="createFilter" placement="bottom" popper-class="dateTip" trigger="" append-to-body>
              <template #reference>
                <div @click.stop="createFilter = true" class="flex items-center justify-start cursor-pointer">
                  {{ t('dms.team_space.table.columns.creator') }}
                  <FilterFile class="ml-4px filter" :class="createFilterValue.length && 'active'" />
                </div>
              </template>
              <div class="rounded-8px bg-white flex flex-col whitespace-nowrap min-w-180px">
                <el-checkbox-group v-model="createFilterValue">
                  <el-checkbox v-for="user in createUserList" :value="user.id" :key="user.id">{{ user.nickname }}</el-checkbox>
                </el-checkbox-group>
                <div class="h-1px w-full bg-[#0000000F]"></div>
                <div class="flex justify-center py-16px">
                  <div @click="createFilterValue = [], getFolderFileList(), createFilter = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                    {{ t('template.reset') }}
                  </div>
                  <div v-loading="loading" @click="getFolderFileList(), createFilter = false" :class="createFilterValue?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
                    class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px">
                    {{ t('extraction.ok') }}
                  </div>
                </div>
              </div>
            </el-popover>
          </template>
          <template #default="scope">
            <div class="flex items-center justify-start">
              <div class="truncate">{{ scope.row.creator.nickname }}</div>
            </div>
          </template>
        </el-table-column>
        <!-- Permissions -->
        <!-- <el-table-column prop="pageCount" :label="t('dms.team_space.table.columns.permissions')" min-width="140px">
          <template #header>
            <el-popover v-model:visible="permissionFilter" placement="bottom" popper-class="dateTip" trigger="" append-to-body>
              <template #reference>
                <div @click.stop="permissionFilter = true" class="flex items-center justify-center cursor-pointer">
                  {{ t('dms.team_space.table.columns.permissions') }}
                  <FilterFile class="ml-4px filter" :class="permissionFilterValue.length && 'active'" />
                </div>
              </template>
              <div class="rounded-8px bg-white flex flex-col whitespace-nowrap min-w-180px">
                <el-checkbox-group v-model="permissionFilterValue">
                  <el-checkbox :label="t('dms.team_space.table.permissions.admin')" value="manager" />
                  <el-checkbox :label="t('dms.team_space.table.permissions.edit')" value="editor" />
                  <el-checkbox :label="t('dms.team_space.table.permissions.view')" value="reader" />
                </el-checkbox-group>
                <div class="h-1px w-full bg-[#0000000F]"></div>
                <div class="flex justify-center py-16px">
                  <div @click="permissionFilterValue = [], getFolderFileList(), permissionFilter = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                    {{ t('template.reset') }}
                  </div>
                  <div v-loading="loading" @click="getFolderFileList(), permissionFilter = false" :class="permissionFilterValue?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
                    class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px">
                    {{ t('extraction.ok') }}
                  </div>
                </div>
              </div>
            </el-popover>
          </template>
          <template #default="scope">
            <div class="flex items-center text-12px leading-16px">
              <div :class="getPermissionStatusClass(scope.row.role)" class="border-1 rounded-6px border-solid py-2px px-8px text-12px leading-16px min-w-115px py-6px mr-4px text-center">
                {{ getPermissionsStatusTxt(scope.row.role) }}
              </div>
            </div>
          </template>
        </el-table-column> -->
        <!-- Update Time -->
        <el-table-column prop="create_time" :label="t('extraction.time')" width="140px">
          <template #header>
            <el-popover v-model:visible="timeFilter" placement="bottom-end" popper-class="dateTip" trigger="" append-to-body>
              <template #reference>
                <div @click.stop="timeFilter = true" class="flex items-center justify-center cursor-pointer">
                  {{ t('extraction.time') }}
                  <FilterFile class="ml-4px filter" :class="(singleDate || doubleDate.length) && 'active'" />
                </div>
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
                  <div @click="singleDate = '', doubleDate = [], timeFilter = false, getFolderFileList()" class="clear">{{ t('template.reset') }}</div>
                </div>
              </div>
            </el-popover>
          </template>
          <template #default="scope">
            <div class="flex justify-start whitespace-nowrap">{{ dayjs.utc(scope.row.create_time).format('DD/MM/YYYY HH:mm:ss') }}</div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="t('extraction.action')" width="140px" align="center">
          <template #default="scope">
            <div class="flex items-center justify-center">
              <div v-if="scope.row.permission !== 1" @click="selectFile = scope.row, selectFiles = [], downloadFile()" class="text-brand-2 text-12px leading-16px mr-12px cursor-pointer">
                <DownloadFile class="cursor-pointer mr-12px downloadFile" />
              </div>
              <el-popover v-if="scope.row.role === 'editor' || scope.row.role === 'manager'" v-model:visible="fileStatusArr[scope.$index].status" placement="bottom-end" popper-class="action" trigger="" append-to-body>
                <template #reference>
                  <FileOption @click.stop="fileStatusArr[scope.$index].status = true" class="cursor-pointer svg fileOption" :class="fileStatusArr[scope.$index].status && 'active'" />
                </template>
                <div class="assistant-shadow bg-white z-3 p-4px rounded-4px">
                  <!-- 移动到：editor, manager 可用 -->
                  <div @click.stop.prevent="selectFile = scope.row, selectFiles = [], getFolderList(), fileStatusArr[scope.$index].status = false, moveDialogVisible = true" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                    {{ t('dms.team_space.file_actions_single.move_to') }}
                  </div>
                  <!-- 重命名：editor, manager 可用 -->
                  <div @click.stop.prevent="selectFile = scope.row, fileExtension = scope.row.name.slice(scope.row.name.lastIndexOf('.')), fileName = scope.row.name.slice(0, scope.row.name.lastIndexOf('.')), renameFileDialogVisible = true, fileStatusArr[scope.$index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                    {{ t('dms.team_space.file_actions_single.rename') }}
                  </div>
                  <!-- 删除：仅 manager 可用 -->
                  <div v-if="scope.row.role === 'manager'" @click.stop.prevent="selectFile = scope.row, selectFiles = [], deleteFile(), fileStatusArr[scope.$index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                    {{ t('dms.team_space.file_actions_single.delete') }}
                  </div>
                </div>
              </el-popover>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <!-- 筛选/搜索结果为空 -->
          <div v-if="searchQueryFile || createFilterValue.length || permissionFilterValue.length || fileTypeFilterValue.length || singleDate || doubleDate.length" class="w-full h-[calc(100vh-452px)] flex flex-col justify-center items-center">
            <img src="/images/search-empty.png" width="120" height="120" alt="Empty">
            <div class="text-[16px] leading-[24px] text-[#52555F] mt-8px">
              {{ t('extraction.searchEmpty') }}
            </div>
          </div>
          <!-- 无文档 -->
          <div v-else class="w-full h-[calc(100vh-452px)] flex flex-col justify-center items-center">
            <img src="/images/kbEmpty.png" width="120" height="120" alt="Empty">
            <div class="text-sm text-brand-3 mt-8px mb-32px max-w-600px text-center">
              {{ t('extraction.noDocument') }}
            </div>
            <div v-if="folderRole === 'editor' || folderRole === 'manager'" @click="dialogVisible = true" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px w-fit flex items-center justify-center font-500 hover:bg-[#244FF0]">
              <Upload class="mr-4px" />
              {{ t('extraction.upload') }}
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

    <!-- 重命名文件 -->
    <el-dialog v-model="renameFileDialogVisible" align-center width="480px">
      <h3 class="text-sm font-600 text-[#0C131F] mb-24px">
        {{ t('dms.team_space.rename.title') }}
      </h3>
      <div class="px-12px">
        <div class="text-sm font-500 text-[#404653] mb-12px">
          {{ t('dms.team_space.rename.name') }}
        </div>
        <div class="flex items-center">
          <el-input v-model="fileName" maxlength="50" :placeholder="t('dms.team_space.rename.placeholder')" :class="{ 'rename-error': fileNameError }" @keyup.enter="renameFile" />
          <span class="text-[#888C94] text-sm ml-8px shrink-0">{{ fileExtension }}</span>
        </div>
        <div v-if="fileNameError" class="text-[#F04438] text-xs mt-4px">{{ fileNameError }}</div>
      </div>
      <div class="flex justify-center mt-24px">
        <div @click="renameFileDialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
          {{ t('dms.team_space.rename.cancel') }}
        </div>
        <div v-loading="loading" @click="renameFile" :class="(fileName && !fileNameError) ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed pointer-events-none'"
          class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
          {{ t('dms.team_space.rename.ok') }}
        </div>
      </div>
    </el-dialog>

    <!-- 上传文件 -->
    <el-dialog v-model="dialogVisible" align-center width="520px" :show-close="false" @closed="uploadActiveTab = 'local'">
      <!-- Title -->
      <h3 class="text-sm font-600 text-[#43474D] py-4px mb-16px">
        {{ t('extraction.upload') }}
      </h3>

      <!-- Tabs -->
      <div class="flex border-b border-[#E2E3E5] mb-16px">
        <div
          v-for="tab in uploadTabs"
          :key="tab.key"
          @click="uploadActiveTab = tab.key"
          class="mr-24px pb-8px text-sm cursor-pointer"
          :class="uploadActiveTab === tab.key
            ? 'text-[#396FFA] font-600 border-b-2 border-[#396FFA]'
            : 'text-[#8C8C8C] font-400 hover:text-[#43474D]'"
        >
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
            @click="input?.click()"
            class="w-fit rounded-6px cursor-pointer border-1 border-[#396FFA] text-[#396FFA] text-sm font-500 py-8px px-16px flex items-center justify-center hover:(bg-[#EEF3FF]) active:(bg-[#D6E0FF])"
          >
            <Upload class="mr-4px" />
            {{ t('extraction.selectFile[0]') }}
          </div>
          <div class="mt-12px text-xs text-[#8C8C8C]">{{ t('extraction.selectFile[1]') }}</div>
          <div class="text-xs text-[#8C8C8C]">{{ t('extraction.selectFile[2]') }}</div>
          <!-- Supported formats bar -->
          <div class="mt-12px rounded-10px bg-[#F6F6FB] px-12px py-8px text-xs text-[#8C8C8C] absolute bottom-0px left-0 w-full text-center">
            {{ t('extraction.supportedFormats') }}
          </div>
        </div>
        <input ref="input" class="hidden" type="file" accept=".pdf, .jpg, .png, .jpeg, .tiff, .bmp, .doc, .docx, .xls, .xlsx, .csv, .ppt, .pptx, .txt" name="file" multiple @change="handleChange">

        <!-- Selected files panel -->
        <template v-if="fileList.length">
          <div class="flex justify-between items-center mt-12px mb-6px">
            <span class="text-xs text-[#43474D] font-500">{{ t('dms.team_space.upload.common.selected_count') }} ({{ fileList.length }})</span>
            <span @click="fileList = []" class="text-xs text-[#396FFA] cursor-pointer hover:underline">
              {{ t('dms.team_space.upload.common.clear') }}
            </span>
          </div>
          <div class="bg-[#F6F6FB] rounded-6px px-12px py-8px max-h-154px overflow-auto">
            <div v-for="(file, index) in fileList" :key="index" :class="index && 'mt-10px'" class="flex justify-between items-center">
              <div class="flex items-center min-w-0">
                <Success class="mr-8px min-w-24px flex-shrink-0" />
                <div class="truncate text-sm text-[#43474D]">{{ file.name }}</div>
              </div>
              <DeleteFile @click="deleteUploadFile(index)" class="cursor-pointer min-w-16px flex-shrink-0 ml-8px" />
            </div>
          </div>
        </template>
      </template>

      <!-- ── THIRD-PARTY TAB ── -->
      <template v-else-if="uploadActiveTab === 'thirdparty'">
        <div class="min-h-329px flex overflow-hidden">
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
              <span class="leading-tight">{{ platform.name }}</span>
            </div>
          </div>
          <!-- Right content -->
          <div class="flex-1 flex flex-col min-w-0">
            <!-- Unauthorized state -->
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
          @click="dialogVisible = false"
          class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])"
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

    <!-- 移动文件 -->
    <el-dialog v-model="moveDialogVisible" align-center width="480px">
      <h3 class="text-sm text-[#0C131F] font-600 mb-24px">
        {{ t('dms.team_space.move_to.title') }}
      </h3>
      <div class="ml-12px mb-24px">
        <div class="text-sm text-[#404653] font-500 mb-12px">
          {{ t('dms.team_space.move_to.selected_files') }}
        </div>
        <div class="text-sm text-[#2E59CA] w-full truncate">
          <template v-if="selectFiles.length">
            {{ selectFiles.map(file => file.name).join('、 ') }}
          </template>
          <template v-else>
            {{ selectFile?.name }}
          </template>
        </div>
      </div>
      <div class="border border-[#E2E3E5] rounded-4px p-12px overflow-auto max-h-304px">
        <div v-for="(folder, index) in folderList" :key="folder.id" class="py-4px pl-8px flex items-center rounded-4px"
          :class="[index && 'mt-8px', folder.id === selectFolderId && 'bg-[#D7E2FE]', folderId === folder.id ? 'cursor-not-allowed text-[#B7BABF]' : 'cursor-pointer']"
          @click="folderId === folder.id ? null : selectFolderId = folder.id"
        >
          <DocFolder class="mr-8px" />
          {{ folder.name }}
        </div>
      </div>
      <div class="flex justify-between items-center mt-24px">
        <div @click="openCreateFolderDialog(true)" class="text-[#2E59CA] flex items-center font-500 cursor-pointer">
          <Create class="mr-4px" />
          {{ t('dms.team_space.toolbar.new_folder') }}
        </div>
        <div class="flex justify-center">
          <div @click="moveDialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
            {{ t('extraction.cancel') }}
          </div>
          <div v-loading="loading" @click="moveFile" :class="selectFolderId ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
            class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
            {{ t('extraction.ok') }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { saveAs } from 'file-saver'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSystemBaseUnit } from '../utils/tools'
import Upload from '../components/images/SplittingUpload.vue'
import Success from './images/Success.vue'
import DeleteFile from './images/DeleteFile.vue'
import Google from './images/Google.vue'
import AWS from './images/Aws.vue'
import NAS from './images/Nas.vue'
import Notion from './images/Notion.vue'
import Trello from './images/Trello.vue'
import Gmail from './images/Gmail.vue'
import Gcs from './images/Gcs.vue'
import JSZip from 'jszip'
import FileArrow from './images/FileArrow.vue'
import DocFolder from './images/DocFolder.vue'
import Docs from './images/Docs.vue'
import Indeterminate from './images/Indeterminate.vue'
import Check from './images/Check.vue'
import Checked from './images/Checked.vue'
import request, { post, get, _delete } from '../utils/request'
import { ref, watch, computed, onMounted, defineAsyncComponent, inject } from 'vue'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import { useUploadTaskStore } from '../stores/uploadTask'
import { useStore } from '../stores'
const Calender = defineAsyncComponent(() => import('../components/calendar/calendar.vue'))
const SingleCalendar = defineAsyncComponent(() => import('../components/calendar/singleCalendar.vue'))

interface status {
 status: boolean
}

interface Creator {
  email: string
  id: string
  nickname: string
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

interface Creator {
  email: string
  id: string
  nickname: string
}

interface folderType {
  create_time: number
  creator: Creator
  file_count: number
  id: string,
  name: string
  role: string
}

const { t } = useI18n()
const store = useStore()
const total = ref(0)
const batchAction = ref(false)
const pageSize = ref(10)
const loading = ref(false)
const currentPage = ref(1)
const dialogVisible = ref(false)

// ── Upload dialog tab state ──
type UploadTab = 'local' | 'thirdparty'
const uploadActiveTab = ref<UploadTab>('local')
const uploadTabs = computed(() => [
  { key: 'local' as UploadTab, label: t('dms.team_space.upload.tabs.local') },
  { key: 'thirdparty' as UploadTab, label: t('dms.team_space.upload.tabs.third_party') },
])

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

const selectedPlatform = ref('google-drive')

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

// 跳转至对应平台授权页
const authorizeThirdParty = () => {
  const routePath = PLATFORM_ROUTE_MAP[selectedPlatform.value]
  if (routePath) location.href = routePath
}

// 辅助：获取节点缓存 key
const getDmsItemKey = (item: DmsFileItem): string => item.path ?? item.id

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

// 上传第三方文件到团队空间
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
      ElMessage.error(t('splitting.fail'))
      loading.value = false
      return
    }

    // 所有文件从第三方下载完成 → 注册 store，立即关闭弹窗
    const realEntries: { tempId: string; file: File }[] = uploadFiles.map((file, i) => {
      const tempId = `splitting-third-${file.name}-${Date.now()}-${i}`
      uploadTaskStore.addFile({ id: tempId, name: file.name, status: 'uploading', type: 'splitting' })
      return { tempId, file }
    })
    dialogVisible.value = false
    loading.value = false

    // 并发上传每个文件（fire-and-forget）
    realEntries.forEach(({ tempId, file }) => {
      uploadSingleDmsFile(file).then(succeeded => {
        if (succeeded) {
          uploadTaskStore.updateFile(tempId, { status: 'success' })
          getFolderFileList()
        } else {
          uploadTaskStore.updateFile(tempId, { status: 'uploadFail' })
          ElMessage.error(`${t('splitting.fail')}: ${file.name}`)
        }
      })
    })
  } catch {
    ElMessage.error(t('splitting.fail'))
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
const selectFolderId = ref('')
const moveDialogVisible = ref(false)
const pageSizes = ref([5, 10, 20])
const createUserList = ref<Creator[]>([])
const renameFileDialogVisible = ref(false)
const selectFiles = ref<DocumentFile[]>([])
const selectFile = ref<DocumentFile | null>(null)
const folderList = ref<folderType[]>([])
const uploadTaskStore = useUploadTaskStore()

onMounted(() => {
  addEventListener('click', () => {
    batchAction.value = false
    fileStatusArr.value.forEach(item => item.status = false)
  })
})

const openCreateFolderDialog = inject('openCreateFolderDialog', (_isMove?: boolean) => {})
const changeFolder = inject('changeFolder', (_id: string) => {}) as (id: string) => void

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getFolderFileList()
}

// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getFolderFileList()
}

// 表格多选事件处理
const handleSelectionChange = (selection: DocumentFile[]) => {
  selectFile.value = null
  selectFiles.value = []
  selection.forEach((item: DocumentFile) => {
    selectFiles.value.push(item)
  })
}

const rowKey = (row: DocumentFile) => row.id

const endTime = ref('')
const fileTypeFilter = ref(false)
const fileTypeFilterValue = ref<string[]>([])
const fileName = ref('')
const fileExtension = ref('')
const INVALID_FILENAME_CHARS = /[\\/:*?"<>|]/
const fileNameError = computed(() => {
  if (!fileName.value || !fileName.value.trim()) return t('dms.team_space.rename.error_empty')
  if (INVALID_FILENAME_CHARS.test(fileName.value)) return t('dms.team_space.rename.error_invalid_chars')
  return ''
})
const folderId = ref('')
const folderRole = ref('')
const startTime = ref('')
const single = ref(false)
const double = ref(false)
const singleDate = ref('')
const dateType = ref('less')
const timeFilter = ref(false)
const searchQueryFile = ref('')
const createFilter = ref(false)
const createFilterValue = ref([])
const permissionFilter = ref(false)
const doubleDate = ref<string[]>([])
const permissionFilterValue = ref([])
const fileStatusArr = ref<status[]>([])
const folderFileList = ref<DocumentFile[]>([])
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
    singleDate.value = `${date}T23:59:59`
  } else if (dateType.value === 'more') {
    singleDate.value = `${date}T00:00:00`
  } else if (dateType.value === 'equal') {
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
  getFolderFileList()
}

const changeFolderId = (id: string, role?: string) => {
  folderId.value = id
  folderRole.value = role || ''
  getFolderFileList(id)
}

const getFolderFileList = async (_id?: string) => {
  loading.value = true
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
  let searchQuery = `?page=${currentPage.value}`
    + `&page_size=${pageSize.value}`
    + `&create_date_start=${startTime.value}`
    + `&create_date_end=${endTime.value}`
    + `&keywords=${searchQueryFile.value}`
    + `&file_type=${fileTypeFilterValue.value}`
    + `&creator_id=${createFilterValue.value}`
    + `&role=${permissionFilterValue.value}`
    + `&folder_id=${folderId.value || ''}`

  const { data } = await get(`/v1/team_space/file/search${searchQuery}`)
  const { data: user } = await get(`/v1/team_space/folder_members?folder_id=${folderId.value || ''}`)
  createUserList.value = (user.data || []).map((u: any) => ({ id: u.user_id || u.id, email: u.email, nickname: u.nickname }))
  loading.value = false
  total.value = data.data.total
  folderFileList.value = data.data.files
  fileStatusArr.value = []
  folderFileList.value.forEach(()=> {
    fileStatusArr.value.push({ status: false })
  })
}

// 权限状态
const getPermissionStatusClass = (role: string) => {
  if (role === 'manager') return 'border-[#FFC0614D] bg-[#FFF4E5] text-[#F09004]'
  if (role === 'editor') return 'border-[#00CF854D] bg-[#E2F7EF] text-[#00CF85]'
  if (role === 'reader') return 'border-[#244FF04D] bg-[#D7E2FE] text-[#2E59CA]'
}

// 获取权限状态对应的标签类型
const getPermissionsStatusTxt = (role: string) => {
  if (role === 'manager') return t('dms.team_space.table.permissions.admin')
  if (role === 'editor') return t('dms.team_space.table.permissions.edit')
  if (role === 'reader') return t('dms.team_space.table.permissions.view')
}

const getSelectedFileIds = (): string[] => selectFiles.value.map(item => item.id)

const getFolderList = async () => {
  const { data: { data } } = await get('/v1/team_space/root_folders')
  folderList.value = data.folders
}

// 下载文件
const downloadFile = async () => {
  const isBatch = !selectFile.value
  const fileIds = selectFile.value ? [selectFile.value?.id] : getSelectedFileIds()
  if (fileIds.length === 0) return

  // 批量下载时弹出确认弹窗
  if (isBatch) {
    try {
      await ElMessageBox.confirm(
        t('dms.team_space.batch_download.description'),
        t('dms.team_space.batch_download.title'),
        {
          confirmButtonText: t('dms.team_space.batch_download.actions.ok'),
          cancelButtonText: t('dms.team_space.batch_download.actions.cancel'),
          type: 'warning',
          customClass: 'delete-file'
        }
      )
    } catch {
      return
    }
  }

  const loadingMsg = isBatch
    ? ElMessage({ message: t('dms.team_space.batch_download.status_packaging'), duration: 0 })
    : null

  try {
    const res = await request({
      method: 'post',
      url: '/v1/team_space/file/download',
      data: {
        file_ids: fileIds
      },
      responseType: 'blob'
    })
    const disposition = (res.headers?.['content-disposition'] as string | undefined) ?? ''
    const filenameMatch = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
    const filenameRaw = filenameMatch?.[1] ?? filenameMatch?.[2]

    const now = new Date()
    const ts = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
    let baseName = filenameRaw ? decodeURIComponent(filenameRaw) : (selectFile.value?.name || 'download')
    const dotIndex = baseName.lastIndexOf('.')
    const filename = dotIndex > 0
      ? `${baseName.slice(0, dotIndex)}_${ts}${baseName.slice(dotIndex)}`
      : `${baseName}_${ts}`
  
    const contentType = (res.headers?.['content-type'] as string | undefined) ?? 'application/octet-stream'
    const blob = res.data instanceof Blob ? res.data : new Blob([res.data], { type: contentType })
    saveAs(blob, filename)
    loadingMsg?.close()
    ElMessage.success(isBatch ? t('dms.team_space.batch_download.status_success') : t('splitting.success'))
  } catch (error) {
    loadingMsg?.close()
    ElMessage.error(t('splitting.fail'))
    return
  }
}

// 重命名文件
const renameFile = async () => {
  const { data } = await post('/v1/team_space/file/rename',
    {
      file_id: selectFile.value?.id,
      name: fileName.value + fileExtension.value
    }
  )
  if (data.code === 0 && data.message === 'success') {
    getFolderFileList()
    renameFileDialogVisible.value = false
    ElMessage.success(t('splitting.success'))
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}

// 移动文件
const moveFile = async () => {
  if (!selectFolderId.value) return
  const fileIds = selectFile.value ? [selectFile.value?.id] : getSelectedFileIds()
  if (fileIds.length === 0) return

  const { data } = await post('/v1/team_space/file/move',
    {
      target_folder_id: selectFolderId.value,
      file_ids: fileIds
    }
  )
  moveDialogVisible.value = false
  if (data.code === 0 && data.message === 'success') {
    ElMessage.success(t('splitting.success'))
    changeFolder(selectFolderId.value)
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}

// 删除文件
const deleteFile = async () => {
  const fileIds = selectFile.value ? [selectFile.value?.id] : getSelectedFileIds()
  if (!fileIds.length) return

  try {
    await ElMessageBox.confirm(
      t('dms.team_space.delete_confirmation_file.description'),
      t('dms.team_space.delete_confirmation_file.title'),
      {
        confirmButtonText: t('dms.team_space.delete_confirmation_file.actions.ok'),
        cancelButtonText: t('dms.team_space.delete_confirmation_file.actions.cancel'),
        type: 'warning',
        customClass: 'delete-file'
      }
    )
  } catch {
    return
  }

  loading.value = true
  const { data } = await _delete(`/v1/team_space/file`,
    {
      file_ids: fileIds
    }
  )
  if (data.code === 0 && data.message === 'success') {
    getFolderFileList()
    ElMessage.success(t('splitting.success'))
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}


const dragover = ref(false)
const fileList = ref<File[]>([])
const input = ref<HTMLInputElement | null>(null)

const base = getSystemBaseUnit()
const MAX_SIZE = base * base * 100 // 10MB
const MAX_COUNT = 32
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

// 上传单个文件到团队空间，返回是否成功
const uploadSingleDmsFile = async (file: File): Promise<boolean> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder_id', folderId.value || '')
  try {
    const res = await post('/v1/team_space/file/upload', formData, {}, {
      headers: { 'Content-Type': 'multipart/form-data' } as any
    })
    return res?.data?.code === 0
  } catch {
    return false
  }
}

// 上传文件到团队空间
const upload = () => {
  if (!fileList.value.length) return

  // 立即在全局上传面板中注册所有文件，状态为 uploading
  const uploadingEntries: { tempId: string; file: File }[] = fileList.value.map((file: File, i: number) => {
    const tempId = `splitting-${file.name}-${Date.now()}-${i}`
    uploadTaskStore.addFile({ id: tempId, name: file.name, status: 'uploading', type: 'splitting' })
    return { tempId, file }
  })

  // 立即关闭弹窗、清空文件列表
  fileList.value = []
  dialogVisible.value = false

  // 并发上传每个文件（fire-and-forget）
  uploadingEntries.forEach(({ tempId, file }) => {
    uploadSingleDmsFile(file).then(succeeded => {
      if (succeeded) {
        // 团队空间上传即完成，无需轮询
        uploadTaskStore.updateFile(tempId, { status: 'success' })
        getFolderFileList()
      } else {
        uploadTaskStore.updateFile(tempId, { status: 'uploadFail' })
        ElMessage.error(`${t('splitting.fail')}: ${file.name}`)
      }
    })
  })
}

// ── isUploadEnabled：当前 tab 下是否有可上传的文件 ──
const isUploadEnabled = computed<boolean>(() => {
  if (uploadActiveTab.value === 'local') return fileList.value.length > 0
  if (uploadActiveTab.value === 'thirdparty') return thirdPartySelectedItems.value.length > 0
  return false
})

// ── handleUpload：根据当前 tab 调度对应的上传方法 ──
const handleUpload = () => {
  if (!isUploadEnabled.value) return
  if (uploadActiveTab.value === 'local') upload()
  else if (uploadActiveTab.value === 'thirdparty') uploadThirdPartyFiles()
}

// ── 上传对话框：打开时加载凭证，关闭时重置所有状态 ──
watch(dialogVisible, async (newVal) => {
  if (newVal) {
    // 加载第三方凭证
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
    thirdPartyDialogFileList.value = []
    thirdPartyDialogCache.clear()
    selectedPlatform.value = 'google-drive'
    thirdPartyCredentials.value = Object.fromEntries(thirdPartyPlatforms.map(p => [p.key, null]))
  }
})

const deleteUploadFile = (index: number) => {
  fileList.value.splice(index, 1)
}

const leave = () => {
  dragover.value = false
}

defineExpose({
  dialogVisible,
  changeFolderId,
  getFolderList
})
</script>

<style lang="scss" scoped>
.shadows {
  margin-top: 20px;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0px 4px 35px 0px #8195C82E;
}
.assistant-shadow {
  box-shadow: 0px 4px 35px 0px #0029921A;
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
:deep(.el-checkbox-group) {
  display: flex;
  margin: 4px 0;
  flex-direction: column;
  &.file-type-filter-group {
    max-height: 280px;
    overflow-y: auto;
  }
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
:deep(.rename-error) {
  .el-input__wrapper {
    box-shadow: 0 0 0 1px #F04438 !important;
  }
}
</style>
