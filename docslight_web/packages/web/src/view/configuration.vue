<template>
  <div class="bg-[#F3F6FF] min-h-100vh w-full">
    <KbHeader />
    <div class="flex min-h-[calc(100vh-88px)] pt-88px">
      <KbSidebar :kbDetail="kbDetail" />
      <div class="py-40px px-32px w-full">
        <div class="flex text-xs mb-36px">
          <a href="/knowledge-base" class="text-brand-1">{{ t('knowledgeBases.title') }}</a>
          <ArrowRight class="mx-12px" />
          <div class="text-brand-2">{{ t('knowledgeBases.configuration.title') }}</div>
        </div>
        <h1 class="text-32px leading-48px font-600 text-brand-0 mb-2px">
          {{ t('knowledgeBases.configuration.title') }}
        </h1>
        <div class="text-brand-0 text-sm mt-4px mb-42px">
          {{ t('knowledgeBases.configuration.desc') }}
        </div>
        <div class="cards flex w-[calc(100vw-380px)]" :class="!loading && 'scrollbar'">
          <div class="card bg-white rounded-10px p-40px w-full min-w-540px">
            <el-form :model="ruleForm" ref="ruleFormRef" :rules="rules" label-position="left">
              <el-form-item prop="name">
                <template #label>
                  <div class="py-8px">
                    <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
                    {{ t('knowledgeBases.configuration.name') }}
                  </div>
                </template>
                <el-input @input="disable = true" :disabled="role === 'viewer'" v-model="ruleForm.name" :placeholder="t('knowledgeBases.configuration.namePlaceholder')" />
              </el-form-item>
              <el-form-item prop="language" :label="t('knowledgeBases.configuration.cover')">
                <div class="flex">
                  <div v-show="ruleForm.avatar" class="w-120px h-120px flex justify-center items-center border border-[#CED6E1] p-8px rounded-4px mr-8px">
                    <img :src="ruleForm.avatar" alt="avatar" class="w-auto">
                  </div>
                  <input :disabled="role === 'viewer'" ref="input" class="hidden" type="file" name="file" accept=".png, .jpg, .jpeg" @change="handleChange">
                  <div @click="input.value = '', input.click()" :class="role === 'viewer' && 'bg-[#f5f7fa] text-[#a8abb2]'" class="relative p-16px bg-[#F3F6FF] w-120px h-120px border-[#CED6E1] border-dashed border flex justify-center items-center flex-col rounded-4px cursor-pointer text-[#52555F] rounded-4px text-14px leading-20px">
                    <Add class="mb-10px" />
                    {{ t('knowledgeBases.configuration.upload') }}
                  </div>
                </div>
              </el-form-item>
              <el-form-item :label="t('knowledgeBases.configuration.description')">
                <el-input @input="disable = true" :disabled="role === 'viewer'" v-model="ruleForm.description" :placeholder="t('knowledgeBases.configuration.descriptionPlaceholder')" />
              </el-form-item>
              <el-form-item>
                <template #label>
                  <div class="py-8px">
                    <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
                    {{ t('knowledgeBases.configuration.management') }}
                  </div>
                </template>
                <div @click="checkPermission" :class="role === 'viewer' && 'bg-[#f5f7fa]'" class="flex items-center cursor-pointer text-xs text-brand-1 py-8px px-12px rounded-4px border border-[#CED6E1] w-full">
                  <Management class="mr-16px" />
                  {{ t('knowledgeBases.configuration.management') }}
                </div>
              </el-form-item>
              <el-form-item>
                <template #label>
                  <div class="py-8px">
                    <span class="text-[#FF5050] font-700 text-18px leading-24px pr-4px">*</span>
                    {{ t('knowledgeBases.configuration.chunk') }}
                  </div>
                </template>
                <el-select @change="disable = true" :disabled="role === 'viewer'" v-model="ruleForm.chunkMethod">
                  <el-option :label="t('knowledgeBases.configuration.naive')" value="naive"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.qa')" value="qa"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.manual')" value="manual"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.table')" value="table"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.paper')" value="paper"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.book')" value="book"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.laws')" value="laws"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.presentation')" value="presentation"></el-option>
                  <el-option :label="t('knowledgeBases.configuration.one')" value="one"></el-option>
                  <!-- <el-option :label="t('knowledgeBases.configuration.tag')" value="tag"></el-option> -->
                </el-select>
              </el-form-item>
            </el-form>
            <div v-show="role !== 'viewer'" class="flex justify-start mt-40px">
              <div class="w-140px rounded-6px font-500 cursor-pointer border-1 border-[#396FFA] text-sm text-[#396FFA] py-8px px-10px mr-24px flex items-center justify-center font-500 hover:(bg-[#396FFA] text-white)" @click="router.go(-1)">
                {{ t('knowledgeBases.configuration.cancel') }}
              </div>
              <div :class="disable ? 'hover:bg-[#244FF0]' : 'opacity-50 cursor-not-allowed'" class="w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center justify-center font-500 hover:bg-[#244FF0]" @click="updateKb">
                {{ t('knowledgeBases.configuration.save') }}
              </div>
            </div>
          </div>
          <div class="card bg-white rounded-10px py-40px px-32px ml-12px max-w-436px w-full min-w-500px">
            <div v-show="ruleForm.chunkMethod === 'naive'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.generalDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3" v-html="t('knowledgeBases.configuration.generalDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-24px" v-html="t('knowledgeBases.configuration.generalDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 flex mt-4px">
                <div class="min-w-3px h-3px rounded-1/2 bg-brand-3 mr-6px mt-6px"></div>
                {{ t('knowledgeBases.configuration.generalDesc[3]') }}
              </div>
              <div class="text-12px leading-16px text-brand-3 flex mt-4px">
                <div class="min-w-3px h-3px rounded-1/2 bg-brand-3 mr-6px mt-6px"></div>
                {{ t('knowledgeBases.configuration.generalDesc[4]') }}
              </div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.generalDesc[5]')"></div>
              <div class="text-12px leading-16px text-brand-3" v-html="t('knowledgeBases.configuration.generalDesc[6]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/naive-01.svg" alt="" width="180" class="mt-8px mr-12px">
                <img src="../components/chunk-method/naive-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'qa'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.qaDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 my-3px" v-html="t('knowledgeBases.configuration.qaDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 flex mt-4px">
                <div class="min-w-3px h-3px rounded-1/2 bg-brand-3 mr-6px mt-6px"></div>
                <div v-html="t('knowledgeBases.configuration.qaDesc[2]')"></div>
              </div>
              <div class="text-12px leading-16px text-brand-3 flex mt-4px">
                <div class="min-w-3px h-3px rounded-1/2 bg-brand-3 mr-6px mt-6px"></div>
                <div v-html="t('knowledgeBases.configuration.qaDesc[3]')"></div>
              </div>
              <div class="text-12px leading-16px text-brand-3" v-html="t('knowledgeBases.configuration.qaDesc[4]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.qaDesc[5]')"></div>
              <div class="text-12px leading-16px text-brand-3" v-html="t('knowledgeBases.configuration.qaDesc[6]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/qa-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/qa-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'resume'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.resumeDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.resumeDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.resumeDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.resumeDesc[3]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.resumeDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.resumeDesc[5]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/resume-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/resume-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'manual'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.manualDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.manualDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.manualDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.manualDesc[3]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.manualDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.manualDesc[5]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/manual-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/manual-02.svg" alt="" width="180" class="mt-8px">
              </div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/manual-03.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/manual-04.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'table'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.tableDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tableDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tableDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 flex mt-4px">
                <div class="min-w-3px h-3px rounded-1/2 bg-brand-3 mr-6px mt-6px"></div>
                <div v-html="t('knowledgeBases.configuration.tableDesc[3]')"></div>
              </div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tableDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 flex mt-4px">
                <div class="min-w-3px h-3px rounded-1/2 bg-brand-3 mr-6px mt-6px"></div>
                <div v-html="t('knowledgeBases.configuration.tableDesc[5]')"></div>
              </div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tableDesc[6]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tableDesc[7]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tableDesc[8]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.tableDesc[9]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tableDesc[10]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/table-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/table-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'paper'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.paperDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.paperDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.paperDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.paperDesc[3]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.paperDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.paperDesc[5]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/paper-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/paper-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'book'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.bookDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.bookDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.bookDesc[2]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.bookDesc[3]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.bookDesc[4]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/book-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/book-02.svg" alt="" width="180" class="mt-8px">
              </div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/book-03.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/book-04.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'laws'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.lawsDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.lawsDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.lawsDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.lawsDesc[3]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.lawsDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.lawsDesc[5]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/law-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/law-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'presentation'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.presentationDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.presentationDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.presentationDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.presentationDesc[3]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.presentationDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.presentationDesc[5]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/presentation-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/presentation-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <div v-show="ruleForm.chunkMethod === 'one'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.oneDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.oneDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.oneDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.oneDesc[3]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.oneDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.oneDesc[5]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/one-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/one-02.svg" alt="" width="180" class="mt-8px">
              </div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/one-03.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/one-04.svg" alt="" width="180" class="mt-8px">
              </div>
            </div>
            <!-- <div v-show="ruleForm.chunkMethod === 'tag'">
              <div class="font-600 text-sm text-brand-0 mb-8px" v-html="t('knowledgeBases.configuration.tagDesc[0]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[1]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[2]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[3]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[4]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[5]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[6]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[7]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[8]')"></div>
              <div class="font-500 text-xs text-black mb-8px mt-24px" v-html="t('knowledgeBases.configuration.tagDesc[9]')"></div>
              <div class="text-12px leading-16px text-brand-3 mt-4px" v-html="t('knowledgeBases.configuration.tagDesc[10]')"></div>
              <div class="flex flex-wrap">
                <img src="../components/chunk-method/tag-01.svg" alt="" width="180" class="mr-12px mt-8px">
                <img src="../components/chunk-method/tag-02.svg" alt="" width="180" class="mt-8px">
              </div>
            </div> -->
          </div>
        </div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" align-center width="400px">
      <h3 class="text-sm font-600 text-[#43474D] py-4px mb-8px">
        {{ t('knowledgeBases.configuration.management') }}
      </h3>
      <!-- 添加成员到当前知识库 -->
      <div class="flex items-center">
        <el-select
          v-model="selectedUsers"
          :placeholder="t('knowledgeBases.configuration.selectUser')"
          multiple
          filterable
          remote
          class="max-w-274px"
          collapse-tags
          :reserve-keyword="false"
          :remote-method="remoteMethod"
          value-key="user_id">
          <el-option
            v-for="user in userList"
            :key="user.user_id"
            ::label="user.nickname"
            :value="user"
            :disabled="user.containKB"
          >
          <div class="flex items-center">
            <img v-if="user.avatar" :src="user.avatar" alt="avatar" class="w-32px h-32px rounded-1/2 mr-8px">
            <div v-else class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">
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
                <img v-if="item.avatar" :src="item.avatar" alt="avatar" class="w-32px h-32px rounded-1/2 mr-8px">
                <div v-else class="bg-[#FFE248] rounded-full w-20px h-20px text-xs font-600 text-brand-0 flex justify-center items-center mr-4px">
                  {{ item?.nickname?.slice(0, 1).toUpperCase() }}
                </div>
                {{ item.nickname }}
              </div>
            </el-tag>
          </template>
        </el-select>
        <el-select v-model="roleMember" class="max-w-96px ml-8px">
          <el-option value="viewer">{{ t('knowledgeBases.configuration.viewer') }}</el-option>
          <el-option value="manager">{{ t('knowledgeBases.configuration.manager') }}</el-option>
          <template #label="{ value }">
            <span>{{ t(`knowledgeBases.configuration.${value}`) }}</span>
          </template>
        </el-select>
      </div>
      <div class="w-full h-1px bg-[#E1E3E8] my-12px"></div>
      <!-- 复制知识库权限 -->
      <div class="text-brand-3 text-xs mr-8px mb-8px">
        {{ t('knowledgeBases.configuration.clone') }}
      </div>
      <div class="flex items-center">
        <el-select
          v-model="selectedKb"
          :placeholder="t('knowledgeBases.configuration.selectKb')"
          filterable
          remote
          class="kbList"
          @change="getCopyUsers"
          @focus="handleFilterKb"
          collapse-tags
          clearable
          value-key="kb_id">
          <template v-if="selectedKb" #label>
            <div class="flex items-center">
              <img v-if="selectedKb?.avatar" :src="selectedKb.avatar" alt="avatar" class="w-32px h-32px rounded-1/2 mr-8px">
              <div v-else class="bg-[#FFE248] rounded-full w-20px h-20px text-xs font-600 text-brand-0 flex justify-center items-center mr-4px">
                {{ selectedKb?.name?.slice(0, 1).toUpperCase() }}
              </div>
              <div class="truncate max-w-200px">{{ selectedKb.name }}</div>
            </div>
          </template>
          <el-option
            v-for="kb in kbrList"
            :key="kb.kb_id"
            ::label="kb.name"
            :value="kb"
          >
            <div class="flex items-center">
              <img v-if="kb.avatar" :src="kb.avatar" alt="avatar" class="w-32px h-32px mr-12px">
              <div v-else class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">{{ kb?.name?.slice(0, 1).toUpperCase() }}</div>
              <div class="flex flex-col">
                <div class="text-[#18191B] text-14px leading-20px font-600 truncate max-w-200px">{{ kb.name }}</div>
                <div class="text-[#94969D] text-12px leading-16px truncate max-w-200px">{{ kb.description }}</div>
              </div>
            </div>
          </el-option>
        </el-select>
      </div>
      <!-- 知识库成员列表 -->
      <div class="border-t border-[#E1E3E8] mt-24px flex flex-col">
        <div :class="user.role === 'owner' ? 'order-1' : 'order-2'" v-for="(user, index) in kbMemberList" :key="index" class="mt-8px py-6px px-8px flex items-center justify-between relative">
          <div class="flex">
            <img v-if="user.avatar" :src="user.avatar" alt="avatar" class="w-32px h-32px mr-12px">
            <div v-else class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">{{ user?.nickname?.slice(0, 1).toUpperCase() }}</div>
            <div class="flex flex-col">
              <div class="text-[#18191B] text-14px leading-20px font-600 truncate max-w-200px">{{ user.nickname }}</div>
              <div class="text-[#94969D] text-12px leading-16px truncate max-w-200px">{{ user.email }}</div>
            </div>
          </div>
          <div @click.stop="changeStatus(index, user.role)" :class="[statusArr[index].status && 'bg-[#F3F6FF]', user.role !== 'owner' && 'cursor-pointer']" class="flex items-center rounded-4px py-10px px-4px">
            {{ t(`knowledgeBases.configuration.${user.role}`) }}
            <ArrowRight class="rotate" v-show="user.role !== 'owner'" />
          </div>
          <div v-show="statusArr[index].status" class="absolute z-2 right-8px top-48px bg-white shadows text-brand-0 text-xs p-4px rounded-4px">
            <div @click="changePermission('manager', user)" class="py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">{{ t('knowledgeBases.configuration.manager') }}</div>
            <div @click="changePermission('viewer', user)" class="py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">{{ t('knowledgeBases.configuration.viewer') }}</div>
            <div @click="changePermission('delete', user)" class="py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">{{ t('knowledgeBases.configuration.delete') }}</div>
          </div>
        </div>
        <template v-if="copyUser.length">
          <div :class="user.role === 'owner' ? 'order-1' : 'order-2'" v-for="(user, index) in copyUser" :key="index" class="mt-8px py-6px px-8px flex items-center justify-between relative border border-[#CED6E1] rounded-4px">
            <div class="flex">
              <img v-if="user.avatar" :src="user.avatar" alt="avatar" class="w-32px h-32px mr-12px">
              <div v-else class="bg-[#FFE248] rounded-1/2 min-w-32px h-32px mr-12px py-4px px-8px text-sm text-brand-0 font-600 flex justify-center items-center">{{ user?.nickname?.slice(0, 1).toUpperCase() }}</div>
              <div class="flex flex-col">
                <div class="text-[#18191B] text-14px leading-20px font-600 truncate max-w-200px">{{ user.nickname }}</div>
                <div class="text-[#94969D] text-12px leading-16px truncate max-w-200px">{{ user.email }}</div>
              </div>
            </div>
            <div @click.stop="changeCopyStatus(index, user.role)" :class="[copyStatusArr[index].status && 'bg-[#F3F6FF]', user.role !== 'owner' && 'cursor-pointer']" class="flex items-center rounded-4px py-10px px-4px">
              {{ t(`knowledgeBases.configuration.${user.role}`) }}
              <ArrowRight class="rotate" v-show="user.role !== 'owner'" />
            </div>
            <div v-show="copyStatusArr[index].status" class="absolute z-2 right-8px top-48px bg-white shadows text-brand-0 text-xs p-4px rounded-4px">
              <div @click="copyUser[index].role = 'manager'" class="py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">{{ t('knowledgeBases.configuration.manager') }}</div>
              <div @click="copyUser[index].role = 'viewer'" class="py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">{{ t('knowledgeBases.configuration.viewer') }}</div>
              <div @click="deleteUser(user.user_id)" class="py-8px px-12px cursor-pointer rounded-6px hover:(bg-[#1460F31A] text-brand-2)">{{ t('knowledgeBases.configuration.delete') }}</div>
            </div>
          </div>
        </template>
      </div>
      <div v-loading="loadings" @click="submit" :class="(selectedKb || selectedUsers.length || save) ? 'hover:bg-[#244FF0]' : 'opacity-50 cursor-not-allowed'" class="center absolute bottom-12px left-[50%] w-140px rounded-6px font-500 cursor-pointer bg-[#396FFA] text-white text-sm py-8px px-10px flex items-center justify-center font-500">
        {{ t('knowledgeBases.configuration.save') }}
      </div>
    </el-dialog>
  </div>
</template>

<script lang='ts' setup>
import { useI18n } from 'vue-i18n'
import { get, post } from '../utils/request'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type UploadFile } from 'element-plus'
import { onMounted, ref, onBeforeUnmount, watch } from 'vue'
import ArrowRight from '../components/images/ArrowRight.vue'
import Management from '../components/images/Management.vue'

interface member {
  role: string
  email: string
  avatar: string
  user_id: string
  nickname: string
}
interface KbrList {
  name: string
  kb_id: string
  avatar: string
  description: string
}
interface kbInfo {
  email: string
  kb_id: string
  avatar: string
  user_id: string
  nickname: string
  containKB: boolean
}
interface status {
 status: boolean
}
const role = ref()
const input = ref()
const save = ref(false)
const { t } = useI18n()
const route = useRoute()
const ruleFormRef = ref()
const disable = ref(false)
const router = useRouter()
const loadings = ref(false)
const roleMember = ref('viewer')
const dialogVisible = ref(false)
const copyUser = ref<member[]>([])
const kbrList = ref<KbrList[]>([])
const userList = ref<kbInfo[]>([])
const statusArr = ref<status[]>([])
const selectedKb = ref<KbrList>()
const kbMemberList = ref<member[]>([])
const copyStatusArr = ref<status[]>([])
const selectedUsers = ref<kbInfo[]>([])
const ruleForm = ref({
  name: '',
  description: '',
  avatar: '',
  chunkMethod: ''
})
const rules = ref({
  name: [
    { required: true, message: t('knowledgeBases.configuration.namePlaceholder'), trigger: 'blur' },
    { max: 30, message: t('knowledgeBases.configuration.must'), trigger: 'blur' }
  ]
})
watch(() => selectedKb.value, (val) => {
  if (val) {
    disable.value = true
  }
})
watch(() => selectedUsers.value, (val) => {
  if (val.length) {
    disable.value = true
  }
})
const removeUser = async (val: kbInfo) => {
  selectedUsers.value = selectedUsers.value.filter(user => user.user_id !== val.user_id)
}
const deleteUser = async (val: string) => {
  copyUser.value = copyUser.value.filter(user => user.user_id !== val)
}
// 获取需要Copy users
const getCopyUsers = async () => {
  const { data } = await get(`/v1/kb/get-copy-kb-users?source_kb_id=${selectedKb.value?.kb_id}&target_kb_id=${route.query.id}`)
  copyUser.value = data.data
  copyUser.value.forEach(()=> {
    copyStatusArr.value.push({ status: false })
  })
}

// 提交权限弹窗数据
const submit = async () => {
  if (!selectedKb.value && !selectedUsers.value.length && !save) return
  const reqData: any = {
    kb_id: route.query.id,
    user_roles: []
  }
  const userMap = new Map()

  // 清空 user_roles
  reqData.user_roles = []

  // 合并两个数组，并打上标记
  const allUsers = [
    ...selectedUsers.value.map(user => ({ ...user, _from: 'selected' })),
    ...copyUser.value.map(user => ({ ...user, _from: 'copy' }))
  ]

  // 去重 + 权限以 manager 为准
  allUsers.forEach((user) => {
    const existing = userMap.get(user.user_id)
    if (!existing) {
      userMap.set(user.user_id, user)
    } else {
      // 如果已有记录，检查是否需要升级权限
      const currentRole = existing.role || (existing._from === 'selected' ? roleMember.value : '')
      const newRole = user.role || (user._from === 'selected' ? roleMember.value : '')
      if (currentRole === 'viewer' && newRole === 'manager') {
        userMap.set(user.user_id, user) // 用权限更高的 user 替换
      }
    }
  })

  // 构造最终的 user_roles 数组
  userMap.forEach((user) => {
    const role = user._from === 'selected' ? roleMember.value : user.role
    reqData.user_roles.push({
      user_id: user.user_id,
      role
    })
  })

  copyUser.value = []
  selectedUsers.value = []
  if (loadings.value) return
  loadings.value = true
  dialogVisible.value = false
  await post('/v1/kb/add-kb-users', reqData)
  save.value = false
  selectedKb.value = undefined
  setTimeout(() => {
    loadings.value = false
  }, 300)
  ElMessage.success(t('knowledgeBases.configuration.update'))
}
// 打开权限编辑下拉
const changeCopyStatus = (index: number, role: string) => {
  if (role === 'owner') return
  copyStatusArr.value.forEach((item, indexArr: number) => {
    if (index === indexArr) {
      item.status = !item.status
    } else {
      item.status = false
    }
  })
}
// 打开权限编辑下拉
const changeStatus = (index: number, role: string) => {
  if (role === 'owner') return
  statusArr.value.forEach((item, indexArr: number) => {
    if (index === indexArr) {
      item.status = !item.status
    } else {
      item.status = false
    }
  })
}
interface ReqData {
  role?: string
  user_id: string
  kb_id: string
}
// 编辑成员权限
const changePermission = async (val: string, role: member) => {
  if (val === role.role) return
  save.value = true
  const reqData:ReqData = {
    kb_id: route.query.id as string,
    user_id: role.user_id
  }
  if (val === 'delete') {
    const { data } = await post('/v1/kb/delete-kb-user', reqData)
    if (data.code === 0 && data.message === 'success') {
      ElMessage.success(t('knowledgeBases.configuration.deleteSuccess'))
    } else {
      ElMessage.error(t('knowledgeBases.configuration.deleteFail'))
    }
  } else {
    reqData.role = val
    const { data } = await post('/v1/kb/update-kb-user', reqData)
    if (data.code === 0 && data.message === 'success') {
      ElMessage.success(t('knowledgeBases.configuration.update'))
    }
  }
  getRole('change')
  checkPermission()
}
const remoteMethod = (query: string) => {
  if (query) {
    setTimeout(async () => {
      const { data } = await get(`/v1/kb/get-kb-user?query=${query}&kb_id=${route.query.id}`)
      userList.value = data.data
    })
  } else {
    userList.value = []
  }
}
const handleFilterKb = async () => {
  const { data } = await get('/v1/kb/get-mykbs')
  kbrList.value = data.data.filter((item: any) => item.kb_id !== route.query.id)
}
const handleClick = () => {
  statusArr.value.forEach(item => {
    item.status = false
  })
  copyStatusArr.value.forEach(item => {
    item.status = false
  })
}
let loading = true
onMounted(() => {
  getRole()
  getKbDetail()
  loading = false
  addEventListener('click', handleClick)
})
const getRole = async (val?: string) => {
  const { data } = await get(`/v1/kb/get-mykb_role?kb_id=${route.query.id}`)
  if (data.code === 0 && data.message === 'success') {
    role.value = data.data.role
  }
  if ((val && role.value === 'viewer') || data.code === 102) {
    location.href = '/knowledge-base'
  }
}
onBeforeUnmount(() => {
  removeEventListener('click', handleClick)
})
const checkPermission = async () => {
  if (role.value === 'viewer') return
  dialogVisible.value = true
  const { data } = await get(`/v1/kb/get-kb-users?kb_id=${route.query.id}`)
  kbMemberList.value = data.data
  kbMemberList.value.forEach(()=> {
    statusArr.value.push({ status: false })
  })
}
// 修改知识库信息
const updateKb = async () => {
  if (!disable.value) return
  ruleFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      interface ParserConfig {
        layout_recognize?: string
        chunk_token_num?: number
        delimiter?: string
        auto_keywords?: number
        auto_questions?: number
        html4excel?: boolean
        graphrag?: {
          use_graphrag: boolean
        }
      }
    
      // 公共默认配置
      const COMMON_CONFIG: ParserConfig = {
        layout_recognize: 'DeepDOC',
        auto_keywords: 0,
        auto_questions: 0
      }
    
      // 特殊配置差异部分（只写不同的部分）
      const parserConfigMap: Record<string, ParserConfig | null> = {
        naive: {
          chunk_token_num: 512,
          delimiter: '\n',
          html4excel: false
        },
        qa: {}, // 空对象表示不传 config
        resume: {},
        table: {},
        tag: {},
        manual: {},
        paper: {},
        laws: {},
        presentation: {},
        one: {},
        book: {
          graphrag: { use_graphrag: false }
        }
      }
    
      const chunkMethod = ruleForm.value.chunkMethod
      const specificConfig = parserConfigMap[chunkMethod] || {}
    
      // 合并公共配置和特定配置（为空则不传）
      const parser_config = Object.keys(specificConfig).length !== 0
        ? { ...COMMON_CONFIG, ...specificConfig }
        : {}
    
      const reqData = {
        kb_id: route.query.id,
        name: ruleForm.value.name,
        avatar: ruleForm.value.avatar,
        description: ruleForm.value.description,
        permission: 'me',
        embd_id: 'nomic-embed-text:latest@Ollama',
        pagerank: 0,
        parser_id: chunkMethod,
        parser_config
      }
      const { data } = await post('/v1/kb/update', reqData)
      if (data.code === 0 && data.message === 'success') {
        location.href = '/knowledge-base/dataset?id=' + route.query.id
        ElMessage.success(t('knowledgeBases.configuration.update'))
      }
    }
  })
}
const handleChange = async (e: any) => {
  const files = e.target.files
  if (!files) return
  disable.value = true
  ruleForm.value.avatar = await getBase64FromUploadFileList(files)
}
const getBase64FromUploadFileList = async (fileList: UploadFile[]) => {
  if (fileList.length > 0) {
    const file = fileList[0]
    const base64 = await transformFile2Base64(file)
    return base64
  }
  return ''
}
const transformFile2Base64 = (val: any): Promise<any> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(val)
    reader.onload = (): void => {
      // Create image object
      const img = new Image()
      img.src = reader.result as string
      img.onload = () => {
        // Create canvas
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        // Calculate compressed dimensions, set max width/height to 800px
        let width = img.width
        let height = img.height
        const maxSize = 100
        if (width > height && width > maxSize) {
          height = (height * maxSize) / width
          width = maxSize
        } else if (height > maxSize) {
          width = (width * maxSize) / height
          height = maxSize
        }
        // Set canvas dimensions
        canvas.width = width
        canvas.height = height
        // Draw image
        ctx?.drawImage(img, 0, 0, width, height)
        // Convert to base64, maintain original format and transparency
        const compressedBase64 = canvas.toDataURL('image/png')
        resolve(compressedBase64);
      }
      img.onerror = reject
    }
    reader.onerror = reject
  })
}
const kbDetail = ref()
const getKbDetail = async () => {
  const { data } = await get(`/v1/kb/detail?kb_id=${route.query.id}`)
  if (data.code === 0 && data.message === 'success') {
    ruleForm.value.name = data.data.name
    ruleForm.value.avatar = data.data.avatar
    ruleForm.value.chunkMethod = data.data.parser_id
    ruleForm.value.description = data.data.description
    kbDetail.value = data.data
  }
}
</script>

<style lang="scss" scoped>
.scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
    display: unset;
  }
  &::-webkit-scrollbar-track {
    display: unset;
    background-color: #f1f1f1;
  }
  &::-webkit-scrollbar-thumb {
    display: unset;
    border-radius: 4px;
    background-color: #c1c1c1;
  }
  &::-webkit-scrollbar-corner {
    display: unset;
    background-color: transparent;
  }
}
.rotate {
  margin-left: 4px;
  transform: rotateZ(90deg);
}
.shadows {
  box-shadow: 0px 4px 35px 0px #0029921A;
}
:deep(.el-dialog) {
  min-height: 524px;
  position: relative;
  .el-dialog__body {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    .center {
      transform: translate(-50%);
    }
  }
}
.cards {
  background-color: transparent;
  @media screen and (max-width: 1440px) {
    flex-direction: column;
    .card {
      & + .card {
        max-width: 100%;
        margin-left: 0;
        margin-top: 20px;
      }
    }
  }
}
.card {
  :deep(.el-form) {
    .el-form-item {
      display: flex;
      margin-top: 22px;
      justify-content: space-between;
      .el-form-item__label {
        width: 100%;
        margin-bottom: 0;
        max-width: 220px;
        align-items: center;
      }
      .el-form-item__content {
        width: calc(100% - 240px);
        .el-input .el-input__wrapper .el-input__inner {
          min-height: 36px;
        }
        .el-select .el-select__wrapper {
          min-height: 36px;
          &.is-focused {
            box-shadow: 0 0 0 1px #396FFA inset;
          }
        }
      }
      .el-form-item__label {
        span {
          font-family: 'Helvetica';
        }
        &::before {
          display: none;
        }
      }
    }
  }
}
</style>