<template>
  <div class="bg-[#F3F6FF] p-32px w-full min-h-[calc(100vh-81px)]">
    <h2 class="text-tiny text-[#0C131F] font-600">{{ t('dms.team_space.title') }}</h2>
    <div class="text-brand-1 text-sx mt-12px">{{ t('dms.team_space.description') }}</div>
    <div class="flex justify-between my-20px">
      <el-input class="max-w-300px" v-model="searchQueryFolder" clearable @clear="getFolderList" @input="getFolderList" :placeholder="t('dms.scanner_inbox.search.by_folder_name')">
        <template #prefix>
          <Search />
        </template>
      </el-input>
      <div class="flex">
        <div v-if="store.role === 'manager'" @click="folderName = '', createFolderDialogVisible = true" class="w-fit rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-16px mx-12px flex items-center justify-center hover:(bg-[#396FFA] text-white)">
          <Create class="mr-4px" />
          {{ t('dms.team_space.toolbar.new_folder') }}
        </div>
        <div v-show="selectFolders.length" @click.stop="batchAction = true" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-7px px-16px w-fit flex items-center justify-center font-500 relative hover:bg-[#244FF0]">
          {{ t('dms.team_space.toolbar.batch_actions') }}
          <BatchAction class="ml-4px" />
          <div v-show="batchAction" class="assistant-shadow bg-white w-full z-3 p-4px rounded-4px absolute right-0 top-42px">
            <div @click.stop.prevent="downloadFile()" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
              {{ t('dms.team_space.batch_actions.download') }}
            </div>
            <div v-if="selectFolders.every(f => f.role === 'manager')" @click.stop.prevent="deleteFolder(selectFolder?.id)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
              {{ t('dms.team_space.batch_actions.delete') }}
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="bg-white shadows">
      <el-table :data="folderList" @selection-change="handleSelectionChange" :row-key="rowKey">
        <el-table-column type="selection" width="50" />
        <el-table-column :label="t('dms.team_space.table.columns.folder_id')" prop="" align="left" width="100px">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column :label="t('dms.team_space.table.columns.name')" min-width="186px" show-overflow-tooltip>
          <template #default="scope">
            <div @click="changeFolder(scope.row.id)" class="flex items-center cursor-pointer hover:text-[#396FFA]">
              <DocFolder class="min-w-20px mr-4px" />
              <div class="truncate">{{ scope.row.name }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('dms.team_space.table.columns.types')" width="140px">
          <template #default>
            <div class="flex items-center justify-start">
              <div class="truncate">{{ t('dms.team_space.table.columns.folder') }}</div>
            </div>
          </template>
        </el-table-column>
        <!-- Creator -->
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
                  <div @click="createFilterValue = [], getFolderList(), createFilter = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                    {{ t('template.reset') }}
                  </div>
                  <div v-loading="loading" @click="getFolderList(), createFilter = false" :class="createFilterValue?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
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
        <el-table-column prop="pageCount" width="140px">
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
                  <div @click="permissionFilterValue = [], getFolderList(), permissionFilter = false" class="w-70px rounded-6px cursor-pointer text-xs text-[#2E59CA] py-2px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
                    {{ t('template.reset') }}
                  </div>
                  <div v-loading="loading" @click="getFolderList(), permissionFilter = false" :class="permissionFilterValue?.length ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
                    class="w-70px rounded-6px text-white bg-[#396FFA] text-xs py-2px flex items-center justify-center ml-12px">
                    {{ t('dms.team_space.folder.new_folder.ok') }}
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
        </el-table-column>
        <!-- Update Time -->
        <el-table-column prop="create_time" :label="t('extraction.time')" width="150px">
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
                  <div @click="singleDate = '', doubleDate = [], timeFilter = false, getFolderList()" class="clear">{{ t('template.reset') }}</div>
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
              <div v-if="scope.row.permission !== 1" @click="downloadFile(scope.row.id)" class="text-brand-2 text-12px leading-16px mr-12px cursor-pointer">
                <DownloadFile class="cursor-pointer mr-12px downloadFile" />
              </div>
              <el-popover v-if="scope.row.role === 'editor' || scope.row.role === 'manager'" v-model:visible="folderTableStatusArr[scope.$index].status" placement="bottom-end" popper-class="action" trigger="" append-to-body>
                <template #reference>
                  <FileOption @click="folderTableStatusArr[scope.$index].status = true" class="cursor-pointer svg fileOption" :class="folderTableStatusArr[scope.$index].status && 'active'" />
                </template>
                <div class="assistant-shadow bg-white z-3 p-4px rounded-4px">
                  <!-- 上传文件：editor, manager 可用 -->
                  <div @click.stop.prevent="changeFolder(scope.row.id, true), dialogVisible = true, folderTableStatusArr[scope.$index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px whitespace-normal hover:(text-brand-2 bg-[#1460F31A])">
                    {{ t('dms.team_space.file_actions_single.upload_file') }}
                  </div>
                  <!-- 权限设置：仅 manager 可用 -->
                  <div v-if="scope.row.role === 'manager'" @click.stop.prevent="openDialog(scope.row.id, scope.$index), selectFolder = scope.row, folderTableStatusArr[scope.$index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                    {{ t('dms.team_space.file_actions_single.set_permissions') }}
                  </div>
                  <!-- 重命名：editor, manager 可用 -->
                  <div @click.stop.prevent="renameFolderDialogVisible = true, folderName = scope.row.name, selectFolder = scope.row, folderTableStatusArr[scope.$index].status = false" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                    {{ t('dms.team_space.file_actions_single.rename') }}
                  </div>
                  <!-- 删除：仅 manager 可用 -->
                  <div v-if="scope.row.role === 'manager'" @click.stop.prevent="deleteFolder(scope.row.id, scope.$index)" class="flex py-8px px-12px text-sm font-600 text-brand-0 cursor-pointer rounded-6px hover:(text-brand-2 bg-[#1460F31A])">
                    {{ t('dms.team_space.file_actions_single.delete') }}
                  </div>
                </div>
              </el-popover>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <!-- 筛选/搜索结果为空 -->
          <div v-if="searchQueryFolder || createFilterValue.length || singleDate || doubleDate.length" class="w-full h-[calc(100vh-452px)] flex flex-col justify-center items-center">
            <img src="/images/search-empty.png" width="120" height="120" alt="Empty">
            <div class="text-[16px] leading-[24px] text-[#52555F] mt-8px">
              {{ t('extraction.searchEmpty') }}
            </div>
          </div>
          <!-- 无文件夹 -->
          <div v-else class="w-full h-[calc(100vh-452px)] flex flex-col justify-center items-center">
            <img src="/images/kbEmpty.png" width="120" height="120" alt="Empty">
            <div class="text-sm text-brand-3 mt-8px mb-32px max-w-600px text-center">
              {{ t('extraction.noFolder') }}
            </div>
            <div v-if="store.role === 'manager'" @click="folderName = '', createFolderDialogVisible = true" class="rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px w-fit flex items-center justify-center font-500 hover:bg-[#244FF0]">
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

      <!-- 创建文件夹 -->
      <el-dialog v-model="createFolderDialogVisible" align-center width="480px">
        <h3 class="text-sm font-600 text-[#0C131F] mb-24px">{{ t('dms.team_space.folder.new_folder.title') }}</h3>
        <div class="px-12px">
          <div class="text-sm font-500 text-[#404653] mb-12px">{{ t('dms.team_space.folder.new_folder.folder_name') }}</div>
          <el-input v-model="folderName" maxlength="50" :placeholder="t('dms.team_space.folder.new_folder.placeholder')" />
        </div>
        <div class="flex justify-center mt-24px">
          <div @click="createFolderDialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
            {{ t('dms.team_space.folder.new_folder.cancel') }}
          </div>
          <div v-loading="loading" @click="createFolder" :class="folderName ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
            class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
            {{ t('dms.team_space.folder.new_folder.ok') }}
          </div>
        </div>
      </el-dialog>

      <!-- 重命名文件夹 -->
      <el-dialog v-model="renameFolderDialogVisible" align-center width="480px">
        <h3 class="text-sm font-600 text-[#0C131F] mb-24px">
          {{ t('dms.team_space.rename.title') }}
        </h3>
        <div class="px-12px">
          <div class="text-sm font-500 text-[#404653] mb-12px">
            {{ t('dms.team_space.rename.name') }}
          </div>
          <el-input v-model="folderName" maxlength="50" :placeholder="t('dms.team_space.rename.placeholder')" @keyup.enter="renameFolder" />
        </div>
        <div class="flex justify-center mt-24px">
          <div @click="renameFolderDialogVisible = false" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
            {{ t('dms.team_space.rename.cancel') }}
          </div>
          <div v-loading="loading" @click="renameFolder" :class="folderName ? 'hover:bg-[#244FF0] cursor-pointer' : 'opacity-50 cursor-not-allowed'"
            class="w-140px rounded-6px font-500 text-white bg-[#396FFA] text-sm py-8px px-10px flex items-center justify-center ml-12px">
            {{ t('dms.team_space.rename.ok') }}
          </div>
        </div>
      </el-dialog>

      <!-- 文件夹权限设置 -->
      <el-dialog v-model="setPermissionDialogVisible" align-center width="480px" append-to-body>
        <h3 class="text-16px font-600 text-[#0C131F] leading-24px mb-8px">
          {{ t('dms.team_space.permissions_dialog.title') }}
        </h3>
        <!-- 添加成员到当前文件夹 -->
        <p class="text-16px font-500 text-[#404653] leading-24px mb-12px">
          {{ t('dms.team_space.permissions_dialog.add_member') }}
        </p>
        <div class="flex items-center">
          <el-select v-model="selectedUsers"
            :placeholder="t('dms.team_space.permissions_dialog.search_placeholder')"
            multiple filterable remote class="max-w-348px"
            collapse-tags :reserve-keyword="false"
            :remote-method="remoteMethod" value-key="user_id">
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.nickname"
              :value="user"
              :disabled="user.containKB"
            >
              <div class="flex items-center">
                <div class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
                  {{ user?.nickname?.slice(0, 1).toUpperCase() }}
                </div>
                <div class="flex flex-col">
                  <div class="text-[#18191B] text-14px leading-20px font-600">{{ user.nickname }}</div>
                  <div class="text-[#94969D] text-12px leading-16px">{{ user.email }}</div>
                </div>
              </div>
            </el-option>
            <template v-if="selectedUsers.length" #tag>
              <el-tag class="custom" closable @close="removeUser(item)" v-for="(item, index) in selectedUsers" :key="index">
                <div class="flex items-center">
                  <div class="bg-[#FFE248] rounded-full w-20px h-20px text-xs font-600 text-brand-0 flex justify-center items-center mr-4px">
                    {{ item?.nickname?.slice(0, 1).toUpperCase() }}
                  </div>
                  {{ item.nickname }}
                </div>
              </el-tag>
            </template>
          </el-select>
          <el-select v-model="roleMember" class="max-w-96px ml-8px" popper-class="role-select-popper">
            <el-option value="reader">
              <div class="text-xs font-500">{{ t('dms.team_space.permissions_dialog.roles.reader.name') }}</div>
              <div class="text-xs font-normal text-[#94969D]">{{ t('dms.team_space.permissions_dialog.roles.reader.description') }}</div>
            </el-option>
            <el-option value="editor">
              <div class="text-xs font-500">{{ t('dms.team_space.permissions_dialog.roles.editor.name') }}</div>
              <div class="text-xs font-normal text-[#94969D]">{{ t('dms.team_space.permissions_dialog.roles.editor.description') }}</div>
            </el-option>
            <el-option value="manager">
              <div class="text-xs font-500">{{ t('dms.team_space.permissions_dialog.roles.manager.name') }}</div>
              <div class="text-xs font-normal text-[#94969D]">{{ t('dms.team_space.permissions_dialog.roles.manager.description') }}</div>
            </el-option>
            <template #label="{ value }">
              <span>{{ t(`dms.team_space.permissions_dialog.roles.${value}.name`) }}</span>
            </template>
          </el-select>
          <div @click="addPermission" class="border border-[#618CFB] text-[#2E59CA] w-80px flex justify-center items-center ml-12px rounded-8px h-40px cursor-pointer hover:(bg-[#D7E2FE] text-[#618CFB])">
            {{ t('dms.team_space.permissions_dialog.ok') }}
          </div>
        </div>
        <!-- 文件夹成员列表 -->
        <div class="border-t border-[#E1E3E8] mt-24px flex flex-col">
          <p class="text-16px font-500 text-[#404653] leading-24px mt-8px">
            {{ t('dms.team_space.permissions_dialog.current_members') }}
          </p>
          <div :class="[selectFolder?.creator.id === user.user_id ? 'order-1' : 'order-2', user.new && 'border border-[#B7BABF] rounded-4px', user.change === 'delete' && 'hidden']" v-for="(user, index) in folderMemberList" :key="index" class="mt-8px py-6px px-8px flex items-center justify-between relative">
            <div class="flex">
              <div class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">{{ user?.nickname?.slice(0, 1).toUpperCase() }}</div>
              <div class="flex flex-col">
                <div class="text-[#18191B] text-14px leading-20px font-600 truncate max-w-200px">{{ user.nickname }}</div>
                <div class="text-[#94969D] text-12px leading-16px truncate max-w-200px">{{ user.email }}</div>
              </div>
            </div>
            <div @click.stop="changeStatus(index, user)" :class="[canChangeUserRole(user) && 'cursor-pointer', getRoleStatusClass(user.change ?? user.role)]" class="w-80px flex justify-center items-center rounded-4px py-6px px-4px border-1 text-12px leading-16px rounded-6px">
              {{ t(`dms.team_space.permissions_dialog.roles.${user.change ?? user.role}.name`) }}
              <RoleArrow class="ml-8px" v-show="canChangeUserRole(user)" />
            </div>
            <div v-show="statusArr[index].status" class="absolute z-2 right-8px top-32px bg-white shadows text-brand-0 text-xs p-4px rounded-4px max-w-240px">
              <div v-if="selectFolder?.creator.id === userInfo?.id" @click="changePermission('manager', user)" class="text-xs font-500 py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">
                {{ t('dms.team_space.permissions_dialog.roles.manager.name') }}
                <div class="font-normal">
                  {{ t('dms.team_space.permissions_dialog.roles.manager.description') }}
                </div>
              </div>
              <div @click="changePermission('editor', user)" class="text-xs font-500 py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">
                {{ t('dms.team_space.permissions_dialog.roles.editor.name') }}
                <div class="font-normal">
                  {{ t('dms.team_space.permissions_dialog.roles.editor.description') }}
                </div>
              </div>
              <div @click="changePermission('reader', user)" class="text-xs font-500 py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">
                {{ t('dms.team_space.permissions_dialog.roles.reader.name') }}
                <div class="font-normal">
                  {{ t('dms.team_space.permissions_dialog.roles.reader.description') }}
                </div>
              </div>
              <div @click="changePermission('delete', user)" class="text-xs font-500 py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">
                {{ t('dms.team_space.permissions_dialog.remove') }}
              </div>
            </div>
          </div>
        </div>
        <div class="flex justify-center mt-24px">
          <div @click="closeDialog" class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#E2E3E5] text-sm text-[#0C131F] py-8px px-10px flex items-center justify-center font-500 mr-12px hover:(bg-[#F6F6FB] text-[#396FFA]) active:(bg-[#EBEDF0] text-[#88A9FC])">
            {{ t('dms.team_space.rename.cancel') }}
          </div>
          <div v-loading="loading" @click="submit" :class="(selectedUsers || save) ? 'hover:bg-[#244FF0]' : 'opacity-50 cursor-not-allowed'" class="w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center justify-center font-500">
            {{ t('knowledgeBases.configuration.save') }}
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { useCookies } from "vue3-cookies"
import { post, get, _delete } from '../utils/request'
import request from '../utils/request'
import { saveAs } from 'file-saver'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ref, onMounted, defineAsyncComponent, inject } from 'vue'
import { useStore } from '../stores'
const Calender = defineAsyncComponent(() => import('../components/calendar/calendar.vue'))
const SingleCalendar = defineAsyncComponent(() => import('../components/calendar/singleCalendar.vue'))

const store = useStore()
const changeFolder = inject('changeFolder', (_val: string, _open?: boolean) => {})
const { cookies } = useCookies()

const props = defineProps({
  folderList: {
    type: Array,
    default: () => []
  }
})
const userInfo = ref(cookies.get('idp_user') || JSON.parse(sessionStorage.getItem('idp_user') || '{}'))

interface FolderMemberList {
 create_time: number
 email: string
 nickname: string
 role: string
 update_time: number
 user_id: string
 new?: boolean
 containKB?: boolean
 change?: string
 is_creator?: boolean
}

type userListType = {
  avatar: string
  nickname: string
  email: string
  id: string
  containKB?: boolean
}

const save = ref(false)
const roleMember = ref('reader')
const statusArr = ref<status[]>([])
const userList = ref<userListType[]>([])
const selectedUsers = ref<userListType[]>([])
const folderMemberList = ref<FolderMemberList[]>([])

const openDialog = async (id: string, index?: number, folder?: folderType) => {
  if (folder) {
    selectFolder.value = folder
  }
  const { data: { data } } = await get(`/v1/team_space/folder_members?folder_id=${id}`)
  folderMemberList.value = data
  folderMemberList.value.forEach(()=> {
    statusArr.value.push({ status: false })
  })
  setPermissionDialogVisible.value = true
  if (index !== undefined) {
    folderStatusArr.value.forEach((item, indexArr: number) => {
      if (index === indexArr) {
        item.status = !item.status
      } else {
        item.status = false
      }
    })
  }
}

const removeUser = async (val: userListType) => {
  selectedUsers.value = selectedUsers.value.filter(user => user.id !== val.id)
}

// 判断当前用户是否可以修改目标用户的权限
const canChangeUserRole = (user: FolderMemberList) => {
  // 不能改自己的权限
  if (userInfo.value?.id === user.user_id) return false
  // 不能改创建者的权限
  if (user.is_creator || selectFolder.value?.creator.id === user.user_id) return false
  const isCreator = selectFolder.value?.creator.id === userInfo.value?.id
  // 创建者可以改任何人
  if (isCreator) return true
  const effectiveRole = user.change ?? user.role
  // 管理员只能改非管理员
  if (selectFolder.value?.role === 'manager' && effectiveRole !== 'manager') return true
  return false
}

// 打开权限编辑下拉
const changeStatus = (index: number, user: FolderMemberList) => {
  if (!canChangeUserRole(user)) return
  statusArr.value.forEach((item, indexArr: number) => {
    if (index === indexArr) {
      item.status = !item.status
    } else {
      item.status = false
    }
  })
}

// 编辑成员权限
const changePermission = async (val: string, role: FolderMemberList) => {
  if (val === role.role) return
  // 仅创建者可以分配管理员权限
  const isCreator = selectFolder.value?.creator.id === userInfo.value?.id
  if (val === 'manager' && !isCreator) return
  save.value = true
  if (val === 'delete') {
    const message = {
      manager: t('dms.team_space.permissions_dialog.remove_confirmation.admin', { xx: role.email }),
      editor: t('dms.team_space.permissions_dialog.remove_confirmation.edit', { xx: role.email }),
      reader: t('dms.team_space.permissions_dialog.remove_confirmation.view', { xx: role.email }),
    }
    ElMessageBox.confirm(message[role.role as keyof typeof message], t('dms.team_space.permissions_dialog.remove_confirmation.title'), {
      confirmButtonText: t('dms.team_space.permissions_dialog.remove_confirmation.actions.ok'),
      cancelButtonText: t('dms.team_space.permissions_dialog.remove_confirmation.actions.cancel'),
      type: 'warning',
      customClass: 'delete-file'
    }).then(async () => {
      folderMemberList.value.forEach((item, index) => {
        if (item.user_id === role.user_id) {
          folderMemberList.value[index].change = val
        }
      })
    }).catch(() => {})
  } else {
    folderMemberList.value.forEach((item, index) => {
      if (item.user_id === role.user_id) {
        folderMemberList.value[index].change = val
      }
    })
  }
}

const addPermission = async () => {
  save.value = true
  folderMemberList.value.push(...selectedUsers.value.map(user => ({
    user_id: user.id,
    nickname: user.nickname,
    email: user.email,
    role: roleMember.value,
    create_time: Date.now(),
    update_time: Date.now(),
    new: true
  })))
  statusArr.value.push(...selectedUsers.value.map(() => ({ status: false })))
  selectedUsers.value = []
}

const closeDialog = () => {
  setPermissionDialogVisible.value = false
  selectedUsers.value = []
  roleMember.value = 'reader'
}

const remoteMethod = (query: string) => {
  if (query) {
    setTimeout(async () => {
      const { data } = await get(`/v1/team_space/members?keyword=${query}`)
      userList.value = data.data.members.map((user: userListType) => ({
        ...user,
        containKB: folderMemberList.value.some(member => member.user_id === user.id)
      }))
    }, 300)
  } else {
    userList.value = []
  }
}

const submit = () => {
  folderMemberList.value.map(async member => {
    if (member.new) {
      await post('/v1/team_space/update_permission', {
        folder_id: selectFolder.value?.id as string,
        user_id: member.user_id,
        role: member.role
      })
    } else if (member.change && member.change !== member.role) {
      if (member.change === 'delete') {
        await post('/v1/team_space/remove_permission', {
          folder_id: selectFolder.value?.id as string,
          user_id: member.user_id
        })
      } else {
        await post('/v1/team_space/update_permission', {
          folder_id: selectFolder.value?.id as string,
          user_id: member.user_id,
          role: member.change
        })
      }
      ElMessage.success(t('dms.team_space.permissions_dialog.update_success'))
    }
  })
  setPermissionDialogVisible.value = false
  selectedUsers.value = []
  roleMember.value = 'reader'
}

interface status {
 status: boolean
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

const rowKey = (row: folderType) => row.id

// 表格多选事件处理
const handleSelectionChange = (selection: folderType[]) => {
  selectFolders.value = []
  selection.forEach((item: folderType) => {
    selectFolders.value.push(item)
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

// 获取权限状态对应的标签类型
const getRoleStatusClass = (role: string) => {
  if (role === 'manager') return 'border-[#FFC0614D] bg-[#FFF4E5] text-[#F09004]'
  if (role === 'editor') return 'border-[#00CF854D] bg-[#E2F7EF] text-[#00CF85]'
  if (role === 'reader') return 'border-[#244FF04D] bg-[#D7E2FE] text-[#2E59CA]'
}

// 改变每页显示条数
const handleSizeChange = (value: number) => {
  pageSize.value = value
  getFolderList()
}

// 改变当前页码
const handleCurrentChange = (value: number) => {
  currentPage.value = value
  getFolderList()
}

onMounted(() => {
  getFolderList()
  addEventListener('click', () => {
    batchAction.value = false
    statusArr.value.forEach(item => {
      item.status = false
    })
    folderStatusArr.value.forEach(item => item.status = false)
  })
})

const { t } = useI18n()
const total = ref(0)
const batchAction = ref(false)
const pageSize = ref(10)
const loading = ref(false)
const currentPage = ref(1)
const folderName = ref('')
const dialogVisible = ref(false)
const searchQueryFolder = ref('')
const pageSizes = ref([5, 10, 20])
const folderList = ref<folderType[]>([])
const folderStatusArr = ref<status[]>([])
const createUserList = ref<Creator[]>([])
const folderTableStatusArr = ref<status[]>([])
const createFolderDialogVisible = ref(false)
const renameFolderDialogVisible = ref(false)
const setPermissionDialogVisible = ref(false)


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
  getFolderList()
}

const getFolderList = async () => {
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
    + `&pageSize=${pageSize.value}`
    + `&create_date_start=${startTime.value}`
    + `&create_date_end=${endTime.value}`
    + `&keywords=${searchQueryFolder.value}`
    + `&role=${permissionFilterValue.value}`
    + `&creator_id=${createFilterValue.value}`
  const { data } = await get(`/v1/team_space/root_folders${searchQuery}`)
  total.value = data.data.total
  folderList.value = data.data.folders
  folderStatusArr.value = []
  folderTableStatusArr.value = []
  folderList.value.forEach(()=> {
    folderStatusArr.value.push({ status: false })
    folderTableStatusArr.value.push({ status: false })
  })
  const { data: { data: user } } = await get('/v1/team_space/folder_creators')
  createUserList.value = user
}

const endTime = ref('')
const startTime = ref('')
const single = ref(false)
const double = ref(false)
const singleDate = ref('')
const dateType = ref('less')
const timeFilter = ref(false)
const createFilter = ref(false)
const permissionFilter = ref(false)
const doubleDate = ref<string[]>([])
const createFilterValue = ref([])
const permissionFilterValue = ref([])
const userFirstLogin = ref('2000-01-01T00:00:00')

const provideChangeFolder = inject('provideChangeFolder') as Function
const provideGetFolderList = inject('provideGetFolderList') as Function

// 创建文件夹
const createFolder = async () => {
  if (folderName.value === '') return
  const { data } = await post('/v1/team_space/folder',
    {
      parent_id: null,
      name: folderName.value
    }
  )
  if (data.code === 0 && data.message === 'success') {
    getFolderList()
    provideGetFolderList()
    provideChangeFolder(data.data.id)
    createFolderDialogVisible.value = false
    ElMessage.success(t('splitting.success'))
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}

const selectFolder = ref<folderType | null>(null)
const selectFolders = ref<folderType[]>([])

const getSelectedFolderIds = (): string[] => selectFolders.value.map(item => item.id)

// 下载文件夹
const downloadFile = async (id?: string, index?: number) => {
  const isBatch = !id
  const folderIds = id ? [id] : getSelectedFolderIds()
  if (folderIds.length === 0) return

  // 批量下载时弹出确认弹窗
  if (isBatch) {
    try {
      await ElMessageBox.confirm(
        t('dms.team_space.batch_download.description_folder'),
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
        folder_id: folderIds
      },
      responseType: 'blob'
    })
    const disposition = (res.headers?.['content-disposition'] as string | undefined) ?? ''
    const filenameMatch = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
    const filenameRaw = filenameMatch?.[1] ?? filenameMatch?.[2]

    const now = new Date()
    const ts = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
    let baseName = filenameRaw ? decodeURIComponent(filenameRaw) : 'download'
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
  }
  if (index) {
    folderTableStatusArr.value[index].status = false
  }
}

// 删除文件夹
const deleteFolder = async (id?: string, index?: number) => {
  try {
    await ElMessageBox.confirm(
      t('dms.team_space.delete_confirmation_folder.description'),
      t('dms.team_space.delete_confirmation_folder.title'),
      {
        confirmButtonText: t('dms.team_space.delete_confirmation_folder.actions.ok'),
        cancelButtonText: t('dms.team_space.delete_confirmation_folder.actions.cancel'),
        type: 'warning',
        customClass: 'delete-file'
      }
    )
  } catch {
    return
  }

  const { data } = await _delete('/v1/team_space/folder',
    {
      folder_id: id
    }
  )
  if (data.code === 0 && data.message === 'success') {
    ElMessage.success(t('splitting.success'))
    getFolderList()
    provideGetFolderList()
  } else {
    ElMessage.error(t('splitting.fail'))
  }
  if (index) {
    folderTableStatusArr.value[index].status = false
  }
}

// 重命名文件夹
const renameFolder = async () => {
  const { data } = await post('/v1/team_space/folder/rename',
    {
      folder_id: selectFolder.value?.id,
      name: folderName.value
    }
  )
  if (data.code === 0 && data.message === 'success') {
    getFolderList()
    provideGetFolderList()
    renameFolderDialogVisible.value = false
    ElMessage.success(t('splitting.success'))
  } else {
    ElMessage.error(t('splitting.fail'))
  }
}

defineExpose({
  folderList,
  openDialog,
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
</style>
