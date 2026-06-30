package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.annotation.PermissionVerify;
import com.compdf.client.LoginClient;
import com.compdf.entity.ExtractFieldPojo;
import com.compdf.entity.IdpFile;
import com.compdf.entity.Template;
import com.compdf.entity.TemplatePin;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.enums.PermissionEnum;
import com.compdf.enums.TemplateStatusEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.mapper.TemplateMapper;
import com.compdf.mapper.TemplatePinMapper;
import com.compdf.pojo.ExtractTemplateDTO;
import com.compdf.pojo.ExtractTemplateV2DTO;
import com.compdf.pojo.GroupPojo;
import com.compdf.pojo.GroupTemplatePojo;
import com.compdf.properties.ResonacProperties;
import com.compdf.service.IdpFileService;
import com.compdf.service.TemplateService;
import com.compdf.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements TemplateService {

    private final TemplateMapper templateMapper;
    private final TemplatePinMapper templatePinMapper;
    private final LoginClient loginClient;
    private final ResonacProperties resonacProperties;
    @Autowired
    @Lazy
    private IdpFileService idpFileService;


    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE_CREATE)
    public String addTemplate(ExtractTemplateV2DTO extractTemplate, String userId, String name) {
        String leaderId = loginClient.getLeaderId();
        // 判断name小于50字符
        if (name.length() > 50) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NAME_TOO_LONG);
        }
        // 判断name是否重复
        if (templateMapper.selectCount(new LambdaQueryWrapper<Template>().eq(Template::getName, name)
                .eq(Template::getLeaderId, leaderId).ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())) > 0) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NAME_EXIST);
        }
        Map<String, ExtractFieldPojo> keys = extractTemplate.getKeys();
        int size;
        if (Objects.isNull(keys)) {
            size = 0;
        }else {
            size = keys.size();
        }
        Map<String, Map<String, ExtractFieldPojo>>  tableHandles = extractTemplate.getTableHeaders();
        if (!CollectionUtils.isEmpty(tableHandles)) {
            for (Map<String, ExtractFieldPojo> tableHandle : tableHandles.values()) {
                size += tableHandle.size();
            }
        }
        if (size > 50) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_KEY_TOO_MANY);
        }
        
        Template template = new Template();
        template.setName(name);
        template.setLeaderId(leaderId);
        template.setStatus(TemplateStatusEnum.ENABLED.getValue());
        templateMapper.insert(template);
        extractTemplate.setId(template.getId());
        extractTemplate.setStatus(template.getStatus());
        template.setContent(JsonUtils.getJsonString(extractTemplate));
        templateMapper.updateById(template);
        return template.getId();
    }

    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE_MODIFY)
    public void updateTemplate(ExtractTemplateV2DTO extractTemplate, String templateId, String name) {
        Template template = templateMapper.selectOne(new LambdaQueryWrapper<Template>().eq(Template::getId, templateId)
                .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue()));
        if (template == null) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }
        // 判断name小于50字符
        if (name.length() > 50) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NAME_TOO_LONG);
        }
        int size = extractTemplate.getKeys().size();
        for (Map<String, ExtractFieldPojo> tableHandle : extractTemplate.getTableHeaders().values()) {
            // size += tableHandle.values().stream().mapToInt(Map::size).sum();
            size += tableHandle.size();
        }
        if (size > 50) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_KEY_TOO_MANY);
        }
        if (template.getLeaderId().equals("default")) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }
        // 判断name是否重复
        if (templateMapper.selectCount(new LambdaQueryWrapper<Template>()
                .eq(Template::getName, name)
                .eq(Template::getLeaderId, template.getLeaderId()).ne(Template::getId, templateId)
                .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())) > 0) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NAME_EXIST);
        }
        extractTemplate.setFileId(template.getFileId());
        template.setContent(JsonUtils.getJsonString(extractTemplate));
        template.setName(name);
        templateMapper.updateById(template);
    }

    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE_DELETE)
    public void deleteTemplate(String templateId) {
        Template template = templateMapper.selectOne(new LambdaQueryWrapper<Template>().eq(Template::getId, templateId)
                .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue()));
        if (template == null) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }
        if (templateId.equals("1") || templateId.equals("2")) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }
        if (template.getLeaderId().equals("default")) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }
        List<GroupTemplatePojo> groupTemplatePojos = getGroupTemplateByTemplateId(templateId);
        if (!CollectionUtils.isEmpty(groupTemplatePojos)){
            groupTemplatePojos.forEach(groupTemplatePojo -> {
                List<IdpFile> idpFileList = idpFileService.getFileListByGroupTemplateId(groupTemplatePojo.getGroupTemplateId());
                if (!CollectionUtils.isEmpty(idpFileList)) {
                    throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_IN_FILE_EXISTS);
                }
            });
        }
        template.setStatus(TemplateStatusEnum.DELETED.getValue());
        templateMapper.updateById(template);
    }

    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE_MODIFY)
    public void enableTemplate(String templateId) {
        updateTemplateStatus(templateId, TemplateStatusEnum.ENABLED.getValue());
    }

    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE_MODIFY)
    public void disableTemplate(String templateId) {
        updateTemplateStatus(templateId, TemplateStatusEnum.DISABLED.getValue());
    }

    private List<GroupTemplatePojo> getGroupTemplateByTemplateId(String templateId) {
        return this.baseMapper.selectGroupTemplateByTemplateId(templateId);
    }


    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE)
    public List<Object> getTemplateList(String userId, String name) {
        List<Template> templates;
        LambdaQueryWrapper<Template> queryWrapper = new LambdaQueryWrapper<Template>()
                .eq(Template::getLeaderId, loginClient.getLeaderId())
                .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())
                .orderByAsc(Template::getCreateDate);
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Template::getName, name);
        }
        templates = templateMapper.selectList(queryWrapper);
        if (Objects.equals(loginClient.getLeaderId(), resonacProperties.getLeaderId())){
            return templates.stream().map(
                            this::toExtractTemplateDTO)
                    .collect(Collectors.toList());
        } else {
            return templates.stream().map(
                            this::toExtractTemplateV2DTO)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE)
    public List<ExtractTemplateV2DTO> getDefaultTemplate() {
        List<Template> templates;
        if(Objects.equals(loginClient.getLeaderId(), resonacProperties.getLeaderId())){
            templates = templateMapper.selectList(new LambdaQueryWrapper<Template>()
                    .eq(Template::getLeaderId, "default")
                    .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())
                    .in(Template::getId, "1","2")
                    .orderByAsc(Template::getCreateDate));
        }else {
            templates = templateMapper.selectList(new LambdaQueryWrapper<Template>()
                    .eq(Template::getLeaderId, "default")
                    .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())
                    .orderByAsc(Template::getCreateDate));
        }

        if (templates == null) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }

        return templates.stream().map(this::toExtractTemplateV2DTO).collect(Collectors.toList());
    }

    @Override
    @PermissionVerify(PermissionEnum.EXTRACT_TEMPLATE_MODIFY)
    @Transactional
    public void addTemplateFile(MultipartFile file, String templateId, Integer page) {
        Template template = this.baseMapper.selectById(templateId);
        ExtractTemplateV2DTO extractTemplateDTO = JsonUtils.jsonStringToBean(template.getContent(), ExtractTemplateV2DTO.class);
        String fileId = idpFileService.addTemplateFile(file);
        template.setFileId(fileId);
        extractTemplateDTO.setFileId(fileId);
        extractTemplateDTO.setPage(page);
        template.setContent(JsonUtils.getJsonString(extractTemplateDTO));
        this.baseMapper.updateById(template);
    }

    @Override
    public void addDefulotTemplateFile(File file, String templateName, Integer page) {
        Template template = this.baseMapper.selectOne(new LambdaQueryWrapper<Template>()
                .eq(Template::getName, templateName)
                .eq(Template::getLeaderId, "default")
                .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())
                .last("limit 1"));
        if (!StringUtils.isEmpty(template.getFileId())){
            return;
        }
        ExtractTemplateV2DTO extractTemplateDTO = JsonUtils.jsonStringToBean(template.getContent(), ExtractTemplateV2DTO.class);
        String fileId = idpFileService.addTemplateFile(file);
        template.setFileId(fileId);
        extractTemplateDTO.setFileId(fileId);
        extractTemplateDTO.setPage(page);
        template.setContent(JsonUtils.getJsonString(extractTemplateDTO));
        this.baseMapper.updateById(template);
    }

    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT)
    public String createTemplateGroup(String groupName) {
        // 参数校验
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new ComPDFKitException(ErrorInfoEnum.PARAM_VALIDATE_ERROR);
        }
        if (groupName.length() > 50) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NAME_TOO_LONG);
        }
        String id = UUID.randomUUID().toString();
        this.baseMapper.insertGroupInfo(id, groupName.trim(), loginClient.getLeaderId());
        return id;
    }

    @Transactional
    @Override
    @PermissionVerify(PermissionEnum.EXTRACT)
    public void insertGroupTemplate(String groupId, List<String> templateIds) {
        // 参数校验
        if (CollectionUtils.isEmpty(templateIds)) {
            throw new ComPDFKitException(ErrorInfoEnum.PARAM_VALIDATE_ERROR);
        }
        if (groupId == null || groupId.trim().isEmpty()) {
            groupId = createTemplateGroup("default");
        }
        List<GroupTemplatePojo> templatePojos = this.baseMapper.selectAllGroupTemplatesByGroupId(groupId);
        Set<String> existingTemplateIds = templatePojos.stream()
                .map(GroupTemplatePojo::getTemplateId)
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(existingTemplateIds)) {
            templateIds = templateIds.stream()
                    .filter(templateId -> !existingTemplateIds.contains(templateId))
                    .collect(Collectors.toList());
        }
        int order = 0;
        if (!CollectionUtils.isEmpty(templatePojos)) {
            order = templatePojos.get(templatePojos.size()-1).getOrder();
        }
        for (String templateId : templateIds) {
            int nextOrder = ++order;
            int restored = this.baseMapper.restoreGroupTemplate(groupId, templateId, nextOrder);
            if (restored == 0) {
                String id = UUID.randomUUID().toString();
                this.baseMapper.insertGroupTemplate(id, groupId, templateId, nextOrder);
            }
        }
    }

    @Override
    @PermissionVerify(PermissionEnum.EXTRACT)
    public List<GroupPojo> getGroupInfoByUserId() {
        String userId = loginClient.getUserId();
        String leaderId = loginClient.getLeaderId();
        List<GroupPojo> groupPojos = this.baseMapper.selectGroupInfoByUserId(leaderId);
        groupPojos = syncEnabledTemplatesToGroup(leaderId, groupPojos);
        if (CollectionUtils.isEmpty(groupPojos)) {
            return groupPojos;
        }
        // 批量查询所有模板，避免 N+1 查询问题
        List<String> groupIds = groupPojos.stream()
                .map(GroupPojo::getGroupId)
                .collect(Collectors.toList());
        List<GroupTemplatePojo> allTemplates = this.baseMapper.selectGroupTemplatesByGroupIds(groupIds, userId);
        // 按 groupId 分组
        Map<String, List<GroupTemplatePojo>> templateMap = allTemplates.stream()
                .collect(Collectors.groupingBy(GroupTemplatePojo::getGroupId));
        // 设置模板到对应的分组，并按置顶/自定义/默认分类
        groupPojos.forEach(groupPojo -> {
            List<GroupTemplatePojo> templates = templateMap.getOrDefault(groupPojo.getGroupId(), Collections.emptyList());

            // 分为三类：置顶、自定义、默认
            List<GroupTemplatePojo> pinned = new ArrayList<>();
            List<GroupTemplatePojo> custom = new ArrayList<>();
            List<GroupTemplatePojo> defaults = new ArrayList<>();

            for (GroupTemplatePojo t : templates) {
                if (Boolean.TRUE.equals(t.getPinned())) {
                    pinned.add(t);
                } else if ("default".equals(t.getLeaderId())) {
                    defaults.add(t);
                } else {
                    custom.add(t);
                }
            }

            // 置顶区按置顶时间倒序（SQL 已排序，这里保持）
            // 自定义和默认区按 order 升序（SQL 已排序，这里保持）
            groupPojo.setPinnedTemplates(pinned);
            groupPojo.setCustomTemplates(custom);
            groupPojo.setDefaultTemplates(defaults);

            // 兼容旧字段：合并三个列表（置顶在前，自定义次之，默认在后）
            List<GroupTemplatePojo> all = new ArrayList<>();
            all.addAll(pinned);
            all.addAll(custom);
            all.addAll(defaults);
            groupPojo.setGroupTemplates(all);
        });
        return groupPojos;
    }

    private List<GroupPojo> syncEnabledTemplatesToGroup(String leaderId, List<GroupPojo> groupPojos) {
        List<Template> availableTemplates = getAvailableGroupTemplates(leaderId);
        if (CollectionUtils.isEmpty(groupPojos)) {
            if (CollectionUtils.isEmpty(availableTemplates)) {
                return groupPojos;
            }
            String groupId = UUID.randomUUID().toString();
            this.baseMapper.insertGroupInfo(groupId, "default", leaderId);
            groupPojos = this.baseMapper.selectGroupInfoByUserId(leaderId);
        }

        Set<String> availableTemplateIds = availableTemplates.stream()
                .map(Template::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (GroupPojo groupPojo : groupPojos) {
            syncGroupTemplates(groupPojo.getGroupId(), availableTemplateIds);
        }
        return groupPojos;
    }

    private List<Template> getAvailableGroupTemplates(String leaderId) {
        List<Template> teamTemplates = templateMapper.selectList(new LambdaQueryWrapper<Template>()
                .eq(Template::getLeaderId, leaderId)
                .eq(Template::getStatus, TemplateStatusEnum.ENABLED.getValue())
                .orderByAsc(Template::getCreateDate));
        List<Template> defaultTemplates = templateMapper.selectList(new LambdaQueryWrapper<Template>()
                .eq(Template::getLeaderId, "default")
                .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())
                .orderByAsc(Template::getCreateDate));

        Map<String, Template> templateMap = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(teamTemplates)) {
            teamTemplates.forEach(template -> templateMap.put(template.getId(), template));
        }
        if (!CollectionUtils.isEmpty(defaultTemplates)) {
            defaultTemplates.forEach(template -> templateMap.putIfAbsent(template.getId(), template));
        }
        return new ArrayList<>(templateMap.values());
    }

    private void syncGroupTemplates(String groupId, Set<String> availableTemplateIds) {
        List<GroupTemplatePojo> existingTemplates = this.baseMapper.selectAllGroupTemplatesByGroupId(groupId);
        Set<String> existingTemplateIds = existingTemplates.stream()
                .map(GroupTemplatePojo::getTemplateId)
                .collect(Collectors.toSet());

        List<String> missingTemplateIds = availableTemplateIds.stream()
                .filter(templateId -> !existingTemplateIds.contains(templateId))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(missingTemplateIds)) {
            insertGroupTemplate(groupId, missingTemplateIds);
        }

        existingTemplates.stream()
                .map(GroupTemplatePojo::getTemplateId)
                .filter(templateId -> !availableTemplateIds.contains(templateId))
                .forEach(templateId -> this.baseMapper.deleteGroupTemplateByGroupIdAndTemplateId(groupId, templateId));
    }

    @Override
    public List<GroupTemplatePojo> getGroupTemplatesByGroupId(String groupId) {
        String userId = loginClient.getUserId();
        return this.baseMapper.selectGroupTemplatesByGroupId(groupId, userId);
    }

    @Override
    public List<GroupTemplatePojo> getGroupTemplatesByGroupTemplateIds(List<String> groupTemplateIds) {
        if (CollectionUtils.isEmpty(groupTemplateIds)) {
            return Collections.emptyList();
        }
        return this.baseMapper.selectGroupTemplatesByGroupTemplateIds(groupTemplateIds);
    }

    @Override
    public List<Template> getTemplateListByIds(List<String> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return Collections.emptyList();
        }
        return this.baseMapper.selectList(new LambdaQueryWrapper<Template>()
                .in(Template::getId, templateIds)
                .and(wrapper -> wrapper
                        .eq(Template::getStatus, TemplateStatusEnum.ENABLED.getValue())
                        .or()
                        .eq(Template::getLeaderId, "default")
                        .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue())));
    }

    @Override
    public GroupTemplatePojo getGroupTemplateByGroupTemplateId(String groupTemplateId) {
        return this.baseMapper.selectGroupTemplateByGroupTemplateId(groupTemplateId);
    }

    @Override
    public Template getTemplateByGroupTemplateId(String groupTemplateId) {
        return this.baseMapper.selectTemplateByGroupTemplateId(groupTemplateId);
    }

    @Override
    @Transactional
    public void deleteGroupTemplate(String groupTemplateId) {
        List<IdpFile> idpFileList = idpFileService.getFileListByGroupTemplateId(groupTemplateId);
        if (!CollectionUtils.isEmpty(idpFileList)) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_IN_FILE_EXISTS);
        }
        this.baseMapper.deleteGroupTemplate(groupTemplateId);
    }

    @Override
    public Object getTemplateById(String id) {
        Template template = this.baseMapper.selectById(id);
        if (template == null || Objects.equals(template.getStatus(), TemplateStatusEnum.DELETED.getValue())) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }
        if (!Objects.equals(template.getLeaderId(), resonacProperties.getLeaderId())) {
            return toExtractTemplateV2DTO(template);
        }
        return toExtractTemplateDTO(template);
    }

    @Override
    @Transactional
    public void pinTemplate(String groupTemplateId, String userId) {
        // 校验分组模板是否存在
        GroupTemplatePojo groupTemplate = this.baseMapper.selectGroupTemplateByGroupTemplateId(groupTemplateId);
        if (groupTemplate == null) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }

        // 检查是否已置顶
        TemplatePin existing = templatePinMapper.selectOne(new LambdaQueryWrapper<TemplatePin>()
                .eq(TemplatePin::getUserId, userId)
                .eq(TemplatePin::getGroupTemplateId, groupTemplateId));

        if (existing != null) {
            if (existing.getStatus() == 0) {
                // 之前取消过，重新置顶
                existing.setStatus(1);
                existing.setPinnedTime(LocalDateTime.now());
                templatePinMapper.updateById(existing);
            }
            // 已置顶，幂等返回
            return;
        }

        // 新增置顶记录
        TemplatePin pin = new TemplatePin();
        pin.setUserId(userId);
        pin.setGroupTemplateId(groupTemplateId);
        pin.setPinnedTime(LocalDateTime.now());
        pin.setStatus(1);
        templatePinMapper.insert(pin);
    }

    @Override
    @Transactional
    public void unpinTemplate(String groupTemplateId, String userId) {
        templatePinMapper.update(null, new LambdaUpdateWrapper<TemplatePin>()
                .eq(TemplatePin::getUserId, userId)
                .eq(TemplatePin::getGroupTemplateId, groupTemplateId)
                .eq(TemplatePin::getStatus, 1)
                .set(TemplatePin::getStatus, 0));
    }

    private void updateTemplateStatus(String templateId, Integer status) {
        Template template = templateMapper.selectOne(new LambdaQueryWrapper<Template>()
                .eq(Template::getId, templateId)
                .ne(Template::getStatus, TemplateStatusEnum.DELETED.getValue()));
        if (template == null) {
            throw new ComPDFKitException(ErrorInfoEnum.TEMPLATE_NOT_EXIST);
        }
        if (isDefaultTemplate(template)) {
            throw new ComPDFKitException(ErrorInfoEnum.DEFAULT_TEMPLATE_STATUS_NOT_MODIFY);
        }
        template.setStatus(status);
        templateMapper.updateById(template);
    }

    private ExtractTemplateDTO toExtractTemplateDTO(Template template) {
        ExtractTemplateDTO dto = JsonUtils.jsonStringToBean(template.getContent(), ExtractTemplateDTO.class);
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setFileId(template.getFileId());
        dto.setStatus(normalizeTemplateStatus(template));
        return dto;
    }

    private ExtractTemplateV2DTO toExtractTemplateV2DTO(Template template) {
        ExtractTemplateV2DTO dto = JsonUtils.jsonStringToBean(template.getContent(), ExtractTemplateV2DTO.class);
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setFileId(template.getFileId());
        dto.setStatus(normalizeTemplateStatus(template));
        return dto;
    }

    private Integer normalizeTemplateStatus(Template template) {
        return isDefaultTemplate(template) ? TemplateStatusEnum.ENABLED.getValue() : template.getStatus();
    }

    private boolean isDefaultTemplate(Template template) {
        return Objects.equals(template.getLeaderId(), "default");
    }


}
