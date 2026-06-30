package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.client.ActuatorClient;
import com.compdf.client.LLMClient;
import com.compdf.client.LoginClient;
import com.compdf.client.RustFsClient;
import com.compdf.constant.RabbitMqConstant;
import com.compdf.constant.RedisConstant;
import com.compdf.entity.IdpFile;
import com.compdf.entity.IdpTask;
import com.compdf.entity.Log;
import com.compdf.entity.Template;
import com.compdf.entity.*;
import com.compdf.enums.*;
import com.compdf.exception.ComPDFKitException;
import com.compdf.license.enums.ConversionModule;
import com.compdf.license.utils.LicenseUtils;
import com.compdf.mapper.IdpFileMapper;
import com.compdf.pojo.*;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.properties.ResonacProperties;
import com.compdf.service.*;
import com.compdf.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdpFileServiceImpl extends ServiceImpl<IdpFileMapper, IdpFile> implements IdpFileService {
    private final RustFsClient rustFsClient;
    private final IdpTaskService taskService;
    private final ComPDFKitProperties properties;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final LoginClient loginClient;
    private final ActuatorClient actuatorClient;
    private final TemplateService templateService;
    private final LLMClient lLMClient;
    private final ResonacProperties resonacProperties;
    private final LogService logService;
    private final UserService userService;
    private final AssetService assetService;
    private final LLMClient llmClient;
    private final DocSlightSettingsService settingsService;
    @Value("${compdf.file.max-size}")
    private Long maxFileSize;

    @Transactional
    @Override
    public IdpFile selectByTaskId(String taskId, int retryCount) {
        while (retryCount < 10) {
            IdpFile idpFile = this.baseMapper.selectOne(new LambdaQueryWrapper<IdpFile>().eq(IdpFile::getTaskId, taskId).in(IdpFile::getStatus, Arrays.asList(FileStatusEnum.CREATED.getValue(), FileStatusEnum.PENDING_EXTRACTION.getValue())).last("LIMIT 1 FOR UPDATE"));
            if (Objects.isNull(idpFile)) {
                // 当前任务已全部处理完成，修改任务状态为已完成
                taskService.updateStatusById(taskId, TaskStatusEnum.SUCCESS);
                return null;
            }
//        idpFile.setServiceId(serviceId);
            idpFile.setStatus(FileStatusEnum.PROCESSING.getValue());
            // 如果没更新成功，重新执行
            if (!this.updateById(idpFile)) {
                retryCount++;
            } else return idpFile;
        }
        return null;
    }

    @Override
    public List<IdpFile> selectByTaskId(String taskId) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<IdpFile>().eq(IdpFile::getTaskId, taskId).ne(IdpFile::getStatus, FileStatusEnum.DELETE.getValue()).orderByAsc(IdpFile::getBulkOrder));
    }

    @Override
    public IdpFile selectById(String fileId) {
        return this.baseMapper.selectById(fileId);
    }

    @Override
    public Long selectCountByTaskIdAndStatus(String taskId, FileStatusEnum statusEnum, Integer minuteTime) {
        return this.baseMapper.selectCount(new LambdaQueryWrapper<IdpFile>().eq(IdpFile::getTaskId, taskId).eq(IdpFile::getStatus, statusEnum.getValue()).between(IdpFile::getUpdateDate, LocalDateTime.now().minusMinutes(minuteTime), LocalDateTime.now()));

    }
    /**
     *     - Office: .docx, .xlsx, .pptx, .doc, .xls, .ppt
     *     - Images: .png, .jpg, .jpeg, .gif, .bmp, .tiff, .tif, .webp
     *     - Text/Markup: .csv, .txt, .rtf, .html, .htm, .mhtml, .mht
     */
    static List<String> exctractFileSuffixList = Arrays.asList(".pdf", ".docx", ".xlsx", ".pptx", ".doc", ".xls", ".ppt", ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".tiff", ".tif", ".webp", ".csv", ".txt", ".rtf", ".html", ".htm", ".mhtml", ".mht");
    static List<String> splitFileSuffixList = Collections.singletonList(".pdf");
    static List<String> layoutFileSuffixList = Arrays.asList(".pdf", ".png", ".jpg", ".doc",".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".html", ".mhtml", ".csv", ".txt", ".ofd", ".rtf");

    @Override
    public String fileUpload(FileUploadPojo fileUploadPojo, String userId) {

        DocSlightSettings settings = settingsService.getSettings();
        if (settings == null) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_DOCSLIGHT_SETTINGS_NOT_FOUND);
        }else if (Objects.equals(settings.getModel(), "Cloud") && StringUtils.isEmpty(settings.getApikey())){
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_DOCSLIGHT_SETTINGS_NOT_FOUND);
        }

        List<File> fileList;
        if (Objects.equals(fileUploadPojo.getSource(), "team_space")) {
            fileList = actuatorClient.teamDownFile(JsonUtils.getJsonString(fileUploadPojo.getFileInfo()));
        } else if (Objects.equals(fileUploadPojo.getSource(), "dms")) {
            fileList = actuatorClient.dmsDownFile(JsonUtils.getJsonString(fileUploadPojo.getFileInfo()));
        } else {
            MultipartFile file = fileUploadPojo.getFile();
            File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), UUID.randomUUID() + "/" + file.getOriginalFilename(), null);
            fileList = Collections.singletonList(localFile);
        }
        for (File localFile : fileList) {
            // 校验文件大小是否小于100MB
            if (localFile.length() > maxFileSize) {
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_UPLOAD);
            }
            String fileExtension = FileUtils.getFileExtension(localFile.getName()).toLowerCase();
            String bucket;
            switch (fileUploadPojo.getTaskType()) {
                case EXTRACTION:
                    // 校验格式是否符合
                    bucket = fileUpExtractHandle(fileExtension);
                    break;
                case LAYOUT:
                    bucket = fileUpLayoutHandle(fileExtension);
                    break;
                case SPLIT:
                    bucket = fileUpSplitHandle(fileExtension);
                    break;
                default:
                    throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_UPLOAD);
            }

            String rustFsId = rustFsClient.uploadFile(localFile, bucket);
            if (fileUploadPojo.getTaskType().equals(TaskTypeEnum.EXTRACTION)) {
                if (Objects.equals(loginClient.getLeaderId(), resonacProperties.getLeaderId())){
                    extractFileUpLoadHandleResonac(localFile,rustFsId, Objects.requireNonNull(localFile.getName()), bucket, fileUploadPojo);
                }else {
                    extractFileUpLoadHandle(localFile,rustFsId, Objects.requireNonNull(localFile.getName()), bucket, fileUploadPojo);
                }
                FileUtils.deleteFile(localFile.toPath());
                return null;
            }
            if (fileUploadPojo.getTaskType().equals(TaskTypeEnum.LAYOUT) && !Objects.equals(fileExtension, ".pdf")) {
                // 转换成PDF后存储rustFs
                File pdfFile = actuatorClient.convertToPDF(localFile);
                rustFsId = rustFsClient.uploadFile(pdfFile, bucket);
                FileUtils.deleteFile(pdfFile.toPath());
            }

            IdpFile idpFile = new IdpFile();
            idpFile.setFileName(localFile.getName());
            idpFile.setFilePath(rustFsId);
            idpFile.setUserId(userId);
            idpFile.setStatus(FileStatusEnum.CREATED.getValue());
            idpFile.setType(fileUploadPojo.getTaskType().name());
            idpFile.setTaskType(fileUploadPojo.getTaskType().name());
            idpFile.setPageCount(FileUtils.getPageCount(localFile.getPath()));
            idpFile.setBulkOrder(fileUploadPojo.getOrder());
            idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
            idpFile.setSource(fileUploadPojo.getSource());
            this.save(idpFile);
            FileUtils.deleteFile(localFile.toPath());
            // 上传文件自动解析
            if (fileUploadPojo.getTaskType().equals(TaskTypeEnum.LAYOUT)) {
                redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + idpFile.getId(), "false," + idpFile.getPageCount() + ",0", Duration.ofDays(1));
                rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_RESOLVE_FILE_HANDLE_ROUTING_KEY, idpFile.getId());
                Log log = new Log();
                log.setUserId(loginClient.getUserId());
                log.setActionType(LogTypeEnum.LAYOUT.getDescription());
                log.setLeaderId(loginClient.getLeaderId());
                log.setRelatedContent(idpFile.getFileName());
                log.setCreateTime(new Date().getTime());
                log.setUpdateTime(new Date().getTime());
                log.setActionDetail("启动 [智能文档解析] 任务，处理文件 ["+idpFile.getFileName()+"]");
                logService.insertLog(log);
            }
        }
        return null;
    }

    private void extractFileUpLoadHandle(File localFile, String rustFsId, String originalFilename, String bucket, FileUploadPojo fileUploadPojo) {
        // 如果文件是PDF，调用拆分功能，一页一页
        List<GroupTemplatePojo> groupTemplatePojoList = templateService.getGroupTemplatesByGroupId(fileUploadPojo.getGroupId());
        List<String> templateIds = groupTemplatePojoList.stream().map(GroupTemplatePojo::getTemplateId).collect(Collectors.toList());
        List<Template> templates = templateService.getTemplateListByIds(templateIds);
        List<ExtractTemplateV2DTO> extractTemplateDTOS = templates.stream().map(template -> JsonUtils.jsonStringToBean(template.getContent(), ExtractTemplateV2DTO.class)).collect(Collectors.toList());
        String classJson = JsonUtils.getJsonString(extractTemplateDTOS);
        File zipFile;
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            File newPDF = actuatorClient.convertToPDF(localFile);
            rustFsId = rustFsClient.uploadFile(newPDF, bucket);
        }
        zipFile = actuatorClient.pdfFileSplit(rustFsId, ConvertParamDTO.builder().fileId(rustFsId).splitMode("ranges").splitArg("1").build(), null);
        try {
            String outFolder = zipFile.getPath().replace(".zip", "");
            ZipUtil.unZip(zipFile.getPath(), outFolder);
            File[] files = new File(outFolder).listFiles();
            if (files != null && files.length > 0) {
                File file = files[0];
                String classifyRustFsId = rustFsClient.uploadFile(file, bucket);
                // 文档分类
                String groupTemplateId;
                String templateName;
                if (StringUtils.hasText(fileUploadPojo.getGroupTemplateId())) {
                    groupTemplateId = fileUploadPojo.getGroupTemplateId();
                    templateName = templateService.getTemplateByGroupTemplateId(groupTemplateId).getName();
                } else {
                    templateName = lLMClient.extractClassify(classJson, classifyRustFsId, Objects.equals(resonacProperties.getLeaderId(), loginClient.getLeaderId()) ? "resonac" : "", true);
                    if (StringUtils.hasText(templateName)) {
                        GroupTemplatePojo groupTemplatePojo = groupTemplatePojoList.stream().filter(item -> Objects.equals(item.getTemplateName(), templateName)).limit(1).collect(Collectors.toList()).get(0);
                        groupTemplateId = groupTemplatePojo.getGroupTemplateId();
                    } else {
                        groupTemplateId = null;
                    }
                }
                IdpFile idpFile = new IdpFile();
                idpFile.setFileName(localFile.getName());
                idpFile.setFilePath(rustFsId);
                idpFile.setUserId(loginClient.getUserId());
                idpFile.setStatus(FileStatusEnum.CREATED.getValue());
                idpFile.setType(fileUploadPojo.getTaskType().name());
                idpFile.setTaskType(fileUploadPojo.getTaskType().name());
                idpFile.setPageCount(1);
                idpFile.setGroupTemplateId(groupTemplateId);
                idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
                idpFile.setBulkOrder(fileUploadPojo.getOrder());
                idpFile.setSource(fileUploadPojo.getSource());
                this.save(idpFile);
                FileUtils.deleteFile(file.toPath());
                if (StringUtils.hasText(groupTemplateId)) {
                    redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + idpFile.getId(), "false," + idpFile.getPageCount() + ",0", Duration.ofDays(1));
                    rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_EXTRACT_FILE_HANDLE_ROUTING_KEY, idpFile.getId());
                    Log log = new Log();
                    log.setUserId(loginClient.getUserId());
                    log.setActionType(LogTypeEnum.EXTRACT.getDescription());
                    log.setLeaderId(loginClient.getLeaderId());
                    log.setRelatedContent(idpFile.getFileName());
                    log.setCreateTime(new Date().getTime());
                    log.setUpdateTime(new Date().getTime());
                    log.setActionDetail("启动 [智能文档抽取] 任务，使用模板 ["+templateName+"] 处理文件 ["+idpFile.getFileName()+"]");
                    logService.insertLog(log);
                }

            }
            FileUtils.deleteFile(zipFile.toPath());
            FileUtils.deleteFolder(Paths.get(outFolder));
        } catch (IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        }
    }


    private void extractFileUpLoadHandleResonac(File localFile, String rustFsId, String originalFilename, String bucket, FileUploadPojo fileUploadPojo) {
        // 如果文件是PDF，调用拆分功能，一页一页
        List<GroupTemplatePojo> groupTemplatePojoList = templateService.getGroupTemplatesByGroupId(fileUploadPojo.getGroupId());
        List<String> templateIds = groupTemplatePojoList.stream().map(GroupTemplatePojo::getTemplateId).collect(Collectors.toList());
        List<Template> templates = templateService.getTemplateListByIds(templateIds);
        List<ExtractTemplateDTO> extractTemplateDTOS = templates.stream().map(template -> JsonUtils.jsonStringToBean(template.getContent(), ExtractTemplateDTO.class)).collect(Collectors.toList());
        String classJson = JsonUtils.getJsonString(extractTemplateDTOS);
        if (originalFilename.toLowerCase().endsWith(".pdf")) {
            File zipFile = actuatorClient.pdfFileSplit(rustFsId, ConvertParamDTO.builder().fileId(rustFsId).splitMode("every_n_pages").splitArg("1").build(), null);
            try {
                String outFolder = zipFile.getPath().replace(".zip", "");
                ZipUtil.unZip(zipFile.getPath(), outFolder);
                File[] files = new File(outFolder).listFiles();
                if (files != null) {
                    for (File file : files) {
                        rustFsId = rustFsClient.uploadFile(file, bucket);
                        // 文档分类
                        String groupTemplateId;
                        if (StringUtils.hasText(fileUploadPojo.getGroupTemplateId())) {
                            groupTemplateId = fileUploadPojo.getGroupTemplateId();
                        } else {
                            String templateName = lLMClient.extractClassify(classJson, rustFsId, Objects.equals(resonacProperties.getLeaderId(), loginClient.getLeaderId()) ? "resonac" : "", false);
                            if (StringUtils.hasText(templateName)) {
                                GroupTemplatePojo groupTemplatePojo = groupTemplatePojoList.stream().filter(item -> Objects.equals(item.getTemplateName(), templateName)).limit(1).collect(Collectors.toList()).get(0);
                                groupTemplateId = groupTemplatePojo.getGroupTemplateId();
                            } else {
                                groupTemplateId = null;
                            }
                        }
                        IdpFile idpFile = new IdpFile();
                        idpFile.setFileName(file.getName());
                        idpFile.setFilePath(rustFsId);
                        idpFile.setUserId(loginClient.getUserId());
                        idpFile.setStatus(StringUtils.hasText(groupTemplateId) ? FileStatusEnum.PENDING_EXTRACTION.getValue() : FileStatusEnum.CLASSIFICATION_FAILED.getValue());
                        idpFile.setType(fileUploadPojo.getTaskType().name());
                        idpFile.setTaskType(fileUploadPojo.getTaskType().name());
                        idpFile.setPageCount(1);
                        idpFile.setGroupTemplateId(groupTemplateId);
                        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
                        idpFile.setBulkOrder(fileUploadPojo.getOrder());
                        this.save(idpFile);
                        FileUtils.deleteFile(file.toPath());
                        if (StringUtils.hasText(groupTemplateId)) {
                            redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + idpFile.getId(), "false," + idpFile.getPageCount() + ",0", Duration.ofDays(1));
                            rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_EXTRACT_FILE_HANDLE_ROUTING_KEY, idpFile.getId());
                        }
                    }
                }
                FileUtils.deleteFile(zipFile.toPath());
                FileUtils.deleteFolder(Paths.get(outFolder));
            } catch (IOException e) {
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
            }
        } else {
            // 文档分类
            String groupTemplateId;
            if (StringUtils.hasText(fileUploadPojo.getGroupTemplateId())) {
                groupTemplateId = fileUploadPojo.getGroupTemplateId();
            } else {
                String templateName = lLMClient.extractClassify(classJson, rustFsId, Objects.equals(resonacProperties.getLeaderId(), loginClient.getLeaderId()) ? "resonac" : "", false);
                if (StringUtils.hasText(templateName)) {
                    GroupTemplatePojo groupTemplatePojo = groupTemplatePojoList.stream().filter(item -> Objects.equals(item.getTemplateName(), templateName)).limit(1).collect(Collectors.toList()).get(0);
                    groupTemplateId = groupTemplatePojo.getGroupTemplateId();
                } else {
                    groupTemplateId = null;
                }
            }
            IdpFile idpFile = new IdpFile();
            idpFile.setFileName(originalFilename);
            idpFile.setFilePath(rustFsId);
            idpFile.setUserId(loginClient.getUserId());
            idpFile.setStatus(StringUtils.hasText(groupTemplateId) ? FileStatusEnum.PENDING_EXTRACTION.getValue() : FileStatusEnum.CLASSIFICATION_FAILED.getValue());
            idpFile.setType(fileUploadPojo.getTaskType().name());
            idpFile.setTaskType(fileUploadPojo.getTaskType().name());
            idpFile.setPageCount(1);
            idpFile.setGroupTemplateId(groupTemplateId);
            idpFile.setBulkOrder(fileUploadPojo.getOrder());
            idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
            this.save(idpFile);
            if (StringUtils.hasText(groupTemplateId)) {
                redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + idpFile.getId(), "false," + idpFile.getPageCount() + ",0", Duration.ofDays(1));
                rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_EXTRACT_FILE_HANDLE_ROUTING_KEY, idpFile.getId());
            }
        }
    }

    private String fileUpLayoutHandle(String fileExtension) {
        String bucket;
        if (!layoutFileSuffixList.contains(fileExtension)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_CONVERT_FORMAT);
        }
        bucket = rustFsClient.BUCKET_LAYOUT;
        PermissionVerifyUtils.checkPermission(PermissionEnum.PARSE_UPLOAD, loginClient);
        return bucket;
    }

    private String fileUpSplitHandle(String fileExtension) {
        String bucket;
        if (!splitFileSuffixList.contains(fileExtension)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_CONVERT_FORMAT);
        }
        bucket = rustFsClient.BUCKET_SPLIT;
        PermissionVerifyUtils.checkPermission(PermissionEnum.SPLIT_UPLOAD, loginClient);
        return bucket;
    }

    @NotNull
    private String fileUpExtractHandle(String fileExtension) {
        String bucket;
        if (!exctractFileSuffixList.contains(fileExtension)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_CONVERT_FORMAT);
        }
        bucket = rustFsClient.BUCKET_EXTRACT;
        PermissionVerifyUtils.checkPermission(PermissionEnum.EXTRACT_UPLOAD, loginClient);
        return bucket;
    }

    @Override
    public void startFile(String taskId, FileParameterDTO fileParameter, TaskTypeEnum taskType) {
        IdpFile idpFile = new IdpFile();
        idpFile.setParameter(JsonUtils.getJsonString(fileParameter));
        idpFile.setStatus(taskType == TaskTypeEnum.EXTRACTION ? FileStatusEnum.PENDING_EXTRACTION.getValue() : FileStatusEnum.CREATED.getValue());
        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        this.baseMapper.update(idpFile, new LambdaQueryWrapper<IdpFile>().eq(IdpFile::getTaskId, taskId).ne(IdpFile::getStatus, FileStatusEnum.DELETE.getValue()));
        List<IdpFile> idpFileList = this.selectByTaskId(taskId);
        switch (taskType) {
            case EXTRACTION:
                idpFileList.forEach(item -> {
                    redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + item.getId(), "false," + item.getPageCount() + ",0", Duration.ofDays(1));
                    rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_EXTRACT_FILE_HANDLE_ROUTING_KEY, item.getId());
                });
                break;
            case LAYOUT:
                idpFileList.forEach(item -> {
                    redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + item.getId(), "false," + item.getPageCount() + ",0", Duration.ofDays(1));
                    rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_RESOLVE_FILE_HANDLE_ROUTING_KEY, item.getId());
                });
                break;
        }
    }

    @Override
    public void startFile(List<String> idpFileIds, String fileParameter, TaskTypeEnum taskType) {
        IdpFile idpFile = new IdpFile();
//        idpFile.setParameter(JsonUtils.getJsonString(fileParameter));
        idpFile.setStatus(taskType == TaskTypeEnum.EXTRACTION ? FileStatusEnum.PENDING_EXTRACTION.getValue() : FileStatusEnum.CREATED.getValue());
        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        if (StringUtils.hasText(fileParameter)) {
            idpFile.setParameter(fileParameter);
        }
        this.baseMapper.update(idpFile, new LambdaQueryWrapper<IdpFile>().in(IdpFile::getId, idpFileIds));
        List<IdpFile> idpFileList = this.baseMapper.selectList(new LambdaQueryWrapper<IdpFile>().in(IdpFile::getId, idpFileIds));
        switch (taskType) {
            case EXTRACTION:
                idpFileList.forEach(item -> {
                    redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + item.getId(), "false," + item.getPageCount() + ",0", Duration.ofDays(1));
                    rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_EXTRACT_FILE_HANDLE_ROUTING_KEY, item.getId());
                    Log log = new Log();
                    IdpFile idpFile1 = this.selectById(item.getId());
                    log.setUserId(loginClient.getUserId());
                    log.setActionType(LogTypeEnum.EXTRACT.getDescription());
                    log.setLeaderId(loginClient.getLeaderId());
                    log.setRelatedContent(idpFile1.getFileName());
                    log.setCreateTime(new Date().getTime());
                    log.setUpdateTime(new Date().getTime());
                    log.setActionDetail("启动 [智能文档抽取] 任务，使用模板 ["+templateService.getTemplateByGroupTemplateId(idpFile1.getGroupTemplateId()).getName()+"] 处理文件 ["+idpFile1.getFileName()+"]");
                    logService.insertLog(log);
                });
                break;
            case LAYOUT:
                idpFileList.forEach(item -> {
                    redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + item.getId(), "false," + item.getPageCount() + ",0", Duration.ofDays(1));
                    rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_RESOLVE_FILE_HANDLE_ROUTING_KEY, item.getId());
                    Log log = new Log();
                    IdpFile idpFile1 = this.selectById(item.getId());
                    log.setUserId(loginClient.getUserId());
                    log.setActionType(LogTypeEnum.LAYOUT.getDescription());
                    log.setLeaderId(loginClient.getLeaderId());
                    log.setRelatedContent(idpFile1.getFileName());
                    log.setCreateTime(new Date().getTime());
                    log.setUpdateTime(new Date().getTime());
                    log.setActionDetail("启动 [智能文档解析] 任务，处理文件 ["+idpFile1.getFileName()+"]");
                    logService.insertLog(log);
                });
                break;
            case SPLIT:
                idpFileList.forEach(item -> rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_SPLIT_FILE_HANDLE_ROUTING_KEY, item.getId()));
                break;
        }
    }

    @Override
    public IdpFileInfoDTO getFileInfo(String fileId, HttpServletRequest request) {
        IdpFile idpFile = this.selectById(fileId);
        if (Objects.isNull(idpFile)) {
            throw new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR);
        }
        FileParameterDTO fileParameter = JsonUtils.jsonStringToBean(idpFile.getParameter(), FileParameterDTO.class);
        String filePath = idpFile.getFilePath();
        String outFilePath = idpFile.getOutFilePath();
        switch (TaskTypeEnum.getEnumByType(idpFile.getType())) {
            case EXTRACTION:
                break;

            case LAYOUT:
                if (!StringUtils.isEmpty(outFilePath)
                        && fileParameter != null
                        && (Objects.equals(fileParameter.getResolveType(), "all") || Objects.equals(fileParameter.getResolveType(), "image"))) {
                    try {
                        Path outDirectory = Paths.get(outFilePath.replace(".zip", ""));
                        Path jsonAllResultPath = Paths.get(outDirectory + "/" + outDirectory.getFileName().toString() + "all_result" + ".json");
                        if (!jsonAllResultPath.toFile().exists()) {
                            ZipUtil.unZip(outFilePath, outDirectory.toString());
                            String outJson = outDirectory + "/" + outDirectory.getFileName().toString() + ".json";
                            try (Stream<Path> walk = Files.walk(outDirectory)) {
                                String outJsonStr = new String(Files.readAllBytes(Paths.get(outJson)), StandardCharsets.UTF_8);
                                List<Path> imagePath = walk.filter(path -> path.toString().contains("figures") && path.toString().endsWith(".png")).collect(Collectors.toList());
                                for (Path path : imagePath) {
                                    outJsonStr = outJsonStr.replace("figures/" + path.getFileName(), FileUtils.getFileDownUrl(path.toString(), request));
                                }
                                Files.write(jsonAllResultPath, outJsonStr.getBytes());
                            }
                        }
                        outFilePath = jsonAllResultPath.toString();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        }
        IdpFileInfoDTO idpFileInfoDTO = new IdpFileInfoDTO();
        idpFileInfoDTO.setFileId(idpFile.getId());
        idpFileInfoDTO.setFileDownUrl(FileUtils.getFileDownUrl(filePath, request));
        idpFileInfoDTO.setResultDownUrl(FileUtils.getFileDownUrl(outFilePath, request));
        idpFileInfoDTO.setStatus(idpFile.getStatus());
        idpFileInfoDTO.setFileName(idpFile.getFileName());
        idpFileInfoDTO.setPageCount(idpFile.getPageCount());
        idpFileInfoDTO.setUploadTime(idpFile.getCreateDate());
        idpFileInfoDTO.setReviewStatus(idpFile.getReviewStatus());
        idpFileInfoDTO.setFailureCode(idpFile.getFailureCode());
        idpFileInfoDTO.setFailureReason(idpFile.getFailureReason());
        idpFileInfoDTO.setFileSchedule(this.getFileSchedule(idpFile.getId()));
        idpFileInfoDTO.setGroupTemplateId(idpFile.getGroupTemplateId());
        return idpFileInfoDTO;
    }

    /**
     * - 每一页抽取完成时候，存入Redis中<fileID:是否暂停:总页数:当前处理页>
     * - 提供根据文件ID来获取Redis中《总页数:当前处理页》信息并返回
     *
     * @param fileId 文件ID
     * @return FileScheduleDTO
     */
    @Override
    public FileScheduleDTO getFileSchedule(String fileId) {
        // <fileID:是否暂停,总页数,当前处理页>
        try {
            String fileHandleInfo = redisTemplate.opsForValue().get(RedisConstant.IDP_FILE_HANDLER + fileId);
            if (StringUtils.isEmpty(fileHandleInfo)) {
                // TODO
                return null;
            }
            String[] split = fileHandleInfo.split(",");
            return FileScheduleDTO.builder().fileId(fileId).isPause(Boolean.parseBoolean(split[0])).totalPageCount(Integer.parseInt(split[1])).currentPageCount(Integer.parseInt(split[2])).build();
        } catch (NumberFormatException e) {
            return FileScheduleDTO.builder().fileId(fileId).isPause(false).totalPageCount(0).currentPageCount(0).build();
        }
    }

    /**
     * - 修改Redis中<fileID:是否暂停,总页数,当前处理页>中《是否暂停》信息为是，
     * - 每一页进行抽取之前获取上述Redis中信息的《是否暂停》信息，如果暂停则不进行后续处理，直接修改文件信息状态《已暂停》
     *
     * @param fileIds fileId
     */
    @Override
    public void filePause(List<String> fileIds) {
        fileIds.forEach(fileId -> {
            String fileHandlerInfo = redisTemplate.opsForValue().get(RedisConstant.IDP_FILE_HANDLER + fileId);
            if (StringUtils.isEmpty(fileHandlerInfo)) {
                return;
            }
            redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + fileId, "true" + fileHandlerInfo.substring(fileHandlerInfo.indexOf(",")), Duration.ofDays(1));
        });
        IdpFile idpFile = new IdpFile();
        idpFile.setStatus(FileStatusEnum.PAUSE.getValue());
        this.update(idpFile, new LambdaQueryWrapper<IdpFile>().in(IdpFile::getId, fileIds));
    }

    @Override
    @Transactional
    public void fileDelete(List<String> fileIds) {
        IdpFile idpFile = new IdpFile();
        idpFile.setStatus(FileStatusEnum.DELETE.getValue());
        this.update(idpFile, new LambdaQueryWrapper<IdpFile>().in(IdpFile::getId, fileIds));
    }

    @Override
    public List<IdpFileInfoDTO> getTaskFileList(String taskId, HttpServletRequest request) {
        List<IdpFile> idpFileList = this.selectByTaskId(taskId);
        return idpFileList.parallelStream().map(item -> {
            IdpFileInfoDTO idpFileInfoDTO = new IdpFileInfoDTO();
            idpFileInfoDTO.setFileId(item.getId());
            idpFileInfoDTO.setFileDownUrl(FileUtils.getFileDownUrl(item.getFilePath(), request));
            if (FileStatusEnum.isSuccessStatus(item.getStatus())) {
                idpFileInfoDTO.setResultDownUrl(FileUtils.getFileDownUrl(item.getOutFilePath(), request));
            }
            idpFileInfoDTO.setStatus(item.getStatus());
            idpFileInfoDTO.setFileName(item.getFileName());
            idpFileInfoDTO.setPageCount(item.getPageCount());
            idpFileInfoDTO.setUploadTime(item.getCreateDate());
            idpFileInfoDTO.setReviewStatus(item.getReviewStatus());
            idpFileInfoDTO.setFailureCode(item.getFailureCode());
            idpFileInfoDTO.setFailureReason(item.getFailureReason());
            idpFileInfoDTO.setFileSchedule(this.getFileSchedule(item.getId()));
            idpFileInfoDTO.setGroupTemplateId(item.getGroupTemplateId());
            return idpFileInfoDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public void downAllFiles(String taskId, HttpServletResponse response) {
        List<IdpFile> idpFileList = this.selectByTaskId(taskId);
        IdpTask idpTask = taskService.selectById(taskId);
        FileParameterDTO fileParameter = JsonUtils.jsonStringToBean(idpTask.getParams(), FileParameterDTO.class);
        TaskTypeEnum enumByType = TaskTypeEnum.getEnumByType(idpTask.getType());
        switch (enumByType) {
            case EXTRACTION:
                switch (fileParameter.getOutType()) {
                    case "txt":
                        idpFileList.forEach(idpFile -> {
                            if (FileStatusEnum.isSuccessStatus(idpFile.getStatus())) {
                                Path outFilePath = Paths.get(idpFile.getOutFilePath());
                                String txt = JsonExtractConvert.json2txt(outFilePath.toString(), outFilePath.getParent().toString() + "/" + FileUtils.getFileName(idpFile.getFileName()) + "_comidp_batch_extract" + ".txt");
                                idpFile.setOutFilePath(txt);
                            }
                        });
                        break;
                    case "csv":
                        idpFileList.forEach(idpFile -> {
                            if (FileStatusEnum.isSuccessStatus(idpFile.getStatus())) {
//                                String txt = JsonExtractConvert.json2csv(outFilePath.toString(), outFilePath.getParent().toString() + "/" + FileUtils.getFileName(idpFile.getFileName()) + "_comidp_batch_extract" + ".csv");
//                                idpFile.setOutFilePath(txt);
                            }
                        });
                        break;
                    case "excel":
                        idpFileList.forEach(idpFile -> {
                            if (FileStatusEnum.isSuccessStatus(idpFile.getStatus())) {
//                                String txt = JsonExtractConvert.json2excel(outFilePath.toString(), outFilePath.getParent().toString() + "/" + FileUtils.getFileName(idpFile.getFileName()) + "_comidp_batch_extract" + ".xlsx");
//                                idpFile.setOutFilePath(txt);
                            }
                        });
                        break;
                }
                break;
            case LAYOUT:
//                idpFileList.forEach(idpFile -> {
//                });
        }
        String outZip = properties.getTmpPath() + "/" + taskId;
        try {
            ZipUtil.zipFile(outZip, idpFileList.stream().filter(item -> FileStatusEnum.isSuccessStatus(item.getStatus())).map(item -> new File(item.getOutFilePath())).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Objects.equals(enumByType, TaskTypeEnum.LAYOUT)) {
            FileUtils.returnFileStream(outZip + ".zip", "comidp_batch_parse_" + LocalDate.now().toString().replace("-", "") + ".zip", response);
        } else {
            FileUtils.returnFileStream(outZip + ".zip", "comidp_batch_extract_" + LocalDate.now().toString().replace("-", "") + ".zip", response);
        }
    }

    @Override
    public IPage<IdpFileInfoDTO> getFileList(FileListQueryPojo queryPojo, String userId, HttpServletRequest request) {
        // 参数校验与默认值处理
        int page = (queryPojo.getPage() == null || queryPojo.getPage() < 1) ? 1 : queryPojo.getPage();
        int pageSize = (queryPojo.getPageSize() == null || queryPojo.getPageSize() < 1) ? 10 : queryPojo.getPageSize();

        // 构建分页对象
        Page<IdpFile> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<IdpFile> queryWrapper = new LambdaQueryWrapper<IdpFile>().eq(StringUtils.hasText(queryPojo.getTaskType()), IdpFile::getTaskType, queryPojo.getTaskType()).eq(IdpFile::getUserId, userId)
                .ne(IdpFile::getStatus, FileStatusEnum.DELETE.getValue()).orderByDesc(IdpFile::getCreateDate);
        if (StringUtils.hasText(queryPojo.getFileName())) {
            queryWrapper.like(IdpFile::getFileName, queryPojo.getFileName());
        }
        if (!CollectionUtils.isEmpty(queryPojo.getStatus())) {
            queryWrapper.in(IdpFile::getStatus, queryPojo.getStatus());
        }
        if (!CollectionUtils.isEmpty(queryPojo.getReviewStatus())) {
            queryWrapper.in(IdpFile::getReviewStatus, queryPojo.getReviewStatus());
        }
        if (!Objects.isNull(queryPojo.getStartTime())) {
            queryWrapper.ge(IdpFile::getCreateDate, queryPojo.getStartTime());
        }
        if (!Objects.isNull(queryPojo.getEndTime())) {
            queryWrapper.le(IdpFile::getCreateDate, queryPojo.getEndTime());
        }
        if (Objects.equals(queryPojo.getTaskType(), TaskTypeEnum.EXTRACTION.name())) {
            if (!StringUtils.hasText(queryPojo.getGroupTemplateId()) && !StringUtils.hasText(queryPojo.getGroupId())) {
                queryWrapper.isNull(IdpFile::getGroupTemplateId);
            } else if (!StringUtils.hasText(queryPojo.getGroupTemplateId()) && StringUtils.hasText(queryPojo.getGroupId())) {
                List<GroupTemplatePojo> groupTemplatesByGroupId = templateService.getGroupTemplatesByGroupId(queryPojo.getGroupId());
                List<String> groupTemplateIds = new ArrayList<>();
                for (GroupTemplatePojo groupTemplatePojo : groupTemplatesByGroupId) {
                    groupTemplateIds.add(groupTemplatePojo.getGroupTemplateId());
                }
                LambdaQueryWrapper<IdpFile> queryWrapperSon = queryWrapper.clone();
                queryWrapperSon.in(IdpFile::getGroupTemplateId, groupTemplateIds);
                queryWrapperSon.select(IdpFile::getId);
                queryWrapper.isNull(IdpFile::getGroupTemplateId);
                List<String> ids = this.baseMapper.selectList(queryWrapperSon).stream().map(IdpFile::getId).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)) {
                    queryWrapper.or().in(IdpFile::getId, ids);
                }
            } else if (StringUtils.hasText(queryPojo.getGroupTemplateId())) {
                queryWrapper.eq(IdpFile::getGroupTemplateId, queryPojo.getGroupTemplateId());
            }
        }

        // 执行分页查询
        IPage<IdpFile> idpFilePage = this.baseMapper.selectPage(pageParam, queryWrapper);

        // 转换为DTO分页对象
        Page<IdpFileInfoDTO> resultPage = new Page<>(idpFilePage.getCurrent(), idpFilePage.getSize(), idpFilePage.getTotal());

        if (CollectionUtils.isEmpty(idpFilePage.getRecords())) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        // 转换为DTO列表
        List<IdpFileInfoDTO> dtoList = idpFilePage.getRecords().stream()
                .map(item -> convertToIdpFileInfoDTO(item, request))
                .collect(Collectors.toList());
        fillTemplateNamesIfExtraction(queryPojo, dtoList);

        resultPage.setRecords(dtoList);
        return resultPage;
    }

    private void fillTemplateNamesIfExtraction(FileListQueryPojo queryPojo, List<IdpFileInfoDTO> dtoList) {
        if (!Objects.equals(queryPojo.getTaskType(), TaskTypeEnum.EXTRACTION.name()) || CollectionUtils.isEmpty(dtoList)) {
            return;
        }
        List<String> groupTemplateIds = dtoList.stream()
                .map(IdpFileInfoDTO::getGroupTemplateId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupTemplateIds)) {
            return;
        }
        Map<String, String> templateNameMap = templateService.getGroupTemplatesByGroupTemplateIds(groupTemplateIds)
                .stream()
                .filter(groupTemplate -> StringUtils.hasText(groupTemplate.getGroupTemplateId()))
                .filter(groupTemplate -> StringUtils.hasText(groupTemplate.getTemplateName()))
                .collect(Collectors.toMap(
                        GroupTemplatePojo::getGroupTemplateId,
                        GroupTemplatePojo::getTemplateName,
                        (first, second) -> first));
        dtoList.forEach(item -> {
            if (StringUtils.hasText(item.getGroupTemplateId())) {
                item.setTemplateName(templateNameMap.get(item.getGroupTemplateId()));
            }
        });
    }

    /**
     * 构建基础查询条件
     */
    private LambdaQueryWrapper<IdpFile> buildBaseQueryWrapper(FileListQueryPojo queryPojo, List<String> teamUserIds) {
        LambdaQueryWrapper<IdpFile> queryWrapper = new LambdaQueryWrapper<IdpFile>()
                .eq(StringUtils.hasText(queryPojo.getTaskType()), IdpFile::getTaskType, queryPojo.getTaskType())
                .in(IdpFile::getUserId, teamUserIds)
                .ne(IdpFile::getStatus, FileStatusEnum.DELETE.getValue())
                .like(StringUtils.hasText(queryPojo.getFileName()), IdpFile::getFileName, queryPojo.getFileName())
                .in(!CollectionUtils.isEmpty(queryPojo.getStatus()), IdpFile::getStatus, queryPojo.getStatus())
                .in(!CollectionUtils.isEmpty(queryPojo.getReviewStatus()), IdpFile::getReviewStatus, queryPojo.getReviewStatus())
                .ge(queryPojo.getStartTime() != null, IdpFile::getCreateDate, queryPojo.getStartTime())
                .le(queryPojo.getEndTime() != null, IdpFile::getCreateDate, queryPojo.getEndTime())
                .orderByDesc(IdpFile::getCreateDate);
        return queryWrapper;
    }

    /**
     * 构建EXTRACTION类型的查询条件
     */
    private void buildExtractionQueryCondition(FileListQueryPojo queryPojo, LambdaQueryWrapper<IdpFile> queryWrapper) {
        String groupTemplateId = queryPojo.getGroupTemplateId();
        String groupId = queryPojo.getGroupId();

        if (StringUtils.hasText(groupTemplateId)) {
            // 指定了具体的groupTemplateId
            queryWrapper.eq(IdpFile::getGroupTemplateId, groupTemplateId);
        } else if (StringUtils.hasText(groupId)) {
            // 指定了groupId，需要查询该group下所有模板的文件以及无模板的文件
            List<GroupTemplatePojo> groupTemplates = templateService.getGroupTemplatesByGroupId(groupId);
            List<String> groupTemplateIds = groupTemplates.stream()
                    .map(GroupTemplatePojo::getGroupTemplateId)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(groupTemplateIds)) {
                // 如果该group下没有模板，只查询无模板的文件
                queryWrapper.isNull(IdpFile::getGroupTemplateId);
            } else {
                // 查询无模板的文件或者属于该group下模板的文件
                queryWrapper.and(wrapper -> wrapper
                        .isNull(IdpFile::getGroupTemplateId)
                        .or()
                        .in(IdpFile::getGroupTemplateId, groupTemplateIds));
            }
        } else {
            // 未指定groupId和groupTemplateId，只查询无模板的文件
            queryWrapper.isNull(IdpFile::getGroupTemplateId);
        }
    }

    /**
     * 将IdpFile转换为IdpFileInfoDTO
     */
    private IdpFileInfoDTO convertToIdpFileInfoDTO(IdpFile item, HttpServletRequest request) {
        IdpFileInfoDTO idpFileInfoDTO = new IdpFileInfoDTO();
        idpFileInfoDTO.setFileId(item.getId());
        idpFileInfoDTO.setFileName(item.getFileName());
        idpFileInfoDTO.setStatus(item.getStatus());
        idpFileInfoDTO.setPageCount(item.getPageCount());
        idpFileInfoDTO.setUploadTime(item.getCreateDate());
        idpFileInfoDTO.setReviewStatus(item.getReviewStatus());
        idpFileInfoDTO.setFileDownUrl(FileUtils.getFileDownUrl(item.getId(), 0, request));
        if (FileStatusEnum.isSuccessStatus(item.getStatus())) {
            idpFileInfoDTO.setResultDownUrl(FileUtils.getFileDownUrl(item.getId(), 1, request));
        }
        idpFileInfoDTO.setGroupTemplateId(item.getGroupTemplateId());
        idpFileInfoDTO.setFailureCode(item.getFailureCode());
        idpFileInfoDTO.setFailureReason(item.getFailureReason());
        idpFileInfoDTO.setFileSchedule(this.getFileSchedule(item.getId()));
        return idpFileInfoDTO;
    }

    @Override
    public List<IdpFileInfoDTO> getFileListByName(FileListQueryPojo queryPojo, HttpServletRequest request) {
        List<String> teamUserIds = loginClient.getTeamUserIds();
        LambdaQueryWrapper<IdpFile> queryWrapper = new LambdaQueryWrapper<IdpFile>().eq(StringUtils.hasText(queryPojo.getTaskType()), IdpFile::getTaskType, queryPojo.getTaskType()).in(IdpFile::getUserId, teamUserIds).like(IdpFile::getFileName, queryPojo.getFileName()).ne(IdpFile::getStatus, FileStatusEnum.DELETE.getValue()).orderByDesc(IdpFile::getCreateDate);
        if (!CollectionUtils.isEmpty(queryPojo.getReviewStatus())) {
            queryWrapper.in(IdpFile::getReviewStatus, queryPojo.getReviewStatus());
        }
        List<IdpFile> idpFiles = this.baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(idpFiles)) {
            return Collections.emptyList();
        }
        return idpFiles.stream().map(item -> {
            IdpFileInfoDTO idpFileInfoDTO = new IdpFileInfoDTO();
            idpFileInfoDTO.setFileId(item.getId());
            idpFileInfoDTO.setFileName(item.getFileName());
            idpFileInfoDTO.setStatus(item.getStatus());
            idpFileInfoDTO.setPageCount(item.getPageCount());
            idpFileInfoDTO.setUploadTime(item.getCreateDate());
            idpFileInfoDTO.setReviewStatus(item.getReviewStatus());
            idpFileInfoDTO.setFileDownUrl(FileUtils.getFileDownUrl(item.getId(), 0, request));
            if (FileStatusEnum.isSuccessStatus(item.getStatus())) {
                idpFileInfoDTO.setResultDownUrl(FileUtils.getFileDownUrl(item.getId(), 1, request));
            }
            idpFileInfoDTO.setFailureCode(item.getFailureCode());
            idpFileInfoDTO.setFailureReason(item.getFailureReason());
            idpFileInfoDTO.setFileSchedule(this.getFileSchedule(item.getId()));
            return idpFileInfoDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public IdpFileInfoDTO getFileInfoById(String fileId, HttpServletRequest request) {
        IdpFile idpFile = this.selectById(fileId);
        if (Objects.isNull(idpFile)) {
            throw new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR);
        }
        IdpFileInfoDTO idpFileInfoDTO = new IdpFileInfoDTO();
        idpFileInfoDTO.setFileId(idpFile.getId());
        idpFileInfoDTO.setFileName(idpFile.getFileName());
        idpFileInfoDTO.setStatus(idpFile.getStatus());
        idpFileInfoDTO.setPageCount(idpFile.getPageCount());
        idpFileInfoDTO.setUploadTime(idpFile.getCreateDate());
        idpFileInfoDTO.setReviewStatus(idpFile.getReviewStatus());
        idpFileInfoDTO.setFileDownUrl(FileUtils.getFileDownUrl(idpFile.getId(), 0, request));
        if (FileStatusEnum.isSuccessStatus(idpFile.getStatus())) {
            idpFileInfoDTO.setResultDownUrl(FileUtils.getFileDownUrl(idpFile.getId(), 1, request));
        }
        idpFileInfoDTO.setFailureCode(idpFile.getFailureCode());
        idpFileInfoDTO.setFailureReason(idpFile.getFailureReason());
        idpFileInfoDTO.setFileSchedule(this.getFileSchedule(idpFile.getId()));
        return idpFileInfoDTO;
    }

    @Override
    public List<IdpFile> selectByIds(List<String> fileIds) {
        return this.baseMapper.selectBatchIds(fileIds);
    }

    @Override
    public String addTemplateFile(MultipartFile file) {
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), FileUtils.getRandomFileName(file.getOriginalFilename()), null);
        String rustFsId = rustFsClient.uploadFile(localFile, rustFsClient.BUCKET_OTHER);
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(file.getOriginalFilename());
        idpFile.setTaskType("TEMPLATE");
        idpFile.setStatus(1);
        idpFile.setUserId(loginClient.getUserId());
        idpFile.setFilePath(rustFsId);
        this.baseMapper.insert(idpFile);
        return idpFile.getId();
    }

    @Override
    public String addTemplateFile(File localFile) {
//        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), FileUtils.getRandomFileName(file.getOriginalFilename()), null);
        String rustFsId = rustFsClient.uploadFile(localFile, rustFsClient.BUCKET_OTHER);
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(localFile.getName());
        idpFile.setTaskType("TEMPLATE");
        idpFile.setStatus(1);
        idpFile.setUserId("1");
        idpFile.setFilePath(rustFsId);
        this.baseMapper.insert(idpFile);
        return idpFile.getId();
    }

    @Override
    public List<IdpFileInfoDTO> getFileInfoByIds(List<String> fileIds, HttpServletRequest request) {
        List<IdpFile> idpFiles = this.selectByIds(fileIds);
        if (CollectionUtils.isEmpty(idpFiles)) {
            throw new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR);
        }
        return idpFiles.stream().map(idpFile -> {
            IdpFileInfoDTO idpFileInfoDTO = new IdpFileInfoDTO();
            idpFileInfoDTO.setFileId(idpFile.getId());
            idpFileInfoDTO.setFileName(idpFile.getFileName());
            idpFileInfoDTO.setStatus(idpFile.getStatus());
            idpFileInfoDTO.setPageCount(idpFile.getPageCount());
            idpFileInfoDTO.setUploadTime(idpFile.getCreateDate());
            idpFileInfoDTO.setReviewStatus(idpFile.getReviewStatus());
            idpFileInfoDTO.setFileDownUrl(FileUtils.getFileDownUrl(idpFile.getId(), 0, request));
        if (FileStatusEnum.isSuccessStatus(idpFile.getStatus())) {
            idpFileInfoDTO.setResultDownUrl(FileUtils.getFileDownUrl(idpFile.getId(), 1, request));
        }
        idpFileInfoDTO.setFailureCode(idpFile.getFailureCode());
            idpFileInfoDTO.setFailureReason(idpFile.getFailureReason());
            idpFileInfoDTO.setFileSchedule(this.getFileSchedule(idpFile.getId()));
            return idpFileInfoDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public void confirmFileResult(String fileId, String newResult) {
        IdpFile idpFile = this.baseMapper.selectById(fileId);
        if (Objects.isNull(idpFile)) {
            throw new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR);
        }
        idpFile.setReviewStatus(FileReviewStatusEnum.CONFIRMED.getValue());
        if (StringUtils.hasText(newResult)) {
            if (isLayoutResult(idpFile)) {
                updateLayoutZipJsonResult(idpFile, newResult);
            } else {
                updateStandaloneResult(idpFile, newResult);
            }
        }
        this.updateById(idpFile);
        Log log = new Log();
        log.setUserId(loginClient.getUserId());
        log.setActionType(LogTypeEnum.CONFIRM.getDescription());
        log.setLeaderId(loginClient.getLeaderId());
        log.setRelatedContent(idpFile.getFileName());
        log.setCreateTime(new Date().getTime());
        log.setUpdateTime(new Date().getTime());
        log.setActionDetail("确认文件 ["+idpFile.getFileName()+"] 的抽取结果");
        logService.insertLog(log);
    }

    @Override
    public void cancelConfirmFileResult(String fileId) {
        IdpFile idpFile = this.baseMapper.selectById(fileId);
        if (Objects.isNull(idpFile)) {
            throw new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR);
        }
        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        this.updateById(idpFile);
    }

    private String getResultBucket(IdpFile idpFile) {
        String taskTypeValue = getTaskTypeValue(idpFile);
        if (taskTypeValue.contains("PARSE") || taskTypeValue.contains("LAYOUT")) {
            return rustFsClient.BUCKET_LAYOUT;
        }
        if (taskTypeValue.contains("SPLIT")) {
            return rustFsClient.BUCKET_SPLIT;
        }
        TaskTypeEnum taskType = TaskTypeEnum.getEnumByType(taskTypeValue);
        switch (taskType) {
            case LAYOUT:
                return rustFsClient.BUCKET_LAYOUT;
            case SPLIT:
                return rustFsClient.BUCKET_SPLIT;
            case EXTRACTION:
            default:
                return rustFsClient.BUCKET_EXTRACT;
        }
    }

    private boolean isLayoutResult(IdpFile idpFile) {
        String taskTypeValue = getTaskTypeValue(idpFile);
        return taskTypeValue.contains("PARSE") || taskTypeValue.contains("LAYOUT") || TaskTypeEnum.LAYOUT.equals(TaskTypeEnum.getEnumByType(taskTypeValue));
    }

    private void updateStandaloneResult(IdpFile idpFile, String newResult) {
        String outFileName = StringUtils.hasText(idpFile.getOutFileName()) ? idpFile.getOutFileName() : safeBaseName(idpFile.getFileName()) + resultFileExtension(idpFile);
        String newFile = FileUtils.stringToFile(UUID.randomUUID() + "/" + outFileName, properties.getTmpPath(), newResult);
        File file = new File(newFile);
        String rustFsId = rustFsClient.uploadFile(file, getResultBucket(idpFile));
        idpFile.setOutFilePath(rustFsId);
        idpFile.setOutFileName(outFileName);
        FileUtils.deleteFile(file.toPath());
    }

    private void updateLayoutZipJsonResult(IdpFile idpFile, String newResult) {
        if (!StringUtils.hasText(idpFile.getOutFilePath())) {
            throw new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR);
        }
        String zipName = StringUtils.hasText(idpFile.getOutFileName()) ? idpFile.getOutFileName() : safeBaseName(idpFile.getFileName()) + ".zip";
        if (!zipName.toLowerCase().endsWith(".zip")) {
            zipName = safeBaseName(zipName) + ".zip";
        }

        Path workDir = Paths.get(properties.getTmpPath(), "confirm-layout", UUID.randomUUID().toString());
        Path zipPath = workDir.resolve(zipName);
        Path unzipDir = workDir.resolve("unzipped");
        Path updatedZipPath = workDir.resolve("updated_" + zipName);
        try {
            Files.createDirectories(workDir);
            try (InputStream inputStream = rustFsClient.downloadFile(idpFile.getOutFilePath())) {
                Files.copy(inputStream, zipPath, StandardCopyOption.REPLACE_EXISTING);
            }
            ZipUtil.unZip(zipPath.toString(), unzipDir.toString());
            Path jsonPath = findMainJsonFile(unzipDir);
            Files.write(jsonPath, newResult.getBytes(StandardCharsets.UTF_8));
            updateLayoutMarkdownAndTxtFiles(unzipDir, jsonPath, newResult);
            ZipUtil.zipFolder(unzipDir.toString(), updatedZipPath.toString());
            String rustFsId = rustFsClient.uploadFile(updatedZipPath.toFile(), rustFsClient.BUCKET_LAYOUT);
            idpFile.setOutFilePath(rustFsId);
            idpFile.setOutFileName(zipName);
        } catch (ComPDFKitException e) {
            throw e;
        } catch (IOException e) {
            log.error("update layout zip json result error, fileId:{}", idpFile.getId(), e);
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_UPLOAD);
        } finally {
            FileUtils.deleteFolder(workDir);
        }
    }

    private void updateLayoutMarkdownAndTxtFiles(Path unzipDir, Path jsonPath, String newResult) throws IOException {
        DocumentAnalysisResult analysisResult = JsonUtils.jsonStringToBean(newResult, DocumentAnalysis.class).getResult();
        if (analysisResult == null || analysisResult.getDetail() == null) {
            throw new ComPDFKitException(ErrorInfoEnum.PARAM_VALIDATE_ERROR);
        }

        Path mdPath = findExistingResultFile(unzipDir, ".md")
                .orElse(jsonPath.resolveSibling("result_md.md"));
        JsonLayoutConverter.json2markdown(analysisResult, mdPath.toString());

        Path txtPath = findExistingResultFile(unzipDir, ".txt")
                .orElse(resolveTxtPath(mdPath));
        String markdown = new String(Files.readAllBytes(mdPath), StandardCharsets.UTF_8);
        if (StringUtils.hasText(markdown)) {
            JsonLayoutConverter.markdownToTxt(markdown, txtPath.toString());
        } else {
            Files.createDirectories(txtPath.getParent());
            Files.write(txtPath, new byte[0]);
        }
    }

    private Optional<Path> findExistingResultFile(Path unzipDir, String extension) throws IOException {
        try (Stream<Path> walk = Files.walk(unzipDir)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(extension))
                    .sorted((left, right) -> {
                        int priorityCompare = Integer.compare(resultFilePriority(left), resultFilePriority(right));
                        if (priorityCompare != 0) {
                            return priorityCompare;
                        }
                        int depthCompare = Integer.compare(unzipDir.relativize(left).getNameCount(), unzipDir.relativize(right).getNameCount());
                        if (depthCompare != 0) {
                            return depthCompare;
                        }
                        return left.getFileName().toString().compareToIgnoreCase(right.getFileName().toString());
                    })
                    .findFirst();
        }
    }

    private int resultFilePriority(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.startsWith("result_") ? 0 : 1;
    }

    private Path resolveTxtPath(Path mdPath) {
        String mdFileName = mdPath.getFileName().toString();
        String lowerName = mdFileName.toLowerCase();
        String txtFileName;
        if (lowerName.endsWith("_md.md")) {
            txtFileName = mdFileName.substring(0, mdFileName.length() - "_md.md".length()) + "_txt.txt";
        } else if (lowerName.endsWith(".md")) {
            txtFileName = mdFileName.substring(0, mdFileName.length() - ".md".length()) + ".txt";
        } else {
            txtFileName = "result_txt.txt";
        }
        return mdPath.resolveSibling(txtFileName);
    }

    private Path findMainJsonFile(Path unzipDir) throws IOException {
        try (Stream<Path> walk = Files.walk(unzipDir)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().toLowerCase().contains("all_result"))
                    .sorted(Comparator.comparingInt(path -> unzipDir.relativize(path).getNameCount()))
                    .findFirst()
                    .orElseThrow(() -> new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR));
        }
    }

    private String safeBaseName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "result";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    private String resultFileExtension(IdpFile idpFile) {
        String taskTypeValue = getTaskTypeValue(idpFile);
        if (taskTypeValue.contains("PARSE") || taskTypeValue.contains("LAYOUT")) {
            return ".json";
        }
        if (taskTypeValue.contains("SPLIT")) {
            return ".zip";
        }
        TaskTypeEnum taskType = TaskTypeEnum.getEnumByType(taskTypeValue);
        switch (taskType) {
            case LAYOUT:
                return ".json";
            case SPLIT:
                return ".zip";
            case EXTRACTION:
            default:
                return ".json";
        }
    }

    private String getTaskTypeValue(IdpFile idpFile) {
        String taskType = StringUtils.hasText(idpFile.getTaskType()) ? idpFile.getTaskType() : idpFile.getType();
        return taskType == null ? "" : taskType.toUpperCase();
    }

    @Override
    public List<IdpFile> getFileListByGroupTemplateId(String groupTemplateId) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<IdpFile>().eq(IdpFile::getGroupTemplateId, groupTemplateId).ne(IdpFile::getStatus, FileStatusEnum.DELETE.getValue()));
    }

    @Override
    public String selectLeaderIdByUserId(String userId) {
        return this.baseMapper.selectLeaderIdByUserId(userId);
    }

    @Override
    public UserInfoPojo selectLeaderIdAndRoleByUserId(String userId) {
        return this.baseMapper.selectLeaderIdAndRoleByUserId(userId);
    }


    /**
     * 解析 在线API
     * 非PDF转成PDF处理
     * 验证登录身份，请求头api-key, RSA解密得到用户ID，验证权限，验证时效性
     *
     * @param file     file
     * @param request  request
     * @param response response
     */
    @Override
    public void parse(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        String apiKey = request.getHeader("x-api-key");
        String leaderId = new RSAUtils().decrypt(apiKey);
        User user = userService.selectByUserId(leaderId);
        if (Objects.isNull(user)) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
        userService.verifyPermission(user, PermissionEnum.PARSE);
        assetService.checkAccountUsable(leaderId);


        String fileExtension = FileUtils.getFileExtension(file.getOriginalFilename()).toLowerCase();

        if (!layoutFileSuffixList.contains(fileExtension)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_CONVERT_FORMAT);
        }
        String bucket = rustFsClient.BUCKET_LAYOUT;
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), UUID.randomUUID() + "/" + file.getOriginalFilename(), null);
        String rustFsId;
        if (!Objects.equals(fileExtension, ".pdf")) {
            // 转换成PDF后存储rustFs
            File pdfFile = actuatorClient.convertToPDF(localFile);
            rustFsId = rustFsClient.uploadFile(pdfFile, bucket);
            FileUtils.deleteFile(pdfFile.toPath());
        } else {
            rustFsId = rustFsClient.uploadFile(localFile, bucket);
        }
        log.info("parse file:{} upload success", file.getOriginalFilename());
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(file.getOriginalFilename());
        idpFile.setCreateDate(LocalDateTime.now());
        idpFile.setFilePath(rustFsId);
        idpFile.setUserId(leaderId);
        idpFile.setType("PARSE-API");
        idpFile.setTaskType("PARSE-API");
        idpFile.setPageCount(FileUtils.getPageCount(localFile.getPath()));
        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        File outFile = null;
        try {
            outFile = actuatorClient.convertLocalFileLayout(idpFile.getFilePath());
            log.info("parse file:{} convertLocalFileLayout success", file.getOriginalFilename());
            idpFile.setOutFilePath(rustFsClient.uploadFile(outFile, rustFsClient.BUCKET_LAYOUT));
            idpFile.setOutFileName(outFile.getName());
            idpFile.setStatus(FileStatusEnum.SUCCESS.getValue());
            assetService.deductAsset(idpFile.getPageCount(), leaderId, leaderId, AssetProductTypeEnum.PARSE);
        } catch (ComPDFKitException e) {
            log.error("file PARSE error,{}", e.getMessage());
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(e.getCode());
            idpFile.setFailureReason(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("file PARSE error,{}", e.getMessage(), e);
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(ErrorInfoEnum.PARSING_ERROR.getCode());
            idpFile.setFailureReason(ErrorInfoEnum.PARSING_ERROR.getUsMsg());
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        } finally {
            idpFile.setUpdateDate(LocalDateTime.now());
            this.save(idpFile);

        }
        log.info("parse file:{} waiting for return", file.getOriginalFilename());
        FileUtils.returnFileStream(outFile.getPath(), outFile.getName(), response);
        FileUtils.deleteFile(localFile.toPath());
        FileUtils.deleteFile(outFile.toPath());
    }

    @Override
    public IdpFileInfoDTO parseOnlineAPIUrlMode(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        String apiKey = request.getHeader("x-api-key");
        String leaderId = new RSAUtils().decrypt(apiKey);
        User user = userService.selectByUserId(leaderId);
        if (Objects.isNull(user)) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
        userService.verifyPermission(user, PermissionEnum.PARSE);
        assetService.checkAccountUsable(leaderId);


        String fileExtension = FileUtils.getFileExtension(file.getOriginalFilename()).toLowerCase();

        if (!layoutFileSuffixList.contains(fileExtension)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_CONVERT_FORMAT);
        }
        String bucket = rustFsClient.BUCKET_LAYOUT;
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), UUID.randomUUID() + "/" + file.getOriginalFilename(), null);
        String rustFsId;
        String originalFilename = file.getOriginalFilename();

        if (!Objects.equals(fileExtension, ".pdf")) {
            // 转换成PDF后存储rustFs
            File pdfFile = actuatorClient.convertToPDF(localFile);
            originalFilename = pdfFile.getName();
            rustFsId = rustFsClient.uploadFile(pdfFile, bucket);
            FileUtils.deleteFile(pdfFile.toPath());
        } else {
            rustFsId = rustFsClient.uploadFile(localFile, bucket);
        }

        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(originalFilename);
        idpFile.setCreateDate(LocalDateTime.now());
        idpFile.setFilePath(rustFsId);
        idpFile.setUserId(leaderId);
        idpFile.setType("PARSE-API");
        idpFile.setTaskType("PARSE-API");
        idpFile.setPageCount(FileUtils.getPageCount(localFile.getPath()));
        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        File outFile = null;
        try {
            outFile = actuatorClient.convertLocalFileLayout(idpFile.getFilePath());
            idpFile.setOutFilePath(rustFsClient.uploadFile(outFile, rustFsClient.BUCKET_LAYOUT));
            idpFile.setOutFileName(outFile.getName());
            idpFile.setStatus(FileStatusEnum.SUCCESS.getValue());
            assetService.deductAsset(idpFile.getPageCount(), leaderId, leaderId, AssetProductTypeEnum.PARSE);
        } catch (ComPDFKitException e) {
            log.error("file PARSE error,{}", e.getMessage());
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(e.getCode());
            idpFile.setFailureReason(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("file PARSE error,{}", e.getMessage(), e);
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(ErrorInfoEnum.PARSING_ERROR.getCode());
            idpFile.setFailureReason(ErrorInfoEnum.PARSING_ERROR.getUsMsg());
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        } finally {
            idpFile.setUpdateDate(LocalDateTime.now());
            this.save(idpFile);

        }
        FileUtils.deleteFile(localFile.toPath());
        FileUtils.deleteFile(outFile.toPath());

        IdpFileInfoDTO idpFileInfoDTO = new IdpFileInfoDTO();
        idpFileInfoDTO.setFileId(idpFile.getId());
        idpFileInfoDTO.setFileName(idpFile.getFileName());
        idpFileInfoDTO.setStatus(idpFile.getStatus());
        idpFileInfoDTO.setPageCount(idpFile.getPageCount());
        idpFileInfoDTO.setUploadTime(idpFile.getCreateDate());
        idpFileInfoDTO.setReviewStatus(idpFile.getReviewStatus());
        idpFileInfoDTO.setFileDownUrl(FileUtils.getFileDownUrl(idpFile.getId(), 0, request));
        if (Objects.equals(idpFile.getStatus(), FileStatusEnum.SUCCESS.getValue())) {
            idpFileInfoDTO.setResultDownUrl(FileUtils.getFileDownUrl(idpFile.getId(), 1, request));
        }
        idpFileInfoDTO.setFailureCode(idpFile.getFailureCode());
        idpFileInfoDTO.setFailureReason(idpFile.getFailureReason());
        idpFileInfoDTO.setFileSchedule(this.getFileSchedule(idpFile.getId()));
        return idpFileInfoDTO;
    }

    @Override
    public void convertToPdf(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        String apiKey = request.getHeader("x-api-key");
        String leaderId = new RSAUtils().decrypt(apiKey);
        User user = userService.selectByUserId(leaderId);
        if (Objects.isNull(user)) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }

        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), UUID.randomUUID() + "/" + file.getOriginalFilename(), null);
        File pdfFile = actuatorClient.convertToPDF(localFile);
        FileUtils.returnFileStream(pdfFile.getPath(), pdfFile.getName(), response);
        FileUtils.deleteFile(localFile.toPath());
        FileUtils.deleteFile(pdfFile.toPath());
    }

    @Override
    public void parseAPI(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        String apiKey = request.getHeader("x-api-key");
        if (!properties.getLicenseKey().equals(apiKey)) {
            throw new ComPDFKitException(ErrorInfoEnum.API_KEY_ERROR);
        }
        LicenseUtils.permissionCheck(ConversionModule.MODULE_PDF_FLAG);

        String fileExtension = FileUtils.getFileExtension(file.getOriginalFilename()).toLowerCase();

        if (!layoutFileSuffixList.contains(fileExtension)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_CONVERT_FORMAT);
        }
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), UUID.randomUUID() + "/" + file.getOriginalFilename(), null);
        File pdfFile;
        if (!Objects.equals(fileExtension, ".pdf")) {
            pdfFile = actuatorClient.convertToPDF(localFile);
            FileUtils.deleteFile(pdfFile.toPath());
        }else {
            pdfFile = localFile;
        }
        File outFile;
        try {
            outFile = actuatorClient.convertLocalFileParseByFileStream(pdfFile);
        } catch (Exception e) {
            log.error("file PARSE error,{}", e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
        FileUtils.returnFileStream(outFile.getPath(), FileUtils.getFileName(Objects.requireNonNull(file.getOriginalFilename())).concat(".zip"), response);
        FileUtils.deleteFile(outFile.toPath());
        FileUtils.deleteFile(pdfFile.toPath());
    }


    @Override
    public void extractAPI(MultipartFile file, ExtractTemplateDTO extractTemplate, List<Integer> pages, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.isNull(extractTemplate)) {
            extractTemplate = new ExtractTemplateDTO();
        }
        String apiKey = request.getHeader("x-api-key");
        if (!properties.getLicenseKey().equals(apiKey)) {
            throw new ComPDFKitException(ErrorInfoEnum.API_KEY_ERROR);
        }

        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), UUID.randomUUID() + "/" + file.getOriginalFilename(), null);
        File pdfFile;
        try {
            if (!localFile.getName().toLowerCase().endsWith(".pdf")) {
                // 转成PDF
                pdfFile = actuatorClient.convertToPDF(localFile);
            }else {
                pdfFile = localFile;
            }
            DataExtractPojo dataExtractPojo = new DataExtractPojo();
            dataExtractPojo.setFile(pdfFile);
            dataExtractPojo.setExtractTemplateDTO(extractTemplate);
            DataExtractDTO dataExtractDTO = llmClient.dataExtractOfVisualModelFileStream(dataExtractPojo);
            String outFileName = FileUtils.getFileName(Objects.requireNonNull(file.getOriginalFilename())).concat(".json");
            String outPath = FileUtils.stringToFile(outFileName, properties.getTmpPath(), dataExtractDTO.getDetails());
            FileUtils.returnFileStream(outPath, outFileName, response);
            FileUtils.deleteFile(pdfFile.toPath());
            FileUtils.deleteFile(Paths.get(outPath));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new ComPDFKitException(ErrorInfoEnum.FILE_INFORMATION_EXTRACTION_FAILED);
        }
    }

}
