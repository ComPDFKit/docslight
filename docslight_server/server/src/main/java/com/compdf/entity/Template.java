package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("template")
public class Template extends BaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String leaderId;

    private String content;

    /**
     * 0 未启用，1 启用，2 删除
     */
    private Integer status;

    private String fileId;

}
