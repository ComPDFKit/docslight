package com.compdf.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.IdpFile;
import com.compdf.enums.FileStatusEnum;
import com.compdf.enums.TaskTypeEnum;
import com.compdf.pojo.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface IdpFileService extends IService<IdpFile> {

    IdpFile selectByTaskId(String taskId, int retryCount);

    List<IdpFile> selectByTaskId(String taskId);

    IdpFile selectById(String fileId);

    Long selectCountByTaskIdAndStatus(String taskId, FileStatusEnum success, Integer minuteTime);

    String fileUpload(FileUploadPojo fileUploadPojo, String userId);

    void startFile(String taskId, FileParameterDTO fileParameter, TaskTypeEnum taskType);

    void startFile(List<String> idpFileIds, String fileParameter, TaskTypeEnum taskType);

    IdpFileInfoDTO getFileInfo(String fileId, HttpServletRequest request);

    /**
     * - 每一页抽取完成时候，存入Redis中<fileID:是否暂停:总页数:当前处理页>
     * - 提供根据文件ID来获取Redis中《总页数:当前处理页》信息并返回
     *
     * @param fileId 文件ID
     * @return FileScheduleDTO
     */
    FileScheduleDTO getFileSchedule(String fileId);

    /**
     * 文件暂停处理
     * @param fileIds fileId
     */
    void filePause(List<String> fileIds);

    void fileDelete(List<String> fileIds);

    List<IdpFileInfoDTO> getTaskFileList(String taskId, HttpServletRequest request);

    void downAllFiles(String taskId, HttpServletResponse response);

    IPage<IdpFileInfoDTO> getFileList(FileListQueryPojo queryPojo, String userId, HttpServletRequest request);

    List<IdpFileInfoDTO> getFileListByName(FileListQueryPojo queryPojo, HttpServletRequest request);

    IdpFileInfoDTO getFileInfoById(String fileId, HttpServletRequest request);

    List<IdpFile> selectByIds(List<String> fileIds);

    String addTemplateFile(MultipartFile file);

    String addTemplateFile(File localFile);

    List<IdpFileInfoDTO> getFileInfoByIds(List<String> fileIds, HttpServletRequest request);

    void confirmFileResult(String fileId, String newResult);

    void cancelConfirmFileResult(String fileId);

    List<IdpFile> getFileListByGroupTemplateId(String groupTemplateId);

    String selectLeaderIdByUserId(String userId);

    UserInfoPojo selectLeaderIdAndRoleByUserId(String userId);

    /**
     * 解析 在线API
     * 非PDF转成PDF处理
     * 验证登录身份，请求头api-key, RSA解密得到用户ID，验证权限，验证时效性
     *
     * @param file file
     * @param request request
     * @param response response
     */
    void parse(MultipartFile file, HttpServletRequest request, HttpServletResponse response);

    IdpFileInfoDTO parseOnlineAPIUrlMode(MultipartFile file, HttpServletRequest request, HttpServletResponse response);

    void convertToPdf(MultipartFile file, HttpServletRequest request, HttpServletResponse response);

    void parseAPI(MultipartFile file, HttpServletRequest request, HttpServletResponse response);

    void extractAPI(MultipartFile file, ExtractTemplateDTO extractTemplateDTO, List<Integer> pages, HttpServletRequest request, HttpServletResponse response);
}