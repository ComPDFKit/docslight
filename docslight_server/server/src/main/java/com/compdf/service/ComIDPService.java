package com.compdf.service;

import com.compdf.entity.IdpFile;
import com.compdf.pojo.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/2/20 0020
 * <p>
 * IDP业务处理Service
 */
public interface ComIDPService {

    DataExtractDTO intelligentDocumentExtractionAPI(MultipartFile file, FileParameterDTO fileParameter, HttpServletRequest request);


    IdpFileInfoDTO extractOnlineAPIUrlmode(MultipartFile localFile, ExtractTemplateDTO extractTemplate, List<Integer> pages, HttpServletRequest request);

    DataExtractDTO intelligentDocumentExtraction(IdpFile idpFile);
    /**
     * 获取官网用户使用额度
     *
     * @param request request
     * @return 剩余额度
     */
    Integer getOverage(HttpServletRequest request);

    /**
     * 抽取文件
     *
     * @param fileId 抽取文件
     */
    DataExtractDTO apiExtractFile(String fileId);

    FileResultDTO apiResolveFile(String fileId);

    FileResultDTO fileResolve(IdpFile idpFile, FileParameterDTO fileParameter, HttpServletRequest request);

    FileResultDTO apiFileEditResolve(JsonEditDTO editDTO, HttpServletRequest request);

    FileResultDTO fileResolveApi(IdpFile idpFile, FileParameterDTO fileParameter, HttpServletRequest request);

    FileResultDTO apiSplitFile(String fileId);

    /**
     * 获取结果文件流返回
     *
     * @param id       文件ID
     * @param flag     是源文件还是结果文件，0：源文件，1：结果文件
     * @param response response
     */
    void getFileById(String id, Integer flag, HttpServletResponse response);

    void splitExport(List<String> fileIds, HttpServletResponse response);

    void layoutExport(FileExportDTO fileExportDTO, HttpServletResponse response);

    void extractExport(FileExportDTO fileExportDTO, HttpServletResponse response);

    /**
     * 手动分组文件到具体模板下
     * 分组完成后自动执行文件抽取任务
     *
     * @param fileManualGroupDTO fileManualGroupDTO
     */
    void fileManualGroup(FileManualGroupDTO fileManualGroupDTO);

    String testExtract(MultipartFile file, String extractTemplateDTO, HttpServletRequest request);
}
