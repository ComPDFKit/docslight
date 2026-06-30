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
@TableName("idp_task")
public class IdpTask extends BaseEntity {

    /**
     * 
     */
    private String folderPath;
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 
     */
    private String serviceIds;
    /**
     * 
     */
    private Integer status;

    private String params;
    private String type;
    private String userId;
}
