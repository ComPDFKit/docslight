package com.compdf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.Template;
import com.compdf.pojo.ExtractTemplateV2DTO;
import com.compdf.pojo.GroupPojo;
import com.compdf.pojo.GroupTemplatePojo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
public interface TemplateService extends IService<Template> {


    @Transactional
    String addTemplate(ExtractTemplateV2DTO extractTemplate, String userId, String name);

    @Transactional
    void updateTemplate(ExtractTemplateV2DTO extractTemplate, String templateId, String name);

    @Transactional
    void deleteTemplate(String templateId);

    @Transactional
    void enableTemplate(String templateId);

    @Transactional
    void disableTemplate(String templateId);

    @Transactional
    List<Object> getTemplateList(String userId,String name);

    @Transactional
    List<ExtractTemplateV2DTO> getDefaultTemplate();

    @Transactional
    void addTemplateFile(MultipartFile file, String templateId, Integer page);

    void addDefulotTemplateFile(File file, String templateName, Integer page);

    @Transactional
    String createTemplateGroup(String groupName);

    @Transactional
    void insertGroupTemplate(String groupId, List<String> templateIds);

    List<GroupPojo> getGroupInfoByUserId();

    List<GroupTemplatePojo>  getGroupTemplatesByGroupId(String groupId);

    List<GroupTemplatePojo> getGroupTemplatesByGroupTemplateIds(List<String> groupTemplateIds);

    List<Template> getTemplateListByIds(List<String> templateIds);

    GroupTemplatePojo getGroupTemplateByGroupTemplateId(String groupTemplateId);

    Template getTemplateByGroupTemplateId(String groupTemplateId);

    @Transactional
    void deleteGroupTemplate(String groupTemplateId);

    Object getTemplateById(String id);

    /**
     * 置顶分组模板（用户维度，幂等操作）
     *
     * @param groupTemplateId 分组模板关联ID
     * @param userId          用户ID
     */
    void pinTemplate(String groupTemplateId, String userId);

    /**
     * 取消置顶分组模板（用户维度，幂等操作）
     *
     * @param groupTemplateId 分组模板关联ID
     * @param userId          用户ID
     */
    void unpinTemplate(String groupTemplateId, String userId);
}
