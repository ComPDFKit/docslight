package com.compdf.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2026/1/28
 */
@Data
@ToString
public class GroupPojo {

    private String groupId;

    private String groupName;

    /**
     * 置顶模板列表（按置顶时间倒序）
     */
    List<GroupTemplatePojo> pinnedTemplates;

    /**
     * 自定义模板列表（按 order 升序）
     */
    List<GroupTemplatePojo> customTemplates;

    /**
     * 默认模板列表（按 order 升序）
     */
    List<GroupTemplatePojo> defaultTemplates;

    /**
     * 所有模板列表（兼容旧字段，包含置顶+自定义+默认，置顶模板不重复）
     */
    List<GroupTemplatePojo> groupTemplates;

}
