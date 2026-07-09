package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模板置顶配置表（用户维度）
 *
 * @author ComPDFKit 2026/06/01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("template_pin")
public class TemplatePin extends BaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 分组模板关联ID（group_template.id）
     */
    private String groupTemplateId;

    /**
     * 置顶时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pinnedTime;

    /**
     * 状态：1=置顶中, 0=已取消
     */
    private Integer status;
}
