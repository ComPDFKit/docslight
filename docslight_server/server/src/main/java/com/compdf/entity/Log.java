package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log")
public class Log extends BaseEntity {

    /**
     *
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     *
     */
    private String userId;

    /**
     *
     */
    private String leaderId;

    /**
     *
     */
    private String actionType;

    /**
     *
     */
    private String relatedContent;

    /**
     *
     */
    private String actionDetail;

    private Long createTime;

    private Long updateTime;

}
