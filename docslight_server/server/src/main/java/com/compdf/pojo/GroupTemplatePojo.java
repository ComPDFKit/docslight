package com.compdf.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author ComPDFKit-WPH 2026/1/28
 */
@Data
@ToString
public class GroupTemplatePojo {

    private String groupTemplateId;

    private String groupId;

    private String templateId;

    private String templateName;

    private Integer order;

    /**
     * 模板状态：0 未启用，1 启用，2 删除
     */
    private Integer status;

    /**
     * 模板所属用户ID（"default" 表示默认模板）
     */
    private String leaderId;

    /**
     * 是否置顶
     */
    private Boolean pinned;

    /**
     * 置顶时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pinnedTime;

}
