package com.compdf.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.compdf.client.ActuatorClient;
import com.compdf.client.LLMClient;
import com.compdf.client.LoginClient;
import com.compdf.client.RustFsClient;
import com.compdf.constant.RabbitMqConstant;
import com.compdf.constant.RedisConstant;
import com.compdf.entity.*;
import com.compdf.enums.*;
import com.compdf.exception.ComPDFKitException;
import com.compdf.pojo.*;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.properties.ResonacProperties;
import com.compdf.service.*;
import com.compdf.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ComPDFKit-WPH 2025/2/20 0020
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ComIDPServiceImpl implements ComIDPService {

    private final ComPDFKitProperties properties;
    private final RustFsClient rustFsClient;
    private final IdpFileService fileService;
    private final LicenseService licenseService;
    private final LLMClient llmClient;
    private final StringRedisTemplate redisTemplate;
    private final ActuatorClient actuatorClient;
    private final RabbitTemplate rabbitTemplate;
    private final LoginClient loginClient;
    private final IdpFileService idpFileService;
    private final TemplateService templateService;
    private final AssetService assetService;
    private final UserService userService;
    @Value("${isWebsite}")
    private Boolean isWebsite;
    private final ResonacProperties resonacProperties;
    private static final DateTimeFormatter exportTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public DataExtractDTO intelligentDocumentExtractionAPI(MultipartFile file, FileParameterDTO fileParameter, HttpServletRequest request) {
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), null);

        // 数据库添加数据记录
        Integer pageCount = FileUtils.getPageCount(localFile.getPath());
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(localFile.getName());
        idpFile.setFilePath(localFile.getPath());
        idpFile.setTaskType(TaskTypeEnum.EXTRACTION.name());
        idpFile.setType(TaskTypeEnum.EXTRACTION.toString());
        idpFile.setStatus(FileStatusEnum.PENDING_EXTRACTION.getValue());
        idpFile.setPageCount(pageCount);
        idpFile.setParameter(JsonUtils.getJsonString(fileParameter));
        idpFile.setOtherInfo(fileParameter.getUser_info());
        if (!isWebsite) {
            idpFile.setUserId(loginClient.getUserId());
        } else {
            idpFile.setUserId(licenseService.selectByLicenseKey(request.getHeader("API_KEY")).getId());
        }
        fileService.save(idpFile);

        DataExtractDTO dataExtractDTO = this.intelligentDocumentExtraction(idpFile);
        log.info("文件提取结果：{}", dataExtractDTO);
        return dataExtractDTO;
    }

    @Override
    public IdpFileInfoDTO extractOnlineAPIUrlmode(MultipartFile file, ExtractTemplateDTO extractTemplate, List<Integer> pages, HttpServletRequest request) {
        if (Objects.isNull(extractTemplate)) {
            extractTemplate = new ExtractTemplateDTO();
        }
        String apiKey = request.getHeader("x-api-key");
        String leaderId = new RSAUtils().decrypt(apiKey);
        User user = userService.selectByUserId(leaderId);
        if (Objects.isNull(user)) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
        userService.verifyPermission(user, PermissionEnum.PARSE);
        assetService.checkAccountUsable(leaderId);
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), UUID.randomUUID() + "/" + file.getOriginalFilename(), null);

        List<File> files = new ArrayList<>();
        IdpFile idpFile = new IdpFile();
        idpFile.setParameter(JsonUtils.getJsonString(extractTemplate));
        idpFile.setUserId(leaderId);
        idpFile.setType("EXTRACT-API");
        idpFile.setTaskType("EXTRACT-API");
        fileService.save(idpFile);
        try {
            if (!localFile.getName().toLowerCase().endsWith(".pdf")) {
                // 转成PDF
                localFile = actuatorClient.convertToPDF(localFile);
            }
//            Integer pageCount = FileUtils.getPageCount(localFile.getPath());
            idpFile.setFileName(localFile.getName());

            String pdfFileRustFsId = rustFsClient.uploadFile(localFile, rustFsClient.BUCKET_EXTRACT);
            // 调用PDF转图片
            // {     "type": "image",     "password": "",     "enable_ai_layout": true,     "contain_image": true,     "json_contain_table": false,     "contain_annotation": true,     "excel_all_content": false,     "excel_single_table_page": true,     "excel_csv_format": false,     "enable_ocr": false,     "compact_text_mode": false,     "txt_table_format": false,     "image_path_enhance": false,     "image_scaling": 1.0,     "page_layout_mode": "flow",     "image_color_mode": "color",     "image_format": "png",     "page_ranges": "1" }
            ConvertParamDTO convertParam = new ConvertParamDTO();
            convertParam.setType("image");
            convertParam.setPassword("");
            convertParam.setEnableAiLayout(true);
            convertParam.setContainImage(true);
            convertParam.setJsonContainTable(false);
            convertParam.setContainAnnotation(true);
            convertParam.setExcelAllContent(false);
            convertParam.setExcelSingleTablePage(false);
            convertParam.setExcelCsvFormat(false);
            convertParam.setEnableOcr(false);
            convertParam.setCompactTextMode(false);
            convertParam.setTxtTableFormat(false);
            convertParam.setImagePathEnhance(false);
            convertParam.setImageScaling("1.0");
            convertParam.setPageLayoutMode("flow");
            convertParam.setImageColorMode("color");
            convertParam.setImageFormat("png");
            convertParam.setFileId("");
            if (CollectionUtils.isEmpty(pages)) {
                convertParam.setPageRanges("");
            } else {
                convertParam.setPageRanges(pages.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            File imageZipFile = actuatorClient.convertLocalFile(localFile, convertParam, null);
            try {
                String unZipFolder = properties.getTmpPath().concat("/" + FileUtils.getFileName(localFile.getName()) + "pdfToImg/");
                ZipUtil.unZip(imageZipFile.getPath(), unZipFolder);
                List<Path> pathList;
                try (Stream<Path> list = Files.list(Paths.get(unZipFolder))) {
                    pathList = list.filter(path -> path.toString().endsWith(".png")).collect(Collectors.toList());
                }
                if (CollectionUtils.isEmpty(pathList)) {
                    throw new ComPDFKitException(ErrorInfoEnum.FILE_INFORMATION_EXTRACTION_FAILED);
                }
                pathList.sort(Comparator.comparingInt(path->{
                    String string = path.toString();
                    String substring = string.substring(string.lastIndexOf("_")+1, string.lastIndexOf("."));
                    return Integer.parseInt(substring);
                }));
                for (Path path : pathList) {
                    files.add(path.toFile());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            DataExtractDTO dataExtractDTO = llmClient.dataExtractOfVisualModelAPI(files, extractTemplate, pages);

            assetService.deductAsset(files.size(), leaderId, leaderId, AssetProductTypeEnum.EXTRACT);

            idpFile.setFilePath(pdfFileRustFsId);
            idpFile.setStatus(FileStatusEnum.EXTRACTION_SUCCESS.getValue());
            idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
            idpFile.setPageCount(files.size());
            idpFile.setStatus(FileStatusEnum.EXTRACTION_SUCCESS.getValue());
            String outFileName = FileUtils.getFileName(idpFile.getFileName()).concat("_comidp_batch_extract").concat(".json");
            String outPath = FileUtils.stringToFile(outFileName, properties.getTmpPath(), JSONUtils.toJSONString(dataExtractDTO.getDetails()));
            idpFile.setOutFilePath(rustFsClient.uploadFile(new File(outPath), rustFsClient.BUCKET_EXTRACT));
            idpFile.setOutFileName(outFileName);


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
            idpFileInfoDTO.setFileSchedule(idpFileService.getFileSchedule(idpFile.getId()));
            return idpFileInfoDTO;
        } catch (ComPDFKitException e) {
            log.error(e.getMessage(), e);
            idpFile.setStatus(FileStatusEnum.EXTRACTION_FAILED.getValue());
            idpFile.setFailureCode(e.getCode());
            idpFile.setFailureReason(e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            idpFile.setStatus(FileStatusEnum.EXTRACTION_FAILED.getValue());
            idpFile.setFailureCode(ErrorInfoEnum.ERROR_INNER.getCode());
            idpFile.setFailureReason(ErrorInfoEnum.ERROR_INNER.getUsMsg());
            throw new ComPDFKitException(ErrorInfoEnum.FILE_INFORMATION_EXTRACTION_FAILED);
        } finally {
            fileService.updateById(idpFile);
        }
    }



    @Override
    public DataExtractDTO intelligentDocumentExtraction(IdpFile idpFile) {
        try {
            idpFile.setStatus(FileStatusEnum.EXTRACTING.getValue());
            fileService.updateById(idpFile);
            redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + idpFile.getId(), "false," + idpFile.getPageCount() + ",0", Duration.ofDays(1));
            String LeaderId = userService.selectLeaderIdByUserId(idpFile.getUserId());
            ExtractTemplateDTO extractTemplateDTO = new ExtractTemplateDTO();
            ExtractTemplateV2DTO extractTemplateV2DTO = new ExtractTemplateV2DTO();
            if (Objects.equals(LeaderId, resonacProperties.getLeaderId())){
                extractTemplateDTO = JsonUtils.jsonStringToBean(idpFile.getParameter(), ExtractTemplateDTO.class);
                extractTemplateDTO.setPage(null);
            }else {
                extractTemplateV2DTO = JsonUtils.jsonStringToBean(idpFile.getParameter(), ExtractTemplateV2DTO.class);
                extractTemplateV2DTO.setPage(null);
            }

            Path path = Paths.get(properties.getTmpPath() + "/" + idpFile.getId() + "_extract_template.pdf");
            try(InputStream inputStream = rustFsClient.downloadFile(idpFile.getFilePath())){
                Files.write(path,
                        IOUtils.toByteArray(inputStream),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            DataExtractDTO dataExtractDTO = llmClient.dataExtractOfVisualModel(DataExtractPojo.builder()
                    .rustFsId(idpFile.getFilePath())
                    .extractTemplateDTO(extractTemplateDTO)
                    .extractTemplateV2DTO(extractTemplateV2DTO)
                    .isV2(!Objects.equals(LeaderId, resonacProperties.getLeaderId()))
                    .file(path.toFile())
                    .taskId(null)
                    .build());
            UserInfoPojo userInfoPojo = idpFileService.selectLeaderIdAndRoleByUserId(idpFile.getUserId());
            if (userInfoPojo.getRole().equals("user")) {
                assetService.deductAsset(idpFile.getPageCount(), userInfoPojo.getLeaderId(), userInfoPojo.getUserId(), AssetProductTypeEnum.EXTRACT);
            }else if (userInfoPojo.getRole().equals("manager")) {
                assetService.deductAsset(idpFile.getPageCount(), userInfoPojo.getUserId(), userInfoPojo.getUserId(), AssetProductTypeEnum.EXTRACT);
            }
            idpFile.setStatus(FileStatusEnum.EXTRACTION_SUCCESS.getValue());
            String outFileName = FileUtils.getFileName(idpFile.getFileName()).concat(".json");
            String outPath = FileUtils.stringToFile(UUID.randomUUID() + "/" + outFileName, properties.getTmpPath(), dataExtractDTO.getDetails());
            if (!Objects.equals(LeaderId, resonacProperties.getLeaderId())){
                JsonExtractConvert.json2json(dataExtractDTO.getDetails(), outPath, extractTemplateV2DTO);
            }
            File outFile = new File(outPath);
            String rustFsId = rustFsClient.uploadFile(outFile, rustFsClient.BUCKET_EXTRACT);
            idpFile.setOutFilePath(rustFsId);
            idpFile.setOutFileName(outFileName);
            FileUtils.deleteFile(outFile.toPath());
            return dataExtractDTO;
        } catch (ComPDFKitException e) {
            log.error(e.getMessage(), e);
            idpFile.setStatus(FileStatusEnum.EXTRACTION_FAILED.getValue());
            idpFile.setFailureCode(e.getCode());
            idpFile.setFailureReason(e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            idpFile.setStatus(FileStatusEnum.EXTRACTION_FAILED.getValue());
            idpFile.setFailureCode(ErrorInfoEnum.ERROR_INNER.getCode());
            idpFile.setFailureReason(ErrorInfoEnum.ERROR_INNER.getUsMsg());
            throw new ComPDFKitException(ErrorInfoEnum.FILE_INFORMATION_EXTRACTION_FAILED);
        } finally {
            fileService.updateById(idpFile);
        }
    }


    @Override
    public Integer getOverage(HttpServletRequest request) {
        String requestIP = IPUtils.getRequestIP(request);
        String websiteKey = RedisConstant.WEBSITE_API_OVERAGE_KEY.concat(requestIP);
        if (!redisTemplate.hasKey(websiteKey)) {
            return 10;
        } else {
            int overage = Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get(websiteKey)));
            return Math.max(overage, 0);
        }
    }

    @Override
    public DataExtractDTO apiExtractFile(String fileId) {
        // 检查文件状态
        IdpFile fileInfo = getIdpFile(fileId);
        if (fileInfo == null) return null;
        // 开始处理
        fileInfo.setStatus(FileStatusEnum.EXTRACTING.getValue());
        fileInfo.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        String groupTemplateId = fileInfo.getGroupTemplateId();
        Template template = templateService.getTemplateByGroupTemplateId(groupTemplateId);
        String content = template.getContent();
        fileInfo.setParameter(content);
        fileService.updateById(fileInfo);
        return this.intelligentDocumentExtraction(fileInfo);
    }

    @Nullable
    private IdpFile getIdpFile(String fileId) {
        IdpFile fileInfo = fileService.selectById(fileId);
        if (fileInfo == null) {
            log.error("File information does not exist.fileID: {}", fileId);
            return null;
        }
        if (!Objects.equals(fileInfo.getStatus(), FileStatusEnum.CREATED.getValue())
                && !Objects.equals(fileInfo.getStatus(), FileStatusEnum.PENDING_EXTRACTION.getValue())) {
            log.error("File status not the initial state.fileID: {}, fileStatus: {}", fileId, fileInfo.getStatus());
            return null;
        }
        return fileInfo;
    }

    @Override
    public FileResultDTO apiResolveFile(String fileId) {
        IdpFile fileInfo = getIdpFile(fileId);
        if (fileInfo == null) return null;
        // 调用文件解析
        FileParameterDTO fileParameterDTO = new FileParameterDTO();

        fileParameterDTO.setIsBilk(true);
        return fileResolve(fileInfo, fileParameterDTO, null);
    }

    /**
     * 文件解析
     *
     * @param idpFile       文件
     * @param fileParameter 文件处理参数
     * @return 结果文件
     */
    @Override
    public FileResultDTO fileResolve(IdpFile idpFile, FileParameterDTO fileParameter, HttpServletRequest request) {
        idpFile.setStatus(FileStatusEnum.PROCESSING.getValue());
        fileService.updateById(idpFile);

        File outFile = null;
        try {
            outFile = actuatorClient.convertLocalFileLayout(idpFile.getFilePath(), fileParameter);
//            outFile = layoutFileHandle(outFile, idpFile, fileParameter, properties);
            idpFile.setOutFilePath(rustFsClient.uploadFile(outFile, rustFsClient.BUCKET_LAYOUT));
            idpFile.setOutFileName(outFile.getName());
            idpFile.setStatus(FileStatusEnum.SUCCESS.getValue());
            deductParseAsset(idpFile);
        } catch (ComPDFKitException e) {
            log.error("file resolve error,{}", e.getMessage());
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(e.getCode());
            idpFile.setFailureReason(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("file resolve error,{}", e.getMessage(), e);
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(ErrorInfoEnum.PARSING_ERROR.getCode());
            idpFile.setFailureReason(ErrorInfoEnum.PARSING_ERROR.getUsMsg());
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        } finally {
            fileService.updateById(idpFile);
            if (!Objects.isNull(outFile)) {
                FileUtils.deleteFile(outFile.toPath());
            }
        }
        return FileResultDTO.builder().fileId(idpFile.getId()).downFileUrl(FileUtils.getFileDownUrl(outFile.getPath(), request)).build();
    }

    @Override
    public FileResultDTO apiFileEditResolve(JsonEditDTO editDTO, HttpServletRequest request) {
        String fileId = editDTO.getFileId();
        String language = editDTO.getLanguage();
        IdpFile idpFile = fileService.selectById(fileId);
        String outZipFilePath = idpFile.getOutFilePath();
        try {
            File outZipFolder = new File(FileUtils.getFileName(outZipFilePath));
            if (!outZipFolder.exists()) {
                ZipUtil.unZip(outZipFilePath, outZipFolder.getPath());
            }
            File[] jsonFiles = outZipFolder.listFiles(file -> file.getName().endsWith(".json"));
            File[] mdFiles = outZipFolder.listFiles(file -> file.getName().endsWith(".md"));
            if (jsonFiles == null) {
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
            }
            File jsonFile = jsonFiles[0];
            if (mdFiles == null) {
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
            }
            File mdFile = mdFiles[0];

            DocumentAnalysisResult analysisResult =
                    JsonUtils.jsonStringToBean(new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8), DocumentAnalysisResult.class);
            List<EditDTO> edits = editDTO.getEdits();
            edits.forEach(edit -> {
                Integer pageId = edit.getPage_id();
                switch (edit.getActionType()) {
                    case "add":
                        List<Double> position = edit.getPosition();
                        String imagePath = outZipFolder + "/page/page_" + pageId + ".png";
                        String croppedImagePath = properties.getTmpPath() + "/" + FileUtils.getRandomFileName("croppedImg") + ".png";
                        // 裁剪图片
                        File imageCropped = FileUtils.imageCropped(imagePath, croppedImagePath,
                                position.get(0).intValue(),
                                position.get(1).intValue(),
                                position.get(2).intValue() - position.get(0).intValue(),
                                position.get(5).intValue() - position.get(3).intValue());
                        // ocr txt 识别
                        ConvertParamDTO convertParam = getConvertOCRParamDTO(language, idpFile);
                        File txtFile = actuatorClient.convertLocalFile(imageCropped, convertParam, null);
                        try {
                            String ocrTxtResultStr = new String(Files.readAllBytes(txtFile.toPath()), StandardCharsets.UTF_8);
                            JsonLayoutConverter.insertItemByPos(analysisResult,
                                    pageId,
                                    position.stream().mapToDouble(Double::doubleValue).toArray(),
                                    ocrTxtResultStr,
                                    edit.getType());
                        } catch (Exception e) {
                            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
                        }
                        break;
                    case "delete":
                        List<DocumentAnalysisResult.Page> pages = analysisResult.getPages().stream()
                                .filter(page -> Objects.equals(page.getPage_id(), pageId)).collect(Collectors.toList());
                        pages.forEach(page -> {
                            List<DocumentAnalysisResult.Content> contentList = page.getContent().stream()
                                    .filter(content -> Objects.equals(content.getPosition(), edit.getPosition())).collect(Collectors.toList());
                            contentList.forEach(content -> page.getContent().remove(content));
                        });

                        List<DocumentAnalysisResult.Detail> details = analysisResult.getDetail().stream()
                                .filter(detail -> Objects.equals(detail.getPage_id(), pageId)
                                        && Objects.equals(detail.getPosition(), edit.getPosition())).collect(Collectors.toList());
                        details.forEach(detail -> analysisResult.getDetail().remove(detail));

                        List<DocumentAnalysisResult.TocItem> tocItems = analysisResult.getCatalog().getToc().stream()
                                .filter(tocItem -> Objects.equals(tocItem.getPage_id(), pageId)
                                        && Objects.equals(tocItem.getPos(), edit.getPosition())).collect(Collectors.toList());
                        tocItems.forEach(tocItem -> analysisResult.getCatalog().getToc().remove(tocItem));
                        break;
                    case "update":
                        List<DocumentAnalysisResult.Page> updatePages = analysisResult.getPages().stream()
                                .filter(page -> Objects.equals(page.getPage_id(), pageId)).collect(Collectors.toList());
                        updatePages.forEach(page -> {
                            List<DocumentAnalysisResult.Content> contentList = page.getContent().stream()
                                    .filter(content -> Objects.equals(content.getPosition(), edit.getPosition())).collect(Collectors.toList());
                            contentList.forEach(content -> content.setType(edit.getType()));
                        });

                        List<DocumentAnalysisResult.Detail> updateDetails = analysisResult.getDetail().stream()
                                .filter(detail -> Objects.equals(detail.getPage_id(), pageId)
                                        && Objects.equals(detail.getPosition(), edit.getPosition())).collect(Collectors.toList());
                        updateDetails.forEach(detail -> detail.setType(edit.getType()));


                        List<DocumentAnalysisResult.TocItem> updateFilterTocItems = analysisResult.getCatalog().getToc().stream()
                                .filter(tocItem -> Objects.equals(tocItem.getPage_id(), pageId)
                                        && Objects.equals(tocItem.getPos(), edit.getPosition())).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(updateFilterTocItems) &&
                                JsonLayoutConverter.CATALOG_TYPE_LIST.contains(edit.getType())) {
                            // Toc中添加记录
                            List<DocumentAnalysisResult.TocItem> updateTocItems = analysisResult.getCatalog().getToc();
                            DocumentAnalysisResult.TocItem newTocItem = new DocumentAnalysisResult.TocItem();
                            newTocItem.setPage_id(pageId);
                            newTocItem.setParagraph_id(0);
                            newTocItem.setPos(edit.getPosition());
                            newTocItem.setType(edit.getType());
                            newTocItem.setContent(updateDetails.get(0).getText());
                            updateTocItems.add(newTocItem);
                            JsonLayoutConverter.sortCatalogByPosAndPageId(updateTocItems);
                        } else if (!CollectionUtils.isEmpty(updateFilterTocItems) &&
                                !JsonLayoutConverter.CATALOG_TYPE_LIST.contains(edit.getType())) {
                            // Toc中删除记录
                            updateFilterTocItems.forEach(toc -> analysisResult.getCatalog().getToc().remove(toc));
                        } else if (!CollectionUtils.isEmpty(updateFilterTocItems) &&
                                JsonLayoutConverter.CATALOG_TYPE_LIST.contains(edit.getType())) {
                            updateFilterTocItems.forEach(toc -> analysisResult.getCatalog().getToc().get(analysisResult.getCatalog().getToc().indexOf(toc)).setType(edit.getType()));
                        }
                        break;
                }
            });
            Files.write(jsonFile.toPath(), JsonUtils.getJsonString(analysisResult).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            JsonLayoutConverter.json2markdown(analysisResult, mdFile.getPath());
            String outNewJsonResult = ZipUtil.zipFolder(outZipFolder.getPath(), outZipFolder + ".zip");
            FileResultDTO fileResultDTO = new FileResultDTO();
            fileResultDTO.setFileId(fileId);
            fileResultDTO.setDownFileUrl(FileUtils.getFileDownUrl(outNewJsonResult, request));
            return fileResultDTO;
        } catch (Exception e) {
            throw new ComPDFKitException(e.getMessage());
        }
    }

    @Override
    public FileResultDTO fileResolveApi(IdpFile idpFile, FileParameterDTO fileParameter, HttpServletRequest request) {
        idpFile.setStatus(FileStatusEnum.PROCESSING.getValue());
        fileService.updateById(idpFile);
        File outFile;
        try {
            outFile = actuatorClient.convertLocalFileLayout(rustFsClient.uploadFile(new File(idpFile.getFilePath()), rustFsClient.BUCKET_LAYOUT));
//            outFile = layoutFileHandle(outFile, idpFile, fileParameter, properties);
            idpFile.setOutFilePath(outFile.getPath());
            idpFile.setOutFileName(outFile.getName());
            idpFile.setStatus(FileStatusEnum.SUCCESS.getValue());
            deductParseAsset(idpFile);
        } catch (ComPDFKitException e) {
            log.error("file resolve error,{}", e.getMessage());
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(e.getCode());
            idpFile.setFailureReason(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("file resolve error,{}", e.getMessage(), e);
            idpFile.setStatus(FileStatusEnum.FAIL.getValue());
            idpFile.setFailureCode(ErrorInfoEnum.PARSING_ERROR.getCode());
            idpFile.setFailureReason(ErrorInfoEnum.PARSING_ERROR.getUsMsg());
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        } finally {
            fileService.updateById(idpFile);
        }
        return FileResultDTO.builder().fileId(idpFile.getId()).downFileUrl(FileUtils.getFileDownUrl(outFile.getPath(), request)).build();
    }

    private void deductParseAsset(IdpFile idpFile) {
        if (idpFile == null || idpFile.getUserId() == null || idpFile.getUserId().trim().isEmpty()) {
            return;
        }
        UserInfoPojo userInfoPojo = idpFileService.selectLeaderIdAndRoleByUserId(idpFile.getUserId());
        if (userInfoPojo == null) {
            return;
        }
        if (Objects.equals(userInfoPojo.getRole(), "user")) {
            assetService.deductAsset(idpFile.getPageCount(), userInfoPojo.getLeaderId(), userInfoPojo.getUserId(), AssetProductTypeEnum.PARSE);
        } else if (Objects.equals(userInfoPojo.getRole(), "manager")) {
            assetService.deductAsset(idpFile.getPageCount(), userInfoPojo.getUserId(), userInfoPojo.getUserId(), AssetProductTypeEnum.PARSE);
        }
    }

    @Override
    public FileResultDTO apiSplitFile(String fileId) {
        IdpFile fileInfo = getIdpFile(fileId);
        if (fileInfo == null) return null;
        // 调用文件解析
        ConvertParamDTO parameterPojo = JsonUtils.jsonStringToBean(fileInfo.getParameter(), ConvertParamDTO.class);
        return fileSplit(fileInfo, parameterPojo);
    }

    @Override
    public void getFileById(String id, Integer flag, HttpServletResponse response) {
        IdpFile idpFile = idpFileService.selectById(id);
        String path;
        String fileName;
        if (Objects.isNull(idpFile)) return;
        if (Objects.equals(flag, 0)) {
            path = idpFile.getFilePath();
            fileName = idpFile.getFileName();
        } else {
            path = idpFile.getOutFilePath();
            fileName = idpFile.getOutFileName();
        }
        try (InputStream in = rustFsClient.downloadFile(path)) {
            FileUtils.returnFileStream(in, fileName, response);
        } catch (IOException e) {
            log.error("return file stream error,{}", e.getMessage(), e);
        }
    }

    @Override
    public void splitExport(List<String> fileIds, HttpServletResponse response) {
        List<IdpFile> idpFiles = idpFileService.selectByIds(fileIds);
        Path tempDir = Paths.get(properties.getTmpPath() + "/splitExport/" + UUID.randomUUID());
        String outputZip = tempDir + ".zip";
        try {
            Files.createDirectories(tempDir);
            AtomicReference<Path> targetPath = new AtomicReference<>();
            idpFiles.forEach(idpFile -> {
                if (!idpFile.getStatus().equals(FileStatusEnum.SUCCESS.getValue())) {
                    return;
                }
                String rustFsOutFileId = idpFile.getOutFilePath();
                try (InputStream inputStream = rustFsClient.downloadFile(rustFsOutFileId)) {
                    targetPath.set(getUniqueFilePath(tempDir, idpFile.getOutFileName()));
                    Files.copy(inputStream, targetPath.get());
                } catch (Exception e) {
                    log.error("download split file error,{}", e.getMessage(), e);
                    throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
                }
            });
            if (idpFiles.size() > 1) {
                ZipUtil.zipFolder(String.valueOf(tempDir), outputZip);
                FileUtils.returnFileStream(outputZip, "split_files.zip", response);
            } else {
                FileUtils.returnFileStream(targetPath.get().toString(), targetPath.get().getFileName().toString(), response);
            }
        } catch (IOException e) {
            log.error("split export error,{}", e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        } finally {
            FileUtils.deleteFolder(tempDir);
            FileUtils.deleteFile(Paths.get(outputZip));
        }
    }

    @Override
    public void layoutExport(FileExportDTO fileExportDTO, HttpServletResponse response) {
        List<IdpFile> idpFiles = idpFileService.selectByIds(fileExportDTO.getFileIds());
        Path tempDir = Paths.get(properties.getTmpPath() + "/layoutExport/" + UUID.randomUUID());
        String outputZip = tempDir + ".zip";
        try {
            Files.createDirectories(tempDir);
            idpFiles.forEach(idpFile -> {
                String rustFsOutFileId = idpFile.getOutFilePath();
                try (InputStream inputStream = rustFsClient.downloadFile(rustFsOutFileId)) {
                    Path targetPath = getUniqueFilePath(tempDir, idpFile.getOutFileName());
                    File outFile = targetPath.toFile();
                    Files.copy(inputStream, targetPath);
                    // 处理文件
                    JsonLayoutConverter.layoutFileHandle(outFile, fileExportDTO.getExportFormat());
                } catch (Exception e) {
                    log.error("download layout file error,{}", e.getMessage(), e);
                    throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
                }
            });
            if (idpFiles.size() == 1){
                File[] files = tempDir.toFile().listFiles();
                if (files != null && files.length > 0) {
                    File singleFile = files[0];
                    FileUtils.returnFileStream(singleFile.getPath(), singleFile.getName(), response);
                    return;
                }
            }
            ZipUtil.zipFolder(String.valueOf(tempDir), outputZip);
            FileUtils.returnFileStream(outputZip, "layout_files.zip", response);
        } catch (IOException e) {
            log.error("layout export error,{}", e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        } finally {
            FileUtils.deleteFolder(tempDir);
            FileUtils.deleteFile(Paths.get(outputZip));
        }
    }

    @Override
    public void extractExport(FileExportDTO fileExportDTO, HttpServletResponse response) {
        List<IdpFile> idpFiles = idpFileService.selectByIds(fileExportDTO.getFileIds());
        Path tempDir = Paths.get(properties.getTmpPath() + "/extractExport/" + UUID.randomUUID());
        String outputZip = tempDir + ".zip";

        try {
            Files.createDirectories(tempDir);
            if (fileExportDTO.getIsCompress()){
                idpFiles.forEach(idpFile -> {
                    String rustFsOutFileId = idpFile.getOutFilePath();
                    try (InputStream inputStream = rustFsClient.downloadFile(rustFsOutFileId)) {
                        Path targetPath = getUniqueFilePath(tempDir, idpFile.getOutFileName());
                        File outFile = targetPath.toFile();
                        Files.copy(inputStream, targetPath);
                        idpFile.setOutFilePath(outFile.getPath());
                    } catch (Exception e) {
                        log.error("download extract file error,{}", e.getMessage(), e);
                        throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
                    }
                });
                // idpFiles按照groupTemplateId进行分组
                Map<String, List<IdpFile>> groupTemplateMap = idpFiles.stream().collect(Collectors.groupingBy(IdpFile::getGroupTemplateId));
                switch (fileExportDTO.getExportFormat()) {
                    case "JSON":
                        StringBuffer jsonBuffer = new StringBuffer();
                        jsonBuffer.append("{");
                        AtomicInteger groupIdx = new AtomicInteger(0);
                        int groupTotal = groupTemplateMap.size();
                        groupTemplateMap.values().forEach(groupIdpFileList -> {
                            ExtractTemplateV2DTO extractTemplateDTO = JsonUtils.jsonStringToBean(groupIdpFileList.get(0).getParameter(), ExtractTemplateV2DTO.class);
                            jsonBuffer.append("\"").append(extractTemplateDTO.getName()).append("\": [");
                            groupIdpFileList.forEach(groupIdpFile -> {
                                try {
                                    String jsonStr = new String(Files.readAllBytes(Paths.get(groupIdpFile.getOutFilePath())), StandardCharsets.UTF_8);
                                    JsonExtractConvert.json2json(jsonStr, groupIdpFile.getOutFilePath(), extractTemplateDTO);
                                    jsonBuffer.append(new String(Files.readAllBytes(Paths.get(groupIdpFile.getOutFilePath())), StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                if (groupIdpFileList.indexOf(groupIdpFile) != groupIdpFileList.size() - 1) {
                                    jsonBuffer.append(",");
                                }
                            });
                            jsonBuffer.append("]");
                            if (groupIdx.incrementAndGet() < groupTotal) {
                                jsonBuffer.append(",");
                            }
                        });
                        jsonBuffer.append("}");
                        String string = jsonBuffer.toString();
                        File outFile = new File(tempDir + "/" + "Extraction_BatchExport_"+ LocalDateTime.now().format(exportTimeFormatter) + ".json");
                        Files.write(outFile.toPath(), string.getBytes(StandardCharsets.UTF_8));
                        FileUtils.returnFileStream(outFile.getPath(), outFile.getName(), response);
                        break;
                    case "CSV":
                        File outFileCsv = new File(tempDir + "/" + "Extraction_BatchExport_" + LocalDateTime.now().format(exportTimeFormatter) + ".zip");
                        JsonExtractConvert.json2csvCompress(groupTemplateMap, tempDir, outFileCsv);
                        // 多模板情况下会生成ZIP文件
                        if (groupTemplateMap.size() > 1) {
                            File zipFile = new File(outFileCsv.getPath().replace(".csv", ".zip"));
                            FileUtils.returnFileStream(zipFile.getPath(), zipFile.getName(), response);
                        } else {
                            FileUtils.returnFileStream(outFileCsv.getPath(), outFileCsv.getName(), response);
                        }
                        break;
                    case "EXCEL":
                        File outFileXlsx = new File(tempDir + "/" + "Extraction_BatchExport_" + LocalDateTime.now().format(exportTimeFormatter) + ".xlsx");
                        JsonExtractConvert.json2excelCompress(groupTemplateMap, tempDir, outFileXlsx);
                        FileUtils.returnFileStream(outFileXlsx.getPath(), outFileXlsx.getName(), response);
                        break;
                }

            }else {
                idpFiles.forEach(idpFile -> {
                    String rustFsOutFileId = idpFile.getOutFilePath();
                    try (InputStream inputStream = rustFsClient.downloadFile(rustFsOutFileId)) {
                        ExtractTemplateV2DTO extractTemplateV2DTO = new ExtractTemplateV2DTO();
                        ExtractTemplateDTO extractTemplateDTO = new ExtractTemplateDTO();
                        String resultFileName;
                        if (loginClient.getLeaderId().equals(resonacProperties.getLeaderId())){
                            extractTemplateDTO = JsonUtils.jsonStringToBean(idpFile.getParameter(), ExtractTemplateDTO.class);
                            resultFileName = idpFile.getFileName()+"_"+extractTemplateDTO.getName()+"_Result.json";
                        }else {
                            extractTemplateV2DTO = JsonUtils.jsonStringToBean(idpFile.getParameter(), ExtractTemplateV2DTO.class);
                            resultFileName = idpFile.getFileName()+"_"+extractTemplateV2DTO.getName()+"_Result.json";
                        }

                        Path targetPath = getUniqueFilePath(tempDir, resultFileName);
                        File outFile = targetPath.toFile();
                        Files.copy(inputStream, targetPath);
                        String json = new String(Files.readAllBytes(outFile.toPath()), StandardCharsets.UTF_8);
                        // 处理文件
                        switch (fileExportDTO.getExportFormat()) {
                            case "JSON":
                                JsonExtractConvert.json2json(json, outFile.getPath(), extractTemplateV2DTO);
                                // do nothing
                                break;
                            case "CSV":
                                String csvFileName = outFile.getName().replace(".json", ".csv");
                                File outFileCsv = getUniqueFilePath(tempDir, csvFileName).toFile();
                                if (loginClient.getLeaderId().equals(resonacProperties.getLeaderId())){
                                    JsonExtractConvert.json2csvOnResonac(json, outFileCsv.getPath(), resonacProperties, JsonUtils.jsonStringToBean(idpFile.getParameter(), ExtractTemplateDTO.class));
                                }else {
                                    JsonExtractConvert.json2csv(json, outFileCsv.getPath(), extractTemplateV2DTO);
                                }
                                FileUtils.deleteFile(outFile.toPath());
                                break;
                            case "EXCEL":
                                String xlsxFileName = outFile.getName().replace(".json", ".xlsx");
                                File outFileXlsx = getUniqueFilePath(tempDir, xlsxFileName).toFile();
                                JsonExtractConvert.json2excel(json, outFileXlsx.getPath(), extractTemplateV2DTO);
                                FileUtils.deleteFile(outFile.toPath());
                                break;
                        }
                    } catch (Exception e) {
                        log.error("download extract file error,{}", e.getMessage(), e);
                        throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
                    }
                });
                if (idpFiles.size() == 1 && !loginClient.getLeaderId().equals(resonacProperties.getLeaderId())){
                    File[] files = tempDir.toFile().listFiles();
                    if (files != null && files.length > 0) {
                        File singleFile = files[0];
                        FileUtils.returnFileStream(singleFile.getPath(), singleFile.getName(), response);
                        return;
                    }
                }
                ZipUtil.zipFolder(String.valueOf(tempDir), outputZip);
                FileUtils.returnFileStream(outputZip, "Extraction_IndividualExport_"+LocalDateTime.now().format(exportTimeFormatter) + ".zip", response);
            }

        } catch (IOException e) {
            log.error("extract export error,{}", e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        } finally {
            FileUtils.deleteFolder(tempDir);
            FileUtils.deleteFile(Paths.get(outputZip));
        }
    }

    @Override
    public void fileManualGroup(FileManualGroupDTO fileManualGroupDTO) {
        IdpFile idpFile = fileService.selectById(fileManualGroupDTO.getFileId());
        if (Objects.isNull(idpFile)) {
            throw new ComPDFKitException(ErrorInfoEnum.FILE_NOT_EXIST_ERROR);
        }
        idpFile.setGroupTemplateId(fileManualGroupDTO.getGroupTemplateId());
        idpFile.setStatus(FileStatusEnum.PENDING_EXTRACTION.getValue());
        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        fileService.updateById(idpFile);
        redisTemplate.opsForValue().set(RedisConstant.IDP_FILE_HANDLER + idpFile.getId(), "false," + idpFile.getPageCount() + ",0", Duration.ofDays(1));
        rabbitTemplate.convertAndSend(RabbitMqConstant.IDP_HANDLE_EXCHANGE, RabbitMqConstant.API_EXTRACT_FILE_HANDLE_ROUTING_KEY, idpFile.getId());
    }

    @Override
    public String testExtract(MultipartFile file, String extractTemplateDTOJson, HttpServletRequest request) {
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), FileUtils.getRandomFileName(file.getOriginalFilename()), null);
        String rustFsId = rustFsClient.uploadFile(localFile, rustFsClient.BUCKET_EXTRACT);
        DataExtractDTO dataExtractDTO;
        ExtractTemplateV2DTO extractTemplateDTOV2 = null;
        if (loginClient.getLeaderId().equals(resonacProperties.getLeaderId())){
            ExtractTemplateDTO extractTemplateDTO = JsonUtils.jsonStringToBean(extractTemplateDTOJson, ExtractTemplateDTO.class);
            dataExtractDTO = llmClient.dataExtractOfVisualModel(DataExtractPojo.builder().extractTemplateDTO(extractTemplateDTO).isV2(false).rustFsId(rustFsId).taskId(null).build());
        }else {
            extractTemplateDTOV2 = JsonUtils.jsonStringToBean(extractTemplateDTOJson, ExtractTemplateV2DTO.class);
            dataExtractDTO = llmClient.dataExtractOfVisualModel(DataExtractPojo.builder().extractTemplateV2DTO(extractTemplateDTOV2).isV2(true).rustFsId(rustFsId).taskId(null).build());
        }
        String outFileName = FileUtils.getFileName(Objects.requireNonNull(file.getOriginalFilename())).concat(".json");
        String outPath = FileUtils.stringToFile(UUID.randomUUID() + "/" + outFileName, properties.getTmpPath(), JSONUtils.toJSONString(dataExtractDTO.getDetails()));
        if (!loginClient.getLeaderId().equals(resonacProperties.getLeaderId())){
            JsonExtractConvert.json2json(dataExtractDTO.getDetails(), outPath, extractTemplateDTOV2);
        }
        File outFile = new File(outPath);
        String outRustFsId = rustFsClient.uploadFile(outFile, rustFsClient.BUCKET_EXTRACT);
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(file.getOriginalFilename());
        idpFile.setFilePath(rustFsId);
        idpFile.setOutFileName(outFileName);
        idpFile.setOutFilePath(outRustFsId);
        idpFile.setTaskType(TaskTypeEnum.TEST_EXTRACTION.name());
        idpFile.setStatus(FileStatusEnum.EXTRACTION_SUCCESS.getValue());
        idpFile.setReviewStatus(FileReviewStatusEnum.NOT_CONFIRMED.getValue());
        idpFile.setParameter(extractTemplateDTOJson);
        idpFile.setUserId(loginClient.getUserId());
        fileService.save(idpFile);
        return FileUtils.getFileDownUrl(idpFile.getId(), 1, request);
    }

    private FileResultDTO fileSplit(IdpFile idpFile, ConvertParamDTO parameterPojo) {
        idpFile.setStatus(FileStatusEnum.PROCESSING.getValue());
        fileService.updateById(idpFile);
        File outFile;
        String rustFsId;
        try {
            outFile = actuatorClient.pdfFileSplit(idpFile.getFilePath(), parameterPojo, null);
            rustFsId = rustFsClient.uploadFile(outFile, rustFsClient.BUCKET_SPLIT);
            idpFile.setOutFilePath(rustFsId);
            idpFile.setOutFileName(idpFile.getFileName().replace(".pdf", ".zip"));
            idpFile.setStatus(FileStatusEnum.SUCCESS.getValue());
        } catch (ComPDFKitException e) {
            log.error("file split error,{}", e.getMessage());
            idpFile.setStatus(FileStatusEnum.EXTRACTION_FAILED.getValue());
            idpFile.setFailureCode(e.getCode());
            idpFile.setFailureReason(e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            idpFile.setStatus(FileStatusEnum.EXTRACTION_FAILED.getValue());
            idpFile.setFailureCode(ErrorInfoEnum.PARSING_ERROR.getCode());
            idpFile.setFailureReason(ErrorInfoEnum.PARSING_ERROR.getUsMsg());
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        } finally {
            fileService.updateById(idpFile);
        }
        return FileResultDTO.builder().fileId(idpFile.getId()).downFileUrl(FileUtils.getFileDownUrl(rustFsId, null)).build();
    }


    @NotNull
    private static ConvertParamDTO getConvertOCRParamDTO(String language, IdpFile idpFile) {
        ConvertParamDTO convertParam = new ConvertParamDTO();
        convertParam.setType("txt");
        convertParam.setPassword("");
        convertParam.setEnableAiLayout(true);
        convertParam.setContainImage(true);
        convertParam.setJsonContainTable(true);
        convertParam.setContainAnnotation(true);
        convertParam.setExcelAllContent(false);
        convertParam.setExcelSingleTablePage(false);
        convertParam.setExcelCsvFormat(false);
        convertParam.setEnableOcr(true);
        convertParam.setOcrLanguage(language);
        convertParam.setCompactTextMode(true);
        convertParam.setTxtTableFormat(true);
        convertParam.setImagePathEnhance(false);
        convertParam.setImageScaling("1.0");
        convertParam.setPageLayoutMode("flow");
        convertParam.setImageColorMode("color");
        convertParam.setImageFormat("png");
        convertParam.setFileId(idpFile.getId());
        convertParam.setPageRanges("");
        return convertParam;
    }

    /**
     * 生成唯一文件路径，当文件名冲突时自动添加后缀 (1), (2), ...
     */
    private Path getUniqueFilePath(Path dir, String fileName) {
        Path target = dir.resolve(fileName);
        if (!Files.exists(target)) {
            return target;
        }
        String baseName;
        String extension;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        } else {
            baseName = fileName;
            extension = "";
        }
        int counter = 1;
        while (Files.exists(target)) {
            target = dir.resolve(baseName + "(" + counter + ")" + extension);
            counter++;
        }
        return target;
    }

}
