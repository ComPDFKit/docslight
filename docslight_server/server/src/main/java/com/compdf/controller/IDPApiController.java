package com.compdf.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.compdf.annotation.PermissionVerify;
import com.compdf.client.LoginClient;
import com.compdf.config.base.R;
import com.compdf.entity.DocSlightSettings;
import com.compdf.entity.IdpTask;
import com.compdf.enums.PermissionEnum;
import com.compdf.enums.TaskTypeEnum;
import com.compdf.pojo.*;
import com.compdf.service.*;
import com.compdf.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/3/19 0019
 */
@RestController
@RequestMapping("/api/idp")
@RequiredArgsConstructor
@Slf4j
public class IDPApiController {

    private final OnlineAPIService onlineAPIService;
    private final IdpTaskService idpTaskService;
    private final IdpFileService idpFileService;
    private final LoginClient loginClient;
    private final TemplateService templateService;
    private final ComIDPService comIDPService;
    private final DocSlightSettingsService settingsService;

    @PostMapping("/data-extract")
    public R<DataExtractDTO> dataExtract(@RequestParam("file") MultipartFile file,
                                         @ModelAttribute FileParameterDTO fileParameter,
                                         HttpServletRequest request) {
        return R.ok(onlineAPIService.onlineAPIDataExtract(file, fileParameter, request));
    }

    @PostMapping("/data-extract-demo")
    public R<DataExtractDTO> dataExtractDemo(@RequestParam("file") MultipartFile file,
                                             @ModelAttribute FileParameterDTO fileParameter,
                                             HttpServletRequest request) {
        return R.ok(onlineAPIService.onlineAPIDataExtract(file, fileParameter, request));
    }

    @PostMapping("/data-extract-api")
    public R<DataExtractDTO> dataExtractApi(@RequestParam("file") MultipartFile file,
                                            @ModelAttribute FileParameterDTO fileParameter,
                                            HttpServletRequest request) {
        return R.ok(onlineAPIService.onlineAPIDataExtract(file, fileParameter, request));
    }

    @PostMapping("api-file-resolve")
    public R<FileResultDTO> apiFileResolve(@RequestParam("file") MultipartFile file,
                                           @ModelAttribute FileParameterDTO fileParameter,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        return R.ok(onlineAPIService.apiFileResolve(file, fileParameter, request, response));
    }

    @PostMapping("api-file-resolve-api")
    public R<FileResultDTO> apiFileResolveApi(@RequestParam("file") MultipartFile file,
                                              @ModelAttribute FileParameterDTO fileParameter,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        return R.ok(onlineAPIService.apiFileResolveApi(file, fileParameter, request, response));
    }

    @PostMapping("/file-resolve-edit")
    public R<FileResultDTO> fileResolveEdit(@RequestBody JsonEditDTO file,
                                            HttpServletRequest request) {

        return R.ok(onlineAPIService.apiFileEditResolve(file, request));
//        return R.ok(FileResultDTO.builder()
//                .downFileUrl("http://192.168.10.147:7000/api/idp/get-file?path=/comidp/server/out_tmp/1754637596872872/temp-2203457134891355192-207551752.zip")
//                .fileId("98daa66fac87553f9f7152aedb387d3b").build());
    }

    @GetMapping("get-asset")
    public R<AssetDTO> getAsset(HttpServletRequest request) {
        return R.ok(onlineAPIService.getAsset(request));
    }

    @PostMapping("/async/data-extract")
    public R<AsyncFileDTO> asyncIntelligentDocumentExtraction(@RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "keys", defaultValue = "") List<String> keys,
                                                              @RequestParam(value = "tableHandles", defaultValue = "") List<String> tableHandles,
                                                              @RequestParam(value = "pages", defaultValue = "") List<Integer> pages) {
        String fileId = onlineAPIService.asyncFileCreateDataExtract(file, keys, tableHandles, pages);
        return R.ok(new AsyncFileDTO(fileId));
    }


    @PostMapping("/pdf2markdown")
    public R<Result> pdf2markdown(@RequestParam MultipartFile file,
                                  @RequestParam(name = "isBase64", defaultValue = "true") Boolean isBase64,
                                  HttpServletRequest request) {
        return R.ok(onlineAPIService.pdf2markdown(file, isBase64, request));
    }

    @GetMapping("get-file")
    public void getFile(@RequestParam String id,
                        @RequestParam Integer flag,
                        HttpServletResponse response) {
        comIDPService.getFileById(id, flag, response);
    }


    @GetMapping("/create-task")
    @Deprecated
    public R<String> createTask(@RequestParam String taskType) {
        String userId = loginClient.getUserId();
        return R.ok(idpTaskService.createTask(taskType, userId).getId());
    }

    @PostMapping("/file-upload")
    public R<String> fileUpload(@ModelAttribute FileUploadPojo fileUploadPojo) {
        String userId = loginClient.getUserId();
        return R.ok(idpFileService.fileUpload(fileUploadPojo, userId));
    }

    @PostMapping("/task-start")
    public R<String> taskStart(@RequestBody FileParameterDTO fileParameter) {
        return R.ok(idpTaskService.taskStart(fileParameter.getTaskId(), fileParameter));
    }

    @PostMapping("/files-start")
    public R<String> filesStart(@RequestParam List<String> idpFileIds,
                                @RequestParam(required = false, defaultValue = "") String parameter,
                                @RequestParam TaskTypeEnum type) {
        idpFileService.startFile(idpFileIds, parameter, type);
        return R.ok();
    }

    @GetMapping("/get-task-list")
    public R<IPage<IdpTask>> getTaskList(@RequestParam(defaultValue = "1", required = false) int pageNum,
                                         @RequestParam(defaultValue = "10", required = false) int pageSize) {
        String userId = loginClient.getUserId();
        return R.ok(idpTaskService.getTaskList(userId, pageNum, pageSize));
    }

    @GetMapping("/get-task-file-list")
    public R<List<IdpFileInfoDTO>> getTaskFileList(@RequestParam String taskId, HttpServletRequest request) {
        return R.ok(idpFileService.getTaskFileList(taskId, request));
    }

    @GetMapping("/get-file-info")
    public R<IdpFileInfoDTO> getFileInfo(@RequestParam String fileId, HttpServletRequest request) {
        return R.ok(idpFileService.getFileInfo(fileId, request));
    }

    @GetMapping("/get-file-schedule")
    public R<FileScheduleDTO> getFileSchedule(@RequestParam String fileId) {
        return R.ok(idpFileService.getFileSchedule(fileId));
    }

    @GetMapping("/file-pause")
    public R<Void> filePause(@RequestParam List<String> fileIds) {
        idpFileService.filePause(fileIds);
        return R.ok();
    }

    @GetMapping("/file-delete")
    public R<Void> fileDelete(@RequestParam List<String> fileIds) {
        idpFileService.fileDelete(fileIds);
        return R.ok();
    }

    @GetMapping("/down-all-files")
    public void downAllFiles(@RequestParam String taskId, HttpServletResponse response) {
        idpFileService.downAllFiles(taskId, response);
    }


    @PostMapping("/add-template")
    public R<String> addTemplate(@RequestBody ExtractTemplateV2DTO extractTemplateDTO) {
        return R.ok(templateService.addTemplate(extractTemplateDTO, loginClient.getUserId(), extractTemplateDTO.getName()));
    }

    @PostMapping("/add-template-file")
    public R<Void> addTemplateFile(@RequestParam MultipartFile file, @RequestParam String templateId, @RequestParam(defaultValue = "1") Integer page) {
        templateService.addTemplateFile(file, templateId, page);
        return R.ok();
    }

    @PostMapping("/update-template")
    public R<Void> updateTemplate(@RequestBody ExtractTemplateV2DTO extractTemplateDTO) {
        templateService.updateTemplate(extractTemplateDTO, extractTemplateDTO.getId(), extractTemplateDTO.getName());
        return R.ok();
    }

    @PostMapping("/delete-template")
    public R<Void> deleteTemplate(@RequestBody ExtractTemplateV2DTO template) {
        templateService.deleteTemplate(template.getId());
        return R.ok();
    }

    @PostMapping("/enable-template")
    public R<Void> enableTemplate(@RequestParam String templateId) {
        templateService.enableTemplate(templateId);
        return R.ok();
    }

    @PostMapping("/disable-template")
    public R<Void> disableTemplate(@RequestParam String templateId) {
        templateService.disableTemplate(templateId);
        return R.ok();
    }

    @PostMapping("/create-template-group")
    public R<String> createTemplateGroup(@RequestParam String groupName) {
        return R.ok(templateService.createTemplateGroup(groupName));
    }

    @PostMapping("/create-group-template")
    public R<Void> insertGroupTemplate(@RequestParam String groupId,
                                         @RequestParam List<String> templateIds) {
        templateService.insertGroupTemplate(groupId, templateIds);
        return R.ok();
    }

    @GetMapping("/get-group-template")
    public R<List<GroupPojo>> getGroupInfo() {
        return R.ok(templateService.getGroupInfoByUserId());
    }

    @PostMapping("/delete-group-template")
    public R<Void> deleteGroupTemplate(@RequestParam String groupTemplateId) {
        templateService.deleteGroupTemplate(groupTemplateId);
        return R.ok();
    }


    @GetMapping("/get-template-list")
    public R<List<Object>> getTemplateList(String name) {
        return R.ok(templateService.getTemplateList(loginClient.getUserId(), name));
    }

    @GetMapping("/get-template-by-id")
    public R<Object> getTemplateById(String templateId) {
        return R.ok(templateService.getTemplateById(templateId));
    }

    @GetMapping("/get-default-template")
    public R<List<ExtractTemplateV2DTO>> getDefaultTemplate() {
        return R.ok(templateService.getDefaultTemplate());
    }

    @PostMapping("/pin-template")
    public R<Void> pinTemplate(@RequestParam String groupTemplateId) {
        templateService.pinTemplate(groupTemplateId, loginClient.getUserId());
        return R.ok();
    }

    @PostMapping("/unpin-template")
    public R<Void> unpinTemplate(@RequestParam String groupTemplateId) {
        templateService.unpinTemplate(groupTemplateId, loginClient.getUserId());
        return R.ok();
    }

    @GetMapping("/is-first-extract")
    public R<Boolean> isFirstExtract() {
        return R.ok(loginClient.isFirstExtract());
    }

    @GetMapping("/getFileList")
    public R<IPage<IdpFileInfoDTO>> getFileList(FileListQueryPojo queryPojo, HttpServletRequest request) {
        String userId = loginClient.getUserId();
        return R.ok(idpFileService.getFileList(queryPojo, userId, request));
    }

    @GetMapping("/getFileListByName")
    public R<List<IdpFileInfoDTO>> getFileListByName(FileListQueryPojo queryPojo, HttpServletRequest request) {
        return R.ok(idpFileService.getFileListByName(queryPojo, request));
    }

    @GetMapping("/get-file-by-id")
    public R<IdpFileInfoDTO> getFileById(@RequestParam String fileId,
                                         HttpServletRequest request) {
        return R.ok(idpFileService.getFileInfoById(fileId, request));
    }

    @GetMapping("/get-file-by-ids")
    public R<List<IdpFileInfoDTO>> getFileByIds(@RequestParam List<String> fileIds,
                                                HttpServletRequest request) {
        return R.ok(idpFileService.getFileInfoByIds(fileIds, request));
    }

    @PostMapping("/split-export")
    @PermissionVerify(PermissionEnum.SPLIT)
    public void splitExport(@RequestBody FileExportDTO fileExportDTO, HttpServletResponse response) {
        comIDPService.splitExport(fileExportDTO.getFileIds(), response);
    }

    @PostMapping("/layout-export")
    @PermissionVerify(PermissionEnum.PARSE)
    public void layoutExport(@RequestBody FileExportDTO fileExportDTO,
                             HttpServletResponse response) {
        comIDPService.layoutExport(fileExportDTO, response);
    }

    @PostMapping("/extract-export")
    @PermissionVerify(PermissionEnum.EXTRACT)
    public void extractExport(@RequestBody FileExportDTO fileExportDTO,
                              HttpServletResponse response) {
        comIDPService.extractExport(fileExportDTO, response);
    }

    @PostMapping("/confirm-file-result")
    public R<Void> confirmFileResult(@RequestParam String fileId,
                                     @RequestParam(defaultValue = "", required = false) String newResult) {
        idpFileService.confirmFileResult(fileId, newResult);
        return R.ok();
    }

    @PostMapping("/cancel-confirm-file-result")
    public R<Void> cancelConfirmFileResult(@RequestParam String fileId) {
        idpFileService.cancelConfirmFileResult(fileId);
        return R.ok();
    }

    @PostMapping("file-manual-group")
    public R<Void> fileManualGroup(@RequestBody FileManualGroupDTO fileManualGroupDTO) {
        comIDPService.fileManualGroup(fileManualGroupDTO);
        return R.ok();
    }

    @PostMapping("/test-extract")
    public R<String> testExtract(@RequestParam("file") MultipartFile file,
                                 @RequestParam String extractTemplateDTO,
                                 HttpServletRequest request) {
        return R.ok(comIDPService.testExtract(file,extractTemplateDTO, request));
    }


    @PostMapping("parse")
    public void parse(@RequestParam("file") MultipartFile file,
                      HttpServletRequest request,
                      HttpServletResponse response) {
        log.info("parse file: {} start.", file.getOriginalFilename());
        idpFileService.parse(file, request, response);
    }

    @PostMapping("parseOnlineAPIUrlMode")
    public R<IdpFileInfoDTO> parseOnlineAPIUrlMode(@RequestParam("file") MultipartFile file,
                      HttpServletRequest request,
                      HttpServletResponse response) {
        return R.ok(idpFileService.parseOnlineAPIUrlMode(file, request, response));
    }

    @PostMapping("extractOnlineAPIUrlMode")
    public R<IdpFileInfoDTO> extractOnlineAPIUrlMode(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "extractTemplate", required = false, defaultValue = "") String extractTemplate,
                                                     @RequestParam(value = "pages", required = false) List<Integer> pages, HttpServletRequest request) {
        return R.ok(comIDPService.extractOnlineAPIUrlmode(file, JsonUtils.jsonStringToBean(extractTemplate, ExtractTemplateDTO.class), pages, request));
    }

    @PostMapping("convertToPdf")
    public void convertToPdf(@RequestParam("file") MultipartFile file,
                                  HttpServletRequest request,
                             HttpServletResponse response) {
        idpFileService.convertToPdf(file, request, response);
    }

    @PostMapping("parse-api")
    public void parseAPI(@RequestParam("file") MultipartFile file,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        idpFileService.parseAPI(file, request, response);
    }

    @PostMapping("extract-api")
    public void extractAPI(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "extractTemplate", required = false, defaultValue = "") String extractTemplate,
                                                     @RequestParam(value = "pages", required = false) List<Integer> pages,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        idpFileService.extractAPI(file, JsonUtils.jsonStringToBean(extractTemplate, ExtractTemplateDTO.class), pages, request, response);
    }



    @PostMapping("update-settings")
    public R<DocSlightSettings> updateSettings(@RequestBody DocSlightSettings docSlightSettings) {
        return R.ok(settingsService.updateSettings(docSlightSettings));
    }

    @GetMapping("get-settings")
    public R<DocSlightSettings> getSettings() {
        return R.ok(settingsService.getSettings());
    }

}
